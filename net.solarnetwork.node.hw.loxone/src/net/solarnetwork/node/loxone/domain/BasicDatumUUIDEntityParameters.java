/* ==================================================================
 * BasicDatumUUIDEntityParameters.java - 1/10/2016 11:25:04 AM
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

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Basic implementation of {@link DatumUUIDEntityParameters}.
 *
 * @author matt
 * @version 1.0
 */
public class BasicDatumUUIDEntityParameters implements DatumUUIDEntityParameters {

	private Integer saveFrequencySeconds;

	/**
	 * Constructor.
	 */
	public BasicDatumUUIDEntityParameters() {
		super();
	}

	/**
	 * Constructor.
	 *
	 * @param saveFrequencySeconds
	 *        the save frequency
	 */
	public BasicDatumUUIDEntityParameters(Integer saveFrequencySeconds) {
		super();
		this.saveFrequencySeconds = saveFrequencySeconds;
	}

	/**
	 * Test if the properties of this instance are all default (or unspecified)
	 * values.
	 *
	 * @return {@code true} only if all properties are configured to default
	 *         values
	 */
	@JsonIgnore
	public boolean isDefaultProperties() {
		return (saveFrequencySeconds == null || saveFrequencySeconds.intValue() == 0);
	}

	@Override
	public Integer getSaveFrequencySeconds() {
		return saveFrequencySeconds;
	}

	/**
	 * Set the save frequency.
	 *
	 * @param saveFrequencySeconds
	 *        the frequency to set, in seconds
	 */
	public void setSaveFrequencySeconds(int saveFrequencySeconds) {
		this.saveFrequencySeconds = saveFrequencySeconds;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((saveFrequencySeconds == null) ? 0 : saveFrequencySeconds.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( obj == null ) {
			return false;
		}
		if ( getClass() != obj.getClass() ) {
			return false;
		}
		BasicDatumUUIDEntityParameters other = (BasicDatumUUIDEntityParameters) obj;
		if ( saveFrequencySeconds == null ) {
			if ( other.saveFrequencySeconds != null ) {
				return false;
			}
		} else if ( !saveFrequencySeconds.equals(other.saveFrequencySeconds) ) {
			return false;
		}
		return true;
	}

}
