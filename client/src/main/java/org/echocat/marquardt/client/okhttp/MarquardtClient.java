/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.client.okhttp;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Request.Builder;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import okio.Buffer;
import org.echocat.marquardt.client.Client;
import org.echocat.marquardt.client.util.Md5Creator;
import org.echocat.marquardt.client.util.ResponseStatusTranslation;
import org.echocat.marquardt.common.CertificateValidator;
import org.echocat.marquardt.common.domain.ClientInformation;
import org.echocat.marquardt.common.domain.Credentials;
import org.echocat.marquardt.common.domain.DeserializingFactory;
import org.echocat.marquardt.common.domain.SignUpAccountData;
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
import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.codec.binary.Base64.decodeBase64;
import static org.apache.commons.codec.binary.Base64.encodeBase64URLSafeString;
import static org.echocat.marquardt.common.web.RequestHeaders.X_SIGNATURE;

/**
 * OkHttp implementation of the client.
 *
 * @param <SIGNABLE> type of the payload contained in the certificate.
 */
public class MarquardtClient<SIGNABLE extends Signable, ROLE extends Role> implements Client<SIGNABLE> {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private static final int OK_STATUS = 200;
    private static final int CREATED_STATUS = 201;
    private static final int NO_CONTENT_STATUS = 204;

    private static final String POST_METHOD = "POST";
    private static final String GET_METHOD = "GET";
    private static final String PUT_METHOD = "PUT";
    private static final String DELETE_METHOD = "DELETE";
    private static final String ACCEPT_LANGUAGE_HEADER = "Accept-Language";

    private final OkHttpClient _httpClient = new OkHttpClient();
    private final OkHttpClient _addSignedHeaderHttpClient = new OkHttpClient();

    private final String _baseUri;
    private final DeserializingFactory<SIGNABLE> _deserializingFactory;
    private final CertificateValidator<SIGNABLE, ROLE> _certificateValidator;
    private final RequestSigner _requestSigner = new RequestSigner();
    private final KeyPairProvider _clientKeyProvider;
    private DateProvider _dateProvider = new DateProvider();

