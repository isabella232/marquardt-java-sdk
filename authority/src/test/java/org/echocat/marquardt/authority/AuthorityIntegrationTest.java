/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.authority;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.CharStreams;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.Charsets;
import org.echocat.marquardt.common.web.JsonWrappedCertificate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.echocat.marquardt.common.web.SignatureHeaders.X_CERTIFICATE;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@RunWith(MockitoJUnitRunner.class)
public class AuthorityIntegrationTest extends AuthorityTest {

    private final ObjectMapper _objectMapper = new ObjectMapper();

    private TestHttpAuthorityServer _testHttpAuthorityServer;
    private HttpURLConnection _connection;
    private String _response;
    private int _status;

    @Override
    @Before
    public void setup() throws Exception {
        super.setup();
        _testHttpAuthorityServer = new TestHttpAuthorityServer(getUserCatalog(), getUserCreator(), getSessionCreator(), getSessionRenewal(), getSessionStore(), getClientAccessPolicy());
        _testHttpAuthorityServer.start();
    }

    @After
    public void tearDown() throws InterruptedException {
        _testHttpAuthorityServer.stop();
    }

    @Test
    public void shouldSignUpUserWithCorrectCredentials() throws Exception {
        givenUserDoesNotExist();
        givenSessionCreationPolicyAllowsAnotherSession();
        givenSignUpCall();
        whenCallingAuthority();
        thenSignedCertificateIsProduced();
    }

    @Test
    public void shouldSignInUserWithCorrectCredentials() throws Exception {
        givenUserExists();
        givenSessionCreationPolicyAllowsAnotherSession();
        givenSignInCall();
        whenCallingAuthority();
        thenSignedCertificateIsProduced();
    }

    @Test
    public void shouldSignOutUserWithActiveSessionAndValidCertificate() throws Exception {
        givenExistingSession();
        givenSignOutCall();
        whenCallingAuthority();
        thenSignOutIsPerformed();
    }

    @Test
    public void shouldRefreshCertificateWithActiveSessionAndValidCertificate() throws Exception {
        givenExistingSession();
        givenUserExists();
        givenRefreshCall();
        whenCallingAuthority();
        thenSignedCertificateIsProduced();
    }

    private void givenSignUpCall() throws Exception {
        doPost("http://localhost:8000/signup", TEST_USER_CREDENTIALS);
    }

    private void givenSignOutCall() throws Exception {
        doPost("http://localhost:8000/signout", null);
    }

    private void givenSignInCall() throws Exception {
        doPost("http://localhost:8000/signin", TEST_USER_CREDENTIALS);
    }

    private void givenRefreshCall() throws Exception {
        doPost("http://localhost:8000/refresh", null);
    }

    private void whenCallingAuthority() throws IOException {
        try (final InputStream inputStream = _connection.getInputStream()) {
            _status = _connection.getResponseCode();
            try (final InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charsets.UTF_8)) {
                _response = CharStreams.toString(inputStreamReader);
            }
        }
    }

    private void thenSignedCertificateIsProduced() throws IOException {
        final JsonWrappedCertificate jsonWrappedCertificate = _objectMapper.readValue(_response, JsonWrappedCertificate.class);
        assertThat(jsonWrappedCertificate.getCertificate(), notNullValue());
    }

    private void thenSignOutIsPerformed() {
        assertThat(_status, is(NO_CONTENT.value()));
    }

    private void doPost(final String url, final Object content) throws Exception {
        final URL urlToPost = new URL(url);
        _connection = (HttpURLConnection) urlToPost.openConnection();
        _connection.setRequestMethod(HttpMethod.POST.name());
        _connection.setRequestProperty(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        _connection.setRequestProperty(X_CERTIFICATE.getHeaderName(), Base64.encodeBase64URLSafeString(CERTIFICATE));
        _connection.setDoOutput(true);
        _objectMapper.writeValue(_connection.getOutputStream(), content);
    }
}