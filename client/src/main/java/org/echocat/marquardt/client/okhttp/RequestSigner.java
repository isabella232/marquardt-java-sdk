/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.client.okhttp;

import com.google.common.primitives.Ints;
import com.squareup.okhttp.Request;
import org.echocat.marquardt.common.Signer;
import org.echocat.marquardt.common.web.SignatureHeaders;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.PrivateKey;
import java.util.List;

import static org.apache.commons.codec.binary.Base64.encodeBase64;

/**
 * Creates signatures of http requests based on request headers.
 */
public class RequestSigner {
    private final Signer _signer = new Signer();

    /**
     * Creates a signature for the request with the provided private key. The signature will be based on
     * the request headers specified in {@link SignatureHeaders}.
     *
     * @param request       the request to be signed
     * @param keyToSignWith private key to be used for the signature
     * @return signature as byte stream
     * @throws IOException
     */
    public byte[] getSignature(final Request request, final PrivateKey keyToSignWith) throws IOException {
        try (final ByteArrayOutputStream bytesToSign = new ByteArrayOutputStream()) {
            writeRequestTo(request, bytesToSign);
            return encodeBase64(getSigner().signatureOf(bytesToSign.toByteArray(), keyToSignWith));
        }
    }

    private void writeRequestTo(final Request request, final ByteArrayOutputStream bytesToSign) throws IOException {
        final byte[] requestBytes = (request.method() + " " + request.uri().getPath()).getBytes();
        bytesToSign.write(Ints.toByteArray(requestBytes.length));
        bytesToSign.write(requestBytes);
        for (final SignatureHeaders headerToInclude : SignatureHeaders.values()) {
            final String headerValue = request.header(headerToInclude.getHeaderName());
            if (headerValue != null) {
                final byte[] headerBytes = (headerToInclude.getHeaderName() + ":" + headerValue).getBytes();
                bytesToSign.write(Ints.toByteArray(headerBytes.length));
                bytesToSign.write(headerBytes);
            }
        }
    }

    private String getFirstHeaderValue(final List<String> headerValues) {
        try {
            return headerValues.get(0);
        } catch (final IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("No payload of known signature header.", e);
        }
    }

    public Signer getSigner() {
        return _signer;
    }
}