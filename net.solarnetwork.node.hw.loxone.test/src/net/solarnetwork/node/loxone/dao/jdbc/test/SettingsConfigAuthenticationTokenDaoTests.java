/* ==================================================================
 * SettingsConfigAuthenticationTokenDaoTests.java - 6/04/2018 9:25:27 AM
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

package net.solarnetwork.node.loxone.dao.jdbc.test;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import org.easymock.EasyMock;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import net.solarnetwork.node.dao.SettingDao;
import net.solarnetwork.node.loxone.dao.jdbc.SettingsConfigAuthenticationTokenDao;
import net.solarnetwork.node.loxone.domain.AuthenticationTokenPermission;
import net.solarnetwork.node.loxone.domain.ConfigAuthenticationToken;
import net.solarnetwork.node.support.KeyValuePair;
import net.solarnetwork.node.test.AbstractNodeTest;

/**
 * Test cases for the {@link SettingsConfigAuthenticationTokenDao} class.
 * 
 * @author matt
 * @version 1.0
 */
public class SettingsConfigAuthenticationTokenDaoTests extends AbstractNodeTest {

	private SettingDao settingDao;

	private SettingsConfigAuthenticationTokenDao authTokenDao;

	@Before
	public void setup() {
		settingDao = EasyMock.createMock(SettingDao.class);
		authTokenDao = new SettingsConfigAuthenticationTokenDao();
		authTokenDao.setSettingDao(settingDao);
	}

	@After
	public void teardown() {
		EasyMock.verify(settingDao);
	}

	@Test
	public void getAuthTokenNoData() {
		// given
		List<KeyValuePair> settings = Collections.emptyList();
		expect(settingDao.getSettings("loxone/1/auth-token")).andReturn(settings);

		// when
		replay(settingDao);
		ConfigAuthenticationToken authToken = authTokenDao.getConfigAuthenticationToken(1L);

		// then
		assertThat("No token found", authToken, nullValue());
	}

	@Test
	public void getAuthToken() {
		// given
		ConfigAuthenticationToken expected = new ConfigAuthenticationToken(1L, "2", new DateTime(),
				EnumSet.of(AuthenticationTokenPermission.App), false, "abff");
		List<KeyValuePair> settings = Arrays.asList(
				new KeyValuePair(SettingsConfigAuthenticationTokenDao.KEY_SETTING, expected.getKeyHex()),
				new KeyValuePair(SettingsConfigAuthenticationTokenDao.TOKEN_SETTING,
						expected.getToken()),
				new KeyValuePair(SettingsConfigAuthenticationTokenDao.VALID_UNTIL_SETTING,
						String.valueOf(expected.getValidUntil().getMillis())),
				new KeyValuePair(SettingsConfigAuthenticationTokenDao.PERMISSIONS_SETTING,
						String.valueOf(expected.getPermissionsBitmask())),
				new KeyValuePair(SettingsConfigAuthenticationTokenDao.PASSWORD_UNSECURE_SETTING,
						String.valueOf(expected.isPasswordUnsecure())));
		expect(settingDao.getSettings("loxone/1/auth-token")).andReturn(settings);

		// when
		replay(settingDao);
		ConfigAuthenticationToken authToken = authTokenDao.getConfigAuthenticationToken(1L);

		// then
		assertThat("Token found", authToken, notNullValue());
		assertThat("Token value", authToken.getToken(), equalTo(expected.getToken()));
		assertThat("Key", authToken.getKeyHex(), equalTo(expected.getKeyHex()));
		assertThat("Valid until", authToken.getValidUntil(), equalTo(expected.getValidUntil()));
		assertThat("Permissions", authToken.getPermissions(), equalTo(expected.getPermissions()));
		assertThat("Password unsecure", authToken.isPasswordUnsecure(),
				equalTo(expected.isPasswordUnsecure()));
	}

	@Test
	public void storeAuthToken() {
		// given
		ConfigAuthenticationToken authToken = new ConfigAuthenticationToken(1L, "2", new DateTime(),
				EnumSet.of(AuthenticationTokenPermission.App), false, "abff");

		settingDao.storeSetting("loxone/1/auth-token", SettingsConfigAuthenticationTokenDao.KEY_SETTING,
				authToken.getKeyHex());
		settingDao.storeSetting("loxone/1/auth-token",
				SettingsConfigAuthenticationTokenDao.TOKEN_SETTING, authToken.getToken());
		settingDao.storeSetting("loxone/1/auth-token",
				SettingsConfigAuthenticationTokenDao.VALID_UNTIL_SETTING,
				String.valueOf(authToken.getValidUntil().getMillis()));
		settingDao.storeSetting("loxone/1/auth-token",
				SettingsConfigAuthenticationTokenDao.PERMISSIONS_SETTING,
				String.valueOf(authToken.getPermissionsBitmask()));
		settingDao.storeSetting("loxone/1/auth-token",
				SettingsConfigAuthenticationTokenDao.PASSWORD_UNSECURE_SETTING,
				String.valueOf(authToken.isPasswordUnsecure()));

		// when
		replay(settingDao);
		authTokenDao.storeConfigAuthenticationToken(authToken);
	}

	@Test
	public void deleteAuthToken() {
		// given
		expect(settingDao.deleteSetting("loxone/1/auth-token",
				SettingsConfigAuthenticationTokenDao.TOKEN_SETTING)).andReturn(true);

		// when
		replay(settingDao);
		authTokenDao.deleteConfigAuthenticationToken(1L);
	}

}
