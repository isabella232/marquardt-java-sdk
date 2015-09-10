/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.example.persistence;

import org.echocat.marquardt.authority.persistence.UserStore;
import org.echocat.marquardt.common.domain.Credentials;
import org.echocat.marquardt.example.domain.PersistentUser;
import org.echocat.marquardt.example.domain.UserInfo;
import org.echocat.marquardt.example.persistence.jpa.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class PersistentUserStore implements UserStore<PersistentUser, UserInfo> {

    private UserRepository _userRepository;
    private PasswordEncoder _passwordEncoder;

    @Autowired
    PersistentUserStore(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        _userRepository = userRepository;
        _passwordEncoder = passwordEncoder;
    }

    @Override
    public Optional<PersistentUser> findUserByCredentials(Credentials credentials) {
        return _userRepository.findByEmailIgnoreCase(credentials.getIdentifier());
    }

    @Override
    public Optional<PersistentUser> findUserByUuid(UUID userId) {
        return _userRepository.findByUserId(userId);
    }

    @Override
    public PersistentUser createUserFromCredentials(Credentials credentials) {
        final PersistentUser persistentUserToCreate = new PersistentUser();
        persistentUserToCreate.setEmail(credentials.getIdentifier());
        persistentUserToCreate.setEncodedPassword(_passwordEncoder.encode(credentials.getPassword()));
        persistentUserToCreate.setUserId(UUID.randomUUID()); // TODO! What is the stored format?
        persistentUserToCreate.setRoles(0L);
        return _userRepository.save(persistentUserToCreate);
    }

    @Override
    public UserInfo createSignableFromUser(PersistentUser persistentUser) {
        return new UserInfo(persistentUser.getUserId());
    }
}
