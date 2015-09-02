/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.client.util;

import org.echocat.marquardt.common.exceptions.SecurityMechanismException;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class Md5CreatorUnitTest {

    private static final String SOME_CONTENT = "SOME_CONTENT";
    private byte[] _bytesToHash;
    private byte[] _md5Hash;

    @Test
    public void shouldCreateMd5OfContent() throws Exception {
        givenSomeContent();
        whenCreatingMd5OfContent();
        thenCorrectMd5HashIsCreated();
    }

    @Test(expected = SecurityMechanismException.class)
    public void shouldThrowSecurityMechanismExceptionWhenAlgorithmIsUnknown() throws Exception {
        Md5Creator.INSTANCE.getMessageDigest("UNKNOWN");
    }

    private void givenSomeContent() {
        _bytesToHash = SOME_CONTENT.getBytes();
    }

    private void whenCreatingMd5OfContent() {
        _md5Hash = Md5Creator.create(_bytesToHash);
    }

    private void thenCorrectMd5HashIsCreated() {
        assertThat(byteArrayToHex(_md5Hash), is("14206a65f912327ae47940979bc0e270"));
    }

    private static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for(byte b: a)
            sb.append(String.format("%02x", b & 0xff));
        return sb.toString();
    }
}