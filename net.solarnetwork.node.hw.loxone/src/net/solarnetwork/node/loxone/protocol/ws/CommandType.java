/* ==================================================================
 * CommandType.java - 17/09/2016 2:05:32 PM
 * 
 * Copyright 2007-2016 SolarNetwork.net Dev Team
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Supported commands.
 * 
 * @author matt
 * @version 1.2
 */
public enum CommandType {

	/** Get the key to use for authenticating the connection. */
	GetAuthenticationKey("jdev/sys/getkey"),

	/** Authenticate the connection. */
	Authenticate("authenticate", "^authenticate/.*", false),

	/** Authenticate response. */
	Auth("Auth"),

	/**
	 * Perform a session key exchange.
	 * 
	 * @since 1.2
	 */
	KeyExchange("jdev/sys/keyexchange", "^j?dev/sys/keyexchange/.*"),

	/**
	 * Get the key to use for authenticating the connection via a token.
	 * 
	 * @since 1.2
	 */
	GetTokenKey("jdev/sys/getkey2", "^j?dev/sys/getkey2/.*"),

	/**
	 * Get an authentication token.
	 * 
	 * @since 1.2
	 */
	GetToken("jdev/sys/gettoken", "^j?dev/sys/gettoken/.*"),

	/**
	 * Authenticate the connection with a token.
	 * 
	 * @since 1.2
	 */
	AuthenticateWithToken("authwithtoken", "^authwithtoken/.*"),

	/**
	 * Refresh an authentication token.
	 * 
	 * @since 1.2
	 */
	RefreshToken("jdev/sys/refreshtoken", "^j?dev/sys/refreshtoken/.*"),

	/**
	 * Delete an authentication token.
	 * 
	 * @since 1.2
	 */
	DeleteToken("jdev/sys/killtoken", "^j?dev/sys/killtoken/.*"),

	/**
	 * An encrypted command.
	 * 
	 * @since 1.2
	 */
	EncryptedCommand("jdev/sys/enc", "^j?dev/sys/enc/.*"),

	/** Get the last modificaiton date of the structure file. */
	StructureFileLastModifiedDate("jdev/sps/LoxAPPversion3", "^j?dev/sps/LoxAPPversion3"),

	/** Get the entire structure file. */
	GetStructureFile("data/LoxAPP3.json", null, false),

	/** Enable receiving status updates from the server. */
	EnableInputStatusUpdate("jdev/sps/enablebinstatusupdate", null, false),

	/** Icons are requested by sending the desired icon name as the command. */
	GetIcon(
			"00000000-0000-0020-2000000000000000",
			"^(?i)[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{16}",
			false),

	/**
	 * Read or update control values.
	 * 
	 * @since 1.1
	 */
	IoControl("jdev/sps/io", "^j?dev/sps/io/([^/]+)(?:/(.*))?"),

	/**
	 * Ping message to server does not disconnect the client after 5 minutes of
	 * inactivity.
	 */
	KeepAlive("keepalive", null, false);

	private final String control;
	private final Pattern regex;
	private final boolean encryptionSupported;

	private CommandType(String control) {
		this(control, false);
	}

	private CommandType(String control, boolean regex) {
		this(regex ? null : control, (regex ? control : null));
	}

	private CommandType(String control, String regex) {
		this(control, regex, true);
	}

	private CommandType(String control, String regex, boolean encryptionSupported) {
		this.control = control;
		this.regex = (regex != null ? Pattern.compile(regex) : null);
		this.encryptionSupported = encryptionSupported;
	}

	/**
	 * The control value to use with this command.
	 * 
	 * @return The control value.
	 */
	public String getControlValue() {
		return control;
	}

	/**
	 * Get an enum from a control value.
	 * 
	 * @param value
	 *        The control value.
	 * @return The enum, or <em>null</em> if not known.
	 */
	public static CommandType forControlValue(String value) {
		for ( CommandType t : CommandType.values() ) {
			if ( t.control != null && t.control.equals(value) ) {
				return t;
			} else if ( t.regex != null && t.regex.matcher(value).matches() ) {
				return t;
			}
		}
		return null;
	}

	/**
	 * Get a {@link Matcher} instance for a value.
	 * 
	 * <p>
	 * This is designed for commands like {@link #IoControl} where
	 * subexpressions are included in the pattern and can be used to extract
	 * values from commands values.
	 * </p>
	 * 
	 * @param value
	 *        the value to
	 * @return a new matcher, or {@literal null} if the command type does not
	 *         use a regular expression
	 * @since 1.1
	 */
	public Matcher getMatcher(String value) {
		return (regex != null ? regex.matcher(value) : null);
	}

	/**
	 * Test if a command supports encryption.
	 * 
	 * @return {@literal true} if the command supports command encryption
	 * @since 1.2
	 */
	public boolean isEncryptionSupported() {
		return encryptionSupported;
	}

}
