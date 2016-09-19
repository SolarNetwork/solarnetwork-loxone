/* ==================================================================
 * BaseCommandHandler.java - 17/09/2016 4:37:07 PM
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
import javax.websocket.Session;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import net.solarnetwork.node.loxone.protocol.ws.CommandHandler;
import net.solarnetwork.node.loxone.protocol.ws.CommandType;
import net.solarnetwork.node.loxone.protocol.ws.LoxoneEndpoint;
import net.solarnetwork.node.loxone.protocol.ws.MessageHeader;
import net.solarnetwork.util.OptionalService;

/**
 * Supporting abstract class for {@link CommandHandler} implementations.
 * 
 * @author matt
 * @version 1.0
 */
public abstract class BaseCommandHandler implements CommandHandler {

	/** An {@link EventAdmin} to publish events with. */
	protected OptionalService<EventAdmin> eventAdmin;

	/** A class-level logger. */
	protected final Logger log = LoggerFactory.getLogger(getClass());

	/**
	 * The {@link EventAdmin} to use for publishing events.
	 * 
	 * @param eventAdmin
	 *        The service to use.
	 */
	public void setEventAdmin(OptionalService<EventAdmin> eventAdmin) {
		this.eventAdmin = eventAdmin;
	}

	@Override
	public boolean handleCommand(CommandType command, MessageHeader header, Session session,
			JsonNode tree) throws IOException {
		int status = tree.path("Code").asInt();
		if ( status != 200 ) {
			log.warn("{} command returned error status {}", command, status);
			return false;
		}
		String value = tree.path("value").textValue();
		return handleCommandValue(command, header, session, tree, value);
	}

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
	 * Internal method called by
	 * {@link #handleCommand(CommandType, MessageHeader, Session, JsonParser)}
	 * with the parsed command value.
	 * 
	 * This implementation simply returns <em>false</em>. Extending classes can
	 * override this to do something useful.
	 * 
	 * @param command
	 *        The command.
	 * @param header
	 *        The message header.
	 * @param session
	 *        The websocket session.
	 * @param tree
	 *        The JSON tree.
	 * @param value
	 *        The parsed command value.
	 * @return <em>true</em> if the command was handled.
	 * @throws IOException
	 *         if any communication error occurs
	 */
	protected boolean handleCommandValue(CommandType command, MessageHeader header, Session session,
			JsonNode tree, String value) throws IOException {
		return false;
	}

	/**
	 * Provides a default implementation that sends just the command's control
	 * value asynchronously, as long as {@link #supportsCommand(CommandType)}
	 * returns <em>true</em>.
	 */
	@Override
	public boolean sendCommand(CommandType command, Session session) throws IOException {
		if ( supportsCommand(command) ) {
			session.getBasicRemote().sendText(command.getControlValue());
			return true;
		}
		return false;
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

}
