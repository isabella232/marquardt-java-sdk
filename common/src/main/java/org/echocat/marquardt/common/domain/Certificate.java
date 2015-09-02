
/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.common.domain;

import com.google.common.primitives.Longs;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.annotation.Nonnull;
import javax.annotation.WillNotClose;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.PublicKey;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Certificate<T extends Signable> implements Signable {

    public static final Byte VERSION = 1;

    private final PublicKey _issuerPublicKey;
    private final PublicKey _clientPublicKey;
    private final Date _expiresAt;
    private final long _roleCodes;
    private final T _payload;

    public static <T extends Signable> Certificate<T> create(final PublicKey issuerPublicKey, final PublicKey clientPublicKey, long roleCodes, final T payload) {
        return new Certificate<T>(issuerPublicKey, clientPublicKey, roleCodes, payload);
    }

    private Certificate(final PublicKey issuerPublicKey, final PublicKey clientPublicKey, final long roleCodes, final T payload) {
        _issuerPublicKey = issuerPublicKey;
        _clientPublicKey = clientPublicKey;
        _roleCodes = roleCodes;
        _expiresAt = new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(15));
        _payload = payload;
    }

    Certificate(final PublicKey issuerPublicKey, final PublicKey clientPublicKey, final Date expiresAt, long roleCodes, final T payload) {
        _issuerPublicKey = issuerPublicKey;
        _clientPublicKey = clientPublicKey;
        _expiresAt = expiresAt;
        _roleCodes = roleCodes;
        _payload = payload;
    }

    public PublicKey getIssuerPublicKey() {
        return _issuerPublicKey;
    }

    public PublicKey getClientPublicKey() {
        return _clientPublicKey;
    }

    public T getPayload() {
        return _payload;
    }

    public Date getExpiresAt() {
        return _expiresAt;
    }

    public long getRoleCodes() {
        return _roleCodes;
    }

    @Override
    public void writeTo(@Nonnull @WillNotClose final OutputStream out) throws IOException {
        out.write(VERSION);
        new PublicKeyWithMechanism(_issuerPublicKey).writeTo(out);
        new PublicKeyWithMechanism(_clientPublicKey).writeTo(out);
        out.write(Longs.toByteArray(_expiresAt.getTime()));
        out.write(Longs.toByteArray(_roleCodes));
        _payload.writeTo(out);
    }

    @Override
    public byte[] getContent() throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            writeTo(out);
            return out.toByteArray();
        } finally {
            try {
                out.close();
            } catch (final IOException ignored) {}
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("_issuerPublicKey", _issuerPublicKey)
                .append("_clientPublicKey", _clientPublicKey)
                .append("_expiresAt", _expiresAt)
                .append("_roleCodes", _roleCodes)
                .append("_payload", _payload)
                .toString();
    }
}
