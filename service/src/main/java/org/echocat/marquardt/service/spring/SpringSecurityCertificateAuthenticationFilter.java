/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.service.spring;

import org.echocat.marquardt.common.CertificateValidator;
import org.echocat.marquardt.common.domain.Signable;
import org.echocat.marquardt.common.domain.certificate.Certificate;
import org.echocat.marquardt.common.domain.certificate.Role;
import org.echocat.marquardt.common.web.RequestValidator;
import org.echocat.marquardt.service.CertificateAuthenticationFilter;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Marquardt Spring Security Filter. Wraps a certificate and adds the authentication to the Spring Security Context.
 *
 * @param <SIGNABLE> Your signable user information.
 */
public abstract class SpringSecurityCertificateAuthenticationFilter<SIGNABLE extends Signable, ROLE extends Role>
        extends CertificateAuthenticationFilter<SIGNABLE, ROLE> {

    public SpringSecurityCertificateAuthenticationFilter(final CertificateValidator<SIGNABLE,ROLE> certificateValidator) {
        super(certificateValidator, new RequestValidator());
    }

    @Override
    protected void authenticateUser(final Certificate<SIGNABLE> certificate) {
        SecurityContextHolder.getContext().setAuthentication(new CertificateAuthenticationWrapper(getIdentifier(certificate.getPayload()), certificate));
    }

    /**
     * Extract a identifier (username) from your Signable implementation.
     *
     * @param signable Your signable implementation.
     * @return The identifier of your user info wrapped into the certificate.
     */
    protected abstract String getIdentifier(SIGNABLE signable);
}