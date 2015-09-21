/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.common.serialization;

import com.google.common.base.Preconditions;
import org.echocat.marquardt.common.domain.Role;

import java.util.Set;

public final class RolesSerializer {

    private static RolesSerializer INSTANCE = new RolesSerializer();

    public static long from(Set<Role> roles) {
        return INSTANCE.fromInternal(roles);
    }

    private RolesSerializer() {}

    private long fromInternal(Set<Role> roles) {
        if(roles == null) {
            throw new IllegalArgumentException("roles must not be null");
        }
        long result = 0;
        for (Role role : roles) {
            Preconditions.checkArgument(role.id().intValue() >= 0 && role.id().intValue() < 64);
            result += Math.pow(2, role.id().doubleValue());
        }
        return result;
    }

}
