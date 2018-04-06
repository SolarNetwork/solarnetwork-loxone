/* ==================================================================
 * ConfigDao.java - 18/09/2016 6:03:08 AM
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

package net.solarnetwork.node.loxone.dao;

import net.solarnetwork.node.loxone.domain.Config;

/**
 * DAO API for {@link Config} data.
 * 
 * @author matt
 * @version 1.0
 */
public interface ConfigDao {

	/**
	 * Store (create or update) a config.
	 * 
	 * @param config
	 *        The config to store.
	 */
	void storeConfig(Config config);

	/**
	 * Get the {@link Config}.
	 * 
	 * @param id
	 *        The ID of the config to get.
	 * @return The config, or {@literal null} if not available.
	 */
	Config getConfig(Long id);

}
