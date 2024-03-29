/* ==================================================================
 * ValueEventBinaryFileHandlerTests.java - 20/09/2016 2:31:08 PM
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
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.Collections;
import java.util.UUID;
import javax.websocket.Session;
import org.easymock.Capture;
import org.easymock.CaptureType;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osgi.service.event.EventAdmin;
import org.springframework.util.FileCopyUtils;
import net.solarnetwork.node.loxone.dao.ValueEventDao;
import net.solarnetwork.node.loxone.domain.ValueEvent;
import net.solarnetwork.node.loxone.protocol.ws.LoxoneEndpoint;
import net.solarnetwork.node.loxone.protocol.ws.MessageHeader;
import net.solarnetwork.node.loxone.protocol.ws.MessageType;
import net.solarnetwork.node.loxone.protocol.ws.handler.ValueEventBinaryFileHandler;
import net.solarnetwork.service.StaticOptionalService;

/**
 * Unit tests for the {@link ValueEventBinaryFileHandler} class.
 * 
 * @author matt
 * @version 2.0
 */
public class ValueEventBinaryFileHandlerTests {

	private static final Long TEST_CONFIG_ID = 123L;

	private ValueEventBinaryFileHandler handler;

	private Session session;
	private EventAdmin eventAdmin;
	private ValueEventDao valueEventDao;

	@Before
	public void setup() {
		session = EasyMock.createMock(Session.class);
		eventAdmin = EasyMock.createMock(EventAdmin.class);
		valueEventDao = EasyMock.createMock(ValueEventDao.class);
		handler = new ValueEventBinaryFileHandler();
		handler.setEventDao(valueEventDao);
		handler.setEventAdmin(new StaticOptionalService<>(eventAdmin));
	}

	@Test
	public void supportsViaMessageType() {
		MessageHeader header = new MessageHeader(MessageType.EventTableValueStates, null, 0);
		Assert.assertTrue("Supported type", handler.supportsDataMessage(header, null));
	}

	@Test
	public void parseEventTable() throws IOException {
		byte[] data = FileCopyUtils.copyToByteArray(
				Base64.getMimeDecoder().wrap(getClass().getResourceAsStream("value-events-01.b64")));
		Assert.assertEquals("Data length", 2664, data.length);
		ByteBuffer buffer = ByteBuffer.wrap(data);
		MessageHeader header = new MessageHeader(MessageType.EventTableValueStates, null, data.length);

		// get Config ID from session
		expect(session.getUserProperties()).andReturn(
				Collections.singletonMap(LoxoneEndpoint.CONFIG_ID_USER_PROPERTY, TEST_CONFIG_ID));

		// should query for existing data, to only store updated values
		expect(valueEventDao.loadEvent(EasyMock.anyObject(Long.class), EasyMock.anyObject(UUID.class)))
				.andReturn(null).times(111);

		Capture<ValueEvent> valueEventCapture = Capture.newInstance(CaptureType.ALL);
		valueEventDao.storeEvent(capture(valueEventCapture));
		expectLastCall().times(111);

		replay(session, eventAdmin, valueEventDao);

		boolean handled = handler.handleDataMessage(header, session, buffer);

		verify(session, eventAdmin, valueEventDao);

		assertThat("Handled", handled, equalTo(true));

		// test just the first few values
		Object[][] expectedEventData = { { "0c89ebac-0021-02be-ffff-a1b98ee6c71d", 4.0 },
				{ "0c89ebac-0025-02d0-ffff-a1b98ee6c71d", 376.0 },
				{ "0c89ebac-0026-02d3-ffff-a1b98ee6c71d", 1093.0 }, };

		int i = 0;
		for ( Object[] eventData : expectedEventData ) {
			ValueEvent event = valueEventCapture.getValues().get(i++);
			assertThat("ValueEvent created date " + i, event.getCreated(), notNullValue());
			assertThat("ValueEvent config ID " + i, event.getConfigId(), equalTo(TEST_CONFIG_ID));
			assertThat("ValueEvent UUID " + i, event.getUuid(),
					equalTo(UUID.fromString((String) eventData[0])));
			assertThat("ValueEvent value" + i, event.getValue(),
					closeTo(((Double) eventData[1]).doubleValue(), 0.01));
		}
	}

