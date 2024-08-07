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

import static net.solarnetwork.service.OptionalService.service;
import static net.solarnetwork.service.support.BasicIdentifiable.basicIdentifiableSettings;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;
import org.osgi.service.event.Event;
import org.springframework.context.MessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import net.solarnetwork.domain.BasicNodeControlInfo;
import net.solarnetwork.domain.InstructionStatus.InstructionState;
import net.solarnetwork.domain.NodeControlInfo;
import net.solarnetwork.domain.NodeControlPropertyType;
import net.solarnetwork.domain.SortDescriptor;
import net.solarnetwork.node.dao.SettingDao;
import net.solarnetwork.node.domain.datum.SimpleNodeControlInfoDatum;
import net.solarnetwork.node.job.DatumDataSourcePollManagedJob;
import net.solarnetwork.node.job.JobUtils;
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
import net.solarnetwork.node.reactor.InstructionStatus;
import net.solarnetwork.node.reactor.InstructionUtils;
import net.solarnetwork.node.service.DatumQueue;
import net.solarnetwork.node.service.MultiDatumDataSource;
import net.solarnetwork.node.service.NodeControlProvider;
import net.solarnetwork.node.settings.support.BasicSetupResourceSettingSpecifier;
import net.solarnetwork.node.setup.SetupResourceProvider;
import net.solarnetwork.service.OptionalService;
import net.solarnetwork.service.RegisteredService;
import net.solarnetwork.service.RemoteServiceException;
import net.solarnetwork.service.ServiceLifecycleObserver;
import net.solarnetwork.service.ServiceRegistry;
import net.solarnetwork.settings.SettingSpecifier;
import net.solarnetwork.settings.SettingSpecifierProvider;
import net.solarnetwork.settings.support.BasicMultiValueSettingSpecifier;
import net.solarnetwork.settings.support.BasicTextFieldSettingSpecifier;
import net.solarnetwork.settings.support.BasicTitleSettingSpecifier;
import net.solarnetwork.settings.support.BasicToggleSettingSpecifier;

/**
 * Websocket based implementation of {@link LoxoneService}.
 *
 * @author matt
 * @version 2.1
 */
