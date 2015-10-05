/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.common;

import com.google.common.base.Function;
import com.google.common.io.CountingInputStream;
import org.apache.commons.io.IOUtils;
import org.echocat.marquardt.common.domain.DeserializingFactory;
import org.echocat.marquardt.common.domain.Signable;
import org.echocat.marquardt.common.domain.Signature;
import org.echocat.marquardt.common.domain.certificate.Certificate;
import org.echocat.marquardt.common.exceptions.SignatureValidationFailedException;
import org.echocat.marquardt.common.util.InputStreamUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
     * Deserializes and validates signed Signables.
     *
     * @param content Serialized Signable including Signature.
     * @param signableDeserializingFactory Factory to deserialize Signable with.
     * @param publicKeyProvider to return matching public key for given signable.
     * @param <T> Type of your Signable. Also Certificate uses this.
     * @return Deserialized and validated Signable.
     *
     * @throws SignatureValidationFailedException If the signature cannot be read or no key is provided to check.
     * @throws IllegalArgumentException when Signable cannot be deserialized from content using the provided factory or
     * no Signature can be extracted from provided content.
     */
    @Nonnull
    public <T extends Signable> T deserializeAndValidate(final byte[] content, final DeserializingFactory<T> signableDeserializingFactory, final Function<T, PublicKey> publicKeyProvider) {
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(content);
        try {
            final CountingInputStream bufferedInputStream = new CountingInputStream(inputStream);
            try {
                bufferedInputStream.mark(0);
                final T signable = signableDeserializingFactory.consume(bufferedInputStream);

                final byte[] certificateBytes = readCertificateBytesAgainForLaterValidation(bufferedInputStream);

                final PublicKey publicKey = publicKeyProvider.apply(signable);
                if (publicKey == null) {
                    throw new SignatureValidationFailedException("no public key provided");
                }
                final int signatureLength = InputStreamUtils.readInt(bufferedInputStream);
                final Signature signature = new Signature(InputStreamUtils.readBytes(bufferedInputStream, signatureLength));
                if (signature.isValidFor(certificateBytes, publicKey)) {
                    return signable;
                }
                throw new SignatureValidationFailedException("signature is invalid for provided public key");
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

    /**
     * Convenience method with signable independent public key to deserializes and validates signed Signables.
     *
     * @param content Serialized Signable including Signature.
     * @param signableDeserializingFactory Factory to deserialize Signable with.
     * @param publicKey to return.
     * @param <T> Type of your Signable. Also Certificate uses this.
     * @return Deserialized and validated Signable.
     *
     * @throws SignatureValidationFailedException If the signature cannot be read or no key is provided to check.
     * @throws IllegalArgumentException when Signable cannot be deserialized from content using the provided factory or
     * no Signature can be extracted from provided content.
     */
    public <T extends Signable> T deserializeAndValidate(final byte[] content, final DeserializingFactory<T> signableDeserializingFactory, final PublicKey publicKey) {
        return deserializeAndValidate(content, signableDeserializingFactory, new Function<T, PublicKey>() {
            @Nullable
            @Override
            public PublicKey apply(final T t) {
                return publicKey;
            }
        });
    }

    private byte[] readCertificateBytesAgainForLaterValidation(final CountingInputStream bufferedInputStream) throws IOException {
        final int position = (int)bufferedInputStream.getCount();
        bufferedInputStream.reset();
        final byte[] bytes = new byte[position];
        IOUtils.read(bufferedInputStream, bytes, 0, position);
        return bytes;
    }
}