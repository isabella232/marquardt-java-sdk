/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.common;

import com.google.common.io.CountingInputStream;
import org.apache.commons.io.IOUtils;
import org.echocat.marquardt.common.domain.Certificate;
import org.echocat.marquardt.common.domain.DeserializingFactory;
import org.echocat.marquardt.common.domain.Signable;
import org.echocat.marquardt.common.domain.Signature;
import org.echocat.marquardt.common.exceptions.InvalidSignatureException;
import org.echocat.marquardt.common.util.InputStreamUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.PublicKey;

/**
 * Validates and deserializes signed Signables.
 *
 * @see Signature
 * @see Signable
 * @see Signer
 * @see Certificate
 */
public class Validator {

    /**
     * Only deserializes the Signable from a byte[].
     *
     * @param content Bytes containing the serialized Signable.
     * @param signableDeserializingFactory Factory capable of deserializing the Signable.
     * @param <T> Your Signable implementation. Also Certificate uses this.
     * @return Deserialized Signable.
     * @throws IllegalArgumentException when Signable cannot be deserialized from content using the provided factory.
     */
    public <T extends Signable> T deserialize(final byte[] content,
                                              final DeserializingFactory<T> signableDeserializingFactory) {
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(content);
        try {
            final CountingInputStream bufferedInputStream = new CountingInputStream(inputStream);
            try {
                bufferedInputStream.mark(0);
                final T signable = signableDeserializingFactory.consume(bufferedInputStream);
                return signable;
            } finally {
                IOUtils.closeQuietly(bufferedInputStream);
            }
        } catch (final IOException e) {
            throw new IllegalArgumentException("Signable cannot be deserialized using " + signableDeserializingFactory.getClass()
                    + " or content is wrong.", e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    /**
     * Deserializes and validates signed Signables.
     *
     * @param content Serialized Signable including Signature.
     * @param signableDeserializingFactory Factory to deserialize Signable with.
     * @param publicKey PublicKey to validate Signature against.
     * @param <T> Type of your Signable. Also Certificate uses this.
     * @return Deserialized and validated Signable.
     *
     * @throws InvalidSignatureException If the signature cannot be read or no key is provided to check.
     * @throws IllegalArgumentException when Signable cannot be deserialized from content using the provided factory or
     * no Signature can be extracted from provided content.
     */
    public <T extends Signable> T deserializeAndValidate(final byte[] content,
                                                         final DeserializingFactory<T> signableDeserializingFactory,
                                                         PublicKey publicKey) {
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(content);
        try {
            final CountingInputStream bufferedInputStream = new CountingInputStream(inputStream);
            try {
                bufferedInputStream.mark(0);
                final T signable = signableDeserializingFactory.consume(bufferedInputStream);

                final byte[] signableBytesWithoutSignature = readBytesAgainForLaterValidation(bufferedInputStream);

                if (publicKey == null) {
                    throw new InvalidSignatureException("no public key provided");
                }
                final int signatureLength = InputStreamUtils.readInt(bufferedInputStream);
                final Signature signature = new Signature(InputStreamUtils.readBytes(bufferedInputStream, signatureLength));
                if (signature.isValidFor(signableBytesWithoutSignature, publicKey)) {
                    return signable;
                }
                throw new InvalidSignatureException("signature is invalid for provided public key");
            } finally {
                IOUtils.closeQuietly(bufferedInputStream);
            }
        } catch (final IOException e) {
            throw new IllegalArgumentException("Signable cannot be deserialized using " + signableDeserializingFactory.getClass()
                    + " or content is wrong / contains no signature.", e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    private byte[] readBytesAgainForLaterValidation(final CountingInputStream bufferedInputStream) throws IOException {
        final int position = (int) bufferedInputStream.getCount();
        bufferedInputStream.reset();
        final byte[] bytes = new byte[position];
        IOUtils.read(bufferedInputStream, bytes, 0, position);
        return bytes;
    }

}
