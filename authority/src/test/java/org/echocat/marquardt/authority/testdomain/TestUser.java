/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.authority.testdomain;

import org.echocat.marquardt.authority.domain.User;
import org.echocat.marquardt.common.TestRoles;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

public class TestUser implements User<TestRoles> {

    public static final UUID USER_ID = UUID.fromString("ccacd003-3a28-48b2-9c0c-35dfac149190");

    @Override
    public UUID getUserId() {
        return USER_ID;
    }

    @Override
    public boolean passwordMatches(final String password) {
        return "right".equals(password);
    }

    @Override
    public Set<TestRoles> getRoles() {
        return Collections.emptySet();
    }
}
