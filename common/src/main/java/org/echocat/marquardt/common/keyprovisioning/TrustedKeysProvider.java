/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.common.keyprovisioning;

import java.security.PublicKey;
import java.util.Collection;

/**
 * Clients and services must trust the key(s) used by the authority. This enables services to trust a certificate
 * without asking the authority on each received certificate.
 *
 * @see KeyPairProvider
 */
public interface TrustedKeysProvider {

    /**
     *
     * @return A collection of all keys a client or service trusts. Must contain the current authority's public key.
     */
    Collection<PublicKey> getPublicKeys();

}