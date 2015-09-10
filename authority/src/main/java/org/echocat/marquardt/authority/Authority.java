/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.authority;

import org.echocat.marquardt.authority.domain.User;
import org.echocat.marquardt.authority.domain.Session;
import org.echocat.marquardt.authority.exceptions.CertificateCreationException;
import org.echocat.marquardt.authority.exceptions.InvalidSessionException;
import org.echocat.marquardt.common.exceptions.NoSessionFoundException;
import org.echocat.marquardt.authority.persistence.UserStore;
import org.echocat.marquardt.authority.persistence.SessionStore;
import org.echocat.marquardt.common.Signer;
import org.echocat.marquardt.common.domain.Certificate;
import org.echocat.marquardt.common.domain.Credentials;
import org.echocat.marquardt.common.domain.JsonWrappedCertificate;
import org.echocat.marquardt.common.domain.KeyPairProvider;
import org.echocat.marquardt.common.domain.PublicKeyWithMechanism;
import org.echocat.marquardt.common.domain.Signable;
import org.echocat.marquardt.common.exceptions.AlreadyLoggedInException;
import org.echocat.marquardt.common.exceptions.LoginFailedException;
import org.echocat.marquardt.common.exceptions.UserExistsException;
import org.echocat.marquardt.common.util.DateProvider;

import java.io.IOException;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Authority<SIGNABLE extends Signable, USER extends User, SESSION extends Session> {

    private final UserStore<SIGNABLE, USER> _userStore;
    private final SessionStore<SESSION> _sessionStore;
    private final Signer _signer = new Signer();
    private final KeyPairProvider _issuerKeyProvider;


    private DateProvider _dateProvider = new DateProvider();

    public Authority(final UserStore<SIGNABLE, USER> userStore, final SessionStore<SESSION> sessionStore, final KeyPairProvider issuerKeyProvider) {
        _userStore = userStore;
        _sessionStore = sessionStore;
        _issuerKeyProvider = issuerKeyProvider;
    }

    public JsonWrappedCertificate signUp(final Credentials credentials) {
        if (!_userStore.findUserByCredentials(credentials).isPresent()) {
            final USER user = _userStore.createUserFromCredentials(credentials);
            return createCertificateAndSession(credentials, user);
        } else {
            throw new UserExistsException();
        }
    }

    public JsonWrappedCertificate signIn(final Credentials credentials) {
        final USER user = _userStore.findUserByCredentials(credentials).orElseThrow(() -> new LoginFailedException("Login failed"));
        if (user.passwordMatches(credentials.getPassword())) {
            // create new session
            final PublicKeyWithMechanism publicKeyWithMechanism = new PublicKeyWithMechanism(credentials.getPublicKey());
            if (_sessionStore.activeSessionExists(user.getUserId(), publicKeyWithMechanism.getValue(), _dateProvider.now())) {
                throw new AlreadyLoggedInException();
            } else {
                return createCertificateAndSession(credentials, user);
            }
        }
        throw new LoginFailedException("Login failed");
    }

    public JsonWrappedCertificate refresh(final byte[] certificate) {
        final SESSION session = getSessionBasedOnValidCertificate(Base64.getDecoder().decode(certificate));
        final USER user = _userStore.findUserByUuid(session.getUserId()).orElseThrow(() -> new IllegalStateException("Could not find user with userId " + session.getUserId()));
        try {
            final byte[] newCertificate = createCertificate(user, clientPublicKeyFrom(session));
            session.setCertificate(newCertificate);
            session.setExpiresAt(nowPlus60Days());
            _sessionStore.save(session);
            return createCertificateResponse(newCertificate);
        } catch (final IOException e) {
            throw new CertificateCreationException("failed to refresh certificate for certificate " + user.getUserId(), e);
        }
    }

    public void signOut(final byte[] certificate) {
        final SESSION session = getSessionBasedOnValidCertificate(Base64.getDecoder().decode(certificate));
        _sessionStore.delete(session);

    }

    public void setDateProvider(final DateProvider dateProvider) {
        _dateProvider = dateProvider;
    }

    private JsonWrappedCertificate createCertificateAndSession(final Credentials credentials, final USER user) {
        final byte[] certificate;
        try {
            certificate = createCertificate(user, credentials.getPublicKey());
            createSession(credentials.getPublicKey(), user.getUserId(), certificate);
            return createCertificateResponse(certificate);
        } catch (final IOException e) {
            throw new CertificateCreationException("failed to create certificate for user with id " + user.getUserId(), e);
        }
    }

    private JsonWrappedCertificate createCertificateResponse(final byte[] certificate) {
        return new JsonWrappedCertificate(certificate);
    }

    private byte[] createCertificate(final USER user, final PublicKey clientPublicKey) throws IOException {
        final SIGNABLE signable = _userStore.createSignableFromUser(user);
        final Certificate<SIGNABLE> certificate = Certificate.create(_issuerKeyProvider.getPublicKey(), clientPublicKey, user.getRoles(), signable);
        return _signer.sign(certificate, _issuerKeyProvider.getPrivateKey());
    }

    private PublicKey clientPublicKeyFrom(final Session session) {
        return new PublicKeyWithMechanism(session.getMechanism(), session.getPublicKey()).toJavaKey();
    }

    private SESSION getSessionBasedOnValidCertificate(final byte[] certificateBytes) {
        final SESSION session = _sessionStore.findByCertificate(certificateBytes).orElseThrow(() -> new NoSessionFoundException("No session found."));
        if (session.getExpiresAt().before(_dateProvider.now())) {
            throw new InvalidSessionException();
        }
        return session;
    }

    private void createSession(final PublicKey publicKey, final UUID userId, final byte[] certificate) {
        final PublicKeyWithMechanism publicKeyWithMechanism = new PublicKeyWithMechanism(publicKey);
        final SESSION session = _sessionStore.create();
        session.setUserId(userId);
        session.setExpiresAt(nowPlus60Days());
        session.setPublicKey(publicKeyWithMechanism.getValue());
        session.setMechanism(publicKeyWithMechanism.getMechanism().getName());
        session.setCertificate(certificate);
        _sessionStore.save(session);
    }

    private Date nowPlus60Days() {
        return new Date(_dateProvider.now().getTime() + TimeUnit.DAYS.toMillis(60));
    }
}
