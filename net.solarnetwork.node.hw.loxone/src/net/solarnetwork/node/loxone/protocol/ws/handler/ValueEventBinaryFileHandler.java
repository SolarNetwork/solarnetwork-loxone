/* ==================================================================
 * ValueEventBinaryFileHandler.java - 19/09/2016 6:23:50 AM
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

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.UUID;
import javax.websocket.Session;
import net.solarnetwork.node.loxone.domain.ValueEvent;
import net.solarnetwork.node.loxone.protocol.ws.BinaryFileHandler;
import net.solarnetwork.node.loxone.protocol.ws.MessageHeader;
import net.solarnetwork.node.loxone.protocol.ws.MessageType;

/**
 * {@link BinaryFileHandler} for value-type event binary messages.
 * 
 * @author matt
 * @version 1.0
 */
public class ValueEventBinaryFileHandler extends BaseEventBinaryFileHandler<ValueEvent> {

	@Override
	public boolean supportsDataMessage(MessageHeader header, ByteBuffer buffer) {
		return (MessageType.EventTableValueStates == header.getType());
	}

	@Override
	public boolean handleDataMessage(MessageHeader header, Session session, ByteBuffer buffer,
			Long configId) {
		int end = buffer.position() + (int) header.getLength();
		Date now = new Date();
		while ( buffer.hasRemaining() && buffer.position() < end ) {
			UUID uuid = readUUID(buffer);
			double value = buffer.asDoubleBuffer().get();
			buffer.position(buffer.position() + 8);
			log.trace("Parsed value event {} = {}", uuid, value);
			ValueEvent ve = new ValueEvent(uuid, configId, now, value);
			eventDao.storeEvent(ve);
		}
		return true;
	}

}
