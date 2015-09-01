/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.common.domain;

import org.echocat.marquardt.common.TestKeyPairProvider;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.StringContains.containsString;

public class PublicKeyWithMechanismUnitTest {

    @Test
    public void shouldCreateInstance() throws Exception {
        new PublicKeyWithMechanism("RSA", new byte[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenCreatedWithUnknownMechanism() throws Exception {
        new PublicKeyWithMechanism("UNKNOWN", new byte[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenCreatedWithEmptyBytes() throws Exception {
        new PublicKeyWithMechanism(new byte[0]);
    }

    @Test
    public void shouldPrintReadableToString() throws Exception {
        final PublicKeyWithMechanism publicKeyWithMechanism = new PublicKeyWithMechanism(TestKeyPairProvider.create().getPublicKey());
        assertThat(publicKeyWithMechanism.toString(), allOf(containsString("PublicKeyWithMechanism of"), containsString(publicKeyWithMechanism.getMechanism().toString())));
    }
}