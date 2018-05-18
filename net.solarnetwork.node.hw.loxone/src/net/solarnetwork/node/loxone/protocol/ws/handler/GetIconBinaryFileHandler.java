/* ==================================================================
 * GetIconBinaryFileHandler.java - 23/09/2016 4:56:26 PM
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
import java.util.concurrent.Future;
import javax.websocket.Session;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import net.solarnetwork.node.loxone.protocol.ws.BinaryFileHandler;
import net.solarnetwork.node.loxone.protocol.ws.CommandType;
import net.solarnetwork.node.loxone.protocol.ws.MessageHeader;
import net.solarnetwork.node.loxone.protocol.ws.MessageType;

/**
 * Request and handle image resources.
 * 
 * @author matt
 * @version 1.2
 */
public class GetIconBinaryFileHandler extends QueuedCommandHandler<String, Resource>
		implements BinaryFileHandler {

	private static final byte[] PNG_HEADER = new byte[] { (byte) 0x89, (byte) 0x50, (byte) 0x4E,
			(byte) 0x47 };

	@Override
	public boolean supportsCommand(CommandType command) {
		return (command == CommandType.GetIcon);
	}

	@Override
	public Future<?> sendCommand(CommandType command, Session session, Object... args)
			throws IOException {
		// we need one and only one argument: the name of the image to load
		Long configId = getConfigId(session);
		if ( supportsCommand(command) && args != null && args.length > 0 && args[0] != null ) {
			log.trace("Requesting image [{}]", args[0]);
			return requestImage(session, configId, args[0].toString());
		}
		return null;
	}

	private Future<Resource> requestImage(Session session, Long configId, String name) {
		return sendTextForKey(session, configId, name, name);
	}

	@Override
	public boolean supportsDataMessage(MessageHeader header, ByteBuffer buffer) {
		if ( header.getType() != MessageType.BinaryFile ) {
			return false;
		}
		if ( (buffer.limit() - buffer.position()) < PNG_HEADER.length ) {
			return false;
		}
		for ( int i = 0; i < PNG_HEADER.length; i++ ) {
			if ( buffer.get() != PNG_HEADER[i] ) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean handleDataMessage(MessageHeader header, Session session, ByteBuffer buffer) {
		byte[] data = new byte[buffer.remaining()];
		buffer.get(data);
		handleImageData(session, data);
		return true;
	}

	private void handleImageData(Session session, byte[] data) {
		String name = peekNextResultKey(session);
		Resource result = new ByteArrayIconResource(data, name);
		handleNextResult(session, result);
	}

	/**
	 * An in-memory {@link Resource} that supports a filename with in-memory
	 * data.
	 */
	private static final class ByteArrayIconResource extends ByteArrayResource {

		private final String filename;

		private ByteArrayIconResource(byte[] data, String filename) {
			super(data);
			this.filename = filename;
		}

		@Override
		public String getFilename() {
			return filename;
		}
	}

	@Override
	public boolean supportsTextMessage(MessageHeader header, Reader reader, int limit)
			throws IOException {
		// read at most 256 to inspect what we have, we'll look for "<svg"
		char[] buf = new char[limit > 256 ? 256 : limit];
		int count = reader.read(buf, 0, buf.length);
		String s = new String(buf, 0, count);
		return s.contains("<svg");
	}

	@Override
	public boolean handleTextMessage(MessageHeader header, Session session, Reader reader)
			throws IOException {
		String s = FileCopyUtils.copyToString(reader);
		log.debug("Got SVG image {}", s);
		handleImageData(session, s.getBytes());
		return true;
	}

}
