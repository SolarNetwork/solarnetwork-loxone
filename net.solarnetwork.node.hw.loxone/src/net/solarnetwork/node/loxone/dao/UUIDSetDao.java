/* ==================================================================
 * UUIDSetDao.java - 27/09/2016 3:54:53 PM
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

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import net.solarnetwork.domain.SortDescriptor;
import net.solarnetwork.node.loxone.domain.ConfigurationEntity;
import net.solarnetwork.node.loxone.domain.UUIDEntity;

/**
 * DAO to track a list of UUID values.
 * 
 * This API is meant to facility managing a subset list of UUID values out of a
 * possibly very large total list, so that any number of UUID values can be
 * added and then {@link #contains(Long, UUID)} can be used to query for the
 * existence of individual values or {@link #findAllForConfig(Long, List)} can
 * be used to get the complete subset list.
 * 
 * @author matt
 * @version 1.0
 */
public interface UUIDSetDao<T extends UUIDEntity> {

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
	 * Test if an entity exists for a given UUID.
	 * 
	 * @param configId
	 *        The config ID to match.
	 * @param uuid
	 *        The UUID of the entity to get.
	 * @return The associated entity, or <em>null</em> if not available.
	 */
	boolean contains(Long configId, UUID uuid);

	/**
	 * Delete an entity.
	 * 
	 * @param configId
	 *        The config ID to match.
	 * @param uuid
	 *        The UUID of the entity to get.
	 */
	int delete(Long configId, UUID uuid);

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
	 * The {@code sortDescriptors} parameter can be {@code null}, in which case
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
	 * Add and remove UUIDs from the set managed by this DAO.
	 * 
	 * If a UUID exists in both {@code add} and {@code remove}, it will be
	 * removed.
	 * 
	 * @param configId
	 *        The config ID to match.
	 * @param add
	 *        An optional set of UUIDs that should be added to the UUID set.
	 * @param remove
	 *        An optional set of UUIDs that should be removed from the UUID set.
	 */
	void updateSetForConfig(Long configId, Collection<UUID> add, Collection<UUID> remove);
}
