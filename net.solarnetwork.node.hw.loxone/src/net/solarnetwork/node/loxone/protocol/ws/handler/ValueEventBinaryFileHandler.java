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
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.websocket.Session;
import org.osgi.service.event.Event;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import net.solarnetwork.node.loxone.domain.Config;
import net.solarnetwork.node.loxone.domain.ValueEvent;
import net.solarnetwork.node.loxone.protocol.ws.BinaryFileHandler;
import net.solarnetwork.node.loxone.protocol.ws.LoxoneEvents;
import net.solarnetwork.node.loxone.protocol.ws.MessageHeader;
import net.solarnetwork.node.loxone.protocol.ws.MessageType;

/**
 * {@link BinaryFileHandler} for value-type event binary messages.
 * 
 * @author matt
 * @version 2.0
 */
public class ValueEventBinaryFileHandler extends BaseEventBinaryFileHandler<ValueEvent> {

	/**
	 * Event broadcast with the last modification date of the Loxone structure
	 * file after it has been successfully saved. The
	 * {@link LoxoneEvents#EVENT_PROPERTY_CONFIG_ID} and
	 * {@link LoxoneEvents#EVENT_PROPERTY_DATE} properties will be available.
	 * 
	 * @since 1.1
	 */
	public static final String VALUE_EVENTS_UPDATED_EVENT = "net/solarnetwork/node/loxone/VALUE_EVENTS_UPDATED";

	/**
	 * The Event property used to convey a {@code Collection} of
	 * {@link ValueEvent} instances associated with the event.
	 * 
	 * @since 1.1
	 */
	public static final String EVENT_PROPERTY_VALUE_EVENTS = "valueEvents";

	private boolean sendValueEventsUpdatedEvents = false;
	private boolean ignoreUnchangedValues = true;

	@Override
	public boolean supportsDataMessage(MessageHeader header, ByteBuffer buffer) {
		return (header != null && MessageType.EventTableValueStates == header.getType());
	}

	// wrap super with @Transactional
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	@Override
	public boolean handleDataMessage(MessageHeader header, Session session, ByteBuffer buffer) {
		return super.handleDataMessage(header, session, buffer);
	}

	@Override
	protected boolean handleDataMessage(MessageHeader header, Session session, ByteBuffer buffer,
			Long configId) {
		final int end = buffer.position() + (int) header.getLength();
		final Instant now = Instant.now();
		final List<ValueEvent> updated = new ArrayList<>();
		while ( buffer.hasRemaining() && buffer.position() < end ) {
			UUID uuid = readUUID(buffer);
			double value = buffer.asDoubleBuffer().get();
			buffer.position(buffer.position() + 8);
			log.trace("Parsed value event {} = {}", uuid, value);

			// check existing value first, so we don't emit an event for a value that has not changed
			ValueEvent existing = (ignoreUnchangedValues ? eventDao.loadEvent(configId, uuid) : null);
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
			log.trace("Got updated value events: {}", updated);
			if ( sendValueEventsUpdatedEvents ) {
				Map<String, Object> eventProps = new LinkedHashMap<>(2);
				eventProps.put(LoxoneEvents.EVENT_PROPERTY_CONFIG_ID, configId);
				eventProps.put(LoxoneEvents.EVENT_PROPERTY_DATE, now.toEpochMilli());
				eventProps.put(EVENT_PROPERTY_VALUE_EVENTS, updated);
				postEvent(new Event(VALUE_EVENTS_UPDATED_EVENT, eventProps));
			}
			String dest = String.format(LoxoneEvents.VALUE_EVENT_MESSAGE_TOPIC,
					Config.idToExternalForm(configId));
			postMessage(dest, updated);
		}

		return true;
	}

	/**
	 * Toggle the sending of {@link #VALUE_EVENTS_UPDATED_EVENT} events.
	 * 
	 * <p>
	 * This property defaults to {@literal false}.
	 * </p>
	 * 
	 * @param sendValueEventsUpdatedEvents
	 *        {@literal true} to enable sending value events updated events
	 * @since 1.1
	 */
	public void setSendValueEventsUpdatedEvents(boolean sendValueEventsUpdatedEvents) {
		this.sendValueEventsUpdatedEvents = sendValueEventsUpdatedEvents;
	}

	/**
	 * Toggle the flag to ignore unchanged value event values.
	 * 
	 * <p>
	 * When {@literal true} (the default) then if the value of the control has
	 * not changed from the last seen value for the same control, no updated
	 * event will be generated and the value will not be re-saved. When
	 * {@literal false} then all value events will be processed regardless of
	 * their value, which can be useful if dealing with devices like RFID card
	 * readers where the event must always be processed.
	 * </p>
	 * 
	 * @param ignoreUnchangedValues
	 *        {@literal true} to ignore processing unchanged value events;
	 *        defaults to {@literal true}
	 * @since 1.2
	 */
	public void setIgnoreUnchangedValues(boolean ignoreUnchangedValues) {
		this.ignoreUnchangedValues = ignoreUnchangedValues;
	}

}
