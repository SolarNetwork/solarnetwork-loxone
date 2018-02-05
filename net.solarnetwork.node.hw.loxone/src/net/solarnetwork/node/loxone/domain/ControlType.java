/* ==================================================================
 * ControlType.java - 20/09/2016 6:13:43 AM
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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Control type values.
 * 
 * @author matt
 * @version 1.1
 */
public enum ControlType {

	Unknown("unknown", -1),
	Alarm("Alarm", 0),
	AlarmClock("AlarmClock", 1),
	AudioZone("AudioZone", 2),
	CarCharger("CarCharger", 3),
	ColorPicker("ColorPicker", 4),
	Daytimer("Daytimer", 5),
	Dimmer("Dimmer", 6),
	Fronius("Fronius", 7),
	Gate("Gate", 8),
	Heatmixer("Heatmixer", 9),
	Hourcounter("Hourcounter", 10),
	InfoOnlyAnalog("InfoOnlyAnalog", 11),
	InfoOnlyDigital("InfoOnlyDigital", 12),
	IntelligentRoomController("IRoomController", 13),
	Intercom("Intercom", 14),
	Jalousie("Jalousie", 15),
	LightController("LightController", 16),
	LightsceneRGB("LightsceneRGB", 17),
	MediaClient("MediaClient", 18),
	Meter("Meter", 19),
	PoolController("PoolController", 21),
	Pushbutton("Pushbutton", 21),
	Radio("Radio", 22),
	Remote("Remote", 23),
	Sauna("Sauna", 24),
	Slider("Slider", 25),
	SmokeAlarm("SmokeAlarm", 26),
	Switch("Switch", 27),
	TextState("TextState", 28),
	TimedSwitch("TimedSwitch", 29),
	Tracker("Tracker", 30),
	UpDownLeftRightDigital("UpDownLeftRight digital", 31),
	UpDownAnalog("UpDownAnalog", 32),
	UpDownLeftRightAnalog("UpDownLeftRight analog", 33),
	ValueSelector("ValueSelector", 34),
	Webpage("Webpage", 35);

	private String key;
	private short index;

	private ControlType(String key, int index) {
		this.key = key;
		this.index = (short) index;
	}

	/**
	 * Get the key value for this type.
	 * 
	 * @return The key value.
	 */
	@JsonValue
	public String getKey() {
		return key;
	}

	/**
	 * Get the index value for this type.
	 * 
	 * @return The index value.
	 */
	public short getIndex() {
		return index;
	}

	public static ControlType forIndexValue(int index) {
		// for now index + 1 == ordinal order
		ControlType[] values = ControlType.values();
		index += 1;
		if ( index >= 0 && index < values.length ) {
			return values[index];
		}
		return Unknown;
	}

	/**
	 * Get an enum from a category value.
	 * 
	 * @param value
	 *        The category value.
	 * @return The enum, or <em>null</em> if not known.
	 */
	@JsonCreator
	public static ControlType forKeyValue(String value) {
		for ( ControlType t : ControlType.values() ) {
			if ( t.key.equals(value) ) {
				return t;
			}
		}
		return Unknown;
	}

}
