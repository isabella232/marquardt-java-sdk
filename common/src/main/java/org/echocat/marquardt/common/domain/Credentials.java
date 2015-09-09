/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.common.domain;

import java.security.PublicKey;

/**
 * Used to transport credentials for SignIn and SignUp from Client to Authority.
 *
 * Please make sure passwords are crypted or use secure transport channels.
 */
public interface Credentials {

    /**
     * Unique identifier of the User.
     *
     * @return unique identifier
     */
    String getIdentifier();

    /**
     * Password of User. Make sure this is crypted or sent via a secure channel.
     *
     * @return password of user
     */
    String getPassword();

    /**
     * Public key of the client which sent the credentials.
     *
     * @see KeyPairProvider
     * @return public key of the client used to signup / signin.
     */
    PublicKey getPublicKey();
}
