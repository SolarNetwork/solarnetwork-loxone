/* ==================================================================
 * SetValueCommand.java - 9/02/2018 3:08:53 PM
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
 * Set the value of a control UUID directly.
 * 
 * @author matt
 * @version 1.0
 * @since 1.2
 */
public class SetValueCommand extends GenericControlCommand {

	private final double value;

	/**
	 * Construct from a string command.
	 * 
	 * @param uuid
	 *        the control UUID
	 * @param command
	 *        the command, which should be parsable as a double
	 */
	public SetValueCommand(UUID uuid, String command) {
		super(uuid, command);
		this.value = Double.parseDouble(command);
	}

	/**
	 * Construct with a value.
	 * 
	 * @param uuid
	 *        the control UUID
	 * @param value
	 *        the value of the control
	 */
	public SetValueCommand(UUID uuid, double value) {
		super(uuid, String.valueOf(value));
		this.value = value;
	}

	/**
	 * Get the control value.
	 * 
	 * @return the control value
	 */
	public double getValue() {
		return value;
	}

}
