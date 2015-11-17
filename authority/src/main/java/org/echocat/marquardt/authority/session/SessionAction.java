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
import org.echocat.marquardt.authority.persistence.SessionStore;
import org.echocat.marquardt.authority.persistence.UserCatalog;
import org.echocat.marquardt.common.Signer;
import org.echocat.marquardt.common.domain.Signable;
import org.echocat.marquardt.common.domain.certificate.Certificate;
import org.echocat.marquardt.common.domain.certificate.Role;
import org.echocat.marquardt.common.keyprovisioning.KeyPairProvider;

import java.io.IOException;
import java.security.PublicKey;

abstract class SessionAction<USER extends User<? extends Role>, SESSION extends Session> {

    private final SessionStore<SESSION> _sessionStore;
    private final UserCatalog<USER> _userCatalog;
    private final ExpiryDateCalculator<USER> _expiryDateCalculator;
    private final KeyPairProvider _issuerKeyProvider;
    private final Signer _signer = new Signer();

    public SessionAction(final SessionStore<SESSION> sessionStore,
                            final UserCatalog<USER> userCatalog,
                            final ExpiryDateCalculator<USER> expiryDateCalculator,
                            final KeyPairProvider issuerKeyProvider) {
        _sessionStore = sessionStore;
        _userCatalog = userCatalog;
        _expiryDateCalculator = expiryDateCalculator;
        _issuerKeyProvider = issuerKeyProvider;
    }

    protected SessionStore<SESSION> getSessionStore() {
        return _sessionStore;
    }

    protected UserCatalog<USER> getUserCatalog() {
        return _userCatalog;
    }

    protected ExpiryDateCalculator<USER> getExpiryDateCalculator() {
        return _expiryDateCalculator;
    }

    protected byte[] createCertificate(final USER user, final PublicKey clientPublicKey, final String clientId) throws IOException {
        final Signable signable = getUserCatalog().toSignable(user);
        final Certificate<Signable> certificate = Certificate.create(_issuerKeyProvider.getPublicKey(), clientPublicKey, clientId, user.getRoles(), signable);
        return _signer.sign(certificate, _issuerKeyProvider.getPrivateKey());
    }
}