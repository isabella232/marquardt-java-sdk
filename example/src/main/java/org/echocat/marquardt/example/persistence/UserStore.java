/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.example.persistence;

import org.echocat.marquardt.common.domain.Credentials;
import org.echocat.marquardt.example.domain.PersistedUser;
import org.echocat.marquardt.example.domain.UserInfo;
import org.echocat.marquardt.example.persistence.jpa.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class UserStore implements org.echocat.marquardt.authority.persistence.UserStore<UserInfo, PersistedUser> {

    private UserRepository _userRepository;
    private PasswordEncoder _passwordEncoder;

    @Autowired
    UserStore(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        _userRepository = userRepository;
        _passwordEncoder = passwordEncoder;
    }

    @Override
    public Optional<PersistedUser> findUserByCredentials(Credentials credentials) {
        return _userRepository.findByEmailIgnoreCase(credentials.getIdentifier());
    }

    @Override
    public Optional<PersistedUser> findUserByUuid(UUID userId) {
        return _userRepository.findByUserId(userId);
    }

    @Override
    public PersistedUser createUserFromCredentials(Credentials credentials) {
        final PersistedUser persistedUserToCreate = new PersistedUser();
        persistedUserToCreate.setEmail(credentials.getIdentifier());
        persistedUserToCreate.setEncodedPassword(_passwordEncoder.encode(credentials.getPassword()));
        persistedUserToCreate.setUserId(UUID.randomUUID()); // TODO! What is the stored format?
        persistedUserToCreate.setRoles(0L);
        return _userRepository.save(persistedUserToCreate);
    }

    @Override
    public UserInfo createSignableFromUser(PersistedUser persistedUser) {
        return new UserInfo(persistedUser.getUserId());
    }
}
