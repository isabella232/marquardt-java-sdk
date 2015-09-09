/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.common.exceptions;

/**
 * Exception intended to be thrown when a sign in fails. This may be due to an unknown user or wrong
 * user identifier / password combination
 */
public class LoginFailedException extends RuntimeException {

    public LoginFailedException(String message) {
        super(message);
    }
}
