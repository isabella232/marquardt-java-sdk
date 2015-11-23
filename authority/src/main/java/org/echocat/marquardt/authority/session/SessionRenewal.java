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
import org.echocat.marquardt.authority.exceptions.ExpiredSessionException;
import org.echocat.marquardt.authority.persistence.SessionStore;
import org.echocat.marquardt.authority.persistence.UserCatalog;
import org.echocat.marquardt.common.domain.PublicKeyWithMechanism;
import org.echocat.marquardt.common.domain.certificate.Role;
import org.echocat.marquardt.common.exceptions.NoSessionFoundException;
import org.echocat.marquardt.common.keyprovisioning.KeyPairProvider;

import java.io.IOException;
import java.security.PublicKey;
import java.util.function.Consumer;

public class SessionRenewal<USER extends User<? extends Role>, SESSION extends Session> extends SessionAction<USER, SESSION> {

    private Consumer<USER> _checkUserToFulfillAllRequirementsToSignInOrRefreshConsumer = user -> {  /* No-op by default */ };

    public SessionRenewal(final SessionStore<SESSION> sessionStore,
                          final UserCatalog<USER> userCatalog,
                          final ExpiryDateCalculator<USER> expiryDateCalculator,
                          final KeyPairProvider issuerKeyProvider) {
        super(sessionStore, userCatalog, expiryDateCalculator, issuerKeyProvider);
    }

    public void setCheckUserToFulfillAllRequirementsToSignInOrRefreshConsumer(final Consumer<USER> checkUserToFulfillAllRequirementsToSignInOrRefreshConsumer) {
        _checkUserToFulfillAllRequirementsToSignInOrRefreshConsumer = checkUserToFulfillAllRequirementsToSignInOrRefreshConsumer;
    }

    public byte[] renewSessionBasedOnCertificate(final byte[] certificate, final Consumer<SESSION> sessionValidator) {
        final SESSION session = getSessionStore().findByCertificate(certificate).orElseThrow(() -> new NoSessionFoundException("No session found."));
        if (getExpiryDateCalculator().isExpired(session.getExpiresAt())) {
            throw new ExpiredSessionException();
        }
        sessionValidator.accept(session);
        final USER user = getUserCatalog().findByUuid(session.getUserId()).orElseThrow(() -> new IllegalStateException("Could not find user with userId " + session.getUserId()));
        _checkUserToFulfillAllRequirementsToSignInOrRefreshConsumer.accept(user);
        try {
            final byte[] newCertificate = createCertificate(user, clientPublicKeyFrom(session));
            session.setCertificate(newCertificate);
            session.setExpiresAt(getExpiryDateCalculator().calculateFor(user));
            getSessionStore().save(session);
            return newCertificate;
        } catch (final IOException e) {
            throw new CertificateCreationException("failed to refresh certificate for certificate " + user.getUserId(), e);
        }
    }

    private PublicKey clientPublicKeyFrom(final Session session) {
        return new PublicKeyWithMechanism(session.getMechanism(), session.getPublicKey()).toJavaKey();
    }
}