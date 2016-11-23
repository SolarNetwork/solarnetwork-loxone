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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * An input/output device, sensor, etc.
 * 
 * <b>Note</b> that when mapping from JSON either {@code uuid} or
 * {@code uuidAction} can be used to set the {@code uuid} property value.
 * However, when serializing into JSON the property will only be written as
 * {@code uuid}.
 * 
 * @author matt
 * @version 1.1
 */
public class Control extends BaseConfigurationEntity {

	private ControlType type;
	private UUID room;

	public Control() {
		super();
	}

	public Control(UUID uuid, Long configId) {
		super();
		setUuid(uuid);
		setConfigId(configId);
	}

	@JsonProperty("cat")
	private UUID category;

	private Map<String, UUID> states;

	private String details;

	/**
	 * Alias for {@link #setUuid(UUID)}.
	 * 
	 * @return The UUID.
	 */
	@JsonGetter("uuid")
	public UUID getUuidAction() {
		return getUuid();
	}

	/**
	 * Alias for {@link #getUuid()}.
	 * 
	 * @param uuid
	 *        The UUID to set.
	 */
	@JsonSetter("uuidAction")
	public void setUuidAction(UUID uuid) {
		setUuid(uuid);
	}

	public ControlType getType() {
		return type;
	}

	public void setType(ControlType type) {
		this.type = type;
	}

	public UUID getRoom() {
		return room;
	}

	public void setRoom(UUID room) {
		this.room = room;
	}

	public UUID getCategory() {
		return category;
	}

	public void setCategory(UUID category) {
		this.category = category;
	}

	public Map<String, UUID> getStates() {
		return states;
	}

	@JsonDeserialize(using = net.solarnetwork.node.loxone.domain.UUIDMapDeserializer.class)
	public void setStates(Map<String, UUID> states) {
		this.states = states;
	}

	@JsonRawValue
	public String getDetails() {
		return details;
	}

	@JsonSetter("details")
	public void setDetailsJson(JsonNode details) {
		this.details = details.toString();
	}

}
