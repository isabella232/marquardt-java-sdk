/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.authority.persistence;

import java.util.UUID;

public interface SessionCreationPolicy {

    /**
     * You can use this to implement different policies how access to create a session is granted.
     * This may be a one session per user per client policy, or maybe you want some clients to create more sessions than
     * others.
     *
     * @param userId User id of the user.
     * @param clientPublicKey Public key of the client.
     * @return True if (another) session may be created, false if not.
     */
    boolean mayCreateSession(UUID userId, byte[] clientPublicKey);

}
