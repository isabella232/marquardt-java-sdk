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
import org.echocat.marquardt.common.util.DateProvider;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class ExpiryDateCalculatorImpl<USER extends User<? extends Role>> implements ExpiryDateCalculator<USER> {

    private DateProvider _dateProvider = new DateProvider();

    public void setDateProvider(final DateProvider dateProvider) {
        _dateProvider = dateProvider;
    }

    @Override
    public Date calculateFor(final USER user) {
        return new Date(_dateProvider.now().getTime() + TimeUnit.DAYS.toMillis(60));
    }

    @Override
    public boolean isExpired(final Date date) {
        return _dateProvider.now().after(date);
    }
}