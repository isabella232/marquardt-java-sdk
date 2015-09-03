/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.service.api;

import org.echocat.marquardt.common.ContentValidator;
import org.echocat.marquardt.common.domain.Certificate;
import org.echocat.marquardt.common.domain.DeserializingFactory;
import org.echocat.marquardt.common.domain.Signable;
import org.echocat.marquardt.common.exceptions.InvalidCertificateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.PublicKey;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public abstract class CertificateValidator<USERINFO extends Signable> extends ContentValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(CertificateValidator.class);

    private final List<PublicKey> _trustedPublicKeys;
    private final DateProvider _dateProvider;

    public CertificateValidator(final DateProvider dateProvider, final List<PublicKey> trustedPublicKeys) {
        _trustedPublicKeys = trustedPublicKeys;
        _dateProvider = dateProvider;
    }

    protected abstract DeserializingFactory<USERINFO> getDeserializingFactory();

    public Certificate<USERINFO> from(final byte[] encodedCertificate) {
        final Optional<Certificate<USERINFO>> certificate = deserializeAndValidate(encodedCertificate);
        return certificate.orElseThrow(InvalidCertificateException::new);
    }

    private Optional<Certificate<USERINFO>> deserializeAndValidate(final byte[] encodedCertificate) {
        final Certificate<USERINFO> certificate = deserializeCertificateAndValidateSignature(encodedCertificate, getDeserializingFactory(), Certificate::getIssuerPublicKey);
        if (certificate == null || isExpired(certificate)) {
            return Optional.empty();
        }
        final PublicKey issuerPublicKey = certificate.getIssuerPublicKey();
        if (!_trustedPublicKeys.contains(issuerPublicKey)) {
            LOGGER.warn("Attack!! ALERT!!! Duck and cover!!! Certificate '{}' could not be found as trusted certificate.", issuerPublicKey);
            throw new InvalidCertificateException();
        }
        return Optional.of(certificate);
    }

    private boolean isExpired(final Certificate<USERINFO> certificate) {
        final Date now = _dateProvider.now();
        return now.after(certificate.getExpiresAt());
    }
}