/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.authority.persistence;

import org.echocat.marquardt.authority.domain.User;
import org.echocat.marquardt.common.domain.ClientInformation;
import org.echocat.marquardt.common.domain.Credentials;
import org.echocat.marquardt.common.domain.SignUpAccountData;
import org.echocat.marquardt.common.domain.certificate.Role;

public interface UserCreator<USER extends User<? extends Role>, CREDENTIALS extends Credentials, SIGN_UP_ACCOUNT_DATA extends SignUpAccountData<CREDENTIALS>> {

    USER createEmptyUser(final ClientInformation clientInformation);

    USER enrichAndUpdateFrom(final USER user, final SIGN_UP_ACCOUNT_DATA accountData);

}