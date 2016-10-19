/* ==================================================================
 * TextEventBinaryFileHandlerTests.java - 20/09/2016 5:51:46 PM
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

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.Collections;
import javax.websocket.Session;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osgi.service.event.EventAdmin;
import org.springframework.util.FileCopyUtils;
import net.solarnetwork.node.loxone.protocol.ws.LoxoneEndpoint;
import net.solarnetwork.node.loxone.protocol.ws.MessageHeader;
import net.solarnetwork.node.loxone.protocol.ws.MessageType;
import net.solarnetwork.node.loxone.protocol.ws.handler.TextEventBinaryFileHandler;
import net.solarnetwork.util.StaticOptionalService;

/**
 * Unit tests for the {@link TextEventBinaryFileHandler} class.
 * 
 * @author matt
 * @version 1.0
 */
public class TextEventBinaryFileHandlerTests {

	private static final Long TEST_CONFIG_ID = 123L;

	private TextEventBinaryFileHandler handler;

	private Session session;
	private EventAdmin eventAdmin;

	@Before
	public void setup() {
		session = EasyMock.createMock(Session.class);
		eventAdmin = EasyMock.createMock(EventAdmin.class);
		handler = new TextEventBinaryFileHandler();
		handler.setEventAdmin(new StaticOptionalService<EventAdmin>(eventAdmin));
	}

	@Test
	public void supportsViaMessageType() {
		MessageHeader header = new MessageHeader(MessageType.EventTableTextStates, null, 0);
		Assert.assertTrue("Supported type", handler.supportsDataMessage(header, null));
	}

	@Test
	public void parseEventTable() throws IOException {
		byte[] data = FileCopyUtils.copyToByteArray(
				Base64.getMimeDecoder().wrap(getClass().getResourceAsStream("text-events-01.b64")));
		Assert.assertEquals("Data length", 936, data.length);
		ByteBuffer buffer = ByteBuffer.wrap(data);
		MessageHeader header = new MessageHeader(MessageType.EventTableTextStates, null, data.length);

		// get Config ID from session
		expect(session.getUserProperties()).andReturn(
				Collections.singletonMap(LoxoneEndpoint.CONFIG_ID_USER_PROPERTY, TEST_CONFIG_ID));

		replay(session, eventAdmin);

		boolean handled = handler.handleDataMessage(header, session, buffer);

		verify(session, eventAdmin);

		Assert.assertTrue("Handled", handled);
	}

}
