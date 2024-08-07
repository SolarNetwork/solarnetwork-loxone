/* ==================================================================
 * BasicValueEventDatumParameters.java - 4/10/2016 1:01:09 PM
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

/**
 * Basic implementation of {@link ValueEventDatumParameters}.
 *
 * @author matt
 * @version 1.0
 */
public class BasicValueEventDatumParameters extends BasicDatumPropertyUUIDEntityParameters
		implements ValueEventDatumParameters {

	private String name;
	private Double value;

	/**
	 * Constructor.
	 */
	public BasicValueEventDatumParameters() {
		super();
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * Set the name.
	 *
	 * @param name
	 *        the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public Double getValue() {
		return value;
	}

	/**
	 * Set the value.
	 *
	 * @param value
	 *        the value to set
	 */
	public void setValue(Double value) {
		this.value = value;
	}

}
