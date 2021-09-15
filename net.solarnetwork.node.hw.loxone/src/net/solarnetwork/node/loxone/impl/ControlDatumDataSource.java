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

import static net.solarnetwork.domain.datum.DatumSamplesType.Accumulating;
import static net.solarnetwork.domain.datum.DatumSamplesType.Instantaneous;
import static net.solarnetwork.domain.datum.DatumSamplesType.Status;
import static net.solarnetwork.node.loxone.protocol.ws.LoxoneEvents.EVENT_PROPERTY_CONFIG_ID;
import static net.solarnetwork.node.loxone.protocol.ws.LoxoneEvents.EVENT_PROPERTY_DATE;
import static net.solarnetwork.node.loxone.protocol.ws.handler.ValueEventBinaryFileHandler.EVENT_PROPERTY_VALUE_EVENTS;
import static net.solarnetwork.node.loxone.protocol.ws.handler.ValueEventBinaryFileHandler.VALUE_EVENTS_UPDATED_EVENT;
import static net.solarnetwork.service.OptionalService.service;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.springframework.core.task.TaskExecutor;
import net.solarnetwork.domain.KeyValuePair;
import net.solarnetwork.domain.datum.DatumSamplesOperations;
import net.solarnetwork.domain.datum.MutableDatumSamplesOperations;
import net.solarnetwork.node.dao.SettingDao;
import net.solarnetwork.node.domain.Setting;
import net.solarnetwork.node.domain.Setting.SettingFlag;
import net.solarnetwork.node.domain.datum.NodeDatum;
import net.solarnetwork.node.domain.datum.SimpleDatum;
import net.solarnetwork.node.loxone.dao.ControlDao;
import net.solarnetwork.node.loxone.domain.Config;
import net.solarnetwork.node.loxone.domain.Control;
import net.solarnetwork.node.loxone.domain.ControlDatumParameters;
import net.solarnetwork.node.loxone.domain.DatumUUIDEntityParameters;
import net.solarnetwork.node.loxone.domain.DatumValueType;
import net.solarnetwork.node.loxone.domain.UUIDEntityParametersPair;
import net.solarnetwork.node.loxone.domain.ValueEvent;
import net.solarnetwork.node.loxone.domain.ValueEventDatumParameters;
import net.solarnetwork.node.service.DatumDataSource;
import net.solarnetwork.node.service.DatumQueue;
import net.solarnetwork.node.service.MultiDatumDataSource;
import net.solarnetwork.node.service.support.DatumDataSourceSupport;

/**
 * A {@link DatumDataSource} to upload Loxone values on a fixed schedule.
 * 
 * @author matt
 * @version 2.0
 */
