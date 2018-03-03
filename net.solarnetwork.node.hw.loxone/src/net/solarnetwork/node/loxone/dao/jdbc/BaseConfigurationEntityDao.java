/* ==================================================================
 * BaseConfigurationEntityDao.java - 19/09/2016 4:53:25 PM
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

package net.solarnetwork.node.loxone.dao.jdbc;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.cache.Cache;
import javax.cache.Cache.Entry;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import net.solarnetwork.domain.SortDescriptor;
import net.solarnetwork.node.loxone.dao.ConfigurationEntityDao;
import net.solarnetwork.node.loxone.domain.BaseConfigurationEntity;
import net.solarnetwork.node.loxone.domain.Config;
import net.solarnetwork.node.loxone.domain.ConfigNameKey;

/**
 * Base DAO for {@link BaseConfigurationEntity} classes.
 * 
 * @author matt
 * @version 1.2
 */
public abstract class BaseConfigurationEntityDao<T extends BaseConfigurationEntity>
		extends BaseUUIDEntityDao<T> implements ConfigurationEntityDao<T> {

	/**
	 * SQL resource to find by name. Accepts a {@code configId} and
	 * {@code name}.
	 */
	public static final String SQL_FIND_FOR_NAME = "find-for-name";

	private Cache<ConfigNameKey, List<T>> entityNameCache;

	/**
	 * Init with an an entity name and table version, deriving various names
	 * based on conventions.
	 * 
	 * @param entityClass
	 *        The class of the entity managed by this DAO.
	 * @param entityName
	 *        The entity name to use. This name forms the basis of the default
	 *        SQL resource prefix, table name, tables version query, and SQL
	 *        init resource.
	 * @param version
	 *        The tables version.
	 * @param rowMapper
	 *        A row mapper to use when mapping entity query results.
	 */
	public BaseConfigurationEntityDao(Class<T> entityClass, String entityName, int version,
			RowMapper<T> rowMapper) {
		super(entityClass, entityName, version, rowMapper);
	}

	/**
	 * Init with an an entity name and table version, deriving various names
	 * based on conventions.
	 * 
	 * @param sqlResourcePrefixTemplate
	 *        a template with a single {@code %s} parameter for the SQL resource
	 *        prefix
	 * @param tableNameTemplate
	 *        a template with a single {@code %s} parameter for the SQL table
	 *        name
	 * @param entityClass
	 *        The class of the entity managed by this DAO.
	 * @param entityName
	 *        The entity name to use. This name forms the basis of the default
	 *        SQL resource prefix, table name, tables version query, and SQL
	 *        init resource.
	 * @param version
	 *        The tables version.
	 * @param rowMapper
	 *        A row mapper to use when mapping entity query results.
	 * @since 1.1
	 */
	public BaseConfigurationEntityDao(String sqlResourcePrefixTemplate, String tableNameTemplate,
			Class<T> entityClass, String entityName, int version, RowMapper<T> rowMapper) {
		super(sqlResourcePrefixTemplate, tableNameTemplate, entityClass, entityName, version, rowMapper);
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	@Override
	public void store(T entity) {
		storeEntity(entity);
	}

	@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
	@Override
	public T load(Long configId, UUID uuid) {
		return getEntityByUUID(configId, uuid);
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	@Override
	public int deleteAllForConfig(Long configId) {
		int result = deleteAllEntitiesForConfig(configId);
		deleteAllEntitiesForConfigFromNameCache(configId);
		return result;
	}

	@Override
	protected int deleteAllEntitiesForConfig(Long configId) {
		int result = super.deleteAllEntitiesForConfig(configId);
		deleteAllEntitiesForConfigFromNameCache(configId);
		return result;
	}

	/**
	 * Remove all entities matching a specific {@code configId} from the entity
	 * cache.
	 * 
	 * <p>
	 * This method does nothing if the entity cache is not configured.
	 * </p>
	 * 
	 * @param configId
	 *        The ID of the {@link Config} to delete all entities for.
	 * @since 1.2
	 */
	protected void deleteAllEntitiesForConfigFromNameCache(Long configId) {
		Cache<ConfigNameKey, List<T>> cache = getEntityNameCache();
		if ( cache == null ) {
			return;
		}
		Set<ConfigNameKey> keysToRemove = new HashSet<>();
		for ( Entry<ConfigNameKey, List<T>> entry : cache ) {
			if ( entry.getKey().getConfigId().equals(configId) ) {
				keysToRemove.add(entry.getKey());
			}
		}
		if ( !keysToRemove.isEmpty() ) {
			cache.removeAll(keysToRemove);
		}
	}

	@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
	@Override
	public List<T> findAllForConfig(Long configId, List<SortDescriptor> sortDescriptors) {
		return findAllEntitiesForConfig(configId, sortDescriptors);
	}

	/**
	 * Get a list of persisted entities matching a given name, optionally sorted
	 * in some way.
	 * 
	 * {@inheritDoc}
	 * 
	 * <p>
	 * This method will return cached entities if {@code sortDescriptors} is
	 * {@literal null} and the entity name cache is configured.
	 * </p>
	 */
	@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
	@Override
	public List<T> findAllForConfigAndName(Long configId, String name,
			List<SortDescriptor> sortDescriptors) {
		List<T> results = (sortDescriptors == null ? findAllForConfigAndNameFromCache(configId, name)
				: null);
		if ( results != null ) {
			return results;
		}
		String sql = getSqlResource(SQL_FIND_FOR_NAME);
		sql = handleSortDescriptors(sql, sortDescriptors, sortDescriptorColumnMapping());
		results = getJdbcTemplate().query(sql, getRowMapper(), configId, name);
		if ( results != null && sortDescriptors == null && !results.isEmpty() ) {
			storeEntitiesForConfigAndNameInCache(configId, name, results);
		}
		return results;
	}

	/**
	 * Store a list of entities in the entity name cache.
	 * 
	 * <p>
	 * This method does nothing if the entity cache is not configured.
	 * </p>
	 * 
	 * @param configId
	 *        The configuration ID of the entities.
	 * @param name
	 *        The name of the entities.
	 * @param entities
	 *        The entities to cache.
	 * @since 1.2
	 */
	protected void storeEntitiesForConfigAndNameInCache(Long configId, String name, List<T> entities) {
		Cache<ConfigNameKey, List<T>> cache = getEntityNameCache();
		if ( cache != null ) {
			cache.put(new ConfigNameKey(configId, name), entities);
		}
	}

	/**
	 * Get a list of persisted entities matching a given name from the entity
	 * name cache.
	 * 
	 * @param configId
	 *        The config ID to match.
	 * @param name
	 *        The name to match.
	 * @return The cached entity list, or {@literal null} if not cached or the
	 *         entity name cache is not configured.
	 */
	protected List<T> findAllForConfigAndNameFromCache(Long configId, String name) {
		Cache<ConfigNameKey, List<T>> cache = getEntityNameCache();
		if ( cache == null ) {
			return null;
		}
		return cache.get(new ConfigNameKey(configId, name));
	}

	/**
	 * Get the entity name cache.
	 * 
	 * @return The cache, or {@literal null} if not configured.
	 * @since 1.2
	 */
	public Cache<ConfigNameKey, List<T>> getEntityNameCache() {
		return entityNameCache;
	}

	/**
	 * Set an entity name cache.
	 * 
	 * <p>
	 * The entity name cache will be used to cache collections of entities
	 * fetched from the underlying database by their name.
	 * </p>
	 * 
	 * @param entityNameCache
	 *        The cache to use, or {@code null} to not use one.
	 * @since 1.2
	 */
	public void setEntityNameCache(Cache<ConfigNameKey, List<T>> entityNameCache) {
		this.entityNameCache = entityNameCache;
	}

}
