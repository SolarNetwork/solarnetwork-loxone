/* ==================================================================
 * SourceMappingPatchSet.java - 12/10/2016 6:11:55 PM
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
import net.solarnetwork.node.loxone.domain.SourceMapping;
import net.solarnetwork.node.loxone.domain.UUIDDeserializer;
import net.solarnetwork.node.loxone.domain.UUIDSerializer;

/**
 * Data bean to support source mapping patching by specifying a set of source
 * mappings to be added or updated and a set of UUIDs of source mappings to be
 * removed.
 *
 * @author matt
 * @version 1.1
 * @since 0.2
 */
public class SourceMappingPatchSet {

	private final List<SourceMapping> store;
	private final List<UUID> remove;

	/**
	 * Construct with a list of UUIDs to add and a list of UUIDs to remove.
	 *
	 * @param store
	 *        The source mappings to be added or updated.
	 * @param remove
	 *        The UUIDs of source mappings to be removed.
	 */
	@JsonCreator
	public SourceMappingPatchSet(
			@JsonProperty(value = "store", required = false) List<SourceMapping> store,
			@JsonProperty(value = "remove", required = false) @JsonDeserialize(contentUsing = UUIDDeserializer.class) List<UUID> remove) {
		super();
		this.store = store;
		this.remove = remove;
	}

	public List<SourceMapping> getStore() {
		return store;
	}

	@JsonSerialize(contentUsing = UUIDSerializer.class)
	public List<UUID> getRemove() {
		return remove;
	}

}
