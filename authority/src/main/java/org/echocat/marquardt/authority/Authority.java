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
import org.echocat.marquardt.authority.persistence.UserCatalog;
import org.echocat.marquardt.authority.persistence.UserCreator;
import org.echocat.marquardt.authority.policies.ClientAccessPolicy;
import org.echocat.marquardt.authority.session.SessionCreator;
import org.echocat.marquardt.authority.session.SessionRenewal;
import org.echocat.marquardt.common.domain.ClientInformation;
import org.echocat.marquardt.common.domain.Credentials;
import org.echocat.marquardt.common.domain.PublicKeyWithMechanism;
import org.echocat.marquardt.common.domain.SignUpAccountData;
import org.echocat.marquardt.common.domain.Signature;
import org.echocat.marquardt.common.domain.certificate.Certificate;
import org.echocat.marquardt.common.domain.certificate.Role;
import org.echocat.marquardt.common.exceptions.AlreadyLoggedInException;
import org.echocat.marquardt.common.exceptions.ClientNotAuthorizedException;
import org.echocat.marquardt.common.exceptions.LoginFailedException;
import org.echocat.marquardt.common.exceptions.NoSessionFoundException;
import org.echocat.marquardt.common.exceptions.SignatureValidationFailedException;
import org.echocat.marquardt.common.exceptions.UserAlreadyExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.function.Consumer;

import static org.apache.commons.codec.binary.Base64.decodeBase64;
import static org.echocat.marquardt.authority.domain.UserStatus.WITHOUT_CREDENTIALS;

/**
 * Authority implementation. Wrap this with your favorite server implementation.
 *
 * @param <USER> Your authority's user implementation.
 * @param <SESSION> Your authority's session implementation.
 * @see User
 * @see Session
 * @see Certificate
 * @see org.echocat.marquardt.authority.spring.SpringAuthorityController
 */
