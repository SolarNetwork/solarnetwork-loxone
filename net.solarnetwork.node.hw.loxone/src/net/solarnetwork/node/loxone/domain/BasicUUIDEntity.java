/* ==================================================================
 * BasicUUIDEntity.java - 19/09/2016 7:29:42 AM
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

import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Base entity that uses a UUID as its primary key.
 * 
 * @author matt
 * @version 1.0
 */
public class BasicUUIDEntity implements UUIDEntity {

	private Long configId;
	private UUID uuid;

	@Override
	@JsonIgnore
	public Long getConfigId() {
		return configId;
	}

	@JsonIgnore
	public void setConfigId(Long configId) {
		this.configId = configId;
	}

	/**
	 * Get a derived {@code sourceId} value from this event.
	 * 
	 * @return The source ID, or {@code null} if unavailable.
	 * @see UUIDEntity#sourceIdForUUIDEntity(UUIDEntity)
	 */
	@JsonGetter
	public String getSourceId() {
		return UUIDEntity.sourceIdForUUIDEntity(this);
	}

	@Override
	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
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
		if ( getClass() != obj.getClass() ) {
			return false;
		}
		BasicUUIDEntity other = (BasicUUIDEntity) obj;
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

}
