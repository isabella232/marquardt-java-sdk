/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.example;

import org.echocat.marquardt.authority.PojoAuthority;
import org.echocat.marquardt.authority.persistence.PrincipalStore;
import org.echocat.marquardt.authority.exceptions.InvalidCertificateException;
import org.echocat.marquardt.common.exceptions.InvalidSessionException;
import org.echocat.marquardt.authority.persistence.SessionStore;
import org.echocat.marquardt.common.ContentSigner;
import org.echocat.marquardt.common.domain.JsonWrappedCertificate;
import org.echocat.marquardt.common.domain.KeyPairProvider;
import org.echocat.marquardt.common.exceptions.AlreadyLoggedInException;
import org.echocat.marquardt.common.exceptions.LoginFailedException;
import org.echocat.marquardt.common.exceptions.UserExistsException;
import org.echocat.marquardt.example.domain.User;
import org.echocat.marquardt.example.domain.UserCredentials;
import org.echocat.marquardt.example.domain.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.IOException;

@Controller
@RequestMapping("/auth")
public class AuthenticationController {

    private final SessionStore _sessionStore;
    private final ContentSigner _contentSigner;
    private final PasswordEncoder _passwordEncoder;
    private final KeyPairProvider _issuerKeyProvider;

    private PrincipalStore<UserInfo, User> _principalStore;
    private PojoAuthority<UserInfo, User> _authority;

    @Autowired
    public AuthenticationController(final SessionStore sessionStore, final ContentSigner contentSigner, final PasswordEncoder passwordEncoder, final KeyPairProvider issuerKeyProvider, PrincipalStore<UserInfo, User> principalStore) {
        _sessionStore = sessionStore;
        _contentSigner = contentSigner;
        _passwordEncoder = passwordEncoder;
        _issuerKeyProvider = issuerKeyProvider;
        _principalStore = principalStore;
        _authority = new PojoAuthority<>(_principalStore, _sessionStore, _issuerKeyProvider);
    }

    @RequestMapping(value = "/signup", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    public JsonWrappedCertificate signUp(@RequestBody final UserCredentials userCredentials) {
        return _authority.signUp(userCredentials);
    }

    @RequestMapping(value = "/signin", method = RequestMethod.POST)
    @ResponseBody
    public JsonWrappedCertificate signIn(@RequestBody final UserCredentials userCredentials) {
        return _authority.signIn(userCredentials);
    }

    @RequestMapping(value = "/refresh", method = RequestMethod.POST)
    @ResponseBody
    public JsonWrappedCertificate refresh(@RequestHeader("X-Certificate") final byte[] certificate) {
        return _authority.refresh(certificate);
    }

    @RequestMapping(value = "/signout", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void signOut(@RequestHeader("X-Certificate") final byte[] certificate) {
        _authority.signOut(certificate);
    }

    @ExceptionHandler(UserExistsException.class)
    @ResponseStatus(value = HttpStatus.CONFLICT, reason = "User already exists.")
    public void handleUserExistsException(final UserExistsException ex) {
        // TODO log
    }

    @ExceptionHandler(LoginFailedException.class)
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "Login failed.")
    public void handleLoginFailedException(final LoginFailedException ex) {
        // TODO log
    }

    @ExceptionHandler(AlreadyLoggedInException.class)
    @ResponseStatus(value = HttpStatus.PRECONDITION_FAILED, reason = "Already logged in.")
    public void handleLoginFailedException(final AlreadyLoggedInException ex) {
        // TODO log
    }

    @ExceptionHandler(InvalidCertificateException.class)
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "Invalid jsonWrappedCertificate.")
    public void handleInvalidTokenException(final InvalidCertificateException ex) {
        // TODO log
    }

    @ExceptionHandler(IOException.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public void handleIOException(final IOException ex) {
        // TODO log
    }

    @ExceptionHandler(InvalidSessionException.class)
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "PersistentSession invalid.")
    public void handleInvalidSessionException(final InvalidSessionException ex) {
        // TODO log
    }


}
