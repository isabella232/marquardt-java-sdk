/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.example;

import org.echocat.marquardt.common.exceptions.*;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
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
    public void shouldSignUpUser() throws IOException {
        givenAccountDataWithCredentials();
        givenClientIdIsAllowed();
        whenSigningUp();
        thenCertificateIsProvided();
    }

    @Test(expected = UserAlreadyExistsException.class)
    public void shouldNotSignUpExistingUser() throws IOException {
        givenExistingUser(Collections.emptySet());
        givenClientIdIsAllowed();
        givenAccountDataWithCredentials();
        whenSigningUp();
    }

    @Test(expected = ClientNotAuthorizedException.class)
    public void shouldNotSignUpUserWhenClientIdIsUnknown() throws IOException {
        givenAccountDataWithCredentials();
        whenSigningUp();
    }

    @Test(expected = ClientNotAuthorizedException.class)
    public void shouldNotSignUpUserWhenClientIdProhibited() throws IOException {
        givenProhibitedClientId();
        givenAccountDataWithCredentials();
        whenSigningUp();
    }

    @Test
    public void shouldLogoutALoggedInUser() throws IOException {
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
}
