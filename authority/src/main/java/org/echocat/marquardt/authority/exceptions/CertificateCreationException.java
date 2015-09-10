/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.authority.exceptions;

/**
 * Exception thrown when creating a certificate fails.
 *
 * This may be caused by corrupt data or IO problems of the authority.
 */
public class CertificateCreationException extends RuntimeException {
    public CertificateCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
