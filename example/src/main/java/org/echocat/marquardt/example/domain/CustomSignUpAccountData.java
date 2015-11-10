/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.example.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.echocat.marquardt.common.domain.SignUpAccountData;

public class CustomSignUpAccountData implements SignUpAccountData<UserCredentials> {

    private String _firstName;
    private String _lastName;
    private UserCredentials _credentials;

    @JsonCreator
    public CustomSignUpAccountData(@JsonProperty("firstName") final String firstName,
                                   @JsonProperty("lastName") final String lastName,
                                   @JsonProperty("credentials") final UserCredentials credentials) {
        _firstName = firstName;
        _lastName = lastName;
        _credentials = credentials;
    }

    @JsonProperty("firstName")
    public String getFirstName() {
        return _firstName;
    }

    @JsonProperty("lastName")
    public String getLastName() {
        return _lastName;
    }

    @Override
    @JsonProperty("credentials")
    public UserCredentials getCredentials() {
        return _credentials;
    }
}
