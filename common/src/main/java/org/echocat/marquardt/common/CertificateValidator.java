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
import org.echocat.marquardt.common.domain.Role;
import org.echocat.marquardt.common.domain.Signable;
import org.echocat.marquardt.common.exceptions.InvalidCertificateException;
import org.echocat.marquardt.common.exceptions.SignatureValidationFailedException;
import org.echocat.marquardt.common.serialization.RolesDeserializer;
import org.echocat.marquardt.common.util.DateProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.PublicKey;
import java.util.Date;
import java.util.List;

/**
 * Implement this to validate certificates shipping your own wrapped user informations payload. You must provide
 * a DeserializationFactory that can deserialize your wrapped payload.
 *
 * @param <USERINFO> Your wrapped user information implementing Signable.
 * @see DeserializingFactory
 * @see Signable
 * @see Certificate
 */
public abstract class CertificateValidator<USERINFO extends Signable, ROLE extends Role> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CertificateValidator.class);

    private final List<PublicKey> _trustedPublicKeys;
    private DateProvider _dateProvider;
    private final Validator _validator = new Validator();

    /**
     * Create a certificate validator that trusts a given list of PublicKeys (most likely the keys used by your authority)
     *
     * @param trustedPublicKeys Keys to trust.
     *
     * @see Validator
     */
    public CertificateValidator(final List<PublicKey> trustedPublicKeys) {
        _trustedPublicKeys = trustedPublicKeys;
        _dateProvider = new DateProvider();
    }

    /**
     * Provide your DeserializingFactory for your wrapped signable user information here!
     * @return
     */
    protected abstract DeserializingFactory<USERINFO> deserializingFactory();

    /**
     * Provide your RoleCodeGenerator for your roles implementation here!
     * @return
     */
    protected abstract RolesDeserializer<ROLE> roleCodeDeserializer();


    protected CertificateFactory<USERINFO, ROLE> getCertificateDeserializingFactory() {
        return new CertificateFactory<USERINFO, ROLE>() {
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

    public void setDateProvider(DateProvider dateProvider) {
        _dateProvider = dateProvider;
    }
}