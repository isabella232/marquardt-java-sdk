/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.common.web;

import com.google.common.primitives.Ints;
import org.apache.commons.io.IOUtils;
import org.echocat.marquardt.common.domain.Signature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.PublicKey;
import java.util.Base64;

/**
 * Client signed header validator. Clients must sign their requests (including their certificate) with a
 * signature to ensure the origin of the request. This enables checking that the (authority signed) certificate
 * is only used by the client that requested the certificate. This is ensured due to the fact that the PublicKey
 * contained in the certificate can validate the signature. Since the client should use its own PrivateKey/PublicKey
 * pair he is the only one capable of producing this signature.
 *
 * Simple: No man-in-the-middle attack is possible since the sender must be the same as the one that
 * obtained the certificate.
 */
public class RequestValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestValidator.class);

    /**
     * Validate a request header that contains a Signature with this.
     *
     * @param request The request to validate.
     * @param keyToValidateWith Client's PublicKey. Should be taken from the X-Certificate header.
     * @return True if the signature is valid. False if not.
     */
    public boolean isValid(HttpServletRequest request, PublicKey keyToValidateWith) {
        final ByteArrayOutputStream bytesToSign = new ByteArrayOutputStream();
        try {
            writeRequestTo(request, bytesToSign);
            final Signature signature = extractSignatureFromHeader(request);
            return signature.isValidFor(bytesToSign.toByteArray(), keyToValidateWith);
        } catch (IOException e) {
            LOGGER.warn("Invalid signature found.", e);
        } finally {
            IOUtils.closeQuietly(bytesToSign);
        }
        return false;
    }

    private Signature extractSignatureFromHeader(HttpServletRequest request) {
        final String header = request.getHeader("X-Signature");
        if (header == null) {
            throw new IllegalArgumentException("Expected non-empty signature header.");
        }
        return new Signature(Base64.getDecoder().decode(header));
    }

    private void writeRequestTo(HttpServletRequest request, ByteArrayOutputStream bytesToSign) throws IOException {
        final byte[] requestBytes = (request.getMethod() + " " + request.getRequestURI()).getBytes();
        bytesToSign.write(Ints.toByteArray(requestBytes.length));
        bytesToSign.write(requestBytes);
        for (SignatureHeaders headerToInclude : SignatureHeaders.values()) {
            final String header = request.getHeader(headerToInclude.getHeaderName());
            if (header != null) {
                final byte[] headerBytes = (headerToInclude.getHeaderName() + ":" + header).getBytes();
                bytesToSign.write(Ints.toByteArray(headerBytes.length));
                bytesToSign.write(headerBytes);
            }
        }
    }
}