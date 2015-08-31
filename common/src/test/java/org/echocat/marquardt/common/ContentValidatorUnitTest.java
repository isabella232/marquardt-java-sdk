/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.common;

import org.echocat.marquardt.common.domain.Certificate;
import org.echocat.marquardt.common.exceptions.InvalidSignatureException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;

public class ContentValidatorUnitTest extends SigningUnitTest {

    private static final String SOME_PAYLOAD = "Some payload";
    private ContentValidator _contentValidator;
    private Certificate<SignablePayload> _deserializedCertificate;

    @Before
    public void setup() {
        super.setup();
        _contentValidator = new ContentValidator();
    }

    @Test
    public void shouldValidateSignedPayload() throws Exception {
        givenSignedPayload();
        whenValidatedPayloadIsDeserialized();
        thenCorrectCertificateIsDeserialized();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldCheckVersion() throws Exception {
        givenSignedPayloadWithWrongVersion();
        whenValidatedPayloadIsDeserialized();
    }

    @Test(expected = InvalidSignatureException.class)
    public void shouldNotValidateCertificateFromUnknownIssuer() throws Exception {
        givenSignedPayloadFromUnknownIssuer();
        whenValidatedPayloadIsDeserialized();
    }

    private void givenSignedPayloadFromUnknownIssuer() throws IOException {
        givenUserInfoCertificate();
        whenSigning(TestKeyPairProvider.create().getPrivateKey());
    }

    private void givenSignedPayload() throws IOException {
        givenUserInfoCertificate();
        whenSigning();
    }

    private void givenSignedPayloadWithWrongVersion() throws IOException {
        givenSignedPayload();
        _signedPayload[0] = Byte.MAX_VALUE;
    }

    private void whenValidatedPayloadIsDeserialized() throws IOException {
        _deserializedCertificate = _contentValidator.validateAndDeserializeCertificate(_signedPayload, SignablePayload.FACTORY, _issuerKeys.getPublicKey());
    }

    private void thenCorrectCertificateIsDeserialized() {
        thenCertificateContainsPublicKeyOfIssuerAndClient();
        thenCertificateContainsExpiryDate();
        thenCertificateContainsRoles();
        thenDeserializedPayloadIsTheSameAsBeforeSigning();
    }

    private void thenCertificateContainsPublicKeyOfIssuerAndClient() {
        assertThat(_deserializedCertificate.getClientPublicKey(), is(_clientKeys.getPublicKey()));
        assertThat(_deserializedCertificate.getIssuerPublicKey(), is(_issuerKeys.getPublicKey()));
    }

    private void thenCertificateContainsExpiryDate() {
        assertThat(_deserializedCertificate.getExpiresAt(), is(not(nullValue())));
    }

    private void thenCertificateContainsRoles() {
        assertThat(_deserializedCertificate.getRoleCodes(), is(ROLE_CODES));
    }

    private void thenDeserializedPayloadIsTheSameAsBeforeSigning() {
        final SignablePayload wrapped = _deserializedCertificate.getPayload();
        assertThat(wrapped.getSomeContent(), is(SOME_PAYLOAD));
    }


}
