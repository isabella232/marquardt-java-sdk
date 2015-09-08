/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.authority;

import org.echocat.marquardt.authority.domain.Session;
import org.echocat.marquardt.authority.persistence.PrincipalStore;
import org.echocat.marquardt.authority.persistence.SessionStore;
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
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

public abstract class AuthorityTest {
    protected static final TestUserCredentials TEST_USER_CREDENTIALS = new TestUserCredentials("test@example.com", "right", TestKeyPairProvider.create().getPublicKey());
    protected static final Credentials CREDENTIALS_WITH_WRONG_PASSWORD = new TestUserCredentials(TEST_USER_CREDENTIALS.getIdentifier(), "wrong", TEST_USER_CREDENTIALS.getPublicKey());
    protected static final byte[] CERTIFICATE = new byte[0];
    private static final TestUser TEST_USER = new TestUser();
    private static final TestUserInfo TEST_USER_INFO = new TestUserInfo();
    private static final UUID USER_ID = UUID.randomUUID();
    private Session _invalidSession;
    protected TestSession _validSession;

    @Mock
    PrincipalStore<TestUserInfo, TestUser> _principalStore;
    @Mock
    SessionStore _sessionStore;

    @Before
    public void setup() throws Exception {
        when(_sessionStore.create()).thenReturn(createTestSession());
        _validSession = createTestSession();
        _invalidSession = createTestSession();
        _invalidSession.setValid(false);
    }

    protected static TestSession createTestSession() {
        final TestSession testSession = new TestSession();
        testSession.setValid(true);
        testSession.setExpiresAt(new Date(new Date().getTime() + TimeUnit.DAYS.toMillis(60)));
        testSession.setUserId(USER_ID);
        testSession.setPublicKey(TestKeyPairProvider.create().getPublicKey().getEncoded());
        return testSession;
    }

    protected void givenUserExists() {
        when(_principalStore.getPrincipalByUuid(USER_ID)).thenReturn(Optional.of(TEST_USER));
        when(_principalStore.createSignableFromPrincipal(any(TestUser.class))).thenReturn(TEST_USER_INFO);
        when(_principalStore.getPrincipalFromCredentials(any(Credentials.class))).thenReturn(Optional.of(TEST_USER));
    }

    protected void givenExistingSession() {
        when(_sessionStore.isActiveAndValidSessionExists(any(UUID.class), any(byte[].class), any(Date.class))).thenReturn(true);
        when(_sessionStore.findByCertificate(any(byte[].class))).thenReturn(Optional.of(_validSession));
    }

    protected void givenExistingInvalidSession() {
        when(_sessionStore.isActiveAndValidSessionExists(any(UUID.class), any(byte[].class), any(Date.class))).thenReturn(true);
        when(_sessionStore.findByCertificate(any(byte[].class))).thenReturn(Optional.of(_invalidSession));
    }

    protected void givenNoExistingSession() {
        when(_sessionStore.isActiveAndValidSessionExists(eq(USER_ID), any(byte[].class), any(Date.class))).thenReturn(false);
        when(_sessionStore.findByCertificate(any(byte[].class))).thenReturn(Optional.empty());
    }

    protected void givenUserDoesNotExist() {
        when(_principalStore.getPrincipalFromCredentials(any(Credentials.class))).thenReturn(Optional.<TestUser>empty());
        when(_principalStore.getPrincipalByUuid(any(UUID.class))).thenReturn(Optional.<TestUser>empty());
        when(_principalStore.createPrincipalFromCredentials(any(Credentials.class))).thenReturn(TEST_USER);
        when(_principalStore.createSignableFromPrincipal(any(TestUser.class))).thenReturn(TEST_USER_INFO);
    }
}
