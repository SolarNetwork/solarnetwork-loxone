/* ==================================================================
 * JdbcControlDao.java - 18/09/2016 12:40:16 PM
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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import net.solarnetwork.node.loxone.dao.ControlDao;
import net.solarnetwork.node.loxone.domain.Control;
import net.solarnetwork.node.loxone.domain.ControlType;

/**
 * JDBC implementation of {@link ControlDao}.
 * 
 * @author matt
 * @version 1.0
 */
public class JdbcControlDao extends BaseConfigurationEntityDao<Control> implements ControlDao {

	/** The default tables version. */
	public static final int TABLES_VERSION = 1;

	public static final String CONTROL_STATES_NAME = "control_states";
	public static final String SQL_CONTROL_STATES_DELETE_FOR_CONTROL = "delete-for-control";
	public static final String SQL_CONTROL_STATES_INSERT = "insert";
	public static final String SQL_CONTROL_STATES_FIND_FOR_CONTROL = "find-for-control";

	/**
	 * Constructor.
	 */
	public JdbcControlDao() {
		super(Control.class, "control", TABLES_VERSION, new ControlRowMapper());
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	@Override
	public void store(final Control control) {
		super.storeEntity(control);
		getJdbcTemplate().execute(new ConnectionCallback<Object>() {

			@Override
			public Object doInConnection(Connection con) throws SQLException, DataAccessException {
				// delete all existing
				PreparedStatement ps = con
						.prepareStatement(getSqlResource(String.format(SQL_RESOURCE_PREFIX + "-%s",
								CONTROL_STATES_NAME, SQL_CONTROL_STATES_DELETE_FOR_CONTROL)));
				prepareUUID(1, control.getUuid(), ps);
				ps.setObject(3, control.getConfigId());
				ps.execute();
				if ( control.getStates() != null && !control.getStates().isEmpty() ) {
					// insert
					PreparedStatement statePs = con
							.prepareStatement(getSqlResource(String.format(SQL_RESOURCE_PREFIX + "-%s",
									CONTROL_STATES_NAME, SQL_CONTROL_STATES_INSERT)));
					for ( Map.Entry<String, UUID> entry : control.getStates().entrySet() ) {
						prepareUUID(1, control.getUuid(), statePs);
						statePs.setObject(3, control.getConfigId());
						statePs.setString(4, entry.getKey());
						prepareUUID(5, entry.getValue(), statePs);
						statePs.addBatch();
					}
					statePs.executeBatch();
				}
				return null;
			}
		});
	}

	@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
	@Override
	public Control load(Long configId, UUID uuid) {
		final Control result = getEntityByUUID(configId, uuid);
		if ( result != null ) {
			final Map<String, UUID> stateMap = new LinkedHashMap<>(4);
			getJdbcTemplate().query(new PreparedStatementCreator() {

				@Override
				public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
					PreparedStatement ps = con
							.prepareStatement(getSqlResource(String.format(SQL_RESOURCE_PREFIX + "-%s",
									CONTROL_STATES_NAME, SQL_CONTROL_STATES_FIND_FOR_CONTROL)));
					prepareUUID(1, result.getUuid(), ps);
					ps.setObject(3, result.getConfigId());
					return ps;
				}
			}, new RowCallbackHandler() {

				@Override
				public void processRow(ResultSet rs) throws SQLException {
					String name = rs.getString(1);
					UUID stateUUID = readUUID(2, rs);
					stateMap.put(name, stateUUID);
				}
			});
			result.setStates(stateMap);
		}
		return result;
	}

	@Override
	protected void setStoreStatementValues(Control control, PreparedStatement ps) throws SQLException {
		// Row order is: uuid_hi, uuid_lo, config_id, name, sort, room_hi, room_lo, cat_hi, cat_lo
		prepareUUID(1, control.getUuid(), ps);
		ps.setObject(3, control.getConfigId());
		ps.setString(4, control.getName());
		ps.setInt(5, (control.getDefaultRating() != null ? control.getDefaultRating().intValue() : 0));
		ps.setShort(6, control.getType() != null ? control.getType().getIndex()
				: ControlType.Unknown.getIndex());
		prepareUUID(7, control.getRoom(), ps);
		prepareUUID(9, control.getCategory(), ps);
	}

	@Override
	protected void setUpdateStatementValues(Control control, PreparedStatement ps) throws SQLException {
		// cols: name, sort, ctype, room_hi, room_lo, cat_hi, cat_lo
		//       uuid_hi, uuid_lo, config_id
		ps.setString(1, control.getName());
		ps.setInt(2, (control.getDefaultRating() != null ? control.getDefaultRating().intValue() : 0));
		ps.setShort(3, control.getType() != null ? control.getType().getIndex()
				: ControlType.Unknown.getIndex());
		prepareUUID(4, control.getRoom(), ps);
		prepareUUID(6, control.getCategory(), ps);
		prepareUUID(8, control.getUuid(), ps);
		ps.setObject(10, control.getConfigId());
	}

	private static final class ControlRowMapper implements RowMapper<Control> {

		@Override
		public Control mapRow(ResultSet rs, int rowNum) throws SQLException {
			Control row = new Control();
			// Row order is: uuid_hi, uuid_lo, config_id, name, sort, ctype, room_hi, room_lo, cat_hi, cat_lo
			row.setUuid(readUUID(1, rs));
			row.setConfigId(rs.getLong(3));
			row.setName(rs.getString(4));
			row.setDefaultRating(rs.getInt(5));
			row.setType(ControlType.forIndexValue(rs.getShort(6)));
			row.setRoom(readUUID(7, rs));
			row.setCategory(readUUID(9, rs));
			return row;
		}
	}

}
