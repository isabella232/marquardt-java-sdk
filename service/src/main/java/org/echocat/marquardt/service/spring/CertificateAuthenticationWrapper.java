/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.service.spring;

import com.google.common.collect.Lists;
import org.echocat.marquardt.common.domain.Certificate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;

/**
 * Wrapper for Certificates to create a Spring Security authentication.
 */
public class CertificateAuthenticationWrapper implements Authentication {

    private final String _identifier;
    private final Certificate<?> _certificate;

    public CertificateAuthenticationWrapper(final String identifier, final Certificate<?> certificate) {
        _identifier = identifier;
        _certificate = certificate;
    }

    @Override
    public String getName() {
        return _identifier;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        //TODO: transform roles from userInfo to Spring granted authorities.
        return Lists.newArrayList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public Object getCredentials() {
        return _certificate;
    }

    @Override
    public Object getDetails() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return this;
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public void setAuthenticated(final boolean isAuthenticated) throws IllegalArgumentException {
        throw new IllegalArgumentException("Authentication change is immutable and disabled by design.");
    }
}
