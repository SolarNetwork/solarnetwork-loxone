/* ==================================================================
 * DatumUUIDEntityParameters.java - 1/10/2016 11:17:27 AM
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

/**
 * Parameters for {@link DatumUUIDEntity} objects.
 * 
 * @author matt
 * @version 1.0
 */
public interface DatumUUIDEntityParameters extends UUIDEntityParameters {

	/**
	 * Get a maximum number of seconds at which {@code Datum} objects should be
	 * saved from the associated entity value.
	 * 
	 * This is meant to serve as a limiting function to how frequently datum
	 * objects are uploaded to SolarNetwork.
	 * 
	 * @return A maximum number of seconds, or {@code 0} for no limit.
	 */
	Integer getSaveFrequencySeconds();

	/**
	 * Get the type of value this UUID's associated values should be represented
	 * as when translated into a {@code Datum} instance.
	 * 
	 * @return The datum value type, or {@code null} to be treated in a default
	 *         manner.
	 */
	DatumValueType getDatumValueType();

}
