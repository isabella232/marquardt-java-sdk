/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.authority.domain;

import org.echocat.marquardt.common.domain.PublicKeyWithMechanism;

import java.util.Date;
import java.util.UUID;

/**
 * A session should be created when a user signs up or signs in.
 * Should be unique by userId and client public key (so the same user may be logged in with
 * different clients at the same time with different sessions).
 *
 * Sessions expire after a long time (60 days). As long as a session exists users may refresh their
 * expired certificates without signing in again.
 *
 * When a user signs out sessions are deleted.
 *
 * Implement this in your Authority implementation with a domain object that can be stored into a database.
 *
 */
public interface Session {

    /**
     * Provides the user id this session is created for.
     *
     * @return User id of the user this session is created for.
     */
    UUID getUserId();

    /**
     * Sets the user id this session belongs to.
     *
     * @param userId User id of the user this session is created for.
     */
    void setUserId(final UUID userId);

    /**
     * Public key of the client this session was requested from.
     *
     * @return Public key of the client. Can be used to verify that the client who signed a request is really
     * the client this session is belonging to.
     */
    byte[] getPublicKey();

    /**
     * Public key of the client this session is created for. Should only be set on signup or signin!
     *
     * @param publicKey Of the client that signed in a user. Please note this is a blob and it's size depends on the
     *                  key-length of the client.
     */
    void setPublicKey(final byte[] publicKey);

    /**
     * Mechanism the public key was created with.
     *
     * @return PublicKeyWithMechanism.Mechanism
     *
     * @see PublicKeyWithMechanism
     */
    String getMechanism();

    /**
     * Mechanism the public key was created with.
     *
     * @param mechanism PublicKeyWithMechanism.Mechanism
     *
     * @see PublicKeyWithMechanism
     */
    void setMechanism(final String mechanism);

    /**
     * The current certificate belonging to this session.
     *
     * @return Current session's certificate.
     */
    byte[] getCertificate();

    /**
     * When a session is created or refreshed, the current certificate must be set.
     *
     * Please note that this certificate may be up to 1KB of data. Please make sure the underlying datastore
     * can persist such blobs.
     *
     * @param certificate Current certificate.
     */
    void setCertificate(final byte[] certificate);

    /**
     * Expiry date of this session.
     *
     * @return Expiry date of this session.
     */
    Date getExpiresAt();

    /**
     * Sets expiry date of this session. Should be updated on successful refreshs.
     *
     * @param expiresAt Expiry date of this session.
     */
    void setExpiresAt(final Date expiresAt);

    String getClientId();

    void setClientId(String clientId);
}
