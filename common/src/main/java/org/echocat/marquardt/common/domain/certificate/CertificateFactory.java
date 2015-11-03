/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.common.domain.certificate;

import org.echocat.marquardt.common.domain.ClientId;
import org.echocat.marquardt.common.domain.PublicKeyWithMechanism;
import org.echocat.marquardt.common.domain.Signable;
import org.echocat.marquardt.common.domain.DeserializingFactory;
import org.echocat.marquardt.common.serialization.RolesDeserializer;

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
 * @param <SIGNABLE> Class of wrapped payload, for example additional user information to use on clients and services.
 * @param <ROLE> Class of your role implementation.
 */
public abstract class CertificateFactory<SIGNABLE extends Signable, ROLE extends Role> implements DeserializingFactory<Certificate<SIGNABLE>> {

    /**
     * Provides the factory to deserialize the wrapped content.
     *
     * @return Factory capable of deserializing wrapped content.
     *
     * @see DeserializingFactory
     */
    protected abstract DeserializingFactory<SIGNABLE> getFactoryOfWrapped();

    /**
     * Provides the roles deserializer for your roles implementation.
     *
     * @return RolesDeserializer capable to produce your roles.
     *
     * @see RolesDeserializer
     */
    protected abstract RolesDeserializer<ROLE> getRolesDeserializer();

    @Nonnull
    @Override
    public Certificate<SIGNABLE> consume(@Nonnull @WillNotClose final InputStream in) throws IOException {
        final byte versionFromInput = readByte(in);
        if (versionFromInput != Certificate.VERSION) {
            throw new IllegalArgumentException("Expected Certificate with version '" + Certificate.VERSION + "' but received '" + versionFromInput + "'");
        }
        final PublicKeyWithMechanism publicKeyWithMechanism = PublicKeyWithMechanism.readFrom(in);
        final PublicKeyWithMechanism clientKeyWithMechanism = PublicKeyWithMechanism.readFrom(in);
        final String clientId = ClientId.readFrom(in).getClientId();
        //noinspection UseOfObsoleteDateTimeApi
        final Date expiryDate = new Date(readLong(in));
        final long roleCodes = readLong(in);
        final SIGNABLE wrapped = getFactoryOfWrapped().consume(in);
        return new Certificate<>(publicKeyWithMechanism.toJavaKey(), clientKeyWithMechanism.toJavaKey(), clientId, expiryDate, getRolesDeserializer().from(roleCodes), wrapped);
    }

}
