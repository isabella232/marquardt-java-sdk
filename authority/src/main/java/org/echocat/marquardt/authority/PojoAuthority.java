/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.authority;

import org.echocat.marquardt.common.domain.Principal;
import org.echocat.marquardt.authority.domain.Session;
import org.echocat.marquardt.common.exceptions.AlreadyLoggedInException;
import org.echocat.marquardt.authority.exceptions.CertificateCreationException;
import org.echocat.marquardt.authority.exceptions.InvalidSessionException;
import org.echocat.marquardt.common.exceptions.LoginFailedException;
import org.echocat.marquardt.authority.exceptions.NoSessionFoundException;
import org.echocat.marquardt.common.exceptions.UserExistsException;
import org.echocat.marquardt.authority.persistence.SessionStore;
import org.echocat.marquardt.common.ContentSigner;
import org.echocat.marquardt.common.domain.Certificate;
import org.echocat.marquardt.common.domain.Credentials;
import org.echocat.marquardt.common.domain.JsonWrappedCertificate;
import org.echocat.marquardt.common.domain.KeyPairProvider;
import org.echocat.marquardt.common.domain.PublicKeyWithMechanism;
import org.echocat.marquardt.common.domain.Signable;

import java.io.IOException;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PojoAuthority<SIGNABLE extends Signable, PRINCIPAL extends Principal> implements Authority {

    private final SignablePrincipalMapper<SIGNABLE, PRINCIPAL> _signablePrincipalMapper;
    private final SessionStore _sessionStore;
    private final ContentSigner _contentSigner = new ContentSigner();
    private final KeyPairProvider _issuerKeyProvider;

    public PojoAuthority(SignablePrincipalMapper<SIGNABLE, PRINCIPAL> signablePrincipalMapper, SessionStore sessionStore, KeyPairProvider issuerKeyProvider) {
        _signablePrincipalMapper = signablePrincipalMapper;
        _sessionStore = sessionStore;
        _issuerKeyProvider = issuerKeyProvider;
    }

    @Override
    public JsonWrappedCertificate signUp(Credentials credentials) {
        if (_signablePrincipalMapper.getPrincipalFromCredentials(credentials).isPresent()) {
            PRINCIPAL principal = _signablePrincipalMapper.createPrincipalFromCredentials(credentials);
            return createCertificateAndSession(credentials, principal);
        } else {
            throw new UserExistsException();
        }
    }

    @Override
    public JsonWrappedCertificate signIn(Credentials credentials) {
        final PRINCIPAL principal = _signablePrincipalMapper.getPrincipalFromCredentials(credentials).orElseThrow(() -> new LoginFailedException("Login failed"));
        if (principal.passwordMatches(credentials.getPassword())) {
            // create new session
            final PublicKeyWithMechanism publicKeyWithMechanism = new PublicKeyWithMechanism(credentials.getPublicKey());
            if (_sessionStore.activeAndValidSessionExists(principal.getUserId(), publicKeyWithMechanism.getValue(), new Date(), true)) {
                throw new AlreadyLoggedInException();
            } else {
                return createCertificateAndSession(credentials, principal);
            }
        }
        throw new LoginFailedException("Login failed");
    }

    @Override
    public JsonWrappedCertificate refresh(byte[] certificate) {
        final Session session = getSessionBasedOnValidCertificate(Base64.getDecoder().decode(certificate));
        final PRINCIPAL principal = _signablePrincipalMapper.getPrincipalByUuid(session.getUserId()).orElseThrow(() -> new IllegalStateException("Could not find principal with userId " + session.getUserId()));
        try {
            final byte[] newCertificate = createCertificate(principal, clientPublicKeyFrom(session));
            session.setCertificate(newCertificate);
            session.setExpiresAt(nowPlus60Days());
            _sessionStore.save(session);
            return createCertificateResponse(newCertificate);
        } catch (IOException e) {
            throw new CertificateCreationException("failed to refresh certificate for certificate " + principal.getUserId(), e);
        }
    }

    @Override
    public void signOut(byte[] certificate) {
        final Session session = getSessionBasedOnValidCertificate(Base64.getDecoder().decode(certificate));
        session.setValid(false);
        _sessionStore.save(session);

    }

    private JsonWrappedCertificate createCertificateAndSession(final Credentials credentials, final PRINCIPAL principal) {
        final byte[] certificate;
        try {
            certificate = createCertificate(principal, credentials.getPublicKey());
            createSession(credentials.getPublicKey(), principal.getUserId(), certificate);
            return createCertificateResponse(certificate);
        } catch (IOException e) {
            throw new CertificateCreationException("failed to create certificate for principal with id " + principal.getUserId(), e);
        }
    }

    private JsonWrappedCertificate createCertificateResponse(final byte[] certificate) {
        return new JsonWrappedCertificate(certificate);
    }

    private byte[] createCertificate(final PRINCIPAL principal, final PublicKey clientPublicKey) throws IOException {
        SIGNABLE signable = _signablePrincipalMapper.createSignableFromPrincipal(principal);
        final Certificate<SIGNABLE> certificate = Certificate.create(_issuerKeyProvider.getPublicKey(), clientPublicKey, principal.getRoles(), signable);
        return _contentSigner.sign(certificate, _issuerKeyProvider.getPrivateKey());
    }

    private PublicKey clientPublicKeyFrom(final Session session) {
        return new PublicKeyWithMechanism(session.getMechanism(), session.getPublicKey()).toJavaKey();
    }

    private Session getSessionBasedOnValidCertificate(final byte[] certificateBytes) {
        final Session session = _sessionStore.findByCertificate(certificateBytes).orElseThrow(NoSessionFoundException::new);
        if (!session.isValid() || session.getExpiresAt().before(new Date())) {
            throw new InvalidSessionException();
        }
        return session;
    }

    private void createSession(final PublicKey publicKey, final UUID userId, final byte[] certificate) {
        final PublicKeyWithMechanism publicKeyWithMechanism = new PublicKeyWithMechanism(publicKey);
        final Session session = new Session();
        session.setUserId(userId);
        session.setExpiresAt(nowPlus60Days());
        session.setPublicKey(publicKeyWithMechanism.getValue());
        session.setMechanism(publicKeyWithMechanism.getMechanism().getName());
        session.setCertificate(certificate);
        session.setValid(true);
        _sessionStore.save(session);
    }

    private Date nowPlus60Days() {
        return new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(60));
    }
}
