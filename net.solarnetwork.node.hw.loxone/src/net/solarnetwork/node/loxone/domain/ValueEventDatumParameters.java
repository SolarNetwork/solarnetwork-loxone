/* ==================================================================
 * ValueEventDatumParameters.java - 4/10/2016 12:21:40 PM
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
 * Extension of {@link DatumPropertyUUIDEntityParameters} that includes the
 * value from an associated {@link ValueEvent}.
 * 
 * @author matt
 * @version 1.0
 */
public interface ValueEventDatumParameters extends DatumPropertyUUIDEntityParameters {

	/**
	 * Get a name for this datum property.
	 * 
	 * @return The property name.
	 */
	String getName();

	/**
	 * Get the property value.
	 * 
	 * @return The property value.
	 */
	Double getValue();

}
