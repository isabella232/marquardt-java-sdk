/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.common.util;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class InputStreamUtilsUnitTest {

    private static final byte[] FIFTY_BYTES = new byte[50];
    private static final int FIFTY_ONE = 51;
    private InputStream _inputStream;

    @Test(expected = EOFException.class)
    public void shouldThrowEofExceptionWhenReadingMoreBytesThenInStream() throws Exception {
        givenInputStream();
        whenReadingMoreBytesThenInStream();
    }

    private void whenReadingMoreBytesThenInStream() throws IOException {
        InputStreamUtils.readBytes(_inputStream, FIFTY_ONE);
    }

    private void givenInputStream() {
        _inputStream = new ByteArrayInputStream(FIFTY_BYTES);
    }
}