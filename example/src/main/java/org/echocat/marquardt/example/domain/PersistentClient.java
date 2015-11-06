/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.example.domain;

import org.echocat.marquardt.authority.domain.Client;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class PersistentClient implements Client {

    @Id
    private String id;
    private boolean allowed;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(final String id) {
        this.id = id;
    }

    @Override
    public boolean isAllowed() {
        return allowed;
    }

    @Override
    public void setAllowed(final boolean allowed) {
        this.allowed = allowed;
    }
}