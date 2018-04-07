/* ==================================================================
 * ConfigAuthenticationToken.java - 6/04/2018 8:38:05 AM
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
 * An {@link AuthenticationToken} associated with a {@link Config}.
 * 
 * @author matt
 * @version 1.0
 * @since 1.3
 */
public class ConfigAuthenticationToken extends AuthenticationToken {

	private final Long configId;

	/**
	 * Copy constructor.
	 * 
	 * @param configId
	 *        the Config ID
	 * @param authToken
	 *        the token data to copy
	 * @throws IllegalArgumentException
	 *         if {@code configId} is null
	 */
	public ConfigAuthenticationToken(Long configId, AuthenticationToken authToken) {
		this(configId, authToken.getToken(), authToken.getValidUntil(), authToken.getPermissions(),
				authToken.isPasswordUnsecure(), authToken.getKeyHex());
	}

	/**
	 * Constructor.
	 * 
	 * @param configId
	 *        the Config ID
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
	 * @throws IllegalArgumentException
	 *         if {@code configId} is null
	 */
	public ConfigAuthenticationToken(Long configId, String token, DateTime validUntil,
			Set<AuthenticationTokenPermission> permissions, boolean passwordUnsecure, String keyHex) {
		super(token, validUntil, permissions, passwordUnsecure, keyHex);
		if ( configId == null ) {
			throw new IllegalArgumentException("The configId argument may not be null");
		}
		this.configId = configId;
	}

	/**
	 * Construct from Loxone properties.
	 * 
	 * @param configId
	 *        the Config ID
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
	 * @throws IllegalArgumentException
	 *         if {@code configId} is null
	 */
	public ConfigAuthenticationToken(Long configId, String token, long validUntilSeconds,
			int permissionsBitmask, boolean passwordUnsecure, String key) {
		super(token, validUntilSeconds, permissionsBitmask, passwordUnsecure, key);
		if ( configId == null ) {
			throw new IllegalArgumentException("The configId argument may not be null");
		}
		this.configId = configId;
	}

	/**
	 * Get the {@link Config} ID.
	 * 
	 * @return the ID
	 */
	public Long getConfigId() {
		return configId;
	}

}
