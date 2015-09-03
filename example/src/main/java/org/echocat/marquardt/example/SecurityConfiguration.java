/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.example;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

//    @Autowired
//    private ClientSignedContentValidator<UserInfo> _clientSignedContentValidator;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeRequests()
//                .antMatchers("/fakeservice/someProtectedResource**").authenticated()
                .antMatchers("/**").permitAll();
    }

//    private CertificateAuthenticationFilter certificateAuthenticationFilter() {
//        return new CertificateAuthenticationFilter<UserInfo>(_clientSignedContentValidator) {
//            @Override
//            protected String getUserName(UserInfo userInfo) {
//                return userInfo.getUserId().toString();
//            }
//        };
//    }

}
