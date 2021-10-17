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

import java.util.List;
import java.util.UUID;
import net.solarnetwork.domain.SortDescriptor;
import net.solarnetwork.node.loxone.domain.ConfigurationEntity;

/**
 * DAO API for configuration entities.
 * 
 * @author matt
 * @version 1.1
 */
public interface ConfigurationEntityDao<T extends ConfigurationEntity> {

	/**
	 * Get the class of the entity managed by this DAO.
	 * 
	 * @return The class.
	 */
	Class<T> entityClass();

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
	 * @param configId
	 *        The config ID to match.
	 * @param uuid
	 *        The UUID of the entity to get.
	 * @return The associated entity, or <em>null</em> if not available.
	 */
	T load(Long configId, UUID uuid);

	/**
	 * Delete all entities matching a given configuration.
	 * 
	 * @param configId
	 *        The config ID to match.
	 * @return The count of deleted items.
	 */
	int deleteAllForConfig(Long configId);

	/**
	 * Get a list of persisted entities, optionally sorted in some way.
	 * 
	 * <p>
	 * The {@code sortDescriptors} parameter can be {@literal null}, in which case
	 * the sort order should default to the
	 * {@link ConfigurationEntity#getDefaultRating()} followed by
	 * {@link ConfigurationEntity#getName()}.
	 * </p>
	 * 
	 * @param configId
	 *        The config ID to match.
	 * @param sortDescriptors
	 *        list of sort descriptors to sort the results by
	 * @return list of all persisted entities, or empty list if none available
	 */
	List<T> findAllForConfig(Long configId, List<SortDescriptor> sortDescriptors);

	/**
	 * Get a list of persisted entities matching a given name, optionally sorted
	 * in some way.
	 * 
	 * <p>
	 * The {@code sortDescriptors} parameter can be {@literal null}, in which case
	 * the sort order should default to the
	 * {@link ConfigurationEntity#getDefaultRating()} followed by
	 * {@link ConfigurationEntity#getName()}.
	 * </p>
	 * 
	 * @param configId
	 *        The config ID to match.
	 * @param name
	 *        The name to match.
	 * @param sortDescriptors
	 *        list of sort descriptors to sort the results by
	 * @return list of all persisted entities, or empty list if none available
	 * @since 1.1
	 */
	List<T> findAllForConfigAndName(Long configId, String name, List<SortDescriptor> sortDescriptors);

}
