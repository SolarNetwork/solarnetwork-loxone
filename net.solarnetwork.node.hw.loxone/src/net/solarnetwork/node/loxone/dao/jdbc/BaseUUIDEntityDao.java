/* ==================================================================
 * BaseUUIDEntityDao.java - 18/09/2016 7:39:28 AM
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
import java.sql.Types;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.RowMapper;
import net.solarnetwork.domain.SortDescriptor;
import net.solarnetwork.node.dao.jdbc.AbstractJdbcDao;
import net.solarnetwork.node.loxone.domain.Config;
import net.solarnetwork.node.loxone.domain.UUIDEntity;

/**
 * Base class for supporting DAO operations on {@link UUIDEntity} objects.
 * 
 * @author matt
 * @version 1.1
 */
public abstract class BaseUUIDEntityDao<T extends UUIDEntity> extends AbstractJdbcDao<T> {

	/** The table name format for data, e.g. {@code loxone_N}. */
	public static final String TABLE_NAME_FORMAT = "loxone_%s";

	/** Prefix format for SQL resources, e.g. {@code derby-N}. */
	public static final String SQL_RESOURCE_PREFIX = "derby-%s";

	/**
	 * The default SQL format for the {@code sqlGetTablesVersion} property. The
	 * {@link #getTableName()} value is used in the pattern, e.g.
	 * {@code T-init.sql}.
	 */
	public static final String SQL_GET_TABLES_VERSION_FORMAT = "SELECT svalue FROM solarnode.sn_settings WHERE skey = "
			+ "'solarnode.%s.version'";

	/**
	 * The default classpath resource format for the {@code initSqlResource}.
	 * The {@link #getSqlResourcePrefix()} value is used in the pattern, e.g.
	 * {@code R-init.sql}.
	 */
	public static final String INIT_SQL_FORMAT = "%s-init.sql";

	public static final String SQL_INSERT = "insert";
	public static final String SQL_UPDATE = "update";
	public static final String SQL_GET_BY_PK = "get-pk";

	/**
	 * SQL resource to delete by config ID. Accepts a {@code configId}.
	 */
	public static final String SQL_DELETE_FOR_CONFIG = "delete-for-config";

	/**
	 * SQL resource to delete by primary key. Accepts the high {@code uuid} bits
	 * as a {@code long}, the low {@code uuid} bits as a {@code long}, and a
	 * {@code configId}.
	 */
	public static final String SQL_DELETE_BY_PK = "delete-pk";

	/**
	 * SQL resource to find by config ID. Accepts a {@code configId}.
	 */
	public static final String SQL_FIND_FOR_CONFIG = "find-for-config";

	/** A static calendar in the UTC time zone, to use for reference only. */
	protected static Calendar UTC_CALENDAR = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

