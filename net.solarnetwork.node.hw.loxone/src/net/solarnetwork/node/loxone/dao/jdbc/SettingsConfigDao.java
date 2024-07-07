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
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import net.solarnetwork.node.dao.SettingDao;
import net.solarnetwork.node.loxone.dao.ConfigDao;
import net.solarnetwork.node.loxone.domain.Config;

/**
 * {@link ConfigDao} that persists data via a {@link SettingDao}.
 *
 * @author matt
 * @version 1.1
 */
public class SettingsConfigDao implements ConfigDao {

	/**
	 * A global setting key used for the client UUID.
	 *
	 * @since 1.1
	 */
	public static final String GLOBAL_UUID_SETTING_KEY = "loxone/uuid";

	private static final AtomicReference<UUID> GLOBAL_UUID = new AtomicReference<UUID>(null);

	private SettingDao settingDao;

	/**
	 * Constructor.
	 */
	public SettingsConfigDao() {
		super();
	}

	private static String lastModifiedDateSettingKey(Long configId) {
		return "loxone/" + Config.idToExternalForm(configId) + "/lastModified";
	}

	@Override
	public void storeConfig(Config config) {
		Long configId = (config != null ? config.getId() : null);
		if ( configId == null ) {
			return;
		}
		Date lastModified = config.getLastModified();
		if ( lastModified != null ) {
			settingDao.storeSetting(lastModifiedDateSettingKey(configId),
					Long.toString(lastModified.getTime(), 16));
		}
		String uuid = config.getClientUuidString();
		if ( uuid != null ) {
			settingDao.storeSetting(GLOBAL_UUID_SETTING_KEY, uuid);
		}
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
		UUID globalUuid = GLOBAL_UUID.get();
		if ( globalUuid == null ) {
			String globalUuidValue = settingDao.getSetting(GLOBAL_UUID_SETTING_KEY);
			if ( globalUuidValue != null ) {
				UUID uuid = new Config(id, null, globalUuidValue).getClientUuid();
				if ( GLOBAL_UUID.compareAndSet(null, uuid) ) {
					globalUuid = uuid;
				} else {
					globalUuid = GLOBAL_UUID.get();
				}
			} else {
				UUID uuid = UUID.randomUUID();
				if ( GLOBAL_UUID.compareAndSet(null, globalUuid) ) {
					globalUuid = uuid;
					settingDao.storeSetting(GLOBAL_UUID_SETTING_KEY,
							new Config(id, null, uuid).getClientUuidString());
				} else {
					globalUuid = GLOBAL_UUID.get();
				}
			}
		}
		return new Config(id, (ts > 0 ? new Date(ts) : null), globalUuid);
	}

	/**
	 * Get the setting DAO.
	 *
	 * @return the DAO
	 */
	public SettingDao getSettingDao() {
		return settingDao;
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

}
