/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.authority.testdomain;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.echocat.marquardt.common.domain.SignUpAccountData;

public class TestSignUpAccountData implements SignUpAccountData<TestUserCredentials> {

    public static TestSignUpAccountData of(final TestUserCredentials credentials) {
        final TestSignUpAccountData result = new TestSignUpAccountData();
        result.setCredentials(credentials);
        return result;
    }

    @JsonProperty("credentials")
    private TestUserCredentials _credentials;

    @Override
    public TestUserCredentials getCredentials() {
        return _credentials;
    }

    public void setCredentials(final TestUserCredentials credentials) {
        _credentials = credentials;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final TestSignUpAccountData other = (TestSignUpAccountData) o;
        return new EqualsBuilder().append(getCredentials(), other.getCredentials()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getCredentials()).toHashCode();
    }
}