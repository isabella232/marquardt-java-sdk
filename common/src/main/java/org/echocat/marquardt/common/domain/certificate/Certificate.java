
/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.common.domain.certificate;

import com.google.common.collect.Sets;
import com.google.common.primitives.Longs;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.echocat.marquardt.common.domain.PublicKeyWithMechanism;
import org.echocat.marquardt.common.domain.Signable;
import org.echocat.marquardt.common.serialization.RolesSerializer;

import javax.annotation.Nonnull;
import javax.annotation.WillNotClose;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Certificate produced by the authority when clients signup or signin.
 *
 * Clients use this certificate to get user authorization (roles) to enable features for a user.
 * Clients use this certificate to communicate with services (with header X-Certificate) to access protected resources.
 *
 * Services use this certificate for authentication of protected resources.
 * Services use this certificate for authorization of service methods (roles).
 *
 * @param <T> Class of wrapped payload, for example additional user information to use on clents and services.
 */
public class Certificate<T extends Signable> implements Signable {

    public static final Byte VERSION = 1;

    private final PublicKey _issuerPublicKey;
    private final PublicKey _clientPublicKey;
    @SuppressWarnings("UseOfObsoleteDateTimeApi")
    private final Date _expiresAt;
    private final Set<? extends Role> _roles;
    private final T _payload;
    private byte[] _signedCertificateBytes;

    /**
     * Factory method to create Certificate (used by the authority)
     *
     * @param issuerPublicKey Authority's public key. Must be trusted by clients and services.
     * @param clientPublicKey Client's public key. Enables login of the same user on different clients.
     * @param roles Roles of the user to enable authorization in clients and services.
     * @param payload Wrapped payload, for example additional user information to use on clients and services
     * @param <T> Class of wrapped payload, for example additional user information to use on clients and services.
     * @return A certificate.
     */
    public static <T extends Signable> Certificate<T> create(final PublicKey issuerPublicKey, final PublicKey clientPublicKey, final Set<? extends Role> roles, final T payload) {
        return new Certificate<>(issuerPublicKey, clientPublicKey, roles, payload);
    }

    private Certificate(final PublicKey issuerPublicKey, final PublicKey clientPublicKey, final Set<? extends Role> roles, final T payload) {
        _issuerPublicKey = issuerPublicKey;
        _clientPublicKey = clientPublicKey;
        _expiresAt = new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(15));
        _roles = roles;
        _payload = payload;
    }

    /**
     * Used by the CertificateFactory only. To serialize certificate from bytes.
     *
     * @param issuerPublicKey Authority's public key. Must be trusted by clients and services.
     * @param clientPublicKey Client's public key. Enabled login of the same user on different clients.
     * @param expiresAt Timestamp of expiration. Should be a relative short timeframe compared to the session of the authority.
     * @param roles Roles of the user to enable authorization in clients and services.
     * @param payload Wrapped payload, for example additional user information to use on clients and services
     */
    Certificate(final PublicKey issuerPublicKey, final PublicKey clientPublicKey, @SuppressWarnings("UseOfObsoleteDateTimeApi") final Date expiresAt, final Set<? extends Role> roles, final T payload) {
        _issuerPublicKey = issuerPublicKey;
        _clientPublicKey = clientPublicKey;
        _expiresAt = expiresAt;
        _roles = Sets.newHashSet(roles);
        _payload = payload;
    }

    /**
     * @return Authority's public key. Must be trusted by clients and services.
     */
    public PublicKey getIssuerPublicKey() {
        return _issuerPublicKey;
    }

    /**
     * @return Client's public key. Enabled login of the same user on different clients.
     */
    public PublicKey getClientPublicKey() {
        return _clientPublicKey;
    }

    /**
     * @return Wrapped payload, for example additional user information to use on clents and services.
     */
    public T getPayload() {
        return _payload;
    }

    /**
     * @return Timestamp of expiration. Should be a relative short timeframe compared to the session of the authority.
     */
    @SuppressWarnings("UseOfObsoleteDateTimeApi")
    public Date getExpiresAt() {
        return _expiresAt;
    }

    /**
     * @return Roles of the user to enable authorization in clients and services.
     */
    public Set<? extends Role> getRoles() {
        return _roles;
    }

    @Override
    public void writeTo(@Nonnull @WillNotClose final OutputStream out) throws IOException {
        if (isSignedCertificate()) {
            out.write(_signedCertificateBytes);
        } else {
            serializeTo(out);
        }
    }

    @Override
    public byte[] getContent() throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            writeTo(out);
            return out.toByteArray();
        } finally {
            IOUtils.closeQuietly(out);
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("_issuerPublicKey", _issuerPublicKey)
            .append("_clientPublicKey", _clientPublicKey)
            .append("_expiresAt", _expiresAt)
            .append("_roles", _roles)
            .append("_payload", _payload)
            .toString();
    }

    /**
     * If the certificate was received from an authority, we can use the already signed bytes when
     * sending the certificate another service.
     */
    public void setSignedCertificateBytes(final byte[] signedCertificateBytes) {
        _signedCertificateBytes = Arrays.copyOf(signedCertificateBytes, signedCertificateBytes.length);
    }

    private void serializeTo(@Nonnull @WillNotClose final OutputStream out) throws IOException {
        out.write(VERSION);
        new PublicKeyWithMechanism(_issuerPublicKey).writeTo(out);
        new PublicKeyWithMechanism(_clientPublicKey).writeTo(out);
        out.write(Longs.toByteArray(_expiresAt.getTime()));
        out.write(Longs.toByteArray(RolesSerializer.from(_roles)));
        _payload.writeTo(out);
    }

    private boolean isSignedCertificate() {
        return _signedCertificateBytes != null;
    }
}
