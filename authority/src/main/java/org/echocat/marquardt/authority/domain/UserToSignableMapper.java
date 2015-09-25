/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.authority.domain;

import org.echocat.marquardt.common.domain.certificate.Certificate;
import org.echocat.marquardt.common.domain.Signable;

/**
 * Interface for transformation of a (persisted) user to a signable user information of choice to
 * put into the certificate.
 *
 * @see Certificate
 * @see Signable
 * @see User
 */
@FunctionalInterface
public interface UserToSignableMapper<USER extends User, SIGNABLE extends Signable> {

    /**
     * Creates a custom signable from a (persisted) user in the authority.
     * @param user User to create signable from.
     * @return Signable (for example to put into Certificate)
     */
    SIGNABLE createSignableFromUser(USER user);
}
