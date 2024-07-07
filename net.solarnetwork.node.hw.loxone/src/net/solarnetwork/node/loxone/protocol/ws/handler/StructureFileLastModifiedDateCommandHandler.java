/* ==================================================================
 * StructureFileLastModifiedDateCommandHandler.java - 17/09/2016 4:34:25 PM
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import javax.websocket.Session;
import org.osgi.service.event.Event;
import com.fasterxml.jackson.databind.JsonNode;
import net.solarnetwork.node.loxone.protocol.ws.CommandHandler;
import net.solarnetwork.node.loxone.protocol.ws.CommandType;
import net.solarnetwork.node.loxone.protocol.ws.LoxoneEvents;
import net.solarnetwork.node.loxone.protocol.ws.MessageHeader;

/**
 * {@link CommandHandler} for {@link CommandType#StructureFileLastModifiedDate}.
 *
 * @author matt
 * @version 1.0
 */
public class StructureFileLastModifiedDateCommandHandler extends BaseCommandHandler {

	private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss"; //"2016-09-19 08:28:15";

	/**
	 * Constructor.
	 */
	public StructureFileLastModifiedDateCommandHandler() {
		super();
	}

	@Override
	public boolean supportsCommand(CommandType command) {
		return (command == CommandType.StructureFileLastModifiedDate);
	}

	@Override
	protected boolean handleCommandValue(CommandType command, MessageHeader header, Session session,
			JsonNode tree, String value) throws IOException {
		if ( value != null ) {
			SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
			sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
			try {
				Date date = sdf.parse(value);
				Map<String, Object> props = new HashMap<>(2);
				props.put(LoxoneEvents.EVENT_PROPERTY_CONFIG_ID, getConfigId(session));
				props.put(LoxoneEvents.EVENT_PROPERTY_DATE, date.getTime());
				Event e = new Event(LoxoneEvents.STRUCTURE_FILE_MODIFICATION_DATE_EVENT, props);
				postEvent(e);
			} catch ( ParseException e ) {
				log.error("Error parsing structure file last modified date value [{}]: {}", value,
						e.getMessage());
			}
			return true;
		}
		return false;
	}

}
