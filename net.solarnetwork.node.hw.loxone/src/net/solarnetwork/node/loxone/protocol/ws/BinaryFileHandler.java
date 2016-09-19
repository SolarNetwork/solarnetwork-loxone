/* ==================================================================
 * BinaryFileHandler.java - 18/09/2016 6:54:58 AM
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

package net.solarnetwork.node.loxone.protocol.ws;

import java.io.IOException;
import java.io.Reader;
import java.nio.ByteBuffer;
import javax.websocket.Session;

/**
 * API for handling binary file data from the Loxone.
 * 
 * @author matt
 * @version 1.0
 */
public interface BinaryFileHandler {

	/**
	 * Test if this instance supports a given text-based file.
	 * 
	 * @param header
	 *        The message header.
	 * @param reader
	 *        The reader. Only a small amount of text should be read.
	 * @param limit
	 *        The limit of characters that should be read.
	 * @return <em>true</em> if the message can be handled by this instance.
	 */
	boolean supportsTextMessage(MessageHeader header, Reader reader, int limit) throws IOException;

	/**
	 * Handle a text-based file response from the Loxone server.
	 * 
	 * @param header
	 *        The message header.
	 * @param session
	 *        The websocket session.
	 * @param reader
	 *        The reader.
	 * @return <em>true</em> if the command was handled.
	 * @throws IOException
	 *         if any communication error occurs
	 */
	boolean handleTextMessage(MessageHeader header, Session session, Reader reader) throws IOException;

	/**
	 * Test if this instance supports a given binary file.
	 * 
	 * @param header
	 *        The message header.
	 * @param buffer
	 *        The byte buffer with the data to inspect.
	 * @return <em>true</em> if the message can be handled by this instance.
	 */
	boolean supportsDataMessage(MessageHeader header, ByteBuffer buffer);

	/**
	 * Handle a binary file response from the Loxone server.
	 * 
	 * @param header
	 *        The message header.
	 * @param session
	 *        The websocket session.
	 * @param buffer
	 *        The byte buffer with the data to process.
	 * @return <em>true</em> if the command was handled.
	 * @throws IOException
	 *         if any communication error occurs
	 */
	boolean handleDataMessage(MessageHeader header, Session session, ByteBuffer buffer);

}
