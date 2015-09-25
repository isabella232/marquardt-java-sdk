/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.example.domain;

import org.echocat.marquardt.common.domain.certificate.Role;
import org.echocat.marquardt.common.serialization.RolesDeserializer;

public enum ExampleRoles implements Role {
    ROLE_USER(0), ROLE_ADMIN(1);

    public static final RolesDeserializer<ExampleRoles> FACTORY = new RolesDeserializer<ExampleRoles>() {
        @Override
        public ExampleRoles createRoleFromId(final Number id) {
            return ExampleRoles.fromId(id.intValue());
        }
    };

    private final int _id;

    ExampleRoles(final int id) {
        _id = id;
    }

    @Override
    public Integer id() {
        return _id;
    }

    public static ExampleRoles fromId(final int id) {
        for (final ExampleRoles role : ExampleRoles.values()) {
            if (role.id() == id) {
                return role;
            }
        }
        return null;
    }
}
