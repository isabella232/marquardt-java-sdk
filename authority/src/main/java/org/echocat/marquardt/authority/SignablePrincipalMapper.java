/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.authority;

import org.echocat.marquardt.common.domain.Credentials;
import org.echocat.marquardt.common.domain.Signable;

import java.util.Optional;
import java.util.UUID;

public interface SignablePrincipalMapper<SIGNABLE extends Signable, PRINCIPAL> {

    Optional<PRINCIPAL> getPrincipalFromCredentials(Credentials credentials);

    Optional<PRINCIPAL> getPrincipalByUuid(UUID userId);

    PRINCIPAL createPrincipalFromCredentials(Credentials credentials);

    SIGNABLE createSignableFromPrincipal(PRINCIPAL principal);
}
