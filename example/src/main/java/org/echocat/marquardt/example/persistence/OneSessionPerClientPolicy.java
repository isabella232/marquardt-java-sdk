/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.example.persistence;

import org.echocat.marquardt.authority.persistence.SessionCreationPolicy;
import org.echocat.marquardt.authority.persistence.SessionStore;
import org.echocat.marquardt.common.util.DateProvider;
import org.echocat.marquardt.example.domain.PersistentSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class OneSessionPerClientPolicy implements SessionCreationPolicy {

    private final SessionStore<PersistentSession> _sessionStore;
    private DateProvider _dateProvider = new DateProvider();

    @Autowired
    public OneSessionPerClientPolicy(SessionStore<PersistentSession> sessionStore) {
        _sessionStore = sessionStore;
    }

    @Override
    public boolean mayCreateSession(UUID userId, byte[] clientPublicKey) {
        return !_sessionStore.existsActiveSession(userId, clientPublicKey, _dateProvider.now());
    }

    public void setDateProvider(DateProvider dateProvider) {
        _dateProvider = dateProvider;
    }
}
