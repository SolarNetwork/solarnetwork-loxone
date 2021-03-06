/* ==================================================================
 * JdbcRoomDao.java - 18/09/2016 12:40:16 PM
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
import org.springframework.jdbc.core.RowMapper;
import net.solarnetwork.node.loxone.dao.RoomDao;
import net.solarnetwork.node.loxone.domain.Room;

/**
 * JDBC implementation of {@link RoomDao}.
 * 
 * @author matt
 * @version 1.1
 */
public class JdbcRoomDao extends BaseConfigurationEntityDao<Room> implements RoomDao {

	/** The default tables version. */
	public static final int TABLES_VERSION = 2;

	/**
	 * Constructor.
	 */
	public JdbcRoomDao() {
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
	 */
	public JdbcRoomDao(String sqlResourcePrefixTemplate, String tableNameTemplate) {
		super(Room.class, "room", TABLES_VERSION, new RoomRowMapper());
	}

	@Override
	protected void setStoreStatementValues(Room room, PreparedStatement ps) throws SQLException {
		// Row order is: uuid_hi, uuid_lo, config_id, name, sort, image
		prepareUUID(1, room.getUuid(), ps);
		ps.setObject(3, room.getConfigId());
		ps.setString(4, room.getName());
		ps.setInt(5, (room.getDefaultRating() != null ? room.getDefaultRating().intValue() : 0));
		ps.setString(6, room.getImage());
	}

	@Override
	protected void setUpdateStatementValues(Room room, PreparedStatement ps) throws SQLException {
		// cols: name, sort, image
		//       uuid_hi, uuid_lo, config_id
		ps.setString(1, room.getName());
		ps.setInt(2, (room.getDefaultRating() != null ? room.getDefaultRating().intValue() : 0));
		ps.setString(3, room.getImage());
		prepareUUID(4, room.getUuid(), ps);
		ps.setObject(6, room.getConfigId());
	}

	private static final class RoomRowMapper implements RowMapper<Room> {

		@Override
		public Room mapRow(ResultSet rs, int rowNum) throws SQLException {
			Room row = new Room();
			// Row order is: uuid_hi, uuid_lo, config_id, name, sort, image
			row.setUuid(readUUID(1, rs));
			row.setConfigId(rs.getLong(3));
			row.setName(rs.getString(4));
			row.setDefaultRating(rs.getInt(5));
			row.setImage(rs.getString(6));
			return row;
		}
	}

}
