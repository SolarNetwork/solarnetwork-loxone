/* ==================================================================
 * Config.java - 18/09/2016 6:07:09 AM
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

package net.solarnetwork.node.loxone.domain;

import java.util.Date;

/**
 * Overall configuration, related to the Loxone structure file.
 * 
 * @author matt
 * @version 1.0
 */
public class Config {

	private final Long id;
	private final Date lastModified;

	/**
	 * Construct with just an ID.
	 * 
	 * The modified date will be set to {@code null}.
	 * 
	 * @param configId
	 *        The config ID.
	 */
	public Config(Long configId) {
		this(configId, null);
	}

	/**
	 * Construct with values.
	 * 
	 * @param configId
	 *        The config ID.
	 * @param lastModified
	 *        The modified date.
	 */
	public Config(Long configId, Date lastModified) {
		super();
		this.id = configId;
		this.lastModified = lastModified;
	}

	/**
	 * Get the unique ID of this configuration.
	 * 
	 * @return The unique ID.
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Get the last modified date.
	 * 
	 * @return The modified date, or {@code null}.
	 */
	public Date getLastModified() {
		return lastModified;
	}

	@Override
	public String toString() {
		return "Config{id=" + idToExternalForm() + ", lastModified=" + lastModified + "}";
	}

	/**
	 * Get a copy of this config with an updated modification date.
	 * 
	 * @param lastModified
	 *        The last modified date.
	 * @return The copy.
	 */
	public Config withLastModified(Date lastModified) {
		return new Config(id, lastModified);
	}

	/**
	 * Get an external representation of {@code #getId()} in string form.
	 * 
	 * This string form is meant to be passed to external applications. Use
	 * {@link #idFromExternalForm(String)} to translate the string back to a
	 * {@code Long}.
	 * 
	 * @return The external ID form.
	 * @see #idFromExternalForm(String)
	 */
	public String idToExternalForm() {
		return Config.idToExternalForm(id);
	}

	/**
	 * Get an external representation of a {@code Config.configId} in string
	 * form.
	 * 
	 * @param id
	 *        The ID to translate into external form.
	 * @return The external ID form, or {@code null} if {code id} is
	 *         {@code null}.
	 */
	public static final String idToExternalForm(Long id) {
		if ( id == null ) {
			return null;
		}
		return Long.toUnsignedString(id.longValue(), 16);
	}

	/**
	 * Get a {@code Long} ID value from an external representation.
	 * 
	 * This is the reverse of the {@link #idToExternalForm()} method.
	 * 
	 * @param s
	 *        The ID in external form.
	 * @return The {@code Long} value, or {@code null} if the string cannot be
	 *         parsed.
	 */
	public static final Long idFromExternalForm(String s) {
		try {
			return Long.parseUnsignedLong(s, 16);
		} catch ( NumberFormatException e ) {
			return null;
		}
	}

}
