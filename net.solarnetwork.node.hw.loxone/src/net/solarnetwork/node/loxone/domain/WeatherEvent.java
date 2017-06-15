/* ==================================================================
 * WeatherEvent.java - 15/06/2017 2:39:21 PM
 * 
 * Copyright 2017 SolarNetwork.net Dev Team
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

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * A weather event.
 * 
 * @author matt
 * @version 1.0
 * @since 1.1
 */
public class WeatherEvent extends BaseEventEntity {

	private final Date lastModified;
	private final List<WeatherEventEntry> entries;

	/**
	 * Constructor.
	 * 
	 * @param uuid
	 *        The UUID of the event.
	 * @param configId
	 *        The config ID.
	 * @param created
	 *        The creation date.
	 * @param lastModified
	 *        The last modified date.
	 * @param entries
	 *        The entries associated with this event.
	 */
	public WeatherEvent(UUID uuid, Long configId, Date created, Date lastModified,
			List<WeatherEventEntry> entries) {
		super(uuid, configId, created);
		this.lastModified = lastModified;
		this.entries = entries;
	}

	/**
	 * Get the last modified date.
	 * 
	 * @return the lastModified
	 */
	public Date getLastModified() {
		return lastModified;
	}

	/**
	 * Get the event entries.
	 * 
	 * @return the entries
	 */
	public List<WeatherEventEntry> getEntries() {
		return entries;
	}

}