	@Test
	public void testUnchangedValueEventIgnored() throws IOException {
		byte[] data = FileCopyUtils.copyToByteArray(
				Base64.getMimeDecoder().wrap(getClass().getResourceAsStream("value-events-02.b64")));
		MessageHeader header = new MessageHeader(MessageType.EventTableValueStates, null, data.length);

		// get Config ID from session
		expect(session.getUserProperties())
				.andReturn(
						Collections.singletonMap(LoxoneEndpoint.CONFIG_ID_USER_PROPERTY, TEST_CONFIG_ID))
				.times(2);

		// should first query for existing data but not find value
		expect(valueEventDao.loadEvent(EasyMock.anyObject(Long.class), EasyMock.anyObject(UUID.class)))
				.andReturn(null);

		ValueEvent seenValueEvent = new ValueEvent(
				UUID.fromString("0c37ae7e-016c-2e06-ffff-6d9b8f6a24c4"), TEST_CONFIG_ID, 18.375);
		expect(valueEventDao.loadEvent(EasyMock.anyObject(Long.class), EasyMock.anyObject(UUID.class)))
				.andReturn(seenValueEvent);

		Capture<ValueEvent> valueEventCapture = Capture.newInstance(CaptureType.ALL);
		valueEventDao.storeEvent(capture(valueEventCapture));
		expectLastCall().times(1);

		replay(session, eventAdmin, valueEventDao);

		ByteBuffer buffer = ByteBuffer.wrap(data);
		boolean handled = handler.handleDataMessage(header, session, buffer);
		assertThat("Message 1 handled", handled, equalTo(true));

		buffer = ByteBuffer.wrap(data);
		handled = handler.handleDataMessage(header, session, buffer);
		assertThat("Message 2 handled", handled, equalTo(true));

		verify(session, eventAdmin, valueEventDao);

		ValueEvent ve = valueEventCapture.getValue();
		assertThat("UUID", ve.getUuid(),
				equalTo(UUID.fromString("0c37ae7e-016c-2e06-ffff-6d9b8f6a24c4")));
		assertThat("Value", ve.getValue(), closeTo(18.375, 0.0001));
	}

	@Test
	public void testUnchangedValueEventNotIgnored() throws IOException {
		handler.setIgnoreUnchangedValues(false);

		byte[] data = FileCopyUtils.copyToByteArray(
				Base64.getMimeDecoder().wrap(getClass().getResourceAsStream("value-events-02.b64")));
		MessageHeader header = new MessageHeader(MessageType.EventTableValueStates, null, data.length);

		// get Config ID from session
		expect(session.getUserProperties())
				.andReturn(
						Collections.singletonMap(LoxoneEndpoint.CONFIG_ID_USER_PROPERTY, TEST_CONFIG_ID))
				.times(2);

		// should NOT query for existing data

		Capture<ValueEvent> valueEventCapture = Capture.newInstance(CaptureType.ALL);
		valueEventDao.storeEvent(capture(valueEventCapture));
		expectLastCall().times(2);

		replay(session, eventAdmin, valueEventDao);

		ByteBuffer buffer = ByteBuffer.wrap(data);
		boolean handled = handler.handleDataMessage(header, session, buffer);
		assertThat("Message 1 handled", handled, equalTo(true));

		buffer = ByteBuffer.wrap(data);
		handled = handler.handleDataMessage(header, session, buffer);
		assertThat("Message 2 handled", handled, equalTo(true));

		verify(session, eventAdmin, valueEventDao);

		for ( int i = 0; i < 2; i++ ) {
			ValueEvent ve = valueEventCapture.getValues().get(i);
			assertThat("UUID " + (i + 1), ve.getUuid(),
					equalTo(UUID.fromString("0c37ae7e-016c-2e06-ffff-6d9b8f6a24c4")));
			assertThat("Value " + (i + 1), ve.getValue(), closeTo(18.375, 0.0001));
		}
	}

}