public class Authority<USER extends User<? extends Role>,
                       SESSION extends Session,
                       CREDENTIALS extends Credentials,
                       SIGNUP_ACCOUNT_DATA extends SignUpAccountData<CREDENTIALS>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Authority.class);

    private final UserCatalog<USER> _userCatalog;
    private final UserCreator<USER, CREDENTIALS, SIGNUP_ACCOUNT_DATA> _userCreator;
    private final SessionCreator<USER, SESSION> _sessionCreator;
    private final SessionRenewal<USER, SESSION> _sessionRenewal;
    private final SessionStore<SESSION> _sessionStore;
    private final ClientAccessPolicy _clientAccessPolicy;
    private Consumer<USER> _checkRequirementsForUser = user -> {  /* No-op by default */ };

    /**
     * Sets up a new Authority singleton.
     * @param userCatalog Your user store.
     * @param userCreator to create new user and assign all data from given account data to user instance.
     * @param sessionStore Your session store.
     */
    public Authority(final UserCatalog<USER> userCatalog,
                     final UserCreator<USER, CREDENTIALS, SIGNUP_ACCOUNT_DATA> userCreator,
                     final SessionCreator<USER, SESSION> sessionCreator,
                     final SessionRenewal<USER, SESSION> sessionRenewal,
                     final SessionStore<SESSION> sessionStore,
                     final ClientAccessPolicy clientIdPolicy) {
        _userCatalog = userCatalog;
        _userCreator = userCreator;
        _sessionCreator = sessionCreator;
        _sessionRenewal = sessionRenewal;
        _sessionStore = sessionStore;
        _clientAccessPolicy = clientIdPolicy;
    }

    /**
     * The consumer will be invoked before a finalize sign-up, sign-in or refresh is permitted. The Consumer may throw any {@link RuntimeException} in case
     * the given user does not fulfill the criteria to successfully complete e.g. a sign-in or refresh. For example when the user have a expiry date
     * and this date is expired the consumer may throw an exception to force the action to fail.
     */
    public void setCheckRequirementsForUser(final Consumer<USER> checkRequirementsForUser) {
        _checkRequirementsForUser = checkRequirementsForUser;
    }

    /**
     * Start of the 2 step sign-up process. In the first step an empty user consisting only of {@link User#getUserId()} and {@link User#getStatus()}
     * will be created and stored.
     * As result the certificate is returned.
     *
     * For example it would be possible to set a expiry date due limit the time frame between initializing and finalizing the sign-up.
     *
     * @param clientInformation consisting of client public key and id.
     * @return certificate for the client.
     * @throws ClientNotAuthorizedException when client id is unknown or null.
     */
    public byte[] initializeSignUp(final ClientInformation clientInformation) {
        throwExceptionWhenClientIdIsProhibited(clientInformation.getClientId());
        final USER user = _userCreator.createEmptyUser();
        return _sessionCreator.createCertificateAndSession(clientInformation, user);
    }

    /**
     * Finalize 2 step sign-up process. Requires an existing session and user. The submitted {@link SignUpAccountData} are used to enrich
     * the existing user.
     * As result an updated certificate is returned.
     *
     * @return certificate for the client.
     * @throws ClientNotAuthorizedException when client id is unknown or null.
     * @throws LoginFailedException If user does not exist or password does not match.
     * @throws AlreadyLoggedInException If the user is not allowed to obtain (another) session.
     * @throws CertificateCreationException If there were problems creating the certificate.
     */
    public byte[] finalizeSignUp(final byte[] certificate, final byte[] signedBytes, final Signature signature, final SIGNUP_ACCOUNT_DATA accountData) {
        throwExceptionWhenClientIdIsProhibited(accountData.getCredentials().getClientId());
        final byte[] decodedCertificate = decodeBase64(certificate);
        final SESSION session = _sessionStore.findByCertificate(decodedCertificate).orElseThrow(NoSessionFoundException::new);
        verifySignature(signedBytes, signature, session);
        final UUID userId = session.getUserId();
        final USER user = _userCatalog.findByUuid(userId).orElseThrow(() -> new IllegalArgumentException("No such user '" + userId + "'."));
        if (user.getStatus() != WITHOUT_CREDENTIALS) {
            throw new IllegalStateException(user + " with status '" + user.getStatus() + "' does not match expected status '" + WITHOUT_CREDENTIALS + "' and will be rejected to proceed with enrichment");
        }
        _checkRequirementsForUser.accept(user);
        final Credentials credentials = accountData.getCredentials();
        if (_userCatalog.findByCredentials(credentials).isPresent()) {
            throw new UserAlreadyExistsException("User with identifier " + credentials.getIdentifier() + " already exists.");
        }

        _userCreator.enrichAndUpdateFrom(user, accountData);
        return refresh(certificate, signedBytes, signature); // ... use refresh to ensure that user with it's latest roles is used to compose certificate
    }

    /**
     * Implements sign-in. Creates a new Session for a known user.
     *
     * @param credentials of the user with the client's public key.
     * @return certificate for the client.
     * @throws LoginFailedException If user does not exist or password does not match.
     * @throws AlreadyLoggedInException If the user is not allowed to obtain (another) session.
     * @throws CertificateCreationException If there were problems creating the certificate.
     */
    public byte[] signIn(final CREDENTIALS credentials) {
        throwExceptionWhenClientIdIsProhibited(credentials.getClientId());
        final USER user = _userCatalog.findByCredentials(credentials).orElseThrow(() -> new LoginFailedException("Login failed"));
        if (!user.passwordMatches(credentials.getPassword())) {
            throw new LoginFailedException("Login failed");
        }
        _checkRequirementsForUser.accept(user);
        return _sessionCreator.createCertificateAndSession(credentials, user);
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
        return _sessionRenewal.renewSessionBasedOnCertificate(
                decodeBase64(certificate),
                session -> {
                    verifySignature(signedBytes, signature, session);
                    throwExceptionWhenClientIdIsProhibited(session.getClientId());
                }
        );
    }

    /**
     * @param certificate Certificate to sign out with (may be an expired one - but must be the last one created for this session).
     * @param signedBytes byte sequence signed by the client.
     * @param signature signature of the byte sequence.
     */
    public void signOut(final byte[] certificate, final byte[] signedBytes, final Signature signature) {
        try {
            final SESSION session = getSessionBasedOnCertificate(decodeBase64(certificate));
            verifySignature(signedBytes, signature, session);
            _sessionStore.delete(session);
        } catch (final NoSessionFoundException ignored) {
            LOGGER.info("Received sign out, but session was not found for provided certificate.");
        }
    }

    private void throwExceptionWhenClientIdIsProhibited(final String clientId) {
        if (!_clientAccessPolicy.isAllowed(clientId)) {
            throw new ClientNotAuthorizedException("Client not authorized");
        }
    }

    private void verifySignature(final byte[] signedBytes, final Signature signature, final SESSION session) {
        final PublicKeyWithMechanism publicKeyWithMechanism = new PublicKeyWithMechanism(session.getMechanism(), session.getPublicKey());
        if (!signature.isValidFor(signedBytes, publicKeyWithMechanism.toJavaKey())) {
            throw new SignatureValidationFailedException("failed to verify signature with client's public key");
        }
    }

    private SESSION getSessionBasedOnCertificate(final byte[] certificateBytes) {
        return _sessionStore.findByCertificate(certificateBytes).orElseThrow(NoSessionFoundException::new);
    }
}