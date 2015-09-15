/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.example;


import org.echocat.marquardt.common.domain.Certificate;
import org.echocat.marquardt.example.domain.UserInfo;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.UUID;

import static org.apache.commons.codec.binary.Base64.encodeBase64;

public class ServiceLoginIntegrationTest extends AbstractSsoIntegrationTest {

    private byte[] _selfSignedCertificate;
    private String _payloadToSign;

    @Test
    public void shouldLoginAtServiceWithValidCertificate() throws Exception {
        givenSignedInUser();
        whenAccessingProtectedResourceOnService();
    }

    @Test(expected = HttpClientErrorException.class)
    public void shouldNotLoginAtServiceWithInvalidCertificate() throws Exception {
        givenSelfSignedCertificate();
        whenAccessingProtectedResourceWithSelfSignedCertificate(_selfSignedCertificate);
    }

    @Test
    public void shouldAcceptSignedContent() throws Exception {
        givenSignedInUser();
        givenContentToSign();
        whenSignedContentIsSent();
    }

    @Test
    public void shouldAcceptEmptySignedContent() throws Exception {
        givenSignedInUser();
        givenEmptyContentToSign();
        whenSignedContentIsSent();
    }

    private void givenSelfSignedCertificate() throws IOException {
        final UserInfo userInfo = new UserInfo(UUID.randomUUID());
        final Certificate<UserInfo> certificate = Certificate.create(_clientKeyProvider.getPublicKey(), _clientKeyProvider.getPublicKey(), 666L, userInfo);
        final byte[] selfSignedCertificate = _clientSigner.sign(certificate, _clientKeyProvider.getPrivateKey());
        _selfSignedCertificate = encodeBase64(selfSignedCertificate);
    }

    private void givenSignedInUser() throws IOException {
        givenExistingUser();
        givenCorrectCredentials();
        whenSigningIn();
    }

    private void givenContentToSign() {
        _payloadToSign = "Some content to sign";
    }

    private void givenEmptyContentToSign() {
        _payloadToSign = "";
    }

    private void whenAccessingProtectedResourceOnService() {
        _client.sendSignedPayloadTo(baseUriOfApp() + "/exampleservice/someProtectedResource", HttpMethod.POST.name(), null, Void.class);
    }

    private void whenAccessingProtectedResourceWithSelfSignedCertificate(byte[] attackersCertificate) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Certificate", new String(attackersCertificate));
        HttpEntity<Object> requestEntity = new HttpEntity<>(headers);

        restTemplate.exchange(baseUriOfApp() + "/exampleservice/someProtectedResource", HttpMethod.POST, requestEntity, Void.class);
    }

    private void whenSignedContentIsSent() {
        _client.sendSignedPayloadTo(baseUriOfApp() + "/exampleservice/someProtectedResourceWithPayload", HttpMethod.POST.name(), _payloadToSign, String.class);
    }
}