public class ControlDatumDataSource extends DatumDataSourceSupport
		implements MultiDatumDataSource, DatumDataSource, EventHandler {

	/**
	 * The default interval at which to save {@code Datum} instances from Loxone
	 * value event data, in seconds, if not configured otherwise.
	 */
	public static final int DEFAULT_FREQUENCY_SECONDS = 60;

	/**
	 * The default {@code datumDaoPersistOnlyStatusUpdates} property value.
	 * 
	 * @since 1.6
	 */
	public static final boolean DEFAULT_PERSIST_ONLY_STATUS_UPDATES = true;

	private final ControlDao controlDao;
	private final SettingDao settingDao;
	private Long configId;
	private int defaultFrequencySeconds = DEFAULT_FREQUENCY_SECONDS;
	private boolean datumPersistOnlyStatusUpdates = DEFAULT_PERSIST_ONLY_STATUS_UPDATES;
	private TaskExecutor taskExecutor;

	/**
	 * A setting key template, takes a single string parameter (the config ID).
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
	public Class<? extends NodeDatum> getDatumType() {
		return NodeDatum.class;
	}

	@Override
	public NodeDatum readCurrentDatum() {
		Collection<NodeDatum> multi = readMultipleDatum();
		return (multi == null || multi.isEmpty() ? null : multi.iterator().next());
	}

	@Override
	public Class<? extends NodeDatum> getMultiDatumType() {
		return NodeDatum.class;
	}

	@Override
	public Collection<NodeDatum> readMultipleDatum() {
		final String settingKey = settingKey();

		// load all Datum "last created" settings
		Map<String, String> createdSettings = loadCreationSettings(settingKey);

		List<UUIDEntityParametersPair<Control, ControlDatumParameters>> datumParameters = controlDao
				.findAllForDatumPropertyUUIDEntities(configId);

		if ( datumParameters == null ) {
			return null;
		}

		final boolean statusOnly = isDatumPersistOnlyStatusUpdates();
		final Instant now = Instant.now();
		final String nowSetting = Long.toString(now.toEpochMilli(), 16);
		List<NodeDatum> results = new ArrayList<>(datumParameters.size());

		for ( UUIDEntityParametersPair<Control, ControlDatumParameters> pair : datumParameters ) {
			NodeDatum datum = generateDatum(now, pair, createdSettings);
			if ( datum == null ) {
				continue;
			}

			final ControlDatumParameters params = pair.getParameters();
			final DatumUUIDEntityParameters datumParams = (params != null ? params.getDatumParameters()
					: null);
			DatumSamplesOperations ops = datum.asSampleOperations();
			if ( statusOnly && (datumParams == null || datumParams.getSaveFrequencySeconds() == null)
					&& ops != null && ops.getSampleData(Instantaneous) == null
					&& ops.getSampleData(Accumulating) == null && ops.getSampleData(Status) != null ) {
				// no datum-specific frequency provided and only Status properties collected; don't collect
				// this datum for the default schedule
				log.debug("Ignoring scheduled datum because contains only status properties: {}", datum);
				continue;
			}

			results.add(datum);

			Setting s = new Setting(settingKey, datum.getSourceId(), nowSetting,
					EnumSet.of(SettingFlag.Volatile, SettingFlag.IgnoreModificationDate));
			settingDao.storeSetting(s);
		}

		return results;
	}

	@Override
	public void handleEvent(Event event) {
		if ( event == null || event.getTopic() == null
				|| !event.getTopic().equals(VALUE_EVENTS_UPDATED_EVENT) ) {
			return;
		}
		if ( !(event.getProperty(EVENT_PROPERTY_CONFIG_ID) instanceof Long) ) {
			return;
		}
		Long configId = (Long) event.getProperty(EVENT_PROPERTY_CONFIG_ID);
		if ( !configId.equals(this.configId) ) {
			return;
		}
		if ( !(event.getProperty(EVENT_PROPERTY_VALUE_EVENTS) instanceof Collection<?>) ) {
			return;
		}
		if ( !(event.getProperty(EVENT_PROPERTY_DATE) instanceof Long) ) {
			return;
		}
		final Instant now = Instant.ofEpochMilli((Long) event.getProperty(EVENT_PROPERTY_DATE));
		@SuppressWarnings("unchecked")
		Collection<ValueEvent> valueEvents = (Collection<ValueEvent>) event
				.getProperty(EVENT_PROPERTY_VALUE_EVENTS);
		if ( valueEvents.isEmpty() ) {
			return;
		}

		Runnable task = new Runnable() {

			@Override
			public void run() {
				log.trace("Got VALUE_EVENTS_UPDATED event: {}", valueEvents);

				Set<UUID> valueEventIds = valueEvents.stream().map(e -> e.getUuid())
						.collect(Collectors.toSet());

				List<UUIDEntityParametersPair<Control, ControlDatumParameters>> datumParameters = controlDao
						.findAllForDatumPropertyUUIDEntities(configId);

				for ( UUIDEntityParametersPair<Control, ControlDatumParameters> pair : datumParameters ) {
					final Control control = pair.getEntity();
					final Collection<UUID> controlStates = (control.getStates() != null
							? control.getStates().values()
							: Collections.emptySet());
					if ( !(valueEventIds.contains(control.getUuid()) || controlStates.stream()
							.filter(u -> valueEventIds.contains(u)).findAny().isPresent()) ) {
						continue;
					}
					NodeDatum d = generateDatum(now, pair, null);
					if ( d != null ) {
						persistDatum(valueEventIds, pair, d);
					}
				}
			}
		};

		TaskExecutor te = getTaskExecutor();
		if ( te != null ) {
			te.execute(task);
		} else {
			task.run();
		}
	}

	private void persistDatum(Set<UUID> valueEventIds,
			UUIDEntityParametersPair<Control, ControlDatumParameters> pair, NodeDatum d) {
		final DatumQueue queue = service(getDatumQueue());
		if ( queue == null ) {
			return;
		}
		final ControlDatumParameters params = pair.getParameters();
		final Map<UUID, ValueEventDatumParameters> propParams = (params != null
				? params.getDatumPropertyParameters()
				: null);
		if ( propParams == null ) {
			return;
		}

		final boolean statusOnly = isDatumPersistOnlyStatusUpdates();
		boolean updateForStatusProperty = false;
		if ( statusOnly ) {
			for ( UUID eventUuid : valueEventIds ) {
				ValueEventDatumParameters vedp = propParams.get(eventUuid);
				if ( vedp != null && vedp.getDatumValueType() == DatumValueType.Status ) {
					updateForStatusProperty = true;
					break;
				}
			}
		}

		if ( statusOnly && !updateForStatusProperty ) {
			return;
		}
		log.info("Persisting datum because of updated status property: {}", d);
		queue.offer(d);
	}

	private NodeDatum generateDatum(Instant now,
			UUIDEntityParametersPair<Control, ControlDatumParameters> pair,
			Map<String, String> createdSettings) {
		final Control valueEvent = pair.getEntity();
		final String sourceId = valueEvent.getSourceIdValue();
		final ControlDatumParameters params = pair.getParameters();
		final DatumUUIDEntityParameters datumParams = (params != null ? params.getDatumParameters()
				: null);
		final Collection<ValueEventDatumParameters> propParamsList = (params != null
				? params.getDatumPropertyParameters().values()
				: null);
		boolean create = false;
		if ( propParamsList != null && !propParamsList.isEmpty()
				&& !(datumParams != null && datumParams.getSaveFrequencySeconds() != null
						&& datumParams.getSaveFrequencySeconds().intValue() < 0) ) {
			int offset = defaultFrequencySeconds;
			if ( datumParams != null && datumParams.getSaveFrequencySeconds() != null ) {
				offset = datumParams.getSaveFrequencySeconds().intValue();
			}
			if ( createdSettings != null ) {
				final Long lastSaveTime = (createdSettings.containsKey(sourceId)
						? Long.valueOf(createdSettings.get(sourceId), 16)
						: null);
				if ( lastSaveTime == null || (lastSaveTime + (offset * 1000)) < now.toEpochMilli() ) {
					create = true;
				}
			} else {
				create = true;
			}
		}
		if ( !create ) {
			return null;
		}
		SimpleDatum datum = SimpleDatum.nodeDatum(resolvePlaceholders(sourceId), now);
		MutableDatumSamplesOperations ops = datum.asMutableSampleOperations();
		for ( ValueEventDatumParameters propParams : propParamsList ) {
			String propName = propParams.getName();
			if ( propName == null ) {
				propName = "value";
			}
			if ( propParams.getDatumValueType() == null
					|| propParams.getDatumValueType() == DatumValueType.Unknown
					|| propParams.getDatumValueType() == DatumValueType.Instantaneous ) {
				ops.putSampleValue(Instantaneous, propName, propParams.getValue());
			} else if ( propParams.getDatumValueType() == DatumValueType.Accumulating ) {
				ops.putSampleValue(Accumulating, propName, propParams.getValue());
			} else {
				ops.putSampleValue(Status, propName, propParams.getValue());
			}
		}
		return datum;
	}

	private String settingKey() {
		return String.format(SETTING_KEY_TEMPLATE,
				(configId == null ? "0" : Config.idToExternalForm(configId)));
	}

	private Map<String, String> loadCreationSettings(String key) {
		List<KeyValuePair> pairs = settingDao.getSettingValues(key);
		Map<String, String> result;
		if ( pairs != null && !pairs.isEmpty() ) {
			result = pairs.stream()
					.collect(Collectors.toMap(KeyValuePair::getKey, KeyValuePair::getValue));
		} else {
			result = Collections.emptyMap();
		}
		return result;
	}

	/**
	 * Set the default frequency seconds.
	 * 
	 * @param defaultFrequencySeconds
	 *        the default frequency, in seconds
	 */
	public void setDefaultFrequencySeconds(int defaultFrequencySeconds) {
		this.defaultFrequencySeconds = defaultFrequencySeconds;
	}

	/**
	 * Set the config ID.
	 * 
	 * @param configId
	 *        the config ID
	 */
	public void setConfigId(Long configId) {
		this.configId = configId;
	}

	/**
	 * Get the status-change-only datum persistence setting.
	 * 
	 * @return {@literal true} to only persist datum that are updated;
	 *         {@literal false} to persist all updates; defaults to
	 *         {@link #DEFAULT_PERSIST_ONLY_STATUS_UPDATES}
	 * @since 2.0
	 */
	public boolean isDatumPersistOnlyStatusUpdates() {
		return datumPersistOnlyStatusUpdates;
	}

	/**
	 * Toggle the status-change-only datum persistence setting.
	 * 
	 * @param datumPersistOnlyStatusUpdates
	 *        {@literal true} to only persist datum that are updated from status
	 *        property type changes
	 * @since 2.0
	 */
	public void setDatumPersistOnlyStatusUpdates(boolean datumPersistOnlyStatusUpdates) {
		this.datumPersistOnlyStatusUpdates = datumPersistOnlyStatusUpdates;
	}

	/**
	 * Get the task executor.
	 * 
	 * @return the executor
	 * @since 1.6
	 */
	public TaskExecutor getTaskExecutor() {
		return taskExecutor;
	}

	/**
	 * Set the task executor.
	 * 
	 * <p>
	 * This must be configured for status update events to be handled.
	 * </p>
	 * 
	 * @param taskExecutor
	 *        the executor to set
	 * @since 1.6
	 */
	public void setTaskExecutor(TaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}

}
