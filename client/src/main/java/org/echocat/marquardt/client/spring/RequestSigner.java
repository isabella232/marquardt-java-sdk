/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.client.spring;

import com.google.common.primitives.Ints;
import org.apache.commons.io.IOUtils;
import org.echocat.marquardt.common.ContentSigner;
import org.echocat.marquardt.common.web.SignatureHeaders;
import org.springframework.http.HttpRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.PrivateKey;
import java.util.Base64;
import java.util.List;

public class RequestSigner {

    public final ContentSigner _contentSigner = new ContentSigner();

    public byte[] getSignature(HttpRequest request, PrivateKey keyToSignWith) throws IOException {
        ByteArrayOutputStream bytesToSign = new ByteArrayOutputStream();
        try {
            writeRequestTo(request, bytesToSign);
            return Base64.getEncoder().encode(_contentSigner.signatureOf(bytesToSign.toByteArray(), keyToSignWith));
        } finally {
            IOUtils.closeQuietly(bytesToSign);
        }
    }

    private void writeRequestTo(HttpRequest request, ByteArrayOutputStream bytesToSign) throws IOException {
        final byte[] requestBytes = (request.getMethod().name() + " " + request.getURI().getPath()).getBytes();
        bytesToSign.write(Ints.toByteArray(requestBytes.length));
        bytesToSign.write(requestBytes);
        for(SignatureHeaders headerToInclude: SignatureHeaders.values()) {
            final List<String> headerValues = request.getHeaders().get(headerToInclude.getHeaderName());
            if(headerValues != null) {
                final byte[] headerBytes = (headerToInclude.getHeaderName() + ":" + headerValues.stream().findFirst().get()).getBytes();
                bytesToSign.write(Ints.toByteArray(headerBytes.length));
                bytesToSign.write(headerBytes);
            }
        }
    }
}