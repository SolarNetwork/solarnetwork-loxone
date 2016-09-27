/* ==================================================================
 * BaseUUIDSetDao.java - 27/09/2016 3:58:21 PM
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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import net.solarnetwork.domain.SortDescriptor;
import net.solarnetwork.node.loxone.dao.UUIDSetDao;
import net.solarnetwork.node.loxone.domain.BasicUUIDEntity;
import net.solarnetwork.node.loxone.domain.UUIDEntity;

/**
 * Base implementation of {@link UUIDSetDao}.
 * 
 * @author matt
 * @version 1.0
 */
public abstract class BaseUUIDSetDao<T extends UUIDEntity> extends BaseUUIDEntityDao<T>
		implements UUIDSetDao<T> {

	/**
	 * SQL resource to get a count by {@code configId} and {@code uuid}. Accepts
	 * the high {@code uuid} bits as a {@code long}, the low {@code uuid} bits
	 * as a {@code long}, and a {@code configId}.
	 */
	public static final String SQL_COUNT_FOR_PK = "count-for-pk";

	/**
	 * Construct with an an entity name and table version, deriving various
	 * names based on conventions.
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
	public BaseUUIDSetDao(Class<T> entityClass, String entityName, int version, RowMapper<T> rowMapper) {
		super(entityClass, entityName, version, rowMapper);
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	@Override
	public void store(T entity) {
		storeEntity(entity);
	}

	@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
	@Override
	public boolean contains(Long configId, UUID uuid) {
		Integer count = getJdbcTemplate().queryForObject(getSqlResource(SQL_COUNT_FOR_PK), Integer.class,
				uuid.getMostSignificantBits(), uuid.getLeastSignificantBits(), configId);
		return (count != null && count.intValue() > 0);
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	@Override
	public int delete(Long configId, UUID uuid) {
		return deleteEntity(configId, uuid);
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	@Override
	public int deleteAllForConfig(Long configId) {
		return deleteAllEntitiesForConfig(configId);
	}

	@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
	@Override
	public List<T> findAllForConfig(Long configId, List<SortDescriptor> sortDescriptors) {
		return findAllEntitiesForConfig(configId, sortDescriptors);
	}

	/**
	 * Overrides {@link BaseUUIDEntityDao#storeEntity(UUIDEntity)} to only
	 * perform inserts, with the assumption there are no columns to update.
	 */
	@Override
	protected void storeEntity(final T entity) {
		assert entity.getUuid() != null;
		if ( !contains(entity.getConfigId(), entity.getUuid()) ) {
			insertDomainObject(entity, getSqlResource(SQL_INSERT));
		}
	}

	@Override
	protected void setStoreStatementValues(T entity, PreparedStatement ps) throws SQLException {
		// Row order is: uuid_hi, uuid_lo, config_id
		prepareUUID(1, entity.getUuid(), ps);
		ps.setObject(3, entity.getConfigId());
	}

	/**
	 * A {@link RowMapper} that maps rows to {@link BasicUUIDEntity} instances.
	 */
	public static final class UUIDEntityRowMapper implements RowMapper<BasicUUIDEntity> {

		@Override
		public BasicUUIDEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
			BasicUUIDEntity row = new BasicUUIDEntity();
			// Row order is: uuid_hi, uuid_lo, config_id
			row.setUuid(readUUID(1, rs));
			row.setConfigId(rs.getLong(3));
			return row;
		}
	}
}
