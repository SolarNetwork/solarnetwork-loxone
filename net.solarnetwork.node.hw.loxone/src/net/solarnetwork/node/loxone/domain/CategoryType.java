/* ==================================================================
 * CategoryType.java - 20/09/2016 5:42:59 AM
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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Category type values.
 * 
 * @author matt
 * @version 1.0
 */
public enum CategoryType {

	Unknown("unknown", -1),

	Undefined("undefined", 0),

	Indoortemperature("indoortemperature", 1),

	Lights("lights", 2),

	Shading("shading", 3),

	Media("media", 4);

	private String key;
	private short index;

	private CategoryType(String key, int index) {
		this.key = key;
		this.index = (short) index;
	}

	/**
	 * Get the key value for this type.
	 * 
	 * @return The key value.
	 */
	@JsonValue
	public String getKey() {
		return key;
	}

	/**
	 * Get the index value for this type.
	 * 
	 * @return The index value.
	 */
	public short getIndex() {
		return index;
	}

	public static CategoryType forIndexValue(int index) {
		// for now index + 1 == ordinal order
		CategoryType[] values = CategoryType.values();
		index += 1;
		if ( index >= 0 && index < values.length ) {
			return values[index];
		}
		return Unknown;
	}

	/**
	 * Get an enum from a category value.
	 * 
	 * @param value
	 *        The category value.
	 * @return The enum, or <em>null</em> if not known.
	 */
	@JsonCreator
	public static CategoryType forKeyValue(String value) {
		for ( CategoryType t : CategoryType.values() ) {
			if ( t.key.equals(value) ) {
				return t;
			}
		}
		return Unknown;
	}

}
