/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.common.domain;

import org.echocat.marquardt.common.ContentSigner;
import org.echocat.marquardt.common.ContentValidator;

import javax.annotation.Nonnull;
import javax.annotation.WillNotClose;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Both the certificate and the wrapped payload sent in the certificate must implement this interface. Basically,
 * implementing this enables an object to get signed and validated by the Signer and Validator.
 *
 * @see Certificate
 * @see ContentSigner
 * @see ContentValidator
 */
public interface Signable {

    /**
     * Serializes the object into an output stream.
     *
     * @param out OutputStream to serialize the object into.
     * @throws IOException When the output stream throws an IOException while writing.
     */
    void writeTo(@Nonnull @WillNotClose OutputStream out) throws IOException;

    /**
     * Serializes the object into a byte array.
     *
     * @return Bytes of the serialized signable.
     * @throws IOException
     */
    byte[] getContent() throws IOException;

}
