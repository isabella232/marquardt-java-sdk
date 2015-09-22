/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.authority.testdomain;

import org.echocat.marquardt.authority.domain.Session;

import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

public class TestSession implements Session {

    private UUID _userId;
    @SuppressWarnings("UseOfObsoleteDateTimeApi")
    private Date _expiredAt;
    private byte[] _publicKey;

    @Override
    public UUID getUserId() {
        return _userId;
    }

    @Override
    public void setUserId(final UUID userId) {
        _userId = userId;
    }

    @Override
    public byte[] getCertificate() {
        return new byte[0];
    }

    @Override
    public void setCertificate(final byte[] certificate) {

    }

    @SuppressWarnings("UseOfObsoleteDateTimeApi")
    @Override
    public Date getExpiresAt() {
        return _expiredAt;
    }

    @Override
    public void setExpiresAt(@SuppressWarnings("UseOfObsoleteDateTimeApi") final Date expiresAt) {
        _expiredAt = expiresAt;
    }

    @Override
    public byte[] getPublicKey() {
        return _publicKey;
    }

    @Override
    public void setPublicKey(final byte[] publicKey) {
        _publicKey = Arrays.copyOf(publicKey, publicKey.length);
    }

    @Override
    public String getMechanism() {
        return "RSA";
    }

    @Override
    public void setMechanism(final String mechanism) {

    }
}
