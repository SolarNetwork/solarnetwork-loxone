/* ==================================================================
 * StopTimerControlCommand.java - 20/02/2018 4:47:31 PM
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
 * Control command for stopping a timer on something like an intelligent room
 * controller.
 * 
 * @author matt
 * @version 1.0
 * @since 1.2
 */
public class StopTimerControlCommand extends GenericControlCommand {

	/** The {@literal stoptimer} command name. */
	public static final String STOP_TIMER_COMMAND = "stoptimer";

	/**
	 * Construct from a string command value.
	 * 
	 * @param uuid
	 *        the UUID of the control to update
	 * @param command
	 *        the string command
	 */
	public StopTimerControlCommand(UUID uuid, String command) {
		super(uuid, command);
		if ( !STOP_TIMER_COMMAND.equals(command) ) {
			throw new IllegalArgumentException("Command is not in the form stoptimer");
		}
	}

	/**
	 * Construct from values.
	 * 
	 * @param uuid
	 *        the UUID of the control to update
	 */
	public StopTimerControlCommand(UUID uuid) {
		super(uuid, STOP_TIMER_COMMAND);
	}

}
