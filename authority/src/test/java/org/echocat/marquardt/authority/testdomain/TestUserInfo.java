/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.authority.testdomain;

import org.echocat.marquardt.common.domain.Signable;

import javax.annotation.Nonnull;
import javax.annotation.WillNotClose;
import java.io.IOException;
import java.io.OutputStream;

public class TestUserInfo implements Signable {
    @Override
    public void writeTo(@Nonnull @WillNotClose OutputStream out) throws IOException {

    }

    @Override
    public byte[] getContent() throws IOException {
        return new byte[0];
    }
}
