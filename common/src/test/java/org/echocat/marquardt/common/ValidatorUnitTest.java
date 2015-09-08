/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.common;

import org.apache.commons.lang3.ArrayUtils;
import org.echocat.marquardt.common.domain.KeyPairProvider;
import org.echocat.marquardt.common.domain.Signable;
import org.junit.Before;
import org.junit.Test;

import java.io.EOFException;
import java.io.IOException;
import java.security.PrivateKey;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.fail;

public class ValidatorUnitTest {

    private static final String SOME_PAYLOAD = "Some payload";

    private Validator _validator;
    private Signer _signer;

    private Signable _signable;
    private SignablePayload _deserializedPayload;
    private byte[] _signedPayload;

    private KeyPairProvider _issuerKeys;

    @Before
    public void setup() {
        _issuerKeys = TestKeyPairProvider.create();
        _validator = new Validator();
        _signer = new Signer();
    }

    @Test
    public void shouldValidateSignedPayload() throws Exception {
        givenSignedPayload();
        whenValidatedPayloadIsDeserialized();
        thenDeserializedPayloadIsTheSameAsBeforeSigning();
    }

    @Test
    public void shouldCatchIoExceptionAndRethrowAsRuntimeException() throws IOException {
        givenSignedPayload();
        whenSigning();
        whenManipulatingPayloadBytes();
        thenAnWrappedIoExceptionIsThrown();
    }

    private void thenAnWrappedIoExceptionIsThrown() {
        try {
            whenValidatedPayloadIsDeserialized();
            fail(RuntimeException.class + " expected to be thrown!");
        } catch (final RuntimeException e) {
            assertThat(e.getCause() instanceof EOFException, is(true));
        }
    }

    private void whenManipulatingPayloadBytes() {
        final int length = _signedPayload.length;
        _signedPayload = ArrayUtils.subarray(_signedPayload, 0, length - 2);
    }

    private void givenSignedPayload() throws IOException {
        _signable = new SignablePayload(SOME_PAYLOAD);
        whenSigning();
    }


    private void whenSigning() throws IOException {
        whenSigning(_issuerKeys.getPrivateKey());
    }

    private void whenSigning(PrivateKey privateKey) throws IOException {
        _signedPayload = _signer.sign(_signable, privateKey);
    }

    private void whenValidatedPayloadIsDeserialized() {
        _deserializedPayload = _validator.deserializeAndValidate(_signedPayload, SignablePayload.FACTORY, _issuerKeys.getPublicKey());
    }

    private void thenDeserializedPayloadIsTheSameAsBeforeSigning() {
        assertThat(_deserializedPayload.getSomeContent(), is(SOME_PAYLOAD));
    }

}