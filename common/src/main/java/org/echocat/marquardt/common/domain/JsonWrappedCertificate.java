/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.common.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Used to transport certificates via JSON.
 */
@SuppressWarnings("AssignmentToCollectionOrArrayFieldFromParameter")
public class JsonWrappedCertificate {

    private final byte[] _certificate;

    @JsonCreator
    public JsonWrappedCertificate(@JsonProperty("certificate") final byte[] certificate) {
        _certificate = certificate;
    }

    @JsonProperty("certificate")
    public byte[] getCertificate() {
        return _certificate;
    }
}
