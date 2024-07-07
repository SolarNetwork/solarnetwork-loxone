/* ==================================================================
 * SecurityUtils.java - 5/04/2018 11:08:47 AM
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

package net.solarnetwork.node.loxone.util;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility helpers for Loxone security measures.
 *
 * @author matt
 * @version 1.0
 */
public final class SecurityUtils {

	private static final Logger log = LoggerFactory.getLogger(SecurityUtils.class);

	private SecurityUtils() {
		// not available
	}

	/**
	 * Parse a X.509 public certificate from a PEM encoded string.
	 *
	 * @param key
	 *        the public key string
	 * @return the key, or {@literal null} if cannot be parsed or {@code key} is
	 *         null or empty
	 */
	public static PublicKey parsePublicKey(String key) {
		if ( key == null || key.length() < 1 ) {
			return null;
		}
		try {
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			String keyString = key.replaceAll("(?i)-+(?:BEGIN|END) CERTIFICATE-+", "");
			byte[] keyData = Base64.getDecoder().decode(keyString);
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyData);
			return keyFactory.generatePublic(keySpec);
		} catch ( NoSuchAlgorithmException | InvalidKeySpecException e ) {
			log.warn("{} error parsing public key [{}]: {}", e.getClass().getSimpleName(), key,
					e.getMessage());
			return null;
		}
	}

}
