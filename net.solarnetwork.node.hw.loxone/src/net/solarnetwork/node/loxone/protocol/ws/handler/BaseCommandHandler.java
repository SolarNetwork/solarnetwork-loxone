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
import java.util.Arrays;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import javax.websocket.Session;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.JsonNode;
import net.solarnetwork.node.loxone.domain.Config;
import net.solarnetwork.node.loxone.protocol.ws.CommandHandler;
import net.solarnetwork.node.loxone.protocol.ws.CommandType;
import net.solarnetwork.node.loxone.protocol.ws.LoxoneEndpoint;
import net.solarnetwork.node.loxone.protocol.ws.MessageHeader;
import net.solarnetwork.node.loxone.protocol.ws.SecurityHelper;
import net.solarnetwork.service.OptionalService;
import net.solarnetwork.service.RemoteServiceException;

/**
 * Supporting abstract class for {@link CommandHandler} implementations.
 *
 * @author matt
 * @version 2.0
 */
public abstract class BaseCommandHandler implements CommandHandler {

	/** An {@link EventAdmin} to publish events with. */
	protected OptionalService<EventAdmin> eventAdmin;

	/** A class-level logger. */
	protected final Logger log = LoggerFactory.getLogger(getClass());

	/**
	 * Constructor.
	 */
	public BaseCommandHandler() {
		super();
	}

	/**
	 * The {@link EventAdmin} to use for publishing events.
	 *
	 * @param eventAdmin
	 *        The service to use.
	 */
	public void setEventAdmin(OptionalService<EventAdmin> eventAdmin) {
		this.eventAdmin = eventAdmin;
	}

	/**
	 * Extract a {@code Code} response value.
	 *
	 * <p>
	 * This method also handles the lower-case version {@code code}.
	 * </p>
	 *
	 * @param tree
	 *        the response JSON
	 * @return the code
	 * @since 1.2
	 */
	protected int extractResponseCode(JsonNode tree) {
		JsonNode codeNode = tree.path("Code");
		if ( codeNode.isMissingNode() ) {
			// try lowercase... dammit Loxone!
			codeNode = tree.path("code");
		}
		return codeNode.asInt();
	}

	@Override
	public boolean handleCommand(CommandType command, MessageHeader header, Session session,
			JsonNode tree) throws IOException {
		int status = extractResponseCode(tree);
		if ( status != 200 ) {
			log.warn("{} command returned error status {}", command, status);
			return handleErrorCommand(command, header, session, tree, status);
		}
		String value = tree.path("value").textValue();
		return handleCommandValue(command, header, session, tree, value);
	}

	/**
	 * Get the session's config ID value.
	 *
	 * @param session
	 *        The current session.
	 * @return The ID, or {@literal null} if not found.
	 */
	protected Long getConfigId(Session session) {
		if ( session == null ) {
			log.warn("Session not available: cannot determine config ID");
			return null;
		}
		Object id = session.getUserProperties().get(LoxoneEndpoint.CONFIG_ID_USER_PROPERTY);
		if ( !(id instanceof Long) ) {
			log.warn("Session user property {} invalid: {}", LoxoneEndpoint.CONFIG_ID_USER_PROPERTY, id);
			return null;
		}
		return (Long) id;
	}

	/**
	 * Internal method called by
	 * {@link #handleCommand(CommandType, MessageHeader, Session, JsonNode)}
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
	 * @return {@literal true} if the command was handled.
	 * @throws IOException
	 *         if any communication error occurs
	 */
	protected boolean handleCommandValue(CommandType command, MessageHeader header, Session session,
			JsonNode tree, String value) throws IOException {
		return false;
	}

	/**
	 * Handle an error command.
	 *
	 * <p>
	 * This method is called from
	 * {@link #handleCommand(CommandType, MessageHeader, Session, JsonNode)} if
	 * the {@code Code} value is not {@literal 200}. This implementation simply
	 * returns {@literal false} but extending implementations may need to
	 * perform further processing.
	 * </p>
	 *
	 * @param command
	 *        The command.
	 * @param header
	 *        The message header.
	 * @param session
	 *        The websocket session.
	 * @param tree
	 *        The JSON tree.
	 * @param statusCode
	 *        The command status code.
	 * @return {@literal true} if the command was actually handled
	 * @since 1.1
	 */
	protected boolean handleErrorCommand(CommandType command, MessageHeader header, Session session,
			JsonNode tree, int statusCode) {
		return false;
	}

	/**
	 * Provides a default implementation that sends just the command's control
	 * value asynchronously, as long as {@link #supportsCommand(CommandType)}
	 * returns {@literal null}.
	 */
	@Override
	public Future<?> sendCommand(CommandType command, Session session, Object... args)
			throws IOException {
		if ( supportsCommand(command) ) {
			String cmdValue = command.getControlValue();
			if ( args != null && args.length > 0 ) {
				cmdValue += "/"
						+ Arrays.stream(args).map(Object::toString).collect(Collectors.joining("/"));
			}
			sendCommandText(session, command, cmdValue);
		}
		return null;
	}

	/**
	 * Send a command asynchronously, handling encryption if a
	 * {@link SecurityHelper} is available.
	 *
	 * <p>
	 * If a {@link SecurityHelper} is available via
	 * {@link #getSecurityHelper(Session)}, then the command will be encrypted
	 * using {@link SecurityHelper#encryptCommand(CommandType, String)}.
	 * </p>
	 *
	 * <p>
	 * <b>Note</b> that the {@code command} value is used for logic purposes
	 * only; it will <b>not</b> be prepended to {@code text} before sending.
	 * </p>
	 *
	 * @param session
	 *        the session
	 * @param type
	 *        an optional command type, or {@literal null} if no type
	 * @param command
	 *        the command to send
	 * @throws IOException
	 *         if any IO error occurs
	 * @since 1.2
	 */
	protected void sendCommandText(Session session, CommandType type, String command)
			throws IOException {
		if ( session == null ) {
			throw new RemoteServiceException(
					"No session available: cannot send " + type + " command [" + command + "]");
		}
		SecurityHelper helper = getSecurityHelper(session);
		String cmdToSend = command;
		if ( helper != null ) {
			cmdToSend = helper.encryptCommand(type, command);
		}
		if ( log.isDebugEnabled() ) {
			if ( cmdToSend.equals(command) ) {
				log.debug("{} sending command: {}", Config.idToExternalForm(getConfigId(session)),
						command);
			} else {
				log.debug("{} sending encrypted command {}: {}",
						Config.idToExternalForm(getConfigId(session)), command, cmdToSend);
			}
		}
		session.getBasicRemote().sendText(cmdToSend);
	}

	/**
	 * Get the session {@link SecurityHelper}.
	 *
	 * <p>
	 * This method looks for a {@link SecurityHelper} on the
	 * {@link LoxoneEndpoint#SECURITY_HELPER_USER_PROPERTY} session user
	 * property.
	 * </p>
	 *
	 * @param session
	 *        the session
	 * @return the helper, or {@literal null} if none available
	 * @since 1.2
	 */
	protected SecurityHelper getSecurityHelper(Session session) {
		if ( session == null ) {
			return null;
		}
		Object helper = session.getUserProperties().get(LoxoneEndpoint.SECURITY_HELPER_USER_PROPERTY);
		return (helper instanceof SecurityHelper ? (SecurityHelper) helper : null);
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
