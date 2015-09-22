/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.example.persistence.jpa;

import org.echocat.marquardt.example.domain.PersistentUser;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("InterfaceNeverImplemented")
public interface UserRepository extends CrudRepository<PersistentUser, Long> {
    Optional<PersistentUser> findByEmailIgnoreCase(String email);

    Optional<PersistentUser> findByUserId(UUID userId);
}
