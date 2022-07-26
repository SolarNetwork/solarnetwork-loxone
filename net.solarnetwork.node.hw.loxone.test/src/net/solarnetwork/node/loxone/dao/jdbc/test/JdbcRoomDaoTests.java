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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import net.solarnetwork.domain.SimpleSortDescriptor;
import net.solarnetwork.domain.SortDescriptor;
import net.solarnetwork.node.dao.jdbc.DatabaseSetup;
import net.solarnetwork.node.loxone.dao.jdbc.JdbcRoomDao;
import net.solarnetwork.node.loxone.domain.Room;
import net.solarnetwork.node.test.AbstractNodeTransactionalTest;

/**
 * Unit tests for the {@link JdbcRoomDao} class.
 * 
 * @author matt
 * @version 1.1
 */
public class JdbcRoomDaoTests extends AbstractNodeTransactionalTest {

	private static final Long TEST_CONFIG_ID = 123L;
	private static final String TEST_NAME = "Test Name";
	private static final Integer TEST_DEFAULT_RATING = 1;

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

	private Room createTestRoom(String name, Integer defaultRating) {
		Room room = new Room();
		room.setUuid(UUID.randomUUID());
		room.setConfigId(TEST_CONFIG_ID);
		room.setName(name);
		room.setDefaultRating(defaultRating);
		return room;
	}

	@Test
	public void insert() {
		Room room = createTestRoom(TEST_NAME, TEST_DEFAULT_RATING);
		dao.store(room);
		lastRoom = room;
	}

	@Test
	public void getByPK() {
		insert();
		Room room = dao.load(TEST_CONFIG_ID, lastRoom.getUuid());
		Assert.assertNotNull("Room inserted", room);
		Assert.assertEquals("UUID", lastRoom.getUuid(), room.getUuid());
		Assert.assertEquals("Config ID", lastRoom.getConfigId(), room.getConfigId());
		Assert.assertEquals("Name", lastRoom.getName(), room.getName());
		Assert.assertEquals("Default rating", lastRoom.getDefaultRating(), room.getDefaultRating());
	}

	@Test
	public void update() {
		insert();
		Room orig = dao.load(TEST_CONFIG_ID, lastRoom.getUuid());
		orig.setName("Updated Name");
		orig.setDefaultRating(2);
		dao.store(orig);
		Room updated = dao.load(TEST_CONFIG_ID, lastRoom.getUuid());
		Assert.assertEquals("Same UUID", orig.getUuid(), updated.getUuid());
		Assert.assertEquals("Same Config ID", orig.getConfigId(), updated.getConfigId());
		Assert.assertEquals("Updated name", orig.getName(), updated.getName());
		Assert.assertEquals("Updated default rating", orig.getDefaultRating(),
				updated.getDefaultRating());
	}

	@Test
	public void deleteForConfig() {
		insert();
		int result = dao.deleteAllForConfig(TEST_CONFIG_ID);
		Assert.assertEquals("Deleted count", 1, result);
		Room room = dao.load(TEST_CONFIG_ID, lastRoom.getUuid());
		Assert.assertNull("Room no longer available", room);
	}

	@Test
	public void findForConfigNoMatch() {
		insert();
		List<Room> results = dao.findAllForConfig(-1L, null);
		Assert.assertNotNull(results);
		Assert.assertEquals("No matches", 0, results.size());
	}

	@Test
	public void findForConfigSingleMatch() {
		insert();
		List<Room> results = dao.findAllForConfig(TEST_CONFIG_ID, null);
		Assert.assertNotNull(results);
		Assert.assertEquals("Match count", 1, results.size());
		Assert.assertEquals("Found object", lastRoom.getUuid(), results.get(0).getUuid());
	}

	@Test
	public void findForConfigMultiMatch() {
		List<Room> all = new ArrayList<>(4);

		// insert rooms in descending order, to verify sort order of query
		for ( int i = 4; i > 0; i-- ) {
			Room r = createTestRoom("Room " + i, TEST_DEFAULT_RATING);
			dao.store(r);
			all.add(r);
		}

		// store some other rooms for a different config
		for ( int i = 0; i < 4; i++ ) {
			Room r = createTestRoom("Room " + i, TEST_DEFAULT_RATING);
			r.setConfigId(-1L);
			dao.store(r);
			all.add(r);
		}

		// verify query with default sort order applied (ascending by name)
		List<Room> results = dao.findAllForConfig(TEST_CONFIG_ID, null);
		Assert.assertNotNull(results);
		Assert.assertEquals("Match count", 4, results.size());
		for ( int i = 0; i < 4; i++ ) {
			Assert.assertEquals("Found object " + (i + 1), all.get(3 - i).getUuid(),
					results.get(i).getUuid());
		}
	}

