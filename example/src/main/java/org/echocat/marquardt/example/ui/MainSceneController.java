/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.echocat.marquardt.example.ui;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import org.echocat.marquardt.client.spring.SpringClient;
import org.echocat.marquardt.common.domain.certificate.Certificate;
import org.echocat.marquardt.example.domain.ExampleRoles;
import org.echocat.marquardt.example.domain.UserCredentials;
import org.echocat.marquardt.example.domain.UserInfo;
import org.echocat.marquardt.example.keyprovisioning.KeyFileReadingKeyPairProvider;
import org.echocat.marquardt.example.keyprovisioning.KeyFileReadingTrustedKeysProvider;

public class MainSceneController {

    public static final String BASE_URI = "http://localhost:8080";

    public static final String PUBLIC_KEY_FILE = "keys/auth-public-key.der";
    public static final String PRIVATE_KEY_FILE = "keys/auth-private-key.der";

    public static final String SUCCESS = "success";
    public static final String FAILED = "failed";

    private final KeyFileReadingKeyPairProvider _clientKeyProvider;

    @FXML
    private TextField _signupEmailField;
    @FXML
    private TextField _signupPasswordField;
    @FXML
    private TextField _signupResponseField;

    @FXML
    private TextField _signinEmailField;
    @FXML
    private TextField _signinPasswordField;
    @FXML
    private TextField _signinResponseField;

    @FXML
    private TextField _refreshResponseField;

    @FXML
    private TextField _signoutResponseField;

    @FXML
    private TextField _clientPublicKeyField;
    @FXML
    private TextField _clientPrivateKeyField;

    @FXML
    private TextField _certificateIssuerPublicKeyField;
    @FXML
    private TextField _certificateClientPublicKeyField;
    @FXML
    private TextField _certificateRoleCodesField;
    @FXML
    private TextField _certificateRoleExpiresAtField;

    @FXML
    private TextField _payloadUuidField;

    private SpringClient<UserInfo, ExampleRoles> _client;

    public MainSceneController() {
        _clientKeyProvider = new KeyFileReadingKeyPairProvider(PUBLIC_KEY_FILE, PRIVATE_KEY_FILE);
        KeyFileReadingTrustedKeysProvider trustedKeysProvider = new KeyFileReadingTrustedKeysProvider(PUBLIC_KEY_FILE);
        _client = new SpringClient<>(BASE_URI, UserInfo.FACTORY, ExampleRoles.FACTORY, _clientKeyProvider, trustedKeysProvider.getPublicKeys());
    }

    @FXML
    private void initialize() {
        _clientPublicKeyField.setText(_clientKeyProvider.getPublicKey().toString());
        _clientPrivateKeyField.setText(_clientKeyProvider.getPrivateKey().toString());
    }

    @FXML
    public void signupButtonClicked() {
        UserCredentials credentials = new UserCredentials(_signupEmailField.getText(), _signupPasswordField.getText(), _clientKeyProvider.getPublicKey());
        try {
            Certificate<UserInfo> certificate = _client.signup(credentials);
            _signupResponseField.setText(SUCCESS);
            renderCertificate(certificate);
        } catch (Exception e) {
            _signupResponseField.setText(e.getMessage());
        }
    }

    @FXML
    public void signinButtonClicked() {
        UserCredentials credentials = new UserCredentials(_signinEmailField.getText(), _signinPasswordField.getText(), _clientKeyProvider.getPublicKey());
        try {
            Certificate<UserInfo> certificate = _client.signin(credentials);
            _signinResponseField.setText(SUCCESS);
            renderCertificate(certificate);
        } catch (Exception e) {
            _signinResponseField.setText(e.getMessage());
        }
    }

    @FXML
    public void refreshButtonClicked() {
        try {
            Certificate<UserInfo> certificate = _client.refresh();
            _refreshResponseField.setText(SUCCESS);
            renderCertificate(certificate);
        } catch (Exception e) {
            _refreshResponseField.setText(e.getMessage());
        }
    }

    @FXML
    public void signoutButtonClicked() {
        try {
            boolean success = _client.signout();
            if (success) {
                _signoutResponseField.setText(SUCCESS);
            } else {
                _signoutResponseField.setText(FAILED);
            }
            clearCertificate();
        } catch (Exception e) {
            _signoutResponseField.setText(e.getMessage());
        }
    }

    private void renderCertificate(Certificate<UserInfo> certificate) {
        _certificateIssuerPublicKeyField.setText(certificate.getIssuerPublicKey().toString());
        _certificateClientPublicKeyField.setText(certificate.getClientPublicKey().toString());
        _certificateRoleCodesField.setText(certificate.getRoles().toString());
        _certificateRoleExpiresAtField.setText(certificate.getExpiresAt().toString());
        _payloadUuidField.setText(certificate.getPayload().getUserId().toString());
    }

    private void clearCertificate() {
        _certificateIssuerPublicKeyField.setText("");
        _certificateClientPublicKeyField.setText("");
        _certificateRoleCodesField.setText("");
        _certificateRoleExpiresAtField.setText("");
        _payloadUuidField.setText("");
    }
}
