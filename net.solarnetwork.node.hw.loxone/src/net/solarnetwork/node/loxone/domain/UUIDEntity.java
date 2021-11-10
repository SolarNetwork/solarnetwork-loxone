/* ==================================================================
 * UUIDEntity.java - 27/09/2016 3:38:41 PM
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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.LongBuffer;
import java.util.UUID;
import org.apache.commons.codec.binary.Base64;

/**
 * Basic entity based on a UUID and {@link Config#getId()} value.
 * 
 * @author matt
 * @version 1.1
 */
public interface UUIDEntity {

	/** The maximum allowed source ID length. */
	int SOURCE_ID_MAX_LENGTH = 32;

	/**
	 * The primary key for the entity.
	 * 
	 * @return The primary key.
	 */
	UUID getUuid();

	/**
	 * The ID of the {@link Config} this entity belongs to.
	 * 
	 * @return The config ID.
	 */
	Long getConfigId();

	/**
	 * Get a derived {@code sourceId} value from an entity.
	 * 
	 * The {@code sourceId} value is suitable for using as a
	 * {@code Datum.sourceId} value. It is derived from the {@code configId} and
	 * {@code uuid} properties of an entity.
	 * 
	 * @param entity
	 *        The entity to derive the source ID value from.
	 * @return The source ID, or {@literal null} if either {@code configId} or
	 *         {@code uuid} are null.
	 */
	static String sourceIdForUUIDEntity(UUIDEntity entity) {
		if ( entity == null ) {
			return null;
		}
		// source IDs are limited to 32 characters, so we could do MD5 as hex, or Base64 SHA1
		// instead just taking 8-byte config ID + 16-byte UUID which is 24 bytes => 32 characters Base64
		Long configId = entity.getConfigId();
		UUID uuid = entity.getUuid();
		if ( configId == null || uuid == null ) {
			return null;
		}
		byte[] bytes = new byte[24];
		LongBuffer buf = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).asLongBuffer();
		buf.put(configId);
		buf.put(uuid.getMostSignificantBits());
		buf.put(uuid.getLeastSignificantBits());
		return Base64.encodeBase64URLSafeString(bytes);
	}

}
