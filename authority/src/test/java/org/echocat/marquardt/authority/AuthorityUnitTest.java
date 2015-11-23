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
import org.echocat.marquardt.authority.testdomain.IOExceptionThrowingTestUserInfo;
import org.echocat.marquardt.authority.testdomain.TestSession;
import org.echocat.marquardt.authority.testdomain.TestSignUpAccountData;
import org.echocat.marquardt.authority.testdomain.TestUser;
import org.echocat.marquardt.authority.testdomain.TestUserCredentials;
import org.echocat.marquardt.common.domain.Signature;
import org.echocat.marquardt.common.exceptions.AlreadyLoggedInException;
import org.echocat.marquardt.common.exceptions.ClientNotAuthorizedException;
import org.echocat.marquardt.common.exceptions.LoginFailedException;
import org.echocat.marquardt.common.exceptions.NoSessionFoundException;
import org.echocat.marquardt.common.exceptions.SignatureValidationFailedException;
import org.echocat.marquardt.common.exceptions.UserAlreadyExistsException;
import org.echocat.marquardt.common.util.DateProvider;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.validation.ValidationException;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
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
    private Signature _signature;

    private Authority<TestUser, TestSession, TestUserCredentials, TestSignUpAccountData> _authority;

    private byte[] _certificate;

    @Before
    @Override
    public void setup() throws Exception {
        super.setup();
        when(_signature.isValidFor(any(), any())).thenReturn(true);
        _authority = new Authority<>(_userCatalog, _userCreator, getSessionCreator(), getSessionRenewal(), _sessionStore, _clientAccessPolicy);
    }

    @Test
    public void shouldSignUpUser() throws Exception {
        givenUserDoesNotExist();
        whenSigningUp();
        thenUserIsStored();
        thenCertificateIsMade();
    }

    @Test(expected = CertificateCreationException.class)
    public void shouldThrowCertificateCreationExceptionWhenSignUpAndSignableSerializationFails() throws Exception {
        givenUserDoesNotExist();
        givenSignableThrowingException();
        whenSigningUp();
    }

    @Test(expected = UserAlreadyExistsException.class)
    public void shouldThrowExceptionWhenUserAlreadyExistsWhenSignUp() throws Exception {
        givenUserExists();
        whenSigningUp();
    }

    @Test(expected = ClientNotAuthorizedException.class)
    public void shouldThrowExceptionOnSignUpWhenClientPolicyProhibitsId() {
        givenClientIdIsProhibited();
        givenUserDoesNotExist();
        whenSigningUp();
    }

    @Test
    public void shouldSignInUser() throws Exception {
        givenUserExists();
        givenNoExistingSession();
        givenSessionCreationPolicyAllowsAnotherSession();
        whenSigningIn();
        thenSessionIsCreated();
        thenCertificateIsMade();
    }

    @Test(expected = ClientNotAuthorizedException.class)
    public void shouldThrowExceptionOnSignInWhenClientPolicyProhibitsId() {
        givenClientIdIsProhibited();
        givenUserExists();
        givenNoExistingSession();
        whenSigningIn();
    }

    @Test(expected = CertificateCreationException.class)
    public void shouldThrowCertificateCreationFailedExceptionWhenSigningInButPayloadCannotBeSigned() throws Exception {
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
        givenOneSessionPerClientAllowed();
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

    @Test(expected = ClientNotAuthorizedException.class)
    public void shouldThrowExceptionOnRefreshWhenClientPolicyProhibitsId() {
        givenClientIdIsProhibited();
        givenUserExists();
        givenExistingSession();
        whenRefreshingCertificate();
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

    @Test(expected = ValidationException.class)
    public void shouldAllowUserCheckConsumerToThrowExceptionOnRefresh() {
        givenAuthorityIsConfiguredWithExceptionThrowingConsumer();
        givenUserExists();
        givenExistingSession();
        whenRefreshingCertificate();
    }

    @Test(expected = ValidationException.class)
    public void shouldAllowUserCheckConsumerToThrowExceptionOnSignIn() {
        givenAuthorityIsConfiguredWithExceptionThrowingConsumer();
        givenUserExists();
        givenNoExistingSession();
        whenSigningIn();
    }

    private void givenOneSessionPerClientAllowed() {
        when(_sessionCreationPolicy.mayCreateSession(eq(TestUser.USER_ID), any())).thenReturn(false);
    }

    private void givenClientIdIsProhibited() {
        doReturn(false).when(_clientAccessPolicy).isAllowed(TEST_CLIENT_ID);
    }

    private void givenAuthorityIsConfiguredWithExceptionThrowingConsumer() {
        final Consumer<TestUser> consumer = testUser -> {
            throw new ValidationException();
        };
        getSessionRenewal().setCheckUserToFulfillAllRequirementsToSignInOrRefreshConsumer(consumer);
        _authority.setCheckUserToFulfillsAllRequirementsToSignInOrRefreshConsumer(consumer);
    }

    private void givenCustomDateProvider() {
        _expiryDateCalculator.setDateProvider(CUSTOM_DATE_PROVIDER);
    }

    private void givenInvalidSignature() {
        when(_signature.isValidFor(any(), any())).thenReturn(false);
    }

    private void givenSignableThrowingException() {
        when(getUserCatalog().toSignable(any(TestUser.class))).thenReturn(new IOExceptionThrowingTestUserInfo());
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
        verify(getUserCreator()).createFrom(TEST_USER_ACCOUNT_DATA);
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
        assertThat(_certificate, notNullValue());
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