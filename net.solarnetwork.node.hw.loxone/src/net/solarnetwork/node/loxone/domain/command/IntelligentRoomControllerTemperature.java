/* ==================================================================
 * IntelligentRoomControllerTemperatures.java - 20/02/2018 8:02:33 PM
 * 
 * Copyright 2018 SolarNetwork.net Dev Team
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

package net.solarnetwork.node.loxone.domain.command;

/**
 * Enumeration of possible intelligent room controller temperatures.
 * 
 * @author matt
 * @version 1.0
 */
public enum IntelligentRoomControllerTemperature implements IndexedControlState {

	Economy(0),

	ComfortHeating(1),

	ComfortCooling(2),

	EmptyHouse(3),

	HeatProtection(4),

	IncreasedHeat(5),

	Party(6),

	Manual(7);

	private final int index;

	private IntelligentRoomControllerTemperature(int index) {
		this.index = index;
	}

	/**
	 * Get the Loxone temperature index for this temperature.
	 * 
	 * <p>
	 * This is the index value to use for temperature commands on the
	 * controller.
	 * </p>
	 * 
	 * @return the temperature index
	 */
	@Override
	public int getControlStateIndex() {
		return index;
	}

	/**
	 * Get an enum value for a temperature index.
	 * 
	 * @param index
	 *        the index of the temperature
	 * @return the enum instance
	 * @throws IllegalArgumentException
	 *         if {@code index} is not a valid value
	 */
	public static IntelligentRoomControllerTemperature forControlStateIndex(int index) {
		for ( IntelligentRoomControllerTemperature val : values() ) {
			if ( index == val.index ) {
				return val;
			}
		}
		throw new IllegalArgumentException(index + " is not a valid index");
	}
}
