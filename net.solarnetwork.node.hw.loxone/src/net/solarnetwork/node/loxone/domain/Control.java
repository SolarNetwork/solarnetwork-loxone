/* ==================================================================
 * Control.java - 18/09/2016 6:26:23 AM
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

import java.util.Map;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * An input/output device, sensor, etc.
 *
 * <b>Note</b> that when mapping from JSON either {@code uuid} or
 * {@code uuidAction} can be used to set the {@code uuid} property value.
 * However, when serializing into JSON the property will only be written as
 * {@code uuid}.
 *
 * @author matt
 * @version 2.1
 */
public class Control extends BaseConfigurationEntity {

	private ControlType type;
	private UUID room;
	private Map<String, UUID> states;
	private String details;

	@JsonProperty("cat")
	private UUID category;

	/**
	 * Constructor.
	 */
	public Control() {
		super();
	}

	/**
	 * Constructor.
	 *
	 * @param uuid
	 *        the UUID
	 * @param configId
	 *        the configuration ID
	 */
	public Control(UUID uuid, Long configId) {
		super();
		setUuid(uuid);
		setConfigId(configId);
	}

	@Override
	@JsonIgnore
	public boolean isValid() {
		if ( !super.isValid() ) {
			return false;
		}
		Map<String, UUID> s = getStates();
		if ( s != null ) {
			for ( Map.Entry<String, UUID> me : s.entrySet() ) {
				if ( me.getKey() == null || me.getValue() == null ) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Alias for {@link #getUuid()}.
	 *
	 * @return The UUID.
	 */
	@JsonGetter("uuid")
	@JsonSerialize(using = UUIDSerializer.class)
	public UUID getUuidAction() {
		return getUuid();
	}

	/**
	 * Alias for {@link #setUuid(UUID)}.
	 *
	 * @param uuid
	 *        The UUID to set.
	 */
	@JsonSetter("uuidAction")
	@JsonDeserialize(using = UUIDDeserializer.class)
	public void setUuidAction(UUID uuid) {
		setUuid(uuid);
	}

	/**
	 * Get the control type.
	 *
	 * @return the type
	 */
	public ControlType getType() {
		return type;
	}

	/**
	 * Set the control type.
	 *
	 * @param type
	 *        the type to set
	 */
	public void setType(ControlType type) {
		this.type = type;
	}

	/**
	 * Get the room.
	 *
	 * @return the room
	 */
	@JsonSerialize(using = UUIDSerializer.class)
	public UUID getRoom() {
		return room;
	}

	/**
	 * Set the room.
	 *
	 * @param room
	 *        the room to set
	 */
	@JsonDeserialize(using = UUIDDeserializer.class)
	public void setRoom(UUID room) {
		this.room = room;
	}

	/**
	 * Get the category.
	 *
	 * @return the category
	 */
	@JsonSerialize(using = UUIDSerializer.class)
	public UUID getCategory() {
		return category;
	}

	/**
	 * Set the category.
	 *
	 * @param category
	 *        the category to set
	 */
	@JsonDeserialize(using = UUIDDeserializer.class)
	public void setCategory(UUID category) {
		this.category = category;
	}

	/**
	 * Get the states.
	 *
	 * @return the states
	 */
	@JsonSerialize(contentUsing = UUIDSerializer.class)
	public Map<String, UUID> getStates() {
		return states;
	}

	/**
	 * Set the states.
	 *
	 * @param states
	 *        the states to set
	 */
	@JsonDeserialize(using = net.solarnetwork.node.loxone.domain.UUIDMapDeserializer.class)
	public void setStates(Map<String, UUID> states) {
		this.states = states;
	}

	/**
	 * Get the details.
	 *
	 * @return the details
	 */
	@JsonRawValue
	public String getDetails() {
		return details;
	}

	/**
	 * Set the details.
	 *
	 * @param details
	 *        the details to set
	 */
	@JsonSetter("details")
	public void setDetailsJson(JsonNode details) {
		this.details = details.toString();
	}

}
