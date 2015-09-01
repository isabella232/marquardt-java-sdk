/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.common.domain;

import org.echocat.marquardt.common.TestKeyPairProvider;
import org.echocat.marquardt.common.exceptions.SecurityMechanismException;
import org.junit.Test;

import java.security.PrivateKey;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.StringContains.containsString;

public class SignatureUnitTest {

    private PrivateKey _privateKey;
    private Signature _signature;
    private String _signatureObjectToString;

    @Test
    public void shouldProduceReadableToString() throws Exception {
        givenSignature();
        whenPrintingToString();
        thenPrintedStringIsReadable();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenUnknownMechanismIsRequested() throws Exception {
        givenSignature();
        whenUnknownMechanismIsRequested();
    }

    @Test(expected = SecurityMechanismException.class)
    public void shouldThrowExceptionWhenCreatingSignatureWithInvalidPrivateKey() throws Exception {
        givenInvalidPrivateKey();
        whenSignatureIsCreated();
    }

    @Test(expected = SecurityMechanismException.class)
    public void shouldThrowExceptionWhenValidatingSignaturesWithInvalidPublicKey() throws Exception {
        givenSignature();
        whenCheckingSignature();
    }

    private void givenInvalidPrivateKey() {
        _privateKey = null;
    }

    private void givenSignature() {
        _signature = Signature.createFor(new byte[0], TestKeyPairProvider.create().getPrivateKey(), Signature.Mechanism.rsa);
    }

    private void whenPrintingToString() {
        _signatureObjectToString = _signature.toString();
    }

    private void whenUnknownMechanismIsRequested() {
        _signature.codeToMechanism(new Byte("9"));
    }

    private void whenSignatureIsCreated() {
        Signature.createFor(new byte[0], _privateKey, Signature.Mechanism.rsa);
    }

    private void whenCheckingSignature() {
        _signature.isValidFor(new byte[0], null);
    }

    private void thenPrintedStringIsReadable() {
        assertThat(_signatureObjectToString, allOf(containsString("Signature signed with"), containsString("SHA1withRSA")));
    }
}