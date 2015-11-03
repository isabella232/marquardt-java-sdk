/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.common;

import com.google.common.collect.Sets;
import org.apache.commons.io.IOUtils;
import org.echocat.marquardt.common.domain.certificate.Certificate;
import org.echocat.marquardt.common.domain.DeserializingFactory;
import org.echocat.marquardt.common.exceptions.ExpiredCertificateException;
import org.echocat.marquardt.common.keyprovisioning.KeyPairProvider;
import org.echocat.marquardt.common.domain.certificate.Role;
import org.echocat.marquardt.common.domain.Signable;
import org.echocat.marquardt.common.exceptions.InvalidCertificateException;
import org.echocat.marquardt.common.exceptions.SecurityMechanismException;
import org.echocat.marquardt.common.exceptions.SignatureValidationFailedException;
import org.echocat.marquardt.common.serialization.RolesDeserializer;
import org.echocat.marquardt.common.util.DateProvider;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;


public class CertificateValidatorUnitTest {

    private static final Set<Role> ROLES = Sets.<Role>newHashSet(TestRoles.TEST_ROLE_1);
    private static final String TEST_CLIENT_ID = "asdf";
    private static final String SOME_PAYLOAD = "Some payload";

    private KeyPairProvider _issuerKeys;
    private KeyPairProvider _clientKeys;
    private Signer _signer;

    private Signable _signable;
    private byte[] _signedPayload;

    private final DateProvider _mockedDateProvider = mock(DateProvider.class);
    private Certificate<SignablePayload> _validationResult;

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
        whenTheCertificateIsDeserializedAndVerifiedWithoutIssuerPublicKeys();
    }

    @Test(expected = InvalidCertificateException.class)
    public void whenCertificateIsExpiredAnExceptionIsThrown() throws IOException {
        givenSignedCertificate();
        givenTheTimeIs16MinutesInTheFuture();
        whenTheCertificateIsDeserializedAndVerified();
    }

    @Test
    public void validCertificateCanBeDeserializedFrom() throws IOException {
        givenSignedCertificate();
        whenTheCertificateIsDeserializedAndVerified();
        thenTheDeserializedCertificateIsObtained();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldCheckVersion() throws Exception {
        givenSignedCertificateWithPayloadWithWrongVersion();
        whenTheCertificateIsDeserializedAndVerified();
    }

    @Test(expected = SignatureValidationFailedException.class)
    public void shouldNotValidateCertificateFromUnknownIssuer() throws Exception {
        givenSignedCertificateFromUnknownIssuer();
        whenTheCertificateIsDeserializedAndVerified();
    }

    @Test(expected = SecurityMechanismException.class)
    public void shouldThrowExceptionWhenCertificateContainsInvalidKey() throws Exception {
        givenSignedCertificateWithDefectUserInfo();
        whenTheCertificateIsDeserializedAndVerified();
    }

    @Test
    public void whenCertificateIsExpiredItsCertificateContainsSignedBytes() throws IOException {
        givenSignedCertificate();
        givenTheTimeIs16MinutesInTheFuture();
        whenExpiredCertificateTheExceptionContainsTheCertificateAndItsSignedSerializedRepresentation();
    }

    private void givenSignedCertificateWithDefectUserInfo() throws IOException {
        _signable = Certificate.create(_issuerKeys.getPublicKey(), _clientKeys.getPublicKey(), TEST_CLIENT_ID, ROLES, new SignablePayload(SOME_PAYLOAD));
        whenSigningWithIssuerKey();
        _signedPayload[30] = Byte.MAX_VALUE;
    }

    private void givenTheTimeIs16MinutesInTheFuture() {
        //noinspection UseOfObsoleteDateTimeApi
        final Date dateIn16MinutesFuture = new Date(new Date().getTime() + TimeUnit.MINUTES.toMillis(16));
        doReturn(dateIn16MinutesFuture).when(_mockedDateProvider).now();
    }

    private void givenSignedCertificateWithPayloadWithWrongVersion() throws IOException {
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
        _signable = Certificate.create(_issuerKeys.getPublicKey(), _clientKeys.getPublicKey(), TEST_CLIENT_ID, ROLES, new SignablePayload(SOME_PAYLOAD));
    }

    private void whenTheCertificateIsDeserializedAndVerified() {
        final TestCertificateValidator validator = new TestCertificateValidator(_mockedDateProvider, Collections.singletonList(_issuerKeys.getPublicKey()));
        _validationResult = validator.deserializeAndValidateCertificate(_signedPayload);
    }

    private void whenExpiredCertificateTheExceptionContainsTheCertificateAndItsSignedSerializedRepresentation() {
        final TestCertificateValidator validator = new TestCertificateValidator(_mockedDateProvider, Collections.singletonList(_issuerKeys.getPublicKey()));
        try {
            validator.deserializeAndValidateCertificate(_signedPayload);
            fail(ExpiredCertificateException.class + " exception should have been thrown!");
        } catch (final ExpiredCertificateException e) {
            final Certificate<?> certificate = e.getCertificate();
            assertThat(certificate, notNullValue());
            final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(_signedPayload.length);
            try {
                certificate.writeTo(byteArrayOutputStream);
                assertThat(byteArrayOutputStream.toByteArray(), is(_signedPayload));
            } catch (IOException e1) {
                throw new RuntimeException(e1);
            } finally {
                IOUtils.closeQuietly(byteArrayOutputStream);
            }
        }
    }

    private void whenTheCertificateIsDeserializedAndVerifiedWithoutIssuerPublicKeys() {
        final TestCertificateValidator validator = new TestCertificateValidator(_mockedDateProvider, new ArrayList<PublicKey>());
        validator.deserializeAndValidateCertificate(_signedPayload);
    }

    private void whenSigningWithIssuerKey() throws IOException {
        whenSigningWith(_issuerKeys.getPrivateKey());
    }

    private void whenSigningWith(final PrivateKey privateKey) throws IOException {
        _signedPayload = _signer.sign(_signable, privateKey);
    }

    private void thenTheDeserializedCertificateIsObtained() {
        assertThat(_validationResult, is(notNullValue()));
    }

    private static class TestCertificateValidator extends CertificateValidator<SignablePayload, TestRoles> {

        public TestCertificateValidator(final DateProvider dateProvider, final List<PublicKey> trustedPublicKeys) {
            super(trustedPublicKeys);
            setDateProvider(dateProvider);
        }

        @Override
        protected DeserializingFactory<SignablePayload> deserializingFactory() {
            return SignablePayload.FACTORY;
        }

        @Override
        protected RolesDeserializer<TestRoles> roleCodeDeserializer() {
            return new RolesDeserializer<TestRoles>() {
                @Override
                public TestRoles createRoleFromId(final Number id) {
                    return TestRoles.fromId(id.intValue());
                }
            };
        }
    }
}