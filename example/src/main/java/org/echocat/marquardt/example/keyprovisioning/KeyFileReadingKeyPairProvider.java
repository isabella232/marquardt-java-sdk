/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.example.keyprovisioning;

import org.apache.commons.io.IOUtils;
import org.echocat.marquardt.common.keyprovisioning.KeyPairProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import static org.echocat.marquardt.common.domain.PublicKeyWithMechanism.Mechanism.rsa;

@Component
public class KeyFileReadingKeyPairProvider implements KeyPairProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeyFileReadingKeyPairProvider.class);

    private final PublicKey _publicKey;
    private final PrivateKey _privateKey;

    @Autowired
    public KeyFileReadingKeyPairProvider(@Value("${authentication.public.key.file}") final String publicKeyFile,
                                         @Value("${authentication.private.key.file}") final String privateKeyFile) {
        _publicKey = loadPublicKey(publicKeyFile);
        _privateKey = loadPrivateKey(privateKeyFile);
    }

    @Override
    public PublicKey getPublicKey() {
        return _publicKey;
    }

    @Override
    public PrivateKey getPrivateKey() {
        return _privateKey;
    }

    private PublicKey loadPublicKey(final String publicKeyFileName) {
        LOGGER.debug("Reading public key file {}.", publicKeyFileName);
        final byte[] keyFilePayload = readKeyFile(publicKeyFileName);
        final X509EncodedKeySpec spec = new X509EncodedKeySpec(keyFilePayload);
        try {
            final KeyFactory keyFactory = KeyFactory.getInstance(rsa.getJavaInternalName());
            return keyFactory.generatePublic(spec);
        } catch (final GeneralSecurityException e) {
            throw new IllegalArgumentException("Failed to create public key from keyfile " + publicKeyFileName + ".", e);
        }
    }

    private PrivateKey loadPrivateKey(final String privateKeyFile) {
        LOGGER.debug("Reading private key file {}.", privateKeyFile);
        final byte[] keyFilePayload = readKeyFile(privateKeyFile);
        final PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyFilePayload);
        try {
            final KeyFactory keyFactory = KeyFactory.getInstance(rsa.getJavaInternalName());
            return keyFactory.generatePrivate(spec);
        } catch (final GeneralSecurityException e) {
            throw new IllegalArgumentException("Failed to create private key from keyfile " + privateKeyFile + ".", e);
        }
    }

    private byte[] readKeyFile(final String keyFileName) {
        try (final InputStream inputStream = openStreamFromFileOrClasspathResource(keyFileName)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Cannot find resource file " + keyFileName);
            }
            return IOUtils.toByteArray(inputStream);
        } catch (final IOException e) {
            throw new IllegalArgumentException("Failed to read key file " + keyFileName + ".", e);
        }
    }

    private InputStream openStreamFromFileOrClasspathResource(final String keyFileName) throws IOException {
        final Path path = Paths.get(keyFileName);
        if (Files.exists(path)) {
            return Files.newInputStream(path);
        }
        return getClass().getClassLoader().getResourceAsStream(keyFileName);
    }
}