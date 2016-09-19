/* ==================================================================
 * GetStructureFileCommandHandlerTests.java - 19/09/2016 5:07:59 PM
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

package net.solarnetwork.node.loxone.protocol.ws.handler.test;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import javax.websocket.Session;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.solarnetwork.node.loxone.dao.CategoryDao;
import net.solarnetwork.node.loxone.dao.ConfigDao;
import net.solarnetwork.node.loxone.dao.ControlDao;
import net.solarnetwork.node.loxone.dao.RoomDao;
import net.solarnetwork.node.loxone.domain.Category;
import net.solarnetwork.node.loxone.domain.Config;
import net.solarnetwork.node.loxone.domain.Control;
import net.solarnetwork.node.loxone.domain.Room;
import net.solarnetwork.node.loxone.protocol.ws.CommandType;
import net.solarnetwork.node.loxone.protocol.ws.LoxoneEndpoint;
import net.solarnetwork.node.loxone.protocol.ws.LoxoneEvents;
import net.solarnetwork.node.loxone.protocol.ws.MessageHeader;
import net.solarnetwork.node.loxone.protocol.ws.MessageType;
import net.solarnetwork.node.loxone.protocol.ws.UUIDDeserializer;
import net.solarnetwork.node.loxone.protocol.ws.UUIDSerializer;
import net.solarnetwork.node.loxone.protocol.ws.handler.GetStructureFileCommandHandler;
import net.solarnetwork.util.ObjectMapperFactoryBean;
import net.solarnetwork.util.StaticOptionalService;

/**
 * Unit tests for the {@link GetStructureFileCommandHandler} class.
 * 
 * @author matt
 * @version 1.0
 */
public class GetStructureFileCommandHandlerTests {

	private static final Long TEST_CONFIG_ID = 123L;

	private GetStructureFileCommandHandler handler;

	private ObjectMapper objectMapper;
	private EventAdmin eventAdmin;
	private Session session;
	private ConfigDao configDao;
	private CategoryDao categoryDao;
	private ControlDao controlDao;
	private RoomDao roomDao;

