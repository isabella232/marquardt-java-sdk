/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.authority.testdomain;

public class TestClient {

    private String _clientId;
    private boolean _allowed;

    public TestClient(final String clientId, final boolean allowed) {
        _clientId = clientId;
        _allowed = allowed;
    }

    public String getId() {
        return _clientId;
    }

    public void setId(final String clientId) {
        _clientId = clientId;
    }

    public boolean isAllowed() {
        return _allowed;
    }

    public void setAllowed(boolean allowed) {
        _allowed = allowed;
    }
}