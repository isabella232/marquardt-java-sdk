/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.example;

import com.google.gson.annotations.SerializedName;
import org.echocat.marquardt.client.okhttp.GsonUserCredentials;
import org.echocat.marquardt.common.domain.SignUpAccountData;

public class GsonSignUpAccountData implements SignUpAccountData<GsonUserCredentials> {

    @SerializedName("credentials")
    private final GsonUserCredentials _credentials;

    public GsonSignUpAccountData(final GsonUserCredentials credentials) {
        _credentials = credentials;
    }

    @Override
    public GsonUserCredentials getCredentials() {
        return _credentials;
    }
}