/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.authority;

import org.echocat.marquardt.authority.exceptions.CertificateCreationException;
import org.echocat.marquardt.authority.exceptions.ExpiredSessionException;
import org.echocat.marquardt.authority.session.ExpiryDateCalculatorImpl;
import org.echocat.marquardt.authority.testdomain.IOExceptionThrowingTestUserInfo;
import org.echocat.marquardt.authority.testdomain.TestSession;
import org.echocat.marquardt.authority.testdomain.TestSignUpAccountData;
import org.echocat.marquardt.authority.testdomain.TestUser;
import org.echocat.marquardt.authority.testdomain.TestUserCredentials;
import org.echocat.marquardt.common.TestKeyPairProvider;
import org.echocat.marquardt.common.domain.Signature;
import org.echocat.marquardt.common.exceptions.AlreadyLoggedInException;
import org.echocat.marquardt.common.exceptions.LoginFailedException;
import org.echocat.marquardt.common.exceptions.NoSessionFoundException;
import org.echocat.marquardt.common.exceptions.SignatureValidationFailedException;
import org.echocat.marquardt.common.exceptions.UserAlreadyExistsException;
import org.echocat.marquardt.common.keyprovisioning.KeyPairProvider;
import org.echocat.marquardt.common.util.DateProvider;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuthorityUnitTest extends AuthorityTest {

    private static final long NEXT_YEAR = new Date().getTime() + TimeUnit.DAYS.toMillis(365);

    private static final DateProvider CUSTOM_DATE_PROVIDER = new DateProvider() {

        @SuppressWarnings("UseOfObsoleteDateTimeApi")
        @Override
        public Date now() {
            return new Date(NEXT_YEAR);
        }
    };

    @Mock
    private KeyPairProvider _issuerKeyProvider;
    @Mock
    private Signature _signature;

    private final ExpiryDateCalculatorImpl<TestUser> _expiryDateCalculator = new ExpiryDateCalculatorImpl<>();
    private Authority<TestUser, TestSession, TestUserCredentials, TestSignUpAccountData> _authority;

    private byte[] _certificate;

    @Before
    @Override
    public void setup() throws Exception {
        final KeyPairProvider keyPairProvider = TestKeyPairProvider.create();
        when(_issuerKeyProvider.getPrivateKey()).thenReturn(keyPairProvider.getPrivateKey());
        when(_issuerKeyProvider.getPublicKey()).thenReturn(keyPairProvider.getPublicKey());
        when(_signature.isValidFor(any(), any())).thenReturn(true);
        super.setup();
        _authority = new Authority<>(_userStore, _sessionStore, getSessionCreationPolicy(), _clientAccessPolicy, _issuerKeyProvider, _expiryDateCalculator);

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

    @Test(expected = ExpiredSessionException.class)
    public void shouldThrowExpiredSessionExceptionWhenCertificateIsOutdated() throws Exception {
        givenUserExists();
        givenExistingSession();
        givenCustomDateProvider();
        whenRefreshingCertificate();
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

    @Test
    public void shouldAllowToSetDateProvider() throws Exception {
        givenCustomDateProvider();
        givenUserExists();
        givenNoExistingSession();
        givenSessionCreationPolicyAllowsAnotherSession();
        whenSigningIn();
        thenSessionExpiringNextYearIsCreated();
    }

    private void givenCustomDateProvider() {
        _expiryDateCalculator.setDateProvider(CUSTOM_DATE_PROVIDER);
    }

    private void givenInvalidSignature() {
        when(_signature.isValidFor(any(), any())).thenReturn(false);
    }

    private void givenSignableThrowingException() {
        when(getUserStore().toSignable(any(TestUser.class))).thenReturn(new IOExceptionThrowingTestUserInfo());
    }

    private void whenSigningInWithWrongPassword() {
        _authority.signIn(CREDENTIALS_WITH_WRONG_PASSWORD);
    }

    private void whenSigningIn() {
        _certificate = _authority.signIn(TEST_USER_CREDENTIALS);
    }

    private void whenSigningUp() {
        _certificate = _authority.signUp(TEST_USER_ACCOUNT_DATA);
    }

    private void whenRefreshingCertificate() {
        _certificate = _authority.refresh(CERTIFICATE, new byte[0], _signature);
    }

    private void whenSigningOut() {
        _authority.signOut(CERTIFICATE, new byte[0], _signature);
    }

    private void thenUserIsStored() {
        verify(getUserStore()).createFrom(TEST_USER_ACCOUNT_DATA);
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

    private void thenSessionExpiringNextYearIsCreated() {
        verify(getSessionStore()).save(argThat(sessionExpiringNextYear()));
    }

    private Matcher<TestSession> sessionExpiringNextYear() {
        //noinspection UseOfObsoleteDateTimeApi
        return new FeatureMatcher<TestSession, Date>(equalTo(new Date(NEXT_YEAR + TimeUnit.DAYS.toMillis(60))), "session expiring next year", "session expiring") {

            @Override
            protected Date featureValueOf(final TestSession session) {
                return session.getExpiresAt();
            }
        };
    }
}