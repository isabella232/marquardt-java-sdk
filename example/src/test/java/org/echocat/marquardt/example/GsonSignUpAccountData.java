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
    @SerializedName("firstName")
    private final String _firstName;
    @SerializedName("lastName")
    private final String _lastName;

    public GsonSignUpAccountData(final GsonUserCredentials credentials, final String firstName, final String lastName) {
        _credentials = credentials;
        _firstName = firstName;
        _lastName = lastName;
    }

    @Override
    public GsonUserCredentials getCredentials() {
        return _credentials;
    }

    public String getFirstName() {
        return _firstName;
    }

    public String getLastName() {
        return _lastName;
    }
}