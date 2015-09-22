/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.example.domain;

import com.google.common.collect.Sets;
import org.echocat.marquardt.authority.domain.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import java.util.Set;
import java.util.UUID;

@Entity
public class PersistentUser implements User<ExampleRoles> {

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

    @ElementCollection(targetClass = ExampleRoles.class)
    @Enumerated(EnumType.STRING)
    private Set<ExampleRoles> roles;

    @SuppressWarnings("unused")
    public Long getId() {
        return id;
    }

    @SuppressWarnings("unused")
    public void setId(final Long id) {
        this.id = id;
    }

    public void setEmail(final String email) {
        this.email = email.toLowerCase();
    }

    @SuppressWarnings("unused")
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
    public boolean passwordMatches(final String password) {
        return _passwordEncoder.matches(password, getEncodedPassword());
    }

    public void setRoles(final Set<ExampleRoles> roles) {
        this.roles = Sets.newHashSet(roles);
    }

    @Override
    public Set<ExampleRoles> getRoles() {
        return roles;
    }

    public String getEncodedPassword() {
        return encodedPassword;
    }
}
