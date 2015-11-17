/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.example;

import org.echocat.marquardt.authority.Authority;
import org.echocat.marquardt.authority.persistence.SessionStore;
import org.echocat.marquardt.authority.persistence.UserCatalog;
import org.echocat.marquardt.authority.persistence.UserCreator;
import org.echocat.marquardt.authority.policies.ClientAccessPolicy;
import org.echocat.marquardt.authority.policies.SessionCreationPolicy;
import org.echocat.marquardt.authority.session.ExpiryDateCalculator;
import org.echocat.marquardt.authority.session.ExpiryDateCalculatorImpl;
import org.echocat.marquardt.authority.session.SessionCreator;
import org.echocat.marquardt.authority.session.SessionRenewal;
import org.echocat.marquardt.common.CertificateValidator;
import org.echocat.marquardt.common.domain.DeserializingFactory;
import org.echocat.marquardt.common.keyprovisioning.KeyPairProvider;
import org.echocat.marquardt.common.keyprovisioning.TrustedKeysProvider;
import org.echocat.marquardt.common.serialization.RolesDeserializer;
import org.echocat.marquardt.example.domain.CustomSignUpAccountData;
import org.echocat.marquardt.example.domain.ExampleRoles;
import org.echocat.marquardt.example.domain.PersistentSession;
import org.echocat.marquardt.example.domain.PersistentUser;
import org.echocat.marquardt.example.domain.UserCredentials;
import org.echocat.marquardt.example.domain.UserInfo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

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
    public SessionCreator<PersistentUser, PersistentSession> sessionCreator(
                                                        final SessionStore<PersistentSession> sessionStore,
                                                        final UserCatalog<PersistentUser> userCatalog,
                                                        final ExpiryDateCalculator<PersistentUser> expiryDateCalculator,
                                                        final KeyPairProvider issuerKeyProvider,
                                                        final Optional<SessionCreationPolicy> sessionCreationPolicy) {
        final SessionCreator<PersistentUser, PersistentSession> result = new SessionCreator<>(sessionStore, userCatalog, expiryDateCalculator, issuerKeyProvider);
        sessionCreationPolicy.ifPresent(result::setSessionCreationPolicy);
        return result;
    }

    @Bean
    public SessionRenewal<PersistentUser, PersistentSession> sessionRenewal(
                                                        final SessionStore<PersistentSession> sessionStore,
                                                        final UserCatalog<PersistentUser> userCatalog,
                                                        final ExpiryDateCalculator<PersistentUser> expiryDateCalculator,
                                                        final KeyPairProvider issuerKeyProvider) {
        return new SessionRenewal<>(sessionStore, userCatalog, expiryDateCalculator, issuerKeyProvider);
    }

    @Bean
    public Authority<PersistentUser, PersistentSession, UserCredentials, CustomSignUpAccountData> authority(
                                        final UserCatalog<PersistentUser> userCatalog,
                                        final UserCreator<PersistentUser, UserCredentials, CustomSignUpAccountData> userCreator,
                                        final SessionCreator<PersistentUser, PersistentSession> sessionCreator,
                                        final SessionRenewal<PersistentUser, PersistentSession> sessionRenewal,
                                        final SessionStore<PersistentSession> sessionStore,
                                        final ClientAccessPolicy clientAccessPolicy) {
        return new Authority<>(userCatalog, userCreator, sessionCreator, sessionRenewal, sessionStore, clientAccessPolicy);
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