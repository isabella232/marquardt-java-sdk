/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.common;

import org.echocat.marquardt.common.domain.certificate.Role;

public enum  TestRoles implements Role {
    TEST_ROLE_1(0), TEST_ROLE_2(1), ROLE_WITH_NEGATIVE_ID(-2), ROLE_WITH_ID_GREATER_THAN_63(64);

    private final int _id;

    TestRoles(final int id) {
        _id = id;
    }

    @Override
    public Integer id() {
        return _id;
    }

    public static TestRoles fromId(final int id) {
        for(final TestRoles role: TestRoles.values()) {
            if(role.id() == id) {
                return role;
            }
        }
        return null;
    }
}
