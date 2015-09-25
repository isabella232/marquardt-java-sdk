/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.common.serialization;

import org.echocat.marquardt.common.domain.certificate.Role;

import java.util.HashSet;
import java.util.Set;

public abstract class RolesDeserializer<T extends Role> {

    public Set<Role> from(final Long roleCodes) {
        if(roleCodes == null) {
            throw new IllegalArgumentException("roleCodes must not be null.");
        }
        final Set<Role> roles = new HashSet<Role>();
        for (int i = 0; i < Role.MAX_ROLE_ID; i++) {
            final long roleCodeOfCurrentRole = new Double(Math.pow(2, i)).longValue();
            if (roleCodes < roleCodeOfCurrentRole) {
                return roles;
            }
            if ((roleCodes & roleCodeOfCurrentRole) == roleCodeOfCurrentRole) {
                roles.add(createRoleFromId(i));
            }
        }
        throw new IllegalArgumentException("Provided role code is greater then the maximum allowed code.");
    }

    public abstract T createRoleFromId(Number id);
}
