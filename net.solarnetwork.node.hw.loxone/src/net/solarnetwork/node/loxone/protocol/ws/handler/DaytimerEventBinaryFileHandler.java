/* ==================================================================
 * DaytimerEventBinaryFileHandler.java - 20/09/2016 7:14:56 PM
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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.websocket.Session;
import net.solarnetwork.node.loxone.domain.DaytimerEvent;
import net.solarnetwork.node.loxone.domain.DaytimerEventEntry;
import net.solarnetwork.node.loxone.protocol.ws.BinaryFileHandler;
import net.solarnetwork.node.loxone.protocol.ws.MessageHeader;
import net.solarnetwork.node.loxone.protocol.ws.MessageType;

/**
 * {@link BinaryFileHandler} for daytimer-type event binary messages.
 * 
 * @author matt
 * @version 1.0
 */
public class DaytimerEventBinaryFileHandler extends BaseEventBinaryFileHandler<DaytimerEvent> {

	@Override
	public boolean supportsDataMessage(MessageHeader header, ByteBuffer buffer) {
		return (header.getType() == MessageType.EventTableDaytimerStates);
	}

	@Override
	protected boolean handleDataMessage(MessageHeader header, Session session, ByteBuffer buffer,
			Long configId) {
		int end = buffer.position() + (int) header.getLength();
		Date now = new Date();
		while ( buffer.hasRemaining() && buffer.position() < end ) {
			UUID uuid = readUUID(buffer);
			double defaultValue = buffer.getDouble();
			int len = buffer.getInt();
			List<DaytimerEventEntry> entries = new ArrayList<>(len);
			for ( int i = 0; i < len && buffer.hasRemaining() && buffer.position() < end; i += 1 ) {
				entries.add(new DaytimerEventEntry(buffer.getInt(), buffer.getInt(), buffer.getInt(),
						buffer.getInt(), buffer.getDouble()));
			}
			DaytimerEvent de = new DaytimerEvent(uuid, configId, now, defaultValue, entries);
			log.trace("Parsed daytimer event {} = {}", uuid, de.getEntries());
		}
		return true;
	}

}
