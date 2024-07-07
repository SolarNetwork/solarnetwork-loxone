/* ==================================================================
 * IntelligentRoomControllerServiceMode.java - 21/02/2018 7:15:25 AM
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
 * Service modes for the intelligent room controller.
 *
 * @author matt
 * @version 1.0
 * @since 1.2
 */
public enum IntelligentRoomControllerManualMode implements IndexedControlState {

	/** Off. */
	Off(0),

	/** Conform override. */
	ComfortOverride(1),

	/** Economy override. */
	EconomyOverride(2),

	/** Timer override. */
	TimerOverride(3),

	/** Presence. */
	Presence(4);

	private final int index;

	private IntelligentRoomControllerManualMode(int index) {
		this.index = index;
	}

	@Override
	public int getControlStateIndex() {
		return index;
	}

	/**
	 * Get an enum value for a mode index.
	 *
	 * @param index
	 *        the index of the mode
	 * @return the enum instance
	 * @throws IllegalArgumentException
	 *         if {@code index} is not a valid value
	 */
	public static IntelligentRoomControllerManualMode forControlStateIndex(int index) {
		for ( IntelligentRoomControllerManualMode mode : values() ) {
			if ( mode.index == index ) {
				return mode;
			}
		}
		throw new IllegalArgumentException(index + " is not a valid index");
	}

}
