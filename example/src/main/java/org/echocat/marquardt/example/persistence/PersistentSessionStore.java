/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.example.persistence;

import org.echocat.marquardt.authority.domain.Session;
import org.echocat.marquardt.authority.persistence.SessionStore;
import org.echocat.marquardt.example.domain.PersistentSession;
import org.echocat.marquardt.example.persistence.jpa.PersistentSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Component
public class PersistentSessionStore implements SessionStore {

    private PersistentSessionRepository _sessionRepository;

    @Autowired
    public PersistentSessionStore(PersistentSessionRepository sessionRepository) {
        _sessionRepository = sessionRepository;
    }

    @Override
    public Optional<Session> findByCertificate(byte[] certificate) {
        return _sessionRepository.findByCertificate(certificate);
    }

    @Override
    public boolean isActiveAndValidSessionExists(UUID userId, byte[] publicKey, Date expiresAt, boolean valid) {
        return _sessionRepository.countByUserIdAndPublicKeyAndExpiresAtGreaterThanAndValid(userId, publicKey, expiresAt, valid) > 0;
    }

    @Override
    public Session save(Session session) {
        return _sessionRepository.save((PersistentSession) session);
    }

    @Override
    public Session create() {
        return new PersistentSession();
    }

    public void deleteAll() {
        _sessionRepository.deleteAll();
    }

}
