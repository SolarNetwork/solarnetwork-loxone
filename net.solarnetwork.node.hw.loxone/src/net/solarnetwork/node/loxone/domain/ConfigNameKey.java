/* ==================================================================
 * ConfigNameKey.java - 6/02/2018 8:22:01 AM
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

/**
 * A key based on a configuration ID and a string name.
 * 
 * @author matt
 * @version 1.0
 * @since 1.2
 */
public class ConfigNameKey {

	private final Long configId;
	private final String name;

	/**
	 * Constructor.
	 * 
	 * @param configId
	 *        the config ID
	 * @param name
	 *        the name
	 */
	public ConfigNameKey(Long configId, String name) {
		super();
		if ( configId == null ) {
			throw new IllegalArgumentException("The configId argument cannot be null.");
		}
		if ( name == null ) {
			throw new IllegalArgumentException("The name argument cannot be null.");
		}
		this.configId = configId;
		this.name = name;
	}

	@Override
	public String toString() {
		return "ConfigNameKey{configId=" + configId + ", name=" + name + "}";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((configId == null) ? 0 : configId.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( obj == null ) {
			return false;
		}
		if ( !(obj instanceof ConfigNameKey) ) {
			return false;
		}
		ConfigNameKey other = (ConfigNameKey) obj;
		if ( configId == null ) {
			if ( other.configId != null ) {
				return false;
			}
		} else if ( !configId.equals(other.configId) ) {
			return false;
		}
		if ( name == null ) {
			if ( other.name != null ) {
				return false;
			}
		} else if ( !name.equals(other.name) ) {
			return false;
		}
		return true;
	}

	/**
	 * Get the configuration ID.
	 * 
	 * @return the configuration ID
	 */
	public Long getConfigId() {
		return configId;
	}

	/**
	 * Get the name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

}
