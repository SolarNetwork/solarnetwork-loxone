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

	/** Unknown. */
	Unknown("unknown", -1),

	/** Alarm. */
	Alarm("Alarm", 0),

	/** AlarmClock. */
	AlarmClock("AlarmClock", 1),

	/** AudioZone. */
	AudioZone("AudioZone", 2),

	/** CarCharger. */
	CarCharger("CarCharger", 3),

	/** ColorPicker. */
	ColorPicker("ColorPicker", 4),

	/** Daytimer. */
	Daytimer("Daytimer", 5),

	/** Dimmer. */
	Dimmer("Dimmer", 6),

	/** Fronius. */
	Fronius("Fronius", 7),

	/** Gate. */
	Gate("Gate", 8),

	/** Heatmixer. */
	Heatmixer("Heatmixer", 9),

	/** Hourcounter. */
	Hourcounter("Hourcounter", 10),

	/** InfoOnlyAnalog. */
	InfoOnlyAnalog("InfoOnlyAnalog", 11),

	/** InfoOnlyDigital. */
	InfoOnlyDigital("InfoOnlyDigital", 12),

	/** IRoomController. */
	IntelligentRoomController("IRoomController", 13),

	/** Intercom. */
	Intercom("Intercom", 14),

	/** Jalousie. */
	Jalousie("Jalousie", 15),

	/** LightController. */
	LightController("LightController", 16),

	/** LightsceneRGB. */
	LightsceneRGB("LightsceneRGB", 17),

	/** MediaClient. */
	MediaClient("MediaClient", 18),

	/** Meter. */
	Meter("Meter", 19),

	/** PoolController. */
	PoolController("PoolController", 21),

	/** Pushbutton. */
	Pushbutton("Pushbutton", 21),

	/** Radio. */
	Radio("Radio", 22),

	/** Remote. */
	Remote("Remote", 23),

	/** Sauna. */
	Sauna("Sauna", 24),

	/** Slider. */
	Slider("Slider", 25),

	/** SmokeAlarm. */
	SmokeAlarm("SmokeAlarm", 26),

	/** Switch. */
	Switch("Switch", 27),

	/** TextState. */
	TextState("TextState", 28),

	/** TimedSwitch. */
	TimedSwitch("TimedSwitch", 29),

	/** Tracker. */
	Tracker("Tracker", 30),

	/** UpDownLeftRight digital. */
	UpDownLeftRightDigital("UpDownLeftRight digital", 31),

	/** UpDownAnalog. */
	UpDownAnalog("UpDownAnalog", 32),

	/** UpDownLeftRight analog. */
	UpDownLeftRightAnalog("UpDownLeftRight analog", 33),

	/** ValueSelector. */
	ValueSelector("ValueSelector", 34),

	/** Webpage. */
	Webpage("Webpage", 35),

	/** UpDownDigital. */
	UpDownDigital("UpDownDigital", 36),

	/** CentralLightController. */
	CentralLightController("CentralLightController", 37),

	/** CentralJalousie. */
	CentralJalousie("CentralJalousie", 38),

	;

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

	/**
	 * Get an enum value for an index.
	 *
	 * @param index
	 *        the index to get the enum value for
	 * @return the enum value, or {@link #Unknown}
	 */
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
