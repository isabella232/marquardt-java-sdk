/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.common;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;

public class ContentSignerUnitTest extends SigningUnitTest {

    @Test
    public void shouldSignUserDetails() throws Exception {
        givenUserInfoCertificate();
        whenSigning();
        thenSignedPayloadIsProduced();
    }

    private void thenSignedPayloadIsProduced() {
        assertThat(_signedPayload, is(not(nullValue())));
    }

}
