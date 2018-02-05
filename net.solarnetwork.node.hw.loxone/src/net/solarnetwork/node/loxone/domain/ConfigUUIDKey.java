/* ==================================================================
 * ConfigUUIDKey.java - 6/02/2018 7:47:21 AM
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

import java.io.Serializable;
import java.util.UUID;

/**
 * A key based on a configuration ID and a UUID.
 * 
 * @author matt
 * @version 1.0
 * @since 1.2
 */
public class ConfigUUIDKey implements Serializable {

	private static final long serialVersionUID = 2754323180632544113L;

	private final Long configId;
	private final UUID uuid;

	/**
	 * Constructor.
	 * 
	 * @param configId
	 *        the config ID
	 * @param uuid
	 *        the UUID
	 */
	public ConfigUUIDKey(Long configId, UUID uuid) {
		super();
		if ( configId == null ) {
			throw new IllegalArgumentException("The configId argument cannot be null.");
		}
		if ( uuid == null ) {
			throw new IllegalArgumentException("The uuid argument cannot be null.");
		}
		this.configId = configId;
		this.uuid = uuid;
	}

	@Override
	public String toString() {
		return "ConfigUUIDKey{configId=" + configId + ", uuid=" + uuid + "}";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((configId == null) ? 0 : configId.hashCode());
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
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
		if ( !(obj instanceof ConfigUUIDKey) ) {
			return false;
		}
		ConfigUUIDKey other = (ConfigUUIDKey) obj;
		if ( configId == null ) {
			if ( other.configId != null ) {
				return false;
			}
		} else if ( !configId.equals(other.configId) ) {
			return false;
		}
		if ( uuid == null ) {
			if ( other.uuid != null ) {
				return false;
			}
		} else if ( !uuid.equals(other.uuid) ) {
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
	 * Get the UUID.
	 * 
	 * @return the UUID
	 */
	public UUID getUuid() {
		return uuid;
	}

}
