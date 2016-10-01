/* ==================================================================
 * UUIDMap.java - 1/10/2016 8:07:59 PM
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

package net.solarnetwork.node.setup.web.loxone;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import net.solarnetwork.node.loxone.domain.UUIDSerializer;

/**
 * Map annoated with JSON configuration to use Loxone specific UUID serializer.
 * 
 * @author matt
 * @version 1.0
 */
@JsonSerialize(keyUsing = UUIDSerializer.class)
public class UUIDMap<V> extends LinkedHashMap<java.util.UUID, V> {

	private static final long serialVersionUID = -2935767201748175368L;

	public UUIDMap() {
		super();
	}

	public UUIDMap(Map<? extends UUID, ? extends V> m) {
		super(m);
	}

}
