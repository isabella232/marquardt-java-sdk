/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.common;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.echocat.marquardt.common.domain.DeserializingFactory;
import org.echocat.marquardt.common.domain.Signable;
import org.echocat.marquardt.common.domain.certificate.Certificate;
import org.echocat.marquardt.common.domain.certificate.CertificateFactory;
import org.echocat.marquardt.common.domain.certificate.Role;
import org.echocat.marquardt.common.exceptions.ExpiredCertificateException;
import org.echocat.marquardt.common.exceptions.InvalidCertificateException;
import org.echocat.marquardt.common.exceptions.SignatureValidationFailedException;
import org.echocat.marquardt.common.serialization.RolesDeserializer;
import org.echocat.marquardt.common.util.DateProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.PublicKey;
import java.util.Collection;
import java.util.Date;

public abstract class CertificateValidator<USERINFO extends Signable, ROLE extends Role> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CertificateValidator.class);

    private final Function<Certificate<USERINFO>, PublicKey> _publicKeyForCertificateProvider = new Function<Certificate<USERINFO>, PublicKey>() {
        @Override
        public PublicKey apply(final Certificate<USERINFO> certificate) {
            return certificate.getIssuerPublicKey();
        }
    };
    private final Collection<PublicKey> _trustedPublicKeys;
    private final Validator _validator = new Validator();
    private final CertificateFactory<USERINFO, ROLE> _certificateFactory;
    private DateProvider _dateProvider;

    /**
     * Create a certificate validator that trusts a given list of PublicKeys (most likely the keys used by your authority)
     *
     * @param trustedPublicKeys Keys to trust.
     *
     * @see Validator
     */
    public CertificateValidator(final Collection<PublicKey> trustedPublicKeys) {
        _trustedPublicKeys = Lists.newArrayList(trustedPublicKeys);
        _dateProvider = new DateProvider();
        _certificateFactory = new CertificateFactory<USERINFO, ROLE>() {
            @Override
            protected DeserializingFactory<USERINFO> getFactoryOfWrapped() {
                return deserializingFactory();
            }

            @Override
            protected RolesDeserializer<ROLE> getRolesDeserializer() {
                return roleCodeDeserializer();
            }
        };
    }

    public void setDateProvider(final DateProvider dateProvider) {
        _dateProvider = dateProvider;
    }

    /**
     * Provide your DeserializingFactory for your wrapped signable user information here!
     */
    protected abstract DeserializingFactory<USERINFO> deserializingFactory();

    /**
     * Provide your RoleCodeGenerator for your roles implementation here!
     */
    protected abstract RolesDeserializer<ROLE> roleCodeDeserializer();

    protected CertificateFactory<USERINFO, ROLE> getCertificateDeserializingFactory() {
        return _certificateFactory;
    }

    /**
     * Validate the bytes of your certificate (including the signature part!!) using this method. You'll receive a
     * deserialized certificate if it is valid.
     *
     * @param encodedCertificate bytes of the certificate with it's signature.
     * @return Deserialized Certificate.
     * @throws InvalidCertificateException If the certificate is from an untrusted authority or expired.
     * @throws SignatureValidationFailedException if the signature cannot be read or used.
     */
    public Certificate<USERINFO> deserializeAndValidateCertificate(final byte[] encodedCertificate) {
        final Certificate<USERINFO> certificate = _validator.deserializeAndValidate(encodedCertificate, getCertificateDeserializingFactory(), _publicKeyForCertificateProvider);
        final PublicKey issuerPublicKey = certificate.getIssuerPublicKey();
        if (!_trustedPublicKeys.contains(issuerPublicKey)) {
            LOGGER.warn("Attack!! ALERT!!! Duck and cover!!! Certificate '{}' could not be found as trusted certificate.", issuerPublicKey);
            throw new InvalidCertificateException("certificate key of " + certificate.getPayload() + " is not trusted");
        }
        certificate.setSignedCertificateBytes(encodedCertificate);
        if (isExpired(certificate)) {
            throw new ExpiredCertificateException(certificate);
        }
        return certificate;
    }

    private boolean isExpired(final Certificate<USERINFO> certificate) {
        //noinspection UseOfObsoleteDateTimeApi
        final Date now = _dateProvider.now();
        return now.after(certificate.getExpiresAt());
    }
}