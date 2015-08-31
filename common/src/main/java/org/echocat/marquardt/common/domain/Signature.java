/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.common.domain;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

public class Signature extends BytesWithMechanism<Signature.Mechanism> {

    public enum Mechanism implements BytesWithMechanism.Mechanism {
        rsa("SHA1withRSA", Byte.MIN_VALUE, "SHA1withRSA");

        @Nonnull
        private final String _name;
        private final byte _code;
        @Nonnull
        private final String _javaAlgorithmName;

        Mechanism(@Nonnull final String name, final byte code, @Nonnull final String javaAlgorithmName) {
            _name = name;
            _code = code;
            _javaAlgorithmName = javaAlgorithmName;
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
        public static Mechanism getMechanism(final byte code) {
            final Mechanism result = findMechanism(code);
            if (result == null) {
                throw new IllegalArgumentException("Unknown mechanism: #" + code);
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
        public java.security.Signature createAlgorithm() {
            try {
                return java.security.Signature.getInstance(_javaAlgorithmName);
            } catch (final NoSuchAlgorithmException e) {
                throw new RuntimeException("Could not load expected algorithm.");
            }
        }

        @Override
        public String toString() {
            return getName();
        }
    }

    private Signature(@Nonnull final Mechanism mechanism, @Nonnull final byte[] value) {
        super(mechanism, value);
    }

    public Signature(@Nonnull final byte[] content) {
        super(content);
    }

    @Override
    protected Mechanism codeToMechanism(final byte code) {
        return Mechanism.getMechanism(code);
    }

    public boolean isValidFor(@Nonnull final byte[] content, @Nonnull final PublicKey publicKey) {
        try {
            final java.security.Signature algorithm = getMechanism().createAlgorithm();
            algorithm.initVerify(publicKey);
            algorithm.update(content);
            return algorithm.verify(getValue());
        } catch (final GeneralSecurityException e) {
            throw new RuntimeException("Could not check signature for content.", e);
        }
    }

    @Nonnull
    public static Signature createFor(@Nonnull final byte[] content, @Nonnull final PrivateKey privateKey, @Nonnull final Mechanism with) {
        try {
            final java.security.Signature algorithm = with.createAlgorithm();
            algorithm.initSign(privateKey);
            algorithm.update(content);
            return new Signature(with, algorithm.sign());
        } catch (final GeneralSecurityException e) {
            throw new RuntimeException("Could not create signature for content.", e);
        }
    }

}
