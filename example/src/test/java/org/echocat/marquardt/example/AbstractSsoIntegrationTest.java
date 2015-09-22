/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.example;

import org.echocat.marquardt.client.Client;
import org.echocat.marquardt.client.spring.SpringClient;
import org.echocat.marquardt.common.Signer;
import org.echocat.marquardt.common.TestKeyPairProvider;
import org.echocat.marquardt.common.domain.Certificate;
import org.echocat.marquardt.common.domain.KeyPairProvider;
import org.echocat.marquardt.common.domain.TrustedKeysProvider;
import org.echocat.marquardt.common.serialization.RolesDeserializer;
import org.echocat.marquardt.example.domain.PersistentRoles;
import org.echocat.marquardt.example.domain.PersistentUser;
import org.echocat.marquardt.example.domain.UserCredentials;
import org.echocat.marquardt.example.domain.UserInfo;
import org.echocat.marquardt.example.persistence.PersistentSessionStore;
import org.echocat.marquardt.example.persistence.jpa.UserRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

@SuppressWarnings({"AbstractClassWithoutAbstractMethods", "SpringJavaAutowiredMembersInspection"})
@IntegrationTest("server.port=0")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ExampleApplication.class)
@WebAppConfiguration
@ActiveProfiles("tests")
public abstract class AbstractSsoIntegrationTest {

    private Certificate<UserInfo> _certificate;

    private final KeyPairProvider _clientKeyProvider = TestKeyPairProvider.create();
    private final Signer _clientSigner = new Signer();

    @Autowired
    private TrustedKeysProvider _trustedKeysProvider;
    @Autowired
    private UserRepository _userRepository;
    @Autowired
    private PersistentSessionStore _sessionStore;
    @Autowired
    private RolesDeserializer<PersistentRoles> _rolesDeserializer;

    @Value("${local.server.port}")
    private String _port;
    private UserCredentials _userCredentials;

    private Client<UserInfo> _client;

    protected String baseUriOfApp() {
        return "http://127.0.0.1:" + _port;
    }

    void whenLoggingOut() throws IOException {
        getClient().signout();
    }

    void whenSigningUp() throws IOException {
        setCertificate(getClient().signup(_userCredentials));
    }

    void whenSigningIn() throws IOException {
        setCertificate(getClient().signin(_userCredentials));
    }

    void givenCorrectCredentials() {
        _userCredentials = new UserCredentials("testuser@example.com", "Mutti123", getClientKeyProvider().getPublicKey());
    }

    void givenIncorrectCredentials() {
        _userCredentials = new UserCredentials("testuser@example.com", "Vati123", TestKeyPairProvider.create().getPublicKey());
    }

    @Before
    public void setup() {
        setClient(new SpringClient<>(baseUriOfApp(), UserInfo.FACTORY, _rolesDeserializer, getClientKeyProvider(), _trustedKeysProvider.getPublicKeys()));
    }

    @After
    public void cleanup() {
        _userRepository.deleteAll();
        _sessionStore.deleteAll();
    }

    void givenExistingUser() {
        final PersistentUser persistentUser = new PersistentUser();
        persistentUser.setUserId(UUID.randomUUID());
        persistentUser.setEmail("testuser@example.com");
        persistentUser.setEncodedPassword("$2a$10$NPdMDuROCDzrzourXzI1eONBa21Xglg9IzuLc1kecWeG3w/DnQjT.");
        persistentUser.setRoles(Collections.emptySet());
        _userRepository.save(persistentUser);
    }

    protected Certificate<UserInfo> getCertificate() {
        return _certificate;
    }

    protected void setCertificate(final Certificate<UserInfo> certificate) {
        _certificate = certificate;
    }

    protected KeyPairProvider getClientKeyProvider() {
        return _clientKeyProvider;
    }

    protected Signer getClientSigner() {
        return _clientSigner;
    }

    protected Client<UserInfo> getClient() {
        return _client;
    }

    protected void setClient(final Client<UserInfo> client) {
        _client = client;
    }

}
