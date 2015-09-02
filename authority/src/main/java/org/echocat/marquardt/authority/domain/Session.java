/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.authority.domain;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.UUID;

public class Session {

    private String id;

    @NotNull
    private UUID userId;

    @NotNull
    private byte[] certificate;

    @NotNull
    private Date expiresAt;

    @NotNull
    private byte[] publicKey;

    @NotNull
    private String mechanism;

    private Boolean valid;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(final UUID userId) {
        this.userId = userId;
    }

    public byte[] getCertificate() {
        return certificate;
    }

    public void setCertificate(final byte[] certificate) {
        this.certificate = certificate;
    }

    public Date getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(final Date expiresAt) {
        this.expiresAt = expiresAt;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(final byte[] publicKey) {
        this.publicKey = publicKey;
    }

    public String getMechanism() {
        return mechanism;
    }

    public void setMechanism(final String mechanism) {
        this.mechanism = mechanism;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(final boolean valid) {
        this.valid = valid;
    }
}
