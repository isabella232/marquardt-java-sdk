/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.common.domain;

import org.echocat.marquardt.common.domain.Signable;

import javax.annotation.Nonnull;
import javax.annotation.WillNotClose;
import java.io.IOException;
import java.io.InputStream;

/**
 * Factory capable of deserializing a specific Signable.
 *
 * @param <T> Signable this factory can deserialize.
 */
public interface DeserializingFactory<T extends Signable> {

    /**
     * Deserializes Signable from bytes of an input stream.
     *
     * @param in Input stream to read.
     * @return A deserialized Signable.
     * @throws IOException That happens while reading input stream.
     */
    @Nonnull
    T consume(@Nonnull @WillNotClose InputStream in) throws IOException;

}
