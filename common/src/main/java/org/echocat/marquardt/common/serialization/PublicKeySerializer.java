/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.common.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import org.echocat.marquardt.common.domain.PublicKeyWithMechanism;

import java.io.IOException;
import java.security.PublicKey;

import static org.apache.commons.codec.binary.Base64.encodeBase64;

public class PublicKeySerializer extends com.fasterxml.jackson.databind.JsonSerializer<PublicKey> {

    public static final String KEY = "key";

    @Override
    public void serialize(final PublicKey publicKey, final JsonGenerator jsonGenerator, final com.fasterxml.jackson.databind.SerializerProvider serializerProvider) throws IOException {
        final PublicKeyWithMechanism publicKeyWithMechanism = new PublicKeyWithMechanism(publicKey);
        jsonGenerator.writeStartObject();
        jsonGenerator.writeBinaryField(KEY, publicKeyWithMechanism.getContent());
        jsonGenerator.writeEndObject();
    }
}
