/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.client.okhttp;

import com.google.gson.*;
import org.echocat.marquardt.common.domain.PublicKeyWithMechanism;

import java.lang.reflect.Type;
import java.security.PublicKey;

import static org.apache.commons.codec.binary.Base64.encodeBase64String;

public class PublicKeyAdapter implements JsonSerializer<PublicKey> {
    @Override
    public JsonElement serialize(PublicKey publicKey, Type type, JsonSerializationContext jsonSerializationContext) {
        PublicKeyWithMechanism publicKeyWithMechanism = new PublicKeyWithMechanism(publicKey);
        JsonObject result = new JsonObject();
        result.add("key", new JsonPrimitive(encodeBase64String(publicKeyWithMechanism.getContent())));
        return result;
    }
}
