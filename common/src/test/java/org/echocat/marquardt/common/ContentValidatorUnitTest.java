/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.common;

import com.google.common.base.Function;
import org.apache.commons.lang3.ArrayUtils;
import org.echocat.marquardt.common.domain.Certificate;
import org.echocat.marquardt.common.exceptions.InvalidSignatureException;
import org.echocat.marquardt.common.exceptions.SecurityMechanismException;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nullable;
import java.io.EOFException;
import java.io.IOException;
import java.security.PublicKey;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.fail;

public class ContentValidatorUnitTest extends SigningUnitTest {

    private static final String SOME_PAYLOAD = "Some payload";
    private static final PublicKey INVALID_PUBLIC_KEY = new PublicKey() {
        @Override
        public String getAlgorithm() {
            return "RSA";
        }
        @Override
        public String getFormat() {
            return "X.509";
        }
        @Override
        public byte[] getEncoded() {
            return new byte[0];
        }
    };

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


    @Test(expected = SecurityMechanismException.class)
    public void shouldThrowExceptionWhenCertificateContainsInvalidKey() throws Exception {
        givenSignedDefectCertificate();
        whenValidatedPayloadIsDeserialized();
    }

    @Test(expected = InvalidSignatureException.class)
    public void shouldThrowExceptionWhenPublicKeyIsNull() throws IOException {
        givenUserInfoCertificate();
        whenSigning();
        whenPublicKeyForCertificateReturnsNull();
    }

    @Test
    public void shouldCatchIoExceptionAndRethrowAsRuntimeException() throws IOException {
        givenUserInfoCertificate();
        whenSigning();
        whenManipulatingCertificateBytes();
        thenAnWrappedIoExceptionIsThrown();
    }

    private void whenPublicKeyForCertificateReturnsNull() {
        _contentValidator.deserializeCertificateAndValidateSignature(_signedPayload, SignablePayload.FACTORY, new Function<Certificate<SignablePayload>, PublicKey>() {
            @Nullable
            @Override
            public PublicKey apply(final Certificate<SignablePayload> signablePayloadCertificate) {
                return null;
            }
        });
    }

    private void thenAnWrappedIoExceptionIsThrown() {
        try {
            whenValidatedPayloadIsDeserialized();
            fail(RuntimeException.class + " expected to be thrown!");
        } catch (final RuntimeException e) {
            assertThat(e.getCause() instanceof EOFException, is(true));
        }
    }

    private void whenManipulatingCertificateBytes() {
        final int length = _signedPayload.length;
        _signedPayload = ArrayUtils.subarray(_signedPayload, 0, length - 2);
    }

    private void givenSignedDefectCertificate() throws IOException {
        givenDefectUserInfoCertificate();
    }

    private void givenDefectUserInfoCertificate() throws IOException {
        _certificate = Certificate.create(_issuerKeys.getPublicKey(), INVALID_PUBLIC_KEY, ROLE_CODES, new SignablePayload(SOME_PAYLOAD));
        whenSigning();
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

    private void whenValidatedPayloadIsDeserialized() {
        _deserializedCertificate = _contentValidator.deserializeCertificateAndValidateSignature(_signedPayload, SignablePayload.FACTORY, new Function<Certificate<SignablePayload>, PublicKey>() {
            @Nullable
            @Override
            public PublicKey apply(final Certificate<SignablePayload> signablePayloadCertificate) {
                return _issuerKeys.getPublicKey();
            }
        });
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