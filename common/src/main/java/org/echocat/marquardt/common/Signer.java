/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.common;

import com.google.common.primitives.Ints;
import org.apache.commons.io.IOUtils;
import org.echocat.marquardt.common.domain.Signable;
import org.echocat.marquardt.common.domain.Signature;

import javax.annotation.Nonnull;
import javax.annotation.WillNotClose;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.PrivateKey;

/**
 * Creates signed bytes from a Signable using a PrivateKey.
 *
 * Authority uses this to create Certificates.
 * Clients use this to sign their requests to identify as the sender.
 *
 * @see Signable
 * @see Signature
 */
public class Signer {

    /**
     * Signs a Signable using a PrivateKey. Produces byte[] containing the serialized Signable and the Signature.
     *
     * @param signable Signable to sign
     * @param privateKey Key to create the Signature with
     * @return Signed bytes.
     * @throws IOException When problems occur while serializing the Signable or while writing the Signature.
     */
    public byte[] sign(final Signable signable, final PrivateKey privateKey) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            final byte[] contentToSign = signable.getContent();
            baos.write(contentToSign);
            writeSignature(baos, contentToSign, privateKey);
            return baos.toByteArray();
        } finally {
            IOUtils.closeQuietly(baos);
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