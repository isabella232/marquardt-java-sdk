/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.example;

import org.echocat.marquardt.common.CertificateValidator;
import org.echocat.marquardt.example.domain.ExampleRoles;
import org.echocat.marquardt.example.domain.UserInfo;
import org.echocat.marquardt.service.spring.SpringSecurityCertificateAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private CertificateValidator<UserInfo, ExampleRoles> _certificateValidator;

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeRequests()
                .antMatchers("/exampleservice/adminResource").hasRole("ADMIN")
                .antMatchers("/exampleservice/someProtectedResource**").authenticated()
                .antMatchers("/**").permitAll()
                .and()
                .addFilterBefore(certificateAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
    }

    private SpringSecurityCertificateAuthenticationFilter<UserInfo,ExampleRoles> certificateAuthenticationFilter() {
        return new SpringSecurityCertificateAuthenticationFilter<UserInfo,ExampleRoles>(_certificateValidator) {
            @Override
            protected String getIdentifier(final UserInfo signable) {
                return signable.getUserId().toString();
            }
        };
    }

}
