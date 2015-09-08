/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.authority;

import org.echocat.marquardt.authority.domain.Session;
import org.echocat.marquardt.authority.exceptions.CertificateCreationException;
import org.echocat.marquardt.authority.exceptions.InvalidSessionException;
import org.echocat.marquardt.authority.exceptions.NoSessionFoundException;
import org.echocat.marquardt.authority.testdomain.TestUser;
import org.echocat.marquardt.authority.testdomain.TestUserInfo;
import org.echocat.marquardt.common.TestKeyPairProvider;
import org.echocat.marquardt.common.domain.JsonWrappedCertificate;
import org.echocat.marquardt.common.domain.KeyPairProvider;
import org.echocat.marquardt.common.exceptions.AlreadyLoggedInException;
import org.echocat.marquardt.common.exceptions.InvalidSignatureException;
import org.echocat.marquardt.common.exceptions.LoginFailedException;
import org.echocat.marquardt.common.exceptions.UserExistsException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuthorityUnitTest extends AuthorityTest {

    @Mock
    private KeyPairProvider _issuerKeyProvider;

    @InjectMocks
    private Authority<TestUserInfo, TestUser> _authority;

    private JsonWrappedCertificate _certificate;

    @Before
    public void setup() throws Exception {
        final KeyPairProvider keyPairProvider = TestKeyPairProvider.create();
        when(_issuerKeyProvider.getPrivateKey()).thenReturn(keyPairProvider.getPrivateKey());
        when(_issuerKeyProvider.getPublicKey()).thenReturn(keyPairProvider.getPublicKey());
        super.setup();
    }

    @Test
    public void shouldSignUpUserWhenUserNotExistsInStore() throws Exception {
        givenUserDoesNotExist();
        whenSigningUp();
        thenUserIsStored();
        thenCertificateIsMade();
    }

    @Test(expected = UserExistsException.class)
    public void shouldThrowExceptionWhenUserAlreadyExistsWhenSignUp() throws Exception {
        givenUserExists();
        whenSigningUp();
    }

    @Test
    public void shouldSigninUserWhenCredentialsAreCorrectAndUserExistsIsStore() throws Exception {
        givenUserExists();
        givenNoExistingSession();
        whenSigningIn();
        thenSessionIsCreated();
        thenCertificateIsMade();
    }

    @Test(expected = LoginFailedException.class)
    public void shouldThrowExceptionWhenUserDoesNotExistWhenSignIn() throws Exception {
        givenUserDoesNotExist();
        whenSigningIn();
    }

    @Test(expected = AlreadyLoggedInException.class)
    public void shouldThrowExceptionWhenSessionAlreadyExistsWhenSignIn() throws Exception {
        givenUserExists();
        givenExistingSession();
        whenSigningIn();
    }

    @Test(expected = LoginFailedException.class)
    public void shouldThrowExceptionWhenSigningInAndPasswordIsNotMatching() throws Exception {
        givenUserExists();
        givenNoExistingSession();
        whenSigningInWithWrongPassword();
    }

    @Test
    public void shouldRefreshCertificate() throws Exception {
        givenUserExists();
        givenExistingSession();
        whenRefreshingCertificate();
        thenSessionIsUpdated();
        thenCertificateIsMade();
    }

    @Test(expected = NoSessionFoundException.class)
    public void shouldThrowNoSessionFoundExceptionWhenRefreshingWithoutSession() throws Exception {
        givenUserExists();
        givenNoExistingSession();
        whenRefreshingCertificate();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowIllegalStateExceptionWhenRefreshingButNoUserExists() throws Exception {
        givenUserDoesNotExist();
        givenExistingSession();
        whenRefreshingCertificate();
    }

    @Test(expected = InvalidSessionException.class)
    public void shouldThrowInvalidSessionExceptionWhenSessionIsInvalid() throws Exception {
        givenUserExists();
        givenExistingInvalidSession();
        whenRefreshingCertificate();
    }

    @Test
    public void shouldSignOut() throws Exception {
        givenUserExists();
        givenExistingSession();
        whenSigningOut();
        thenSessionIsInvalidated();
        thenSessionIsUpdated();
    }

    @Test(expected = InvalidSessionException.class)
    public void shouldThrowIllegalStateExceptionWhenSigningOutButNoUserExists() throws Exception {
        givenUserDoesNotExist();
        givenExistingInvalidSession();
        whenSigningOut();
    }

    @Test(expected = NoSessionFoundException.class)
    public void shouldThrowNoSessionFoundExceptionWhenSigningOutWithoutSession() throws Exception {
        givenUserExists();
        givenNoExistingSession();
        whenSigningOut();
    }

    private void whenSigningInWithWrongPassword() {
        _authority.signIn(CREDENTIALS_WITH_WRONG_PASSWORD);
    }

    private void whenSigningIn() {
        _certificate = _authority.signIn(TEST_USER_CREDENTIALS);
    }

    private void whenSigningUp() {
        _certificate = _authority.signUp(TEST_USER_CREDENTIALS);
    }

    private void whenRefreshingCertificate() {
        _certificate = _authority.refresh(CERTIFICATE);
    }

    private void whenSigningOut() {
        _authority.signOut(CERTIFICATE);
    }

    private void thenUserIsStored() {
        verify(_principalStore).createPrincipalFromCredentials(TEST_USER_CREDENTIALS);
    }

    private void thenSessionIsCreated() {
        verify(_sessionStore).save(any(Session.class));
    }

    private void thenSessionIsInvalidated() {
        assertThat(_validSession.isValid(), is(false));
    }

    private void thenSessionIsUpdated() {
        verify(_sessionStore).save(any(Session.class));
    }

    private void thenCertificateIsMade() {
        assertThat(_certificate, is(not(nullValue())));
    }
}