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
 * Special deserializer for a map of string keys and UUID values, ignoring other
 * data types during parsing such as array values.
 * 
 * This can be useful when parsing control state objects, for example, where
 * some states are encoded as an array of UUID values.
 * 
 * @author matt
 * @version 1.0
 * @since 1.0.4
 */
public class UUIDMapDeserializer extends StdDeserializer<Map<String, UUID>> {

	private static final long serialVersionUID = 1332932574760401530L;

	public UUIDMapDeserializer() {
		super(Map.class);
	}

	@Override
	public Map<String, UUID> deserialize(JsonParser parser, DeserializationContext context)
			throws IOException, JsonProcessingException {
		Map<String, UUID> result = new LinkedHashMap<>();
		JsonToken tok = parser.getCurrentToken();
		while ( tok != null && tok != JsonToken.END_OBJECT ) {
			if ( tok == JsonToken.VALUE_STRING ) {
				result.put(parser.getCurrentName(), UUIDDeserializer.deserializeUUID(parser.getText()));
			}
			tok = parser.nextValue();
		}
		return result;
	}

}
