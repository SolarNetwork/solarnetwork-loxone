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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import net.solarnetwork.domain.SortDescriptor;
import net.solarnetwork.node.loxone.dao.UUIDSetDao;
import net.solarnetwork.node.loxone.domain.UUIDEntityParameters;
import net.solarnetwork.node.loxone.domain.UUIDSetEntity;

/**
 * Base implementation of {@link UUIDSetDao}.
 * 
 * @author matt
 * @version 1.1
 */
public abstract class BaseUUIDSetDao<T extends UUIDSetEntity<P>, P extends UUIDEntityParameters>
		extends BaseUUIDEntityDao<T> implements UUIDSetDao<T, P> {

	/**
	 * SQL resource to get a count by {@code configId} and {@code uuid}. Accepts
	 * the high {@code uuid} bits as a {@code long}, the low {@code uuid} bits
	 * as a {@code long}, and a {@code configId}.
	 */
	public static final String SQL_COUNT_FOR_PK = "count-for-pk";

	private final Class<P> parametersClass;

	/**
	 * Construct with an an entity name and table version, deriving various
	 * names based on conventions.
	 * 
	 * @param entityClass
	 *        The class of the entity managed by this DAO.
	 * @param parametersClass
	 *        The class of the parameters managed by this DAO.
	 * @param entityName
	 *        The entity name to use. This name forms the basis of the default
	 *        SQL resource prefix, table name, tables version query, and SQL
	 *        init resource.
	 * @param version
	 *        The tables version.
	 * @param rowMapper
	 *        A row mapper to use when mapping entity query results.
	 */
	public BaseUUIDSetDao(Class<T> entityClass, Class<P> parametersClass, String entityName, int version,
			RowMapper<T> rowMapper) {
		super(entityClass, entityName, version, rowMapper);
		this.parametersClass = parametersClass;
	}

	/**
	 * Construct with an an entity name and table version, deriving various
	 * names based on conventions.
	 * 
	 * @param sqlResourcePrefixTemplate
	 *        a template with a single {@code %s} parameter for the SQL resource
	 *        prefix
	 * @param tableNameTemplate
	 *        a template with a single {@code %s} parameter for the SQL table
	 *        name
	 * @param entityClass
	 *        The class of the entity managed by this DAO.
	 * @param parametersClass
	 *        The class of the parameters managed by this DAO.
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
	public BaseUUIDSetDao(String sqlResourcePrefixTemplate, String tableNameTemplate,
			Class<T> entityClass, Class<P> parametersClass, String entityName, int version,
			RowMapper<T> rowMapper) {
		super(sqlResourcePrefixTemplate, tableNameTemplate, entityClass, entityName, version, rowMapper);
		this.parametersClass = parametersClass;
	}

	@Override
	public Class<P> parametersClass() {
		return parametersClass;
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

	@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
	@Override
	public T load(Long configId, UUID uuid) {
		return getEntityByUUID(configId, uuid);
	}

	@Override
	protected final void setStoreStatementValues(T entity, PreparedStatement ps) throws SQLException {
		setStoreStatementValues(ps, entity.getConfigId(), entity.getUuid(), entity.getParameters());
	}

	/**
	 * Called from
	 * {@link #setStoreStatementValues(UUIDSetEntity, PreparedStatement)} as
	 * well as {@link #updateSetForConfig(Long, Collection, Collection, Map)} to
	 * set prepared statement values for inserting rows.
	 * 
	 * This method will set the UUID and config ID column values, so extending
	 * classes can start on column {@code 4}.
	 * 
	 * @param ps
	 *        The prepared statement.
	 * @param configId
	 *        The config ID.
	 * @param uuid
	 *        The UUID.
	 * @param parameters
	 *        The parameters.
	 * @throws SQLException
	 *         If any SQL error occurs.
	 */
	protected void setStoreStatementValues(PreparedStatement ps, Long configId, UUID uuid, P parameters)
			throws SQLException {
		// Row order is: uuid_hi, uuid_lo, config_id
		prepareUUID(1, uuid, ps);
		ps.setObject(3, configId);
	}

	@Override
	protected final void setUpdateStatementValues(T entity, PreparedStatement ps) throws SQLException {
		int col = setUpdateStatementValues(ps, entity.getConfigId(), entity.getUuid(),
				entity.getParameters());
		prepareUUID(col, entity.getUuid(), ps);
		col += 2;
		ps.setObject(col, entity.getConfigId());
	}

	/**
	 * Called from
	 * {@link #setUpdateStatementValues(UUIDSetEntity, PreparedStatement)} as
	 * well as {@link #updateSetForConfig(Long, Collection, Collection, Map)} to
	 * set prepared statement values for updating rows.
	 * 
	 * @param ps
	 *        The prepared statement.
	 * @param configId
	 *        The config ID.
	 * @param uuid
	 *        The UUID.
	 * @param parameters
	 *        The parameters.
	 * @return The next colum value to use, for setting the UUID and config ID
	 *         parameters
	 * @throws SQLException
	 *         If any SQL error occurs.
	 */
	protected abstract int setUpdateStatementValues(PreparedStatement ps, Long configId, UUID uuid,
			P parameters) throws SQLException;

	/**
	 * Called from
	 * {@link #updateSetForConfig(Long, Collection, Collection, Map)} to update
	 * existing row parameter values.
	 * 
	 * The {@link #SQL_GET_BY_PK} SQL will have been used, so updated columns
	 * must match that query.
	 * 
	 * @param set
	 *        An updatable result set.
	 * @param parameters
	 *        The parameters to update.
	 * @throws SQLException
	 *         If any SQL error occurs.
	 */
	protected abstract void updateResultSetValues(ResultSet set, P parameters) throws SQLException;

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	@Override
	public void updateSetForConfig(final Long configId, final Collection<UUID> add,
			final Collection<UUID> remove, final Map<UUID, P> parameters) {
		final String getSql = getSqlResource(SQL_GET_BY_PK)
				+ (getSqlForUpdateSuffix() != null ? getSqlForUpdateSuffix() : "");
		final String addSql = getSqlResource(SQL_INSERT);
		final Set<UUID> toAdd = (add != null ? new LinkedHashSet<>(add) : Collections.emptySet());
		final Map<UUID, P> paramsToAdd = (parameters != null ? new LinkedHashMap<>(parameters)
				: Collections.emptyMap());
		getJdbcTemplate().execute(new ConnectionCallback<Object>() {

			@Override
			public Object doInConnection(Connection con) throws SQLException, DataAccessException {
				// find existing rows we don't need to add, removing them from toAdd after 
				// applying any parameters update as necessary
				if ( !toAdd.isEmpty() ) {
					PreparedStatement ps = con.prepareStatement(getSql, ResultSet.TYPE_FORWARD_ONLY,
							ResultSet.CONCUR_UPDATABLE);
					try {
						for ( Iterator<UUID> itr = toAdd.iterator(); itr.hasNext(); ) {
							UUID uuid = itr.next();
							prepareUUID(1, uuid, ps);
							ps.setObject(3, configId);
							ResultSet rs = ps.executeQuery();
							try {
								if ( rs.next() ) {
									P params = paramsToAdd.remove(uuid);
									if ( params != null ) {
										updateResultSetValues(rs, params);
										rs.updateRow();
									}
									itr.remove();
								}
							} finally {
								if ( rs != null ) {
									rs.close();
								}
							}
						}
					} finally {
						if ( ps != null ) {
							ps.close();
						}
					}

					// now insert add values
					if ( !toAdd.isEmpty() ) {
						ps = con.prepareStatement(addSql);
						try {
							for ( UUID uuid : toAdd ) {
								P params = paramsToAdd.remove(uuid);
								setStoreStatementValues(ps, configId, uuid, params);
								ps.addBatch();
							}
							ps.executeBatch();
						} finally {
							if ( ps != null ) {
								ps.close();
							}
						}
					}
				}

				// now any UUIDs left in paramsToAdd are updates to existing UUIDs
				if ( !paramsToAdd.isEmpty() ) {
					PreparedStatement ps = con.prepareStatement(getSqlResource(SQL_UPDATE));
					try {
						for ( Map.Entry<UUID, P> me : paramsToAdd.entrySet() ) {
							int col = setUpdateStatementValues(ps, configId, me.getKey(), me.getValue());
							prepareUUID(col, me.getKey(), ps);
							col += 2;
							ps.setObject(col, configId);
							ps.addBatch();
						}
						ps.executeBatch();
					} finally {
						if ( ps != null ) {
							ps.close();
						}
					}
				}

				// finally delete remove values
				if ( remove != null ) {
					PreparedStatement ps = con.prepareStatement(getSqlResource(SQL_DELETE_BY_PK));
					try {
						for ( UUID uuid : remove ) {
							prepareUUID(1, uuid, ps);
							ps.setObject(3, configId);
							ps.addBatch();
						}
						ps.executeBatch();
					} finally {
						if ( ps != null ) {
							ps.close();
						}
					}
				}

				return null;
			}
		});
	}

}
