/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.authority;

import org.echocat.marquardt.authority.persistence.SessionStore;
import org.echocat.marquardt.authority.persistence.UserCatalog;
import org.echocat.marquardt.authority.persistence.UserCreator;
import org.echocat.marquardt.authority.policies.ClientAccessPolicy;
import org.echocat.marquardt.authority.policies.SessionCreationPolicy;
import org.echocat.marquardt.authority.session.ExpiryDateCalculatorImpl;
import org.echocat.marquardt.authority.session.SessionCreator;
import org.echocat.marquardt.authority.session.SessionRenewal;
import org.echocat.marquardt.authority.testdomain.TestClientInformation;
import org.echocat.marquardt.authority.testdomain.TestSession;
import org.echocat.marquardt.authority.testdomain.TestSignUpAccountData;
import org.echocat.marquardt.authority.testdomain.TestUser;
import org.echocat.marquardt.authority.testdomain.TestUserCredentials;
import org.echocat.marquardt.authority.testdomain.TestUserInfo;
import org.echocat.marquardt.common.TestKeyPairProvider;
import org.echocat.marquardt.common.domain.Credentials;
import org.echocat.marquardt.common.keyprovisioning.KeyPairProvider;
import org.junit.Before;
import org.mockito.Mock;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.echocat.marquardt.authority.testdomain.TestUser.USER_ID;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@SuppressWarnings("AbstractClassWithoutAbstractMethods")
public abstract class AuthorityTest {
    protected static final String TEST_CLIENT_ID = "asdf";
    protected static final TestClientInformation TEST_CLIENT_INFORMATION = new TestClientInformation(TestKeyPairProvider.create().getPublicKey(), TEST_CLIENT_ID);
    protected static final TestUserCredentials TEST_USER_CREDENTIALS = new TestUserCredentials("test@example.com", "right", TEST_CLIENT_INFORMATION.getPublicKey(), TEST_CLIENT_INFORMATION.getClientId());
    protected static final TestSignUpAccountData TEST_USER_ACCOUNT_DATA = TestSignUpAccountData.of(TEST_USER_CREDENTIALS);
    protected static final TestUserCredentials CREDENTIALS_WITH_WRONG_PASSWORD = new TestUserCredentials(TEST_USER_CREDENTIALS.getIdentifier(), "wrong", TEST_USER_CREDENTIALS.getPublicKey(), TEST_CLIENT_ID);
    protected static final byte[] CERTIFICATE = new byte[0];
    private static final TestUserInfo TEST_USER_INFO = new TestUserInfo();
    protected final ExpiryDateCalculatorImpl<TestUser> _expiryDateCalculator = new ExpiryDateCalculatorImpl<>();

    @Mock
    protected UserCatalog<TestUser> _userCatalog;

    @Mock
    protected UserCreator<TestUser, TestUserCredentials, TestSignUpAccountData> _userCreator;
    @Mock
    protected SessionStore<TestSession> _sessionStore;
    @Mock
    protected ClientAccessPolicy _clientAccessPolicy;
    @Mock
    protected SessionCreationPolicy _sessionCreationPolicy;
    @Mock
    protected KeyPairProvider _issuerKeyProvider;

    protected final TestUser _testUser = new TestUser();

    private SessionCreator<TestUser, TestSession> _sessionCreator;
    private SessionRenewal<TestUser, TestSession> _sessionRenewal;
    private TestSession _validSession;

    @Before
    public void setup() throws Exception {
        final KeyPairProvider keyPairProvider = TestKeyPairProvider.create();
        when(_issuerKeyProvider.getPrivateKey()).thenReturn(keyPairProvider.getPrivateKey());
        when(_issuerKeyProvider.getPublicKey()).thenReturn(keyPairProvider.getPublicKey());
        when(getSessionStore().createTransient()).thenReturn(createTestSession());
        when(_clientAccessPolicy.isAllowed(TEST_CLIENT_ID)).thenReturn(true);
        setValidSession(createTestSession());
        when(_sessionCreationPolicy.mayCreateSession(any(), any())).thenReturn(true);
        _sessionCreator = new SessionCreator<>(_sessionStore, _userCatalog, _expiryDateCalculator, _issuerKeyProvider);
        _sessionCreator.setSessionCreationPolicy(_sessionCreationPolicy);
        _sessionRenewal = new SessionRenewal<>(_sessionStore, _userCatalog, _expiryDateCalculator, _issuerKeyProvider);
    }

    protected static TestSession createTestSession() {
        final TestSession testSession = new TestSession();
        testSession.setExpiresAt(new Date(new Date().getTime() + TimeUnit.DAYS.toMillis(60)));
        testSession.setUserId(USER_ID);
        testSession.setPublicKey(TestKeyPairProvider.create().getPublicKey().getEncoded());
        testSession.setClientId(TEST_CLIENT_ID);
        return testSession;
    }

    protected void givenEmptyUserExistsAndNoOneElseUsesSameCredentials() {
        when(getUserCatalog().findByUuid(USER_ID)).thenReturn(Optional.of(_testUser));
        when(getUserCatalog().toSignable(any(TestUser.class))).thenReturn(TEST_USER_INFO);
        when(getUserCatalog().findByCredentials(any(Credentials.class))).thenReturn(Optional.empty());
        when(getUserCreator().enrichAndUpdateFrom(_testUser, TEST_USER_ACCOUNT_DATA)).thenReturn(_testUser);
    }

    protected void givenUserExists() {
        when(getUserCatalog().findByUuid(USER_ID)).thenReturn(Optional.of(_testUser));
        when(getUserCatalog().toSignable(any(TestUser.class))).thenReturn(TEST_USER_INFO);
        when(getUserCatalog().findByCredentials(any(Credentials.class))).thenReturn(Optional.of(_testUser));
    }

    protected void givenExistingSession() {
        when(getSessionStore().findByCertificate(any(byte[].class))).thenReturn(Optional.of(getValidSession()));
    }

    protected void givenNoExistingSession() {
        when(getSessionStore().findByCertificate(any(byte[].class))).thenReturn(Optional.empty());
    }

    protected void givenEmptyUserWillBeCreated() {
        when(getUserCreator().createEmptyUser()).thenReturn(_testUser);
        when(getUserCatalog().toSignable(any(TestUser.class))).thenReturn(TEST_USER_INFO);
    }

    protected void givenUserDoesNotExist() {
        when(getUserCatalog().findByCredentials(any(Credentials.class))).thenReturn(Optional.<TestUser>empty());
        when(getUserCatalog().findByUuid(any(UUID.class))).thenReturn(Optional.<TestUser>empty());
        when(getUserCatalog().toSignable(any(TestUser.class))).thenReturn(TEST_USER_INFO);
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

    protected UserCatalog<TestUser> getUserCatalog() {
        return _userCatalog;
    }

    protected UserCreator<TestUser, TestUserCredentials, TestSignUpAccountData> getUserCreator() {
        return _userCreator;
    }

    protected SessionCreator<TestUser, TestSession> getSessionCreator() {
        return _sessionCreator;
    }

    protected SessionRenewal<TestUser, TestSession> getSessionRenewal() {
        return _sessionRenewal;
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