/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.service.api;

import org.echocat.marquardt.common.SignablePayload;
import org.echocat.marquardt.common.SigningUnitTest;
import org.echocat.marquardt.common.domain.Certificate;
import org.echocat.marquardt.common.domain.DeserializingFactory;
import org.echocat.marquardt.common.exceptions.InvalidCertificateException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.security.PublicKey;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;


public class CertificateValidatorUnitTest extends SigningUnitTest {

    private final DateProvider _mockedDateProvider = mock(DateProvider.class);

    @Before
    public void prepareDateTimeProviderByAlwaysReturningCurrentDate() {
        doReturn(new Date()).when(_mockedDateProvider).now();
    }

    @Test(expected = InvalidCertificateException.class)
    public void whenIssuerPublicKeyIsNotInTrustedListAnExceptionIsThrown() throws IOException {
        givenUserInfoCertificate();
        whenSigning();
        thenCertificateCannotBeVerifiedDueToMissingIssuerPublicKey();
    }

    @Test(expected = InvalidCertificateException.class)
    public void whenCertificateIsExpiredAnExceptionIsThrown() throws IOException {
        givenUserInfoCertificate();
        whenSigning();
        thenCertificateExpiredAsNowIsInTheFuture();
    }

    @Test
    public void validCertificateCanBeDeserializedFrom() throws IOException {
        givenUserInfoCertificate();
        whenSigning();
        thenCertificateCanBeDeserializedAndVerified();
    }

    private void thenCertificateExpiredAsNowIsInTheFuture() {
        final Date dateIn16MinutesFuture = Date.from(ZonedDateTime.now().plusMinutes(16).toInstant());
        doReturn(dateIn16MinutesFuture).when(_mockedDateProvider).now();
        final TestCertificateValidator validator = new TestCertificateValidator(_mockedDateProvider, asList(_issuerKeys.getPublicKey()));
        validator.from(_signedPayload);
    }

    private void thenCertificateCannotBeVerifiedDueToMissingIssuerPublicKey() {
        final TestCertificateValidator validator = new TestCertificateValidator(_mockedDateProvider, asList());
        validator.from(_signedPayload);
    }

    private void thenCertificateCanBeDeserializedAndVerified() {
        final TestCertificateValidator validator = new TestCertificateValidator(_mockedDateProvider, asList(_issuerKeys.getPublicKey()));
        final Certificate<SignablePayload> result = validator.from(_signedPayload);
        assertThat(result, is(notNullValue()));
    }

    private static class TestCertificateValidator extends CertificateValidator<SignablePayload> {

        public TestCertificateValidator(final DateProvider dateProvider, final List<PublicKey> trustedPublicKeys) {
            super(dateProvider, trustedPublicKeys);
        }

        @Override
        protected DeserializingFactory<SignablePayload> getDeserializingFactory() {
            return SignablePayload.FACTORY;
        }
    }
}