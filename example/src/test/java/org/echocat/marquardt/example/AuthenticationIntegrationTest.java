/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.example;

import org.echocat.marquardt.common.exceptions.AlreadyLoggedInException;
import org.echocat.marquardt.common.exceptions.ClientNotAuthorizedException;
import org.echocat.marquardt.common.exceptions.LoginFailedException;
import org.echocat.marquardt.common.exceptions.NoSessionFoundException;
import org.echocat.marquardt.common.exceptions.UserAlreadyExistsException;
import org.echocat.marquardt.example.domain.PersistentUser;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

import static org.echocat.marquardt.authority.domain.UserStatus.CONFIRMED;
import static org.echocat.marquardt.authority.domain.UserStatus.WITHOUT_CREDENTIALS;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class AuthenticationIntegrationTest extends AbstractSsoIntegrationTest {

    @Test
    public void shouldSignInWithCorrectCredentials() throws IOException {
        givenExistingUser(Collections.emptySet());
        givenClientIdIsAllowed();
        givenCorrectCredentials();
        whenSigningIn();
        thenCertificateIsProvided();
    }

    @Test(expected = LoginFailedException.class)
    public void shouldRejectLoginWithIncorrectCredentials() throws IOException {
        givenExistingUser(Collections.emptySet());
        givenClientIdIsAllowed();
        givenIncorrectCredentials();
        whenSigningIn();
    }

    @Test
    public void shouldRejectLoginWhenUserIsLoggedIn() throws IOException {
        givenExistingUser(Collections.emptySet());
        givenClientIdIsAllowed();
        givenCorrectCredentials();
        whenSigningIn();
        try { // Do not use @Test(expected = ) as the 1st sign-in invocation might as well throw this exception!
            whenSigningIn();
            fail("Expected " + AlreadyLoggedInException.class + " was not thrown!");
        } catch (final AlreadyLoggedInException ignored) {}
    }

    /**
     * No client id at all is defined for the test
     */
    @Test(expected = ClientNotAuthorizedException.class)
    public void shouldNotSignInWithCorrectCredentialsWhenClientIdIsUnknown() throws IOException {
        givenExistingUser(Collections.emptySet());
        givenCorrectCredentials();
        whenSigningIn();
    }

    @Test(expected = ClientNotAuthorizedException.class)
    public void shouldNotSignInWithCorrectCredentialsWhenClientIdProhibited() throws IOException {
        givenProhibitedClientId();
        givenExistingUser(Collections.emptySet());
        givenCorrectCredentials();
        whenSigningIn();
    }

    @Test
    public void shouldInitializeSignUp() throws IOException {
        givenCorrectCredentials();
        givenClientIdIsAllowed();
        whenInitializingSignUp();
        thenCertificateIsProvided();
        thenUserWithoutCredentialsWasCreated();
    }

    @Test
    public void shouldFinalizeSignUp() throws IOException {
        givenAccountDataWithCredentials();
        givenClientIdIsAllowed();
        givenEmptyUserAndSession();
        whenFinalizingSignUp();
        thenCertificateIsProvided();
        thenUserWasEnrichedByAccountData();
    }

    @Test(expected = UserAlreadyExistsException.class)
    public void shouldNotFinalizeSignUpWhenCredentialsAreInUseByExistingUser() throws IOException {
        givenExistingUser(Collections.emptySet());
        givenClientIdIsAllowed();
        givenAccountDataWithCredentials();
        givenEmptyUserAndSession();
        whenFinalizingSignUp();
    }

    @Test(expected = ClientNotAuthorizedException.class)
    public void shouldNotFinalizeSignUpWhenClientIdIsUnknown() throws IOException {
        givenAccountDataWithCredentials();
        givenEmptyUserAndSession();
        // No client id is set at all
        whenFinalizingSignUp();
    }

    @Test(expected = ClientNotAuthorizedException.class)
    public void shouldNotFinalizeSignUpWhenClientIdProhibited() throws IOException {
        givenAccountDataWithCredentials();
        givenEmptyUserAndSession();
        givenProhibitedClientId();
        whenFinalizingSignUp();
    }

    @Test
    public void shouldLogoutAnLoggedInUser() throws IOException {
        givenExistingUser(Collections.emptySet());
        givenClientIdIsAllowed();
        givenCorrectCredentials();
        whenSigningIn();
        whenLoggingOut();
        thenCertificateIsProvided();
    }

    @Test
    public void shouldRefreshCertificateOfSignedInUsers() throws IOException {
        givenExistingUser(Collections.emptySet());
        givenClientIdIsAllowed();
        givenCorrectCredentials();
        whenSigningIn();
        whenRefreshingCertificate();
        thenCertificateIsProvided();
    }

    @Test(expected = NoSessionFoundException.class)
    public void shouldNotRefreshCertificatesOfUsersThatAreSignedOut() throws Exception {
        givenExistingUser(Collections.emptySet());
        givenClientIdIsAllowed();
        givenCorrectCredentials();
        whenSigningIn();
        whenLoggingOut();
        whenRefreshingCertificate();
    }

    @Test
    public void shouldNotRefreshWhenClientIdIsProhibited() throws Exception {
        givenExistingUser(Collections.emptySet());
        givenClientIdIsAllowed();
        givenCorrectCredentials();
        whenSigningIn();
        givenProhibitedClientId();
        try { // Do not use @Test(expected = ) as sign-in might as well throw this exception!
            whenRefreshingCertificate();
            fail("Expected " + ClientNotAuthorizedException.class + " was not thrown!");
        } catch (final ClientNotAuthorizedException ignored) {}
    }

    private void whenRefreshingCertificate() throws IOException {
        //noinspection unchecked
        setCertificate(getClient().refresh(_certificate));
    }

    private void thenCertificateIsProvided() {
        assertThat(getCertificate(), is(not(nullValue())));
    }

    private void thenUserWithoutCredentialsWasCreated() {
        final UUID userId = getCertificate().getPayload().getUserId();
        final PersistentUser user = getUserRepository().findByUserId(userId).orElseThrow(() -> new IllegalStateException("Could not find expected user with id '" + userId + "'."));
        assertThat(user.getStatus(), is(WITHOUT_CREDENTIALS));
        assertThat(user.getEmail(), nullValue());
        assertThat(user.getEncodedPassword(), nullValue());
        assertThat(user.getLastName(), nullValue());
        assertThat(user.getFirstName(), nullValue());
    }

    private void thenUserWasEnrichedByAccountData() {
        final UUID userId = getCertificate().getPayload().getUserId();
        final PersistentUser user = getUserRepository().findByUserId(userId).orElseThrow(() -> new IllegalStateException("Could not find expected user with id '" + userId + "'."));
        assertThat(user.getStatus(), is(CONFIRMED));
        assertThat(user.getEmail(), is(_signUpAccountData.getCredentials().getIdentifier()));
        assertThat(user.getEncodedPassword(), notNullValue());
        assertThat(user.getFirstName(), is(_signUpAccountData.getFirstName()));
        assertThat(user.getLastName(), is(_signUpAccountData.getLastName()));
    }
}