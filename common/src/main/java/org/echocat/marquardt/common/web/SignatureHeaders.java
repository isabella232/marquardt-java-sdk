/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.common.web;

public enum SignatureHeaders {

    X_CERTIFICATE(RequestHeaders.X_CERTIFICATE),
    CONTENT_LENGTH("Content-Length"),
    DATE("Date"),
    CONTENT("Content-MD5"),
    RANGE("Range");

    private final String _headerName;

    private SignatureHeaders(String headerName) {
        _headerName = headerName;
    }

    public String getHeaderName() {
        return _headerName;
    }
}
