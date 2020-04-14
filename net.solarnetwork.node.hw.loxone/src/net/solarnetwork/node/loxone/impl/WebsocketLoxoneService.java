/* ==================================================================
 * WebsocketLoxoneService.java - 21/09/2016 4:37:10 PM
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

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;
import org.osgi.service.event.Event;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.context.MessageSource;
import org.springframework.core.io.Resource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import net.solarnetwork.domain.NodeControlInfo;
import net.solarnetwork.domain.NodeControlPropertyType;
import net.solarnetwork.domain.SortDescriptor;
import net.solarnetwork.node.NodeControlProvider;
import net.solarnetwork.node.RemoteServiceException;
import net.solarnetwork.node.dao.DatumDao;
import net.solarnetwork.node.dao.SettingDao;
import net.solarnetwork.node.domain.GeneralNodeDatum;
import net.solarnetwork.node.domain.NodeControlInfoDatum;
import net.solarnetwork.node.job.DatumDataSourceLoggerJob;
import net.solarnetwork.node.loxone.LoxoneService;
import net.solarnetwork.node.loxone.LoxoneSourceMappingParser;
import net.solarnetwork.node.loxone.dao.ConfigurationEntityDao;
import net.solarnetwork.node.loxone.dao.ControlDao;
import net.solarnetwork.node.loxone.dao.EventEntityDao;
import net.solarnetwork.node.loxone.dao.SourceMappingDao;
import net.solarnetwork.node.loxone.dao.UUIDSetDao;
import net.solarnetwork.node.loxone.domain.AuthenticationTokenPermission;
import net.solarnetwork.node.loxone.domain.Config;
import net.solarnetwork.node.loxone.domain.ConfigurationEntity;
import net.solarnetwork.node.loxone.domain.Control;
import net.solarnetwork.node.loxone.domain.ControlDatumParameters;
import net.solarnetwork.node.loxone.domain.EventEntity;
import net.solarnetwork.node.loxone.domain.SourceMapping;
import net.solarnetwork.node.loxone.domain.UUIDEntityParameters;
import net.solarnetwork.node.loxone.domain.UUIDEntityParametersPair;
import net.solarnetwork.node.loxone.domain.UUIDSetEntity;
import net.solarnetwork.node.loxone.domain.ValueEvent;
import net.solarnetwork.node.loxone.domain.command.ControlCommand;
import net.solarnetwork.node.loxone.protocol.ws.AuthenticationType;
import net.solarnetwork.node.loxone.protocol.ws.CommandType;
import net.solarnetwork.node.loxone.protocol.ws.LoxoneEndpoint;
import net.solarnetwork.node.reactor.Instruction;
import net.solarnetwork.node.reactor.InstructionHandler;
import net.solarnetwork.node.reactor.InstructionStatus.InstructionState;
import net.solarnetwork.node.settings.SettingSpecifier;
import net.solarnetwork.node.settings.SettingSpecifierProvider;
import net.solarnetwork.node.settings.support.BasicMultiValueSettingSpecifier;
import net.solarnetwork.node.settings.support.BasicSetupResourceSettingSpecifier;
import net.solarnetwork.node.settings.support.BasicTextFieldSettingSpecifier;
import net.solarnetwork.node.settings.support.BasicTitleSettingSpecifier;
import net.solarnetwork.node.setup.SetupResourceProvider;
import net.solarnetwork.util.OptionalService;

/**
 * Websocket based implementation of {@link LoxoneService}.
 * 
 * @author matt
 * @version 1.8
 */
