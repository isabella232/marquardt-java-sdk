/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.example.persistence.jpa;

import org.echocat.marquardt.example.domain.PersistentSession;
import org.springframework.data.repository.CrudRepository;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("InterfaceNeverImplemented")
public interface PersistentSessionRepository extends CrudRepository<PersistentSession, Long> {

    Optional<PersistentSession> findByCertificate(byte[] certificate);

    Long countByUserIdAndPublicKeyAndExpiresAtGreaterThan(UUID userId, byte[] publicKey, @SuppressWarnings("UseOfObsoleteDateTimeApi") Date expiresAt);
}