	@Before
	public void setup() throws Exception {
		eventAdmin = EasyMock.createMock(EventAdmin.class);
		session = EasyMock.createMock(Session.class);

		ObjectMapperFactoryBean factory = new ObjectMapperFactoryBean();
		factory.setDeserializers(Arrays.asList(new UUIDDeserializer()));
		factory.setSerializers(Arrays.asList(new UUIDSerializer()));
		factory.setFeaturesToDisable(Arrays.asList(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
		objectMapper = factory.getObject();

		configDao = EasyMock.createMock(ConfigDao.class);
		categoryDao = EasyMock.createMock(CategoryDao.class);
		controlDao = EasyMock.createMock(ControlDao.class);
		roomDao = EasyMock.createMock(RoomDao.class);

		handler = new GetStructureFileCommandHandler();
		handler.setEventAdmin(new StaticOptionalService<EventAdmin>(eventAdmin));
		handler.setObjectMapper(objectMapper);
		handler.setConfigDao(configDao);
		handler.setCategoryDao(categoryDao);
		handler.setControlDao(controlDao);
		handler.setRoomDao(roomDao);
	}

	@Test
	public void supportsCommandType() {
		Assert.assertTrue(handler.supportsCommand(CommandType.GetStructureFile));
	}

	@Test
	public void supportsTextMessageWithLastModified() throws IOException {
		String testJson = "{\"lastModified\":\"2016-09-19 08:28:15\"}";
		MessageHeader header = new MessageHeader(MessageType.BinaryFile, null, testJson.length());

		Assert.assertTrue(
				handler.supportsTextMessage(header, new StringReader(testJson), testJson.length()));
	}

	private void replayAll() {
		replay(eventAdmin, session, configDao, categoryDao, controlDao, roomDao);
	}

	private void verifyAll() {
		verify(eventAdmin, session, configDao, categoryDao, controlDao, roomDao);
	}

	@Test
	public void parseStructureFile() throws IOException {
		Resource r = new ClassPathResource("structure-file-01.json", getClass());
		File f = r.getFile();
		Reader reader = new InputStreamReader(r.getInputStream(), "UTF-8");
		MessageHeader header = new MessageHeader(MessageType.BinaryFile, null, f.length());

		// get Config ID from session
		expect(session.getUserProperties()).andReturn(
				Collections.singletonMap(LoxoneEndpoint.CONFIG_ID_USER_PROPERTY, TEST_CONFIG_ID));

		// then get the Config, but we have none yet
		expect(configDao.getConfig(TEST_CONFIG_ID)).andReturn(null);

		// now we find the categories; first delete existing...
		Capture<Config> configCapture = new Capture<>();
		Capture<Category> categoryCapture = new Capture<>();
		expect(categoryDao.deleteAllForConfig(capture(configCapture))).andReturn(0);
		categoryDao.store(capture(categoryCapture));
		expectLastCall().times(11);

		// next up, controls
		Capture<Control> controlCapture = new Capture<>();
		expect(controlDao.deleteAllForConfig(capture(configCapture))).andReturn(0);
		controlDao.store(capture(controlCapture));
		expectLastCall().times(37);

		// next up, rooms
		Capture<Room> roomCapture = new Capture<>();
		expect(roomDao.deleteAllForConfig(capture(configCapture))).andReturn(0);
		roomDao.store(capture(roomCapture));
		expectLastCall().times(4);

		// store our config
		configDao.storeConfig(capture(configCapture));

		// post "last modified date" event
		Capture<Event> eventCapture = new Capture<>();
		eventAdmin.postEvent(capture(eventCapture));

		replayAll();

		boolean result = handler.handleTextMessage(header, session, reader);

		verifyAll();

		Assert.assertTrue("Handled", result);

		// verify "last modified date" event posted
		Event event = eventCapture.getValue();
		Assert.assertNotNull("Event emitted", event);
		Assert.assertEquals("Event topic", LoxoneEvents.STRUCTURE_FILE_SAVED_EVENT, event.getTopic());
		Assert.assertEquals("Event modification date property", Long.valueOf(1474279810000L),
				event.getProperty(LoxoneEvents.EVENT_PROPERTY_DATE));

		// now every captured config should actually have the same values (and be the same object, really)
		for ( Config config : configCapture.getValues() ) {
			assertEquals("Config ID", TEST_CONFIG_ID, config.getId());
			assertNotNull("Last modified date", config.getLastModified());
			assertEquals("Last modified date", 1474279810000L, config.getLastModified().getTime());
		}

		// and verify just a sampling of the parsed data
		Category cat = categoryCapture.getValues().get(0);
		assertEquals("Category config ID", TEST_CONFIG_ID, cat.getConfigId());
		assertEquals("Category UUID", UUID.fromString("0dcf4c06-0264-37ef-ffff-a1b98ee6c71d"),
				cat.getUuid());
		assertEquals("Category name", "AIR", cat.getName());
		assertEquals("Category color", "#83B817", cat.getColor());

		Control control = controlCapture.getValues().get(0);
		assertEquals("Control config ID", TEST_CONFIG_ID, control.getConfigId());
		assertEquals("Control UUID", UUID.fromString("0e2bc5cf-01dc-684e-ffff-a1b98ee6c71d"),
				control.getUuid());
		assertEquals("Control name", "KNX power supply", control.getName());
		assertEquals("Control category", UUID.fromString("0c89ebac-0035-033a-ffff-a1b98ee6c71d"),
				control.getCategory());
		assertEquals("Control room", UUID.fromString("0cb25281-0198-64a2-ffff-a1b98ee6c71d"),
				control.getRoom());

		Room room = roomCapture.getValues().get(0);
		assertEquals("Room config ID", TEST_CONFIG_ID, room.getConfigId());
		assertEquals("Room UUID", UUID.fromString("0cb25281-0198-64a2-ffff-a1b98ee6c71d"),
				room.getUuid());
		assertEquals("Room name", "Test Cabinet", room.getName());
		assertEquals("Room color", "00000000-0000-002e-2100000000000000.svg", room.getImage());
	}

}
