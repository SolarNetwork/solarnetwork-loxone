/* ==================================================================
 * WeatherEventBinaryFileHandler.java - 15/06/2017 2:39:00 PM
 *
 * Copyright 2017 SolarNetwork.net Dev Team
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
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.websocket.Session;
import org.osgi.service.event.Event;
import net.solarnetwork.node.loxone.domain.Config;
import net.solarnetwork.node.loxone.domain.WeatherEvent;
import net.solarnetwork.node.loxone.domain.WeatherEventEntry;
import net.solarnetwork.node.loxone.protocol.ws.BinaryFileHandler;
import net.solarnetwork.node.loxone.protocol.ws.LoxoneEvents;
import net.solarnetwork.node.loxone.protocol.ws.MessageHeader;
import net.solarnetwork.node.loxone.protocol.ws.MessageType;

/**
 * {@link BinaryFileHandler} for weather-type event binary messages.
 *
 * @author matt
 * @version 2.0
 * @since 1.1
 */
public class WeatherEventBinaryFileHandler extends BaseEventBinaryFileHandler<WeatherEvent> {

	/**
	 * Event broadcast with the last modification date of the Loxone structure
	 * file after it has been successfully saved. The
	 * {@link LoxoneEvents#EVENT_PROPERTY_CONFIG_ID} and
	 * {@link LoxoneEvents#EVENT_PROPERTY_DATE} properties will be available.
	 */
	public static final String WEATHER_EVENT = "net/solarnetwork/node/loxone/WEATHER_EVENT";

	/**
	 * The Event property used to convey a {@code WeatherEvent} instance
	 * associated with the event.
	 */
	public static final String EVENT_PROPERTY_WEATHER_EVENT = "weatherEvent";

	private boolean sendWeatherEvents = false;

	/**
	 * Constructor.
	 */
	public WeatherEventBinaryFileHandler() {
		super();
	}

	@Override
	public boolean supportsDataMessage(MessageHeader header, ByteBuffer buffer) {
		return (header != null && header.getType() == MessageType.EventTableWeatherStates);
	}

	@Override
	protected boolean handleDataMessage(MessageHeader header, Session session, ByteBuffer buffer,
			Long configId) {
		final int end = buffer.position() + (int) header.getLength();
		final Instant now = Instant.now();
		while ( buffer.hasRemaining() && buffer.position() < end ) {
			UUID uuid = readUUID(buffer);
			long modDate = buffer.getInt() & 0xFFFFFFFFL; // 4 bytes as unsigned int;
			int len = buffer.getInt();
			List<WeatherEventEntry> entries = new ArrayList<>(len);
			for ( int i = 0; i < len && buffer.hasRemaining() && buffer.position() < end; i += 1 ) {
				entries.add(new WeatherEventEntry(buffer.getInt(), buffer.getInt(), buffer.getInt(),
						buffer.getInt(), buffer.getInt(), buffer.getDouble(), buffer.getDouble(),
						buffer.getDouble(), buffer.getDouble(), buffer.getDouble(), buffer.getDouble()));
			}
			if ( entries.isEmpty() ) {
				continue;
			}
			WeatherEvent we = new WeatherEvent(uuid, configId, now, new Date(modDate), entries);
			log.trace("Parsed weather event {} = {}", uuid, we.getEntries());
			if ( sendWeatherEvents ) {
				Map<String, Object> eventProps = new LinkedHashMap<>(2);
				eventProps.put(LoxoneEvents.EVENT_PROPERTY_CONFIG_ID, configId);
				eventProps.put(LoxoneEvents.EVENT_PROPERTY_DATE, now.toEpochMilli());
				eventProps.put(EVENT_PROPERTY_WEATHER_EVENT, we);
				postEvent(new Event(WEATHER_EVENT, eventProps));
			}
			String dest = String.format(LoxoneEvents.VALUE_EVENT_MESSAGE_TOPIC,
					Config.idToExternalForm(configId));
			postMessage(dest, we);
		}
		return true;
	}

	/**
	 * Toggle the sending of {@link #WEATHER_EVENT} events.
	 *
	 * <p>
	 * This property defaults to {@literal false}.
	 * </p>
	 *
	 * @param sendWeatherEvents
	 *        {@literal true} to enable sending weather events
	 */
	public void setSendWeatherEvents(boolean sendWeatherEvents) {
		this.sendWeatherEvents = sendWeatherEvents;
	}

}
