/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.example;

import org.echocat.marquardt.authority.domain.UserStatus;
import org.echocat.marquardt.authority.session.SessionCreator;
import org.echocat.marquardt.client.okhttp.GsonUserCredentials;
import org.echocat.marquardt.client.okhttp.MarquardtClient;
import org.echocat.marquardt.common.Signer;
import org.echocat.marquardt.common.TestKeyPairProvider;
import org.echocat.marquardt.common.domain.DeserializingFactory;
import org.echocat.marquardt.common.domain.certificate.Certificate;
import org.echocat.marquardt.common.domain.certificate.CertificateFactory;
import org.echocat.marquardt.common.keyprovisioning.KeyPairProvider;
import org.echocat.marquardt.common.keyprovisioning.TrustedKeysProvider;
import org.echocat.marquardt.common.serialization.RolesDeserializer;
import org.echocat.marquardt.example.domain.Client;
import org.echocat.marquardt.example.domain.ExampleRoles;
import org.echocat.marquardt.example.domain.PersistentSession;
import org.echocat.marquardt.example.domain.PersistentUser;
import org.echocat.marquardt.example.domain.UserInfo;
import org.echocat.marquardt.example.persistence.PersistentSessionStore;
import org.echocat.marquardt.example.persistence.jpa.ClientRepository;
import org.echocat.marquardt.example.persistence.jpa.UserRepository;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings({"AbstractClassWithoutAbstractMethods", "SpringJavaAutowiredMembersInspection", "unchecked"})
@IntegrationTest("server.port=0")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ExampleApplication.class)
@WebAppConfiguration
@ActiveProfiles("tests")
public abstract class AbstractSsoIntegrationTest {

    protected static final String TEST_CLIENT_ID = "asdf";

    protected Certificate<UserInfo> _certificate;

    private final KeyPairProvider _clientKeyProvider = TestKeyPairProvider.create();
    private final Signer _clientSigner = new Signer();

    @Autowired
    private TrustedKeysProvider _trustedKeysProvider;
    @Autowired
    private UserRepository _userRepository;
    @Autowired
    private PersistentSessionStore _sessionStore;
    @Autowired
    private ClientRepository _clientRepository;
    @Autowired
    private SessionCreator<PersistentUser, PersistentSession> _sessionCreator;

    private CertificateFactory<UserInfo, ExampleRoles> _certificateFactory = new CertificateFactory<UserInfo, ExampleRoles>() {
        @Override
        protected DeserializingFactory<UserInfo> getFactoryOfWrapped() {
            return UserInfo.FACTORY;
        }

        @Override
        protected RolesDeserializer<ExampleRoles> getRolesDeserializer() {
            return ExampleRoles.FACTORY;
        }
    };

    @Value("${local.server.port}")
    private String _port;
    private GsonUserCredentials _userCredentials;
    protected GsonSignUpAccountData _signUpAccountData;

    private org.echocat.marquardt.client.Client _client;

    protected String baseUriOfApp() {
        return "http://127.0.0.1:" + _port;
    }

    void whenLoggingOut() throws IOException {
        getClient().signOut(_certificate);
    }

    void whenFinalizingSignUp() throws IOException {
        setCertificate(getClient().finalizeSignUp(getCertificate(), _signUpAccountData));
    }

    void whenInitializingSignUp() throws IOException {
        setCertificate(getClient().initializeSignUp(_userCredentials));
    }

    void whenSigningIn() throws IOException {
        setCertificate(getClient().signIn(_userCredentials));
    }

    void givenCorrectCredentials() {
        _userCredentials = new GsonUserCredentials("testuser@example.com", "Mutti123", getClientKeyProvider().getPublicKey(), TEST_CLIENT_ID);
    }

    void givenIncorrectCredentials() {
        _userCredentials = new GsonUserCredentials("testuser@example.com", "Vati123", TestKeyPairProvider.create().getPublicKey(), TEST_CLIENT_ID);
    }

    void givenAccountDataWithCredentials() {
        givenCorrectCredentials();
        _signUpAccountData = new GsonSignUpAccountData(_userCredentials, "Bon", "Scott");
    }

    @Before
    public void setup() {
        setClient(new MarquardtClient<>(baseUriOfApp(), UserInfo.FACTORY, ExampleRoles.FACTORY, getClientKeyProvider(), _trustedKeysProvider.getPublicKeys()));
    }

    @Before
    public void purgeEntities() {
        _userRepository.deleteAll();
        _sessionStore.deleteAll();
        _clientRepository.deleteAll();
    }

    void givenClientIdIsAllowed() {
        final Client entry = new Client();
        entry.setId(TEST_CLIENT_ID);
        entry.setAllowed(true);
        _clientRepository.save(entry);
    }

    void givenProhibitedClientId() {
        final Client entry = new Client();
        entry.setId(TEST_CLIENT_ID);
        entry.setAllowed(false);
        _clientRepository.save(entry);
    }

    void givenEmptyUserAndSession() {
        final PersistentUser user = new PersistentUser();
        user.setUserId(UUID.randomUUID());
        user.setEncodedPassword("$2a$10$NPdMDuROCDzrzourXzI1eONBa21Xglg9IzuLc1kecWeG3w/DnQjT.");
        user.setRoles(Collections.<ExampleRoles>emptySet());
        user.setStatus(UserStatus.WITHOUT_CREDENTIALS);
        _userRepository.save(user);
        givenAccountDataWithCredentials();
        final byte[] createdCertificate = _sessionCreator.createCertificateAndSession(_userCredentials, user);
        try (final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(createdCertificate)) {
            _certificate = _certificateFactory.consume(byteArrayInputStream);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        _certificate.setSignedCertificateBytes(createdCertificate);
    }

    void givenExistingUser(final Set<ExampleRoles> roles) {
        final PersistentUser user = new PersistentUser();
        user.setUserId(UUID.randomUUID());
        user.setEmail("testuser@example.com");
        user.setEncodedPassword("$2a$10$NPdMDuROCDzrzourXzI1eONBa21Xglg9IzuLc1kecWeG3w/DnQjT.");
        user.setRoles(roles);
        user.setStatus(UserStatus.CONFIRMED);
        _userRepository.save(user);
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

    protected org.echocat.marquardt.client.Client getClient() {
        return _client;
    }

    protected void setClient(final org.echocat.marquardt.client.Client client) {
        _client = client;
    }

    protected UserRepository getUserRepository() {
        return _userRepository;
    }
}