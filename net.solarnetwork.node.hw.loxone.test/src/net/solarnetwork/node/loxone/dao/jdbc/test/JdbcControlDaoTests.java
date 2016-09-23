/* ==================================================================
 * JdbcControlDaoTests.java - 18/09/2016 8:50:38 AM
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

package net.solarnetwork.node.loxone.dao.jdbc.test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Resource;
import javax.sql.DataSource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import net.solarnetwork.node.dao.jdbc.DatabaseSetup;
import net.solarnetwork.node.loxone.dao.jdbc.JdbcControlDao;
import net.solarnetwork.node.loxone.domain.Control;
import net.solarnetwork.node.loxone.domain.ControlType;
import net.solarnetwork.node.test.AbstractNodeTransactionalTest;

/**
 * Unit tests for the {@link JdbcControlDao} class.
 * 
 * @author matt
 * @version 1.0
 */
public class JdbcControlDaoTests extends AbstractNodeTransactionalTest {

	private static final Long TEST_CONFIG_ID = 123L;
	private static final String TEST_NAME = "Test Name";
	private static final Integer TEST_DEFAULT_RATING = 1;

	@Resource(name = "dataSource")
	private DataSource dataSource;

	private JdbcControlDao dao;
	private Control lastControl;

	@Before
	public void setup() {
		DatabaseSetup setup = new DatabaseSetup();
		setup.setDataSource(dataSource);
		setup.init();

		dao = new JdbcControlDao();
		dao.setDataSource(dataSource);
		dao.init();
	}

	@Test
	public void insert() {
		Control control = new Control();
		control.setUuid(UUID.randomUUID());
		control.setConfigId(TEST_CONFIG_ID);
		control.setName(TEST_NAME);
		control.setDefaultRating(TEST_DEFAULT_RATING);
		control.setType(ControlType.Meter);
		control.setRoom(UUID.randomUUID());
		control.setCategory(UUID.randomUUID());
		dao.store(control);
		lastControl = control;
	}

	private Map<String, UUID> getTestStatesMap() {
		Map<String, UUID> map = new LinkedHashMap<>(4);
		map.put("foo", UUID.randomUUID());
		map.put("bar", UUID.randomUUID());
		return map;
	}

	@Test
	public void insertWithStates() {
		Control control = new Control();
		control.setUuid(UUID.randomUUID());
		control.setConfigId(TEST_CONFIG_ID);
		control.setName(TEST_NAME);
		control.setDefaultRating(TEST_DEFAULT_RATING);
		control.setType(ControlType.Meter);
		control.setRoom(UUID.randomUUID());
		control.setCategory(UUID.randomUUID());
		control.setStates(getTestStatesMap());
		dao.store(control);
		lastControl = control;
	}

	@Test
	public void getByPKWithStates() {
		insertWithStates();
		Control control = dao.load(TEST_CONFIG_ID, lastControl.getUuid());
		Assert.assertNotNull("Control inserted", control);
		Assert.assertEquals("UUID", lastControl.getUuid(), control.getUuid());
		Assert.assertEquals("Config ID", lastControl.getConfigId(), lastControl.getConfigId());
		Assert.assertEquals("Name", lastControl.getName(), control.getName());
		Assert.assertEquals("Default rating", lastControl.getDefaultRating(),
				control.getDefaultRating());
		Assert.assertEquals("Type", lastControl.getType(), control.getType());
		Assert.assertEquals("Room", lastControl.getRoom(), control.getRoom());
		Assert.assertEquals("Category", lastControl.getCategory(), control.getCategory());
		Assert.assertEquals("State map", lastControl.getStates(), control.getStates());
	}

	@Test
	public void deleteForConfigWithStates() {
		insertWithStates();
		int result = dao.deleteAllForConfig(TEST_CONFIG_ID);
		Assert.assertEquals("Deleted count", 1, result);
		Control cat = dao.load(TEST_CONFIG_ID, lastControl.getUuid());
		Assert.assertNull("Control no longer available", cat);
	}

	@Test
	public void getByPK() {
		insert();
		Control control = dao.load(TEST_CONFIG_ID, lastControl.getUuid());
		Assert.assertNotNull("Control inserted", control);
		Assert.assertEquals("UUID", lastControl.getUuid(), control.getUuid());
		Assert.assertEquals("Config ID", lastControl.getConfigId(), lastControl.getConfigId());
		Assert.assertEquals("Name", lastControl.getName(), control.getName());
		Assert.assertEquals("Default rating", lastControl.getDefaultRating(),
				control.getDefaultRating());
		Assert.assertEquals("Type", lastControl.getType(), control.getType());
		Assert.assertEquals("Room", lastControl.getRoom(), control.getRoom());
		Assert.assertEquals("Category", lastControl.getCategory(), control.getCategory());
	}

