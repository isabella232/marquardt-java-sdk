/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.common.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.echocat.marquardt.common.domain.PublicKeyWithMechanism;

import java.io.IOException;
import java.security.PublicKey;
import java.util.Base64;

public class PublicKeySerializer extends JsonSerializer<PublicKey> {

    @Override
    public void serialize(final PublicKey publicKey, final JsonGenerator jsonGenerator, final SerializerProvider serializerProvider) throws IOException {
        // TODO! Please transport like in the certificate.
        final PublicKeyWithMechanism publicKeyWithMechanism = new PublicKeyWithMechanism(publicKey);
        jsonGenerator.writeStartObject();
        jsonGenerator.writeBinaryField("key", Base64.getEncoder().encode(publicKeyWithMechanism.getContent()));
        jsonGenerator.writeEndObject();
    }
}
