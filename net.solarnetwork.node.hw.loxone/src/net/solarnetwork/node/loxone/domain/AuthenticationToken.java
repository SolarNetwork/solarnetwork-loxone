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
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.HmacUtils;
import org.joda.time.DateTime;
import net.solarnetwork.node.loxone.protocol.ws.CommandType;

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
	private final byte[] key;

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
	 * @param keyHex
	 *        the hex-encoded key to use for commands
	 */
	public AuthenticationToken(String token, DateTime validUntil,
			Set<AuthenticationTokenPermission> permissions, boolean passwordUnsecure, String keyHex) {
		super();
		this.token = token;
		this.validUntil = validUntil;
		this.permissions = permissions;
		this.passwordUnsecure = passwordUnsecure;
		try {
			this.key = keyHex != null ? Hex.decodeHex(keyHex.toCharArray()) : null;
		} catch ( DecoderException e ) {
			throw new RuntimeException(e);
		}
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
	 * Create a new instance as a copy of this with updated properties from a
	 * refresh operation.
	 * 
	 * @param validUntilSeconds
	 *        the valid until date, as Loxone epoch seconds
	 * @param passwordUnsecure
	 *        {@literal true} if the password is considered unsecure and should
	 *        be changed
	 * @return the new instance
	 */
	public AuthenticationToken refreshedCopy(long validUntilSeconds, boolean passwordUnsecure) {
		return new AuthenticationToken(token, validUntilSeconds, getPermissionsBitmask(),
				passwordUnsecure, getKeyHex());
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

	/**
	 * Encode a username for use with token authentication.
	 * 
	 * @param username
	 *        the username
	 * @return the hashed token value to authenticate with
	 */
	@SuppressWarnings("deprecation")
	public String hash(String username) {
		String authString = username + ":" + token;
		return HmacUtils.hmacSha1Hex(this.key, authString.getBytes()).toUpperCase();
	}

	/**
	 * Encode this token for use with token refresh.
	 * 
	 * @param key
	 *        the key to use, which is the result of a
	 *        {@link CommandType#GetAuthenticationKey} request
	 * @return the hashed token value to refresh with
	 */
	@SuppressWarnings("deprecation")
	public String hashToken(byte[] key) {
		String authString = token;
		return HmacUtils.hmacSha1Hex(key, authString.getBytes()).toUpperCase();
	}

	/**
	 * Test if the token has expired.
	 * 
	 * @return {@literal true} if the token is not valid right now
	 */
	public boolean isExpired() {
		return validUntil == null || validUntil.isBeforeNow();
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

	/**
	 * Get the permissions as a bitmask.
	 * 
	 * @return the permissions bitmask
	 */
	public int getPermissionsBitmask() {
		return AuthenticationTokenPermission.bitmaskForPermissions(permissions);
	}

	public boolean isPasswordUnsecure() {
		return passwordUnsecure;
	}

	public String getKeyHex() {
		return new String(Hex.encodeHex(key, false));
	}

}
