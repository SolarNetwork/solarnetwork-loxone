/* ==================================================================
 * TokenSecurityHelper.java - 5/04/2018 11:36:47 AM
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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Map;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.solarnetwork.node.loxone.domain.AuthenticationKey;
import net.solarnetwork.node.loxone.domain.AuthenticationToken;
import net.solarnetwork.node.loxone.domain.ConfigApi;

/**
 * Helper class for token based authenticated and encrypted connections.
 * 
 * @author matt
 * @version 1.0
 * @since 1.3
 */
public class TokenSecurityHelper implements SecurityHelper {

	/** The default value of the {@code saltLength} property. */
	public static final int DEFAULT_SALT_LENGTH = 16;

	/** The default value of the {@code saltMaxAge} property. */
	public static final long DEFAULT_SALT_MAX_AGE = 60 * 60 * 1000L;

	/** The default value of the {@code saltMaxUse} property. */
	public static final int DEFAULT_SALT_MAX_USE_COUNT = 30;

	private static final Logger log = LoggerFactory.getLogger(TokenSecurityHelper.class);

	private static final int AES_IV_LENGTH_BYTES = 16;
	private static final String AES_CIPHER_NAME = "AES/CBC/PKCS5Padding";

	private final ConfigApi apiConfig;
	private int saltLength = DEFAULT_SALT_LENGTH;
	private long saltMaxAge = DEFAULT_SALT_MAX_AGE;
	private int saltMaxUse = DEFAULT_SALT_MAX_USE_COUNT;

	private SecretKey aesKey;
	private Cipher aesEncryptCipher;
	private Cipher aesDecryptCipher;
	private SecureRandom secureRandom;
	private String salt;
	private int saltUseCount;
	private long saltTimestamp;
	private boolean encryptionReady;
	private AuthenticationToken authToken;

	private final byte[] aesInitVector = new byte[AES_IV_LENGTH_BYTES];

	/**
	 * Constructor.
	 * 
	 * @param apiConfig
	 *        the API configuration to use
	 * @throws IllegalArgumentException
	 *         if {@code apiConfig} not provided
	 * @throws RuntimeException
	 *         if there is any problem initializing the encryption systems
	 */
	public TokenSecurityHelper(ConfigApi apiConfig) {
		super();
		if ( apiConfig == null ) {
			throw new IllegalArgumentException("ConfigApi must not be null");
		}
		this.apiConfig = apiConfig;
		initialize();
	}

	private boolean initialize() {
		try {
			// generate a random key for the session
			KeyGenerator aesKeyGen = KeyGenerator.getInstance("AES");
			aesKeyGen.init(256);
			aesKey = aesKeyGen.generateKey();

			secureRandom = new SecureRandom();
			secureRandom.nextBytes(aesInitVector);

			IvParameterSpec ivSpec = new IvParameterSpec(aesInitVector);

			aesEncryptCipher = Cipher.getInstance(AES_CIPHER_NAME);
			aesEncryptCipher.init(Cipher.ENCRYPT_MODE, aesKey, ivSpec);

			aesDecryptCipher = Cipher.getInstance(AES_CIPHER_NAME);
			aesDecryptCipher.init(Cipher.DECRYPT_MODE, aesKey, ivSpec);
		} catch ( InvalidParameterException | NoSuchAlgorithmException | InvalidKeyException
				| NoSuchPaddingException | InvalidAlgorithmParameterException e ) {
			throw new RuntimeException(
					e.getClass().getSimpleName() + " initializing AES encryption: " + e.getMessage());
		}
		return true;
	}

