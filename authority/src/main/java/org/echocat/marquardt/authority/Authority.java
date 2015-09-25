/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.authority;

import org.echocat.marquardt.authority.domain.Session;
import org.echocat.marquardt.authority.domain.User;
import org.echocat.marquardt.authority.exceptions.CertificateCreationException;
import org.echocat.marquardt.authority.exceptions.ExpiredSessionException;
import org.echocat.marquardt.authority.persistence.SessionStore;
import org.echocat.marquardt.authority.persistence.UserStore;
import org.echocat.marquardt.common.Signer;
import org.echocat.marquardt.common.domain.certificate.Certificate;
import org.echocat.marquardt.common.domain.Credentials;
import org.echocat.marquardt.common.keyprovisioning.KeyPairProvider;
import org.echocat.marquardt.common.domain.PublicKeyWithMechanism;
import org.echocat.marquardt.common.domain.certificate.Role;
import org.echocat.marquardt.common.domain.Signable;
import org.echocat.marquardt.common.domain.Signature;
import org.echocat.marquardt.common.exceptions.AlreadyLoggedInException;
import org.echocat.marquardt.common.exceptions.LoginFailedException;
import org.echocat.marquardt.common.exceptions.NoSessionFoundException;
import org.echocat.marquardt.common.exceptions.SignatureValidationFailedException;
import org.echocat.marquardt.common.exceptions.UserExistsException;
import org.echocat.marquardt.common.util.DateProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.PublicKey;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.codec.binary.Base64.decodeBase64;

/**
 * Authority implementation. Wrap this with your favorite server implementation.
 *
 * @param <USER> Your authority's user implementation.
 * @param <SESSION> Your authority's session implementation.
 * @param <SIGNABLE> Your user information object to wrap into Certificate.
 *
 * @see User
 * @see Session
 * @see Signable
 * @see Certificate
 * @see org.echocat.marquardt.authority.spring.SpringAuthorityController
 */
