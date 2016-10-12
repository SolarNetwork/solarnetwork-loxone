/* ==================================================================
 * JdbcSourceMappingDao.java - 12/10/2016 4:18:00 PM
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import net.solarnetwork.node.loxone.dao.SourceMappingDao;
import net.solarnetwork.node.loxone.domain.SourceMapping;

/**
 * JDBC based implementation of {@link SourceMappingDao}
 * 
 * @author matt
 * @version 1.0
 */
public class JdbcSourceMappingDao extends BaseConfigurationEntityDao<SourceMapping>
		implements SourceMappingDao {

	/** The default tables version. */
	public static final int TABLES_VERSION = 1;

	/**
	 * Constructor.
	 */
	public JdbcSourceMappingDao() {
		super(SourceMapping.class, "smap", TABLES_VERSION, new SourceMappingRowMapper());
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	@Override
	public int delete(Long configId, UUID uuid) {
		return deleteEntity(configId, uuid);
	}

	@Override
	protected void setStoreStatementValues(SourceMapping sourceMapping, PreparedStatement ps)
			throws SQLException {
		// col order is: uuid_hi, uuid_lo, config_id, source_id
		prepareUUID(1, sourceMapping.getUuid(), ps);
		ps.setObject(3, sourceMapping.getConfigId());
		ps.setString(4, sourceMapping.getSourceId());
	}

	@Override
	protected void setUpdateStatementValues(SourceMapping sourceMapping, PreparedStatement ps)
			throws SQLException {
		// cols: source_id
		//       uuid_hi, uuid_lo, config_id
		ps.setString(1, sourceMapping.getSourceId());
		prepareUUID(2, sourceMapping.getUuid(), ps);
		ps.setObject(4, sourceMapping.getConfigId());
	}

	private static final class SourceMappingRowMapper implements RowMapper<SourceMapping> {

		@Override
		public SourceMapping mapRow(ResultSet rs, int rowNum) throws SQLException {
			SourceMapping row = new SourceMapping();
			// col order is: uuid_hi, uuid_lo, config_id, source_id
			row.setUuid(readUUID(1, rs));
			row.setConfigId(rs.getLong(3));
			row.setSourceId(rs.getString(4));
			return row;
		}
	}
}
