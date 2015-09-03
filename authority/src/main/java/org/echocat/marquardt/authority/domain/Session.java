/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.authority.domain;

import java.util.Date;
import java.util.UUID;

public interface Session {

    UUID getUserId();

    void setUserId(final UUID userId);

    byte[] getCertificate();

    void setCertificate(final byte[] certificate);

    Date getExpiresAt();

    void setExpiresAt(final Date expiresAt);

    byte[] getPublicKey();

    void setPublicKey(final byte[] publicKey);

    String getMechanism();

    void setMechanism(final String mechanism);

    boolean getValid();

    void setValid(final boolean valid);
}