public class Authority<USER extends User<? extends Role>, SESSION extends Session, SIGNABLE extends Signable> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Authority.class);

    private final UserStore<USER, SIGNABLE> _userStore;
    private final SessionStore<SESSION> _sessionStore;
    private final Signer _signer = new Signer();
    private final KeyPairProvider _issuerKeyProvider;

    private DateProvider _dateProvider = new DateProvider();

    /**
     * Sets up a new Authority singleton.
     *
     * @param userStore Your user store.
     * @param sessionStore Your session store.
     * @param issuerKeyProvider KeyPairProvider of the authority. Public key should be trusted by the clients and services.
     */
    public Authority(final UserStore<USER, SIGNABLE> userStore, final SessionStore<SESSION> sessionStore, final KeyPairProvider issuerKeyProvider) {
        _userStore = userStore;
        _sessionStore = sessionStore;
        _issuerKeyProvider = issuerKeyProvider;
    }

    /**
     * Implements signup. Creates a new User and a new Session.
     *
     * @param credentials Credentials of the user that should be signed up.
     * @return certificate for the client.
     * @throws UserExistsException If a user with the same identifier already exists.
     * @throws CertificateCreationException If there were problems creating the certificate.
     */
    public byte[] signUp(final Credentials credentials) {
        if (!_userStore.findByCredentials(credentials).isPresent()) {
            final USER user = _userStore.createFromCredentials(credentials);
            return createCertificateAndSession(credentials, user);
        } else {
            throw new UserExistsException("User with identifier " + credentials.getIdentifier() + " already exists.");
        }
    }

    /**
     * Implements signin. Creates a new Session for a known user.
     * @param credentials Credentials of the user with the client's public key.
     * @return certificate for the client.
     * @throws LoginFailedException If user does not exist or password does not match.
     * @throws AlreadyLoggedInException If the user already has an active session for this client's public key.
     * @throws CertificateCreationException If there were problems creating the certificate.
     */
    public byte[] signIn(final Credentials credentials) {
        final USER user = _userStore.findByCredentials(credentials).orElseThrow(() -> new LoginFailedException("Login failed"));
        if (user.passwordMatches(credentials.getPassword())) {
            // create new session
            final PublicKeyWithMechanism publicKeyWithMechanism = new PublicKeyWithMechanism(credentials.getPublicKey());
            if (_sessionStore.existsActiveSession(user.getUserId(), publicKeyWithMechanism.getValue(), _dateProvider.now())) {
                throw new AlreadyLoggedInException("User with id " + user.getUserId() + " is already logged in for current client.");
            } else {
                return createCertificateAndSession(credentials, user);
            }
        }
        throw new LoginFailedException("Login failed");
    }

    /**
     * Implements refresh. Updates the Session of the User with a fresh certificate.
     *
     * @param certificate Certificate to replace (may be an expired one - but must be the last one created for this session).
     * @param signedBytes byte sequence signed by the client.
     * @param signature signature of the byte sequence.
     * @return certificate for the client.
     * @throws NoSessionFoundException If no session exists for this certificate. You must sign in again to handle this.
     * @throws ExpiredSessionException When the session is already expired. You must sign in again to handle this.
     * @throws IllegalStateException When the user id of the session is not found. Caused by a data inconsistency or a wrong UserStore implementation.
     * @throws CertificateCreationException If there were problems creating the certificate.
     */
    public byte[] refresh(final byte[] certificate, final byte[] signedBytes, final Signature signature) {
        final SESSION session = getValidSessionBasedOnCertificate(decodeBase64(certificate));
        verifySignature(signedBytes, signature, session);
        final USER user = _userStore.findByUuid(session.getUserId()).orElseThrow(() -> new IllegalStateException("Could not find user with userId " + session.getUserId()));
        try {
            final byte[] newCertificate = createCertificate(user, clientPublicKeyFrom(session));
            session.setCertificate(newCertificate);
            session.setExpiresAt(nowPlus60Days());
            _sessionStore.save(session);
            return newCertificate;
        } catch (final IOException e) {
            throw new CertificateCreationException("failed to refresh certificate for certificate " + user.getUserId(), e);
        }
    }

    /**
     *
     * @param certificate Certificate to sign out with (may be an expired one - but must be the last one created for this session).
     * @param signedBytes byte sequence signed by the client.
     * @param signature signature of the byte sequence.
     */
    public void signOut(final byte[] certificate, final byte[] signedBytes, final Signature signature) {
        final SESSION session;
        try {
            session = getSessionBasedOnCertificate(decodeBase64(certificate));
            verifySignature(signedBytes, signature, session);
            _sessionStore.delete(session);
        } catch (final NoSessionFoundException ignored) {
            LOGGER.info("Received sign out, but session was not found for provided certificate.");
        }
    }

    @SuppressWarnings("unused")
    public void setDateProvider(final DateProvider dateProvider) {
        _dateProvider = dateProvider;
    }

    private void verifySignature(final byte[] signedBytes, final Signature signature, final SESSION session) {
        final PublicKeyWithMechanism publicKeyWithMechanism = new PublicKeyWithMechanism(session.getMechanism(), session.getPublicKey());
        if (!signature.isValidFor(signedBytes, publicKeyWithMechanism.toJavaKey())){
            throw new SignatureValidationFailedException("failed to verify signature with client's public key");
        }
    }

    private byte[] createCertificateAndSession(final Credentials credentials, final USER user) {
        final byte[] certificate;
        try {
            certificate = createCertificate(user, credentials.getPublicKey());
            createSession(credentials.getPublicKey(), user.getUserId(), certificate);
            return certificate;
        } catch (final IOException e) {
            throw new CertificateCreationException("failed to create certificate for user with id " + user.getUserId(), e);
        }
    }

    private byte[] createCertificate(final USER user, final PublicKey clientPublicKey) throws IOException {
        final SIGNABLE signable = _userStore.createSignableFromUser(user);
        final Certificate<SIGNABLE> certificate = Certificate.create(_issuerKeyProvider.getPublicKey(), clientPublicKey, user.getRoles(), signable);
        return _signer.sign(certificate, _issuerKeyProvider.getPrivateKey());
    }

    private PublicKey clientPublicKeyFrom(final Session session) {
        return new PublicKeyWithMechanism(session.getMechanism(), session.getPublicKey()).toJavaKey();
    }

    private SESSION getValidSessionBasedOnCertificate(final byte[] certificateBytes) {
        final SESSION session = getSessionBasedOnCertificate(certificateBytes);
        if (session.getExpiresAt().before(_dateProvider.now())) {
            throw new ExpiredSessionException();
        }
        return session;
    }

    private SESSION getSessionBasedOnCertificate(final byte[] certificateBytes) {
        return _sessionStore.findByCertificate(certificateBytes).orElseThrow(() -> new NoSessionFoundException("No session found."));
    }

    private void createSession(final PublicKey publicKey, final UUID userId, final byte[] certificate) {
        final PublicKeyWithMechanism publicKeyWithMechanism = new PublicKeyWithMechanism(publicKey);
        final SESSION session = _sessionStore.createTransient();
        session.setUserId(userId);
        session.setExpiresAt(nowPlus60Days());
        session.setPublicKey(publicKeyWithMechanism.getValue());
        session.setMechanism(publicKeyWithMechanism.getMechanism().getName());
        session.setCertificate(certificate);
        _sessionStore.save(session);
    }

    @SuppressWarnings("UseOfObsoleteDateTimeApi")
    private Date nowPlus60Days() {
        return new Date(_dateProvider.now().getTime() + TimeUnit.DAYS.toMillis(60));
    }
}
