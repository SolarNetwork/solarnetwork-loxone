/* ==================================================================
 * MessageHeader.java - 16/09/2016 7:36:51 PM
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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Set;

/**
 * Message header structure.
 * 
 * @author matt
 * @version 1.0
 */
public class MessageHeader {

	/** A magic byte at the start of the message header. */
	public static final byte MAGIC_BYTE = 0x03;

	private final MessageType type;
	private final Set<MessageInfo> info;
	private final long length;

	private final byte rawType;

	/**
	 * Construct with values.
	 */
	public MessageHeader(MessageType type, Set<MessageInfo> info, long length) {
		super();
		this.type = type;
		this.rawType = type.getRawValue();
		this.info = info;
		this.length = length;
	}

	/**
	 * Construct from a byte buffer.
	 * 
	 * Starts reading from the buffer's current position, and when finished the
	 * position will be after the end of the header.
	 * 
	 * @param buf
	 *        The buffer to read from.
	 */
	public MessageHeader(ByteBuffer buf) {
		super();
		if ( buf.remaining() < 8 ) {
			throw new IllegalArgumentException("Not enough data available.");
		}
		byte b = buf.get();
		if ( b != MAGIC_BYTE ) {
			throw new IllegalArgumentException(
					"Unexpected magic byte: " + Integer.toHexString(b & 0xFF));
		}
		if ( buf.order() != ByteOrder.LITTLE_ENDIAN ) {
			buf.order(ByteOrder.LITTLE_ENDIAN);
		}

		// stash the raw type value so if it is an unsupported value we can still see what it was
		this.rawType = buf.get();
		MessageType t;
		try {
			t = MessageType.forRawDataValue(this.rawType);
		} catch ( IllegalArgumentException e ) {
			t = MessageType.Unknown;
		}
		this.type = t;

		this.info = MessageInfo.forRawDataValue(buf.get());
		buf.position(buf.position() + 1); // skip next bytes
		this.length = buf.getInt() & 0xFFFFFFFFL; // read last 4 bytes as unsigned int
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder("MessageHeader{");
		if ( type == MessageType.Unknown ) {
			buf.append("0x").append(Integer.toHexString(rawType));
		} else {
			buf.append(type);
		}
		if ( info != null && !info.isEmpty() ) {
			buf.append("; flags=");
			for ( MessageInfo flag : info ) {
				if ( buf.charAt(buf.length() - 1) != '=' ) {
					buf.append(',');
				}
				buf.append(flag);
			}
		}
		buf.append("; length=").append(length).append("}");
		return buf.toString();
	}

	/**
	 * Get the message type.
	 * 
	 * @return The message type.
	 */
	public MessageType getType() {
		return type;
	}

	/**
	 * Get the message info flags.
	 * 
	 * @return The message info flags.
	 */
	public Set<MessageInfo> getInfo() {
		return info;
	}

	/**
	 * Get the expected message length. The value might only be an estimate if
	 * the {@link MessageInfo#EstimatedSize} flag is present.
	 * 
	 * @return The expected length of the message.
	 */
	public long getLength() {
		return length;
	}

	/**
	 * Get the raw type value. If {@link #getType()} returns {@code Unknown}
	 * this method can be used to see what the actual data was.
	 * 
	 * @return The raw message header type.
	 */
	public byte getRawType() {
		return rawType;
	}

}
