/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.client.okhttp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.squareup.okhttp.*;
import okio.Buffer;
import org.echocat.marquardt.client.Client;
import org.echocat.marquardt.client.util.Md5Creator;
import org.echocat.marquardt.client.util.ResponseStatusTranslation;
import org.echocat.marquardt.common.CertificateValidator;
import org.echocat.marquardt.common.domain.Credentials;
import org.echocat.marquardt.common.domain.DeserializingFactory;
import org.echocat.marquardt.common.domain.Signable;
import org.echocat.marquardt.common.domain.certificate.Certificate;
import org.echocat.marquardt.common.domain.certificate.Role;
import org.echocat.marquardt.common.exceptions.InvalidCertificateException;
import org.echocat.marquardt.common.keyprovisioning.KeyPairProvider;
import org.echocat.marquardt.common.serialization.RolesDeserializer;
import org.echocat.marquardt.common.util.DateProvider;
import org.echocat.marquardt.common.web.SignatureHeaders;

import java.io.IOException;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Collection;

import static org.apache.commons.codec.binary.Base64.encodeBase64URLSafeString;

/**
 * OkHttp implementation of the client.
 *
 * @param <SIGNABLE> type of the payload contained in the certificate.
 */
public class MarquardtClient<SIGNABLE extends Signable, ROLE extends Role> implements Client<SIGNABLE> {

    private static final int OK_STATUS = 200;
    private static final int NO_CONTENT_STATUS = 204;
    private static final int CREATED_STATUS = 201;

    private final OkHttpClient _httpClient = new OkHttpClient();
    private final OkHttpClient _headerSignedHttpClient = new OkHttpClient();

    private final String _baseUri;

    private final DeserializingFactory<SIGNABLE> _deserializingFactory;
    private final CertificateValidator<SIGNABLE, ROLE> _certificateValidator;
    private final RequestSigner _requestSigner = new RequestSigner();
    private final KeyPairProvider _clientKeyProvider;
    private DateProvider _dateProvider = new DateProvider();
    private static final Gson GSON = new GsonBuilder().registerTypeAdapter(PublicKey.class, new PublicKeyAdapter()).create();

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    /**
     * Create a client instance.
     *
     * @param baseUri               base uri of the authority.
     * @param deserializingFactory  factory used to deserialize the payload with type SIGNABLE.
     * @param roleRolesDeserializer RolesDeserializer for your roles implementation.
     * @param clientKeyProvider     key provider that returns the client's public/private key pair.
     * @param trustedKeys           a collection of pre-shared, trusted keys used by the authority to sign certificates. The client uses this list to verify the authenticity of certificates.
     */
    public MarquardtClient(final String baseUri,
                           final DeserializingFactory<SIGNABLE> deserializingFactory,
                           final RolesDeserializer<ROLE> roleRolesDeserializer,
                           final KeyPairProvider clientKeyProvider,
                           final Collection<PublicKey> trustedKeys) {
        _baseUri = baseUri;
        _deserializingFactory = deserializingFactory;
        _clientKeyProvider = clientKeyProvider;
        _headerSignedHttpClient.networkInterceptors().add(
                new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request originalRequest = chain.request();
                        final Buffer requestBodyBuffer = new Buffer();
                        originalRequest.body().writeTo(requestBodyBuffer);
                        Request requestWithContentMd5 = originalRequest.newBuilder()
                                .addHeader(SignatureHeaders.CONTENT.getHeaderName(), encodeBase64URLSafeString(Md5Creator.create(requestBodyBuffer.readByteArray())))
                                .build();

                        Request requestWithSignature = requestWithContentMd5.newBuilder()
                                .addHeader("X-Signature", new String(_requestSigner.getSignature(requestWithContentMd5, _clientKeyProvider.getPrivateKey())))
                                .build();
                        return chain.proceed(requestWithSignature);
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
        Response response;
        Request request = postRequestWithCredentialsParameter(_baseUri + "/auth/signup", credentials);
        response = _httpClient.newCall(request).execute();
        if (response.code() != CREATED_STATUS) {
            throw ResponseStatusTranslation.from(response.code()).translateToException(response.message());
        }
        return extractCertificateFrom(response);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Certificate<SIGNABLE> signin(final Credentials credentials) throws IOException {
        Response response;
        Request request = postRequestWithCredentialsParameter(_baseUri + "/auth/signin", credentials);
        response = _httpClient.newCall(request).execute();
        if (response.code() != OK_STATUS) {
            throw ResponseStatusTranslation.from(response.code()).translateToException(response.message());
        }
        return extractCertificateFrom(response);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Certificate<SIGNABLE> refresh(Certificate<SIGNABLE> certificateToRefesh) throws IOException {
        Response response;
        Request request = postRequestWithCertificateHeader(_baseUri + "/auth/refresh", certificateToRefesh);
        response = _headerSignedHttpClient.newCall(request).execute();
        if (response.code() != OK_STATUS) {
            throw ResponseStatusTranslation.from(response.code()).translateToException(response.message());
        }
        return extractCertificateFrom(response);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean signout(Certificate<SIGNABLE> certificate) throws IOException {
        Response response;
        Request request = postRequestWithCertificateHeader(_baseUri + "/auth/signout", certificate);
        response = _headerSignedHttpClient.newCall(request).execute();
        if (response.code() != NO_CONTENT_STATUS) {
            throw ResponseStatusTranslation.from(response.code()).translateToException(response.message());
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <REQUEST, RESPONSE> RESPONSE sendSignedPayloadTo(final String url,
                                                            final String httpMethod,
                                                            final REQUEST payload,
                                                            final Class<RESPONSE> responseType,
                                                            final Certificate<SIGNABLE> certificate) throws IOException {
        Request request = postRequestWithCertificateHeader(url, certificate, payload);
        Response response = _headerSignedHttpClient.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw ResponseStatusTranslation.from(response.code()).translateToException(response.message());
        }
        return GSON.fromJson(response.body().string(), responseType);
    }

    private Certificate<SIGNABLE> extractCertificateFrom(Response response) throws IOException {
        JsonElement certificateJsonElement = GSON.fromJson(response.body().string(), JsonObject.class).get("certificate");
        final byte[] certificate = Base64.getDecoder().decode(certificateJsonElement.getAsString());
        final Certificate<SIGNABLE> deserializedCertificate = _certificateValidator.deserializeAndValidateCertificate(certificate);
        if (!deserializedCertificate.getClientPublicKey().equals(_clientKeyProvider.getPublicKey())) {
            throw new InvalidCertificateException("certificate key does not match my public key");
        }
        return deserializedCertificate;
    }

    private Request postRequestWithCredentialsParameter(String url, Credentials credentials) {
        RequestBody body = RequestBody.create(JSON, GSON.toJson(credentials));
        return new Request.Builder()
                .url(url)
                .post(body)
                .build();
    }

    private Request postRequestWithCertificateHeader(String url, Certificate<SIGNABLE> certificate) throws IOException {
        return postRequestWithCertificateHeader(url, certificate, "");
    }

    private Request postRequestWithCertificateHeader(String url, Certificate<SIGNABLE> certificate, Object bodyContent) throws IOException {
        RequestBody body = RequestBody.create(JSON, GSON.toJson(bodyContent));
        return new Request.Builder()
                .url(url)
                .post(body)
                .addHeader(SignatureHeaders.X_CERTIFICATE.getHeaderName(), encodeBase64URLSafeString(certificate.getContent()))
                .build();
    }
}