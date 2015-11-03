/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.common;

import com.google.common.base.Charsets;
import org.echocat.marquardt.common.domain.ClientId;
import org.junit.Test;

import java.io.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ClientIdUnitTest {

    private static final String TEST_CLIENT_ID = "abcd";

    private InputStream _inputStream;
    private ClientId _clientId;

    private ByteArrayOutputStream _outputStream = new ByteArrayOutputStream();

    @Test
    public void shouldDeserializeClientIdFromBytes() throws IOException {
        givenAnInputStreamWithClientIdAsUtf8Bytes();
        whenReadingTheClientId();
        thenClientIdIsObtained();
    }

    @Test
    public void shouldSerializeClientIdToBytes() throws IOException {
        givenAClientId();
        givenAnOutputStream();
        whenWritingClientIdToOutputStream();
        thenClientIdBytesAreObtained();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowAnExceptionWhenClientIdHasIllegalLength() {
        _clientId = new ClientId("this client id is too long");
    }

    private void thenClientIdBytesAreObtained() {
        assertThat(_outputStream.toByteArray(), is(equalTo(TEST_CLIENT_ID.getBytes(Charsets.US_ASCII))));
    }


    private void givenAClientId() {
        _clientId = new ClientId(TEST_CLIENT_ID);
    }

    private void givenAnOutputStream() {
        _outputStream = new ByteArrayOutputStream(TEST_CLIENT_ID.length());
    }

    private void givenAnInputStreamWithClientIdAsUtf8Bytes() {
        _inputStream = new ByteArrayInputStream(TEST_CLIENT_ID.getBytes(Charsets.US_ASCII));
    }

    private void whenReadingTheClientId() throws IOException {
        _clientId = ClientId.readFrom(_inputStream);
    }


    private void whenWritingClientIdToOutputStream() throws IOException {
        _clientId.writeTo(_outputStream);
    }

    private void thenClientIdIsObtained() {
        assertThat(_clientId.getClientId(), is(equalTo(TEST_CLIENT_ID)));
    }

}
