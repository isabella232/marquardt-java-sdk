/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.common.exceptions;

import org.echocat.marquardt.common.domain.certificate.Certificate;

/**
 * Exception intended to be thrown when a certificate is expired. Through the exception you may access the expired
 * certificate e. g. to refresh it.
 */
public class ExpiredCertificateException extends InvalidCertificateException {

    private Certificate<?> _certificate;

    public ExpiredCertificateException(final Certificate<?> certificate) {
        super("Certificate of " + certificate.getPayload() + " is expired");
        _certificate = certificate;
    }

    public Certificate<?> getCertificate() {
        return _certificate;
    }
}