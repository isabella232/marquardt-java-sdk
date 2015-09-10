/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.authority.domain;

import java.util.UUID;

/**
 * Represents a known user to the authority. Is created at sign up.
 *
 * Implement this in your Authority implementation with a domain object that can be stored into a database.
 */
public interface User {

    /**
     * Unique user id of the user.
     *
     * @return Unique user id of the user.
     */
    UUID getUserId();

    /**
     * Implement this to check if the credentials given on sign in match the password of the user that registers.
     *
     * @param password Password to check. Please do not log or persist this password.
     * @return True if the password matches, false otherwise.
     */
    boolean passwordMatches(String password);

    /**
     * Roles of the user masked with bitmask.
     *
     * @return Roles of the user to read with the bitmask.
     */
    long getRoles();
}
