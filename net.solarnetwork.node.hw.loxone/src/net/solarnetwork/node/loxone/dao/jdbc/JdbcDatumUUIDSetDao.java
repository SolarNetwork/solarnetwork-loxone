/* ==================================================================
 * JdbcDatumUUIDSetDao.java - 27/09/2016 4:12:55 PM
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

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import net.solarnetwork.node.loxone.dao.DatumUUIDSetDao;
import net.solarnetwork.node.loxone.domain.BasicDatumUUIDEntity;
import net.solarnetwork.node.loxone.domain.DatumUUIDEntity;

/**
 * JDBC implementation of {@link DatumUUIDSetDao}.
 * 
 * @author matt
 * @version 1.0
 */
public class JdbcDatumUUIDSetDao extends BaseUUIDSetDao<DatumUUIDEntity> implements DatumUUIDSetDao {

	/** The default tables version. */
	public static final int TABLES_VERSION = 1;

	public JdbcDatumUUIDSetDao() {
		super(DatumUUIDEntity.class, "datumset", TABLES_VERSION, new DatumUUIDEntityRowMapper());
	}

	private static final class DatumUUIDEntityRowMapper implements RowMapper<DatumUUIDEntity> {

		@Override
		public DatumUUIDEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
			BasicDatumUUIDEntity row = new BasicDatumUUIDEntity();
			// Row order is: uuid_hi, uuid_lo, config_id
			row.setUuid(readUUID(1, rs));
			row.setConfigId(rs.getLong(3));
			return row;
		}

	}

}
