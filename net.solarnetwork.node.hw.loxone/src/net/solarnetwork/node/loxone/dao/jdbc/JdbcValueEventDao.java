/* ==================================================================
 * JdbcValueEventDao.java - 19/09/2016 7:28:31 AM
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

import static net.solarnetwork.node.dao.jdbc.JdbcUtils.setUtcTimestampStatementValue;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.UUID;
import org.springframework.jdbc.core.RowMapper;
import net.solarnetwork.node.dao.jdbc.JdbcUtils;
import net.solarnetwork.node.loxone.dao.ValueEventDao;
import net.solarnetwork.node.loxone.domain.ValueEvent;

/**
 * JDBC implementation of {@link ValueEventDao}.
 *
 * @author matt
 * @version 2.1
 */
public class JdbcValueEventDao extends BaseEventEntityDao<ValueEvent> implements ValueEventDao {

	/** The default tables version. */
	public static final int TABLES_VERSION = 1;

	/** SQL resource to find value events for a datum set. */
	public static final String SQL_FIND_FOR_DATUMSET = "vevent-find-for-datumset";

	/**
	 * Constructor.
	 */
	public JdbcValueEventDao() {
		this(SQL_RESOURCE_PREFIX, TABLE_NAME_FORMAT);
	}

	/**
	 * Construct with custom SQL settings.
	 *
	 * @param sqlResourcePrefixTemplate
	 *        a template with a single {@code %s} parameter for the SQL resource
	 *        prefix
	 * @param tableNameTemplate
	 *        a template with a single {@code %s} parameter for the SQL table
	 *        name
	 * @since 1.1
	 */
	public JdbcValueEventDao(String sqlResourcePrefixTemplate, String tableNameTemplate) {
		super(sqlResourcePrefixTemplate, tableNameTemplate, ValueEvent.class, "vevent", TABLES_VERSION,
				new ValueEventRowMapper());
	}

	@Override
	protected void setStoreStatementValues(ValueEvent event, PreparedStatement ps) throws SQLException {
		// Row order is: (uuid_hi, uuid_lo, config_id, created, value)
		prepareUUID(1, event.getUuid(), ps);
		ps.setObject(3, event.getConfigId());
		setUtcTimestampStatementValue(ps, 4, event.getCreated() != null ? event.getCreated() : Instant.now());
		ps.setDouble(5, event.getValue());
	}

	@Override
	protected void setUpdateStatementValues(ValueEvent event, PreparedStatement ps) throws SQLException {
		// cols: created = ?, value = ?
		//       uuid_hi, uuid_lo, config_id
		setUtcTimestampStatementValue(ps, 1, event.getCreated() != null ? event.getCreated() : Instant.now());
		ps.setDouble(2, event.getValue());
		prepareUUID(3, event.getUuid(), ps);
		ps.setObject(5, event.getConfigId());
	}

	private static final class ValueEventRowMapper implements RowMapper<ValueEvent> {

		@Override
		public ValueEvent mapRow(ResultSet rs, int rowNum) throws SQLException {
			// Row order is: uuid_hi, uuid_lo, config_id, created, value
			UUID uuid = readUUID(1, rs);
			Long configId = rs.getLong(3);
			Instant created = JdbcUtils.getUtcTimestampColumnValue(rs, 4);
			double value = rs.getDouble(5);
			return new ValueEvent(uuid, configId, created, value);
		}
	}

}
