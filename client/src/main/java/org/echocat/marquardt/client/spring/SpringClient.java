/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.client.spring;

import org.echocat.marquardt.client.Client;
import org.echocat.marquardt.client.util.Md5Creator;
import org.echocat.marquardt.client.util.ResponseStatusTranslation;
import org.echocat.marquardt.common.CertificateValidator;
import org.echocat.marquardt.common.domain.Certificate;
import org.echocat.marquardt.common.domain.Credentials;
import org.echocat.marquardt.common.domain.DeserializingFactory;
import org.echocat.marquardt.common.domain.KeyPairProvider;
import org.echocat.marquardt.common.domain.Role;
import org.echocat.marquardt.common.domain.Signable;
import org.echocat.marquardt.common.exceptions.InvalidCertificateException;
import org.echocat.marquardt.common.serialization.RolesDeserializer;
import org.echocat.marquardt.common.util.DateProvider;
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

import java.io.IOException;
import java.security.PublicKey;
import java.util.List;

import static org.apache.commons.codec.binary.Base64.encodeBase64;
import static org.apache.commons.codec.binary.Base64.encodeBase64URLSafeString;

/**
 * Spring implementation of the client.
 *
 * @param <SIGNABLE> type of the payload contained in the certificate.
 */
public class SpringClient<SIGNABLE extends Signable, ROLE extends Role> implements Client<SIGNABLE> {

    private final RestTemplate _restTemplate = new RestTemplate();
    private final RestTemplate _authorizedRestTemplate = new RestTemplate();
    private final String _baseUri;
    private final DeserializingFactory<SIGNABLE> _deserializingFactory;

    private final CertificateValidator<SIGNABLE, ROLE> _certificateValidator;

    private final RequestSigner _requestSigner = new RequestSigner();

    private final KeyPairProvider _clientKeyProvider;
    private DateProvider _dateProvider = new DateProvider();
    private byte[] _certificate;

    /**
     * Create a client instance.
     *
     * @param baseUri              base uri of the authority.
     * @param deserializingFactory factory used to deserialize the payload with type SIGNABLE.
     * @param roleRolesDeserializer RolesDeserializer for your roles implementation.
     * @param clientKeyProvider    key provider that returns the client's public/private key pair.
     * @param trustedKeys          a list of pre-shared, trusted keys used by the authority to sign certificates. The client uses this list to verify the authenticity of certificates.
     */
    public SpringClient(final String baseUri,
                        final DeserializingFactory<SIGNABLE> deserializingFactory,
                        final RolesDeserializer<ROLE> roleRolesDeserializer,
                        final KeyPairProvider clientKeyProvider,
                        final List<PublicKey> trustedKeys) {
        _baseUri = baseUri;
        _deserializingFactory = deserializingFactory;
        _clientKeyProvider = clientKeyProvider;
        _authorizedRestTemplate.getInterceptors().add(
                new ClientHttpRequestInterceptor() {
                    @Override
                    public ClientHttpResponse intercept(final HttpRequest httpRequest,
                                                        final byte[] bytes,
                                                        final ClientHttpRequestExecution clientHttpRequestExecution) throws IOException {
                        HttpHeaders headers = httpRequest.getHeaders();
                        headers.add(SignatureHeaders.CONTENT.getHeaderName(), encodeBase64URLSafeString(Md5Creator.create(bytes)));
                        headers.add(SignatureHeaders.X_CERTIFICATE.getHeaderName(), new String(encodeBase64(_certificate)));
                        headers.add("X-Signature", new String(_requestSigner.getSignature(httpRequest, _clientKeyProvider.getPrivateKey())));
                        return clientHttpRequestExecution.execute(httpRequest, bytes);
                    }
                });
        _certificateValidator = new CertificateValidator<SIGNABLE, ROLE>(trustedKeys) {
            @Override
            protected DeserializingFactory<SIGNABLE> deserializingFactory() {
                return _deserializingFactory;
            }

            @Override
            protected RolesDeserializer<ROLE> roleCodeDeserializer() {
                return roleRolesDeserializer;
            }
        };
        _certificateValidator.setDateProvider(_dateProvider);
    }

    /**
     * Used for internal (testing) purposes only.
     *
     * @param dateProvider
     */
    public void setDateProvider(DateProvider dateProvider) {
        _dateProvider = dateProvider;
        _certificateValidator.setDateProvider(_dateProvider);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Certificate<SIGNABLE> signup(final Credentials credentials) throws IOException {
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

    private Certificate<SIGNABLE> extractCertificateFrom(ResponseEntity<JsonWrappedCertificate> response) {
        Certificate<SIGNABLE> deserializedCertificate = _certificateValidator.deserializeAndValidateCertificate(response.getBody().getCertificate());
        if (!deserializedCertificate.getClientPublicKey().equals(_clientKeyProvider.getPublicKey())) {
            throw new InvalidCertificateException("certificate key does not match my public key");
        }
        _certificate = response.getBody().getCertificate();
        return deserializedCertificate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Certificate<SIGNABLE> signin(final Credentials credentials) throws IOException {
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Certificate<SIGNABLE> refresh() throws IOException {

        final ResponseEntity<JsonWrappedCertificate> response;
        try {
            response = _authorizedRestTemplate.postForEntity(_baseUri + "/auth/refresh/", null, JsonWrappedCertificate.class);
        } catch (HttpClientErrorException e) {
            throw ResponseStatusTranslation.from(e.getStatusCode().value()).translateToException(e.getMessage());
        }
        return extractCertificateFrom(response);
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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

}