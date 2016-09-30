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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import org.springframework.jdbc.core.RowMapper;
import net.solarnetwork.node.loxone.dao.DatumUUIDSetDao;
import net.solarnetwork.node.loxone.domain.BasicDatumUUIDEntity;
import net.solarnetwork.node.loxone.domain.BasicDatumUUIDEntityParameters;
import net.solarnetwork.node.loxone.domain.DatumUUIDEntity;
import net.solarnetwork.node.loxone.domain.DatumUUIDEntityParameters;

/**
 * JDBC implementation of {@link DatumUUIDSetDao}.
 * 
 * @author matt
 * @version 1.0
 */
public class JdbcDatumUUIDSetDao extends BaseUUIDSetDao<DatumUUIDEntity, DatumUUIDEntityParameters>
		implements DatumUUIDSetDao {

	/** The default tables version. */
	public static final int TABLES_VERSION = 1;

	public JdbcDatumUUIDSetDao() {
		super(DatumUUIDEntity.class, DatumUUIDEntityParameters.class, "datumset", TABLES_VERSION,
				new DatumUUIDEntityRowMapper());
	}

	@Override
	protected void setStoreStatementValues(PreparedStatement ps, Long configId, UUID uuid,
			DatumUUIDEntityParameters parameters) throws SQLException {
		super.setStoreStatementValues(ps, configId, uuid, parameters);
		ps.setInt(4, parameters != null ? parameters.getSaveFrequencySeconds() : 0);
	}

	@Override
	protected int setUpdateStatementValues(PreparedStatement ps, Long configId, UUID uuid,
			DatumUUIDEntityParameters parameters) throws SQLException {
		ps.setInt(1, parameters != null ? parameters.getSaveFrequencySeconds() : 0);
		return 2;
	}

	@Override
	protected void updateResultSetValues(ResultSet set, DatumUUIDEntityParameters parameters)
			throws SQLException {
		set.updateInt(1, parameters != null ? parameters.getSaveFrequencySeconds() : 0);
	}

	private static final class DatumUUIDEntityRowMapper implements RowMapper<DatumUUIDEntity> {

		@Override
		public DatumUUIDEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
			BasicDatumUUIDEntity row = new BasicDatumUUIDEntity();
			// Row order is: uuid_hi, uuid_lo, config_id
			row.setUuid(readUUID(1, rs));
			row.setConfigId(rs.getLong(3));

			BasicDatumUUIDEntityParameters params = new BasicDatumUUIDEntityParameters();
			row.setParameters(params);
			params.setSaveFrequencySeconds(rs.getInt(4));

			return row;
		}

	}

}
