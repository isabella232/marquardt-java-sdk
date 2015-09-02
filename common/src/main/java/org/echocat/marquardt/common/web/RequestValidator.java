/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.common.web;

import com.google.common.primitives.Ints;
import org.echocat.marquardt.common.domain.Signature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.PublicKey;
import java.util.Base64;

public class RequestValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestValidator.class);

    public boolean isValid(HttpServletRequest request, PublicKey keyToValidateWith) {
        try {
            ByteArrayOutputStream bytesToSign = new ByteArrayOutputStream();
            try {
                writeRequestTo(request, bytesToSign);
                final Signature signature = extractSignatureFromHeader(request);
                return signature.isValidFor(bytesToSign.toByteArray(), keyToValidateWith);
            } finally {
                bytesToSign.close();
            }
        } catch (IOException e) {
            LOGGER.warn("Invalid signature found.", e);
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
        final byte[] requestBytes = new String(request.getMethod() + " " + request.getRequestURI()).getBytes();
        bytesToSign.write(Ints.toByteArray(requestBytes.length));
        bytesToSign.write(requestBytes);
        for (SignatureHeaders headerToInclude : SignatureHeaders.values()) {
            final String header = request.getHeader(headerToInclude.getHeaderName());
            if (header != null) {
                final byte[] headerBytes = new String(headerToInclude.getHeaderName() + ":" + header).getBytes();
                bytesToSign.write(Ints.toByteArray(headerBytes.length));
                bytesToSign.write(headerBytes);
            }
        }
    }

}
