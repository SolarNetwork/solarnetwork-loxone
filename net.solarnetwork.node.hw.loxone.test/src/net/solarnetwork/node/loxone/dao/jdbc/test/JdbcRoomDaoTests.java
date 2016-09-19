/* ==================================================================
 * JdbcRoomDaoTests.java - 18/09/2016 8:50:38 AM
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

import java.util.UUID;
import javax.annotation.Resource;
import javax.sql.DataSource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import net.solarnetwork.node.dao.jdbc.DatabaseSetup;
import net.solarnetwork.node.loxone.dao.jdbc.JdbcRoomDao;
import net.solarnetwork.node.loxone.domain.Config;
import net.solarnetwork.node.loxone.domain.Room;
import net.solarnetwork.node.test.AbstractNodeTransactionalTest;

/**
 * Unit tests for the {@link JdbcRoomDao} class.
 * 
 * @author matt
 * @version 1.0
 */
public class JdbcRoomDaoTests extends AbstractNodeTransactionalTest {

	private static final Long TEST_CONFIG_ID = 123L;
	private static final String TEST_NAME = "Test Name";
	private static final Integer TEST_DEFAULT_RATING = 1;

	@Resource(name = "dataSource")
	private DataSource dataSource;

	private JdbcRoomDao dao;
	private Room lastRoom;

	@Before
	public void setup() {
		DatabaseSetup setup = new DatabaseSetup();
		setup.setDataSource(dataSource);
		setup.init();

		dao = new JdbcRoomDao();
		dao.setDataSource(dataSource);
		dao.init();
	}

	@Test
	public void insert() {
		Room room = new Room();
		room.setUuid(UUID.randomUUID());
		room.setConfigId(TEST_CONFIG_ID);
		room.setName(TEST_NAME);
		room.setDefaultRating(TEST_DEFAULT_RATING);
		dao.store(room);
		lastRoom = room;
	}

	@Test
	public void getByPK() {
		insert();
		Room room = dao.load(lastRoom.getUuid());
		Assert.assertNotNull("Room inserted", room);
		Assert.assertEquals("UUID", lastRoom.getUuid(), room.getUuid());
		Assert.assertEquals("Config ID", lastRoom.getConfigId(), room.getConfigId());
		Assert.assertEquals("Name", lastRoom.getName(), room.getName());
		Assert.assertEquals("Default rating", lastRoom.getDefaultRating(), room.getDefaultRating());
	}

	@Test
	public void update() {
		insert();
		Room orig = dao.load(lastRoom.getUuid());
		orig.setName("Updated Name");
		orig.setDefaultRating(2);
		dao.store(orig);
		Room updated = dao.load(lastRoom.getUuid());
		Assert.assertEquals("Same UUID", orig.getUuid(), updated.getUuid());
		Assert.assertEquals("Same Config ID", orig.getConfigId(), updated.getConfigId());
		Assert.assertEquals("Updated name", orig.getName(), updated.getName());
		Assert.assertEquals("Updated default rating", orig.getDefaultRating(),
				updated.getDefaultRating());
	}

	@Test
	public void deleteForConfig() {
		insert();
		int result = dao.deleteAllForConfig(new Config(TEST_CONFIG_ID));
		Assert.assertEquals("Deleted count", 1, result);
		Room room = dao.load(lastRoom.getUuid());
		Assert.assertNull("Room no longer available", room);
	}
}
