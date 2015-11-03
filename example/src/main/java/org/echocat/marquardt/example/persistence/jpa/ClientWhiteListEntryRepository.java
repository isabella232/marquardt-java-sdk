/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.example.persistence.jpa;

import org.echocat.marquardt.authority.domain.ClientWhiteListEntry;
import org.echocat.marquardt.example.domain.PersistentClientWhitelistEntry;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

@Component
public interface ClientWhiteListEntryRepository extends CrudRepository<PersistentClientWhitelistEntry, Long> {

    ClientWhiteListEntry findByClientId(String clientId);
}
