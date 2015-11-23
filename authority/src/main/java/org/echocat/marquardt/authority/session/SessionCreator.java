/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.authority.session;

import org.echocat.marquardt.authority.domain.Session;
import org.echocat.marquardt.authority.domain.User;
import org.echocat.marquardt.authority.exceptions.CertificateCreationException;
import org.echocat.marquardt.authority.persistence.SessionStore;
import org.echocat.marquardt.authority.persistence.UserCatalog;
import org.echocat.marquardt.authority.policies.SessionCreationPolicy;
import org.echocat.marquardt.common.domain.Credentials;
import org.echocat.marquardt.common.domain.PublicKeyWithMechanism;
import org.echocat.marquardt.common.domain.certificate.Role;
import org.echocat.marquardt.common.exceptions.AlreadyLoggedInException;
import org.echocat.marquardt.common.keyprovisioning.KeyPairProvider;

import java.io.IOException;
import java.security.PublicKey;
import java.util.Date;

public class SessionCreator<USER extends User<? extends Role>, SESSION extends Session> extends SessionAction<USER, SESSION> {

    /**
     * By default all sessions creation requests are accepted. For example if you want to allow only one session per user you
     * may configure a custom policy.
     */
    private SessionCreationPolicy _sessionCreationPolicy = (userId, clientPublicKey) -> true;

    public SessionCreator(final SessionStore<SESSION> sessionStore,
                          final UserCatalog<USER> userCatalog,
                          final ExpiryDateCalculator<USER> expiryDateCalculator,
                          final KeyPairProvider issuerKeyProvider) {
        super(sessionStore, userCatalog, expiryDateCalculator, issuerKeyProvider);
    }

    public void setSessionCreationPolicy(final SessionCreationPolicy sessionCreationPolicy) {
        _sessionCreationPolicy = sessionCreationPolicy;
    }

    public byte[] createCertificateAndSession(final Credentials credentials, final USER user) {
        final PublicKeyWithMechanism publicKeyWithMechanism = new PublicKeyWithMechanism(credentials.getPublicKey());
        if (!_sessionCreationPolicy.mayCreateSession(user.getUserId(), publicKeyWithMechanism.getValue())) {
            throw new AlreadyLoggedInException("User with id " + user.getUserId() + " is already logged in for current client.");
        }
        try {
            final byte[] certificate = createCertificate(user, credentials.getPublicKey());
            createAndStoreSession(credentials.getPublicKey(), credentials.getClientId(), user, certificate);
            return certificate;
        } catch (final IOException e) {
            throw new CertificateCreationException("failed to create certificate for user with id " + user.getUserId(), e);
        }
    }

    private void createAndStoreSession(final PublicKey publicKey, final String clientId, final USER user, final byte[] certificate) {
        final Date expiresAt = getExpiryDateCalculator().calculateFor(user);
        final PublicKeyWithMechanism publicKeyWithMechanism = new PublicKeyWithMechanism(publicKey);
        final SESSION session = getSessionStore().createTransient();
        session.setUserId(user.getUserId());
        session.setExpiresAt(expiresAt);
        session.setPublicKey(publicKeyWithMechanism.getValue());
        session.setMechanism(publicKeyWithMechanism.getMechanism().getName());
        session.setClientId(clientId);
        session.setCertificate(certificate);
        getSessionStore().save(session);
    }
}