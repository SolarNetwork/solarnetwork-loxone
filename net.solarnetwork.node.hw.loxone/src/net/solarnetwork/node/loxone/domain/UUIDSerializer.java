/* ==================================================================
 * UUIDSerializer.java - 19/09/2016 4:36:53 PM
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
import java.io.Serializable;
import java.util.UUID;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;

/**
 * JSON serializer for Loxone encoded {@link UUID} objects.
 * 
 * @author matt
 * @version 1.2
 */
public class UUIDSerializer extends StdScalarSerializer<UUID> implements Serializable {

	private static final long serialVersionUID = 7754182067053966014L;

	/**
	 * A specialized JSON serializer for Loxone UUID values used as keys in a
	 * {@code Map}.
	 * 
	 * @author matt
	 * @version 1.1
	 */
	public static final class UUIDKeySerializer extends UUIDSerializer {

		private static final long serialVersionUID = 8490124390190520595L;

		public UUIDKeySerializer() {
			super(true);
		}
	}

	private final boolean keyMode;

	public UUIDSerializer() {
		this(false);
	}

	private UUIDSerializer(boolean keyMode) {
		super(UUID.class);
		this.keyMode = keyMode;
	}

	@Override
	public void serialize(UUID uuid, JsonGenerator generator, SerializerProvider provider)
			throws IOException, JsonGenerationException {
		if ( uuid == null ) {
			generator.writeNull();
		} else {
			String value = serializeUUID(uuid);
			if ( keyMode ) {
				generator.writeFieldName(value);
			} else {
				generator.writeString(value);
			}
		}
	}

	/**
	 * Utility method for serializing a Loxone UUID value.
	 * 
	 * @param uuid
	 *        The UUID value.
	 * @return The serialized UUID object.
	 * @since 1.2
	 */
	public static final String serializeUUID(UUID uuid) {
		StringBuilder buf = new StringBuilder(uuid.toString());
		buf.deleteCharAt(23);
		return buf.toString();
	}

}
