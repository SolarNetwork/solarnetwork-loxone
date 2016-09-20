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
import java.nio.LongBuffer;
import java.util.UUID;
import javax.websocket.Session;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.solarnetwork.node.loxone.dao.EventEntityDao;
import net.solarnetwork.node.loxone.domain.BaseEventEntity;
import net.solarnetwork.node.loxone.domain.Config;
import net.solarnetwork.node.loxone.protocol.ws.BinaryFileHandler;
import net.solarnetwork.node.loxone.protocol.ws.LoxoneEndpoint;
import net.solarnetwork.node.loxone.protocol.ws.MessageHeader;
import net.solarnetwork.util.OptionalService;

/**
 * Base class for binary event data handlers.
 * 
 * @author matt
 * @version 1.0
 */
public abstract class BaseEventBinaryFileHandler<T extends BaseEventEntity>
		implements BinaryFileHandler {

	/** The DAO to use. */
	protected EventEntityDao<T> eventDao;

	/** An optional {@link EventAdmin} service to use. */
	protected OptionalService<EventAdmin> eventAdmin;

	/** A class-level logger. */
	protected final Logger log = LoggerFactory.getLogger(getClass());

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
		LongBuffer buf = buffer.order(ByteOrder.BIG_ENDIAN).asLongBuffer();
		UUID uuid = new UUID(buf.get(), buf.get());
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.position(buffer.position() + 16);
		return uuid;
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
	 * Set an {@link EventAdmin} to publish events with.
	 * 
	 * @param eventAdmin
	 *        The event admin service.
	 */
	public void setEventAdmin(OptionalService<EventAdmin> eventAdmin) {
		this.eventAdmin = eventAdmin;
	}

}
