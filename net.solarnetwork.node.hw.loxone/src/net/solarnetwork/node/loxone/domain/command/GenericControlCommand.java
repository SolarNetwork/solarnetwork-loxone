/* ==================================================================
 * GenericControlCommand.java - 9/02/2018 3:03:56 PM
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
 * Non-specific control command implementation.
 * 
 * @author matt
 * @version 1.0
 * @since 1.2
 */
public class GenericControlCommand implements ControlCommand {

	private final UUID uuid;
	private final String command;

	public GenericControlCommand(UUID uuid, String command) {
		super();
		this.uuid = uuid;
		this.command = command;
	}

	@Override
	public UUID getUuid() {
		return uuid;
	}

	@Override
	public String getCommandValue() {
		return command;
	}

	@Override
	public String toString() {
		return getCommandValue();
	}

}
