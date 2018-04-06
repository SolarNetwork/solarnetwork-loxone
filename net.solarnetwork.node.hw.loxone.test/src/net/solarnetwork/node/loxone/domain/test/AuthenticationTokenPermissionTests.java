/* ==================================================================
 * AuthenticationTokenPermissionTests.java - 6/04/2018 9:01:03 AM
 * 
 * Copyright 2018 SolarNetwork.net Dev Team
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License as 
 * published by the Free Software Foundation; either version 2 of 
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 
 * 02111-1307 USA
 * ==================================================================
 */

package net.solarnetwork.node.loxone.domain.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import java.util.EnumSet;
import java.util.Set;
import org.junit.Test;
import net.solarnetwork.node.loxone.domain.AuthenticationTokenPermission;

/**
 * Test cases for the {@link AuthenticationTokenPermission} class.
 * 
 * @author matt
 * @version 1.0
 */
public class AuthenticationTokenPermissionTests {

	@Test
	public void bitmaskForPermission() {
		int result = AuthenticationTokenPermission
				.bitmaskForPermissions(EnumSet.of(AuthenticationTokenPermission.App));
		assertThat("Mask", result, equalTo(4));
	}

	@Test
	public void bitmaskForPermissions() {
		int result = AuthenticationTokenPermission.bitmaskForPermissions(
				EnumSet.of(AuthenticationTokenPermission.App, AuthenticationTokenPermission.Web));
		assertThat("Mask", result, equalTo(6));
	}

	@Test
	public void permissionForBitmask() {
		Set<AuthenticationTokenPermission> perms = AuthenticationTokenPermission
				.permissionsForBitmask(4);
		assertThat("Set", perms, containsInAnyOrder(AuthenticationTokenPermission.App));
	}

	@Test
	public void permissionsForBitmask() {
		Set<AuthenticationTokenPermission> perms = AuthenticationTokenPermission
				.permissionsForBitmask(6);
		assertThat("Set", perms, containsInAnyOrder(AuthenticationTokenPermission.App,
				AuthenticationTokenPermission.Web));
	}

	@Test
	public void permissionsForBitmask1026() {
		Set<AuthenticationTokenPermission> perms = AuthenticationTokenPermission
				.permissionsForBitmask(1026);
		assertThat("Set", perms, containsInAnyOrder(AuthenticationTokenPermission.Web));
	}

	@Test
	public void permissionsForBitmask1028() {
		Set<AuthenticationTokenPermission> perms = AuthenticationTokenPermission
				.permissionsForBitmask(1028);
		assertThat("Set", perms, containsInAnyOrder(AuthenticationTokenPermission.App));
	}

}
