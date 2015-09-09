/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.example.persistence.jpa;

import org.echocat.marquardt.example.domain.PersistedUser;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends CrudRepository<PersistedUser, Long> {
    Optional<PersistedUser> findByEmailIgnoreCase(String email);

    Optional<PersistedUser> findByUserId(UUID userId);
}
