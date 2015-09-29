/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.example.domain;

import com.google.common.primitives.Longs;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.echocat.marquardt.common.domain.DeserializingFactory;
import org.echocat.marquardt.common.domain.Signable;
import org.echocat.marquardt.common.util.InputStreamUtils;

import javax.annotation.Nonnull;
import javax.annotation.WillNotClose;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class UserInfo implements Signable {

    public static final DeserializingFactory<UserInfo> FACTORY = (@Nonnull @WillNotClose InputStream in) -> {
        final byte versionFromInput = InputStreamUtils.readByte(in);
        if (versionFromInput != UserInfo.VERSION) {
            throw new IllegalArgumentException("Expected UserInfo with version '" + UserInfo.VERSION + "' but received '" + versionFromInput + "'");
        }
        final long mostSigBits = InputStreamUtils.readLong(in);
        final long leastSigBits = InputStreamUtils.readLong(in);
        final UUID userId = new UUID(mostSigBits, leastSigBits);
        return new UserInfo(userId);
    };

    private static final byte VERSION = 1;

    private final UUID _userId;

    public UserInfo(final UUID userId) {
        _userId = userId;
    }

    public UUID getUserId() {
        return _userId;
    }

    @Override
    public void writeTo(@Nonnull @WillNotClose final OutputStream out) throws IOException {
        out.write(VERSION);
        out.write(Longs.toByteArray(_userId.getMostSignificantBits()));
        out.write(Longs.toByteArray(_userId.getLeastSignificantBits()));
    }

    @Override
    public byte[] getContent() throws IOException {
        return new byte[0];
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("_userId", _userId)
                .toString();
    }
}
