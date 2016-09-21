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

import java.util.Collection;
import java.util.List;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import net.solarnetwork.domain.SortDescriptor;
import net.solarnetwork.node.loxone.LoxoneService;
import net.solarnetwork.node.loxone.dao.ConfigurationEntityDao;
import net.solarnetwork.node.loxone.domain.Config;
import net.solarnetwork.node.loxone.domain.ConfigurationEntity;
import net.solarnetwork.node.loxone.protocol.ws.LoxoneEndpoint;
import net.solarnetwork.node.settings.SettingSpecifier;
import net.solarnetwork.node.settings.support.BasicTextFieldSettingSpecifier;
import net.solarnetwork.node.settings.support.BasicTitleSettingSpecifier;

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
		return results;
	}

	public void setConfigurationDaos(
			List<ConfigurationEntityDao<ConfigurationEntity>> configurationDaos) {
		this.configurationDaos = configurationDaos;
	}

}
