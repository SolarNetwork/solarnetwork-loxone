/* ==================================================================
 * DatumValueType.java - 2/10/2016 10:09:41 AM
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
 * An enumeration of possible datum value types.
 * 
 * @author matt
 * @version 1.0
 */
public enum DatumValueType {

	Unknown(0),

	Instantaneous(1),

	Accumulating(2),

	Status(3);

	private final int code;

	private DatumValueType(int code) {
		this.code = code;
	}

	/**
	 * Get an enum from a code value.
	 * 
	 * @param code
	 *        The code value.
	 * @return The enum.
	 */
	public static DatumValueType forCodeValue(int code) {
		switch (code) {
			case 1:
				return Instantaneous;

			case 2:
				return Accumulating;

			case 3:
				return Status;

			default:
				return Unknown;
		}
	}

	/**
	 * Get a code value from this enum.
	 * 
	 * @return The code value.
	 */
	public int getCode() {
		return code;
	}

}
