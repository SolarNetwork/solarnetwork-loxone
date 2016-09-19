/* ==================================================================
 * MessageHeaderTests.java - 17/09/2016 6:21:28 AM
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

package net.solarnetwork.node.loxone.protocol.ws.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.nio.ByteBuffer;
import java.util.EnumSet;
import org.junit.Test;
import net.solarnetwork.node.loxone.protocol.ws.MessageHeader;
import net.solarnetwork.node.loxone.protocol.ws.MessageInfo;
import net.solarnetwork.node.loxone.protocol.ws.MessageType;

/**
 * Unit tests for the {@link MessageHeader} class.
 * 
 * @author matt
 * @version 1.0
 */
public class MessageHeaderTests {

	@Test
	public void constructTextMessageHeaderFromByteBuffer() {
		final long length = 0xFAFBFCFDL;
		final ByteBuffer buf = ByteBuffer.wrap(new byte[] { 0x03, 0x00, 0x00, 0x00,
				(byte) (length & 0xFF), (byte) ((length >> 8) & 0xFF), (byte) ((length >> 16) & 0xFF),
				(byte) ((length >> 24) & 0xFF) });
		MessageHeader header = new MessageHeader(buf);
		assertEquals(MessageType.TextMessage, header.getType());
		assertNotNull(header.getInfo());
		assertTrue(header.getInfo().isEmpty());
		assertEquals("Message length encoded little endian", length, header.getLength());
		assertEquals("Buffer positioned at end of header", 8, buf.position());
		assertEquals("MessageHeader{TextMessage; length=" + length + "}", header.toString());
	}

	@Test
	public void constructBinaryFileMessageHeaderWithEstimatedLengthFromByteBuffer() {
		final long length = 0xFFFEFDFCL;
		final ByteBuffer buf = ByteBuffer.wrap(new byte[] { 0x03, 0x01, 0x01, 0x00,
				(byte) (length & 0xFF), (byte) ((length >> 8) & 0xFF), (byte) ((length >> 16) & 0xFF),
				(byte) ((length >> 24) & 0xFF) });
		MessageHeader header = new MessageHeader(buf);
		assertEquals(MessageType.BinaryFile, header.getType());
		assertEquals(EnumSet.of(MessageInfo.EstimatedSize), header.getInfo());
		assertEquals("Message length encoded little endian", length, header.getLength());
		assertEquals("Buffer positioned at end of header", 8, buf.position());
		assertEquals("MessageHeader{BinaryFile; flags=EstimatedSize; length=" + length + "}",
				header.toString());
	}

}
