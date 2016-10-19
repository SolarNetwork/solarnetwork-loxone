/* ==================================================================
 * TextEvent.java - 20/09/2016 5:35:34 PM
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

package net.solarnetwork.node.loxone.domain;

import java.util.Date;
import java.util.UUID;

/**
 * A text event.
 * 
 * @author matt
 * @version 1.0
 */
public class TextEvent extends BaseEventEntity {

	private final UUID iconUUID;
	private final String text;

	/**
	 * Construct a text event.
	 * 
	 * @param uuid
	 *        The UUID of the event.
	 * @param configId
	 *        The config ID.
	 * @param created
	 *        The creation date.
	 * @param iconUUID
	 *        The icon UUID.
	 * @param text
	 *        The text.
	 */
	public TextEvent(UUID uuid, Long configId, Date created, UUID iconUUID, String text) {
		super(uuid, configId, created);
		this.iconUUID = iconUUID;
		this.text = text;
	}

	/**
	 * Construct a text event. The creation date will be set to the current
	 * time.
	 * 
	 * @param uuid
	 *        The UUID of the event.
	 * @param configId
	 *        The config ID.
	 * @param iconUUID
	 *        The icon UUID.
	 * @param text
	 *        The text.
	 */
	public TextEvent(UUID uuid, Long configId, UUID iconUUID, String text) {
		this(uuid, configId, new Date(), iconUUID, text);
	}

	public UUID getIconUUID() {
		return iconUUID;
	}

	public String getText() {
		return text;
	}

}
