/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.common.domain;

import javax.annotation.Nonnull;
import javax.annotation.WillNotClose;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import static org.echocat.marquardt.common.util.InputStreamUtils.readByte;
import static org.echocat.marquardt.common.util.InputStreamUtils.readLong;

/**
 * Extend this to enable deserialization of your wrapped Signable.
 *
 * Used to deserialize Certificates from Bytes.
 *
 * @param <T> Class of wrapped payload, for example additional user information to use on clents and services.
 */
public abstract class CertificateFactory<T extends Signable> implements DeserializingFactory<Certificate<T>> {

    /**
     * Provides the factory to deserialize the wrapped content.
     *
     * @return Factory capable of deserializing wrapped content.
     *
     * @see DeserializingFactory
     */
    protected abstract DeserializingFactory<T> getFactoryOfWrapped();

    @Nonnull
    @Override
    public Certificate<T> consume(@Nonnull @WillNotClose final InputStream in) throws IOException {
        final byte versionFromInput = readByte(in);
        if (versionFromInput != Certificate.VERSION) {
            throw new IllegalArgumentException("Expected Certificate with version '" + Certificate.VERSION + "' but received '" + versionFromInput + "'");
        }
        final PublicKeyWithMechanism publicKeyWithMechanism = PublicKeyWithMechanism.readFrom(in);
        final PublicKeyWithMechanism clientKeyWithMechanism = PublicKeyWithMechanism.readFrom(in);
        final Date expiryDate = new Date(readLong(in));
        final long roleCodes = readLong(in);
        final T wrapped = getFactoryOfWrapped().consume(in);
        return new Certificate<T>(publicKeyWithMechanism.toJavaKey(), clientKeyWithMechanism.toJavaKey(), expiryDate, roleCodes, wrapped);
    }

}
