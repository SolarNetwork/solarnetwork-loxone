/* ==================================================================
 * ValueEvent.java - 19/09/2016 7:24:13 AM
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

import java.time.Instant;
import java.util.UUID;

/**
 * A value event.
 *
 * @author matt
 * @version 2.0
 */
public class ValueEvent extends BaseEventEntity {

	private final double value;

	/**
	 * Constructor.
	 *
	 * @param uuid
	 *        the UUID
	 * @param configId
	 *        the configuration ID
	 * @param value
	 *        the value
	 */
	public ValueEvent(UUID uuid, Long configId, double value) {
		this(uuid, configId, Instant.now(), value);
	}

	/**
	 * Constructor.
	 *
	 * @param uuid
	 *        the UUID
	 * @param configId
	 *        the configuration ID
	 * @param created
	 *        the creation date
	 * @param value
	 *        the value
	 */
	public ValueEvent(UUID uuid, Long configId, Instant created, double value) {
		super(uuid, configId, created);
		this.value = value;
	}

	/**
	 * Get the value.
	 *
	 * @return the value
	 */
	public double getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "ValueEvent{uuid=" + getUuid() + ",value=" + value + "}";
	}

}
