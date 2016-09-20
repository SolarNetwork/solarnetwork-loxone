/* ==================================================================
 * ValueEventTableBinaryFileHandler.java - 19/09/2016 6:23:50 AM
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

package net.solarnetwork.node.loxone.protocol.ws.handler;

import java.io.IOException;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.LongBuffer;
import java.util.UUID;
import javax.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.solarnetwork.node.loxone.dao.ValueEventDao;
import net.solarnetwork.node.loxone.domain.ValueEvent;
import net.solarnetwork.node.loxone.protocol.ws.BinaryFileHandler;
import net.solarnetwork.node.loxone.protocol.ws.LoxoneEndpoint;
import net.solarnetwork.node.loxone.protocol.ws.MessageHeader;
import net.solarnetwork.node.loxone.protocol.ws.MessageType;

/**
 * {@link BinaryFileHandler} for value-type event binary messages.
 * 
 * @author matt
 * @version 1.0
 */
public class ValueEventTableBinaryFileHandler implements BinaryFileHandler {

	private ValueEventDao valueEventDao;
	private final Logger log = LoggerFactory.getLogger(getClass());

	@Override
	public boolean supportsTextMessage(MessageHeader header, Reader reader, int limit)
			throws IOException {
		return false;
	}

	@Override
	public boolean handleTextMessage(MessageHeader header, Session session, Reader reader)
			throws IOException {
		return false;
	}

	@Override
	public boolean supportsDataMessage(MessageHeader header, ByteBuffer buffer) {
		return (MessageType.EventTableValueStates == header.getType());
	}

	private Long getConfigId(Session session) {
		Object id = session.getUserProperties().get(LoxoneEndpoint.CONFIG_ID_USER_PROPERTY);
		if ( !(id instanceof Long) ) {
			return null;
		}
		return (Long) id;
	}

	@Override
	public boolean handleDataMessage(MessageHeader header, Session session, ByteBuffer buffer) {
		if ( buffer.order() != ByteOrder.LITTLE_ENDIAN ) {
			buffer.order(ByteOrder.LITTLE_ENDIAN);
		}
		Long configId = getConfigId(session);
		if ( configId == null ) {
			return false;
		}
		int end = buffer.position() + (int) header.getLength();
		while ( buffer.hasRemaining() && buffer.position() < end ) {
			UUID uuid = readUUID(buffer);
			double value = buffer.asDoubleBuffer().get();
			buffer.position(buffer.position() + 8);
			log.trace("Parsed value event {} = {}", uuid, value);
			ValueEvent ve = new ValueEvent(uuid, configId, value);
			valueEventDao.storeEvent(ve);
		}
		return true;
	}

	private UUID readUUID(ByteBuffer buffer) {
		LongBuffer buf = buffer.order(ByteOrder.BIG_ENDIAN).asLongBuffer();
		UUID uuid = new UUID(buf.get(), buf.get());
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.position(buffer.position() + 16);
		return uuid;
	}

	public void setValueEventDao(ValueEventDao valueEventDao) {
		this.valueEventDao = valueEventDao;
	}

}
