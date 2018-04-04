/* ==================================================================
 * AuthenticationToken.java - 4/04/2018 2:01:44 PM
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

import java.util.Set;
import org.joda.time.DateTime;

/**
 * An authentication token.
 * 
 * @author matt
 * @version 1.0
 * @since 1.3
 */
public class AuthenticationToken {

	private final String token;
	private final DateTime validUntil;
	private final Set<AuthenticationTokenPermission> permissions;
	private final boolean passwordUnsecure;
	private final String key;

	/**
	 * Constructor.
	 * 
	 * @param token
	 *        the token value for authenticating with
	 * @param validUntil
	 *        the date the token is valid until
	 * @param permissions
	 *        the permissions granted with the token
	 * @param passwordUnsecure
	 *        {@literal true} if the password is considered unsecure and should
	 *        be changed
	 * @param key
	 *        the key to use for commands
	 */
	public AuthenticationToken(String token, DateTime validUntil,
			Set<AuthenticationTokenPermission> permissions, boolean passwordUnsecure, String key) {
		super();
		this.token = token;
		this.validUntil = validUntil;
		this.permissions = permissions;
		this.passwordUnsecure = passwordUnsecure;
		this.key = key;
	}

	/**
	 * Construct from epoch seconds and a permissions bitmask.
	 * 
	 * @param token
	 *        the token value for authenticating with
	 * @param validUntilSeconds
	 *        the valid until date, as Loxone epoch seconds
	 * @param permissionsBitmask
	 *        a bitmask of permission codes
	 * @param passwordUnsecure
	 *        {@literal true} if the password is considered unsecure and should
	 *        be changed
	 * @param key
	 *        the key to use for commands
	 */
	public AuthenticationToken(String token, long validUntilSeconds, int permissionsBitmask,
			boolean passwordUnsecure, String key) {
		this(token, dateForOffsetSeconds(validUntilSeconds),
				AuthenticationTokenPermission.permissionsForBitmask(permissionsBitmask),
				passwordUnsecure, key);
	}

	/**
	 * The Java timestamp in milliseconds for the Loxone epoch of 1 Jan 2009
	 * UTC.
	 */
	public static final long LOXONE_EPOCH = 1230768000000L;

	/**
	 * Convert a Loxone epoch of seconds since 1 Jan 2009 UTC into a date
	 * object.
	 * 
	 * @param seconds
	 *        the seconds
	 * @return the date
	 */
	public static final DateTime dateForOffsetSeconds(long seconds) {
		long ms = (seconds * 1000) + LOXONE_EPOCH;
		return new DateTime(ms);
	}

	public String getToken() {
		return token;
	}

	public DateTime getValidUntil() {
		return validUntil;
	}

	public Set<AuthenticationTokenPermission> getPermissions() {
		return permissions;
	}

	public boolean isPasswordUnsecure() {
		return passwordUnsecure;
	}

	public String getKey() {
		return key;
	}

}
