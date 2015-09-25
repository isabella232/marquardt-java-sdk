/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.common;

import org.echocat.marquardt.common.keyprovisioning.KeyPairProvider;
import org.echocat.marquardt.common.domain.Signable;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.security.PrivateKey;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;

public class SignerUnitTest {

    private static final String SOME_PAYLOAD = "Some payload";

    private KeyPairProvider _keys;
    private Signer _signer;

    private Signable _signable;
    private byte[] _signedPayload;

    @Before
    public void setup() {
        _keys = TestKeyPairProvider.create();
        _signer = new Signer();
    }

    @Test
    public void shouldSignUserDetails() throws Exception {
        givenSignedPayload();
        whenSigning();
        thenSignedPayloadIsProduced();
    }

    private void thenSignedPayloadIsProduced() {
        assertThat(_signedPayload, is(not(nullValue())));
    }

    private void givenSignedPayload() throws IOException {
        _signable = new SignablePayload(SOME_PAYLOAD);
        whenSigning();
    }

    private void whenSigning() throws IOException {
        whenSigning(_keys.getPrivateKey());
    }

    private void whenSigning(final PrivateKey privateKey) throws IOException {
        _signedPayload = _signer.sign(_signable, privateKey);
    }

}
