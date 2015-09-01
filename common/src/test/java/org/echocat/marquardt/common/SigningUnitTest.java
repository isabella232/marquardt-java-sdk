/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.common;

import org.echocat.marquardt.common.domain.Certificate;
import org.echocat.marquardt.common.domain.KeyPairProvider;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.security.PrivateKey;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;

public abstract class SigningUnitTest {

    protected static final long ROLE_CODES = 123L;
    protected static final String SOME_PAYLOAD = "Some payload";

    protected KeyPairProvider _issuerKeys;
    protected KeyPairProvider _clientKeys;
    private ContentSigner _contentSigner;

    protected Certificate<SignablePayload> _certificate;
    protected byte[] _signedPayload;

    @Before
    public void setup() {
        _issuerKeys = TestKeyPairProvider.create();
        _clientKeys = TestKeyPairProvider.create();
        _contentSigner = new ContentSigner();
    }


    protected void givenUserInfoCertificate() {
        final SignablePayload payload = new SignablePayload(SOME_PAYLOAD);
        _certificate = Certificate.create(_issuerKeys.getPublicKey(), _clientKeys.getPublicKey(), ROLE_CODES, payload);
    }

    protected void whenSigning() throws IOException {
        whenSigning(_issuerKeys.getPrivateKey());
    }

    protected void whenSigning(PrivateKey privateKey) throws IOException {
        _signedPayload = _contentSigner.sign(_certificate, privateKey);
    }

}