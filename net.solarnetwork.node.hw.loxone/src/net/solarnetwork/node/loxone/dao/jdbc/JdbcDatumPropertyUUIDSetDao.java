/* ==================================================================
 * JdbcDatumPropertyUUIDSetDao.java - 27/09/2016 4:12:55 PM
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
import net.solarnetwork.node.loxone.dao.DatumPropertyUUIDSetDao;
import net.solarnetwork.node.loxone.domain.BasicDatumPropertyUUIDEntity;
import net.solarnetwork.node.loxone.domain.BasicDatumPropertyUUIDEntityParameters;
import net.solarnetwork.node.loxone.domain.DatumPropertyUUIDEntity;
import net.solarnetwork.node.loxone.domain.DatumPropertyUUIDEntityParameters;
import net.solarnetwork.node.loxone.domain.DatumValueType;

/**
 * JDBC implementation of {@link DatumPropertyUUIDSetDao}.
 * 
 * @author matt
 * @version 1.1
 */
public class JdbcDatumPropertyUUIDSetDao
		extends BaseUUIDSetDao<DatumPropertyUUIDEntity, DatumPropertyUUIDEntityParameters>
		implements DatumPropertyUUIDSetDao {

	/** The default tables version. */
	public static final int TABLES_VERSION = 1;

	public JdbcDatumPropertyUUIDSetDao() {
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
	public JdbcDatumPropertyUUIDSetDao(String sqlResourcePrefixTemplate, String tableNameTemplate) {
		super(sqlResourcePrefixTemplate, tableNameTemplate, DatumPropertyUUIDEntity.class,
				DatumPropertyUUIDEntityParameters.class, "propset", TABLES_VERSION,
				new DatumPropertyUUIDEntityRowMapper());
	}

	@Override
	protected void setStoreStatementValues(PreparedStatement ps, Long configId, UUID uuid,
			DatumPropertyUUIDEntityParameters parameters) throws SQLException {
		super.setStoreStatementValues(ps, configId, uuid, parameters);
		ps.setShort(4, parameters != null && parameters.getDatumValueType() != null
				? (short) parameters.getDatumValueType().getCode() : (short) 0);
	}

	@Override
	protected int setUpdateStatementValues(PreparedStatement ps, Long configId, UUID uuid,
			DatumPropertyUUIDEntityParameters parameters) throws SQLException {
		ps.setShort(1, parameters != null && parameters.getDatumValueType() != null
				? (short) parameters.getDatumValueType().getCode() : (short) 0);
		return 2;
	}

	@Override
	protected void updateResultSetValues(ResultSet set, DatumPropertyUUIDEntityParameters parameters)
			throws SQLException {
		if ( parameters != null ) {
			if ( parameters.getDatumValueType() != null ) {
				set.updateShort(4, (short) parameters.getDatumValueType().getCode());
			}
		}
	}

	private static final class DatumPropertyUUIDEntityRowMapper
			implements RowMapper<DatumPropertyUUIDEntity> {

		private final DatumPropertyUUIDEntityParametersRowMapper parametersMapper = new DatumPropertyUUIDEntityParametersRowMapper(
				3);

		@Override
		public DatumPropertyUUIDEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
			BasicDatumPropertyUUIDEntity row = new BasicDatumPropertyUUIDEntity();
			// Row order is: uuid_hi, uuid_lo, config_id
			row.setUuid(readUUID(1, rs));
			row.setConfigId(rs.getLong(3));

			DatumPropertyUUIDEntityParameters params = parametersMapper.mapRow(rs, rowNum);
			row.setParameters(params);

			return row;
		}

	}

	/**
	 * A {@link RowMapper} that maps columns to a
	 * {@link DatumPropertyUUIDEntityParameters} object.
	 * 
	 * <b>Note</b> this mapper will return {@literal null} if the parameters
	 * represent default values only!
	 */
	public static final class DatumPropertyUUIDEntityParametersRowMapper
			implements RowMapper<DatumPropertyUUIDEntityParameters> {

		private final int columnOffset;

		public DatumPropertyUUIDEntityParametersRowMapper(int columnOffset) {
			super();
			this.columnOffset = columnOffset;
		}

		@Override
		public DatumPropertyUUIDEntityParameters mapRow(ResultSet rs, int rowNum) throws SQLException {
			int col = columnOffset + 1;
			BasicDatumPropertyUUIDEntityParameters params = new BasicDatumPropertyUUIDEntityParameters();
			params.setDatumValueType(DatumValueType.forCodeValue(rs.getShort(col++)));

			// Return params instance, as long as some property is not a default value
			if ( params.isDefaultProperties() ) {
				return null;
			}
			return params;
		}

	}

}
