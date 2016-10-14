/* ==================================================================
 * ConfigurationEntity.java - 21/09/2016 4:29:08 PM
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

import java.util.regex.Pattern;

/**
 * API for a Loxone configuration entity.
 * 
 * @author matt
 * @version 1.1
 */
public interface ConfigurationEntity extends UUIDEntity {

	/**
	 * A pattern to use for removing unwanted characters from the {@code name}
	 * when deriving a source ID value.
	 */
	Pattern SOURCE_ID_REMOVE_PAT = Pattern.compile("\\s");

	/**
	 * Get a sorting priority. Higher values should be sorted before lower
	 * values.
	 * 
	 * @return The sorting priority.
	 */
	Integer getDefaultRating();

	/**
	 * Get a display-friendly name.
	 * 
	 * @return The display name.
	 */
	String getName();

}
