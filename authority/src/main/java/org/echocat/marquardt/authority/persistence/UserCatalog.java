/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.authority.persistence;

import org.echocat.marquardt.authority.domain.User;
import org.echocat.marquardt.common.domain.Credentials;
import org.echocat.marquardt.common.domain.Signable;
import org.echocat.marquardt.common.domain.certificate.Role;

import java.util.Optional;
import java.util.UUID;

public interface UserCatalog<USER extends User<? extends Role>>  {

    Optional<USER> findByCredentials(final Credentials credentials);

    Optional<USER> findByUuid(final UUID userId);

    /**
     * Creates a signable from a user in the authority.
     * @param user User to create signable from.
     * @return Signable instance
     */
    Signable toSignable(final USER user);
}