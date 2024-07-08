/* ==================================================================
 * UUIDPatchSet.java - 27/09/2016 6:10:29 PM
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

package net.solarnetwork.node.setup.web.loxone;

import java.util.List;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import net.solarnetwork.node.loxone.domain.UUIDDeserializer;
import net.solarnetwork.node.loxone.domain.UUIDSerializer;

/**
 * Data bean to support UUID set patching by specifying a set of UUIDs to be
 * added or removed from a UUID set.
 *
 * @author matt
 * @version 1.1
 */
public class UUIDPatchSet {

	private final List<UUID> add;
	private final List<UUID> remove;

	/**
	 * Construct with a list of UUIDs to add and a list of UUIDs to remove.
	 *
	 * @param add
	 *        The UUIDs to be added.
	 * @param remove
	 *        The UUIDs to be removed.
	 */
	@JsonCreator
	public UUIDPatchSet(
			@JsonProperty(value = "add", required = false) @JsonDeserialize(contentUsing = UUIDDeserializer.class) List<UUID> add,
			@JsonProperty(value = "remove", required = false) @JsonDeserialize(contentUsing = UUIDDeserializer.class) List<UUID> remove) {
		super();
		this.add = add;
		this.remove = remove;
	}

	@JsonSerialize(contentUsing = UUIDSerializer.class)
	public List<UUID> getAdd() {
		return add;
	}

	@JsonSerialize(contentUsing = UUIDSerializer.class)
	public List<UUID> getRemove() {
		return remove;
	}

}
