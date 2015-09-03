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
import org.echocat.marquardt.common.exceptions.UserExistsException;

public enum ResponseStatusTranslation {

    BAD_REQUEST(400) {
        @Override
        public RuntimeException translateToException(String message) {
            return new IllegalArgumentException(message);
        }
    },
    UNAUTHORIZED(401) {
        @Override
        public RuntimeException translateToException(String message) {
            return new LoginFailedException(message);
        }
    },
    FORBIDDEN(403) {
        @Override
        public RuntimeException translateToException(String message) { return new IllegalArgumentException(message); }
    },
    CONFLICT(409) {
        @Override
        public RuntimeException translateToException(String message) {
            return new UserExistsException(message);
        }
    },
    PRECONDITION_FAILED(412) {
        @Override
        public RuntimeException translateToException(String message) {
            return new AlreadyLoggedInException(message);
        }
    };

    private Integer _statusCode;

    ResponseStatusTranslation(Integer statusCode) {
        _statusCode = statusCode;
    }

    public Integer getStatusCode() {
        return _statusCode;
    }

    public static ResponseStatusTranslation from(Integer statusCode) {
        for(ResponseStatusTranslation statusToExceptionMapper: values()) {
            if (statusToExceptionMapper.getStatusCode().equals(statusCode)) {
                return statusToExceptionMapper;
            }
        }
        throw new IllegalArgumentException("unexpected response status received: " + statusCode);
    }

    public abstract RuntimeException translateToException(String message);
}
