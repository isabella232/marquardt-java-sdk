/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.common.domain;

import java.security.PrivateKey;
import java.security.PublicKey;

public interface KeyPairProvider {

    PublicKey getPublicKey();

    PrivateKey getPrivateKey();

}
