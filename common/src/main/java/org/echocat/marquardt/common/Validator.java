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
import org.echocat.marquardt.common.domain.DeserializingFactory;
import org.echocat.marquardt.common.domain.Signable;
import org.echocat.marquardt.common.domain.Signature;
import org.echocat.marquardt.common.exceptions.InvalidSignatureException;
import org.echocat.marquardt.common.util.InputStreamUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.PublicKey;

public class Validator {

    public <T extends Signable> T deserialize(final byte[] content, final DeserializingFactory<T> signableDeserializingFactory) {
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
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    public <T extends Signable> T deserializeAndValidate(final byte[] content, final DeserializingFactory<T> signableDeserializingFactory, PublicKey publicKey) {
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(content);
        try {
            final CountingInputStream bufferedInputStream = new CountingInputStream(inputStream);
            try {
                bufferedInputStream.mark(0);
                final T signable = signableDeserializingFactory.consume(bufferedInputStream);

                final byte[] certificateBytes = readCertificateBytesAgainForLaterValidation(bufferedInputStream);

                if (publicKey == null) {
                    throw new InvalidSignatureException();
                }
                final int signatureLength = InputStreamUtils.readInt(bufferedInputStream);
                final Signature signature = new Signature(InputStreamUtils.readBytes(bufferedInputStream, signatureLength));
                if (signature.isValidFor(certificateBytes, publicKey)) {
                    return signable;
                }
                throw new InvalidSignatureException();
            } finally {
                IOUtils.closeQuietly(bufferedInputStream);
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    private byte[] readCertificateBytesAgainForLaterValidation(final CountingInputStream bufferedInputStream) throws IOException {
        final int position = (int) bufferedInputStream.getCount();
        bufferedInputStream.reset();
        final byte[] bytes = new byte[position];
        IOUtils.read(bufferedInputStream, bytes, 0, position);
        return bytes;
    }

}
