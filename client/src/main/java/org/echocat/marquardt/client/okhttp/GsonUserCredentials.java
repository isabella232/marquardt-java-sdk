/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.client.okhttp;

import com.google.gson.annotations.SerializedName;
import org.echocat.marquardt.common.domain.Credentials;

import java.security.PublicKey;

public class GsonUserCredentials implements Credentials {

    @SerializedName("email")
    private final String _email;
    @SerializedName("password")
    private final String _password;
    @SerializedName("publicKey")
    private final PublicKey _publicKey;
    @SerializedName("clientId")
    private final String _clientId;

    public GsonUserCredentials(final String email,
                               final String password,
                               final PublicKey publicKey,
                               final String clientId) {
        _email = email;
        _password = password;
        _publicKey = publicKey;
        _clientId = clientId;
    }

    @Override
    public String getIdentifier() {
        return _email;
    }

    @Override
    public String getPassword() {
        return _password;
    }

    @Override
    public PublicKey getPublicKey() {
        return _publicKey;
    }

    @Override
    public String getClientId() {
        return _clientId;
    }
}
