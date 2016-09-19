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
import java.util.UUID;
import javax.websocket.Session;
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
		Long configId = getConfigId(session);
		if ( configId == null ) {
			return false;
		}
		int end = buffer.position() + (int) header.getLength();
		while ( buffer.hasRemaining() && buffer.position() < end ) {
			UUID uuid = readUUID(buffer);
			double value = buffer.getDouble();
			ValueEvent ve = new ValueEvent(uuid, configId, value);
			valueEventDao.storeValueEvent(ve);
		}
		return true;
	}

	private UUID readUUID(ByteBuffer buffer) {
		StringBuilder buf = new StringBuilder();
		int d1 = buffer.getInt();
		short d2 = buffer.getShort();
		short d3 = buffer.getShort();
		byte[] d4 = new byte[8];
		buffer.get(d4);
		zeroPadUnsignedAppend(buf, d1);
		buf.append('-');
		zeroPadUnsignedAppend(buf, d2);
		buf.append('-');
		zeroPadUnsignedAppend(buf, d3);
		buf.append('-');
		for ( byte b : d4 ) {
			zeroPadUnsignedAppend(buf, Byte.toUnsignedInt(b), 2);
		}
		return UUID.fromString(buf.toString());
	}

	private void zeroPadUnsignedAppend(StringBuilder buf, short value) {
		zeroPadUnsignedAppend(buf, Short.toUnsignedInt(value), 4);
	}

	private void zeroPadUnsignedAppend(StringBuilder buf, int value) {
		zeroPadUnsignedAppend(buf, value, 8);
	}

	private void zeroPadUnsignedAppend(StringBuilder buf, int value, int padLength) {
		String s = Integer.toUnsignedString(value, 16);
		int pads = (padLength - s.length());
		for ( int i = 0; i < pads; i++ ) {
			buf.append('0');
		}
		buf.append(s);
	}

	public void setValueEventDao(ValueEventDao valueEventDao) {
		this.valueEventDao = valueEventDao;
	}

}
