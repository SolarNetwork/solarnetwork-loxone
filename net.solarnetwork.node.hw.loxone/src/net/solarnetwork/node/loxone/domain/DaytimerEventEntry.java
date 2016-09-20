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

import org.joda.time.LocalTime;

/**
 * An entry in a daytimer event.
 * 
 * @author matt
 * @version 1.0
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
	 *        The from time, in minutes since midnight.
	 * @param to
	 *        The to time, in minutes since midnight.
	 * @param needActivate
	 *        Need trigger activate.
	 * @param value
	 *        For analog daytimer.
	 */
	public DaytimerEventEntry(int mode, int from, int to, int needActivate, double value) {
		super();
		this.mode = mode;
		this.from = LocalTime.fromMillisOfDay(from * 60L * 1000L);
		this.to = LocalTime.fromMillisOfDay(to * 60L * 1000L);
		this.needActivate = needActivate;
		this.value = value;
	}

	@Override
	public String toString() {
		return "DaytimerEventEntry{mode=" + mode + ", from=" + from + ", to=" + to + ", needActivate="
				+ needActivate + ", value=" + value + "}";
	}

	public int getMode() {
		return mode;
	}

	public LocalTime getFrom() {
		return from;
	}

	public LocalTime getTo() {
		return to;
	}

	public int getNeedActivate() {
		return needActivate;
	}

	public double getValue() {
		return value;
	}

}
