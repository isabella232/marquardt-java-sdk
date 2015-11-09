/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.authority.session;

import org.echocat.marquardt.authority.domain.User;
import org.echocat.marquardt.common.domain.certificate.Role;

import java.util.Date;

public interface ExpiryDateCalculator<USER extends User<? extends Role>> {

    public Date calculateFor(final USER user);

    public boolean isExpired(final Date date);
}