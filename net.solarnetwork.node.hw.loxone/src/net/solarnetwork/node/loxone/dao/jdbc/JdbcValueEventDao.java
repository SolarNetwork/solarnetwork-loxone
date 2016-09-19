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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import net.solarnetwork.node.loxone.dao.ValueEventDao;
import net.solarnetwork.node.loxone.domain.ValueEvent;

/**
 * JDBC implementation of {@link ValueEventDao}.
 * 
 * @author matt
 * @version 1.0
 */
public class JdbcValueEventDao extends BaseUUIDEntityDao<ValueEvent> implements ValueEventDao {

	/** The default tables version. */
	public static final int TABLES_VERSION = 1;

	/**
	 * Constructor.
	 */
	public JdbcValueEventDao() {
		super("vevent", TABLES_VERSION, new ValueEventRowMapper());
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void storeValueEvent(final ValueEvent event) {
		storeEntity(event);
	}

	@Override
	protected void setStoreStatementValues(ValueEvent event, PreparedStatement ps) throws SQLException {
		// Row order is: (uuid_hi, uuid_lo, config_id, created, value)
		prepareUUID(1, event.getUuid(), ps);
		ps.setObject(3, event.getConfigId());
		ps.setTimestamp(4, new Timestamp(
				event.getCreated() != null ? event.getCreated().getTime() : System.currentTimeMillis()),
				(Calendar) UTC_CALENDAR.clone());
		ps.setDouble(5, event.getValue());
	}

	@Override
	protected void setUpdateStatementValues(ValueEvent event, PreparedStatement ps) throws SQLException {
		// cols: value = ?
		//       uuid_hi, uuid_lo
		ps.setDouble(1, event.getValue());
		prepareUUID(2, event.getUuid(), ps);
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
	public ValueEvent getValueEvent(UUID uuid) {
		return getEntityByUUID(uuid);
	}

	private static final class ValueEventRowMapper implements RowMapper<ValueEvent> {

		@Override
		public ValueEvent mapRow(ResultSet rs, int rowNum) throws SQLException {
			// Row order is: uuid_hi, uuid_lo, config_id, created, value
			UUID uuid = readUUID(1, rs);
			Long configId = rs.getLong(3);
			Date created = null;
			Timestamp ts = rs.getTimestamp(4, (Calendar) UTC_CALENDAR.clone());
			if ( ts != null ) {
				created = new Date(ts.getTime());
			}
			double value = rs.getDouble(5);
			return new ValueEvent(uuid, configId, created, value);
		}
	}

}
