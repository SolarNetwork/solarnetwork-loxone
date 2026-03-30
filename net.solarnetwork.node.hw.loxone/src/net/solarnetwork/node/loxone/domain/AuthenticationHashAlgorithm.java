/* ==================================================================
 * AuthenticationHashAlgorithm.java - 31/03/2026 9:13:08 am
 *
 * Copyright 2026 SolarNetwork.net Dev Team
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

/**
 * Enumeration of hash algorithms used for authentication.
 *
 * @author matt
 * @version 1.0
 */
public enum AuthenticationHashAlgorithm {

	/** The SHA-1 hash. */
	SHA1("SHA1"),

	/** The SHA-256 hash. */
	SHA256("SHA256"),

	;

	private String key;

	private AuthenticationHashAlgorithm(String key) {
		this.key = key;
	}

	/**
	 * Resolve an enumeration from a key value.
	 *
	 * @param value
	 *        the value to resolve
	 * @return the associated key value, or {@code SHA1} if the value is
	 *         {@code null} or not known
	 */
	public static AuthenticationHashAlgorithm forKey(String value) {
		AuthenticationHashAlgorithm result = AuthenticationHashAlgorithm.SHA1;
		if ( value != null ) {
			for ( AuthenticationHashAlgorithm e : AuthenticationHashAlgorithm.values() ) {
				if ( value.equalsIgnoreCase(e.key) ) {
					result = e;
					break;
				}
			}
		}
		return result;
	}

}
