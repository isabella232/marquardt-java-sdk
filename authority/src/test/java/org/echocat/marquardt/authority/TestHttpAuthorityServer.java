/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.authority;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.echocat.marquardt.authority.persistence.SessionStore;
import org.echocat.marquardt.authority.persistence.UserStore;
import org.echocat.marquardt.authority.policies.ClientAccessPolicy;
import org.echocat.marquardt.authority.policies.SessionCreationPolicy;
import org.echocat.marquardt.authority.session.ExpiryDateCalculatorImpl;
import org.echocat.marquardt.authority.testdomain.TestSession;
import org.echocat.marquardt.authority.testdomain.TestSignUpAccountData;
import org.echocat.marquardt.authority.testdomain.TestUser;
import org.echocat.marquardt.authority.testdomain.TestUserCredentials;
import org.echocat.marquardt.common.TestKeyPairProvider;
import org.echocat.marquardt.common.domain.Signature;
import org.echocat.marquardt.common.web.JsonWrappedCertificate;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import static org.apache.commons.codec.binary.Base64.decodeBase64;
import static org.echocat.marquardt.common.web.SignatureHeaders.X_CERTIFICATE;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestHttpAuthorityServer {

    private final HttpServer _server;
    private final ObjectMapper _objectMapper;
    private final Authority<TestUser, TestSession, TestUserCredentials, TestSignUpAccountData> _authority;
    private final Signature _signature = mock(Signature.class);

    public TestHttpAuthorityServer(final UserStore<TestUser, TestUserCredentials, TestSignUpAccountData> userStore, final SessionStore<TestSession> sessionStore, final SessionCreationPolicy sessionAccess, final ClientAccessPolicy clientAccessPolicy) throws IOException {
        _server = HttpServer.create(new InetSocketAddress(8000), 0);
        _objectMapper = new ObjectMapper();
        _authority = new Authority<>(userStore, sessionStore, sessionAccess, clientAccessPolicy, TestKeyPairProvider.create(), new ExpiryDateCalculatorImpl<>());
        when(_signature.isValidFor(any(), any())).thenReturn(true);
    }

    public void start() throws IOException {
        _server.createContext("/signup", new SignupHandler(201));
        _server.createContext("/signin", new SigninHandler(200));
        _server.createContext("/signout", new SignoutHandler(204));
        _server.createContext("/refresh", new RefreshHandler(200));
        _server.setExecutor(null);
        _server.start();
    }

    public void stop() throws InterruptedException {
        _server.stop(0);
        Thread.sleep(100L);
    }

    class SignupHandler extends Handler {

        public SignupHandler(final Integer successResponseCode) {
            super(successResponseCode);
        }

        @Override
        String getResponse(final InputStream requestBody, final Headers headers)  throws IOException {
            final TestUserCredentials testUserCredentials = _objectMapper.readValue(requestBody, TestUserCredentials.class);
            final TestSignUpAccountData signUpAccountData = new TestSignUpAccountData();
            signUpAccountData.setCredentials(testUserCredentials);
            final JsonWrappedCertificate jsonWrappedCertificate = createCertificateResponse(_authority.signUp(signUpAccountData));
            return _objectMapper.writeValueAsString(jsonWrappedCertificate);
        }
    }

    class SigninHandler extends Handler {

        public SigninHandler(final Integer successResponseCode) {
            super(successResponseCode);
        }

        @Override
        String getResponse(final InputStream requestBody, final Headers headers)  throws IOException {
            final TestUserCredentials testUserCredentials = _objectMapper.readValue(requestBody, TestUserCredentials.class);
            final JsonWrappedCertificate jsonWrappedCertificate = createCertificateResponse(_authority.signIn(testUserCredentials));
            return _objectMapper.writeValueAsString(jsonWrappedCertificate);
        }
    }

    class SignoutHandler extends Handler {
        public SignoutHandler(final Integer successResponseCode) {
            super(successResponseCode);
        }

        @Override
        String getResponse(final InputStream requestBody, final Headers headers)  throws IOException {
            final byte[] certificate = decodeBase64(headers.get(X_CERTIFICATE.getHeaderName()).get(0));
            _authority.signOut(certificate, certificate, _signature);
            return null;
        }
    }

    class RefreshHandler extends Handler {

        public RefreshHandler(final Integer successResponseCode) {
            super(successResponseCode);
        }
        @Override
        String getResponse(final InputStream requestBody, final Headers headers) throws IOException {
            final byte[] certificate = decodeBase64(headers.get(X_CERTIFICATE.getHeaderName()).get(0));
            final JsonWrappedCertificate refresh = createCertificateResponse(_authority.refresh(certificate, certificate, _signature));
            return _objectMapper.writeValueAsString(refresh);
        }
    }

    abstract class Handler implements HttpHandler {

        private final Integer _successResponseCode;

        public Handler(final Integer successResponseCode) {
            _successResponseCode = successResponseCode;
        }

        @Override
        public void handle(final HttpExchange httpExchange) throws IOException {
            if (!"POST".equals(httpExchange.getRequestMethod())) {
                httpExchange.sendResponseHeaders(405, 0);
            } else if (!"application/json".equals(httpExchange.getRequestHeaders().get("Content-Type").get(0))) {
                httpExchange.sendResponseHeaders(415, 0);
            }
            final String response = getResponse(httpExchange.getRequestBody(), httpExchange.getRequestHeaders());
            if (response != null) {
                httpExchange.sendResponseHeaders(_successResponseCode, response.length());
                final OutputStream os = httpExchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                httpExchange.sendResponseHeaders(_successResponseCode, -1);
            }
        }


        abstract String getResponse(InputStream requestBody, Headers headers) throws IOException;

    }

    private JsonWrappedCertificate createCertificateResponse(final byte[] certificate) {
        return new JsonWrappedCertificate(certificate);
    }
}