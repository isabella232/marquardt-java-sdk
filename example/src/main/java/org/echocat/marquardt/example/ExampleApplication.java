/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.example;

import org.echocat.marquardt.authority.session.ExpiryDateCalculator;
import org.echocat.marquardt.authority.session.ExpiryDateCalculatorImpl;
import org.echocat.marquardt.common.CertificateValidator;
import org.echocat.marquardt.common.domain.DeserializingFactory;
import org.echocat.marquardt.common.keyprovisioning.TrustedKeysProvider;
import org.echocat.marquardt.common.serialization.RolesDeserializer;
import org.echocat.marquardt.example.domain.ExampleRoles;
import org.echocat.marquardt.example.domain.PersistentUser;
import org.echocat.marquardt.example.domain.UserInfo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@Import(SecurityConfiguration.class)
public class ExampleApplication {

    public static void main(final String[] args) {
        SpringApplication.run(ExampleApplication.class, args);
    }

    @Bean
    public ExpiryDateCalculator<PersistentUser> expiryDateCalculator() {
        return new ExpiryDateCalculatorImpl<>();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CertificateValidator<UserInfo, ExampleRoles> clientSignedContentValidator(final TrustedKeysProvider keysProvider) {
        return new CertificateValidator<UserInfo, ExampleRoles>(keysProvider.getPublicKeys()) {
            @Override
            protected DeserializingFactory<UserInfo> deserializingFactory() {
                return UserInfo.FACTORY;
            }

            @Override
            protected RolesDeserializer<ExampleRoles> roleCodeDeserializer() {
                return ExampleRoles.FACTORY;
            }
        };
    }
}