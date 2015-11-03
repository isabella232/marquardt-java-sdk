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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.echocat.marquardt.common.web.SignatureHeaders.X_CERTIFICATE;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class AuthorityIntegrationTest extends AuthorityTest {

    private TestHttpAuthorityServer _testHttpAuthorityServer;

    private final ObjectMapper _objectMapper = new ObjectMapper();

    private HttpURLConnection _connection;
    private String _response;
    private int _status;

    @Override
    @Before
    public void setup() throws Exception {
        _testHttpAuthorityServer = new TestHttpAuthorityServer(getUserStore(), getSessionStore(), getSessionCreationPolicy(), getClientWhiteList());
        _testHttpAuthorityServer.start();
        super.setup();
    }

    @Test
    public void shouldSignupUserWithCorrectCredentials() throws Exception {
        givenUserDoesNotExist();
        givenSessionCreationPolicyAllowsAnotherSession();
        givenSignupCall();
        whenCallingAuthority();
        thenSignedCertificateIsProduced();
    }

    @Test
    public void shouldSigninUserWithCorrectCredentials() throws Exception {
        givenUserExists();
        givenSessionCreationPolicyAllowsAnotherSession();
        givenSigninCall();
        whenCallingAuthority();
        thenSignedCertificateIsProduced();
    }

    @Test
    public void shouldSignoutUserWithActiveSessionAndValidCertificate() throws Exception {
        givenExistingSession();
        givenSignoutCall();
        whenCallingAuthority();
        thenSignoutIsPerformed();
    }

    @Test
    public void shouldRefreshCertificateWithActiveSessionAndValidCertificate() throws Exception {
        givenExistingSession();
        givenUserExists();
        givenRefreshCall();
        whenCallingAuthority();
        thenSignedCertificateIsProduced();
    }

    private void givenSignupCall() throws Exception {
        doPost("http://localhost:8000/signup", TEST_USER_CREDENTIALS);
    }

    private void givenSignoutCall() throws Exception {
        doPost("http://localhost:8000/signout", null);
    }

    private void givenSigninCall() throws Exception {
        doPost("http://localhost:8000/signin", TEST_USER_CREDENTIALS);
    }

    private void givenRefreshCall() throws Exception {
        doPost("http://localhost:8000/refresh", null);
    }

    private void whenCallingAuthority() throws IOException {
        try (InputStream inputStream = _connection.getInputStream()) {
            _status = _connection.getResponseCode();
            _response = CharStreams.toString(new InputStreamReader(inputStream, Charsets.UTF_8));
        }
    }

    private void thenSignedCertificateIsProduced() throws IOException {
        final JsonWrappedCertificate jsonWrappedCertificate = _objectMapper.readValue(_response, JsonWrappedCertificate.class);
        assertThat(jsonWrappedCertificate.getCertificate(), is(not(nullValue())));
    }

    private void thenSignoutIsPerformed() {
        assertThat(_status, is(204));
    }

    private void doPost(final String url, final Object content) throws Exception {
        final URL urlToPost = new URL(url);
        _connection = (HttpURLConnection) urlToPost.openConnection();
        _connection.setRequestMethod("POST");
        _connection.setRequestProperty("Content-Type", "application/json");
        _connection.setRequestProperty(X_CERTIFICATE.getHeaderName(), Base64.encodeBase64URLSafeString(CERTIFICATE));
        _connection.setDoOutput(true);
        _objectMapper.writeValue(_connection.getOutputStream(), content);
    }

    @After
    public void teardown() throws InterruptedException {
        _testHttpAuthorityServer.stop();
    }

}