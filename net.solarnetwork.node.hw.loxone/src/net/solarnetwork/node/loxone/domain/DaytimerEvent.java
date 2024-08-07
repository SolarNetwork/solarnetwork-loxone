/* ==================================================================
 * DaytimerEvent.java - 20/09/2016 7:15:15 PM
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
import java.util.List;
import java.util.UUID;

/**
 * A daytimer event.
 *
 * @author matt
 * @version 2.0
 */
public class DaytimerEvent extends BaseEventEntity {

	private final double defaultValue;
	private final List<DaytimerEventEntry> entries;

	/**
	 * Constructor.
	 *
	 * @param uuid
	 *        The UUID of the event.
	 * @param configId
	 *        The config ID.
	 * @param created
	 *        The creation date.
	 * @param defaultValue
	 *        The default value.
	 * @param entries
	 *        The entries associated with this event.
	 */
	public DaytimerEvent(UUID uuid, Long configId, Instant created, double defaultValue,
			List<DaytimerEventEntry> entries) {
		super(uuid, configId, created);
		this.defaultValue = defaultValue;
		this.entries = entries;
	}

	/**
	 * Get the default value.
	 *
	 * @return the default value
	 */
	public double getDefaultValue() {
		return defaultValue;
	}

	/**
	 * Get the entries.
	 *
	 * @return the entries
	 */
	public List<DaytimerEventEntry> getEntries() {
		return entries;
	}

}
