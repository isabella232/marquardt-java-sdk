/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.authority;

import org.echocat.marquardt.common.domain.Credentials;
import org.echocat.marquardt.common.domain.JsonWrappedCertificate;

public interface Authority {

    JsonWrappedCertificate signUp(Credentials credentials);

    JsonWrappedCertificate signIn(Credentials credentials);

    JsonWrappedCertificate refresh(byte[] certificate);

    void signOut(byte[] certificate);
}
