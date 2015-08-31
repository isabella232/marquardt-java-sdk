/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.common;

import org.echocat.marquardt.common.domain.KeyPairProvider;

import java.security.*;

public final class TestKeyPairProvider implements KeyPairProvider {

    private PublicKey _publicKey;
    private PrivateKey _privateKey;
    private TestKeyPairProvider() {
    }

    public static KeyPairProvider create() {
        try {
            final KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
            final SecureRandom random = new SecureRandom();
            keyGenerator.initialize(1024, random);
            final KeyPair keyPair = keyGenerator.generateKeyPair();
            final TestKeyPairProvider testKeyProvider = new TestKeyPairProvider();
            testKeyProvider._privateKey = keyPair.getPrivate();
            testKeyProvider._publicKey = keyPair.getPublic();
            return testKeyProvider;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PublicKey getPublicKey() {
        return _publicKey;
    }

    @Override
    public PrivateKey getPrivateKey() {
        return _privateKey;
    }
}
