/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.example;

import org.echocat.marquardt.authority.persistence.SessionStore;
import org.echocat.marquardt.authority.persistence.UserStore;
import org.echocat.marquardt.authority.policies.ClientAccessPolicy;
import org.echocat.marquardt.authority.policies.SessionCreationPolicy;
import org.echocat.marquardt.authority.session.ExpiryDateCalculator;
import org.echocat.marquardt.authority.spring.SpringAuthorityController;
import org.echocat.marquardt.common.keyprovisioning.KeyPairProvider;
import org.echocat.marquardt.example.domain.PersistentSession;
import org.echocat.marquardt.example.domain.PersistentUser;
import org.echocat.marquardt.example.domain.UserCredentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth")
public class ExampleAuthorityController extends SpringAuthorityController<PersistentUser, PersistentSession, UserCredentials, UserCredentials> {

    @Autowired
    public ExampleAuthorityController(final UserStore<PersistentUser, UserCredentials> userStore, final SessionStore<PersistentSession> sessionStore, SessionCreationPolicy sessionCreationPolicy,
                                      final ClientAccessPolicy clientAccessPolicy, final KeyPairProvider issuerKeyProvider, final ExpiryDateCalculator<PersistentUser> expiryDateCalculator) {
        super(userStore, sessionStore, sessionCreationPolicy, clientAccessPolicy, issuerKeyProvider, expiryDateCalculator);
    }
}
