/* ==================================================================
 * BaseEventBinaryFileHandler.java - 20/09/2016 4:59:03 PM
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
import java.util.Map;
import java.util.UUID;
import javax.websocket.Session;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.core.MessageSendingOperations;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.support.GenericMessage;
import net.solarnetwork.domain.Result;
import net.solarnetwork.node.loxone.dao.EventEntityDao;
import net.solarnetwork.node.loxone.domain.BaseEventEntity;
import net.solarnetwork.node.loxone.domain.Config;
import net.solarnetwork.node.loxone.protocol.ws.BinaryFileHandler;
import net.solarnetwork.node.loxone.protocol.ws.LoxoneEndpoint;
import net.solarnetwork.node.loxone.protocol.ws.MessageHeader;
import net.solarnetwork.service.OptionalService;

/**
 * Base class for binary event data handlers.
 *
 * @param <T>
 *        the entity type
 * @author matt
 * @version 2.0
 */
public abstract class BaseEventBinaryFileHandler<T extends BaseEventEntity>
		implements BinaryFileHandler {

	/** The DAO to use. */
	protected EventEntityDao<T> eventDao;

	/** An optional {@link EventAdmin} service to use. */
	protected OptionalService<EventAdmin> eventAdmin;

	/** An optional {@link SimpMessageSendingOperations} service to use. */
	protected OptionalService<SimpMessageSendingOperations> messageSendingOps;

	/** A class-level logger. */
	protected final Logger log = LoggerFactory.getLogger(getClass());

	/**
	 * Constructor.
	 */
	public BaseEventBinaryFileHandler() {
		super();
	}

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

	/**
	 * This method forces the byte order to {@code LITTLE_ENDIAN} and then calls
	 * {@link #handleDataMessage(MessageHeader, Session, ByteBuffer, Long)}.
	 */
	@Override
	public boolean handleDataMessage(MessageHeader header, Session session, ByteBuffer buffer) {
		if ( buffer.order() != ByteOrder.LITTLE_ENDIAN ) {
			buffer.order(ByteOrder.LITTLE_ENDIAN);
		}
		Long configId = getConfigId(session);
		if ( configId == null ) {
			return false;
		}
		return handleDataMessage(header, session, buffer, configId);
	}

	/**
	 * Handle the data message for the session's config ID.
	 *
	 * @param header
	 *        The message header.
	 * @param session
	 *        The websocket session.
	 * @param buffer
	 *        The byte buffer with the data to process.
	 * @param configId
	 *        The {@link Config} ID associated with the session.
	 * @return <em>true</em> if the command was handled.
	 */
	protected abstract boolean handleDataMessage(MessageHeader header, Session session,
			ByteBuffer buffer, Long configId);

	/**
	 * Get the session's config ID value.
	 *
	 * @param session
	 *        The current session.
	 * @return The ID, or <em>null</em> if not found.
	 */
	protected Long getConfigId(Session session) {
		Object id = session.getUserProperties().get(LoxoneEndpoint.CONFIG_ID_USER_PROPERTY);
		if ( !(id instanceof Long) ) {
			log.warn("Session user property {} invalid: {}", LoxoneEndpoint.CONFIG_ID_USER_PROPERTY, id);
			return null;
		}
		return (Long) id;
	}

	/**
	 * Read a UUID from a buffer.
	 *
	 * This method will force the buffer into {@code LITTLE_ENDIAN} order before
	 * returning.
	 *
	 * @param buffer
	 *        The buffer to use.
	 * @return The parsed UUID.
	 */
	protected UUID readUUID(ByteBuffer buffer) {
		ByteBuffer buf = buffer.order(ByteOrder.LITTLE_ENDIAN);
		long d1 = Integer.toUnsignedLong(buf.getInt());
		long d2 = Short.toUnsignedLong(buf.getShort());
		long d3 = Short.toUnsignedLong(buf.getShort());
		UUID uuid = new UUID(((d1 << 32) | (d2 << 16) | d3), buf.order(ByteOrder.BIG_ENDIAN).getLong());
		buf.order(ByteOrder.LITTLE_ENDIAN);
		return uuid;
	}

	/**
	 * Post an event. Will silently ignore the event if no {@link EventAdmin} is
	 * available.
	 *
	 * @param e
	 *        The event to post.
	 */
	protected void postEvent(Event e) {
		EventAdmin service = (eventAdmin != null ? eventAdmin.service() : null);
		if ( service != null ) {
			service.postEvent(e);
		}
	}

	/**
	 * Post a message without any headers, converting it first.
	 *
	 * <p>
	 * Will silently ignore the event if no {@link MessageSendingOperations} is
	 * available.
	 * </p>
	 *
	 * @param dest
	 *        The destination to post to.
	 * @param body
	 *        The message body to post. This will be wrapped in a {@link Result}
	 *        object if it is not one already.
	 * @see #postMessage(String, Object, Map, boolean)
	 */
	protected void postMessage(String dest, Object body) {
		postMessage(dest, body, null, true);
	}

	/**
	 * Post a message. Will silently ignore the event if no
	 * {@link MessageSendingOperations} is available.
	 *
	 * <p>
	 * If {@code convert} is {@literal true} the message will be sent via the
	 * {@link MessageSendingOperations#convertAndSend(Object, Object, Map)}
	 * method. Otherwise the
	 * {@link MessageSendingOperations#send(Object, Message)} method will be
	 * used to send the body as-is.
	 * </p>
	 *
	 * @param dest
	 *        The destination to post to.
	 * @param body
	 *        The message body to post. If {@code convert} is {@literal true}
	 *        then this will be wrapped in a {@link Result} object if it is not
	 *        one already.
	 * @param headers
	 *        an optional set of message headers to include
	 * @param convert
	 *        {@literal true} to convert the message before sending,
	 *        {@literal false} to send without any conversion
	 * @since 1.1
	 */
	protected void postMessage(String dest, Object body, Map<String, Object> headers, boolean convert) {
		SimpMessageSendingOperations ops = (messageSendingOps != null ? messageSendingOps.service()
				: null);
		if ( ops == null ) {
			return;
		}
		if ( convert ) {
			Result<?> r = (body instanceof Result ? (Result<?>) body : Result.result(body));
			ops.convertAndSend(dest, r, headers);
		} else {
			Message<Object> msg = new GenericMessage<Object>(body, headers);
			ops.send(dest, msg);
		}
	}

	/**
	 * Set the DAO to use.
	 *
	 * @param eventDao
	 *        The DAO.
	 */
	public void setEventDao(EventEntityDao<T> eventDao) {
		this.eventDao = eventDao;
	}

	/**
	 * Work-around for OSGi Blueprint failures to populate via
	 * {@link #setEventDao(EventEntityDao)}. Calls
	 * {@link #setEventDao(EventEntityDao)}.
	 *
	 * @param o
	 *        The DAO to set.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void setGenericEventDao(Object o) {
		if ( o instanceof EventEntityDao ) {
			setEventDao((EventEntityDao) o);
		}
	}

	/**
	 * Set an {@link EventAdmin} to publish events with.
	 *
	 * @param eventAdmin
	 *        The event admin service.
	 */
	public void setEventAdmin(OptionalService<EventAdmin> eventAdmin) {
		this.eventAdmin = eventAdmin;
	}

	/**
	 * Set a {@link SimpMessageSendingOperations} to publish messages with.
	 *
	 * @param messagingOps
	 *        The service to use.
	 */
	public void setMessageSendingOps(OptionalService<SimpMessageSendingOperations> messagingOps) {
		this.messageSendingOps = messagingOps;
	}

}
