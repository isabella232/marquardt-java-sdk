/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.common.domain;

import com.google.common.base.Charsets;
import org.echocat.marquardt.common.util.InputStreamUtils;

import javax.annotation.Nonnull;
import javax.annotation.WillNotClose;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ClientId {

    public static final int NUMBER_OF_CHARACTERS = 4;
    private String _clientId;

    public ClientId(String clientId) {
        if (clientId.length() != NUMBER_OF_CHARACTERS) {
            throw new IllegalArgumentException("client id must be 4 characters long");
        }
        _clientId = clientId;
    }

    public static ClientId readFrom(InputStream in) throws IOException {
        final byte[] bytes = InputStreamUtils.readBytes(in, NUMBER_OF_CHARACTERS);
        return new ClientId(new String(bytes));
    }

    public void writeTo(@Nonnull @WillNotClose final OutputStream out) throws IOException {
        out.write(_clientId.getBytes(Charsets.US_ASCII));
    }

    public String getClientId() {
        return _clientId;
    }

}
