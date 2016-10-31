/* ==================================================================
 * ControlDatumDataSource.java - 2/10/2016 11:09:59 AM
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

package net.solarnetwork.node.loxone.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.solarnetwork.node.DatumDataSource;
import net.solarnetwork.node.MultiDatumDataSource;
import net.solarnetwork.node.Setting;
import net.solarnetwork.node.Setting.SettingFlag;
import net.solarnetwork.node.dao.SettingDao;
import net.solarnetwork.node.domain.GeneralNodeDatum;
import net.solarnetwork.node.loxone.dao.ControlDao;
import net.solarnetwork.node.loxone.domain.Config;
import net.solarnetwork.node.loxone.domain.Control;
import net.solarnetwork.node.loxone.domain.ControlDatumParameters;
import net.solarnetwork.node.loxone.domain.DatumUUIDEntityParameters;
import net.solarnetwork.node.loxone.domain.DatumValueType;
import net.solarnetwork.node.loxone.domain.UUIDEntityParametersPair;
import net.solarnetwork.node.loxone.domain.ValueEventDatumParameters;
import net.solarnetwork.node.support.KeyValuePair;

/**
 * A {@link DatumDataSource} to upload Loxone values on a fixed schedule.
 * 
 * @author matt
 * @version 1.2
 */
public class ControlDatumDataSource
		implements MultiDatumDataSource<GeneralNodeDatum>, DatumDataSource<GeneralNodeDatum> {

	/**
	 * The default interval at which to save {@code Datum} instances from Loxone
	 * value event data, in seconds, if not configured otherwise.
	 */
	public static final int DEFAULT_FREQUENCY_SECONDS = 60;

	private final ControlDao controlDao;
	private final SettingDao settingDao;
	private Long configId;
	private int defaultFrequencySeconds = DEFAULT_FREQUENCY_SECONDS;

	/**
	 * A setting key tempalte, takes a single string parameter (the config ID).
	 */
	public static final String SETTING_KEY_TEMPLATE = "loxone/%s/valueCaptured";

	/**
	 * Construct with values.
	 * 
	 * @param configId
	 *        The config ID.
	 * @param controlDao
	 *        The control DAO to use.
	 * @param settingDao
	 *        The setting DAO to use.
	 */
	public ControlDatumDataSource(Long configId, ControlDao controlDao, SettingDao settingDao) {
		super();
		this.configId = configId;
		this.controlDao = controlDao;
		this.settingDao = settingDao;
	}

	@Override
	public String getUID() {
		return "net.solarnetwork.node.loxone.impl.ControlDatumDataSource";
	}

	@Override
	public String getGroupUID() {
		return null;
	}

	@Override
	public Class<? extends GeneralNodeDatum> getDatumType() {
		return GeneralNodeDatum.class;
	}

	@Override
	public GeneralNodeDatum readCurrentDatum() {
		Collection<GeneralNodeDatum> multi = readMultipleDatum();
		return (multi == null || multi.isEmpty() ? null : multi.iterator().next());
	}

	@Override
	public Class<? extends GeneralNodeDatum> getMultiDatumType() {
		return GeneralNodeDatum.class;
	}

	@Override
	public Collection<GeneralNodeDatum> readMultipleDatum() {
		final String settingKey = settingKey();

		// load all Datum "last created" settings
		Map<String, String> createdSettings = loadCreationSettings(settingKey);

		List<UUIDEntityParametersPair<Control, ControlDatumParameters>> datumParameters = controlDao
				.findAllForDatumPropertyUUIDEntities(configId);

		if ( datumParameters == null ) {
			return null;
		}

		final Date now = new Date();
		final String nowSetting = Long.toString(now.getTime(), 16);
		List<GeneralNodeDatum> results = new ArrayList<>(datumParameters.size());

		for ( UUIDEntityParametersPair<Control, ControlDatumParameters> pair : datumParameters ) {
			final Control valueEvent = pair.getEntity();
			final String sourceId = valueEvent.getSourceIdValue();
			final ControlDatumParameters params = pair.getParameters();
			final DatumUUIDEntityParameters datumParams = (params != null ? params.getDatumParameters()
					: null);
			final Collection<ValueEventDatumParameters> propParamsList = (params != null
					? params.getDatumPropertyParameters().values() : null);
			boolean create = false;
			if ( propParamsList != null && !propParamsList.isEmpty()
					&& !(datumParams != null && datumParams.getSaveFrequencySeconds() != null
							&& datumParams.getSaveFrequencySeconds().intValue() < 0) ) {
				int offset = defaultFrequencySeconds;
				if ( datumParams != null && datumParams.getSaveFrequencySeconds() != null ) {
					offset = datumParams.getSaveFrequencySeconds().intValue();
				}
				final Long lastSaveTime = (createdSettings.containsKey(sourceId)
						? Long.valueOf(createdSettings.get(sourceId), 16) : null);
				if ( lastSaveTime == null || (lastSaveTime + (offset * 1000)) < now.getTime() ) {
					create = true;
				}
			}
			if ( !create ) {
				continue;
			}
			GeneralNodeDatum datum = new GeneralNodeDatum();
			datum.setSourceId(sourceId);
			datum.setCreated(now);
			for ( ValueEventDatumParameters propParams : propParamsList ) {
				String propName = propParams.getName();
				if ( propName == null ) {
					propName = "value";
				}
				if ( propParams.getDatumValueType() == null
						|| propParams.getDatumValueType() == DatumValueType.Unknown
						|| propParams.getDatumValueType() == DatumValueType.Instantaneous ) {
					datum.putInstantaneousSampleValue(propName, propParams.getValue());
				} else if ( propParams.getDatumValueType() == DatumValueType.Accumulating ) {
					datum.putAccumulatingSampleValue(propName, propParams.getValue());
				} else {
					datum.putStatusSampleValue(propName, propParams.getValue());
				}
			}
			results.add(datum);

			Setting s = new Setting(settingKey, sourceId, nowSetting,
					EnumSet.of(SettingFlag.Volatile, SettingFlag.IgnoreModificationDate));
			settingDao.storeSetting(s);
		}

		return results;

	}

	private String settingKey() {
		return String.format(SETTING_KEY_TEMPLATE,
				(configId == null ? "0" : Config.idToExternalForm(configId)));
	}

	private Map<String, String> loadCreationSettings(String key) {
		List<KeyValuePair> pairs = settingDao.getSettings(key);
		Map<String, String> result;
		if ( pairs != null && !pairs.isEmpty() ) {
			result = pairs.stream()
					.collect(Collectors.toMap(KeyValuePair::getKey, KeyValuePair::getValue));
		} else {
			result = Collections.emptyMap();
		}
		return result;
	}

	public void setDefaultFrequencySeconds(int defaultFrequencySeconds) {
		this.defaultFrequencySeconds = defaultFrequencySeconds;
	}

	public void setConfigId(Long configId) {
		this.configId = configId;
	}

}
