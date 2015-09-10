/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.authority.testdomain;

import org.echocat.marquardt.authority.domain.User;

import java.util.UUID;

public class TestUser implements User {
    @Override
    public UUID getUserId() {
        return null;
    }

    @Override
    public boolean passwordMatches(String password) {
        return "right".equals(password);
    }

    @Override
    public long getRoles() {
        return 0;
    }
}
