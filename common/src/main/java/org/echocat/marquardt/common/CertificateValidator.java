/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.common;

import org.echocat.marquardt.common.domain.Certificate;
import org.echocat.marquardt.common.domain.CertificateFactory;
import org.echocat.marquardt.common.domain.DeserializingFactory;
import org.echocat.marquardt.common.domain.Signable;
import org.echocat.marquardt.common.exceptions.InvalidCertificateException;
import org.echocat.marquardt.common.util.DateProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.PublicKey;
import java.util.Date;
import java.util.List;

public abstract class CertificateValidator<USERINFO extends Signable> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CertificateValidator.class);

    private final List<PublicKey> _trustedPublicKeys;
    private final DateProvider _dateProvider;
    private final Validator _validator = new Validator();

    public CertificateValidator(final DateProvider dateProvider, final List<PublicKey> trustedPublicKeys) {
        _trustedPublicKeys = trustedPublicKeys;
        _dateProvider = dateProvider;
    }

    protected abstract DeserializingFactory<USERINFO> getDeserializingFactory();


    protected CertificateFactory<USERINFO> getCertificateDeserializingFactory() {
        return new CertificateFactory<USERINFO>() {
            @Override
            protected DeserializingFactory<USERINFO> getFactoryOfWrapped() {
                return getDeserializingFactory();
            }
        };
    }

    public Certificate<USERINFO> deserializeAndValidateCertificate(final byte[] encodedCertificate) {
        final Certificate<USERINFO> certificate = _validator.deserialize(encodedCertificate, getCertificateDeserializingFactory());
        final PublicKey issuerPublicKey = certificate.getIssuerPublicKey();
        if (!_trustedPublicKeys.contains(issuerPublicKey)) {
            LOGGER.warn("Attack!! ALERT!!! Duck and cover!!! Certificate '{}' could not be found as trusted certificate.", issuerPublicKey);
            throw new InvalidCertificateException("certificate key of " + certificate.getPayload() + " is not trusted");
        }
        _validator.deserializeAndValidate(encodedCertificate, getCertificateDeserializingFactory(), issuerPublicKey);
        if (isExpired(certificate)) {
            throw new InvalidCertificateException("certificate of " + certificate.getPayload() + " is expired");
        }
        return certificate;
    }

    private boolean isExpired(final Certificate<USERINFO> certificate) {
        final Date now = _dateProvider.now();
        return now.after(certificate.getExpiresAt());
    }
}