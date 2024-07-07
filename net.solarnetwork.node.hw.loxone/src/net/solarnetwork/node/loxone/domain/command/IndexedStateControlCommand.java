/* ==================================================================
 * IndexedStateControlCommand.java - 9/02/2018 3:31:11 PM
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Control command for setting the value of an indexed control state, like the
 * temperature of an intelligent room controller.
 *
 * <p>
 * These commands take the form {@literal C/N/V} where {@code C} is the command,
 * {@code N} is the control state index to set, and {@code V} is the state value
 * to set it to.
 * </p>
 *
 * @author matt
 * @version 1.0
 * @since 1.2
 */
public class IndexedStateControlCommand extends GenericControlCommand {

	/** Regular expression for the indexed state value. */
	public static final Pattern INDEXED_STATE_VALUE_REGEX = Pattern
			.compile("(\\w+)/(\\d+)/(\\d+\\.?\\d*)");

	private final String subcmd;
	private final int index;
	private final double value;

	/**
	 * Construct from a string command value.
	 *
	 * @param uuid
	 *        the UUID of the control to update
	 * @param command
	 *        the string command
	 */
	public IndexedStateControlCommand(UUID uuid, String command) {
		super(uuid, command);
		Matcher m = INDEXED_STATE_VALUE_REGEX.matcher(command);
		if ( !m.matches() ) {
			throw new IllegalArgumentException("Command is not in the form C/N/V");
		}
		subcmd = m.group(1);
		index = Integer.parseInt(m.group(2));
		value = Double.parseDouble(m.group(3));
	}

	/**
	 * Construct from values.
	 *
	 * @param uuid
	 *        the UUID of the control to update
	 * @param subCommand
	 *        the sub-command to use
	 * @param index
	 *        the index of the control state to update
	 * @param value
	 *        the state value to set
	 */
	public IndexedStateControlCommand(UUID uuid, String subCommand, int index, double value) {
		super(uuid, String.format("%s/%d/%.6f", subCommand, index, value));
		this.subcmd = subCommand;
		this.index = index;
		this.value = value;
	}

	/**
	 * Construct from values.
	 *
	 * @param uuid
	 *        the UUID of the control to update
	 * @param subCommand
	 *        the sub-command to use
	 * @param indexedState
	 *        the state to update
	 * @param value
	 *        the state value to set
	 */
	public IndexedStateControlCommand(UUID uuid, String subCommand, IndexedControlState indexedState,
			double value) {
		this(uuid, subCommand, indexedState.getControlStateIndex(), value);
	}

	/**
	 * Get the sub-command name.
	 *
	 * @return the sub-command
	 */
	public String getSubCommand() {
		return subcmd;
	}

	/**
	 * Get the control state index.
	 *
	 * @return the index
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Get the control state value.
	 *
	 * @return the value
	 */
	public double getValue() {
		return value;
	}

}
