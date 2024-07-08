/* ==================================================================
 * UUIDKeyDeserializer.java - 1/10/2016 4:52:45 PM
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
import java.util.UUID;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import net.solarnetwork.codec.TypedKeyDeserializer;

/**
 * A {@link KeyDeserializer} for Loxone UUID values.
 *
 * @author matt
 * @version 2.0
 */
public class UUIDKeyDeserializer extends KeyDeserializer implements TypedKeyDeserializer {

	/**
	 * Constructor.
	 */
	public UUIDKeyDeserializer() {
		super();
	}

	@Override
	public Class<?> getKeyType() {
		return UUID.class;
	}

	@Override
	public KeyDeserializer getKeyDeserializer() {
		return this;
	}

	@Override
	public Object deserializeKey(String key, DeserializationContext context)
			throws IOException, JsonProcessingException {
		return UUIDDeserializer.deserializeUUID(key);
	}

}
