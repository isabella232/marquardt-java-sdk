/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.authority.testdomain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.echocat.marquardt.common.domain.ClientInformation;
import org.echocat.marquardt.common.serialization.PublicKeyDeserializer;
import org.echocat.marquardt.common.serialization.PublicKeySerializer;

import java.security.PublicKey;

public class TestClientInformation implements ClientInformation {

    private final PublicKey _publicKey;
    private final String _clientId;

    public TestClientInformation(
            @JsonProperty("publicKey") @JsonDeserialize(using = PublicKeyDeserializer.class) final PublicKey publicKey,
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

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final TestClientInformation other = (TestClientInformation) o;
        return new EqualsBuilder().append(getClientId(), other.getClientId()).append(getPublicKey(), other.getPublicKey()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(_publicKey).append(_clientId).toHashCode();
    }
}