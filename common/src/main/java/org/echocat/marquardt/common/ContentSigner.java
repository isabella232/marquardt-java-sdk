/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.common;

import com.google.common.primitives.Ints;
import org.echocat.marquardt.common.domain.Signable;
import org.echocat.marquardt.common.domain.Signature;

import javax.annotation.Nonnull;
import javax.annotation.WillNotClose;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.PrivateKey;

public class ContentSigner {

    public byte[] sign(final Signable signable, final PrivateKey privateKey) throws IOException {
        try (final ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            final byte[] contentToSign = signable.getContent();
            baos.write(contentToSign);
            writeSignature(baos, contentToSign, privateKey);
            return baos.toByteArray();
        }
    }

    private void writeSignature(@Nonnull @WillNotClose final OutputStream outputStream, final byte[] contentToSign, final PrivateKey privateKey) throws IOException {
        final byte[] signature = signatureOf(contentToSign, privateKey);
        outputStream.write(Ints.toByteArray(signature.length));
        outputStream.write(signature);
    }

    public byte[] signatureOf(final byte[] contentToSign, final PrivateKey privateKey) {
        final Signature signature = Signature.createFor(contentToSign,
                privateKey,
                Signature.Mechanism.rsa);
        return signature.getContent();
    }
}