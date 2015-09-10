/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.common.domain;

import org.echocat.marquardt.common.SignablePayload;
import org.echocat.marquardt.common.TestKeyPairProvider;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;

public class CertificateUnitTest {

    private Certificate<SignablePayload> _certificate;
    private SignablePayload _payload;
    private static final long ROLE_CODES = 123L;
    private static final String SOME_PAYLOAD = "Some payload";

    private KeyPairProvider _issuerKeys;
    private KeyPairProvider _clientKeys;

    @Before
    public void setup() {
        _issuerKeys = TestKeyPairProvider.create();
        _clientKeys = TestKeyPairProvider.create();
    }

    @Test
    public void shouldCreateCertificate() throws Exception {
        whenCertificateIsCreated();
        thenCerificateCanBeWrittenAsString();
    }

    private void whenCertificateIsCreated() {
        _payload = new SignablePayload(SOME_PAYLOAD);
        _certificate = Certificate.create(_issuerKeys.getPublicKey(), _clientKeys.getPublicKey(), ROLE_CODES, _payload);
    }

    private void thenCerificateCanBeWrittenAsString() {
        assertThat(_certificate.toString(), containsString(_payload.toString()));
    }
}