/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.common;

import org.apache.commons.io.IOUtils;
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
            IOUtils.closeQuietly(inputStream);
        }
    }

    private <T extends Signable> Certificate<T> validateAndDeserialize(final byte[] content, final DeserializingFactory<Certificate<T>> signableFactory, final PublicKeyProvider<T> publicKeyProvider) throws IOException {
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(content);
        try {
            final Certificate<T> signable = signableFactory.consume(inputStream);

            final PublicKey publicKey = publicKeyProvider.provide(signable);
            final int signatureLength = InputStreamUtils.readInt(inputStream);
            final Signature signature = new Signature(InputStreamUtils.readBytes(inputStream, signatureLength));
            if (signature.isValidFor(signable.getContent(), publicKey)) {
                return signable;
            }
            throw new InvalidSignatureException();
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    public <T extends Signable> Certificate<T> deserializeCertificateAndValidateSignature(final byte[] content, final DeserializingFactory<T> wrappedSignableFactory, final PublicKeyProvider<T> provider) throws IOException {
        return validateAndDeserialize(content, getCertificateFactory(wrappedSignableFactory), provider);
    }

    public <T extends Signable> Certificate<T> validateAndDeserializeCertificate(final byte[] content, final DeserializingFactory<T> wrappedSignableFactory, final PublicKey publicKey) throws IOException {
        return validateAndDeserialize(content, getCertificateFactory(wrappedSignableFactory), publicKey);
    }

    public <T extends Signable> T deserialize(final byte[] content, final DeserializingFactory<T> signableFactory) throws IOException {
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(content);
        try {
            return signableFactory.consume(inputStream);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    public <T extends Signable> Certificate<T> deserializeCertificate(final byte[] content, final DeserializingFactory<T> wrappedSignableFactory) throws IOException {
        return deserialize(content, getCertificateFactory(wrappedSignableFactory));
    }

    private <T extends Signable> CertificateFactory<T> getCertificateFactory(final DeserializingFactory<T> signableFactory) {
        return new CertificateFactory<T>() {
            @Override
            protected DeserializingFactory<T> getFactoryOfWrapped() {
                return signableFactory;
            }
        };
    }

    public static interface PublicKeyProvider<T extends Signable> {

        public PublicKey provide(final Certificate<T> signable);
    }
}
