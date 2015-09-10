/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.authority.persistence;

import org.echocat.marquardt.authority.domain.Session;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;


public interface SessionStore<T extends Session> {

    Optional<T> findByCertificate(byte[] certificate);

    boolean activeSessionExists(UUID userId, byte[] publicKey, Date expiresAt);

    T save(T session);

    T create();

    void delete(T session);
}
