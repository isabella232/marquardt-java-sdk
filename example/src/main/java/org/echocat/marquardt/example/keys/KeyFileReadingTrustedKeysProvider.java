/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.example.keys;

import org.echocat.marquardt.common.domain.TrustedKeysProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class KeyFileReadingTrustedKeysProvider implements TrustedKeysProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeyFileReadingKeyPairProvider.class);

    private final List<PublicKey> _publicKeys = new ArrayList<>();

    @Autowired
    public KeyFileReadingTrustedKeysProvider(@Value("${authentication.trusted.public.keys.files}") final String publicKeyFiles) {
        loadKeys(publicKeyFiles);
    }

    private void loadKeys(final String publicKeyFiles) {
        Arrays.stream(publicKeyFiles.split(",")).forEach(p -> _publicKeys.add(loadPublicKey(p.trim())));
    }

    @Override
    public List<PublicKey> getPublicKeys() {
        return _publicKeys;
    }

    private PublicKey loadPublicKey(final String publicKeyFileName) {
        LOGGER.debug("Reading public key file {}.", publicKeyFileName);
        final byte[] keyFilePayload = readKeyFile(publicKeyFileName);
        final X509EncodedKeySpec spec =
                new X509EncodedKeySpec(keyFilePayload);
        try {
            final KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(spec);
        } catch (final GeneralSecurityException e) {
            throw new IllegalArgumentException("Failed to create public key from keyfile " + publicKeyFileName + ".", e);
        }
    }

    private byte[] readKeyFile(final String keyFileName) {
        final URL resource = getClass().getClassLoader().getResource(keyFileName);
        if(resource == null) {
            throw new IllegalArgumentException("Cannot find resource file " + keyFileName);
        }
        final File keyFile = new File(resource.getFile());
        try (FileInputStream fileInputStream = new FileInputStream(keyFile)) {
            try (DataInputStream dataInputStream = new DataInputStream(fileInputStream)) {
                final byte[] keyBytes = new byte[(int) keyFile.length()];
                dataInputStream.readFully(keyBytes);
                return keyBytes;
            }
        } catch (final IOException e) {
            throw new IllegalArgumentException("Failed to read key file " + keyFile + ".", e);
        }
    }
}
