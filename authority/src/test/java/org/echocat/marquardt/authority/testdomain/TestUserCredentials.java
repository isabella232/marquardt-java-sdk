/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.authority.testdomain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.echocat.marquardt.common.domain.Credentials;
import org.echocat.marquardt.common.serialization.PublicKeyDeserializer;
import org.echocat.marquardt.common.serialization.PublicKeySerializer;

import java.security.PublicKey;

public class TestUserCredentials implements Credentials {

    private final String _email;
    private final String _password;
    private final PublicKey _publicKey;

    @JsonCreator
    public TestUserCredentials(@JsonProperty("email") final String email,
                               @JsonProperty("password") final String password,
                               @JsonProperty("publicKey") @JsonDeserialize(using = PublicKeyDeserializer.class) final PublicKey publicKey) {
        _email = email;
        _password = password;
        _publicKey = publicKey;
    }

    @JsonProperty("email")
    public String getIdentifier() {
        return _email;
    }

    @JsonProperty("password")
    public String getPassword() {
        return _password;
    }

    @JsonProperty("publicKey")
    @JsonSerialize(using = PublicKeySerializer.class)
    public PublicKey getPublicKey() {
        return _publicKey;
    }
}
