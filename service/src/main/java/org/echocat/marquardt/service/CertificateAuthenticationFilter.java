/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.service;

import org.echocat.marquardt.common.CertificateValidator;
import org.echocat.marquardt.common.domain.Certificate;
import org.echocat.marquardt.common.domain.Signable;
import org.echocat.marquardt.common.web.RequestValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Base64;

public abstract class CertificateAuthenticationFilter<SIGNABLE extends Signable> implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CertificateAuthenticationFilter.class);
    private final CertificateValidator<SIGNABLE> _certificateValidator;
    private final RequestValidator _requestValidator;

    public CertificateAuthenticationFilter(final CertificateValidator<SIGNABLE> certificateValidator, RequestValidator requestValidator) {
        _certificateValidator = certificateValidator;
        _requestValidator = requestValidator;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        try {
            final String header = httpServletRequest.getHeader("X-Certificate");
            if (header != null) {
                final byte[] decodedCertificate = Base64.getDecoder().decode(header);
                final Certificate<SIGNABLE> certificate = _certificateValidator.deserializeAndValidateCertificate(decodedCertificate);
                LOGGER.debug("Successful extracted user info from header {}", certificate.getPayload());
                if (_requestValidator.isValid(httpServletRequest, certificate.getClientPublicKey())) {
                    authenticateUser(certificate);
                }

            }
        } finally {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    protected abstract void authenticateUser(Certificate<SIGNABLE> certificate);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // empty default implementation
    }

    @Override
    public void destroy() {
        // empty default implementation
    }
}
