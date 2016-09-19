/* ==================================================================
 * ConfigurationEntityDao.java - 19/09/2016 4:47:48 PM
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

import java.util.UUID;
import net.solarnetwork.node.loxone.domain.BaseConfigurationEntity;
import net.solarnetwork.node.loxone.domain.Config;

/**
 * DAO API for configuration entities.
 * 
 * @author matt
 * @version 1.0
 */
public interface ConfigurationEntityDao<T extends BaseConfigurationEntity> {

	/**
	 * Store (create or update) an entity. The {@code uuid} value is the primary
	 * key.
	 * 
	 * @param entity
	 *        The entity to store.
	 */
	void store(T entity);

	/**
	 * Get an entity for a given UUID.
	 * 
	 * @param uuid
	 *        The UUID of the entity to get.
	 * @return The associated entity, or <em>null</em> if not available.
	 */
	T load(UUID uuid);

	/**
	 * Delete all entities matching a given configuration.
	 * 
	 * @param config
	 *        The configuration to match.
	 * @return The count of deleted items.
	 */
	int deleteAllForConfig(Config config);

}
