/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.common.domain;

import com.google.common.primitives.Ints;
import org.echocat.marquardt.common.util.InputStreamUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.WillNotClose;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

public class PublicKeyWithMechanism extends BytesWithMechanism<PublicKeyWithMechanism.Mechanism> {

    public enum Mechanism implements BytesWithMechanism.Mechanism {
        rsa("RSA", Byte.MIN_VALUE, "RSA");

        @Nonnull
        private final String _name;

        private final byte _code;
        @Nonnull
        private final String _javaInternalName;

        Mechanism(@Nonnull final String name, final byte code, @Nonnull final String javaInternalName) {
            _name = name;
            _code = code;
            _javaInternalName = javaInternalName;
        }

        @Nullable
        public static Mechanism findMechanism(@Nullable final String name) {
            Mechanism result = null;
            if (name != null) {
                for (final Mechanism candidate : values()) {
                    if (name.equals(candidate._name)) {
                        result = candidate;
                        break;
                    }
                }
            }
            return result;
        }

        @Nullable
        public static Mechanism findMechanism(final byte code) {
            Mechanism result = null;
            for (final Mechanism candidate : values()) {
                if (code == candidate._code) {
                    result = candidate;
                    break;
                }
            }
            return result;
        }

        @Nonnull
        @Override
        public String getName() {
            return _name;
        }

        @Override
        public byte getCode() {
            return _code;
        }

        @Nonnull
        public String getJavaInternalName() {
            return _javaInternalName;
        }

        @Override
        public String toString() {
            return getName();
        }
    }

    public PublicKeyWithMechanism(@Nonnull final PublicKey publicKey) {
        super(Mechanism.findMechanism(publicKey.getAlgorithm()), publicKey.getEncoded());
    }

    public PublicKeyWithMechanism(@Nonnull final String name, final byte[] value) {
        super(Mechanism.findMechanism(name), value);
    }

    public PublicKeyWithMechanism(@Nonnull final byte[] content) {
        super(content);
    }

    @Override
    protected Mechanism codeToMechanism(final byte code) {
        return Mechanism.findMechanism(code);
    }

    @Nonnull
    public PublicKey toJavaKey() {
        try {
            final X509EncodedKeySpec spec = new X509EncodedKeySpec(getValue());
            final KeyFactory factory;
            factory = KeyFactory.getInstance(getMechanism().getJavaInternalName());
            return factory.generatePublic(spec);
        } catch (final GeneralSecurityException e) {
            throw new RuntimeException("Could not convert to java key.", e);
        }
    }

    public void writeTo(@Nonnull @WillNotClose final OutputStream out) throws IOException {
        final byte[] content = this.getContent();
        out.write(Ints.toByteArray(content.length));
        out.write(content);
    }

    public static PublicKeyWithMechanism readFrom(final InputStream in) throws IOException {
        final int serializedKeySize = InputStreamUtils.readInt(in);
        return new PublicKeyWithMechanism(InputStreamUtils.readBytes(in, serializedKeySize));
    }

}
