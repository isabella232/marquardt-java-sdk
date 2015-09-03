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

    public SimpleHttpAuthorityServer(PrincipalStore<TestUserInfo, TestUser> principalStore, SessionStore sessionStore) throws IOException {
        _server = HttpServer.create(new InetSocketAddress(8000), 0);
        _objectMapper = new ObjectMapper();
        _authority = new PojoAuthority<>(principalStore, sessionStore, TestKeyPairProvider.create());
    }

    public void start() throws IOException {
        _server.createContext("/signup", new SignupHandler());
        _server.setExecutor(null);
        _server.start();
    }

    public void stop() {
        _server.stop(0);
    }

    class SignupHandler extends Handler {

        @Override
        String getResponse(InputStream requestBody) throws IOException {
            final TestUserCredentials testUserCredentials = _objectMapper.readValue(requestBody, TestUserCredentials.class);
            final JsonWrappedCertificate jsonWrappedCertificate = _authority.signUp(testUserCredentials);
            return _objectMapper.writeValueAsString(jsonWrappedCertificate);
        }
    }

    abstract class Handler implements HttpHandler {

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            if (!httpExchange.getRequestMethod().equals("POST")) {
                httpExchange.sendResponseHeaders(405, 0);
            } else if (!"application/json".equals(httpExchange.getRequestHeaders().get("Content-Type").get(0))) {
                httpExchange.sendResponseHeaders(415, 0);
            }
            String response = getResponse(httpExchange.getRequestBody());
            httpExchange.sendResponseHeaders(200, response.length());
            OutputStream os = httpExchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }


        abstract String getResponse(InputStream requestBody) throws IOException;

    }

}
