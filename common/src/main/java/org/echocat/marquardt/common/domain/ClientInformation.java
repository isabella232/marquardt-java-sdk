/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.common.domain;

import java.security.PublicKey;

public interface ClientInformation {

    /**
     * Client id  of the user.
     *
     * @return client id
     */
    String getClientId();

    /**
     * Public key of the client which sent the credentials.
     *
     * @see org.echocat.marquardt.common.keyprovisioning.KeyPairProvider
     * @return public key of the client used to sign-up / sign-in.
     */
    PublicKey getPublicKey();
}