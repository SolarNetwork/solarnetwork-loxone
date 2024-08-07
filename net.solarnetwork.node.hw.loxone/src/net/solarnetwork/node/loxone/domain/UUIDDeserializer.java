/* ==================================================================
 * UUIDDeserializer.java - 19/09/2016 3:30:29 PM
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

package net.solarnetwork.node.loxone.domain;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.UUID;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.FromStringDeserializer;

/**
 * Deserializer for Loxone encoded UUID values.
 *
 * @author matt
 * @version 1.0
 */
public class UUIDDeserializer extends FromStringDeserializer<UUID> {

	private static final long serialVersionUID = -2425483383178032052L;

	/**
	 * Constructor.
	 */
	public UUIDDeserializer() {
		super(UUID.class);
	}

	@Override
	protected UUID _deserialize(String value, DeserializationContext context) throws IOException {
		return deserializeUUID(value);
	}

	/**
	 * Utility method for deserializing a Loxone UUID value. "Regular" Java
	 * style UUID strings are supported, too.
	 *
	 * @param value
	 *        The Loxone UUID string value.
	 * @return The parsed UUID object.
	 * @throws IOException
	 *         If any parsing error occurs.
	 */
	public static final UUID deserializeUUID(String value) throws IOException {
		byte[] data;
		try {
			data = Hex.decodeHex(value.replace("-", "").toCharArray());
		} catch ( DecoderException e ) {
			throw new IOException("Error decoding UUID value [" + value + "]", e);
		}
		LongBuffer buf = ByteBuffer.wrap(data).asLongBuffer();
		UUID uuid = new UUID(buf.get(), buf.get());
		return uuid;
	}

}
