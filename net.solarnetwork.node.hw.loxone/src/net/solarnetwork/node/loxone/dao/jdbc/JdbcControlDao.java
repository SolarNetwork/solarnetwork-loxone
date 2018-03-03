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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.cache.Cache;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import net.solarnetwork.domain.SortDescriptor;
import net.solarnetwork.node.loxone.dao.ControlDao;
import net.solarnetwork.node.loxone.dao.SourceMappingDao;
import net.solarnetwork.node.loxone.dao.jdbc.JdbcDatumUUIDSetDao.DatumUUIDEntityParametersRowMapper;
import net.solarnetwork.node.loxone.domain.BasicControlDatumParameters;
import net.solarnetwork.node.loxone.domain.BasicValueEventDatumParameters;
import net.solarnetwork.node.loxone.domain.ConfigUUIDKey;
import net.solarnetwork.node.loxone.domain.Control;
import net.solarnetwork.node.loxone.domain.ControlDatumParameters;
import net.solarnetwork.node.loxone.domain.ControlType;
import net.solarnetwork.node.loxone.domain.DatumUUIDEntityParameters;
import net.solarnetwork.node.loxone.domain.DatumValueType;
import net.solarnetwork.node.loxone.domain.UUIDEntityParametersPair;

/**
 * JDBC implementation of {@link ControlDao}.
 * 
 * <b>Note</b> that the {@link Control#getSourceId()} value will not be
 * saved/updated when persisting entities. When querying via
 * {@link #findAllForConfig(Long, List)} or
 * {@link #findAllForDatumPropertyUUIDEntities(Long)}, however, the
 * {@code sourceId} will be populated by a matching row from the
 * {@link SourceMappingDao} data.
 * 
 * @author matt
 * @version 1.3
 */
public class JdbcControlDao extends BaseConfigurationEntityDao<Control> implements ControlDao {

	/** The default tables version. */
	public static final int TABLES_VERSION = 2;

	public static final String SQL_FIND_FOR_DATUM_PROPSET = "find-for-propset";

	public static final String SQL_FIND_FOR_STATE_UUID = "find-for-state-uuid";

	public static final String CONTROL_STATES_NAME = "control_states";
	public static final String SQL_CONTROL_STATES_DELETE_FOR_CONTROL = "delete-for-control";
	public static final String SQL_CONTROL_STATES_INSERT = "insert";
	public static final String SQL_CONTROL_STATES_FIND_FOR_CONTROL = "find-for-control";
	public static final String SQL_COUNT_FOR_CONFIG = "count-for-config";

