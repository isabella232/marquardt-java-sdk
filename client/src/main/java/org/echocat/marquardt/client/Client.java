/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.client;

import org.echocat.marquardt.common.domain.ClientInformation;
import org.echocat.marquardt.common.domain.Credentials;
import org.echocat.marquardt.common.domain.SignUpAccountData;
import org.echocat.marquardt.common.domain.Signable;
import org.echocat.marquardt.common.domain.certificate.Certificate;

import java.io.IOException;
import java.util.Locale;

/**
 * Interface of the client to perform authentication with the authority and to call service APIs.
 *
 * @param <T> type of the payload contained in the certificate.
 */
public interface Client<T extends Signable> {

    /**
     * Start of the 2 step sign-up process. In the first step an empty user will be created and stored.
     *
     * @param clientInformation consisting of client public key and id.
     *
     * @throws IOException
     */
    Certificate<T> initializeSignUp(final ClientInformation clientInformation) throws IOException;


    /**
     * Finalize 2 step sign-up process. Requires an existing session and user. The submitted {@link SignUpAccountData}
     * are used to enrich the existing user.
     *
     * @throws IOException
     */
    Certificate<T> finalizeSignUp(final Certificate<T> certificate, final SignUpAccountData<? extends Credentials> signUpAccountData) throws IOException;

    /**
     * Sign in to the authority with the provided user credentials. This will return a certificate if the
     * credentials are accepted by the authority.
     *
     * @throws IOException
     */
    Certificate<T> signIn(final Credentials credentials) throws IOException;

    /**
     * Sign out the user by deleting the current session. This will return true if sign out was successful.
     *
     * @throws IOException
     */
    boolean signOut(final Certificate<T> certificate) throws IOException;

    /**
     * Refresh the current session by obtaining a new certificate. This will return a new certificate if a valid
     * certificate is provided. Refreshing the certificate is also possible if the certificate is expired.
     *
     * @throws IOException
     */
    Certificate<T> refresh(final Certificate<T> certificateToRefresh) throws IOException;

    /**
     * Call a protected service API endpoint by using the certificate obtained earlier (either by signing in or signing up).
     */
    <REQUEST, RESPONSE> RESPONSE sendSignedPayloadTo(final String url,
                                                     final String httpMethod,
                                                     final REQUEST payload,
                                                     final Class<RESPONSE> responseType,
                                                     final Certificate<T> certificate) throws IOException;

    /**
     * Lets you define the locale used by the client. Defaults to Locale.getDefault().
     */
    void setLocale(final Locale locale);
}
