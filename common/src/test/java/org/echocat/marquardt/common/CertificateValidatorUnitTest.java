/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.common;

import org.echocat.marquardt.common.domain.Certificate;
import org.echocat.marquardt.common.domain.DeserializingFactory;
import org.echocat.marquardt.common.domain.KeyPairProvider;
import org.echocat.marquardt.common.domain.Signable;
import org.echocat.marquardt.common.exceptions.InvalidCertificateException;
import org.echocat.marquardt.common.exceptions.InvalidSignatureException;
import org.echocat.marquardt.common.exceptions.SecurityMechanismException;
import org.echocat.marquardt.common.util.DateProvider;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;


public class CertificateValidatorUnitTest {

    private static final long ROLE_CODES = 123L;
    private static final String SOME_PAYLOAD = "Some payload";

    private KeyPairProvider _issuerKeys;
    private KeyPairProvider _clientKeys;
    private Signer _signer;

    private Signable _signable;
    private byte[] _signedPayload;
    private Certificate<SignablePayload> _deserializedCertificate;

    private final DateProvider _mockedDateProvider = mock(DateProvider.class);

    @Before
    public void setUp() {
        _issuerKeys = TestKeyPairProvider.create();
        _clientKeys = TestKeyPairProvider.create();
        _signer = new Signer();
        doReturn(new Date()).when(_mockedDateProvider).now();
    }

    @Test(expected = InvalidCertificateException.class)
    public void whenIssuerPublicKeyIsNotInTrustedListAnExceptionIsThrown() throws IOException {
        givenSignedCertificate();
        thenCertificateCannotBeVerifiedDueToMissingIssuerPublicKey();
    }

    @Test(expected = InvalidCertificateException.class)
    public void whenCertificateIsExpiredAnExceptionIsThrown() throws IOException {
        givenSignedCertificate();
        thenCertificateExpiredAsNowIsInTheFuture();
    }

    @Test
    public void validCertificateCanBeDeserializedFrom() throws IOException {
        givenSignedCertificate();
        thenCertificateCanBeDeserializedAndVerified();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldCheckVersion() throws Exception {
        givenSignedPayloadWithWrongVersion();
        whenValidatedPayloadIsDeserialized();
    }

    @Test(expected = InvalidSignatureException.class)
    public void shouldNotValidateCertificateFromUnknownIssuer() throws Exception {
        givenSignedCertificateFromUnknownIssuer();
        whenValidatedPayloadIsDeserialized();
    }

    @Test(expected = SecurityMechanismException.class)
    public void shouldThrowExceptionWhenCertificateContainsInvalidKey() throws Exception {
        givenDefectUserInfoCertificate();
        whenValidatedPayloadIsDeserialized();
    }


    private void givenDefectUserInfoCertificate() throws IOException {
        _signable = Certificate.create(_issuerKeys.getPublicKey(), _clientKeys.getPublicKey(), ROLE_CODES, new SignablePayload(SOME_PAYLOAD));
        whenSigningWithIssuerKey();
        _signedPayload[30] = Byte.MAX_VALUE;
    }

    private void whenValidatedPayloadIsDeserialized() {
        final TestCertificateValidator validator = new TestCertificateValidator(_mockedDateProvider, asList(_issuerKeys.getPublicKey()));
        _deserializedCertificate = validator.deserializeAndValidateCertificate(_signedPayload);
    }

    private void thenCertificateExpiredAsNowIsInTheFuture() {
        final Date dateIn16MinutesFuture = new Date(new Date().getTime() + TimeUnit.MINUTES.toMillis(16));
        doReturn(dateIn16MinutesFuture).when(_mockedDateProvider).now();
        final TestCertificateValidator validator = new TestCertificateValidator(_mockedDateProvider, asList(_issuerKeys.getPublicKey()));
        validator.deserializeAndValidateCertificate(_signedPayload);
    }

    private void thenCertificateCannotBeVerifiedDueToMissingIssuerPublicKey() {
        final TestCertificateValidator validator = new TestCertificateValidator(_mockedDateProvider, new ArrayList<PublicKey>());
        validator.deserializeAndValidateCertificate(_signedPayload);
    }

    private void thenCertificateCanBeDeserializedAndVerified() {
        final TestCertificateValidator validator = new TestCertificateValidator(_mockedDateProvider, asList(_issuerKeys.getPublicKey()));
        final Certificate<SignablePayload> result = validator.deserializeAndValidateCertificate(_signedPayload);
        assertThat(result, is(notNullValue()));
    }

    private void givenSignedPayloadWithWrongVersion() throws IOException {
        givenSignedCertificate();
        _signedPayload[0] = Byte.MAX_VALUE;
    }

    private void givenSignedCertificate() throws IOException {
        givenUserInfoCertificate();
        whenSigningWithIssuerKey();
    }

    private void givenSignedCertificateFromUnknownIssuer() throws IOException {
        givenUserInfoCertificate();
        whenSigningWith(TestKeyPairProvider.create().getPrivateKey());
    }

    private void givenUserInfoCertificate() {
        _signable =  Certificate.create(_issuerKeys.getPublicKey(), _clientKeys.getPublicKey(), ROLE_CODES, new SignablePayload(SOME_PAYLOAD));
    }

    private void whenSigningWithIssuerKey() throws IOException {
        whenSigningWith(_issuerKeys.getPrivateKey());
    }

    private void whenSigningWith(PrivateKey privateKey) throws IOException {
        _signedPayload = _signer.sign(_signable, privateKey);
    }

    private static class TestCertificateValidator extends CertificateValidator<SignablePayload> {

        public TestCertificateValidator(final DateProvider dateProvider, final List<PublicKey> trustedPublicKeys) {
            super(trustedPublicKeys);
            this.setDateProvider(dateProvider);
        }

        @Override
        protected DeserializingFactory<SignablePayload> getDeserializingFactory() {
            return SignablePayload.FACTORY;
        }
    }
}