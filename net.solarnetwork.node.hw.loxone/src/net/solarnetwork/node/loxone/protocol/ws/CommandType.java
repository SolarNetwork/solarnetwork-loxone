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
	Authenticate("^authenticate/.*", true),

	/** Authenticate response. */
	Auth("Auth"),

	/**
	 * Get the X.509 public key.
	 * 
	 * @since 1.2
	 */
	GetPublicKey("j?dev/sys/getPublicKey"),

	/**
	 * Perform a session key exchange.
	 * 
	 * @since 1.2
	 */
	KeyExchange("^j?dev/sys/keyexchange/.*", true),

	/**
	 * Get the key to use for authenticating the connection via a token.
	 * 
	 * @since 1.2
	 */
	GetTokenKey("^j?dev/sys/getkey2/.*", true),

	/**
	 * Get an authentication token.
	 * 
	 * @since 1.2
	 */
	GetToken("^j?dev/sys/gettoken/.*", true),

	/**
	 * Authenticate the connection with a token.
	 * 
	 * @since 1.2
	 */
	AuthenticateWithToken("^authwithtoken/.*", true),

	/**
	 * Refresh an authentication token.
	 * 
	 * @since 1.2
	 */
	RefreshToken("^j?dev/sys/refreshtoken/.*", true),

	/**
	 * Delete an authentication token.
	 * 
	 * @since 1.2
	 */
	DeleteToken("^j?dev/sys/killtoken/.*", true),

	/** Get the last modificaiton date of the structure file. */
	StructureFileLastModifiedDate("jdev/sps/LoxAPPversion3", "^j?dev/sps/LoxAPPversion3"),

	/** Get the entire structure file. */
	GetStructureFile("data/LoxAPP3.json"),

	/** Enable receiving status updates from the server. */
	EnableInputStatusUpdate("jdev/sps/enablebinstatusupdate"),

	/** Icons are requested by sending the desired icon name as the command. */
	GetIcon(
			"00000000-0000-0020-2000000000000000",
			"^(?i)[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{16}"),

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
	KeepAlive("keepalive");

	private final String control;
	private final Pattern regex;

	private CommandType(String control) {
		this(control, false);
	}

	private CommandType(String control, boolean regex) {
		this.control = (regex ? null : control);
		this.regex = (regex ? Pattern.compile(control) : null);
	}

	private CommandType(String control, String regex) {
		this.control = control;
		this.regex = Pattern.compile(regex);
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

}
