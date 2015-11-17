/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.example.persistence;

import org.echocat.marquardt.authority.persistence.UserCatalog;
import org.echocat.marquardt.common.domain.Credentials;
import org.echocat.marquardt.example.domain.PersistentUser;
import org.echocat.marquardt.example.domain.UserInfo;
import org.echocat.marquardt.example.persistence.jpa.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class PersistentUserCatalog implements UserCatalog<PersistentUser> {

    private final UserRepository _userRepository;

    @Autowired
    PersistentUserCatalog(final UserRepository userRepository) {
        _userRepository = userRepository;
    }

    @Override
    public Optional<PersistentUser> findByCredentials(final Credentials credentials) {
        return _userRepository.findByEmailIgnoreCase(credentials.getIdentifier());
    }

    @Override
    public Optional<PersistentUser> findByUuid(final UUID userId) {
        return _userRepository.findByUserId(userId);
    }

    @Override
    public UserInfo toSignable(final PersistentUser persistentUser) {
        return new UserInfo(persistentUser.getUserId());
    }
}