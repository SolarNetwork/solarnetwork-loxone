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
 * @version 1.1
 */
public class JdbcDatumUUIDSetDao extends BaseUUIDSetDao<DatumUUIDEntity, DatumUUIDEntityParameters>
		implements DatumUUIDSetDao {

	/** The default tables version. */
	public static final int TABLES_VERSION = 1;

	public JdbcDatumUUIDSetDao() {
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
	public JdbcDatumUUIDSetDao(String sqlResourcePrefixTemplate, String tableNameTemplate) {
		super(sqlResourcePrefixTemplate, tableNameTemplate, DatumUUIDEntity.class,
				DatumUUIDEntityParameters.class, "datumset", TABLES_VERSION,
				new DatumUUIDEntityRowMapper());
	}

	@Override
	protected void setStoreStatementValues(PreparedStatement ps, Long configId, UUID uuid,
			DatumUUIDEntityParameters parameters) throws SQLException {
		super.setStoreStatementValues(ps, configId, uuid, parameters);
		ps.setInt(4, parameters != null && parameters.getSaveFrequencySeconds() != null
				? parameters.getSaveFrequencySeconds() : 0);
	}

	@Override
	protected int setUpdateStatementValues(PreparedStatement ps, Long configId, UUID uuid,
			DatumUUIDEntityParameters parameters) throws SQLException {
		ps.setInt(1, parameters != null && parameters.getSaveFrequencySeconds() != null
				? parameters.getSaveFrequencySeconds() : 0);
		return 2;
	}

	@Override
	protected void updateResultSetValues(ResultSet set, DatumUUIDEntityParameters parameters)
			throws SQLException {
		if ( parameters != null ) {
			if ( parameters.getSaveFrequencySeconds() != null ) {
				set.updateInt(4, parameters.getSaveFrequencySeconds());
			}
		}
	}

	private static final class DatumUUIDEntityRowMapper implements RowMapper<DatumUUIDEntity> {

		private final DatumUUIDEntityParametersRowMapper parametersMapper = new DatumUUIDEntityParametersRowMapper(
				3);

		@Override
		public DatumUUIDEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
			BasicDatumUUIDEntity row = new BasicDatumUUIDEntity();
			// Row order is: uuid_hi, uuid_lo, config_id
			row.setUuid(readUUID(1, rs));
			row.setConfigId(rs.getLong(3));

			DatumUUIDEntityParameters params = parametersMapper.mapRow(rs, rowNum);
			row.setParameters(params);

			return row;
		}

	}

	/**
	 * A {@link RowMapper} that maps columns to a
	 * {@link DatumUUIDEntityParameters} object.
	 * 
	 * <b>Note</b> this mapper will return {@code null} if the parameters
	 * represent default values only!
	 */
	public static final class DatumUUIDEntityParametersRowMapper
			implements RowMapper<DatumUUIDEntityParameters> {

		private final int columnOffset;

		public DatumUUIDEntityParametersRowMapper(int columnOffset) {
			super();
			this.columnOffset = columnOffset;
		}

		@Override
		public DatumUUIDEntityParameters mapRow(ResultSet rs, int rowNum) throws SQLException {
			int col = columnOffset + 1;
			BasicDatumUUIDEntityParameters params = new BasicDatumUUIDEntityParameters();
			params.setSaveFrequencySeconds(rs.getInt(col++));

			// Return params instance, as long as some property is not a default value
			if ( params.isDefaultProperties() ) {
				return null;
			}
			return params;
		}

	}

}
