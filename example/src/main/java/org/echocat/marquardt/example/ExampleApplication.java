/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.example;

import org.echocat.marquardt.common.CertificateValidator;
import org.echocat.marquardt.common.domain.DeserializingFactory;
import org.echocat.marquardt.common.domain.TrustedKeysProvider;
import org.echocat.marquardt.common.serialization.RolesDeserializer;
import org.echocat.marquardt.example.domain.PersistentRoles;
import org.echocat.marquardt.example.domain.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@Import({SecurityConfiguration.class})
public class ExampleApplication {

    public static void main(final String[] args) {
        SpringApplication.run(ExampleApplication.class, args);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    RolesDeserializer<PersistentRoles> rolesSerializer() {
        return new RolesDeserializer<PersistentRoles>() {
            @Override
            public PersistentRoles createRoleFromId(final Number id) {
                return PersistentRoles.fromId(id.intValue());
            }
        };
    }

    @Bean
    @Autowired
    public CertificateValidator<UserInfo, PersistentRoles> clientSignedContentValidator(final TrustedKeysProvider keysProvider) {
        return new CertificateValidator<UserInfo, PersistentRoles>(keysProvider.getPublicKeys()) {
            @Override
            protected DeserializingFactory<UserInfo> deserializingFactory() {
                return UserInfo.FACTORY;
            }

            @Override
            protected RolesDeserializer<PersistentRoles> roleCodeDeserializer() {
                return rolesSerializer();
            }
        };
    }

}