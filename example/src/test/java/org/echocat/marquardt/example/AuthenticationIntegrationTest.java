/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.example;

import org.echocat.marquardt.common.exceptions.AlreadyLoggedInException;
import org.echocat.marquardt.common.exceptions.LoginFailedException;
import org.echocat.marquardt.common.exceptions.NoSessionFoundException;
import org.echocat.marquardt.common.exceptions.UserAlreadyExistsException;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

public class AuthenticationIntegrationTest extends AbstractSsoIntegrationTest {

    @Test
    public void shouldSignInWithCorrectCredentials() throws IOException {
        givenExistingUser(Collections.emptySet());
        givenCorrectCredentials();
        whenSigningIn();
        thenCertificateIsProvided();
    }

    @Test(expected = LoginFailedException.class)
    public void shouldRejectLoginWithIncorrectCredentials() throws IOException {
        givenExistingUser(Collections.emptySet());
        givenIncorrectCredentials();
        whenSigningIn();
    }

    @Test(expected = AlreadyLoggedInException.class)
    public void shouldRejectLoginWhenUserIsLoggedIn() throws IOException {
        givenExistingUser(Collections.emptySet());
        givenCorrectCredentials();
        whenSigningIn();
        whenSigningIn();
    }

    @Test
    public void shouldSignUpUser() throws IOException {
        givenCorrectCredentials();
        whenSigningUp();
        thenCertificateIsProvided();
    }

    @Test(expected = UserAlreadyExistsException.class)
    public void shouldNotSignupExistingUser() throws IOException {
        givenExistingUser(Collections.emptySet());
        givenCorrectCredentials();
        whenSigningUp();
    }

    @Test
    public void shouldLogoutALoggedInUser() throws IOException {
        givenExistingUser(Collections.emptySet());
        givenCorrectCredentials();
        whenSigningIn();
        whenLoggingOut();
        thenCertificateIsProvided();
    }

    @Test
    public void shouldRefreshCertificateOfSignedInUsers() throws IOException {
        givenExistingUser(Collections.emptySet());
        givenCorrectCredentials();
        whenSigningIn();
        whenRefreshingCertificate();
        thenCertificateIsProvided();
    }

    @Test(expected = NoSessionFoundException.class)
    public void shouldNotRefreshCertificatesOfUsersThatAreSignedOut() throws Exception {
        givenExistingUser(Collections.emptySet());
        givenCorrectCredentials();
        whenSigningIn();
        whenLoggingOut();
        whenRefreshingCertificate();
    }

    private void whenRefreshingCertificate() throws IOException {
        setCertificate(getClient().refresh(_certificate));
    }


    private void thenCertificateIsProvided() {
        assertThat(getCertificate(), is(not(nullValue())));
    }

}
