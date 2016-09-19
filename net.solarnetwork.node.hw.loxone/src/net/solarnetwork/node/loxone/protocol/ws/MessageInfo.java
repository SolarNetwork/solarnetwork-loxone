/* ==================================================================
 * MessageInfo.java - 16/09/2016 7:45:03 PM
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

package net.solarnetwork.node.loxone.protocol.ws;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/**
 * Bitmask flags for the message info byte of a message header.
 * 
 * @author matt
 * @version 1.0
 */
public enum MessageInfo {

	EstimatedSize;

	/**
	 * Get an enum set from a raw data value.
	 * 
	 * @param data
	 *        The raw data value.
	 * @return The set of matching flags. May be empty.
	 * @throws IllegalArgumentException
	 *         if {@code data} is not a recognized value.
	 */
	public static Set<MessageInfo> forRawDataValue(byte data) {
		int count = MessageInfo.values().length;
		Set<MessageInfo> infoSet = new HashSet<MessageInfo>(count);
		for ( int i = 0; i < count; i++ ) {
			if ( ((data >> i) & 0x1) == 1 ) {
				infoSet.add(MessageInfo.values()[i]);
			}
		}
		return (infoSet.isEmpty() ? Collections.emptySet() : EnumSet.copyOf(infoSet));
	}

}
