/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.common;

import org.echocat.marquardt.common.domain.Certificate;
import org.echocat.marquardt.common.domain.CertificateFactory;
import org.echocat.marquardt.common.domain.DeserializingFactory;
import org.echocat.marquardt.common.domain.Signable;
import org.echocat.marquardt.common.domain.Signature;
import org.echocat.marquardt.common.exceptions.InvalidSignatureException;
import org.echocat.marquardt.common.util.InputStreamUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.PublicKey;

public class ContentValidator {

    private <T extends Signable> T validateAndDeserialize(final byte[] content, final DeserializingFactory<T> signableFactory, final PublicKey publicKey) throws IOException {
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(content);
        try {
            final T signable = signableFactory.consume(inputStream);

            final int signatureLength = InputStreamUtils.readInt(inputStream);
            final Signature signature = new Signature(InputStreamUtils.readBytes(inputStream, signatureLength));
            if (signature.isValidFor(signable.getContent(), publicKey)) {
                return signable;
            }
            throw new InvalidSignatureException();
        } finally {
            try {
                inputStream.close();
            } catch (final IOException ignored) {}
        }
    }

    public <T extends Signable> Certificate<T> validateAndDeserializeCertificate(final byte[] content, final DeserializingFactory<T> wrappedSignableFactory, final PublicKey publicKey) throws IOException {
        return validateAndDeserialize(content, getCertificateFactory(wrappedSignableFactory), publicKey);
    }

    private <T extends Signable> CertificateFactory<T> getCertificateFactory(final DeserializingFactory<T> signableFactory) {
        return new CertificateFactory<T>() {
            @Override
            protected DeserializingFactory<T> getFactoryOfWrapped() {
                return signableFactory;
            }
        };
    }

}
