/* ==================================================================
 * ControlDatumParameters.java - 4/10/2016 10:56:41 AM
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

import java.util.Map;
import java.util.UUID;

/**
 * A combination of datum and datum property UUID parameters along with a value
 * from an associated value event.
 * 
 * This API is designed to support finding control state values associated with
 * a datum property UUID set.
 * 
 * @author matt
 * @version 1.0
 */
public interface ControlDatumParameters extends UUIDEntityParameters {

	/**
	 * Get the datum parameters to use with this control.
	 * 
	 * @return The datum parameters.
	 */
	DatumUUIDEntityParameters getDatumParameters();

	/**
	 * Get a map of control state UUIDs to associated value event parameters.
	 * 
	 * @return The map of control state UUIDs to value event parameters.
	 */
	Map<UUID, ValueEventDatumParameters> getDatumPropertyParameters();

}
