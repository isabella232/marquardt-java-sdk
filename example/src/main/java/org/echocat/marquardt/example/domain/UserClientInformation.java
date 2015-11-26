/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.example.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.echocat.marquardt.common.domain.ClientInformation;
import org.echocat.marquardt.common.serialization.PublicKeyDeserializer;
import org.echocat.marquardt.common.serialization.PublicKeySerializer;

import java.security.PublicKey;

public class UserClientInformation implements ClientInformation {

    private final PublicKey _publicKey;
    private final String _clientId;

    public UserClientInformation(@JsonProperty("publicKey") @JsonDeserialize(using = PublicKeyDeserializer.class) final PublicKey publicKey,
                                 @JsonProperty("clientId") final String clientId) {
        _publicKey = publicKey;
        _clientId = clientId;
    }

    @Override
    @JsonProperty("clientId")
    public String getClientId() {
        return _clientId;
    }

    @Override
    @JsonProperty("publicKey")
    @JsonSerialize(using = PublicKeySerializer.class)
    public PublicKey getPublicKey() {
        return _publicKey;
    }
}
