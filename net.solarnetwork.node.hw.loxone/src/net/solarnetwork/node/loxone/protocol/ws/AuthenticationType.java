/* ==================================================================
 * AuthenticationType.java - 4/04/2018 3:21:27 PM
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

package net.solarnetwork.node.loxone.protocol.ws;

/**
 * The authentication style to use with Loxone.
 * 
 * @author matt
 * @version 1.0
 * @since 1.3
 */
public enum AuthenticationType {

	/**
	 * Choose token authentication if available, falling back to password
	 * otherwise.
	 */
	Auto(0),

	/** Use password based authentication. */
	Password(1),

	/**
	 * Use token-based authentication along with command encryption (available
	 * in Loxone 9.0+).
	 */
	Token(2);

	private int code;

	private AuthenticationType(int code) {
		this.code = code;
	}

	/**
	 * Get a code value for this enum.
	 * 
	 * @return the code
	 */
	public int getCode() {
		return code;
	}

	/**
	 * Get an enum instance for a code value.
	 * 
	 * @param code
	 *        the code value to get the enum for
	 * @return the enum
	 * @throws IllegalArgumentException
	 *         if {@code code} is not supported
	 */
	public static AuthenticationType forCode(int code) {
		for ( AuthenticationType t : AuthenticationType.values() ) {
			if ( code == t.code ) {
				return t;
			}
		}
		throw new IllegalArgumentException("Unknown code: " + code);
	}

}
