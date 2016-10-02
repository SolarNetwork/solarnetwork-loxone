/* ==================================================================
 * ValueEventDatumDataSource.java - 2/10/2016 11:09:59 AM
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
import net.solarnetwork.node.loxone.dao.ValueEventDao;
import net.solarnetwork.node.loxone.domain.Config;
import net.solarnetwork.node.loxone.domain.DatumUUIDEntityParameters;
import net.solarnetwork.node.loxone.domain.DatumValueType;
import net.solarnetwork.node.loxone.domain.UUIDEntityParametersPair;
import net.solarnetwork.node.loxone.domain.ValueEvent;
import net.solarnetwork.node.support.KeyValuePair;

/**
 * A {@link DatumDataSource} to upload Loxone values on a fixed schedule.
 * 
 * @author matt
 * @version 1.0
 */
public class ValueEventDatumDataSource implements MultiDatumDataSource<GeneralNodeDatum> {

	private final ValueEventDao valueEventDao;
	private final SettingDao settingDao;
	private final Long configId;
	private int defaultFrequencySeconds = 60;

	public static final String SETTING_KEY_PREFIX = "LoxoneValue.";

	public ValueEventDatumDataSource(Long configId, ValueEventDao valueEventDao, SettingDao settingDao) {
		super();
		this.configId = configId;
		this.valueEventDao = valueEventDao;
		this.settingDao = settingDao;
	}

	@Override
	public String getUID() {
		return "net.solarnetwork.node.loxone.impl.ValueEventDatumDataSource";
	}

	@Override
	public String getGroupUID() {
		return null;
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

		List<UUIDEntityParametersPair<ValueEvent, DatumUUIDEntityParameters>> datumParameters = valueEventDao
				.findAllForDatumUUIDEntities(configId);

		if ( datumParameters == null ) {
			return null;
		}

		final Date now = new Date();
		final String nowSetting = Long.toString(now.getTime(), 16);
		List<GeneralNodeDatum> results = new ArrayList<>(datumParameters.size());

		for ( UUIDEntityParametersPair<ValueEvent, DatumUUIDEntityParameters> pair : datumParameters ) {
			final ValueEvent valueEvent = pair.getEntity();
			final String sourceId = valueEvent.getSourceId();
			final DatumUUIDEntityParameters params = pair.getParameters();
			boolean create = false;
			if ( !(params != null && params.getSaveFrequencySeconds() != null
					&& params.getSaveFrequencySeconds().intValue() < 0) ) {
				int offset = defaultFrequencySeconds;
				if ( params != null && params.getSaveFrequencySeconds() != null ) {
					// maximum frequency supported is 1 hour, so wrap for anything past that
					offset = (params.getSaveFrequencySeconds().intValue() % 3600);
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
			if ( params == null || params.getDatumValueType() == null
					|| params.getDatumValueType() == DatumValueType.Unknown
					|| params.getDatumValueType() == DatumValueType.Instantaneous ) {
				datum.putInstantaneousSampleValue("value", valueEvent.getValue());
			} else if ( params.getDatumValueType() == DatumValueType.Accumulating ) {
				datum.putAccumulatingSampleValue("value", valueEvent.getValue());
			} else {
				datum.putStatusSampleValue("value", valueEvent.getValue());
			}
			results.add(datum);

			Setting s = new Setting(settingKey, sourceId, nowSetting,
					EnumSet.of(SettingFlag.Volatile, SettingFlag.IgnoreModificationDate));
			settingDao.storeSetting(s);
		}

		return results;
	}

	private String settingKey() {
		return SETTING_KEY_PREFIX + (configId == null ? "0" : Config.idToExternalForm(configId));
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

}
