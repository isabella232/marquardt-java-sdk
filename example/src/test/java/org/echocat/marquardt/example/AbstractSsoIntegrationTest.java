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
import org.echocat.marquardt.common.domain.Certificate;
import org.echocat.marquardt.common.domain.KeyPairProvider;
import org.echocat.marquardt.common.domain.TrustedKeysProvider;
import org.echocat.marquardt.example.domain.User;
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
import java.util.UUID;

@IntegrationTest("server.port=0")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ExampleApplication.class)
@WebAppConfiguration
@ActiveProfiles("tests")
public abstract class AbstractSsoIntegrationTest {
    Certificate<UserInfo> _certificate;
    KeyPairProvider _clientKeyProvider = TestKeyPairProvider.create();

    @Autowired
    private TrustedKeysProvider _trustedKeysProvider;

    Signer _clientSigner = new Signer();
    @Autowired
    private UserRepository _userRepository;
    @Autowired
    private PersistentSessionStore _sessionStore;

    @Value("${local.server.port}")
    private String _port;
    private UserCredentials _userCredentials;

    protected Client<UserInfo> _client;
    protected boolean _signOutSuccessful;

    protected String baseUriOfApp() {
        return "http://127.0.0.1:" + _port;
    }

    void whenLoggingOut() throws IOException {
        _signOutSuccessful = _client.signout();
    }

    void whenSigningUp() throws IOException {
        _certificate = _client.signup(_userCredentials);
    }

    void whenSigningIn() throws IOException {
        _certificate = _client.signin(_userCredentials);
    }

    void givenCorrectCredentials() {
        _userCredentials = new UserCredentials("testuser@example.com", "Mutti123", _clientKeyProvider.getPublicKey());
    }

    void givenIncorrectCredentials() {
        _userCredentials = new UserCredentials("testuser@example.com", "Vati123", TestKeyPairProvider.create().getPublicKey());
    }

    @Before
    public void setup() {
        _client = new SpringClient<>(baseUriOfApp(), UserInfo.FACTORY, _clientKeyProvider.getPrivateKey(), _trustedKeysProvider.getPublicKeys());
    }

    @After
    public void cleanup() {
        _userRepository.deleteAll();
        _sessionStore.deleteAll();
    }

    void givenExistingUser() {
        final User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setEmail("testuser@example.com");
        user.setEncodedPassword("$2a$10$NPdMDuROCDzrzourXzI1eONBa21Xglg9IzuLc1kecWeG3w/DnQjT.");
        user.setRoles(123);
        _userRepository.save(user);
    }
}
