/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.authority;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.echocat.marquardt.authority.persistence.PrincipalStore;
import org.echocat.marquardt.authority.persistence.SessionStore;
import org.echocat.marquardt.authority.testdomain.TestUser;
import org.echocat.marquardt.authority.testdomain.TestUserCredentials;
import org.echocat.marquardt.authority.testdomain.TestUserInfo;
import org.echocat.marquardt.common.TestKeyPairProvider;
import org.echocat.marquardt.common.domain.JsonWrappedCertificate;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class SimpleHttpAuthorityServer {

    private final HttpServer _server;
    private final ObjectMapper _objectMapper;
    private final Authority _authority;

    public SimpleHttpAuthorityServer(final PrincipalStore<TestUserInfo, TestUser> principalStore, final SessionStore sessionStore) throws IOException {
        _server = HttpServer.create(new InetSocketAddress(8000), 0);
        _objectMapper = new ObjectMapper();
        _authority = new PojoAuthority<>(principalStore, sessionStore, TestKeyPairProvider.create());
    }

    public void start() throws IOException {
        _server.createContext("/signup", new SignupHandler(200));
        _server.createContext("/signout", new SignoutHandler(204));
        _server.createContext("/refresh", new SignoutHandler(200));
        _server.setExecutor(null);
        _server.start();
    }

    public void stop() throws InterruptedException {
        _server.stop(0);
        Thread.sleep(100L);
    }

    class SignupHandler extends Handler {

        public SignupHandler(Integer successResponseCode) {
            super(successResponseCode);
        }

        @Override
        String getResponse(final InputStream requestBody) throws IOException {
            final TestUserCredentials testUserCredentials = _objectMapper.readValue(requestBody, TestUserCredentials.class);
            final JsonWrappedCertificate jsonWrappedCertificate = _authority.signUp(testUserCredentials);
            return _objectMapper.writeValueAsString(jsonWrappedCertificate);
        }
    }

    class SignoutHandler extends Handler {
        public SignoutHandler(Integer successResponseCode) {
            super(successResponseCode);
        }

        @Override
        String getResponse(final InputStream requestBody) throws IOException {
            final JsonWrappedCertificate certificate = _objectMapper.readValue(requestBody, JsonWrappedCertificate.class);
            _authority.signOut(certificate.getCertificate());
            return null;
        }
    }

    class RefreshHandler extends Handler {

        public RefreshHandler(Integer successResponseCode) {
            super(successResponseCode);
        }

        @Override
        String getResponse(InputStream requestBody) throws IOException {
            final JsonWrappedCertificate certificate = _objectMapper.readValue(requestBody, JsonWrappedCertificate.class);
            final JsonWrappedCertificate refresh = _authority.refresh(certificate.getCertificate());
            return _objectMapper.writeValueAsString(refresh);
        }
    }

    abstract class Handler implements HttpHandler {

        private final Integer _successResponseCode;

        public Handler(Integer successResponseCode) {
            _successResponseCode = successResponseCode;
        }

        @Override
        public void handle(final HttpExchange httpExchange) throws IOException {
            if (!httpExchange.getRequestMethod().equals("POST")) {
                httpExchange.sendResponseHeaders(405, 0);
            } else if (!"application/json".equals(httpExchange.getRequestHeaders().get("Content-Type").get(0))) {
                httpExchange.sendResponseHeaders(415, 0);
            }
            String response = getResponse(httpExchange.getRequestBody());
            if (response != null) {
                httpExchange.sendResponseHeaders(_successResponseCode, response.length());
                OutputStream os = httpExchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                httpExchange.sendResponseHeaders(_successResponseCode, -1);
            }
        }


        abstract String getResponse(InputStream requestBody) throws IOException;

    }

}
