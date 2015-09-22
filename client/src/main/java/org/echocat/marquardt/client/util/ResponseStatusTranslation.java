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
import org.echocat.marquardt.common.exceptions.UserExistsException;

/**
 * Provides a mapping from HTTP status codes to Java Exceptions to be use in implementations of
 * {@link org.echocat.marquardt.client.Client} to translate http status codes to exceptions.
 *
 */
public enum ResponseStatusTranslation {

    BAD_REQUEST(400) {
        @Override
        public RuntimeException translateToException(final String message) {
            return new IllegalArgumentException(message);
        }
    },
    UNAUTHORIZED(401) {
        @Override
        public RuntimeException translateToException(final String message) {
            return new LoginFailedException(message);
        }
    },
    FORBIDDEN(403) {
        @Override
        public RuntimeException translateToException(final String message) { return new IllegalArgumentException(message); }
    },
    NOT_FOUND(404) {
        @Override
        public RuntimeException translateToException(final String message) { return new NoSessionFoundException(message); }
    },
    CONFLICT(409) {
        @Override
        public RuntimeException translateToException(final String message) {
            return new UserExistsException(message);
        }
    },
    PRECONDITION_FAILED(412) {
        @Override
        public RuntimeException translateToException(final String message) {
            return new AlreadyLoggedInException(message);
        }
    };

    private final Integer _statusCode;

    ResponseStatusTranslation(final Integer statusCode) {
        _statusCode = statusCode;
    }

    /**
     * Returns the HTTP status code.
     *
     * @return the status code.
     */
    public Integer getStatusCode() {
        return _statusCode;
    }

    /**
     * Returns the response status translation for a given status code.
     */
    public static ResponseStatusTranslation from(final Integer statusCode) {
        for(final ResponseStatusTranslation statusToExceptionMapper: values()) {
            if (statusToExceptionMapper.getStatusCode().equals(statusCode)) {
                return statusToExceptionMapper;
            }
        }
        throw new IllegalArgumentException("unexpected response status received: " + statusCode);
    }

    /**
     * Implementations of this method should return the corresponding RuntimeException for the status code and
     * reuse the provided message for the exception.
     */
    public abstract RuntimeException translateToException(String message);
}
