/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.authority.testdomain;

import org.echocat.marquardt.authority.domain.Session;

import java.util.Date;
import java.util.UUID;

public class TestSession implements Session {
    @Override
    public UUID getUserId() {
        return null;
    }

    @Override
    public void setUserId(UUID userId) {

    }

    @Override
    public byte[] getCertificate() {
        return new byte[0];
    }

    @Override
    public void setCertificate(byte[] certificate) {

    }

    @Override
    public Date getExpiresAt() {
        return null;
    }

    @Override
    public void setExpiresAt(Date expiresAt) {

    }

    @Override
    public byte[] getPublicKey() {
        return new byte[0];
    }

    @Override
    public void setPublicKey(byte[] publicKey) {

    }

    @Override
    public String getMechanism() {
        return null;
    }

    @Override
    public void setMechanism(String mechanism) {

    }

    @Override
    public boolean getValid() {
        return false;
    }

    @Override
    public void setValid(boolean valid) {

    }
}
