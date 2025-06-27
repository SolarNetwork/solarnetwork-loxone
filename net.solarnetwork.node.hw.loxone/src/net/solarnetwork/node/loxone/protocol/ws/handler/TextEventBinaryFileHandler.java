/* ==================================================================
 * TextEventBinaryFileHandler.java - 20/09/2016 5:35:09 PM
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

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import jakarta.websocket.Session;
import net.solarnetwork.node.loxone.domain.Config;
import net.solarnetwork.node.loxone.domain.TextEvent;
import net.solarnetwork.node.loxone.protocol.ws.BinaryFileHandler;
import net.solarnetwork.node.loxone.protocol.ws.LoxoneEvents;
import net.solarnetwork.node.loxone.protocol.ws.MessageHeader;
import net.solarnetwork.node.loxone.protocol.ws.MessageType;

/**
 * {@link BinaryFileHandler} for text-type event binary messages.
 *
 * @author matt
 * @version 1.0
 */
public class TextEventBinaryFileHandler extends BaseEventBinaryFileHandler<TextEvent> {

	/**
	 * Constructor.
	 */
	public TextEventBinaryFileHandler() {
		super();
	}

	@Override
	public boolean supportsDataMessage(MessageHeader header, ByteBuffer buffer) {
		return (header != null && header.getType() == MessageType.EventTableTextStates);
	}

	@Override
	protected boolean handleDataMessage(MessageHeader header, Session session, ByteBuffer buffer,
			Long configId) {
		final int end = buffer.position() + (int) header.getLength();
		final Instant now = Instant.now();
		final List<TextEvent> updated = new ArrayList<>();
		while ( buffer.hasRemaining() && buffer.position() < end ) {
			UUID uuid = readUUID(buffer);
			UUID icon = readUUID(buffer);
			long len = Integer.toUnsignedLong(buffer.getInt());
			int padding = (int) (len % 4);
			// NOTE: we are truncating to (int) here because Java doesn't allow 64-bit arrays...
			//       really this should be safe, because hopefully the Loxone device cannot
			//       actually send more than Integer.MAX_VALUE
			byte[] textBytes = new byte[(int) len];
			buffer.get(textBytes);
			if ( padding > 0 ) {
				buffer.position(buffer.position() + (4 - padding));
			}
			TextEvent te;
			try {
				te = new TextEvent(uuid, configId, now, icon, new String(textBytes, "UTF-8"));
				log.trace("Parsed text event {} = {}", uuid, te.getText());
				// TODO: are we going to store these? eventDao.storeEvent(te);
				updated.add(te);
			} catch ( UnsupportedEncodingException e ) {
				// should never happen
			}
		}
		// post updated values to message channel
		if ( !updated.isEmpty() ) {
			String dest = String.format(LoxoneEvents.TEXT_EVENT_MESSAGE_TOPIC,
					Config.idToExternalForm(configId));
			postMessage(dest, updated);
		}
		return true;
	}

}
