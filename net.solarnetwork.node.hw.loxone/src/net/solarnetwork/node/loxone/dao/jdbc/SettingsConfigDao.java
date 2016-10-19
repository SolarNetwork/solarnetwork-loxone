/* ==================================================================
 * SettingsConfigDao.java - 19/09/2016 1:56:38 PM
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

import java.util.Date;
import net.solarnetwork.node.dao.SettingDao;
import net.solarnetwork.node.loxone.dao.ConfigDao;
import net.solarnetwork.node.loxone.domain.Config;

/**
 * {@link ConfigDao} that persists data via a {@link SettingDao}.
 * 
 * @author matt
 * @version 1.0
 */
public class SettingsConfigDao implements ConfigDao {

	private SettingDao settingDao;

	private String lastModifiedDateSettingKey(Long configId) {
		return "loxone/" + Config.idToExternalForm(configId) + "/lastModified";
	}

	@Override
	public void storeConfig(Config config) {
		Long configId = (config != null ? config.getId() : null);
		if ( configId == null ) {
			return;
		}
		Date lastModified = config.getLastModified();
		if ( lastModified == null ) {
			return;
		}
		settingDao.storeSetting(lastModifiedDateSettingKey(configId),
				Long.toString(lastModified.getTime(), 16));
	}

	@Override
	public Config getConfig(Long id) {
		String value = settingDao.getSetting(lastModifiedDateSettingKey(id));
		long ts = -1;
		try {
			ts = Long.parseLong(value, 16);
		} catch ( NumberFormatException e ) {
			// ignore
		} catch ( NullPointerException e ) {
			// ignore
		}
		if ( ts < 0 ) {
			return null;
		}
		Config result = new Config(id, new Date(ts));
		return result;
	}

	public SettingDao getSettingDao() {
		return settingDao;
	}

	public void setSettingDao(SettingDao settingDao) {
		this.settingDao = settingDao;
	}

}
