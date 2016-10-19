/* ==================================================================
 * BasicControlDatumParameters.java - 4/10/2016 11:32:51 AM
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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import net.solarnetwork.node.loxone.domain.UUIDSerializer.UUIDKeySerializer;

/**
 * Basic implementation of {@link ControlDatumParameters}.
 * 
 * @author matt
 * @version 1.0
 */
public class BasicControlDatumParameters
		implements ControlDatumParameters {

	private DatumUUIDEntityParameters datumParameters;
	private Map<UUID, ValueEventDatumParameters> datumPropertyParameters;

	public BasicControlDatumParameters() {
		super();
		datumPropertyParameters = new LinkedHashMap<>(2);
	}

	@Override
	public DatumUUIDEntityParameters getDatumParameters() {
		return datumParameters;
	}

	public void setDatumParameters(DatumUUIDEntityParameters datumParameters) {
		this.datumParameters = datumParameters;
	}

	@JsonSerialize(keyUsing = UUIDKeySerializer.class)
	@Override
	public Map<UUID, ValueEventDatumParameters> getDatumPropertyParameters() {
		return datumPropertyParameters;
	}

	public void setDatumPropertyParameters(
			Map<UUID, ValueEventDatumParameters> datumPropertyParameters) {
		this.datumPropertyParameters = datumPropertyParameters;
	}

}
