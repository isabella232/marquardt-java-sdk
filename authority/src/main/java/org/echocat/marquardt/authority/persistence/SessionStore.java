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

/**
 * Store (service) that provides access to Sessions.
 * @param <T> Your Session implementation.
 */
public interface SessionStore<T extends Session> {

    /**
     * Implement a finder based on a provided certificate. Used for refeshs.
     *
     * @param certificate Certificate of the session.
     * @return Optional with the session (when exists) or Optional.empty when no session exists with the given certificate.
     */
    Optional<T> findByCertificate(byte[] certificate);

    /**
     * Implement a query that checks if there is an active session for a user on a client with a public key.
     *
     * @param userId User id of the user.
     * @param publicKey Public key of the client.
     * @param dateToCheck Date to check - in most cases NOW.
     * @return True if user has an active session on the current client, false if not.
     */
    boolean activeSessionExists(UUID userId, byte[] publicKey, Date dateToCheck);

    /**
     * Implement to persist a new or updated session.
     * @param session Session to persist.
     * @return Session after persisting.
     */
    T save(T session);

    /**
     * Implement this to create a new transient session.
     * @return Created session.
     */
    T create();

    /**
     * Implement this to delete a session. You may also implement a invalidation here.
     *
     * @param session Session to delete.
     */
    void delete(T session);
}
