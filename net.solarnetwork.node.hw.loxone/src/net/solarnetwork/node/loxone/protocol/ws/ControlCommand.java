/* ==================================================================
 * ControlCommand.java - 9/02/2018 2:53:50 PM
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

package net.solarnetwork.node.loxone.protocol.ws;

import java.util.UUID;

/**
 * API for a control command object.
 * 
 * @author matt
 * @version 1.0
 * @since 1.2
 */
public interface ControlCommand {

	/**
	 * Get the control UUID the command is for.
	 * 
	 * @return the UUID
	 */
	UUID getUuid();

	/**
	 * Get the command as a string value.
	 * 
	 * <p>
	 * It is expected that the {@code toString()} method of any class that
	 * implements this interface will return the same value as this method.
	 * </p>
	 * 
	 * @return the command string
	 */
	String getCommandValue();

}