	@Override
	public String generateSessionKey() {
		try {
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.PUBLIC_KEY, apiConfig.getPublicKey());
			String key = Hex.encodeHexString(aesKey.getEncoded()) + ":"
					+ Hex.encodeHexString(aesInitVector);
			byte[] sessionKey = cipher.doFinal(key.getBytes());
			String result = java.util.Base64.getEncoder().encodeToString(sessionKey);
			log.debug("Generated Loxone [{}] session key [{}]", apiConfig.getWebsocketUri(), result);
			return result;
		} catch ( IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException
				| NoSuchPaddingException | InvalidKeyException e ) {
			throw new RuntimeException(e.getClass().getSimpleName() + " generating session key for ["
					+ apiConfig.getWebsocketUri() + "]:" + e.getMessage());

		}

	}

	private synchronized boolean isSaltExpired() {
		return (salt != null && ((saltTimestamp + saltMaxAge < System.currentTimeMillis())
				|| saltUseCount > saltMaxUse));
	}

	private String currSalt() {
		return salt;
	}

	private String generateSalt() {
		byte[] bytes = new byte[saltLength];
		secureRandom.nextBytes(bytes);
		String salt = Hex.encodeHexString(bytes);
		log.info("Generated salt for [{}]: {}", apiConfig.getWebsocketUri(), salt);
		return salt;
	}

	private synchronized String generateAndSaveSalt() {
		String newSalt = generateSalt();
		saltTimestamp = System.currentTimeMillis();
		saltUseCount = 0;
		salt = newSalt;
		return newSalt;
	}

	@Override
	public AuthenticationToken extractTokenValue(Map<String, Object> data) {
		if ( data != null ) {
			String token = data.containsKey("token") ? data.get("token").toString() : null;
			String key = data.containsKey("key") ? data.get("key").toString() : null;
			Number validUntil = data.containsKey("validUntil") ? (Number) data.get("validUntil") : null;
			Number tokenRights = data.containsKey("tokenRights") ? (Number) data.get("tokenRights")
					: null;
			Boolean unsecurePass = data.containsKey("unsecurePass") ? (Boolean) data.get("unsecurePass")
					: null;
			if ( token != null && key != null && validUntil != null && tokenRights != null
					&& unsecurePass != null ) {
				this.authToken = new AuthenticationToken(token, validUntil.longValue(),
						tokenRights.intValue(), unsecurePass, key);
			}
		}
		return this.authToken;
	}

	@Override
	public AuthenticationToken extractTokenRefreshValue(Map<String, Object> data) {
		AuthenticationToken token = this.authToken;
		if ( data != null && token != null ) {
			Number validUntil = data.containsKey("validUntil") ? (Number) data.get("validUntil") : null;
			Boolean unsecurePass = data.containsKey("unsecurePass") ? (Boolean) data.get("unsecurePass")
					: null;
			if ( validUntil != null && unsecurePass != null ) {
				this.authToken = token.refreshedCopy(validUntil.longValue(), unsecurePass);
			}
		}
		return this.authToken;
	}

	@Override
	public AuthenticationToken getAuthenticationToken() {
		return authToken;
	}

	@Override
	public void setAuthenticationToken(AuthenticationToken token) {
		this.authToken = token;
	}

	@Override
	public boolean hasValidToken() {
		return (authToken != null && !authToken.isExpired());
	}

	@Override
	public AuthenticationKey extractAuthenticationKey(Map<String, Object> data) {
		AuthenticationKey key = null;
		if ( data != null && data.containsKey("key") && data.containsKey("salt") ) {
			key = new AuthenticationKey(data.get("key").toString(), data.get("salt").toString());
		}
		return key;
	}

	@Override
	public void keyExchangeComplete() {
		encryptionReady = true;
	}

	private boolean isEncryptionReady() {
		return encryptionReady;
	}

	@Override
	public synchronized String encryptCommand(CommandType type, String command) {
		if ( !isEncryptionReady() || (type != null && !type.isEncryptionSupported()) ) {
			return command;
		}
		String strToEncrypt;
		if ( isSaltExpired() ) {
			String prevSalt = currSalt();
			String currSalt = generateAndSaveSalt();
			strToEncrypt = "nextSalt/" + prevSalt + "/" + currSalt + "/" + command + "\0";
		} else {
			String currSalt = currSalt();
			if ( currSalt == null ) {
				currSalt = generateAndSaveSalt();
			}
			strToEncrypt = "salt/" + currSalt + "/" + command + "\0";
		}
		try {
			String encrypted = null;
			try {
				encrypted = URLEncoder.encode(java.util.Base64.getEncoder().encodeToString(
						aesEncryptCipher.doFinal(strToEncrypt.getBytes("UTF-8"))), "UTF-8");
			} catch ( UnsupportedEncodingException e ) {
				// should never happen
			}
			if ( log.isTraceEnabled() ) {
				log.trace("Encrypted command [{}] as: {}", strToEncrypt, encrypted);
			}
			saltUseCount += 1;
			return CommandType.EncryptedCommand.getControlValue() + "/" + encrypted;
		} catch ( IllegalBlockSizeException | BadPaddingException e ) {
			log.warn("Loxone [{}] command [{}] encryption failed: {}", apiConfig.getWebsocketUri(),
					strToEncrypt, e.getMessage());
			return command;
		}
	}

	@Override
	public String decryptCommand(String encryptedCommand) {
		CommandType cmd = CommandType.forControlValue(encryptedCommand);
		String string = encryptedCommand;
		if ( cmd != CommandType.EncryptedCommand ) {
			return string;
		}
		string = string.substring(CommandType.EncryptedCommand.getControlValue().length() + 1);
		try {
			byte[] bytes = Base64.decodeBase64(string);
			bytes = aesDecryptCipher.doFinal(bytes);
			string = new String(bytes, "UTF-8");
			string = string.replaceAll("\0+.*$", "");
			string = string.replaceFirst("^salt/[^/]*/", "");
			string = string.replaceFirst("^nextSalt/[^/]+/[^/]+/", "");
			return string;
		} catch ( IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException e ) {
			log.warn("Loxone [{}] command [{}] decryption failed: {}", apiConfig.getWebsocketUri(),
					encryptedCommand, e.getMessage());
		}
		return string;
	}

	/**
	 * Get the configured encryption salt length.
	 * 
	 * @return the number of bytes of random encryption salt to use
	 */
	public int getSaltLength() {
		return saltLength;
	}

	/**
	 * Set the number of bytes of encryption salt to use.
	 * 
	 * <p>
	 * Changes to this property only affects salt generated in the future, after
	 * any existing salt expires.
	 * </p>
	 * 
	 * @param saltLength
	 *        the number of bytes of random encryption salt to use
	 * @throws IllegalArgumentException
	 *         if {@code saltLength} is less than 2 or greater than 64; defaults
	 *         to {@link #DEFAULT_SALT_LENGTH}
	 */
	public void setSaltLength(int saltLength) {
		if ( saltLength < 2 ) {
			throw new IllegalArgumentException("Salt length must be at least 2");
		} else if ( saltLength > 64 ) {
			throw new IllegalArgumentException("Salt length must be no more than 64");
		}
		this.saltLength = saltLength;
	}

	/**
	 * Get the maximum age of encryption salt, before new salt must be
	 * generated.
	 * 
	 * @return the maximum age, in milliseconds; defaults to
	 *         {@link #DEFAULT_SALT_MAX_AGE}
	 */
	public long getSaltMaxAge() {
		return saltMaxAge;
	}

	/**
	 * Set the maximum age of encryption salt, before new salt must be
	 * generated.
	 * 
	 * <p>
	 * Changes to this property only affects salt generated in the future, after
	 * any existing salt expires.
	 * </p>
	 * 
	 * @param saltMaxAge
	 *        the maximum age, in milliseconds
	 */
	public void setSaltMaxAge(long saltMaxAge) {
		this.saltMaxAge = saltMaxAge;
	}

	/**
	 * Get the maximum times the encryption salt may be used before new salt
	 * must be generated.
	 * 
	 * @return the maximum times a specific salt may be used; defaults to
	 *         {@link #DEFAULT_SALT_MAX_USE_COUNT}
	 */
	public int getSaltMaxUse() {
		return saltMaxUse;
	}

	/**
	 * Set the maximum times the encryption salt may be used before new salt
	 * must be generated.
	 * 
	 * <p>
	 * Changes to this property only affects salt generated in the future, after
	 * any existing salt expires.
	 * </p>
	 * 
	 * @param saltMaxUse
	 *        the maximum use count
	 */
	public void setSaltMaxUse(int saltMaxUse) {
		this.saltMaxUse = saltMaxUse;
	}

}
