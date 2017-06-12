/* ==================================================================
 * IoControlCommandHandler.java - 12/06/2017 9:14:48 AM
 * 
 * Copyright 2017 SolarNetwork.net Dev Team
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import javax.websocket.Session;
import com.fasterxml.jackson.databind.JsonNode;
import net.solarnetwork.node.loxone.domain.UUIDDeserializer;
import net.solarnetwork.node.loxone.domain.UUIDSerializer;
import net.solarnetwork.node.loxone.domain.ValueEvent;
import net.solarnetwork.node.loxone.protocol.ws.CommandType;
import net.solarnetwork.node.loxone.protocol.ws.MessageHeader;

/**
 * Handler for {@code IoControl} messages.
 * 
 * @author matt
 * @version 1.0
 * @since 1.1
 */
public class IoControlCommandHandler extends QueuedCommandHandler<String, List<ValueEvent>> {

	@Override
	public boolean supportsCommand(CommandType command) {
		return (command == CommandType.IoControl);
	}

	@Override
	public Future<?> sendCommand(CommandType command, Session session, Object... args)
			throws IOException {
		// we need two arguments: the control UUID and the state to set
		Long configId = getConfigId(session);
		if ( supportsCommand(command) && args != null && args.length > 1 && args[0] instanceof UUID
				&& args[1] != null ) {
			log.trace("Setting control {} to {}", args[0], args[1]);
			String key = CommandType.IoControl.getControlValue() + "/"
					+ UUIDSerializer.serializeUUID((UUID) args[0]);
			String cmd = key + "/" + args[1];
			return sendTextForKey(session, configId, key, cmd);
		}
		return null;
	}

	@Override
	protected boolean handleCommandValue(CommandType command, MessageHeader header, Session session,
			JsonNode tree, String value) throws IOException {
		String control = tree.path("control").textValue();
		Matcher m = CommandType.IoControl.getMatcher(control);
		Long configId = getConfigId(session);
		List<ValueEvent> result = null;
		if ( m.matches() ) {
			log.debug("Set control {} value", control);
			UUID controlUuid = UUIDDeserializer.deserializeUUID(m.group(1));
			if ( tree.get("v1") != null ) {
				// there is a list of results, and value is the number of results and there are uX, vX attributes
				int numResults = Integer.parseInt(value);
				result = new ArrayList<>(numResults);
				for ( int i = 1; i <= numResults; i++ ) {
					String uuid = tree.path("u" + i).textValue();
					String val = tree.path("v" + i).textValue();
					if ( uuid != null && val != null ) {
						result.add(new ValueEvent(UUIDDeserializer.deserializeUUID(uuid), configId,
								Double.parseDouble(val)));
					}
				}
			} else {
				// a single result; value is the actual value
				result = Arrays.asList(new ValueEvent(controlUuid, configId, Double.parseDouble(value)));
			}
		}
		handleNextResult(configId, result);
		return true;
	}

}
