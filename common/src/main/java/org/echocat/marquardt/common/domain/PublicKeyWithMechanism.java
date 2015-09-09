/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.common.domain;

import com.google.common.primitives.Ints;
import org.echocat.marquardt.common.exceptions.SecurityMechanismException;
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

/**
 * Used to serialize / deserialize public keys. Capable of writing a public key and the type / algorithm
 * of this key into a byte array.
 */
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

        @Nonnull
        private static Mechanism mechanismWithName(@Nullable final String name) {
            final Mechanism mechanism = findMechanism(name);
            if (mechanism == null) {
                throw new IllegalArgumentException("No mechanism with name " + name + " is supported.");
            }
            return mechanism;
        }

        @Nullable
        public static Mechanism findMechanism(@Nullable final String name) {
            if (name != null) {
                for (final Mechanism candidate : values()) {
                    if (name.equals(candidate.getName())) {
                        return candidate;
                    }
                }
            }
            return null;
        }

        @Nullable
        public static Mechanism findMechanism(final byte code) {
            for (final Mechanism candidate : values()) {
                if (code == candidate.getCode()) {
                    return candidate;
                }
            }
            return null;
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
        super(Mechanism.mechanismWithName(publicKey.getAlgorithm()), publicKey.getEncoded());
    }

    public PublicKeyWithMechanism(@Nonnull final String name, final byte[] value) {
        super(Mechanism.mechanismWithName(name), value);
    }

    public PublicKeyWithMechanism(@Nonnull final byte[] content) {
        super(content);
    }

    @Override
    protected Mechanism codeToMechanism(final byte code) {
        return Mechanism.findMechanism(code);
    }

    /**
     *
     * @return java.security.PublicKey.
     */
    @Nonnull
    public PublicKey toJavaKey() {
        try {
            final X509EncodedKeySpec spec = new X509EncodedKeySpec(getValue());
            final KeyFactory factory = KeyFactory.getInstance(getMechanism().getJavaInternalName());
            return factory.generatePublic(spec);
        } catch (final GeneralSecurityException e) {
            throw new SecurityMechanismException("Could not convert to java key.", e);
        }
    }

    /**
     * Writes this key into an output stream.
     *
     * @param out OutputStream to write to.
     * @throws IOException That happened while writing to the stream.
     */
    public void writeTo(@Nonnull @WillNotClose final OutputStream out) throws IOException {
        final byte[] content = this.getContent();
        out.write(Ints.toByteArray(content.length));
        out.write(content);
    }

    /**
     * Reads this key from an input stream.
     *
     * @param in
     * @return PublicKeyWithMechanism
     * @throws IOException That happened while reading from the stream.
     */
    public static PublicKeyWithMechanism readFrom(@Nonnull @WillNotClose final InputStream in) throws IOException {
        final int serializedKeySize = InputStreamUtils.readInt(in);
        return new PublicKeyWithMechanism(InputStreamUtils.readBytes(in, serializedKeySize));
    }

    @Override
    public String toString() {
        return "PublicKeyWithMechanism of " + super.toString();
    }
}
