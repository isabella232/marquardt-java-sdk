/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.example;

import org.echocat.marquardt.authority.spring.SpringAuthorityController;
import org.echocat.marquardt.authority.persistence.UserStore;
import org.echocat.marquardt.authority.persistence.SessionStore;
import org.echocat.marquardt.common.Signer;
import org.echocat.marquardt.common.domain.KeyPairProvider;
import org.echocat.marquardt.example.domain.PersistedUser;
import org.echocat.marquardt.example.domain.UserCredentials;
import org.echocat.marquardt.example.domain.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth")
public class ExampleAuthorityController extends SpringAuthorityController<UserInfo, PersistedUser, UserCredentials> {

    @Autowired
    public ExampleAuthorityController(SessionStore sessionStore, KeyPairProvider issuerKeyProvider, UserStore<UserInfo, PersistedUser> userStore) {
        super(sessionStore, issuerKeyProvider, userStore);
    }
}