	private final String baseSqlResourceTemplate;
	private final Class<T> entityClass;
	private final RowMapper<T> rowMapper;

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
	public BaseUUIDEntityDao(Class<T> entityClass, String entityName, int version,
			RowMapper<T> rowMapper) {
		this(SQL_RESOURCE_PREFIX, TABLE_NAME_FORMAT, entityClass, entityName, version, rowMapper);
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
	 */
	public BaseUUIDEntityDao(String sqlResourcePrefixTemplate, String tableNameTemplate,
			Class<T> entityClass, String entityName, int version, RowMapper<T> rowMapper) {
		super();

		setSqlResourcePrefix(String.format(sqlResourcePrefixTemplate, entityName));
		setTableName(String.format(tableNameTemplate, entityName));
		setTablesVersion(version);
		setSqlGetTablesVersion(String.format(SQL_GET_TABLES_VERSION_FORMAT, getTableName()));
		setInitSqlResource(new ClassPathResource(String.format(INIT_SQL_FORMAT, getSqlResourcePrefix()),
				getClass()));
		this.baseSqlResourceTemplate = sqlResourcePrefixTemplate;
		this.entityClass = entityClass;
		this.rowMapper = rowMapper;
	}

	/**
	 * Get the base SQL resource template, as originally passed to the
	 * constructor.
	 * 
	 * <p>
	 * The {@link #getSqlResourcePrefix()} method will return this prefix with
	 * the entity named appended, so this method can be used to get the original
	 * value.
	 * </p>
	 * 
	 * @return the baseSqlResourceTemplate
	 */
	public String getBaseSqlResourceTemplate() {
		return baseSqlResourceTemplate;
	}

	/**
	 * Get the entity class managed by this DAO.
	 * 
	 * @return The class.
	 */
	public Class<T> entityClass() {
		return entityClass;
	}

	/**
	 * Get the default row mapper.
	 * 
	 * @return The row mapper.
	 */
	protected RowMapper<T> getRowMapper() {
		return rowMapper;
	}

	/**
	 * Insert or update an entity.
	 * 
	 * @param entity
	 *        The entity to store.
	 */
	protected void storeEntity(final T entity) {
		assert entity.getUuid() != null;
		T existing = getEntityByUUID(entity.getConfigId(), entity.getUuid());
		if ( existing != null ) {
			updateDomainObject(entity, getSqlResource(SQL_UPDATE));
		} else {
			insertDomainObject(entity, getSqlResource(SQL_INSERT));
		}
	}

	/**
	 * Load an entity by its UUID.
	 * 
	 * @param configId
	 *        The config ID to match.
	 * @param uuid
	 *        The UUID of the entity to load.
	 * @return The loaded entity, or <em>null</em> if no matching entity found.
	 */
	protected T getEntityByUUID(Long configId, UUID uuid) {
		List<T> results = getJdbcTemplate().query(getSqlResource(SQL_GET_BY_PK), rowMapper,
				uuid.getMostSignificantBits(), uuid.getLeastSignificantBits(), configId);
		if ( results != null && results.size() > 0 ) {
			return results.get(0);
		}
		return null;
	}

	/**
	 * Set a UUID value on a {@code PreparedStatement}. Two {@code BIGINT}
	 * columns are used to store the value, first the most significant bits and
	 * then the least significant bits.
	 * 
	 * @param uuid
	 *        The UUID to set, or <em>null</em> to set {@code NULL} values.
	 * @param ps
	 *        The prepared statement to set the UUID onto.
	 * @param col
	 *        The starting column on the prepared statement to use for the most
	 *        significant bits. The least significant bits will be stored on
	 *        {@code col + 1}.
	 * @return The next column on the prepared statement <em>after</em> the UUID
	 *         values.
	 * @throws SQLException
	 *         If any SQL error occurs.
	 */
	protected static int prepareUUID(int col, UUID uuid, PreparedStatement ps) throws SQLException {
		if ( uuid == null ) {
			ps.setNull(col, Types.BIGINT);
			ps.setNull(col + 1, Types.BIGINT);
		} else {
			ps.setLong(col, uuid.getMostSignificantBits());
			ps.setLong(col + 1, uuid.getLeastSignificantBits());
		}
		return col + 2;
	}

	/**
	 * Read a UUID value from a {@link ResultSet}. Two {@code BIGINT} columns
	 * are expected for the value, first the most significant bits and then the
	 * least significant bits.
	 * 
	 * @param col
	 *        The starting column on the {@code ResultSet} to use for the most
	 *        significant bits. The least significant bits will be read from
	 *        {@code col + 1}.
	 * @param rs
	 *        The result set to read from.
	 * @return The UUID, or <em>null</em> if either columns held a {@code NULL}
	 *         value.
	 * @throws SQLException
	 *         If any SQL error occurs.
	 */
	protected static UUID readUUID(int col, ResultSet rs) throws SQLException {
		long hi = rs.getLong(col);
		if ( rs.wasNull() ) {
			return null;
		}
		long lo = rs.getLong(col + 1);
		if ( rs.wasNull() ) {
			return null;
		}
		return new UUID(hi, lo);
	}

	/**
	 * Delete all entities matching a specific {@code configId}.
	 * 
	 * @param configId
	 *        The ID of the {@link Config} to delete all entities for.
	 * @return The number of deleted entities
	 */
	protected int deleteAllEntitiesForConfig(Long configId) {
		int result = getJdbcTemplate().update(getSqlResource(SQL_DELETE_FOR_CONFIG), configId);
		return result;
	}

	/**
	 * Delete an entity matching a specific {@code configId} and {@code uuid}.
	 * 
	 * The {@link BaseUUIDEntityDao#SQL_DELETE_BY_PK} resource is used.
	 * 
	 * @param configId
	 *        The ID of the {@link Config} of the entity to delete.
	 * @param uuid
	 *        The UUID of the entity to delete.
	 * @return The number of deleted entities
	 */
	protected int deleteEntity(Long configId, UUID uuid) {
		int result = getJdbcTemplate().update(getSqlResource(SQL_DELETE_BY_PK),
				uuid.getMostSignificantBits(), uuid.getLeastSignificantBits(), configId);
		return result;
	}

	/**
	 * Find all entities for a specific {@code configId}.
	 * 
	 * This will call {@link #sortDescriptorColumnMapping()} to get the mapping
	 * of supported sort descriptor keys, and pass that to
	 * {@link #handleSortDescriptors(String, List, Map)}.
	 * 
	 * @param configId
	 *        The ID of the {@link Config} to get all entities for.
	 * @param sortDescriptors
	 *        Optional sort descriptors.
	 * @return The found entities.
	 */
	protected List<T> findAllEntitiesForConfig(Long configId, List<SortDescriptor> sortDescriptors) {
		String sql = getSqlResource(SQL_FIND_FOR_CONFIG);
		sql = handleSortDescriptors(sql, sortDescriptors, sortDescriptorColumnMapping());
		List<T> results = getJdbcTemplate().query(sql, getRowMapper(), configId);
		return results;
	}

	/**
	 * Get a mapping of sort descriptor keys to associated SQL column values for
	 * use in an {@code ORDER BY} clause.
	 * 
	 * This method returns mappings for the {@code name} and
	 * {@code defaultrating} keys.
	 * 
	 * @return The mapping.
	 */
	protected Map<String, String> sortDescriptorColumnMapping() {
		Map<String, String> m = new HashMap<String, String>();
		m.put("name", "lower(name)");
		m.put("defaultrating", "sort");
		m.put("sourceid", "source_id");
		return m;
	}

	/**
	 * Add an {@code ORDER BY} clause to a SQL query based on a set of sort
	 * descriptors.
	 * 
	 * If {@code sortDescriptors} is {@code null} or empty, this method does
	 * nothing.
	 * 
	 * Otherwise, the method will look for an existing {@code ORDER BY} clause
	 * (case sensitive!) and replace it with one generated by the descriptors.
	 * This allows the SQL to be written with a default ordering, which is only
	 * changed if specific sort descriptors are provided. If an existing
	 * {@code ORDER BY} clause is not present, the method will append the
	 * generated clause.
	 * 
	 * <b>Note:</b> the sort keys will be forced to lower case. The
	 * {@code columnMapping} keys should all be lower case already.
	 * 
	 * @param sql
	 *        The original SQL statement.
	 * @param sortDescriptors
	 *        The optional sort to apply.
	 * @param columnMapping
	 *        A mapping of sort keys (must be lower case) to associated column
	 *        values to use in the {@code ORDER BY} clause.
	 * @return The updated SQL statement.
	 */
	protected String handleSortDescriptors(String sql, List<SortDescriptor> sortDescriptors,
			Map<String, String> columnMapping) {
		if ( sortDescriptors != null && !sortDescriptors.isEmpty() ) {
			StringBuilder buf = new StringBuilder();
			for ( SortDescriptor sort : sortDescriptors ) {
				String key = sort.getSortKey();
				if ( key == null ) {
					continue;
				}
				String column = columnMapping.get(key.toLowerCase());
				if ( column == null ) {
					continue;
				}
				if ( buf.length() > 0 ) {
					buf.append(",");
				}
				buf.append(" ").append(column).append(" ").append(sort.isDescending() ? "DESC" : "ASC");
			}
			if ( buf.length() > 0 ) {
				int idx = sql.lastIndexOf("ORDER BY");
				if ( idx < 0 ) {
					buf.insert(0, " ORDER BY ");
					buf.insert(0, sql);
				} else {
					buf.insert(0, "ORDER BY ");
					buf.insert(0, sql.substring(0, idx));
				}
				sql = buf.toString();
			}
		}
		return sql;
	}

}
