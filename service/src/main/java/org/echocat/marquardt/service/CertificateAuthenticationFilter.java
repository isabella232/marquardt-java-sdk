/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.service;

import org.echocat.marquardt.common.CertificateValidator;
import org.echocat.marquardt.common.domain.certificate.Certificate;
import org.echocat.marquardt.common.domain.certificate.Role;
import org.echocat.marquardt.common.domain.Signable;
import org.echocat.marquardt.common.exceptions.InvalidCertificateException;
import org.echocat.marquardt.common.exceptions.SignatureValidationFailedException;
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

import static org.apache.commons.codec.binary.Base64.decodeBase64;

/**
 * Implement this filter to enable login at a marquardt service (not authority!).
 * <p>
 * Clients must add a X-Certificate header which contains their (Base64 encoded) certificate payload.
 * Clients must sign their header using their PrivateKey matching to the PublicKey in the certificate.
 * This signature signes all headers including X-Certificate.<br>
 * <p>
 * The filter checks if
 * <ul>
 * <li>The certificate is signed by the authority using a trusted key</li>
 * <li>The certificate is not expired</li>
 * <li>The signature of the headers can be validated with the clients public key from the certificate</li></ul><br>
 * <p>
 * Implement the abstract method authenticateUser to build your security context with the user info from the Certificate.
 *
 * @param <SIGNABLE> Your user information.
 * @param <ROLE> Your roles implementation.
 * @see org.echocat.marquardt.common.web.SignatureHeaders
 * @see org.echocat.marquardt.service.spring.SpringSecurityCertificateAuthenticationFilter
 */
public abstract class CertificateAuthenticationFilter<SIGNABLE extends Signable, ROLE extends Role> implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CertificateAuthenticationFilter.class);
    private final CertificateValidator<SIGNABLE, ROLE> _certificateValidator;
    private final RequestValidator _requestValidator;

    public CertificateAuthenticationFilter(final CertificateValidator<SIGNABLE, ROLE> certificateValidator, final RequestValidator requestValidator) {
        _certificateValidator = certificateValidator;
        _requestValidator = requestValidator;
    }

    protected abstract String provideBase64EncodedCertificate(final HttpServletRequest httpServletRequest);

    @Override
    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain) throws IOException, ServletException {
        final HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        try {
            final String header = provideBase64EncodedCertificate(httpServletRequest);
            if (header != null) {
                final byte[] decodedCertificate = decodeBase64(header);
                final Certificate<SIGNABLE> certificate = _certificateValidator.deserializeAndValidateCertificate(decodedCertificate);
                LOGGER.debug("Successful extracted user info from header {}", certificate.getPayload());
                if (_requestValidator.isValid(httpServletRequest, certificate.getClientPublicKey())) {
                    authenticateUser(certificate);
                }
            }
        } catch (final InvalidCertificateException e) {
            LOGGER.debug("invalid certificate provided: ", e);
        }  catch (final SignatureValidationFailedException e) {
            LOGGER.debug("request signature could not be validated: ", e);
        } finally {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    /**
     * You can take over here and create a security context for the user.
     *
     * @param certificate Trusted and valid certificate.
     */
    protected abstract void authenticateUser(Certificate<SIGNABLE> certificate);

    @SuppressWarnings("NoopMethodInAbstractClass")
    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        // empty default implementation
    }

    @SuppressWarnings("NoopMethodInAbstractClass")
    @Override
    public void destroy() {
        // empty default implementation
    }
}
