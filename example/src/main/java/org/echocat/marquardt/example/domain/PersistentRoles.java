/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.example.domain;

import org.echocat.marquardt.common.domain.Role;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public enum PersistentRoles implements Role {
    USER(0), ADMIN(1);

    @Id
    private final int _id;

    PersistentRoles(final int id) {
        _id = id;
    }

    @Override
    public Integer id() {
        return _id;
    }

    public static PersistentRoles fromId(final int id) {
        for(final PersistentRoles role: PersistentRoles.values()) {
            if(role.id() == id) {
                return role;
            }
        }
        return null;
    }
}
