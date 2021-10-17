/* ==================================================================
 * SettingsConfigAuthenticationTokenDao.java - 6/04/2018 8:44:14 AM
 * 
 * Copyright 2018 SolarNetwork.net Dev Team
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

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import net.solarnetwork.domain.KeyValuePair;
import net.solarnetwork.node.dao.SettingDao;
import net.solarnetwork.node.loxone.dao.ConfigAuthenticationTokenDao;
import net.solarnetwork.node.loxone.domain.AuthenticationTokenPermission;
import net.solarnetwork.node.loxone.domain.Config;
import net.solarnetwork.node.loxone.domain.ConfigAuthenticationToken;

/**
 * {@link ConfigAuthenticationTokenDao} that persists data via a
 * {@link SettingDao}.
 * 
 * @author matt
 * @version 2.0
 * @since 1.3
 */
public class SettingsConfigAuthenticationTokenDao implements ConfigAuthenticationTokenDao {

	/** The setting key for the {@code passwordUnsecure} property. */
	public static final String PASSWORD_UNSECURE_SETTING = "unsecure";

	/** The setting key for the {@code permissions} property. */
	public static final String PERMISSIONS_SETTING = "permissions";

	/** The setting key for the {@code validUntil} property. */
	public static final String VALID_UNTIL_SETTING = "expires";

	/** The setting key for the {@code token} property. */
	public static final String TOKEN_SETTING = "token";

	/** The setting key for the {@code keyHex} property. */
	public static final String KEY_SETTING = "key";

	private SettingDao settingDao;

	private static String settingKey(Long configId) {
		return "loxone/" + Config.idToExternalForm(configId) + "/auth-token";
	}

	@Override
	public void storeConfigAuthenticationToken(ConfigAuthenticationToken token) {
		Long configId = (token != null ? token.getConfigId() : null);
		if ( configId == null ) {
			return;
		}
		String key = token.getKeyHex();
		settingDao.storeSetting(settingKey(configId), KEY_SETTING, key == null ? "" : key);
		settingDao.storeSetting(settingKey(configId), PERMISSIONS_SETTING,
				String.valueOf(token.getPermissionsBitmask()));
		String value = token.getToken();
		settingDao.storeSetting(settingKey(configId), TOKEN_SETTING, value == null ? "" : value);
		settingDao.storeSetting(settingKey(configId), VALID_UNTIL_SETTING, String
				.valueOf(token.getValidUntil() != null ? token.getValidUntil().toEpochMilli() : 0));
		settingDao.storeSetting(settingKey(configId), PASSWORD_UNSECURE_SETTING,
				String.valueOf(token.isPasswordUnsecure()));
	}

	@Override
	public void deleteConfigAuthenticationToken(Long configId) {
		if ( configId == null ) {
			return;
		}
		// deleting the token is sufficient for getConfigAuthenticationToken() to think it's not there 
		settingDao.deleteSetting(settingKey(configId), TOKEN_SETTING);
	}

	private String settingForKey(List<KeyValuePair> settings, String key) {
		if ( settings == null ) {
			return null;
		}
		for ( KeyValuePair s : settings ) {
			if ( key.equals(s.getKey()) ) {
				return s.getValue();
			}
		}
		return null;
	}

	@Override
	public ConfigAuthenticationToken getConfigAuthenticationToken(Long id) {
		if ( id == null ) {
			return null;
		}
		List<KeyValuePair> settings = settingDao.getSettingValues(settingKey(id));
		String key = settingForKey(settings, KEY_SETTING);
		String tok = settingForKey(settings, TOKEN_SETTING);
		String exp = settingForKey(settings, VALID_UNTIL_SETTING);
		String perms = settingForKey(settings, PERMISSIONS_SETTING);
		String unsec = settingForKey(settings, PASSWORD_UNSECURE_SETTING);

		if ( key == null || key.isEmpty() || tok == null || tok.isEmpty() || exp == null
				|| exp.isEmpty() ) {
			return null;
		}

		return new ConfigAuthenticationToken(id, tok, Instant.ofEpochMilli(Long.parseLong(exp)),
				(perms != null
						? AuthenticationTokenPermission.permissionsForBitmask(Integer.parseInt(perms))
						: Collections.emptySet()),
				(unsec != null ? Boolean.parseBoolean(unsec) : true), key);
	}

	public SettingDao getSettingDao() {
		return settingDao;
	}

	public void setSettingDao(SettingDao settingDao) {
		this.settingDao = settingDao;
	}
}