	/**
	 * Constructor.
	 */
	public JdbcControlDao() {
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
	public JdbcControlDao(String sqlResourcePrefixTemplate, String tableNameTemplate) {
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
				PreparedStatement ps = con.prepareStatement(
						getSqlResource(String.format(getBaseSqlResourceTemplate() + "-%s",
								CONTROL_STATES_NAME, SQL_CONTROL_STATES_DELETE_FOR_CONTROL)));
				prepareUUID(1, control.getUuid(), ps);
				ps.setObject(3, control.getConfigId());
				ps.execute();
				if ( control.getStates() != null && !control.getStates().isEmpty() ) {
					// insert
					PreparedStatement statePs = con.prepareStatement(
							getSqlResource(String.format(getBaseSqlResourceTemplate() + "-%s",
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

	@Override
	protected void storeEntityInCache(Control entity) {
		Cache<ConfigUUIDKey, Control> cache = getEntityCache();
		if ( cache == null ) {
			return;
		}

		// if the control has states, also add those states to the cache to help with the findForConfigAndState
		Map<String, UUID> states = entity.getStates();
		if ( states == null || states.isEmpty() ) {
			super.storeEntityInCache(entity);
			return;
		}

		Map<ConfigUUIDKey, Control> cacheEntries = new HashMap<>(states.size());
		for ( UUID stateUuid : states.values() ) {
			cacheEntries.put(new ConfigUUIDKey(entity.getConfigId(), stateUuid), entity);
		}
		ConfigUUIDKey controlUuidKey = new ConfigUUIDKey(entity.getConfigId(), entity.getUuid());
		if ( !cacheEntries.containsKey(controlUuidKey) ) {
			cacheEntries.put(controlUuidKey, entity);
		}

		cache.putAll(cacheEntries);
	}

	@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
	@Override
	public Control load(Long configId, UUID uuid) {
		final Control result = getEntityByUUID(configId, uuid);
		if ( result != null && result.getStates() == null ) {
			final Map<String, UUID> stateMap = new LinkedHashMap<>(4);
			getJdbcTemplate().query(new PreparedStatementCreator() {

				@Override
				public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
					PreparedStatement ps = con.prepareStatement(
							getSqlResource(String.format(getBaseSqlResourceTemplate() + "-%s",
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
	public Control getForConfigAndState(Long configId, UUID stateUuid) {
		// check entity cache first
		Control result = getEntityFromCache(configId, stateUuid);
		if ( result != null ) {
			return result;
		}

		String sql = getSqlResource(SQL_FIND_FOR_STATE_UUID);
		List<Control> results = getJdbcTemplate().query(sql,
				new ControlWithStateRowMapper(getRowMapper()), configId,
				stateUuid.getMostSignificantBits(), stateUuid.getLeastSignificantBits());
		result = (results == null || results.isEmpty() ? null : results.get(0));
		if ( result != null ) {
			storeEntityInCache(result);
		}
		return result;
	}

	@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
	@Override
	public List<Control> findAllForConfig(Long configId, List<SortDescriptor> sortDescriptors) {
		String sql = getSqlResource(SQL_FIND_FOR_CONFIG);
		// SQL sorting not supported when grouping with states
		List<Control> results = getJdbcTemplate().query(sql,
				new ControlWithStateRowMapper(getRowMapper()), configId);
		return results;
	}

	@Override
	public List<Control> findAllForConfigAndName(Long configId, String name,
			List<SortDescriptor> sortDescriptors) {
		List<Control> results = (sortDescriptors == null
				? findAllForConfigAndNameFromCache(configId, name)
				: null);
		if ( results != null ) {
			return results;
		}
		String sql = getSqlResource(SQL_FIND_FOR_NAME);
		// SQL sorting not supported when grouping with states
		results = getJdbcTemplate().query(sql, new ControlWithStateRowMapper(getRowMapper()), configId,
				name);
		if ( results != null && sortDescriptors == null && !results.isEmpty() ) {
			storeEntitiesForConfigAndNameInCache(configId, name, results);
		}
		return results;
	}

	@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
	@Override
	public List<UUIDEntityParametersPair<Control, ControlDatumParameters>> findAllForDatumPropertyUUIDEntities(
			final Long configId) {
		if ( configId == null ) {
			return null;
		}
		return getJdbcTemplate().query(new PreparedStatementCreator() {

			@Override
			public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
				String sql = getSqlResource(SQL_FIND_FOR_DATUM_PROPSET);
				PreparedStatement ps = con.prepareStatement(sql);
				ps.setLong(1, configId.longValue());
				return ps;
			}
		}, new ControlDatumPropertyResultSetExtractor(getRowMapper(),
				new DatumUUIDEntityParametersRowMapper(11)));
	}

	@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
	@Override
	public int countForConfig(Long configId) {
		return getJdbcTemplate().queryForObject(getSqlResource(SQL_COUNT_FOR_CONFIG), Integer.class,
				configId);
	}

	@Override
	protected Map<String, String> sortDescriptorColumnMapping() {
		Map<String, String> m = new HashMap<String, String>();
		m.put("name", "lower(co.name)");
		m.put("defaultrating", "co.sort");
		return m;
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
			row.setSourceId(rs.getString(4));
			row.setName(rs.getString(5));
			row.setDefaultRating(rs.getInt(6));
			row.setType(ControlType.forIndexValue(rs.getShort(7)));
			row.setRoom(readUUID(8, rs));
			row.setCategory(readUUID(10, rs));
			return row;
		}
	}

	private static final class ControlWithStateRowMapper implements ResultSetExtractor<List<Control>> {

		private Control lastControl;
		private final RowMapper<Control> rowMapper;

		private ControlWithStateRowMapper(RowMapper<Control> rowMapper) {
			super();
			this.rowMapper = rowMapper;
		}

		@Override
		public List<Control> extractData(ResultSet rs) throws SQLException, DataAccessException {
			List<Control> results = new ArrayList<>(64);
			int i = 0;
			while ( rs.next() ) {
				Control row = rowMapper.mapRow(rs, i++);
				if ( lastControl == null || !lastControl.getUuid().equals(row.getUuid()) ) {
					// starting a new control
					results.add(row);
					lastControl = row;
				}
				Map<String, UUID> states = lastControl.getStates();
				if ( states == null ) {
					states = new LinkedHashMap<>(4);
					lastControl.setStates(states);
				}
				String state = rs.getString(12);
				UUID uuid = readUUID(13, rs);
				if ( state != null && uuid != null ) {
					states.put(state, uuid);
				}
			}
			return results;
		}

	}

	private static final class ControlDatumPropertyResultSetExtractor implements
			ResultSetExtractor<List<UUIDEntityParametersPair<Control, ControlDatumParameters>>> {

		private final RowMapper<Control> rowMapper;
		private final RowMapper<DatumUUIDEntityParameters> datumParamsRowMapper;

		private BasicControlDatumParameters lastParams;
		private UUIDEntityParametersPair<Control, ControlDatumParameters> lastPair;

		private ControlDatumPropertyResultSetExtractor(RowMapper<Control> rowMapper,
				RowMapper<DatumUUIDEntityParameters> datumParamsRowMapper) {
			super();
			this.rowMapper = rowMapper;
			this.datumParamsRowMapper = datumParamsRowMapper;
		}

		@Override
		public List<UUIDEntityParametersPair<Control, ControlDatumParameters>> extractData(ResultSet rs)
				throws SQLException, DataAccessException {
			List<UUIDEntityParametersPair<Control, ControlDatumParameters>> results = new ArrayList<>(
					64);
			int i = 0;
			while ( rs.next() ) {
				Control row = rowMapper.mapRow(rs, i++);
				if ( lastPair == null || !lastPair.getEntity().getUuid().equals(row.getUuid()) ) {
					// starting a new control
					lastParams = new BasicControlDatumParameters();
					lastParams.setDatumParameters(datumParamsRowMapper.mapRow(rs, i));
					lastPair = new UUIDEntityParametersPair<>(row, lastParams);
					results.add(lastPair);
				}

				// after control and datum params, columns start at 13 and are 
				// st.event_hi, st.event_lo, st.name AS event_name
				// ps.dtype,
				// ve.fvalue

				UUID stateUuid = readUUID(13, rs);
				BasicValueEventDatumParameters valueParams = (BasicValueEventDatumParameters) lastParams
						.getDatumPropertyParameters().get(stateUuid);
				if ( valueParams == null ) {
					valueParams = new BasicValueEventDatumParameters();
					lastParams.getDatumPropertyParameters().put(stateUuid, valueParams);
				}
				valueParams.setName(rs.getString(15));
				valueParams.setDatumValueType(DatumValueType.forCodeValue(rs.getInt(16)));
				valueParams.setValue(rs.getDouble(17));
			}
			return results;
		}

	}
}
