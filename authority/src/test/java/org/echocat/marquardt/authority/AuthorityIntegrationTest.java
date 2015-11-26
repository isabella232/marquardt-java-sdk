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
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.OkHttpClientHttpRequestFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;

import static org.echocat.marquardt.common.web.SignatureHeaders.X_CERTIFICATE;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@RunWith(MockitoJUnitRunner.class)
public class AuthorityIntegrationTest extends AuthorityTest {

    private final ObjectMapper _objectMapper = new ObjectMapper();

    private TestHttpAuthorityServer _testHttpAuthorityServer;
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
    public void shouldInitializeSignUpWithCorrectClientInformation() throws Exception {
        givenEmptyUserWillBeCreated();
        whenCallingInitializeSignUp();
        thenSignedCertificateIsProduced();
        thenEmptyUserWasCreated();
    }

    @Test
    public void shouldFinalizeSignUpWithCorrectCredentials() throws Exception {
        givenEmptyUserExistsAndNoOneElseUsesSameCredentials();
        givenExistingSession();
        whenCallingFinalizeSignUp();
        thenSignedCertificateIsProduced();
        thenUserWasEnrichedWithAccountData();
    }

    @Test
    public void shouldSignInUserWithCorrectCredentials() throws Exception {
        givenUserExists();
        givenSessionCreationPolicyAllowsAnotherSession();
        whenCallingSignIn();
        thenSignedCertificateIsProduced();
    }

    @Test
    public void shouldSignOutUserWithActiveSessionAndValidCertificate() throws Exception {
        givenExistingSession();
        whenCallingSignOut();
        thenSignOutIsPerformed();
    }

    @Test
    public void shouldRefreshCertificateWithActiveSessionAndValidCertificate() throws Exception {
        givenExistingSession();
        givenUserExists();
        whenCallingRefresh();
        thenSignedCertificateIsProduced();
    }

    private void whenCallingInitializeSignUp() throws Exception {
        doPost("http://localhost:8000/initializeSignUp", TEST_CLIENT_INFORMATION);
    }

    private void whenCallingFinalizeSignUp() throws Exception {
        doPost("http://localhost:8000/finalizeSignUp", TEST_USER_ACCOUNT_DATA);
    }

    private void whenCallingSignOut() throws Exception {
        doPost("http://localhost:8000/signOut", null);
    }

    private void whenCallingSignIn() throws Exception {
        doPost("http://localhost:8000/signIn", TEST_USER_CREDENTIALS);
    }

    private void whenCallingRefresh() throws Exception {
        doPost("http://localhost:8000/refresh", null);
    }

    private void thenSignedCertificateIsProduced() throws IOException {
        final JsonWrappedCertificate jsonWrappedCertificate = _objectMapper.readValue(_response, JsonWrappedCertificate.class);
        assertThat(jsonWrappedCertificate.getCertificate(), notNullValue());
    }

    private void thenUserWasEnrichedWithAccountData() {
        verify(_userCreator).enrichAndUpdateFrom(_testUser, TEST_USER_ACCOUNT_DATA);
    }

    private void thenEmptyUserWasCreated() {
        verify(_userCreator).createEmptyUser(TEST_CLIENT_INFORMATION);
    }

    private void thenSignOutIsPerformed() {
        assertThat(_status, is(NO_CONTENT.value()));
    }

    private void doPost(final String url, final Object content) throws Exception {
        final byte[] bytes;
        try (final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            _objectMapper.writeValue(byteArrayOutputStream, content);
            byteArrayOutputStream.flush();
            bytes = byteArrayOutputStream.toByteArray();
        }
        final URI urlToPost = new URI(url);
        final ClientHttpRequest request = new OkHttpClientHttpRequestFactory().createRequest(urlToPost, HttpMethod.POST);
        request.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        request.getHeaders().add(X_CERTIFICATE.getHeaderName(), Base64.encodeBase64URLSafeString(CERTIFICATE));
        request.getBody().write(bytes);
        final ClientHttpResponse response = request.execute();
        _status = response.getStatusCode().value();
        try (final InputStream inputStream = response.getBody()) {
            try (final InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charsets.UTF_8)) {
                _response = CharStreams.toString(inputStreamReader);
            }
        }
    }
}