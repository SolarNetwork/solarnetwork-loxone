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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.websocket.Session;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import net.solarnetwork.node.loxone.domain.ValueEvent;
import net.solarnetwork.node.loxone.impl.ValueEventDatumGenerator;
import net.solarnetwork.node.loxone.protocol.ws.BinaryFileHandler;
import net.solarnetwork.node.loxone.protocol.ws.LoxoneEvents;
import net.solarnetwork.node.loxone.protocol.ws.MessageHeader;
import net.solarnetwork.node.loxone.protocol.ws.MessageType;

/**
 * {@link BinaryFileHandler} for value-type event binary messages.
 * 
 * @author matt
 * @version 1.0
 */
public class ValueEventBinaryFileHandler extends BaseEventBinaryFileHandler<ValueEvent> {

	private ValueEventDatumGenerator datumGenerator;

	@Override
	public boolean supportsDataMessage(MessageHeader header, ByteBuffer buffer) {
		return (MessageType.EventTableValueStates == header.getType());
	}

	// wrap super with @Transactional
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	@Override
	public boolean handleDataMessage(MessageHeader header, Session session, ByteBuffer buffer) {
		return super.handleDataMessage(header, session, buffer);
	}

	@Override
	public boolean handleDataMessage(MessageHeader header, Session session, ByteBuffer buffer,
			Long configId) {
		int end = buffer.position() + (int) header.getLength();
		Date now = new Date();
		List<ValueEvent> updated = new ArrayList<>();
		while ( buffer.hasRemaining() && buffer.position() < end ) {
			UUID uuid = readUUID(buffer);
			double value = buffer.asDoubleBuffer().get();
			buffer.position(buffer.position() + 8);
			log.trace("Parsed value event {} = {}", uuid, value);

			// check existing value first, so we don't emit an event for a value that has not changed
			ValueEvent existing = eventDao.loadEvent(configId, uuid);
			if ( existing != null && Double.compare(existing.getValue(), value) == 0 ) {
				log.trace("ValueEvent {} unchanged: {}", uuid, value);
			} else {
				ValueEvent ve = new ValueEvent(uuid, configId, now, value);
				eventDao.storeEvent(ve);
				updated.add(ve);
			}
		}

		// post updated values to message channel
		if ( !updated.isEmpty() ) {
			String dest = String.format(LoxoneEvents.VALUE_EVENT_MESSAGE_TOPIC, configId);
			postMessage(dest, updated);
		}

		// translate into Datum
		if ( datumGenerator != null ) {
			int result = datumGenerator.handleValueEvents(updated, now);
			log.debug("Saved {} value events as datum", result);
		}

		return true;
	}

	/**
	 * Set a generator to translate value events into {@code Datum} instances.
	 * 
	 * @param datumGenerator
	 *        The generator to use.
	 */
	public void setDatumGenerator(ValueEventDatumGenerator datumGenerator) {
		this.datumGenerator = datumGenerator;
	}

}
