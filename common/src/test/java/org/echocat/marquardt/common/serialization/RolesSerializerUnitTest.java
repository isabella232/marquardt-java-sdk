/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.common.serialization;

import org.echocat.marquardt.common.domain.certificate.Role;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.echocat.marquardt.common.TestRoles.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class RolesSerializerUnitTest {

    private Set<Role> _roles = new HashSet<Role>();
    private long _roleCode;

    @Test
    public void shouldCreateRoleCodeFromRole() throws Exception {
        givenFirstRole();
        whenCreatingRoleCode();
        thenRoleCodeForFirstRoleIsGenerated();
    }

    @Test
    public void shouldCreateRoleCodeFromEmptyRoles() throws Exception{
        whenCreatingRoleCode();
        thenRoleCodeForNoRoleIsGenerated();
    }

    @Test
    public void shouldCreateRoleCodeFromRoles() throws Exception {
        givenFirstRole();
        givenSecondRole();
        whenCreatingRoleCode();
        thenRoleCodeForBothRolesIsGenerated();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenRolesSetIsNull() throws Exception {
        givenNullRoles();
        whenCreatingRoleCode();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenRoleHasAnIdLessThanZero() throws Exception {
        givenRoleWithNegativeId();
        whenCreatingRoleCode();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenRoleHasAnIdGreaterThanSixtyThree() throws Exception {
        givenRoleWithIdGreaterThanSixtyThree();
        whenCreatingRoleCode();
    }

    private void givenRoleWithNegativeId() {
        _roles.add(ROLE_WITH_NEGATIVE_ID);
    }

    private void givenRoleWithIdGreaterThanSixtyThree() {
        _roles.add(ROLE_WITH_ID_GREATER_THAN_63);
    }

    private void givenNullRoles() {
        _roles = null;
    }

    private void givenFirstRole() {
        _roles.add(TEST_ROLE_1);
    }

    private void givenSecondRole() {
        _roles.add(TEST_ROLE_2);
    }

    private void whenCreatingRoleCode() {
        _roleCode = RolesSerializer.from(_roles);
    }

    private void thenRoleCodeForFirstRoleIsGenerated() {
        assertThat(_roleCode, is(1L));
    }

    private void thenRoleCodeForNoRoleIsGenerated() {
        assertThat(_roleCode, is(0L));
    }

    private void thenRoleCodeForBothRolesIsGenerated() {
        assertThat(_roleCode, is(3L));
    }

}