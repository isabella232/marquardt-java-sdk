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

public abstract class CertificateFactory<T extends Signable> implements DeserializingFactory<Certificate<T>> {

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
        return new Certificate<>(publicKeyWithMechanism.toJavaKey(), clientKeyWithMechanism.toJavaKey(), expiryDate, roleCodes, wrapped);
    }

}
