/* ==================================================================
 * BaseEventEntityDao.java - 20/09/2016 2:50:30 PM
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

import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import net.solarnetwork.domain.SortDescriptor;
import net.solarnetwork.node.loxone.dao.EventEntityDao;
import net.solarnetwork.node.loxone.domain.BaseEventEntity;

/**
 * Base DAO for {@link BaseEventEntity} classes.
 * 
 * @author matt
 * @version 1.1
 */
public abstract class BaseEventEntityDao<T extends BaseEventEntity> extends BaseUUIDEntityDao<T>
		implements EventEntityDao<T> {

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
	public BaseEventEntityDao(Class<T> entityClass, String entityName, int version,
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
	public BaseEventEntityDao(String sqlResourcePrefixTemplate, String tableNameTemplate,
			Class<T> entityClass, String entityName, int version, RowMapper<T> rowMapper) {
		super(sqlResourcePrefixTemplate, tableNameTemplate, entityClass, entityName, version, rowMapper);
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	@Override
	public void storeEvent(T entity) {
		// we expect mostly updates, not inserts, so try for that first
		assert entity.getUuid() != null;
		int count = updateDomainObject(entity, getSqlResource(SQL_UPDATE));
		if ( count == 0 ) {
			insertDomainObject(entity, getSqlResource(SQL_INSERT));
		}
	}

	@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
	@Override
	public T loadEvent(Long configId, UUID uuid) {
		return getEntityByUUID(configId, uuid);
	}

	@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
	@Override
	public List<T> findAllForConfig(Long configId, List<SortDescriptor> sortDescriptors) {
		return findAllEntitiesForConfig(configId, sortDescriptors);
	}

}
