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

import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import net.solarnetwork.domain.SortDescriptor;
import net.solarnetwork.node.loxone.dao.ConfigurationEntityDao;
import net.solarnetwork.node.loxone.domain.BaseConfigurationEntity;

/**
 * Base DAO for {@link BaseConfigurationEntity} classes.
 * 
 * @author matt
 * @version 1.0
 */
public abstract class BaseConfigurationEntityDao<T extends BaseConfigurationEntity>
		extends BaseUUIDEntityDao<T> implements ConfigurationEntityDao<T> {

	public static final String SQL_DELETE_FOR_CONFIG = "delete-for-config";
	public static final String SQL_FIND_FOR_CONFIG = "find-for-config";

	private final Class<T> entityClass;

	@Override
	public Class<T> entityClass() {
		return entityClass;
	}

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
		super(entityName, version, rowMapper);
		this.entityClass = entityClass;
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
		int result = getJdbcTemplate().update(getSqlResource(SQL_DELETE_FOR_CONFIG), configId);
		return result;
	}

	@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
	@Override
	public List<T> findAllForConfig(Long configId, List<SortDescriptor> sortDescriptors) {
		String sql = getSqlResource(SQL_FIND_FOR_CONFIG);
		if ( sortDescriptors != null ) {
			StringBuilder buf = new StringBuilder(sql);
			buf.setLength(buf.lastIndexOf("ORDER BY"));
			buf.append("ORDER BY");
			for ( SortDescriptor sort : sortDescriptors ) {
				boolean added = false;
				if ( "name".equalsIgnoreCase(sort.getSortKey()) ) {
					buf.append(" lower(name)");
				}
				if ( added ) {
					buf.append(" ").append(sort.isDescending() ? "DESC" : "ASC");
				}
			}
			sql = buf.toString();
		}
		List<T> results = getJdbcTemplate().query(sql, getRowMapper(), configId);
		return results;
	}

}
