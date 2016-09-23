/* ==================================================================
 * MessageType.java - 16/09/2016 7:27:55 PM
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

/**
 * Message type indicator.
 * 
 * @author matt
 * @version 1.0
 */
public enum MessageType {

	Unknown(0xFF),

	TextMessage(0),

	BinaryFile(1),

	EventTableValueStates(2),

	EventTableTextStates(3),

	EventTableDaytimerStates(4),

	OutOfServiceIndicator(5),

	Keepalive(6);

	private byte data;

	private MessageType(int value) {
		this.data = (byte) (value & 0xFF);
	}

	/**
	 * Get an enum from a raw data value.
	 * 
	 * @param data
	 *        The raw data value.
	 * @return The enum.
	 * @throws IllegalArgumentException
	 *         if {@code data} is not a recognized value.
	 */
	public static MessageType forRawDataValue(byte data) {
		switch (data) {
			case (byte) 0x00:
				return TextMessage;

			case (byte) 0x01:
				return BinaryFile;

			case (byte) 0x02:
				return EventTableValueStates;

			case (byte) 0x03:
				return EventTableTextStates;

			case (byte) 0x04:
				return EventTableDaytimerStates;

			case (byte) 0x05:
				return OutOfServiceIndicator;

			case (byte) 0x06:
				return Keepalive;

			case (byte) 0xFF:
				return Unknown;

		}
		throw new IllegalArgumentException("0x" + Integer.toHexString(data) + " is not a valid value");
	}

	public byte getRawValue() {
		return data;
	}
}
