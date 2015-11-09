/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.example.persistence;

import org.echocat.marquardt.authority.domain.Client;
import org.echocat.marquardt.authority.policies.ClientIdPolicy;
import org.echocat.marquardt.example.persistence.jpa.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ClientIdPolicyImpl implements ClientIdPolicy {

    private final ClientRepository _clientRepository;

    @Autowired
    public ClientIdPolicyImpl(final ClientRepository clientRepository) {
        _clientRepository = clientRepository;
    }

    @Override
    public boolean isAllowed(final String clientId) {
        return _clientRepository.findById(clientId).map(Client::isAllowed).orElse(false);
    }
}