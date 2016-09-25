/* ==================================================================
 * EventEntity.java - 26/09/2016 11:03:54 AM
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

import java.util.Date;
import java.util.UUID;

/**
 * API for Loxone event entities.
 * 
 * @author matt
 * @version 1.0
 */
public interface EventEntity {

	/**
	 * The primary key for the entity.
	 * 
	 * @return The primary key.
	 */
	UUID getUuid();

	/**
	 * The ID of the {@link Config} this entity belongs to.
	 * 
	 * @return The config ID.
	 */
	Long getConfigId();

	/**
	 * Get the event creation date.
	 * 
	 * @return The created date.
	 */
	Date getCreated();

}
