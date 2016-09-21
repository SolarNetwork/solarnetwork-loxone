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
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.RowMapper;
import net.solarnetwork.node.dao.jdbc.AbstractJdbcDao;
import net.solarnetwork.node.loxone.domain.BaseUUIDEntity;

/**
 * Base class for supporting DAO operations on {@link BaseUUIDEntity} objects.
 * 
 * @author matt
 * @version 1.0
 */
public abstract class BaseUUIDEntityDao<T extends BaseUUIDEntity> extends AbstractJdbcDao<T> {

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

	/** A static calendar in the UTC time zone, to use for reference only. */
	protected static Calendar UTC_CALENDAR = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

	private final RowMapper<T> rowMapper;

	/**
	 * Init with an an entity name and table version, deriving various names
	 * based on conventions.
	 * 
	 * @param entityName
	 *        The entity name to use. This name forms the basis of the default
	 *        SQL resource prefix, table name, tables version query, and SQL
	 *        init resource.
	 * @param version
	 *        The tables version.
	 * @param rowMapper
	 *        A row mapper to use when mapping entity query results.
	 */
	public BaseUUIDEntityDao(String entityName, int version, RowMapper<T> rowMapper) {
		super();
		setSqlResourcePrefix(String.format(SQL_RESOURCE_PREFIX, entityName));
		setTableName(String.format(TABLE_NAME_FORMAT, entityName));
		setTablesVersion(version);
		setSqlGetTablesVersion(String.format(SQL_GET_TABLES_VERSION_FORMAT, getTableName()));
		setInitSqlResource(new ClassPathResource(String.format(INIT_SQL_FORMAT, getSqlResourcePrefix()),
				getClass()));
		this.rowMapper = rowMapper;
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
		T existing = getEntityByUUID(entity.getUuid());
		if ( existing != null ) {
			updateDomainObject(entity, getSqlResource(SQL_UPDATE));
		} else {
			insertDomainObject(entity, getSqlResource(SQL_INSERT));
		}
	}

	/**
	 * Load an entity by its UUID.
	 * 
	 * @param uuid
	 *        The UUID of the entity to load.
	 * @return The loaded entity, or <em>null</em> if no matching entity found.
	 */
	protected T getEntityByUUID(UUID uuid) {
		List<T> results = getJdbcTemplate().query(getSqlResource(SQL_GET_BY_PK), rowMapper,
				uuid.getMostSignificantBits(), uuid.getLeastSignificantBits());
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

}
