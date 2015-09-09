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

/**
 * Utils class for InputStream reading. You may want to use this when you implement your own Signable's
 * DeserializationFactory.
 *
 * @see org.echocat.marquardt.common.domain.DeserializingFactory
 * @see org.echocat.marquardt.common.domain.CertificateFactory
 * @see org.echocat.marquardt.common.domain.PublicKeyWithMechanism
 *
 */
public final class InputStreamUtils {

    private static final InputStreamUtils INSTANCE = new InputStreamUtils();

    private InputStreamUtils(){}

    /**
     * Reads a fixed number of bytes from an input stream.
     *
     * @param inputStream The stream to read from.
     * @param numberOfBytes The number of bytes to read.
     * @return byte[] with numberOfBytes length and the content from the stream.
     * @throws IOException When a problem occurs while reading the stream, ie. the stream contains less bytes than given
     * numberOfBytes.
     */
    @Nonnull
    public static byte[] readBytes(@Nonnull @WillNotClose final InputStream inputStream, final int numberOfBytes) throws IOException {
       return INSTANCE.internalReadBytes(inputStream, numberOfBytes);
    }

    /**
     * Reads the next byte from the input stream.
     * @param inputStream The stream to read from.
     * @return The next byte from the input stream.
     * @throws IOException When the stream contains no more byte or is already closed.
     */
    public static byte readByte(@Nonnull @WillNotClose final InputStream inputStream) throws IOException {
        return INSTANCE.internalReadByte(inputStream);
    }

    /**
     * Reads a long value from the input stream.
     * @param inputStream The stream to read from.
     * @return Deserialized long value.
     * @throws IOException When the stream contains no long value at cursor position or is already closed.
     */
    public static long readLong(@Nonnull @WillNotClose final InputStream inputStream) throws IOException {
        return INSTANCE.internalReadLong(inputStream);
    }

    /**
     * Reads an int value from the input stream.
     * @param inputStream The stream to read from.
     * @return Deserialized int value.
     * @throws IOException When the stream contains no int value at cursor position or is already closed.
     */
    public static int readInt(@Nonnull @WillNotClose final InputStream inputStream) throws IOException {
        return INSTANCE.internalReadInt(inputStream);
    }

    private byte[] internalReadBytes(@Nonnull @WillNotClose final InputStream inputStream, final int numberOfBytes) throws IOException {
        final byte[] bytes = new byte[numberOfBytes];
        final int read = inputStream.read(bytes);
        if (read != numberOfBytes) {
            throw new EOFException();
        }
        return bytes;
    }

    private byte internalReadByte(@Nonnull @WillNotClose final InputStream inputStream) throws IOException {
        final byte[] bytes = readBytes(inputStream, 1);
        return bytes[0];
    }

    private long internalReadLong(@Nonnull @WillNotClose final InputStream inputStream) throws IOException {
        final byte[] bytes = readBytes(inputStream, Longs.BYTES);
        return Longs.fromByteArray(bytes);
    }

    public int internalReadInt(@Nonnull @WillNotClose final InputStream inputStream) throws IOException {
        final byte[] bytes = readBytes(inputStream, Ints.BYTES);
        return Ints.fromByteArray(bytes);
    }


}
