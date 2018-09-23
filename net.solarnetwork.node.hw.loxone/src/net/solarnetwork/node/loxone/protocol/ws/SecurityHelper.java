/* ==================================================================
 * SecurityHelper.java - 5/04/2018 1:24:51 PM
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

import java.util.Map;
import net.solarnetwork.node.loxone.domain.AuthenticationKey;
import net.solarnetwork.node.loxone.domain.AuthenticationToken;

/**
 * API for security related functions.
 * 
 * @author matt
 * @version 1.0
 * @since 1.3
 */
public interface SecurityHelper {

	/**
	 * Generate a new session key value.
	 * 
	 * @return the session key, encoded as Base64
	 */
	String generateSessionKey();

	/**
	 * Test if a valid token is available on this helper.
	 * 
	 * @return {@literal true} if a valid token is available
	 */
	boolean hasValidToken();

	/**
	 * Signal to the helper that the encryption key exchange has completed
	 * successfully.
	 */
	public void keyExchangeComplete();

	/**
	 * Extract an authentication key from a command response.
	 * 
	 * @param data
	 *        the data to extract a key from, taken from the value of a
	 *        {@link CommandType#GetTokenKey} response
	 * @return the key, or {@literal null} if one cannot be extracted
	 */
	AuthenticationKey extractAuthenticationKey(Map<String, Object> data);

	/**
	 * Extract and save an authentication token from a command response.
	 * 
	 * <p>
	 * Once this method has been called successfully, the token will be saved
	 * and {@link SecurityHelper#hasValidToken()} will return {@literal true}
	 * until the token expires.
	 * </p>
	 * 
	 * @param data
	 *        the data to extract a token details from, taken from the value of
	 *        a {@link CommandType#GetToken} response
	 * @return the token, or {@literal null} if one cannot be extracted
	 */
	AuthenticationToken extractTokenValue(Map<String, Object> data);

	/**
	 * Extract and save refreshed authentication token data from a command
	 * response.
	 * 
	 * <p>
	 * Once this method has been called successfully, the token will be saved
	 * and {@link SecurityHelper#hasValidToken()} will return {@literal true}
	 * until the token expires.
	 * </p>
	 * 
	 * @param data
	 *        the data to extract token refresh details from, taken from the
	 *        value of a {@link CommandType#RefreshToken} response
	 * @return the token, or {@literal null} if one cannot be extracted
	 */
	AuthenticationToken extractTokenRefreshValue(Map<String, Object> data);

	/**
	 * Get the saved authentication token.
	 * 
	 * @return the saved token
	 */
	AuthenticationToken getAuthenticationToken();

	/**
	 * Configure an existing token.
	 * 
	 * <p>
	 * The configured token will be used for any future token authentication,
	 * until replaced or refreshed.
	 * </p>
	 * 
	 * @param token
	 *        the token to use
	 */
	void setAuthenticationToken(AuthenticationToken token);

	/**
	 * Encrypt a command.
	 * 
	 * <p>
	 * If encryption is not available yet (before
	 * {@link #extractAuthenticationKey(Map)} has been called) then this method
	 * will return {@code command} unaltered.
	 * </p>
	 * 
	 * @param type
	 *        the command type, or {@literal null} if no type
	 * @param command
	 *        the command to encrypt if possible
	 * @return the encrypted command, or {@code command} if encryption not
	 *         available
	 */
	String encryptCommand(CommandType type, String command);

	/**
	 * Decrypt a command.
	 * 
	 * <p>
	 * If {@code encryptedCommand} is not actually encrypted, this method will
	 * simply return that value unaltered.
	 * </p>
	 * 
	 * @param encryptedCommand
	 *        the potentially encrypted command to decrypt
	 * @return the decrypted command
	 */
	String decryptCommand(String encryptedCommand);

}