    private static final Gson GSON = new GsonBuilder().registerTypeAdapter(PublicKey.class, new PublicKeyAdapter()).create();
    private Locale _locale = Locale.getDefault();

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
        _addSignedHeaderHttpClient.networkInterceptors().add(
                new Interceptor() {
                    @Override
                    public Response intercept(final Chain chain) throws IOException {
                        final Request originalRequest = chain.request();
                        final Buffer requestBodyBuffer = new Buffer();
                        if (originalRequest.body() != null) {
                            originalRequest.body().writeTo(requestBodyBuffer);
                        }
                        final Request requestWithContentMd5 = originalRequest.newBuilder()
                                .addHeader(SignatureHeaders.CONTENT.getHeaderName(), encodeBase64URLSafeString(Md5Creator.create(requestBodyBuffer.readByteArray())))
                                .build();

                        final Request requestWithSignature = requestWithContentMd5.newBuilder()
                                .addHeader(X_SIGNATURE, new String(_requestSigner.getSignature(requestWithContentMd5, _clientKeyProvider.getPrivateKey())))
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
     * Used to change timeouts of marquardt client.
     */
    public void setTimeouts(final int connectTimeout,
                            final int readTimeout,
                            final int writeTimeout,
                            final TimeUnit timeUnit) {
        for (final OkHttpClient client : Lists.newArrayList(_httpClient, _addSignedHeaderHttpClient)) {
            client.setConnectTimeout(connectTimeout, timeUnit);
            client.setReadTimeout(readTimeout, timeUnit);
            client.setWriteTimeout(writeTimeout, timeUnit);
        }
    }

    /**
     * Used for internal (testing) purposes only.
     */
    public void setDateProvider(final DateProvider dateProvider) {
        _dateProvider = dateProvider;
        _certificateValidator.setDateProvider(_dateProvider);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Certificate<SIGNABLE> initializeSignUp(final ClientInformation clientInformation) throws IOException {
        final Request request = postRequestWithJsonObjectParameter(_baseUri + "/auth/initializeSignUp", clientInformation);
        final Response response = _httpClient.newCall(request).execute();
        if (response.code() != CREATED_STATUS) {
            throw ResponseStatusTranslation.from(response.code()).translateToException(response.message());
        }
        return extractCertificateFrom(response);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Certificate<SIGNABLE> finalizeSignUp(final Certificate<SIGNABLE> certificate, final SignUpAccountData<? extends Credentials> signUpAccountData) throws IOException {
        final Request request = sendRequestWithCertificateHeader(_baseUri + "/auth/finalizeSignUp", POST_METHOD, certificate, signUpAccountData);
        final Response response = _addSignedHeaderHttpClient.newCall(request).execute();
        if (response.code() != OK_STATUS) {
            throw ResponseStatusTranslation.from(response.code()).translateToException(response.message());
        }
        return extractCertificateFrom(response);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Certificate<SIGNABLE> signIn(final Credentials credentials) throws IOException {
        final Request request = postRequestWithJsonObjectParameter(_baseUri + "/auth/signIn", credentials);
        final Response response = _httpClient.newCall(request).execute();
        if (response.code() != OK_STATUS) {
            throw ResponseStatusTranslation.from(response.code()).translateToException(response.message());
        }
        return extractCertificateFrom(response);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean signOut(final Certificate<SIGNABLE> certificate) throws IOException {
        final Request request = sendRequestWithCertificateHeader(_baseUri + "/auth/signOut", POST_METHOD, certificate);
        final Response response = _addSignedHeaderHttpClient.newCall(request).execute();
        if (response.code() != NO_CONTENT_STATUS) {
            throw ResponseStatusTranslation.from(response.code()).translateToException(response.message());
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Certificate<SIGNABLE> refresh(final Certificate<SIGNABLE> certificateToRefesh) throws IOException {
        final Request request = sendRequestWithCertificateHeader(_baseUri + "/auth/refresh", POST_METHOD, certificateToRefesh);
        final Response response = _addSignedHeaderHttpClient.newCall(request).execute();
        if (response.code() != OK_STATUS) {
            throw ResponseStatusTranslation.from(response.code()).translateToException(response.message());
        }
        return extractCertificateFrom(response);
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
        final Request request = sendRequestWithCertificateHeader(url, httpMethod, certificate, payload);
        final Response response = _addSignedHeaderHttpClient.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw ResponseStatusTranslation.from(response.code()).translateToException(response.message());
        }
        return GSON.fromJson(response.body().string(), responseType);
    }

    @Override
    public void setLocale(Locale locale) {
        _locale = locale;
    }

    private Certificate<SIGNABLE> extractCertificateFrom(final Response response) throws IOException {
        final JsonElement certificateJsonElement = GSON.fromJson(response.body().string(), JsonObject.class).get("certificate");
        final byte[] certificate = decodeBase64(certificateJsonElement.getAsString());
        final Certificate<SIGNABLE> deserializedCertificate = _certificateValidator.deserializeAndValidateCertificate(certificate);
        if (!deserializedCertificate.getClientPublicKey().equals(_clientKeyProvider.getPublicKey())) {
            throw new InvalidCertificateException("certificate key does not match my public key");
        }
        return deserializedCertificate;
    }

    private Request postRequestWithJsonObjectParameter(final String url, final Object jsonObject) {
        final RequestBody body = RequestBody.create(JSON, GSON.toJson(jsonObject));
        return new Request.Builder()
                .url(url)
                .post(body)
                .addHeader(ACCEPT_LANGUAGE_HEADER, _locale.toLanguageTag())
                .build();
    }

    private Request sendRequestWithCertificateHeader(final String url, final String httpMethod, final Certificate<SIGNABLE> certificate) throws IOException {
        return sendRequestWithCertificateHeader(url, httpMethod, certificate, "");
    }

    private Request sendRequestWithCertificateHeader(final String url, final String httpMethod, final Certificate<SIGNABLE> certificate, final Object bodyContent) throws IOException {
        final RequestBody body = RequestBody.create(JSON, GSON.toJson(bodyContent));
        final Builder builder = new Builder().url(url);
        setRequestHttpMethod(httpMethod, body, builder);
        return builder
                .addHeader(SignatureHeaders.X_CERTIFICATE.getHeaderName(), encodeBase64URLSafeString(certificate.getContent()))
                .addHeader(ACCEPT_LANGUAGE_HEADER, _locale.toLanguageTag())
                .build();
    }

    private void setRequestHttpMethod(final String httpMethod, final RequestBody body, final Builder builder) {
        if (httpMethod.equalsIgnoreCase(GET_METHOD)) {
            builder.get();
        } else if (httpMethod.equalsIgnoreCase(POST_METHOD)) {
            builder.post(body);
        } else if (httpMethod.equalsIgnoreCase(PUT_METHOD)) {
            builder.put(body);
        } else if (httpMethod.equalsIgnoreCase(DELETE_METHOD)) {
            builder.delete(body);
        } else {
            throw new IllegalArgumentException("HttpMethod " + httpMethod + " is not supported by this client.");
        }
    }
}