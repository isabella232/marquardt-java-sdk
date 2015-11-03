/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.example.persistence;

import org.echocat.marquardt.authority.domain.ClientWhiteListEntry;
import org.echocat.marquardt.authority.persistence.ClientWhiteList;
import org.echocat.marquardt.example.persistence.jpa.ClientWhiteListEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PersistentClientWhitelist implements ClientWhiteList {

    private ClientWhiteListEntryRepository _clientWhiteListEntryRepository;

    @Autowired
    public PersistentClientWhitelist(ClientWhiteListEntryRepository clientWhiteListEntryRepository) {
        _clientWhiteListEntryRepository = clientWhiteListEntryRepository;
    }

    @Override
    public ClientWhiteListEntry findByClientId(String clientId) {
        return _clientWhiteListEntryRepository.findByClientId(clientId);
    }
}
