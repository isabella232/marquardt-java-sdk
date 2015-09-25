/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.common.serialization;

import org.echocat.marquardt.common.TestRoles;
import org.echocat.marquardt.common.domain.certificate.Role;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;

public class RolesDeserializerUnitTest {

    private final RolesDeserializer<TestRoles> _rolesDeserializer = new RolesDeserializer<TestRoles>() {
        @Override
        public TestRoles createRoleFromId(final Number id) {
            return TestRoles.fromId(id.intValue());
        }
    };

    private Set<Role> _roles = new HashSet<Role>();
    private Long _roleCode;

    @Test
    public void shouldCreateRolesFromRoleCodes() throws Exception {
        givenRoleCode(3L);
        whenCreatingRoles();
        thenRolesAreGenerated();
    }

    @Test
    public void shouldCreateEmptyRolesSetFromRoleCode() throws Exception {
        givenRoleCode(0L);
        whenCreatingRoles();
        thenEmptyRolesSetIsDeserialized();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenRoleCodeIsNull() throws Exception {
        givenRoleCode(null);
        whenCreatingRoles();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenRoleCodeIsGreaterThanSupported() throws Exception {
        givenRoleCode(Long.MAX_VALUE);
        whenCreatingRoles();
    }

    private void givenRoleCode(final Long roleCode) {
        _roleCode = roleCode;
    }

    private void whenCreatingRoles() {
        _roles = _rolesDeserializer.from(_roleCode);
    }


    private void thenRolesAreGenerated() {
        assertThat(_roles.size(), is(2));
        assertThat(_roles, allOf(hasItem(TestRoles.TEST_ROLE_1), hasItem(TestRoles.TEST_ROLE_2)));
    }


    private void thenEmptyRolesSetIsDeserialized() {
        assertThat(_roles.size(), is(0));
    }

}