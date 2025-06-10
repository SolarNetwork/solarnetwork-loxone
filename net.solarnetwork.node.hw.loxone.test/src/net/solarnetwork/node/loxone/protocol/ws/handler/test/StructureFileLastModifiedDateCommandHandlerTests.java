/* ==================================================================
 * StructureFileLastModifiedDateCommandHandlerTests.java - 19/09/2016 9:52:13 AM
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
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import java.io.IOException;
import java.util.Collections;
import jakarta.websocket.Session;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.solarnetwork.node.loxone.protocol.ws.CommandType;
import net.solarnetwork.node.loxone.protocol.ws.LoxoneEndpoint;
import net.solarnetwork.node.loxone.protocol.ws.LoxoneEvents;
import net.solarnetwork.node.loxone.protocol.ws.MessageHeader;
import net.solarnetwork.node.loxone.protocol.ws.MessageType;
import net.solarnetwork.node.loxone.protocol.ws.handler.StructureFileLastModifiedDateCommandHandler;
import net.solarnetwork.service.StaticOptionalService;

/**
 * Unit tests for the {@link StructureFileLastModifiedDateCommandHandler} class.
 * 
 * @author matt
 * @version 2.0
 */
public class StructureFileLastModifiedDateCommandHandlerTests {

	private static final Long TEST_CONFIG_ID = 123L;

	private StructureFileLastModifiedDateCommandHandler handler;

	private final ObjectMapper objectMapper = new ObjectMapper();
	private EventAdmin eventAdmin;
	private Session session;

	@Before
	public void setup() {
		eventAdmin = EasyMock.createMock(EventAdmin.class);
		session = EasyMock.createMock(Session.class);

		handler = new StructureFileLastModifiedDateCommandHandler();
		handler.setEventAdmin(new StaticOptionalService<>(eventAdmin));
	}

	@Test
	public void supportsCommandType() {
		Assert.assertTrue(handler.supportsCommand(CommandType.StructureFileLastModifiedDate));
	}

	@Test
	public void parseDate() throws IOException {
		// get Config ID from session
		expect(session.getUserProperties()).andReturn(
				Collections.singletonMap(LoxoneEndpoint.CONFIG_ID_USER_PROPERTY, TEST_CONFIG_ID));

		Capture<Event> eventCapture = Capture.newInstance();
		eventAdmin.postEvent(capture(eventCapture));

		final String testDate = "2016-09-19 10:10:10";
		final JsonNode tree = objectMapper
				.readTree("{\"control\":\"jdev/sps/LoxAPPversion3\",\"value\":\"" + testDate
						+ "\",\"Code\":\"200\"}");

		MessageHeader header = new MessageHeader(MessageType.TextMessage, null, testDate.length());

		replay(eventAdmin, session);

		boolean result = handler.handleCommand(CommandType.StructureFileLastModifiedDate, header,
				session, tree);

		verify(eventAdmin, session);

		Assert.assertTrue("Handler success", result);
		Event event = eventCapture.getValue();
		Assert.assertNotNull("Event emitted", event);
		Assert.assertEquals("Event topic", LoxoneEvents.STRUCTURE_FILE_MODIFICATION_DATE_EVENT,
				event.getTopic());
		Assert.assertEquals("Event config ID property", TEST_CONFIG_ID,
				event.getProperty(LoxoneEvents.EVENT_PROPERTY_CONFIG_ID));
		Assert.assertEquals("Event modification date property", Long.valueOf(1474279810000L),
				event.getProperty(LoxoneEvents.EVENT_PROPERTY_DATE));

	}

}
