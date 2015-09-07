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
import org.apache.commons.io.Charsets;
import org.echocat.marquardt.authority.persistence.PrincipalStore;
import org.echocat.marquardt.authority.persistence.SessionStore;
import org.echocat.marquardt.authority.testdomain.TestSession;
import org.echocat.marquardt.authority.testdomain.TestUser;
import org.echocat.marquardt.authority.testdomain.TestUserCredentials;
import org.echocat.marquardt.authority.testdomain.TestUserInfo;
import org.echocat.marquardt.common.TestKeyPairProvider;
import org.echocat.marquardt.common.domain.Credentials;
import org.echocat.marquardt.common.domain.JsonWrappedCertificate;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PojoAuthorityIntegrationTest {

    private static final TestUserCredentials TEST_USER_CREDENTIALS = new TestUserCredentials("test@example.com", "fgfdg", TestKeyPairProvider.create().getPublicKey());
    private static final TestUser TEST_USER = new TestUser();
    private static final TestUserInfo TEST_USER_INFO = new TestUserInfo();
    private static final byte[] CERTIFICATE = new byte[0];

    private static final UUID USER_ID = UUID.randomUUID();
    private static final long SIXTY_DAYS = 1000 * 60 * 60 * 24 * 60;
    private SimpleHttpAuthorityServer _simpleHttpAuthorityServer;

    private final ObjectMapper _objectMapper = new ObjectMapper();

    @Mock
    PrincipalStore<TestUserInfo, TestUser> _principalStore;
    @Mock
    SessionStore _sessionStore;

    private HttpURLConnection _connection;
    private TestSession _validSession;
    private String _response;
    private int _status;

    @Before
    public void setup() throws IOException {
        _simpleHttpAuthorityServer = new SimpleHttpAuthorityServer(_principalStore, _sessionStore);
        _simpleHttpAuthorityServer.start();
        _validSession = createTestSession();
    }

    @Test
    public void shouldSigninUserWithCorrectCredentials() throws Exception {
        givenSignupCall();
        givenUserDoesNotExist();
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

    private void givenUserExists() {
        when(_principalStore.getPrincipalByUuid(USER_ID)).thenReturn(Optional.of(TEST_USER));
        when(_principalStore.createSignableFromPrincipal(any(TestUser.class))).thenReturn(TEST_USER_INFO);
    }

    private void givenExistingSession() {
        when(_sessionStore.findByCertificate(any(byte[].class))).thenReturn(Optional.of(_validSession));
    }

    private void givenUserDoesNotExist() {
        when(_principalStore.getPrincipalFromCredentials(any(Credentials.class))).thenReturn(Optional.<TestUser>empty());
        when(_principalStore.createPrincipalFromCredentials(any(Credentials.class))).thenReturn(TEST_USER);
        when(_principalStore.createSignableFromPrincipal(any(TestUser.class))).thenReturn(TEST_USER_INFO);
        when(_sessionStore.create()).thenReturn(createTestSession());
    }

    private void givenSignupCall() throws Exception {
        doPost("http://localhost:8000/signup", TEST_USER_CREDENTIALS);
    }

    private void givenSignoutCall() throws Exception {
        doPost("http://localhost:8000/signout", new JsonWrappedCertificate(CERTIFICATE));
    }

    private void givenRefreshCall() throws Exception{
        doPost("http://localhost:8000/refresh", new JsonWrappedCertificate(CERTIFICATE));
    }

    private void whenCallingAuthority() throws IOException {
        final InputStream inputStream = _connection.getInputStream();
        try {
            _status = _connection.getResponseCode();
            _response = CharStreams.toString(new InputStreamReader(inputStream, Charsets.UTF_8));
        } finally {
            inputStream.close();
        }
    }

    private void thenSignedCertificateIsProduced() throws IOException {
        final JsonWrappedCertificate jsonWrappedCertificate = _objectMapper.readValue(_response, JsonWrappedCertificate.class);
        assertThat(jsonWrappedCertificate.getCertificate(), is(not(nullValue())));
    }

    private void thenSignoutIsPerformed() {
        assertThat(_status, is(204));
    }

    private void doPost(String url, Object content) throws Exception{
        final URL urlToPost = new URL(url);
        _connection = (HttpURLConnection) urlToPost.openConnection();
        _connection.setRequestMethod("POST");
        _connection.setRequestProperty("Content-Type", "application/json");
        _connection.setDoOutput(true);
        _objectMapper.writeValue(_connection.getOutputStream(), content);
    }

    @After
    public void teardown() throws InterruptedException {
        _simpleHttpAuthorityServer.stop();
    }

    private static TestSession createTestSession() {
        final TestSession testSession = new TestSession();
        testSession.setValid(true);
        testSession.setExpiresAt(new Date(new Date().getTime() + SIXTY_DAYS));
        testSession.setUserId(USER_ID);
        testSession.setPublicKey(TestKeyPairProvider.create().getPublicKey().getEncoded());
        return testSession;
    }


}