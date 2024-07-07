/* ==================================================================
 * BasicDatumUUIDEntity.java - 27/09/2016 5:58:06 PM
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

/**
 * Basic implementation of {@link DatumUUIDEntity}.
 *
 * @author matt
 * @version 1.0
 */
public class BasicDatumUUIDEntity extends BasicUUIDEntity implements DatumUUIDEntity {

	private DatumUUIDEntityParameters parameters;

	/**
	 * Constructor.
	 */
	public BasicDatumUUIDEntity() {
		super();
	}

	/**
	 * Constructor.
	 *
	 * @param configId
	 *        the configuration ID
	 * @param uuid
	 *        the UUID
	 * @param parameters
	 *        the parameters
	 */
	public BasicDatumUUIDEntity(Long configId, UUID uuid, DatumUUIDEntityParameters parameters) {
		super();
		setConfigId(configId);
		setUuid(uuid);
		setParameters(parameters);
	}

	/**
	 * Set the parameters.
	 *
	 * @param parameters
	 *        the parameters to set
	 */
	public void setParameters(DatumUUIDEntityParameters parameters) {
		this.parameters = parameters;
	}

	@Override
	public DatumUUIDEntityParameters getParameters() {
		return parameters;
	}

}
