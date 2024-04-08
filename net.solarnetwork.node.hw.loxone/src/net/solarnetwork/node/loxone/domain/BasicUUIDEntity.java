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
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Base entity that uses a UUID as its primary key.
 *
 * @author matt
 * @version 1.2
 */
public class BasicUUIDEntity implements UUIDEntity {

	private Long configId;
	private UUID uuid;
	private String sourceId;

	@Override
	@JsonIgnore
	public Long getConfigId() {
		return configId;
	}

	@JsonIgnore
	public void setConfigId(Long configId) {
		this.configId = configId;
	}

	@JsonIgnore
	public String getSourceId() {
		return this.sourceId;
	}

	/**
	 * Set a specific source ID.
	 *
	 * @param sourceId
	 *        The source ID to use.
	 * @since 1.1
	 */
	@JsonSetter
	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}

	/**
	 * Get a {@code sourceId} value from this entity.
	 *
	 * @return The source ID, or {@literal null} if unavailable.
	 * @see #sourceIdValue(String)
	 */
	@JsonGetter("sourceId")
	public String getSourceIdValue() {
		return sourceIdValue(this.sourceId);
	}

	/**
	 * Get a source ID value from a given {@code sourceId}.
	 *
	 * If {@code sourceId} is {@literal null}, this will return a derived value
	 * via {@link UUIDEntity#sourceIdForUUIDEntity(UUIDEntity)}. Otherwise, a
	 * combination of the configured {@code configId} and the {@code sourceId}
	 * value will be returned.
	 *
	 * @param sourceId
	 *        The source ID to construct a value from.
	 * @return The source ID value, or {@literal null} if unavailable.
	 * @see UUIDEntity#sourceIdForUUIDEntity(UUIDEntity)
	 */
	protected String sourceIdValue(String sourceId) {
		if ( sourceId != null ) {
			if ( configId != null ) {
				StringBuilder buf = new StringBuilder("/").append(Config.idToExternalForm(configId));
				if ( buf.length() > 11 ) {
					// truncate to only 5 most significant bytes of ID to conserve source ID space
					buf.setLength(11);
				}
				buf.append('/').append(sourceId);
				if ( buf.length() > SOURCE_ID_MAX_LENGTH ) {
					buf.setLength(32);
				}
				return buf.toString();
			}
			if ( sourceId.length() > SOURCE_ID_MAX_LENGTH ) {
				return sourceId.substring(0, SOURCE_ID_MAX_LENGTH);
			}
			return sourceId;
		}
		return UUIDEntity.sourceIdForUUIDEntity(this);
	}

	@Override
	@JsonSerialize(using = UUIDSerializer.class)
	public UUID getUuid() {
		return uuid;
	}

	@JsonDeserialize(using = UUIDDeserializer.class)
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
