/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.authority.exceptions;

public class NoSessionFoundException extends RuntimeException {

    public NoSessionFoundException() {
        super();
    }

    public NoSessionFoundException(String message) {
        super(message);
    }
}