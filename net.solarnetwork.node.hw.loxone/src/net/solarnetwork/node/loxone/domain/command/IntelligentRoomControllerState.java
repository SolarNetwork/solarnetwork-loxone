/* ==================================================================
 * IntelligentRoomControllerState.java - 21/02/2018 7:00:06 AM
 *
 * Copyright 2018 SolarNetwork.net Dev Team
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

package net.solarnetwork.node.loxone.domain.command;

/**
 * State names for the intelligent room controller.
 *
 * @author matt
 * @version 1.0
 * @since 1.2
 */
public enum IntelligentRoomControllerState implements NamedControlState {

	/** EconomyTemperature. */
	EconomyTemperature("temperatures[0]"),

	/** ComfortTemperature. */
	ComfortTemperature("temperatures[1]"),

	/** ComfortCoolingTemperature. */
	ComfortCoolingTemperature("temperatures[2]"),

	/** EmptyHouseTemperature. */
	EmptyHouseTemperature("temperatures[3]"),

	/** HeatProtectionTemperature. */
	HeatProtectionTemperature("temperatures[4]"),

	/** IncreasedHeatTemperature. */
	IncreasedHeatTemperature("temperatures[5]"),

	/** PartyTemperature. */
	PartyTemperature("temperatures[6]"),

	/** ManualTemperature. */
	ManualTemperature("temperatures[7]"),

	/** TargetTemperature. */
	TargetTemperature("tempTarget"),

	/** ActualTemperature. */
	ActualTemperature("tempActual"),

	/** Error. */
	Error("error"),

	/** Mode. */
	Mode("mode"),

	/** ServiceMode. */
	ServiceMode("serviceMode"),

	/** CurrHeatTemperature. */
	CurrHeatTemperature("currHeatTempIx"),

	/** CurrCoolTemperature. */
	CurrCoolTemperature("currCoolTempIx"),

	/** Override. */
	Override("override"),

	/** OverrideTotal. */
	OverrideTotal("overrideTotal"),

	/** ManualMode. */
	ManualMode("manualMode"),

	/** Stop. */
	Stop("stop");

	private String name;

	private IntelligentRoomControllerState(String name) {
		this.name = name;
	}

	@Override
	public String getControlStateName() {
		return name;
	}

	/**
	 * Get an enum value for a state name.
	 *
	 * @param name
	 *        the state name to get an enum for
	 * @return the enum instance
	 * @throws IllegalArgumentException
	 *         if {@code name} is not a valid value
	 */
	public static IntelligentRoomControllerState forControlStateName(String name) {
		for ( IntelligentRoomControllerState state : values() ) {
			if ( name.equals(state.name) ) {
				return state;
			}
		}
		throw new IllegalArgumentException("Unknown state name");
	}

}