public class WebsocketLoxoneService extends LoxoneEndpoint
		implements LoxoneService, SettingSpecifierProvider, WebsocketLoxoneServiceSettings,
		NodeControlProvider, InstructionHandler {

	/**
	 * The name used to schedule the {@link PostOfflineChargeSessionsJob} as.
	 */
	public static final String DATUM_LOGGER_JOB_NAME = "Loxone_DatumLogger";

	/**
	 * The job and trigger group used to schedule the
	 * {@link DatumDataSourceLoggerJob} with. Note the trigger name will be the
	 * {@link #getUID()} property value.
	 */
	public static final String SCHEDULER_GROUP = "Loxone";

	/**
	 * The default minimum interval at which to save {@code Datum} instances
	 * from Loxone value event data, in seconds.
	 */
	public static final int DATUM_LOGGER_JOB_INTERVAL = 60;

	private static final String DEFAULT_UID = "Loxone";

	private String uid = DEFAULT_UID;
	private String groupUID;
	private List<ConfigurationEntityDao<ConfigurationEntity>> configurationDaos;
	private List<EventEntityDao<? extends EventEntity>> eventDaos;
	private List<UUIDSetDao<UUIDSetEntity<UUIDEntityParameters>, UUIDEntityParameters>> uuidSetDaos;
	private SetupResourceProvider settingResourceProvider;
	private SettingDao settingDao;
	private ControlDao controlDao;
	private SourceMappingDao sourceMappingDao;
	private OptionalService<DatumDao<GeneralNodeDatum>> datumDao;
	private Scheduler scheduler;
	private int datumLoggerFrequencySeconds = DATUM_LOGGER_JOB_INTERVAL;
	private MessageSource messageSource;

	private ControlDatumDataSource datumDataSource;
	private SimpleTrigger datumLoggerTrigger;

	@Override
	public void init() {
		super.init();
		datumDataSource = new ControlDatumDataSource(null, controlDao, settingDao);
		datumDataSource.setEventAdmin(getEventAdmin());
	}

	@Override
	public synchronized void disconnect() {
		super.disconnect();
		configureLoxoneDatumLoggerJob(0);
	}

	@Override
	public void onClose(Session session, CloseReason closeReason) {
		super.onClose(session, closeReason);
		configureLoxoneDatumLoggerJob(0);
	}

	@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
	@Override
	public <T extends ConfigurationEntity> Collection<T> getAllConfiguration(Class<T> type,
			List<SortDescriptor> sortDescriptors) {
		Collection<T> result = null;
		Config config = getConfiguration();
		ConfigurationEntityDao<T> dao = configurationDaoForType(type);
		if ( dao != null && config.getId() != null ) {
			result = dao.findAllForConfig(config.getId(), sortDescriptors);
		}
		return result;
	}

	@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
	@Override
	public <T extends EventEntity> Collection<T> getAllEvents(Class<T> type,
			List<SortDescriptor> sortDescriptors) {
		Collection<T> result = null;
		Config config = getConfiguration();
		EventEntityDao<T> dao = eventDaoForType(type);
		if ( dao != null && config.getId() != null ) {
			result = dao.findAllForConfig(config.getId(), sortDescriptors);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized Future<Resource> getImage(String name) {
		Future<Resource> result;
		try {
			result = (Future<Resource>) sendCommandIfPossible(CommandType.GetIcon, name);
		} catch ( IOException e ) {
			throw new RemoteServiceException(e);
		}
		return result;
	}

	@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
	@Override
	public List<Control> findControlsForName(String name, List<SortDescriptor> sortDescriptors) {
		Config config = getConfiguration();
		if ( config == null || config.getId() == null ) {
			return Collections.emptyList();
		}
		return controlDao.findAllForConfigAndName(config.getId(), name, sortDescriptors);
	}

	@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
	@Override
	public Control getControlForState(UUID uuid) {
		Config config = getConfiguration();
		if ( config == null || config.getId() == null ) {
			return null;
		}
		return controlDao.getForConfigAndState(config.getId(), uuid);
	}

	@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
	@Override
	public ValueEvent getControlState(UUID uuid) {
		Config config = getConfiguration();
		if ( config == null || config.getId() == null ) {
			return null;
		}
		EventEntityDao<ValueEvent> valueEventDao = eventDaoForType(ValueEvent.class);
		return valueEventDao.loadEvent(config.getId(), uuid);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Future<List<ValueEvent>> setControlState(UUID uuid, Double state) {
		Future<List<ValueEvent>> result;
		try {
			result = (Future<List<ValueEvent>>) sendCommandIfPossible(CommandType.IoControl, uuid,
					state);
		} catch ( IOException e ) {
			throw new RemoteServiceException(e);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Future<List<ValueEvent>> sendControlCommand(ControlCommand command) {
		Future<List<ValueEvent>> result;
		try {
			result = (Future<List<ValueEvent>>) sendCommandIfPossible(CommandType.IoControl,
					command.getUuid(), command);
		} catch ( IOException e ) {
			throw new RemoteServiceException(e);
		}
		return result;
	}

	@Override
	public <T extends UUIDSetEntity<P>, P extends UUIDEntityParameters> Map<UUID, P> getUUIDSet(
			Class<T> type, List<SortDescriptor> sortDescriptors) {
		Map<UUID, P> result = null;
		Config config = getConfiguration();
		if ( config != null && config.getId() != null ) {
			UUIDSetDao<T, P> dao = uuidSetDaoForType(type);
			Collection<T> entities = null;
			if ( dao != null ) {
				entities = dao.findAllForConfig(config.getId(), sortDescriptors);
			}
			if ( entities != null ) {
				result = new LinkedHashMap<>(entities.size());
				for ( T entity : entities ) {
					result.put(entity.getUuid(), entity.getParameters());
				}
			}
		}
		return result;
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	@Override
	public <T extends UUIDSetEntity<P>, P extends UUIDEntityParameters> void updateUUIDSet(Class<T> type,
			Collection<UUID> add, Collection<UUID> remove, Map<UUID, P> parameters) {
		Config config = getConfiguration();
		if ( config != null && config.getId() != null ) {
			UUIDSetDao<T, P> dao = uuidSetDaoForType(type);
			if ( dao != null && config.getId() != null ) {
				dao.updateSetForConfig(config.getId(), add, remove, parameters);
			}
		}
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	@Override
	public void updateSourceMappings(Collection<SourceMapping> store, Collection<UUID> remove) {
		final Config config = getConfiguration();
		final Long configId = (config != null ? config.getId() : null);
		if ( configId == null ) {
			return;
		}
		if ( store != null ) {
			for ( SourceMapping smap : store ) {
				smap.setConfigId(configId);
				sourceMappingDao.store(smap);
			}
		}
		if ( remove != null ) {
			for ( UUID uuid : remove ) {
				sourceMappingDao.delete(configId, uuid);
			}
		}
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	@Override
	public void importSourceMappings(InputStream in, LoxoneSourceMappingParser parser)
			throws IOException {
		assert in != null;
		assert parser != null;

		final Config config = getConfiguration();
		final Long configId = (config != null ? config.getId() : null);
		if ( configId == null ) {
			return;
		}
		final String configKey = Config.idToExternalForm(configId);
		final int sourceIdMaxLength = ConfigurationEntity.SOURCE_ID_MAX_LENGTH - configKey.length() - 2;

		parser.parseInputStream(in, new LoxoneSourceMappingParser.SourceMappingCallback() {

			@Override
			public void parsedSourceMapping(SourceMapping mapping) {
				mapping.setConfigId(configId);

				// sanitize source ID
				String sourceId = mapping.getSourceId();
				if ( sourceId == null ) {
					return;
				}

				// remove whitespace
				sourceId = ConfigurationEntity.SOURCE_ID_REMOVE_PAT.matcher(sourceId).replaceAll("");

				// verify length
				if ( sourceId.length() > sourceIdMaxLength ) {
					sourceId = sourceId.substring(0, sourceIdMaxLength);
				}

				// save back onto mapping
				mapping.setSourceId(sourceId);

				sourceMappingDao.store(mapping);
			}
		});
	}

	@SuppressWarnings("unchecked")
	private <T extends ConfigurationEntity> ConfigurationEntityDao<T> configurationDaoForType(
			Class<T> type) {
		if ( configurationDaos != null ) {
			for ( ConfigurationEntityDao<ConfigurationEntity> dao : configurationDaos ) {
				if ( type.isAssignableFrom(dao.entityClass()) ) {
					return (ConfigurationEntityDao<T>) dao;
				}
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private <T extends EventEntity> EventEntityDao<T> eventDaoForType(Class<T> type) {
		if ( eventDaos != null ) {
			for ( EventEntityDao<? extends EventEntity> dao : eventDaos ) {
				if ( type.isAssignableFrom(dao.entityClass()) ) {
					return (EventEntityDao<T>) dao;
				}
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private <T extends UUIDSetEntity<P>, P extends UUIDEntityParameters> UUIDSetDao<T, P> uuidSetDaoForType(
			Class<T> type) {
		if ( uuidSetDaos != null ) {
			for ( UUIDSetDao<UUIDSetEntity<UUIDEntityParameters>, UUIDEntityParameters> dao : uuidSetDaos ) {
				if ( type.isAssignableFrom(dao.entityClass()) ) {
					return (UUIDSetDao<T, P>) dao;
				}
			}
		}
		return null;
	}

	@Override
	protected void handleEvent(Event event, Long configId) {
		if ( datumDataSource != null ) {
			datumDataSource.handleEvent(event);
		}
	}

	@Override
	public String getUID() {
		return uid;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	@Override
	public String getGroupUID() {
		return groupUID;
	}

	public void setGroupUID(String groupUID) {
		this.groupUID = groupUID;
	}

	private String getConfigurationIdExternalForm() {
		Config config = getConfiguration();
		return (config == null ? null : config.idToExternalForm());
	}

	@Override
	public String getSettingUID() {
		return "net.solarnetwork.node.loxone.ws";
	}

	@Override
	public String getDisplayName() {
		return "Loxone Miniserver";
	}

	@Override
	public MessageSource getMessageSource() {
		return messageSource;
	}

	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	@Override
	public List<SettingSpecifier> getSettingSpecifiers() {
		WebsocketLoxoneService defaults = new WebsocketLoxoneService();
		List<SettingSpecifier> results = new ArrayList<SettingSpecifier>(16);

		if ( isAuthenticationFailure() ) {
			results.add(new BasicTitleSettingSpecifier("authFailure",
					messageSource.getMessage("error.authFailure", null, null), true));
		}

		// service settings
		results.add(new BasicTextFieldSettingSpecifier("uid", DEFAULT_UID));
		results.add(new BasicTextFieldSettingSpecifier("groupUID", null));
		results.add(new BasicTextFieldSettingSpecifier("configKey", defaults.getConfigKey()));
		results.add(new BasicTextFieldSettingSpecifier("host", defaults.getHost()));

		// authentication
		results.add(new BasicTextFieldSettingSpecifier("username", defaults.getUsername()));
		results.add(new BasicTextFieldSettingSpecifier("password", defaults.getPassword(), true));

		BasicMultiValueSettingSpecifier authTypeSpec = new BasicMultiValueSettingSpecifier(
				"authenticationTypeCode", String.valueOf(defaults.getAuthenticationTypeCode()));
		authTypeSpec.setValueTitles(Arrays.stream(AuthenticationType.values())
				.collect(Collectors.toMap(t -> String.valueOf(t.getCode()), t -> t.toString())));
		results.add(authTypeSpec);

		BasicMultiValueSettingSpecifier tokenPermSpec = new BasicMultiValueSettingSpecifier(
				"tokenRequestPermissionCode", String.valueOf(defaults.getTokenRequestPermissionCode()));
		tokenPermSpec.setValueTitles(Arrays.stream(AuthenticationTokenPermission.values())
				.collect(Collectors.toMap(t -> String.valueOf(t.getCode()), t -> t.toString())));
		results.add(tokenPermSpec);

		results.add(new BasicTextFieldSettingSpecifier("tokenRefreshOffsetHours",
				String.valueOf(defaults.getTokenRefreshOffsetHours())));

		// datum logging
		results.add(new BasicTextFieldSettingSpecifier("datumLoggerFrequencySeconds",
				String.valueOf(DATUM_LOGGER_JOB_INTERVAL)));
		results.add(new BasicTextFieldSettingSpecifier("datumDataSource.defaultFrequencySeconds",
				String.valueOf(ControlDatumDataSource.DEFAULT_FREQUENCY_SECONDS)));

		// other
		String configurationId = getConfigurationIdExternalForm();
		if ( configurationId != null ) {
			results.add(new BasicTitleSettingSpecifier("configurationId", configurationId, true));
		}

		if ( settingResourceProvider != null ) {
			Map<String, Object> setupProps = Collections.singletonMap("config-id", configurationId);
			results.add(new BasicSetupResourceSettingSpecifier(settingResourceProvider, setupProps));
		}

		return results;
	}

	@Override
	protected Config configurationIdDidChange() {
		super.configurationIdDidChange();
		configureLoxoneDatumLoggerJob(0);
		Config config = getConfiguration();
		if ( config == null || config.getId() == null ) {
			return null;
		}

		Config result = null;

		// if we have a last modified date, but no actual controls, assume we have restored
		// from backup and need to refresh from the miniserver
		if ( config.getLastModified() != null ) {
			int controlCount = controlDao.countForConfig(config.getId());
			if ( controlCount < 1 ) {
				log.info("Loxone {} control information not available: forcing a refresh.",
						config.idToExternalForm());
				result = config.withLastModified(null);
			}
		}

		datumDataSource.setConfigId(config.getId());

		return result;
	}

	@Override
	public void onOpen(Session session, EndpointConfig config) {
		super.onOpen(session, config);
		scheduleDatumLoggerJobIfNeeded();
	}

	private void scheduleDatumLoggerJobIfNeeded() {
		configureLoxoneDatumLoggerJob(datumLoggerFrequencySeconds * 1000L);
	}

	private boolean configureLoxoneDatumLoggerJob(final long interval) {
		final Config config = getConfiguration();
		if ( config == null || config.getId() == null ) {
			return false;
		}
		final String configIdDisplay = config.idToExternalForm();
		final Scheduler sched = scheduler;

		if ( sched == null ) {
			log.info("No scheduler avaialable, cannot schedule Loxone {} datum logger job",
					configIdDisplay);
			return false;
		}
		final DatumDao<GeneralNodeDatum> dao = (datumDao != null ? datumDao.service() : null);
		if ( dao == null ) {
			log.info(
					"No DatumDao<GeneralNodeDatum> avaialable, cannot schedule Loxone {} datum logger job",
					configIdDisplay);
			return false;
		}
		SimpleTrigger trigger = datumLoggerTrigger;
		if ( trigger != null ) {
			// check if interval actually changed
			if ( trigger.getRepeatInterval() == interval ) {
				log.debug("Loxone {} datum logger interval unchanged at {}s", configIdDisplay, interval);
				return true;
			}
			// trigger has changed!
			if ( interval == 0 ) {
				try {
					sched.unscheduleJob(trigger.getKey());
					log.info("Unscheduled Loxone {} datum logger job", configIdDisplay);
				} catch ( SchedulerException e ) {
					log.error("Error unscheduling Loxone {} datum logger job", configIdDisplay, e);
				} finally {
					datumLoggerTrigger = null;
				}
			} else {
				trigger = TriggerBuilder.newTrigger().withIdentity(trigger.getKey())
						.forJob(DATUM_LOGGER_JOB_NAME, SCHEDULER_GROUP)
						.withSchedule(
								SimpleScheduleBuilder.repeatMinutelyForever((int) (interval / (60000L))))
						.build();
				try {
					sched.rescheduleJob(trigger.getKey(), trigger);
				} catch ( SchedulerException e ) {
					log.error("Error rescheduling Loxone {} datum logger job", configIdDisplay, e);
				} finally {
					datumLoggerTrigger = null;
				}
			}
			return true;
		} else if ( interval == 0 ) {
			return true;
		}

		synchronized ( sched ) {
			try {
				final JobKey jobKey = new JobKey(DATUM_LOGGER_JOB_NAME, SCHEDULER_GROUP);
				JobDetail jobDetail = sched.getJobDetail(jobKey);
				if ( jobDetail == null ) {
					jobDetail = JobBuilder.newJob(DatumDataSourceLoggerJob.class).withIdentity(jobKey)
							.storeDurably().build();
					sched.addJob(jobDetail, true);
				}
				final TriggerKey triggerKey = new TriggerKey(
						DATUM_LOGGER_JOB_NAME + config.idToExternalForm(), SCHEDULER_GROUP);
				final Map<String, Object> jd = new HashMap<>();
				jd.put("datumDataSource", datumDataSource);
				jd.put("datumDao", dao);
				trigger = TriggerBuilder.newTrigger().withIdentity(triggerKey).forJob(jobKey)
						.startAt(new Date(System.currentTimeMillis() + interval))
						.usingJobData(new JobDataMap(jd))
						.withSchedule(
								SimpleScheduleBuilder.repeatSecondlyForever((int) (interval / (1000L)))
										.withMisfireHandlingInstructionNextWithExistingCount())
						.build();
				sched.scheduleJob(trigger);
				log.info("Scheduled Loxone {} datum logger job to run every {} seconds", configIdDisplay,
						(interval / 1000));
				datumLoggerTrigger = trigger;
				return true;
			} catch ( Exception e ) {
				log.error("Error scheduling Loxone {} datum logger job", configIdDisplay, e);
				return false;
			}
		}
	}

	// NodeControlProvider

	@Override
	public List<String> getAvailableControlIds() {
		Config config = getConfiguration();
		if ( config == null || config.getId() == null ) {
			return Collections.emptyList();
		}

		// return source IDs for all controls configured as datum

		List<UUIDEntityParametersPair<Control, ControlDatumParameters>> controlParameters = controlDao
				.findAllForDatumPropertyUUIDEntities(config.getId());

		return controlParameters.stream().map(pair -> pair.getEntity().getSourceIdValue())
				.collect(Collectors.toList());
	}

	@Override
	public NodeControlInfo getCurrentControlInfo(String controlId) {
		Config config = getConfiguration();
		if ( config == null || config.getId() == null ) {
			return null;
		}

		List<UUIDEntityParametersPair<Control, ControlDatumParameters>> controlParameters = controlDao
				.findAllForDatumPropertyUUIDEntities(config.getId());
		Control control = controlParameters.stream()
				.filter(pair -> controlId.equals(pair.getEntity().getSourceIdValue()))
				.map(pair -> pair.getEntity()).findFirst().orElse(null);
		if ( control == null ) {
			log.debug("Control {} not available", controlId);
			return null;
		}
		log.debug("Reading {} status", controlId);
		NodeControlInfoDatum result = null;
		EventEntityDao<ValueEvent> valueEventDao = eventDaoForType(ValueEvent.class);
		try {
			ValueEvent value = valueEventDao.loadEvent(config.getId(), control.getUuid());
			result = newNodeControlInfoDatum(controlId, value);
		} catch ( Exception e ) {
			log.error("Error reading {} status: {}", controlId, e.getMessage());
		}
		return result;
	}

	private NodeControlInfoDatum newNodeControlInfoDatum(String controlId, ValueEvent valueEvent) {
		NodeControlInfoDatum info = new NodeControlInfoDatum();
		info.setCreated(valueEvent.getCreated());
		info.setSourceId(valueEvent.getSourceIdValue());
		info.setType(NodeControlPropertyType.Float);
		info.setReadonly(false);
		info.setValue(String.valueOf(valueEvent.getValue()));
		return info;
	}

	// InstructionHandler

	@Override
	public boolean handlesTopic(String topic) {
		return InstructionHandler.TOPIC_SET_CONTROL_PARAMETER.equals(topic);
	}

	@Override
	public InstructionState processInstruction(Instruction instruction) {
		Config config = getConfiguration();
		if ( config == null || config.getId() == null ) {
			return null;
		}

		// look for a parameter name that matches a control ID
		InstructionState result = null;

		List<UUIDEntityParametersPair<Control, ControlDatumParameters>> controlParameters = controlDao
				.findAllForDatumPropertyUUIDEntities(config.getId());

		Map<String, Control> supportedControlIds = controlParameters.stream().collect(
				Collectors.toMap(pair -> pair.getEntity().getSourceIdValue(), pair -> pair.getEntity()));

		log.debug("Inspecting instruction {} against controls {}", instruction.getId(),
				supportedControlIds);

		for ( String paramName : instruction.getParameterNames() ) {
			log.trace("Got instruction parameter {}", paramName);
			if ( supportedControlIds.containsKey(paramName) ) {
				// treat parameter value as a Double
				String str = instruction.getParameterValue(paramName);

				List<ValueEvent> loxoneResult = null;
				try {
					Future<List<ValueEvent>> promise = setControlState(
							supportedControlIds.get(paramName).getUuid(),
							new BigDecimal(str).doubleValue());
					loxoneResult = promise.get(10, TimeUnit.SECONDS);
				} catch ( Exception e ) {
					log.warn("Error handling instruction {} on control {}: {}", instruction.getTopic(),
							paramName, e.getMessage());
				}
				if ( loxoneResult != null ) {
					result = InstructionState.Completed;
				} else {
					result = InstructionState.Declined;
				}
			}
		}
		return result;
	}

	// General getters/setters

	public void setConfigurationDaos(
			List<ConfigurationEntityDao<ConfigurationEntity>> configurationDaos) {
		this.configurationDaos = configurationDaos;
	}

	public void setEventDaos(List<EventEntityDao<? extends EventEntity>> eventDaos) {
		this.eventDaos = eventDaos;
	}

	public void setUuidSetDaos(
			List<UUIDSetDao<UUIDSetEntity<UUIDEntityParameters>, UUIDEntityParameters>> uuidSetDaos) {
		this.uuidSetDaos = uuidSetDaos;
	}

	@Override
	public ControlDatumDataSource getDatumDataSource() {
		return datumDataSource;
	}

	public void setSettingResourceProvider(SetupResourceProvider settingResourceProvider) {
		this.settingResourceProvider = settingResourceProvider;
	}

	@Override
	public void setDatumLoggerFrequencySeconds(int datumLoggerFrequencySeconds) {
		if ( datumLoggerFrequencySeconds == this.datumLoggerFrequencySeconds ) {
			return;
		}
		this.datumLoggerFrequencySeconds = datumLoggerFrequencySeconds;
		configureLoxoneDatumLoggerJob(0);
		configureLoxoneDatumLoggerJob(datumLoggerFrequencySeconds * 1000);
	}

	public void setScheduler(Scheduler scheduler) {
		this.scheduler = scheduler;
	}

	public void setDatumDao(OptionalService<DatumDao<GeneralNodeDatum>> datumDao) {
		this.datumDao = datumDao;
	}

	public void setSettingDao(SettingDao settingDao) {
		this.settingDao = settingDao;
	}

	public void setControlDao(ControlDao controlDao) {
		this.controlDao = controlDao;
	}

	public void setSourceMappingDao(SourceMappingDao sourceMappingDao) {
		this.sourceMappingDao = sourceMappingDao;
	}

	/**
	 * Configure the {@link AuthenticationType} via a code value.
	 * 
	 * <p>
	 * If {@code code} is not supported, then {@link AuthenticationType#Auto}
	 * will be used.
	 * </p>
	 * 
	 * @param code
	 *        the {@link AuthenticationType#getCode()} value to use
	 * @see #setAuthenticationType(AuthenticationType)
	 * @since 1.7
	 */
	public void setAuthenticationTypeCode(int code) {
		AuthenticationType type;
		try {
			type = AuthenticationType.forCode(code);
		} catch ( IllegalArgumentException e ) {
			type = AuthenticationType.Auto;
		}
		setAuthenticationType(type);
	}

	/**
	 * Get the {@link AuthenticationType} as a code value.
	 * 
	 * @return the {@link AuthenticationType} code
	 * @see #getAuthenticationType()
	 * @since 1.7
	 */
	public int getAuthenticationTypeCode() {
		AuthenticationType type = getAuthenticationType();
		return (type != null ? type.getCode() : AuthenticationType.Auto.getCode());
	}

	/**
	 * Get the {@link AuthenticationTokenPermission} code to request for
	 * authentication tokens.
	 * 
	 * @return the permission code
	 * @since 1.7
	 */
	public int getTokenRequestPermissionCode() {
		AuthenticationTokenPermission perm = getTokenRequestPermission();
		return (perm != null ? perm.getCode() : AuthenticationTokenPermission.App.getCode());
	}

	/**
	 * Set the {@link AuthenticationTokenPermission} code to request for
	 * authentication tokens.
	 *
	 * <p>
	 * If {@code code} is not supported, then
	 * {@link AuthenticationTokenPermission#App} will be used.
	 * </p>
	 *
	 * @param code
	 *        the permission code to use; defaults to {@literal App}
	 * @since 1.7
	 */
	public void setTokenRequestPermissionsCode(int code) {
		AuthenticationTokenPermission perm;
		try {
			perm = AuthenticationTokenPermission.forCode(code);
		} catch ( IllegalArgumentException e ) {
			perm = AuthenticationTokenPermission.App;
		}
		setTokenRequestPermission(perm);
	}
}
