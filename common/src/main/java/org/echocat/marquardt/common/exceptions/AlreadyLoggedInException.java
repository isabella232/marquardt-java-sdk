/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.common.exceptions;

/**
 * Exception intended to be thrown when a user tries to sign in but already has a valid session.
 */
public class AlreadyLoggedInException extends RuntimeException {
    public AlreadyLoggedInException(final String message) {
        super(message);
    }
}
