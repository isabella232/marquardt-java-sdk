/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.common.util;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import javax.annotation.Nonnull;
import javax.annotation.WillNotClose;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class InputStreamUtils {

    @Nonnull
    public static byte[] readBytes(@Nonnull @WillNotClose final InputStream inputStream, final int numberOfBytes) throws IOException {
        final byte[] bytes = new byte[numberOfBytes];
        final int read = inputStream.read(bytes);
        if (read != numberOfBytes) {
            throw new EOFException();
        }
        return bytes;
    }

    @Nonnull
    public static byte readByte(@Nonnull @WillNotClose final InputStream inputStream) throws IOException {
        final byte[] bytes = readBytes(inputStream, 1);
        return bytes[0];
    }

    @Nonnull
    public static long readLong(@Nonnull @WillNotClose final InputStream inputStream) throws IOException {
        final byte[] bytes = readBytes(inputStream, Longs.BYTES);
        return Longs.fromByteArray(bytes);
    }

    @Nonnull
    public static int readInt(@Nonnull @WillNotClose final InputStream inputStream) throws IOException {
        final byte[] bytes = readBytes(inputStream, Ints.BYTES);
        return Ints.fromByteArray(bytes);
    }
}
