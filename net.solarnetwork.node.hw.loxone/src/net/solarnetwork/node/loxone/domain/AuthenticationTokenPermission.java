/* ==================================================================
 * AuthenticationTokenPermission.java - 4/04/2018 1:58:23 PM
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

package net.solarnetwork.node.loxone.domain;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/**
 * Authentication token permission values.
 * 
 * @author matt
 * @version 1.0
 * @since 1.3
 */
public enum AuthenticationTokenPermission {

	Web(2),

	App(4);

	private final int code;

	private AuthenticationTokenPermission(int code) {
		this.code = code;
	}

	/**
	 * Get the code associated with this permission.
	 * 
	 * @return the code
	 */
	public int getCode() {
		return code;
	}

	/**
	 * Get an enum for a code value.
	 * 
	 * @param code
	 *        the code to get the enum for
	 * @return the enum
	 * @throws IllegalArgumentException
	 *         if {@code code} is not supported
	 */
	public static AuthenticationTokenPermission forCode(int code) {
		for ( AuthenticationTokenPermission p : AuthenticationTokenPermission.values() ) {
			if ( code == p.code ) {
				return p;
			}
		}
		throw new IllegalArgumentException("Unknown code: " + code);
	}

	/**
	 * Get a set of permissions out of a bitmask of permission codes.
	 * 
	 * @param bitmask
	 *        the bitmask of permission codes
	 * @return the set of permissions, never {@literal null}
	 */
	public static Set<AuthenticationTokenPermission> permissionsForBitmask(int bitmask) {
		Set<AuthenticationTokenPermission> perms = new HashSet<>(4);
		for ( AuthenticationTokenPermission p : AuthenticationTokenPermission.values() ) {
			int bit = 1 << p.getCode();
			if ( (bitmask & bit) == bit ) {
				perms.add(p);
			}
		}
		return (perms.isEmpty() ? Collections.emptySet() : EnumSet.copyOf(perms));
	}

	/**
	 * Get a bitmask out of a set of permissions.
	 * 
	 * @param the
	 *        set of permissions
	 * @return the bitmask of permission codes
	 */
	public static int bitmaskForPermissions(Set<AuthenticationTokenPermission> permissions) {
		int mask = 0;
		if ( permissions != null ) {
			for ( AuthenticationTokenPermission p : permissions ) {
				mask |= (1 << p.getCode());
			}
		}
		return mask;
	}

}
