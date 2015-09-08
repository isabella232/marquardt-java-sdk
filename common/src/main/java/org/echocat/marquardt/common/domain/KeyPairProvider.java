/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.common.domain;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Both client and authority should implement this.
 *
 * Clients should create their keys on first startup, because the public key is used to identify the
 * corresponding session of a user on a certain client. Clients use the private key to sign their request header when
 * accessing protected resources on a service.
 *
 * The authority typically uses well-known public keys (all clients should trust this key)
 * and a corresponding secret private key only known to the authority.
 *
 *  @see org.echocat.marquardt.common.domain.TrustedKeysProvider
 */
public interface KeyPairProvider {

    /**
     * Provides the public key.
     *
     * @return java.security.PublicKey
     */
    PublicKey getPublicKey();

    /**
     * Provides the private key.
     *
     * @return java.security.PrivateKey
     */
    PrivateKey getPrivateKey();

}
