/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.client.spring;

import com.google.common.base.Function;
import org.echocat.marquardt.client.Client;
import org.echocat.marquardt.client.util.Md5Creator;
import org.echocat.marquardt.client.util.ResponseStatusTranslation;
import org.echocat.marquardt.common.ContentValidator;
import org.echocat.marquardt.common.domain.Certificate;
import org.echocat.marquardt.common.domain.Credentials;
import org.echocat.marquardt.common.domain.DeserializingFactory;
import org.echocat.marquardt.common.domain.Signable;
import org.echocat.marquardt.common.web.JsonWrappedCertificate;
import org.echocat.marquardt.common.web.SignatureHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Nullable;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

public class SpringClient<T extends Signable> implements Client<T> {

    private final RestTemplate _restTemplate = new RestTemplate();
    private final RestTemplate _authorizedRestTemplate = new RestTemplate();
    private final String _baseUri;
    private final DeserializingFactory<T> _deserializingFactory;

    private final ContentValidator _signedContentValidator = new ContentValidator();
    private final RequestSigner _requestSigner = new RequestSigner();

    private final PrivateKey _privateKey;
    private byte[] _certificate;

    public SpringClient(final String baseUri,
                        final DeserializingFactory<T> deserializingFactory,
                        final PrivateKey privateKey) {
        _baseUri = baseUri;
        _deserializingFactory = deserializingFactory;
        _privateKey = privateKey;
        _authorizedRestTemplate.getInterceptors().add(
                new ClientHttpRequestInterceptor() {
                    @Override
                    public ClientHttpResponse intercept(final HttpRequest httpRequest,
                                                        final byte[] bytes,
                                                        final ClientHttpRequestExecution clientHttpRequestExecution) throws IOException {
                        HttpHeaders headers = httpRequest.getHeaders();
                        headers.add(SignatureHeaders.CONTENT.getHeaderName(), Base64.getEncoder().encodeToString(Md5Creator.create(bytes)));
                        headers.add(SignatureHeaders.X_CERTIFICATE.getHeaderName(), new String(Base64.getEncoder().encode(_certificate)));
                        headers.add("X-Signature", new String(_requestSigner.getSignature(httpRequest, _privateKey)));
                        return clientHttpRequestExecution.execute(httpRequest, bytes);
                    }
                });
    }

    @Override
    public Certificate<T> signup(final Credentials credentials) throws IOException {
        final ResponseEntity<JsonWrappedCertificate> response;
        try {
            response = _restTemplate.postForEntity(_baseUri + "/auth/signup/", credentials, JsonWrappedCertificate.class);
        } catch (HttpClientErrorException e) {
            throw ResponseStatusTranslation.from(e.getStatusCode().value()).translateToException(e.getMessage());
        }
        if (response.getStatusCode() == HttpStatus.CREATED) {
            return extractCertificateFrom(response);
        } else {
            return null;
        }
    }

    @Override
    public Certificate<T> signin(final Credentials credentials) throws IOException {
        final ResponseEntity<JsonWrappedCertificate> response;
        try {
            response = _restTemplate.postForEntity(_baseUri + "/auth/signin/", credentials, JsonWrappedCertificate.class);
        } catch (HttpClientErrorException e) {
            throw ResponseStatusTranslation.from(e.getStatusCode().value()).translateToException(e.getMessage());
        }
        if (response.getStatusCode() == HttpStatus.OK) {
            return extractCertificateFrom(response);
        } else {
            return null;
        }
    }

    @Override
    public Certificate<T> refresh() throws IOException {

        final ResponseEntity<JsonWrappedCertificate> response;
        try {
            response = _authorizedRestTemplate.postForEntity(_baseUri + "/auth/refresh/", null, JsonWrappedCertificate.class);
        } catch (HttpClientErrorException e) {
            throw ResponseStatusTranslation.from(e.getStatusCode().value()).translateToException(e.getMessage());
        }
        return extractCertificateFrom(response);
    }

    @Override
    public boolean signout() throws IOException {
        final ResponseEntity<Void> response;
        try {
            response = _authorizedRestTemplate.postForEntity(_baseUri + "/auth/signout/", null, Void.class);
        } catch (HttpClientErrorException e) {
            throw ResponseStatusTranslation.from(e.getStatusCode().value()).translateToException(e.getMessage());
        }
        return response.getStatusCode() == HttpStatus.NO_CONTENT;
    }

    @Override
    public <REQUEST, RESPONSE> RESPONSE sendSignedPayloadTo(final String url,
                                                                            final String httpMethod,
                                                                            final REQUEST payload,
                                                                            final Class<RESPONSE> responseType) {
        try {
            final ResponseEntity<RESPONSE> exchange =
                    _authorizedRestTemplate.exchange(
                            url, HttpMethod.valueOf(httpMethod.toUpperCase()), new HttpEntity<REQUEST>(payload), responseType);
            return exchange.getBody();
        } catch (HttpClientErrorException e) {
            throw ResponseStatusTranslation.from(e.getStatusCode().value()).translateToException(e.getMessage());
        }
    }

    private Certificate<T> extractCertificateFrom(ResponseEntity<JsonWrappedCertificate> response) throws IOException {
        _certificate = response.getBody().getCertificate();
        return _signedContentValidator.deserializeCertificateAndValidateSignature(_certificate, _deserializingFactory, new Function<Certificate<T>, PublicKey>() {
            @Nullable
            @Override
            public PublicKey apply(final Certificate<T> certificate) {
                return certificate.getIssuerPublicKey();
            }
        });
    }
}