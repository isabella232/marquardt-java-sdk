/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.client;

import org.echocat.marquardt.common.domain.Certificate;
import org.echocat.marquardt.common.domain.Credentials;
import org.echocat.marquardt.common.domain.Signable;

import java.io.IOException;

public interface Client<T extends Signable> {

    Certificate<T> signup(final Credentials credentials) throws IOException;

    Certificate<T> signin(final Credentials credentials) throws IOException;

    Certificate<T> refresh() throws IOException;

    boolean signout() throws IOException;

    <REQUEST, RESPONSE> RESPONSE sendSignedPayloadTo(final String url,
                                                     final String httpMethod,
                                                     final REQUEST payload,
                                                     final Class<RESPONSE> responseType);
}
