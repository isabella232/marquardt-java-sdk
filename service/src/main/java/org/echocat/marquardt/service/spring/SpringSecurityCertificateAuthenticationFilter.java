/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.service.spring;

import org.echocat.marquardt.common.CertificateValidator;
import org.echocat.marquardt.common.domain.Certificate;
import org.echocat.marquardt.common.domain.Signable;
import org.echocat.marquardt.common.web.RequestValidator;
import org.echocat.marquardt.service.CertificateAuthenticationFilter;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @param <SIGNABLE>
 */
public abstract class SpringSecurityCertificateAuthenticationFilter<SIGNABLE extends Signable> extends CertificateAuthenticationFilter<SIGNABLE> {

    public SpringSecurityCertificateAuthenticationFilter(CertificateValidator certificateValidator) {
        super(certificateValidator, new RequestValidator());
    }

    @Override
    protected void authenticateUser(Certificate<SIGNABLE> certificate) {
        SecurityContextHolder.getContext().setAuthentication(new CertificateAuthenticationWrapper(getIdentifier(certificate.getPayload()), certificate));
    }

    /**
     * @param signable
     * @return
     */
    protected abstract String getIdentifier(SIGNABLE signable);
}