public class WebsocketLoxoneService extends LoxoneEndpoint
		implements LoxoneService, SettingSpecifierProvider, WebsocketLoxoneServiceSettings,
		NodeControlProvider, InstructionHandler, ServiceLifecycleObserver {

	/**
	 * The name used to schedule the {@link ControlDatumDataSource} as.
	 */
	public static final String DATUM_POLL_JOB_NAME = "Loxone_DatumPoll";

	/**
	 * The job and trigger group used to schedule the
	 * {@link ScheduledDatumDataSourcePollJob} with. Note the trigger name will
	 * be the {@link #getUid()} property value.
	 */
	public static final String SCHEDULER_GROUP = "Loxone";

	/**
	 * The default minimum interval at which to save {@code Datum} instances
	 * from Loxone value event data, in seconds.
	 */
	public static final int DATUM_LOGGER_JOB_INTERVAL = 60;

	private static final String DEFAULT_UID = "Loxone";

	private String uid = DEFAULT_UID;
	private String groupUid;
	private List<ConfigurationEntityDao<ConfigurationEntity>> configurationDaos;
	private List<EventEntityDao<? extends EventEntity>> eventDaos;
	private List<UUIDSetDao<UUIDSetEntity<UUIDEntityParameters>, UUIDEntityParameters>> uuidSetDaos;
	private SetupResourceProvider settingResourceProvider;
	private SettingDao settingDao;
	private ControlDao controlDao;
	private SourceMappingDao sourceMappingDao;
	private OptionalService<DatumQueue> datumQueue;
	private int datumLoggerFrequencySeconds = DATUM_LOGGER_JOB_INTERVAL;
	private MessageSource messageSource;
	private TaskExecutor taskExecutor;
	private ServiceRegistry serviceRegistry;

	private ControlDatumDataSource datumDataSource;
	private ScheduledDatumDataSourcePollJob datumLoggerTrigger;
	private RegisteredService<MultiDatumDataSource> datumDataSourceRegistration;

	private static final class ScheduledDatumDataSourcePollJob {

		private Runnable task;
		private ScheduledFuture<?> future;
		private long interval;

	}

	/**
	 * Constructor.
	 */
	public WebsocketLoxoneService() {
		super();
	}

	@Override
	public void serviceDidStartup() {
		init();
	}

	@Override
	public void serviceDidShutdown() {
		close();
		if ( datumDataSourceRegistration != null && serviceRegistry != null ) {
			serviceRegistry.unregisterService(datumDataSourceRegistration);
			datumDataSourceRegistration = null;
		}
	}

	@Override
	public synchronized void init() {
		if ( datumDataSourceRegistration != null && serviceRegistry != null ) {
			serviceRegistry.unregisterService(datumDataSourceRegistration);
			datumDataSourceRegistration = null;
		}
		datumDataSource = new ControlDatumDataSource(getSettingUid(), null, controlDao, settingDao);
		datumDataSource.setMessageSource(messageSource);
		datumDataSource.setDatumQueue(datumQueue);
		datumDataSource.setTaskExecutor(taskExecutor);
		datumDataSource.setUid(getConfigurationIdExternalForm());
		super.init();
		if ( serviceRegistry != null ) {
			datumDataSourceRegistration = serviceRegistry.registerService(
					(MultiDatumDataSource) datumDataSource, null, MultiDatumDataSource.class);
		}
	}

	@Override
	public synchronized void disconnect() {
		configureLoxoneDatumLoggerJob(0);
		super.disconnect();
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
	public String getUid() {
		return uid;
	}

	/**
	 * Set the UID.
	 *
	 * @param uid
	 *        the UID
	 */
	public void setUid(String uid) {
		this.uid = uid;
	}

	/**
	 * Get the UID.
	 *
	 * @return the UID
	 */
	public String getUID() {
		return getUid();
	}

	/**
	 * Set the UID.
	 *
	 * @param uid
	 *        the UID to set
	 */
	public void setUID(String uid) {
		setUid(uid);
	}

	@Override
	public String getGroupUid() {
		return groupUid;
	}

	/**
	 * Set the group UID.
	 *
	 * @param groupUid
	 *        the group UID to set
	 */
	public void setGroupUid(String groupUid) {
		this.groupUid = groupUid;
	}

	/**
	 * Get the group UID.
	 *
	 * @return the group UID
	 */
	public String getGroupUID() {
		return getGroupUid();
	}

	/**
	 * Set the group UID.
	 *
	 * @param groupUID
	 *        the group UID
	 */
	public void setGroupUID(String groupUID) {
		setGroupUid(groupUID);
	}

	private String getConfigurationIdExternalForm() {
		Config config = getConfiguration();
		return (config == null ? null : config.idToExternalForm());
	}

	@Override
	public String getSettingUid() {
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

	/**
	 * Set the message source.
	 *
	 * @param messageSource
	 *        the message source to set
	 */
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
		results.addAll(basicIdentifiableSettings("", DEFAULT_UID, null));
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
		results.add(new BasicToggleSettingSpecifier("datumDataSource.datumDaoPersistOnlyStatusUpdates",
				ControlDatumDataSource.DEFAULT_PERSIST_ONLY_STATUS_UPDATES));

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
		datumDataSource.setUid(config.idToExternalForm());

		return result;
	}

	@Override
	public void onOpen(Session session, EndpointConfig config) {
		super.onOpen(session, config);
		scheduleDatumLoggerJobIfNeeded();
	}

	private void scheduleDatumLoggerJobIfNeeded() {
		configureLoxoneDatumLoggerJob(datumLoggerFrequencySeconds);
	}

	private boolean configureLoxoneDatumLoggerJob(final long interval) {
		final Config config = getConfiguration();
		if ( config == null || config.getId() == null ) {
			return false;
		}
		final String configIdDisplay = config.idToExternalForm();
		final TaskScheduler sched = getTaskScheduler();

		if ( sched == null ) {
			log.info("No scheduler avaialable, cannot schedule Loxone {} datum poll job",
					configIdDisplay);
			return false;
		}
		final DatumQueue queue = service(datumQueue);
		if ( queue == null ) {
			log.info("No DatumQueue avaialable, cannot schedule Loxone {} datum poll job",
					configIdDisplay);
			return false;
		}
		ScheduledDatumDataSourcePollJob trigger = datumLoggerTrigger;
		if ( trigger != null && trigger.future != null && !trigger.future.isDone()
				&& trigger.task != null ) {
			// check if interval actually changed
			if ( trigger.interval == interval ) {
				log.debug("Loxone {} datum poll interval unchanged at {}s", configIdDisplay, interval);
				return true;
			}
			// trigger has changed!
			if ( interval == 0 ) {
				try {
					trigger.future.cancel(true);
					log.info("Unscheduled Loxone {} datum poll job", configIdDisplay);
				} finally {
					datumLoggerTrigger = null;
				}
			} else {
				Trigger t = JobUtils.triggerForExpression(String.valueOf(interval), TimeUnit.SECONDS,
						false);
				trigger.future = sched.schedule(trigger.task, t);
			}
			return true;
		} else if ( interval == 0 ) {
			return true;
		}

		synchronized ( sched ) {
			try {
				Trigger t = JobUtils.triggerForExpression(String.valueOf(interval), TimeUnit.SECONDS,
						false);
				DatumDataSourcePollManagedJob job = new DatumDataSourcePollManagedJob();
				job.setDatumQueue(datumQueue);
				job.setMultiDatumDataSource(datumDataSource);
				if ( trigger == null ) {
					trigger = new ScheduledDatumDataSourcePollJob();
					datumLoggerTrigger = trigger;
				}
				trigger.interval = interval;
				trigger.future = sched.schedule(new Runnable() {

					@Override
					public void run() {
						try {
							job.executeJobService();
						} catch ( Exception e ) {
							log.error("Error polling Loxone {} datum logger job", configIdDisplay, e);
						}
					}

				}, t);
				log.info("Scheduled Loxone {} datum logger job to run every {} seconds", configIdDisplay,
						interval);
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
		SimpleNodeControlInfoDatum result = null;
		EventEntityDao<ValueEvent> valueEventDao = eventDaoForType(ValueEvent.class);
		try {
			ValueEvent value = valueEventDao.loadEvent(config.getId(), control.getUuid());
			result = newNodeControlInfoDatum(controlId, value);
		} catch ( Exception e ) {
			log.error("Error reading {} status: {}", controlId, e.getMessage());
		}
		return result;
	}

	private SimpleNodeControlInfoDatum newNodeControlInfoDatum(String controlId, ValueEvent valueEvent) {
		// @formatter:off
		NodeControlInfo info = BasicNodeControlInfo.builder()
				.withControlId(valueEvent.getSourceIdValue())
				.withType(NodeControlPropertyType.Float)
				.withReadonly(false)
				.withValue(String.valueOf(valueEvent.getValue()))
				.build();
		// @formatter:on
		return new SimpleNodeControlInfoDatum(info, valueEvent.getCreated());
	}

	// InstructionHandler

	@Override
	public boolean handlesTopic(String topic) {
		return InstructionHandler.TOPIC_SET_CONTROL_PARAMETER.equals(topic);
	}

	@Override
	public InstructionStatus processInstruction(Instruction instruction) {
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
		return InstructionUtils.createStatus(instruction, result);
	}

	// General getters/setters

	/**
	 * Set the configuration DAOs.
	 *
	 * @param configurationDaos
	 *        the DAOs to set
	 */
	public void setConfigurationDaos(
			List<ConfigurationEntityDao<ConfigurationEntity>> configurationDaos) {
		this.configurationDaos = configurationDaos;
	}

	/**
	 * Set the event DAOs.
	 *
	 * @param eventDaos
	 *        the DAOs to set
	 */
	public void setEventDaos(List<EventEntityDao<? extends EventEntity>> eventDaos) {
		this.eventDaos = eventDaos;
	}

	/**
	 * Set the UUID set DAOs.
	 *
	 * @param uuidSetDaos
	 *        the DAOs to set
	 */
	public void setUuidSetDaos(
			List<UUIDSetDao<UUIDSetEntity<UUIDEntityParameters>, UUIDEntityParameters>> uuidSetDaos) {
		this.uuidSetDaos = uuidSetDaos;
	}

	@Override
	public ControlDatumDataSource getDatumDataSource() {
		return datumDataSource;
	}

	/**
	 * Set the setting resource provider.
	 *
	 * @param settingResourceProvider
	 *        the provider to set
	 */
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

	/**
	 * Set the datum queue.
	 *
	 * @param datumQueue
	 *        the queue
	 */
	public void setDatumQueue(OptionalService<DatumQueue> datumQueue) {
		this.datumQueue = datumQueue;
	}

	/**
	 * Set the setting DAO.
	 *
	 * @param settingDao
	 *        the DAO to set
	 */
	public void setSettingDao(SettingDao settingDao) {
		this.settingDao = settingDao;
	}

	/**
	 * Set the control DAO.
	 *
	 * @param controlDao
	 *        the DAO to set
	 */
	public void setControlDao(ControlDao controlDao) {
		this.controlDao = controlDao;
	}

	/**
	 * Set the source mapping DAO.
	 *
	 * @param sourceMappingDao
	 *        the DAO to set
	 */
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

	/**
	 * Get the task executor.
	 *
	 * @return the executor
	 * @since 1.9
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
	 * @since 1.9
	 */
	public void setTaskExecutor(TaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
		if ( datumDataSource != null ) {
			datumDataSource.setTaskExecutor(taskExecutor);
		}
	}

	/**
	 * Get the service registry.
	 *
	 * @return the service registry
	 * @since 2.1
	 */
	public final ServiceRegistry getServiceRegistry() {
		return serviceRegistry;
	}

	/**
	 * Set the service registry.
	 *
	 * @param serviceRegistry
	 *        the service registry to set
	 * @since 2.1
	 */
	public final void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

}
