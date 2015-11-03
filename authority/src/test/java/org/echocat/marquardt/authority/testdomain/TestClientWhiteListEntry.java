/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.authority.testdomain;

import org.echocat.marquardt.authority.domain.ClientWhiteListEntry;

public class TestClientWhiteListEntry implements ClientWhiteListEntry {

    private String _clientId;
    private boolean _isWhiteListed;

    public TestClientWhiteListEntry(String clientId, boolean isWhiteListed) {
        _clientId = clientId;
        _isWhiteListed = isWhiteListed;
    }

    @Override
    public String getClientId() {
        return _clientId;
    }

    @Override
    public void setClientId(String clientId) {
        _clientId = clientId;
    }

    @Override
    public boolean isWhitelisted() {
        return _isWhiteListed;
    }

    @Override
    public void setIsWhitelisted(boolean isWhitelisted) {
        _isWhiteListed = isWhitelisted;
    }
}
