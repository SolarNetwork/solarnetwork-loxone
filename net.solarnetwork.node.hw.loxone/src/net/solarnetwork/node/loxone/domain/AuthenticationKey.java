/* ==================================================================
 * AuthenticationKey.java - 5/04/2018 2:30:50 PM
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

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.HmacUtils;

/**
 * An authentication key.
 *
 * @author matt
 * @version 1.0
 * @since 1.3
 */
public class AuthenticationKey {

	private final byte[] key;
	private final String saltHex;

	/**
	 * Constructor.
	 *
	 * @param keyHex
	 *        the key value, in hex
	 * @param saltHex
	 *        the salt value, in hex
	 */
	public AuthenticationKey(String keyHex, String saltHex) {
		super();
		try {
			this.key = Hex.decodeHex(keyHex.toCharArray());
		} catch ( DecoderException e ) {
			throw new RuntimeException(e);
		}
		this.saltHex = saltHex;
	}

	/**
	 * Encode a username and password for use with token authentication.
	 *
	 * @param username
	 *        the username
	 * @param password
	 *        the password
	 * @return the hashed token value to authenticate with
	 */
	@SuppressWarnings("deprecation")
	public String hash(String username, String password) {
		String pwHash = DigestUtils.sha1Hex(password + ":" + saltHex).toUpperCase();
		String authString = username + ":" + pwHash;
		return HmacUtils.hmacSha1Hex(this.key, authString.getBytes()).toUpperCase();
	}

	/**
	 * Get the key.
	 *
	 * @return the key
	 */
	public byte[] getKey() {
		return key;
	}

	/**
	 * Get the salt.
	 *
	 * @return the salt
	 */
	public String getSaltHex() {
		return saltHex;
	}

}
