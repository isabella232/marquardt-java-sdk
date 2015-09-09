/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.authority.spring;

import org.echocat.marquardt.authority.Authority;
import org.echocat.marquardt.authority.exceptions.InvalidSessionException;
import org.echocat.marquardt.authority.persistence.UserStore;
import org.echocat.marquardt.authority.persistence.SessionStore;
import org.echocat.marquardt.common.domain.Credentials;
import org.echocat.marquardt.common.domain.JsonWrappedCertificate;
import org.echocat.marquardt.common.domain.KeyPairProvider;
import org.echocat.marquardt.authority.domain.User;
import org.echocat.marquardt.common.domain.Signable;
import org.echocat.marquardt.common.exceptions.AlreadyLoggedInException;
import org.echocat.marquardt.common.exceptions.InvalidCertificateException;
import org.echocat.marquardt.common.exceptions.LoginFailedException;
import org.echocat.marquardt.common.exceptions.UserExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.IOException;

public abstract class SpringAuthorityController<SIGNABLE extends Signable, USER extends User, CREDENTIALS extends Credentials> {

    private final SessionStore _sessionStore;
    private final KeyPairProvider _issuerKeyProvider;
    private UserStore<SIGNABLE, USER> _userStore;
    private Authority<SIGNABLE, USER> _authority;

    public SpringAuthorityController(final SessionStore sessionStore, final KeyPairProvider issuerKeyProvider, UserStore<SIGNABLE, USER> userStore) {
        _sessionStore = sessionStore;
        _issuerKeyProvider = issuerKeyProvider;
        _userStore = userStore;
        _authority = new Authority<>(_userStore, _sessionStore, _issuerKeyProvider);
    }

    @RequestMapping(value = "/signup", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    public JsonWrappedCertificate signUp(@RequestBody final CREDENTIALS credentials) {
        return _authority.signUp(credentials);
    }

    @RequestMapping(value = "/signin", method = RequestMethod.POST)
    @ResponseBody
    public JsonWrappedCertificate signIn(@RequestBody final CREDENTIALS credentials) {
        return _authority.signIn(credentials);
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
    public void handleAlreadyLoggedInException(final LoginFailedException ex) {
        // TODO log
    }

    @ExceptionHandler(AlreadyLoggedInException.class)
    @ResponseStatus(value = HttpStatus.PRECONDITION_FAILED, reason = "Already logged in.")
    public void handleAlreadyLoggedInException(final AlreadyLoggedInException ex) {
        // TODO log
    }

    @ExceptionHandler(InvalidCertificateException.class)
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "Invalid jsonWrappedCertificate.")
    public void handleInvalidCertificateException(final InvalidCertificateException ex) {
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