	@Test
	public void update() {
		insert();
		Control orig = dao.load(TEST_CONFIG_ID, lastControl.getUuid());
		orig.setName("Updated Name");
		orig.setDefaultRating(2);
		orig.setType(ControlType.Dimmer);
		orig.setRoom(UUID.randomUUID());
		orig.setCategory(UUID.randomUUID());
		dao.store(orig);
		Control updated = dao.load(TEST_CONFIG_ID, lastControl.getUuid());
		Assert.assertEquals("Same UUID", orig.getUuid(), updated.getUuid());
		Assert.assertEquals("Same Config ID", orig.getConfigId(), updated.getConfigId());
		Assert.assertEquals("Updated name", orig.getName(), updated.getName());
		Assert.assertEquals("Updated default rating", orig.getDefaultRating(),
				updated.getDefaultRating());
		Assert.assertEquals("Updated type", orig.getType(), updated.getType());
		Assert.assertEquals("Updated room", orig.getRoom(), updated.getRoom());
		Assert.assertEquals("Updated category", orig.getCategory(), updated.getCategory());
	}

	@Test
	public void updateWithStates() {
		insertWithStates();
		Control orig = dao.load(TEST_CONFIG_ID, lastControl.getUuid());
		orig.setName("Updated Name");
		orig.setDefaultRating(2);
		orig.setType(ControlType.Dimmer);
		orig.setRoom(UUID.randomUUID());
		orig.setCategory(UUID.randomUUID());
		orig.setStates(getTestStatesMap());
		dao.store(orig);
		Control updated = dao.load(TEST_CONFIG_ID, lastControl.getUuid());
		Assert.assertEquals("Same UUID", orig.getUuid(), updated.getUuid());
		Assert.assertEquals("Same Config ID", orig.getConfigId(), updated.getConfigId());
		Assert.assertEquals("Updated name", orig.getName(), updated.getName());
		Assert.assertEquals("Updated default rating", orig.getDefaultRating(),
				updated.getDefaultRating());
		Assert.assertEquals("Updated type", orig.getType(), updated.getType());
		Assert.assertEquals("Updated room", orig.getRoom(), updated.getRoom());
		Assert.assertEquals("Updated category", orig.getCategory(), updated.getCategory());
		Assert.assertEquals("Updated states", orig.getStates(), updated.getStates());
	}

	@Test
	public void deleteForConfig() {
		insert();
		int result = dao.deleteAllForConfig(TEST_CONFIG_ID);
		Assert.assertEquals("Deleted count", 1, result);
		Control cat = dao.load(TEST_CONFIG_ID, lastControl.getUuid());
		Assert.assertNull("Control no longer available", cat);
	}

	@Test
	public void insertWithoutRoomOrCategory() {
		Control control = new Control();
		control.setUuid(UUID.randomUUID());
		control.setConfigId(TEST_CONFIG_ID);
		control.setName(TEST_NAME);
		dao.store(control);
		lastControl = control;
	}

	@Test
	public void getByPKWithoutRoomOrCategory() {
		insertWithoutRoomOrCategory();
		Control control = dao.load(TEST_CONFIG_ID, lastControl.getUuid());
		Assert.assertNotNull("Control inserted", control);
		Assert.assertEquals("UUID", lastControl.getUuid(), control.getUuid());
		Assert.assertEquals("Config ID", lastControl.getConfigId(), lastControl.getConfigId());
		Assert.assertEquals("Name", lastControl.getName(), control.getName());
		Assert.assertEquals("Default rating", Integer.valueOf(0), control.getDefaultRating());
		Assert.assertEquals("Default type", ControlType.Unknown, control.getType());
		Assert.assertNull("No room", control.getRoom());
		Assert.assertNull("No category", control.getCategory());
	}

	@Test
	public void findForConfigNoMatch() {
		insert();
		List<Control> results = dao.findAllForConfig(-1L, null);
		Assert.assertNotNull(results);
		Assert.assertEquals("No matches", 0, results.size());
	}

	@Test
	public void findForConfigSingleMatch() {
		insert();
		List<Control> results = dao.findAllForConfig(TEST_CONFIG_ID, null);
		Assert.assertNotNull(results);
		Assert.assertEquals("Match count", 1, results.size());
		Assert.assertEquals("Found object", lastControl.getUuid(), results.get(0).getUuid());
	}

	@Test
	public void findForConfigWithStatesSingleMatch() {
		insertWithStates();
		List<Control> results = dao.findAllForConfig(TEST_CONFIG_ID, null);
		Assert.assertNotNull(results);
		Assert.assertEquals("Match count", 1, results.size());
		Assert.assertEquals("Found object", lastControl.getUuid(), results.get(0).getUuid());
		Assert.assertEquals("State map", lastControl.getStates(), results.get(0).getStates());
	}
}
