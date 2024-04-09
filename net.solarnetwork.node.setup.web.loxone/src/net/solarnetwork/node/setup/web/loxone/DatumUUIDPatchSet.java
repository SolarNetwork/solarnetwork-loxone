/* ==================================================================
 * DatumUUIDPatchSet.java - 1/10/2016 3:36:57 PM
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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import net.solarnetwork.node.loxone.domain.BasicDatumUUIDEntityParameters;
import net.solarnetwork.node.loxone.domain.DatumUUIDEntityParameters;
import net.solarnetwork.node.loxone.domain.UUIDDeserializer;
import net.solarnetwork.node.loxone.domain.UUIDKeyDeserializer;
import net.solarnetwork.node.loxone.domain.UUIDSerializer.UUIDKeySerializer;

/**
 * Extension of {@link UUIDPatchSet} to support
 * {@link DatumUUIDEntityParameters}.
 *
 * @author matt
 * @version 1.1
 */
public class DatumUUIDPatchSet extends UUIDPatchSet {

	private final Map<UUID, DatumUUIDEntityParameters> parameters;

	/**
	 * Construct with a list of UUIDs to add and a list of UUIDs to remove.
	 *
	 * Note that a {@link UUIDKeyDeserializer} is specifically configured for
	 * the {@code parameters} map, because registering a {@code KeyDeserializer}
	 * on a {@code Module} does not seem to override the built-in support for
	 * UUID values in Jackson.
	 *
	 * @param add
	 *        The UUIDs to be added.
	 * @param remove
	 *        The UUIDs to be removed.
	 * @param parameters
	 *        Any parameters to set.
	 */
	@JsonCreator
	public DatumUUIDPatchSet(
			@JsonDeserialize(contentUsing = UUIDDeserializer.class) @JsonProperty(value = "add", required = false) List<UUID> add,
			@JsonDeserialize(contentUsing = UUIDDeserializer.class) @JsonProperty(value = "remove", required = false) List<UUID> remove,
			@JsonDeserialize(keyUsing = UUIDKeyDeserializer.class) @JsonProperty(value = "parameters", required = false) Map<UUID, BasicDatumUUIDEntityParameters> parameters) {
		super(add, remove);
		if ( parameters != null ) {
			this.parameters = new LinkedHashMap<>();
			this.parameters.putAll(parameters);
		} else {
			this.parameters = null;
		}
	}

	@JsonSerialize(keyUsing = UUIDKeySerializer.class)
	public Map<UUID, DatumUUIDEntityParameters> getParameters() {
		return parameters;
	}

}