	@Test
	public void findForConfigDefaultRatingSort() {
		List<Room> all = new ArrayList<>(4);

		// insert rooms in descending order, to verify sort order of query
		for ( int i = 4; i > 0; i-- ) {
			Room r = createTestRoom("Room " + i, i);
			dao.store(r);
			all.add(r);
		}

		// store some other rooms for a different config
		for ( int i = 0; i < 4; i++ ) {
			Room r = createTestRoom("Room " + i, TEST_DEFAULT_RATING);
			r.setConfigId(-1L);
			dao.store(r);
			all.add(r);
		}

		// verify query with default sort order applied (descending by defaultRating)
		List<Room> results = dao.findAllForConfig(TEST_CONFIG_ID, null);
		Assert.assertNotNull(results);
		Assert.assertEquals("Match count", 4, results.size());
		for ( int i = 0; i < 4; i++ ) {
			Assert.assertEquals("Found object " + (i + 1), all.get(i).getUuid(),
					results.get(i).getUuid());
		}
	}

	@Test
	public void findForConfigDefaultRatingThenNameSort() {
		List<Room> all = new ArrayList<>(4);

		// insert rooms with two at same defaultRating, so name sort applied
		all.add(createTestRoom("Room C", 2));
		all.add(createTestRoom("Room B", 1));
		all.add(createTestRoom("Room A", 1));
		for ( Room r : all ) {
			dao.store(r);
		}

		List<Room> results = dao.findAllForConfig(TEST_CONFIG_ID, null);
		Assert.assertNotNull(results);
		Assert.assertEquals("Match count", 3, results.size());
		Assert.assertEquals("Found object 1", all.get(0).getUuid(), results.get(0).getUuid());
		Assert.assertEquals("Found object 2", all.get(2).getUuid(), results.get(1).getUuid());
		Assert.assertEquals("Found object 3", all.get(1).getUuid(), results.get(2).getUuid());
	}

	@Test
	public void findForConfigCustomNameSort() {
		List<SortDescriptor> sorts = Collections.singletonList(new SimpleSortDescriptor("name", true));
		List<Room> all = new ArrayList<>(4);

		// insert rooms with two at same defaultRating, so name sort applied
		all.add(createTestRoom("Room A", 3));
		all.add(createTestRoom("Room B", 2));
		all.add(createTestRoom("Room C", 1));
		for ( Room r : all ) {
			dao.store(r);
		}

		List<Room> results = dao.findAllForConfig(TEST_CONFIG_ID, sorts);
		Assert.assertNotNull(results);
		Assert.assertEquals("Match count", 3, results.size());
		Assert.assertEquals("Found object 1", all.get(2).getUuid(), results.get(0).getUuid());
		Assert.assertEquals("Found object 2", all.get(1).getUuid(), results.get(1).getUuid());
		Assert.assertEquals("Found object 3", all.get(0).getUuid(), results.get(2).getUuid());
	}

	@Test
	public void findForConfigCustomNameAndDefaultRatingSort() {
		List<SortDescriptor> sorts = Arrays.asList(new SimpleSortDescriptor("name", true),
				new SimpleSortDescriptor("defaultRating", false));
		List<Room> all = new ArrayList<>(4);

		// insert rooms with two at same defaultRating, so name sort applied
		all.add(createTestRoom("Room A", 3));
		all.add(createTestRoom("Room A", 2));
		all.add(createTestRoom("Room C", 1));
		for ( Room r : all ) {
			dao.store(r);
		}

		List<Room> results = dao.findAllForConfig(TEST_CONFIG_ID, sorts);
		Assert.assertNotNull(results);
		Assert.assertEquals("Match count", 3, results.size());
		Assert.assertEquals("Found object 1", all.get(2).getUuid(), results.get(0).getUuid());
		Assert.assertEquals("Found object 2", all.get(1).getUuid(), results.get(1).getUuid());
		Assert.assertEquals("Found object 3", all.get(0).getUuid(), results.get(2).getUuid());
	}

	@Test
	public void findForName() {
		insert();
		List<Room> results = dao.findAllForConfigAndName(TEST_CONFIG_ID, lastRoom.getName(), null);
		Assert.assertNotNull("Results", results);
		Assert.assertEquals("Result count", 1, results.size());
		Assert.assertEquals("Category", lastRoom, results.get(0));
	}

}
