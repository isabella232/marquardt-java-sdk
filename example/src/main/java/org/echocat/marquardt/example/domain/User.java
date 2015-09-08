/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.example.domain;

import org.echocat.marquardt.authority.domain.Principal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.UUID;

@Entity
public class User implements Principal {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Transient
    private final PasswordEncoder _passwordEncoder = new BCryptPasswordEncoder();

    @NotNull
    private UUID userId;

    @NotNull
    private String email;

    @NotNull
    private String encodedPassword;

    private long roles;

    private Date created;

    private Date modified;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public void setEmail(final String email) {
        this.email = email.toLowerCase();
    }

    public String getEmail() {
        return email;
    }

    public void setUserId(final UUID userId) {
        this.userId = userId;
    }

    @Override
    public UUID getUserId() {
        return userId;
    }

    public void setEncodedPassword(final String encodedPassword) {
        this.encodedPassword = encodedPassword;
    }

    @Override
    public String getEncodedPassword() {
        return encodedPassword;
    }

    @Override
    public boolean passwordMatches(String password) {
        return _passwordEncoder.matches(password, getEncodedPassword());
    }

    public void setRoles(final long roles) {
        this.roles = roles;
    }

    @Override
    public long getRoles() {
        return roles;
    }
}
