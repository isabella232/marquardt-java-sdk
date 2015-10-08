/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.authority;

import org.echocat.marquardt.authority.exceptions.CertificateCreationException;
import org.echocat.marquardt.authority.testdomain.IOExceptionThrowingTestUserInfo;
import org.echocat.marquardt.authority.testdomain.TestSession;
import org.echocat.marquardt.authority.testdomain.TestUser;
import org.echocat.marquardt.authority.testdomain.TestUserInfo;
import org.echocat.marquardt.common.TestKeyPairProvider;
import org.echocat.marquardt.common.domain.Signature;
import org.echocat.marquardt.common.exceptions.AlreadyLoggedInException;
import org.echocat.marquardt.common.exceptions.LoginFailedException;
import org.echocat.marquardt.common.exceptions.NoSessionFoundException;
import org.echocat.marquardt.common.exceptions.SignatureValidationFailedException;
import org.echocat.marquardt.common.exceptions.UserAlreadyExistsException;
import org.echocat.marquardt.common.keyprovisioning.KeyPairProvider;
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
    private Authority<TestUser, TestSession, TestUserInfo> _authority;

    private byte[] _certificate;
    @Mock
    private Signature _signature;

    @Before
    @Override
    public void setup() throws Exception {
        final KeyPairProvider keyPairProvider = TestKeyPairProvider.create();
        when(_issuerKeyProvider.getPrivateKey()).thenReturn(keyPairProvider.getPrivateKey());
        when(_issuerKeyProvider.getPublicKey()).thenReturn(keyPairProvider.getPublicKey());
        when(_signature.isValidFor(any(), any())).thenReturn(true);
        super.setup();
    }

    @Test
    public void shouldSignUpUser() throws Exception {
        givenUserDoesNotExist();
        whenSigningUp();
        thenUserIsStored();
        thenCertificateIsMade();
    }

    @Test(expected = CertificateCreationException.class)
    public void shouldThrowCertificateCreationExceptionWhenSignupAndSignableSerializationFails() throws Exception {
        givenUserDoesNotExist();
        givenSignableThrowingException();
        whenSigningUp();
    }

    @Test(expected = UserAlreadyExistsException.class)
    public void shouldThrowExceptionWhenUserAlreadyExistsWhenSignUp() throws Exception {
        givenUserExists();
        whenSigningUp();
    }

    @Test
    public void shouldSigninUser() throws Exception {
        givenUserExists();
        givenNoExistingSession();
        givenSessionCreationPolicyAllowsAnotherSession();
        whenSigningIn();
        thenSessionIsCreated();
        thenCertificateIsMade();
    }

    @Test(expected = CertificateCreationException.class)
    public void shouldThrowCerificateCreationFailedExceptionWhenSigningInButPayloadCannotBeSigned() throws Exception {
        givenUserExists();
        givenNoExistingSession();
        givenSessionCreationPolicyAllowsAnotherSession();
        givenSignableThrowingException();
        whenSigningIn();
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

    @Test(expected = SignatureValidationFailedException.class)
    public void shouldThrowExceptionWhenSignatureIsInvalidOnRefresh() throws Exception {
        givenUserExists();
        givenExistingSession();
        givenInvalidSignature();
        whenRefreshingCertificate();
    }

    private void givenInvalidSignature() {
        when(_signature.isValidFor(any(), any())).thenReturn(false);
    }

    @Test(expected = CertificateCreationException.class)
    public void shouldThrowCertificateCreationFailedExceptionWhenRefreshingInButPayloadCannotBeSigned() throws Exception {
        givenUserExists();
        givenExistingSession();
        givenSignableThrowingException();
        whenRefreshingCertificate();
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

    @Test
    public void shouldSignOut() throws Exception {
        givenUserExists();
        givenExistingSession();
        whenSigningOut();
        thenSessionIsDeleted();
    }

    @Test(expected = SignatureValidationFailedException.class)
    public void shouldThrowExceptionWhenSignatureIsInvalidOnSignOut() throws Exception {
        givenUserExists();
        givenExistingSession();
        givenInvalidSignature();
        whenSigningOut();
    }

    @Test
    public void shouldQuietlyHandleNoSessionFoundExceptionWhenSigningOutButNoUserExists() throws Exception {
        givenUserDoesNotExist();
        givenNoExistingSession();
        whenSigningOut();
    }

    @Test
    public void shouldQuietlyHandleNoSessionFoundExceptionWhenSigningOutWithoutSession() throws Exception {
        givenUserExists();
        givenNoExistingSession();
        whenSigningOut();
    }

    private void givenSignableThrowingException() {
        when(getUserStore().createSignableFromUser(any(TestUser.class))).thenReturn(new IOExceptionThrowingTestUserInfo());
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
        _certificate = _authority.refresh(CERTIFICATE, new byte[0], _signature);
    }

    private void whenSigningOut() {
        _authority.signOut(CERTIFICATE, new byte[0], _signature);
    }

    private void thenUserIsStored() {
        verify(getUserStore()).createFromCredentials(TEST_USER_CREDENTIALS);
    }

    private void thenSessionIsCreated() {
        verify(getSessionStore()).save(any(TestSession.class));
    }

    private void thenSessionIsUpdated() {
        verify(getSessionStore()).save(any(TestSession.class));
    }

    private void thenSessionIsDeleted() {
        verify(getSessionStore()).delete(any(TestSession.class));
    }

    private void thenCertificateIsMade() {
        assertThat(_certificate, is(not(nullValue())));
    }
}