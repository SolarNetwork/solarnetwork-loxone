/* ==================================================================
 * SetTempControlCommand.java - 9/02/2018 3:07:24 PM
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

import java.util.UUID;

/**
 * Control command for setting the temperature of something like an intelligent
 * room controller.
 * 
 * <p>
 * These commands take the form {@literal settemp/N/V} where {@code N} is the
 * temperature state index to set and {@code V} is the double temperature value
 * to set it to.
 * </p>
 * 
 * @author matt
 * @version 1.0
 * @since 1.2
 */
public class SetTempControlCommand extends IndexedStateControlCommand {

	/** The {@literal settemp} command name. */
	public static final String SET_TEMP_COMMAND = "settemp";

	/**
	 * Construct from a string command value.
	 * 
	 * @param uuid
	 *        the UUID of the control to update
	 * @param command
	 *        the string command
	 */
	public SetTempControlCommand(UUID uuid, String command) {
		super(uuid, command);
		if ( !SET_TEMP_COMMAND.equals(getSubCommand()) ) {
			throw new IllegalArgumentException("Command is not in the form settemp/N/V");
		}
	}

	/**
	 * Construct from values.
	 * 
	 * @param uuid
	 *        the UUID of the control to update
	 * @param index
	 *        the index of the temperature state to update
	 * @param value
	 *        the temperature value to set
	 */
	public SetTempControlCommand(UUID uuid, int index, double value) {
		super(uuid, SET_TEMP_COMMAND, index, value);
	}

	/**
	 * Construct from values.
	 * 
	 * @param uuid
	 *        the UUID of the control to update
	 * @param indexedState
	 *        the temperature state to update
	 * @param value
	 *        the temperature value to set
	 */
	public SetTempControlCommand(UUID uuid, IndexedControlState indexedState, double value) {
		super(uuid, SET_TEMP_COMMAND, indexedState, value);
	}

	/**
	 * Get a temperature enum for the configured temperature state.
	 * 
	 * @return the enum
	 * @throws IllegalArgumentException
	 *         if the index is not a valid value
	 */
	public IntelligentRoomControllerTemperature getTemperatureState() {
		return IntelligentRoomControllerTemperature.forControlStateIndex(getIndex());
	}

}
