/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.client.util;

import org.echocat.marquardt.common.exceptions.AlreadyLoggedInException;
import org.echocat.marquardt.common.exceptions.LoginFailedException;
import org.echocat.marquardt.common.exceptions.NoSessionFoundException;
import org.echocat.marquardt.common.exceptions.UserAlreadyExistsException;
import org.junit.Test;

import static org.echocat.marquardt.client.util.ResponseStatusTranslation.*;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.Is.isA;
import static org.junit.Assert.assertThat;

public class ResponseStatusTranslationUnitTest {

    private int _statusCode;
    private ResponseStatusTranslation _translation;

    @Test
    public void shouldTranslateBadRequest() throws Exception {
        givenBadRequest();
        whenTranslating();
        thenTranslationIs(BAD_REQUEST);
        thenTranslatedExceptionIs(IllegalArgumentException.class);
    }

    @Test
    public void shouldTranslateForbidden() throws Exception {
        givenForbidden();
        whenTranslating();
        thenTranslationIs(FORBIDDEN);
        thenTranslatedExceptionIs(IllegalArgumentException.class);
    }

    @Test
    public void shouldTranslateUnauthorized() throws Exception {
        givenUnauthorized();
        whenTranslating();
        thenTranslationIs(UNAUTHORIZED);
        thenTranslatedExceptionIs(LoginFailedException.class);
    }

    @Test
    public void shouldTranslateConflict() throws Exception {
        givenConflict();
        whenTranslating();
        thenTranslationIs(CONFLICT);
        thenTranslatedExceptionIs(UserAlreadyExistsException.class);
    }

    @Test
    public void shouldTranslatePreconditionFailed() throws Exception {
        givenPreconditionFailed();
        whenTranslating();
        thenTranslationIs(PRECONDITION_FAILED);
        thenTranslatedExceptionIs(AlreadyLoggedInException.class);
    }

    @Test
    public void shouldTranslateNoSessionFound() throws Exception {
        givenNotFound();
        whenTranslating();
        thenTranslationIs(NOT_FOUND);
        thenTranslatedExceptionIs(NoSessionFoundException.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenResponseCodeIsUnknown() throws Exception {
        givenUnknownStatusCode();
        whenTranslating();
    }

    private void givenUnknownStatusCode() {
        _statusCode = 666;
    }

    private void givenBadRequest() {
        _statusCode = 400;
    }

    private void givenForbidden() {
        _statusCode = 403;
    }

    private void givenUnauthorized() {
        _statusCode = 401;
    }

    private void givenNotFound() {
        _statusCode = 404;
    }

    private void givenConflict() {
        _statusCode = 409;
    }

    private void givenPreconditionFailed() {
        _statusCode = 412;
    }

    private void whenTranslating() {
        _translation = ResponseStatusTranslation.from(_statusCode);
    }

    private void thenTranslationIs(final ResponseStatusTranslation translation) {
        assertThat(_translation, is(translation));
    }

    @SuppressWarnings("unchecked")
    private void thenTranslatedExceptionIs(final Class<? extends RuntimeException> exceptionClass) {
        assertThat(_translation.translateToException("test"), isA((Class<? super RuntimeException>)exceptionClass));
    }
}