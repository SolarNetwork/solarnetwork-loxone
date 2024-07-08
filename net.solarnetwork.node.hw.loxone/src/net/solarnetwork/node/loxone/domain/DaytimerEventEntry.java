/* ==================================================================
 * DaytimerEventEntry.java - 20/09/2016 7:15:48 PM
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

import java.time.LocalTime;

/**
 * An entry in a daytimer event.
 *
 * @author matt
 * @version 2.1
 */
public class DaytimerEventEntry {

	private final int mode;
	private final LocalTime from;
	private final LocalTime to;
	private final int needActivate;
	private final double value;

	/**
	 * Construct with values.
	 *
	 * @param mode
	 *        The mode.
	 * @param from
	 *        The from time, in minutes since midnight. Note that a value &gt;=
	 *        1440 will be stored as {@link LocalTime#MAX}.
	 * @param to
	 *        The to time, in minutes since midnight. Note that a value &gt;=
	 *        1440 will be stored as {@link LocalTime#MAX}.
	 * @param needActivate
	 *        Need trigger activate.
	 * @param value
	 *        For analog daytimer.
	 */
	public DaytimerEventEntry(int mode, int from, int to, int needActivate, double value) {
		super();
		this.mode = mode;
		this.from = (from < 1440 ? LocalTime.ofSecondOfDay(from * 60) : LocalTime.MAX);
		this.to = (to < 1440 ? LocalTime.ofSecondOfDay(to * 60) : LocalTime.MAX);
		this.needActivate = needActivate;
		this.value = value;
	}

	@Override
	public String toString() {
		return "DaytimerEventEntry{mode=" + mode + ", from=" + from + ", to=" + to + ", needActivate="
				+ needActivate + ", value=" + value + "}";
	}

	/**
	 * Get the mode.
	 *
	 * @return the mode
	 */
	public int getMode() {
		return mode;
	}

	/**
	 * Get the start time.
	 *
	 * @return the time
	 */
	public LocalTime getFrom() {
		return from;
	}

	/**
	 * Get the end time.
	 *
	 * @return the time
	 */
	public LocalTime getTo() {
		return to;
	}

	/**
	 * Get the activate flag.
	 *
	 * @return the flag
	 */
	public int getNeedActivate() {
		return needActivate;
	}

	/**
	 * Get the value.
	 *
	 * @return the value
	 */
	public double getValue() {
		return value;
	}

}
