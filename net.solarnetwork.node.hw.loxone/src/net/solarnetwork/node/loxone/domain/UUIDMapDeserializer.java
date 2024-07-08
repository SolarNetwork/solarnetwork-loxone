/* ==================================================================
 * UUIDMapDeserializer.java - 24/11/2016 9:38:42 AM
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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

/**
 * Special deserializer for a map of string keys and UUID values, including
 * arrays of UUID values.
 *
 * <p>
 * This can be useful when parsing control state objects, for example, where
 * some states are encoded as an array of UUID values. When an array of strings
 * is encountered, each array value is treated as a UUID and the resulting map
 * key will have {@literal [i]} appended, where {@literal i} is the array index.
 * </p>
 *
 * <p>
 * For example, given a JSON structure like this:
 * </p>
 *
 * <pre>
 * <code>{"temperatures" : [ "1030808a-01de-58fe-ffff89145a801961", "1030808a-01de-58ff-ffff89145a801961" ]}</code>
 * </pre>
 *
 * <p>
 * then the resulting map would contain two entries like this:
 * </p>
 *
 * <pre>
 * <code>{
 *   "temperatures[0]" -&gt; UUID("1030808a-01de-58fe-ffff-89145a801961"),
 *   "temperatures[1]" -&gt; UUID("1030808a-01de-58ff-ffff-89145a801961")
 * }</code>
 * </pre>
 *
 * @author matt
 * @version 1.1
 * @since 1.0.4
 */
public class UUIDMapDeserializer extends StdDeserializer<Map<String, UUID>> {

	private static final long serialVersionUID = 1332932574760401530L;

	/**
	 * Constructor.
	 */
	public UUIDMapDeserializer() {
		super(Map.class);
	}

	@Override
	public Map<String, UUID> deserialize(JsonParser parser, DeserializationContext context)
			throws IOException, JsonProcessingException {
		Map<String, UUID> result = new LinkedHashMap<>();
		JsonToken tok = parser.getCurrentToken();
		while ( tok != null && tok != JsonToken.END_OBJECT ) {
			if ( tok == JsonToken.VALUE_STRING && parser.getCurrentName() != null ) {
				result.put(parser.getCurrentName(), UUIDDeserializer.deserializeUUID(parser.getText()));
			} else if ( tok == JsonToken.START_ARRAY && parser.getCurrentName() != null ) {
				// parse array values using indexed key names
				String baseName = parser.getCurrentName();
				int idx = 0;
				for ( tok = parser.nextValue(); tok != null
						&& tok != JsonToken.END_ARRAY; tok = parser.nextToken(), idx++ ) {
					if ( tok == JsonToken.VALUE_STRING ) {
						String arrayName = baseName + "[" + idx + "]";
						result.put(arrayName, UUIDDeserializer.deserializeUUID(parser.getText()));
					}
				}
			}
			tok = parser.nextValue();
		}
		return result;
	}

}
