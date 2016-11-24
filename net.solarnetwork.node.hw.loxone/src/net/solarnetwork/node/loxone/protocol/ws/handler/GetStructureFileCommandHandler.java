/* ==================================================================
 * GetStructureFileCommandHandler.java - 17/09/2016 5:43:36 PM
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import javax.websocket.Session;
import org.osgi.service.event.Event;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.solarnetwork.node.loxone.dao.CategoryDao;
import net.solarnetwork.node.loxone.dao.ConfigDao;
import net.solarnetwork.node.loxone.dao.ControlDao;
import net.solarnetwork.node.loxone.dao.RoomDao;
import net.solarnetwork.node.loxone.domain.Category;
import net.solarnetwork.node.loxone.domain.Config;
import net.solarnetwork.node.loxone.domain.Control;
import net.solarnetwork.node.loxone.domain.Room;
import net.solarnetwork.node.loxone.protocol.ws.BinaryFileHandler;
import net.solarnetwork.node.loxone.protocol.ws.CommandType;
import net.solarnetwork.node.loxone.protocol.ws.LoxoneEvents;
import net.solarnetwork.node.loxone.protocol.ws.MessageHeader;

/**
 * Handle the Loxone structure file.
 * 
 * @author matt
 * @version 1.0
 */
public class GetStructureFileCommandHandler extends BaseCommandHandler implements BinaryFileHandler {

	private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss"; //"2016-09-19 08:28:15";

	private ObjectMapper objectMapper;
	private ConfigDao configDao;
	private CategoryDao categoryDao;
	private ControlDao controlDao;
	private RoomDao roomDao;

	@Override
	public boolean supportsCommand(CommandType command) {
		return (command == CommandType.GetStructureFile);
	}

	@Override
	public boolean handleCommand(CommandType command, MessageHeader header, Session session,
			JsonNode tree) throws IOException {
		// we don't get a command response, we get a binary file response
		return false;
	}

	@Override
	public boolean supportsDataMessage(MessageHeader header, ByteBuffer buffer) {
		return false;
	}

	@Override
	public boolean handleDataMessage(MessageHeader header, Session session, ByteBuffer buffer) {
		return false;
	}

	@Override
	public boolean supportsTextMessage(MessageHeader header, Reader reader, int limit)
			throws IOException {
		// read our limit to inspect what we have
		char[] buf = new char[limit];
		int count = reader.read(buf, 0, limit);
		String s = new String(buf, 0, count);
		return s.contains("\"lastModified\"");
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	@Override
	public boolean handleTextMessage(MessageHeader header, Session session, Reader reader)
			throws IOException {
		Long configId = getConfigId(session);
		if ( configId == null ) {
			return false;
		}
		Config config = configDao.getConfig(configId);
		if ( config == null ) {
			config = new Config(configId);
		}
		JsonNode json = objectMapper.readTree(reader);
		for ( Iterator<Entry<String, JsonNode>> itr = json.fields(); itr.hasNext(); ) {
			Entry<String, JsonNode> entry = itr.next();
			if ( "lastModified".equals(entry.getKey()) ) {
				SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
				sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
				try {
					Date d = sdf.parse(entry.getValue().textValue());
					config = config.withLastModified(d);
				} catch ( ParseException e ) {
					log.warn("Error parsing last modified date [{}]: {}", entry.getValue().asText(),
							e.getMessage());
				}
			} else if ( "cats".equals(entry.getKey()) ) {
				categoryDao.deleteAllForConfig(configId);
				for ( JsonNode node : entry.getValue() ) {
					Category category = objectMapper.treeToValue(node, Category.class);
					category.setConfigId(config.getId());
					if ( category.isValid() ) {
						categoryDao.store(category);
					} else {
						log.warn("Ignoring invalid category {}", category.getUuid());
					}
				}
			} else if ( "controls".equals(entry.getKey()) ) {
				controlDao.deleteAllForConfig(configId);
				for ( JsonNode node : entry.getValue() ) {
					Control control = objectMapper.treeToValue(node, Control.class);
					control.setConfigId(config.getId());
					if ( control.isValid() ) {
						controlDao.store(control);
					} else {
						log.warn("Ignoring invalid control {}", control.getUuid());
					}
				}
			} else if ( "rooms".equals(entry.getKey()) ) {
				roomDao.deleteAllForConfig(configId);
				for ( JsonNode node : entry.getValue() ) {
					Room room = objectMapper.treeToValue(node, Room.class);
					room.setConfigId(config.getId());
					if ( room.isValid() ) {
						roomDao.store(room);
					} else {
						log.warn("Ignoring invalid room {}", room.getUuid());
					}
				}
			}
		}
		configDao.storeConfig(config);

		// and finally, post our last modified event date
		Map<String, Object> props = new HashMap<>(2);
		props.put(LoxoneEvents.EVENT_PROPERTY_CONFIG_ID, configId);
		props.put(LoxoneEvents.EVENT_PROPERTY_DATE, config.getLastModified().getTime());
		Event e = new Event(LoxoneEvents.STRUCTURE_FILE_SAVED_EVENT, props);
		postEvent(e);

		return true;
	}

	public void setCategoryDao(CategoryDao categoryDao) {
		this.categoryDao = categoryDao;
	}

	public void setControlDao(ControlDao controlDao) {
		this.controlDao = controlDao;
	}

	public void setRoomDao(RoomDao roomDao) {
		this.roomDao = roomDao;
	}

	public void setConfigDao(ConfigDao configDao) {
		this.configDao = configDao;
	}

	public void setObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

}
