/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.service;


import org.echocat.marquardt.authority.testdomain.TestUserInfo;
import org.echocat.marquardt.common.CertificateValidator;
import org.echocat.marquardt.common.domain.Certificate;
import org.echocat.marquardt.common.exceptions.InvalidCertificateException;
import org.echocat.marquardt.common.web.RequestValidator;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Base64;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CertificateAuthenticationFilterUnitTest {

    private MockHttpServletRequest _httpServletRequest = new MockHttpServletRequest();
    private MockHttpServletResponse _httpServletResponse = new MockHttpServletResponse();

    @Mock
    private Certificate<TestUserInfo> _certificate;

    @Mock
    private CertificateValidator<TestUserInfo> _certificateValidator;

    @Mock
    private RequestValidator _requestValidator;

    @Mock
    private FilterChain _filterChain;

    @Mock
    private FilterConfig _filterConfig;

    private TestCertificateAuthenticationFilter _testCertificateAuthenticationFilter;

    @Before
    public void setUp() {
        _testCertificateAuthenticationFilter = new TestCertificateAuthenticationFilter(_certificateValidator, _requestValidator);
        when(_certificate.getPayload()).thenReturn(new TestUserInfo());
    }

    @Test
    public void shouldAuthenticateUserWithValidCertificate() throws IOException, ServletException {
        givenValidRequest();
        whenFilterIsExecuted();
        thenUserIsAuthenticated();
        thenDoFilterIsCalled();
    }

    @Test
    public void shouldNotAuthenticateUserWithoutCertificate() throws IOException, ServletException {
        givenRequestWithoutCertificate();
        whenFilterIsExecuted();
        thenUserIsNotAuthenticated();
        thenDoFilterIsCalled();
    }

    @Test(expected = InvalidCertificateException.class)
    public void shouldNotAuthenticateUserWithInvalidCertificate() throws IOException, ServletException {
        givenRequestWithInvalidCertificate();
        whenFilterIsExecuted();
        thenUserIsNotAuthenticated();
        thenDoFilterIsCalled();
    }

    @Test
    public void shouldNotAuthenticateUserWithInvalidSignature() throws IOException, ServletException {
        givenRequestWithInvalidSignature();
        whenFilterIsExecuted();
        thenUserIsNotAuthenticated();
        thenDoFilterIsCalled();
    }

    @Test
    public void shouldNotChangeFilterConfig() throws ServletException {
        whenFilterIsInitialized();
        thenFilterConfigIsUnchanged();
    }

    @After
    public void tearDown() {
        _testCertificateAuthenticationFilter.destroy();
    }

    private void thenFilterConfigIsUnchanged() {
        verifyZeroInteractions(_filterConfig);
    }

    private void whenFilterIsInitialized() throws ServletException {
        _testCertificateAuthenticationFilter.init(_filterConfig);
    }

    private void givenValidRequest() {
        String encodedCertificate = Base64.getEncoder().encodeToString("CERTIFICATE".getBytes());
        _httpServletRequest.addHeader("X-Certificate", encodedCertificate);
        when(_certificateValidator.deserializeAndValidateCertificate(argThat(byteArrayEqualTo("CERTIFICATE")))).thenReturn(_certificate);
        when(_requestValidator.isValid(_httpServletRequest, null)).thenReturn(true);
    }

    private void givenRequestWithoutCertificate() {
        when(_certificateValidator.deserializeAndValidateCertificate(argThat(byteArrayEqualTo("CERTIFICATE")))).thenReturn(_certificate);
    }

    private void givenRequestWithInvalidCertificate() {
        String encodedCertificate = Base64.getEncoder().encodeToString("CERTIFICATE".getBytes());
        _httpServletRequest.addHeader("X-Certificate", encodedCertificate);
        when(_certificateValidator.deserializeAndValidateCertificate(argThat(byteArrayEqualTo("CERTIFICATE")))).thenThrow(InvalidCertificateException.class);
    }

    private void givenRequestWithInvalidSignature() {
        String encodedCertificate = Base64.getEncoder().encodeToString("CERTIFICATE".getBytes());
        _httpServletRequest.addHeader("X-Certificate", encodedCertificate);
        when(_certificateValidator.deserializeAndValidateCertificate(argThat(byteArrayEqualTo("CERTIFICATE")))).thenReturn(_certificate);
        when(_requestValidator.isValid(_httpServletRequest, null)).thenReturn(false);
    }


    private void whenFilterIsExecuted() throws IOException, ServletException {
        _testCertificateAuthenticationFilter.doFilter(_httpServletRequest, _httpServletResponse, _filterChain);
    }

    private void thenUserIsAuthenticated() {
        assertThat(_testCertificateAuthenticationFilter.isAuthenticated(), is(true));
    }

    private void thenUserIsNotAuthenticated() {
        assertThat(_testCertificateAuthenticationFilter.isAuthenticated(), is(false));
    }

    private void thenDoFilterIsCalled() throws IOException, ServletException {
        verify(_filterChain).doFilter(_httpServletRequest, _httpServletResponse);
    }

    class TestCertificateAuthenticationFilter extends CertificateAuthenticationFilter<TestUserInfo> {

        private boolean _authenticated = false;

        public TestCertificateAuthenticationFilter(CertificateValidator<TestUserInfo> certificateValidator, RequestValidator requestValidator) {
            super(certificateValidator, requestValidator);
        }


        @Override
        protected void authenticateUser(Certificate<TestUserInfo> certificate) {
            _authenticated = true;
        }

        public boolean isAuthenticated() {
            return _authenticated;
        }

    }

    private Matcher<byte[]> byteArrayEqualTo(String certificate) {
        return new FeatureMatcher<byte[], String>(equalTo(certificate), "bytes equal to ", "bytes") {
            @Override
            protected String featureValueOf(byte[] bytes) {
                return new String(bytes);
            }
        };
    }


}