/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.service.security;

import org.echocat.marquardt.common.CertificateValidator;
import org.echocat.marquardt.common.domain.Certificate;
import org.echocat.marquardt.common.domain.Signable;
import org.echocat.marquardt.common.web.RequestValidator;
import org.echocat.marquardt.service.spring.CertificateAuthenticationWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Base64;

public abstract class CertificateAuthenticationFilter<USERINFO extends Signable> extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CertificateAuthenticationFilter.class);
    private final CertificateValidator<USERINFO> _certificateValidator;
    private RequestValidator _requestValidator;

    public CertificateAuthenticationFilter(final CertificateValidator<USERINFO> certificateValidator) {
        _certificateValidator = certificateValidator;
        _requestValidator = new RequestValidator();

    }

    @Override
    protected void doFilterInternal(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse, final FilterChain filterChain) throws ServletException, IOException {
            try {
                final String header = httpServletRequest.getHeader("X-Certificate");
                if (header != null) {
                    final byte[] decodedCertificate = Base64.getDecoder().decode(header);
                    final Certificate<USERINFO> certificate = _certificateValidator.deserializeAndValidateCertificate(decodedCertificate);
                    LOGGER.debug("Successful extracted user info from header {}", certificate.getPayload());
                    if(_requestValidator.isValid(httpServletRequest, certificate.getClientPublicKey())) {
                        SecurityContextHolder.getContext().setAuthentication(new CertificateAuthenticationWrapper(getUserName(certificate.getPayload()), certificate));
                    }

                }
            } finally {
                filterChain.doFilter(httpServletRequest, httpServletResponse);
            }
    }

    protected abstract String getUserName(USERINFO userinfo);
}
