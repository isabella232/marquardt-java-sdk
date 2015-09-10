/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.example.persistence;

import org.echocat.marquardt.authority.persistence.SessionStore;
import org.echocat.marquardt.example.domain.PersistentSession;
import org.echocat.marquardt.example.persistence.jpa.PersistentSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Component
public class PersistentSessionStore implements SessionStore<PersistentSession> {

    private PersistentSessionRepository _sessionRepository;

    @Autowired
    public PersistentSessionStore(PersistentSessionRepository sessionRepository) {
        _sessionRepository = sessionRepository;
    }

    @Override
    public Optional<PersistentSession> findByCertificate(byte[] certificate) {
        return _sessionRepository.findByCertificate(certificate);
    }

    @Override
    public boolean activeSessionExists(UUID userId, byte[] publicKey, Date expiresAt) {
        return _sessionRepository.countByUserIdAndPublicKeyAndExpiresAtGreaterThan(userId, publicKey, expiresAt) > 0;
    }

    @Override
    public PersistentSession save(PersistentSession session) {
        return _sessionRepository.save(session);
    }

    @Override
    public PersistentSession create() {
        return new PersistentSession();
    }

    @Override
    public void delete(PersistentSession session) {
        _sessionRepository.delete(session);
    }

    public void deleteAll() {
        _sessionRepository.deleteAll();
    }

}
