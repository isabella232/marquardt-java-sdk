/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.common;

import com.google.common.primitives.Ints;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.echocat.marquardt.common.domain.DeserializingFactory;
import org.echocat.marquardt.common.domain.Signable;
import org.echocat.marquardt.common.util.InputStreamUtils;

import javax.annotation.Nonnull;
import javax.annotation.WillNotClose;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SignablePayload implements Signable {

    public static final DeserializingFactory<SignablePayload> FACTORY = (@Nonnull @WillNotClose InputStream in) -> {
        final int stringSize = InputStreamUtils.readInt(in);
        final byte[] payloadAsBytes = InputStreamUtils.readBytes(in, stringSize);
        return new SignablePayload(new String(payloadAsBytes));
    };

    private final String _someContent;

    public SignablePayload(final String someContent) {
        _someContent = someContent;
    }

    public String getSomeContent() {
        return _someContent;
    }

    @Override
    public void writeTo(@Nonnull @WillNotClose final OutputStream out) throws IOException {
        final byte[] bytes = _someContent.getBytes();
        out.write(Ints.toByteArray(bytes.length));
        out.write(bytes);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("_someContent", _someContent)
                .toString();
    }

}
