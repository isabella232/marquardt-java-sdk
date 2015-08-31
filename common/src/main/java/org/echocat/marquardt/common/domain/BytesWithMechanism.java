/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.common.domain;

import com.google.common.primitives.Ints;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Base64;

@SuppressWarnings("AssignmentToCollectionOrArrayFieldFromParameter")
public abstract class BytesWithMechanism<M extends BytesWithMechanism.Mechanism> {

    public interface Mechanism {
        @Nonnull
        String getName();

        byte getCode();
    }

    @Nonnull
    private final byte[] _value;
    @Nonnull
    private final M _mechanism;

    protected BytesWithMechanism(@Nonnull final M mechanism, @Nonnull final byte[] value) {
        _mechanism = mechanism;
        _value = value;
    }

    protected BytesWithMechanism(@Nonnull final byte[] content) {
        if (content.length < 1) {
            throw new IllegalArgumentException("Illegal format");
        }
        _mechanism = codeToMechanism(content[0]);
        _value = new byte[content.length - 1];
        System.arraycopy(content, 1, _value, 0, content.length - 1);
    }

    protected abstract M codeToMechanism(byte code);

    @Nonnull
    public byte[] getValue() {
        return _value;
    }

    @Nonnull
    public M getMechanism() {
        return _mechanism;
    }

    @Nonnull
    public byte[] getContent() {
        final byte[] value = getValue();
        final byte[] result = new byte[value.length + 1];
        result[0] = getMechanism().getCode();
        System.arraycopy(value, 0, result, 1, value.length);
        return result;
    }

    @Override
    public String toString() {
        return getMechanism() + ":" + Base64.getEncoder().withoutPadding().encodeToString(getValue());
    }

}
