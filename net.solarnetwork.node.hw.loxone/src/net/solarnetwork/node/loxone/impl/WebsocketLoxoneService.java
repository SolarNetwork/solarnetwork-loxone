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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import org.springframework.core.io.Resource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import net.solarnetwork.domain.SortDescriptor;
import net.solarnetwork.node.RemoteServiceException;
import net.solarnetwork.node.loxone.LoxoneService;
import net.solarnetwork.node.loxone.dao.ConfigurationEntityDao;
import net.solarnetwork.node.loxone.domain.Config;
import net.solarnetwork.node.loxone.domain.ConfigurationEntity;
import net.solarnetwork.node.loxone.protocol.ws.CommandType;
import net.solarnetwork.node.loxone.protocol.ws.LoxoneEndpoint;
import net.solarnetwork.node.settings.SettingSpecifier;
import net.solarnetwork.node.settings.support.BasicSetupResourceSettingSpecifier;
import net.solarnetwork.node.settings.support.BasicTextFieldSettingSpecifier;
import net.solarnetwork.node.settings.support.BasicTitleSettingSpecifier;
import net.solarnetwork.node.setup.SetupResourceProvider;

/**
 * Websocket based implementation of {@link LoxoneService}.
 * 
 * @author matt
 * @version 1.0
 */
public class WebsocketLoxoneService extends LoxoneEndpoint implements LoxoneService {

	private static final String DEFAULT_UID = "Loxone";

	private String uid = DEFAULT_UID;
	private String groupUID;
	private List<ConfigurationEntityDao<ConfigurationEntity>> configurationDaos;
	private SetupResourceProvider settingResourceProvider;

	@Override
	public Long getConfigurationId() {
		Config config = getConfiguration();
		return (config == null ? null : config.getId());
	}

	@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
	@SuppressWarnings("unchecked")
	@Override
	public <T extends ConfigurationEntity> Collection<T> getAllConfiguration(Class<T> type,
			List<SortDescriptor> sortDescriptors) {
		Collection<T> result = null;
		Config config = getConfiguration();
		if ( configurationDaos != null && config.getId() != null ) {
			for ( ConfigurationEntityDao<ConfigurationEntity> dao : configurationDaos ) {
				if ( type.isAssignableFrom(dao.entityClass()) ) {
					result = (List<T>) dao.findAllForConfig(config.getId(), sortDescriptors);
				}
			}
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

	@Override
	public List<SettingSpecifier> getSettingSpecifiers() {
		List<SettingSpecifier> results = super.getSettingSpecifiers();
		results.add(0, new BasicTextFieldSettingSpecifier("uid", DEFAULT_UID));
		results.add(0, new BasicTextFieldSettingSpecifier("groupUID", null));

		Long configurationId = getConfigurationId();
		if ( configurationId != null ) {
			results.add(
					new BasicTitleSettingSpecifier("configurationId", configurationId.toString(), true));
		}

		if ( settingResourceProvider != null ) {
			Map<String, Object> setupProps = Collections.singletonMap("config-id", getConfigurationId());
			results.add(new BasicSetupResourceSettingSpecifier(settingResourceProvider, setupProps));
		}

		return results;
	}

	public void setConfigurationDaos(
			List<ConfigurationEntityDao<ConfigurationEntity>> configurationDaos) {
		this.configurationDaos = configurationDaos;
	}

	public void setSettingResourceProvider(SetupResourceProvider settingResourceProvider) {
		this.settingResourceProvider = settingResourceProvider;
	}

}
