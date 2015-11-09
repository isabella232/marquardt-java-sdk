/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.authority;

import org.echocat.marquardt.authority.policies.ClientAccessPolicy;
import org.echocat.marquardt.authority.policies.SessionCreationPolicy;
import org.echocat.marquardt.authority.persistence.SessionStore;
import org.echocat.marquardt.authority.persistence.UserStore;
import org.echocat.marquardt.authority.testdomain.TestSession;
import org.echocat.marquardt.authority.testdomain.TestUser;
import org.echocat.marquardt.authority.testdomain.TestUserCredentials;
import org.echocat.marquardt.authority.testdomain.TestUserInfo;
import org.echocat.marquardt.common.TestKeyPairProvider;
import org.echocat.marquardt.common.domain.Credentials;
import org.junit.Before;
import org.mockito.Mock;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@SuppressWarnings("AbstractClassWithoutAbstractMethods")
public abstract class AuthorityTest {
    private static final String TEST_CLIENT_ID = "asdf";
    protected static final TestUserCredentials TEST_USER_CREDENTIALS = new TestUserCredentials("test@example.com", "right", TestKeyPairProvider.create().getPublicKey(), TEST_CLIENT_ID);
    protected static final TestUserCredentials CREDENTIALS_WITH_WRONG_PASSWORD = new TestUserCredentials(TEST_USER_CREDENTIALS.getIdentifier(), "wrong", TEST_USER_CREDENTIALS.getPublicKey(), TEST_CLIENT_ID);
    protected static final byte[] CERTIFICATE = new byte[0];
    private static final TestUser TEST_USER = new TestUser();
    private static final TestUserInfo TEST_USER_INFO = new TestUserInfo();
    private static final UUID USER_ID = UUID.randomUUID();
    private TestSession _validSession;

    @Mock
    protected UserStore<TestUser, TestUserCredentials> _userStore;

    @Mock
    protected SessionStore<TestSession> _sessionStore;

    @Mock
    protected ClientAccessPolicy _clientAccessPolicy;

    @Mock
    protected SessionCreationPolicy _sessionCreationPolicy;

    @Before
    public void setup() throws Exception {
        when(getSessionStore().createTransient()).thenReturn(createTestSession());
        when(_clientAccessPolicy.isAllowed(TEST_CLIENT_ID)).thenReturn(true);
        setValidSession(createTestSession());
    }

    protected static TestSession createTestSession() {
        final TestSession testSession = new TestSession();
        testSession.setExpiresAt(new Date(new Date().getTime() + TimeUnit.DAYS.toMillis(60)));
        testSession.setUserId(USER_ID);
        testSession.setPublicKey(TestKeyPairProvider.create().getPublicKey().getEncoded());
        testSession.setClientId(TEST_CLIENT_ID);
        return testSession;
    }

    protected void givenUserExists() {
        when(getUserStore().findByUuid(USER_ID)).thenReturn(Optional.of(TEST_USER));
        when(getUserStore().toSignable(any(TestUser.class))).thenReturn(TEST_USER_INFO);
        when(getUserStore().findByCredentials(any(Credentials.class))).thenReturn(Optional.of(TEST_USER));
    }

    protected void givenExistingSession() {
        when(getSessionStore().findByCertificate(any(byte[].class))).thenReturn(Optional.of(getValidSession()));
    }

    protected void givenNoExistingSession() {
        when(getSessionStore().findByCertificate(any(byte[].class))).thenReturn(Optional.empty());
    }

    protected void givenUserDoesNotExist() {
        when(getUserStore().findByCredentials(any(Credentials.class))).thenReturn(Optional.<TestUser>empty());
        when(getUserStore().findByUuid(any(UUID.class))).thenReturn(Optional.<TestUser>empty());
        when(getUserStore().createFromCredentials(any(TestUserCredentials.class))).thenReturn(TEST_USER);
        when(getUserStore().toSignable(any(TestUser.class))).thenReturn(TEST_USER_INFO);
    }

    protected void givenSessionCreationPolicyAllowsAnotherSession() {
        when(getSessionCreationPolicy().mayCreateSession(any(UUID.class), any(byte[].class))).thenReturn(true);
    }

    protected TestSession getValidSession() {
        return _validSession;
    }

    protected void setValidSession(final TestSession validSession) {
        _validSession = validSession;
    }

    protected UserStore<TestUser, TestUserCredentials> getUserStore() {
        return _userStore;
    }

    protected SessionStore<TestSession> getSessionStore() {
        return _sessionStore;
    }

    protected SessionCreationPolicy getSessionCreationPolicy() {
        return _sessionCreationPolicy;
    }

    protected ClientAccessPolicy getClientAccessPolicy() {
        return _clientAccessPolicy;
    }
}