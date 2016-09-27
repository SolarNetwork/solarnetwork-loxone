/* ==================================================================
 * JdbcDatumUUIDSetDaoTests.java - 27/09/2016 4:35:40 PM
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
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Resource;
import javax.sql.DataSource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import net.solarnetwork.node.dao.jdbc.DatabaseSetup;
import net.solarnetwork.node.loxone.dao.jdbc.JdbcDatumUUIDSetDao;
import net.solarnetwork.node.loxone.domain.BasicUUIDEntity;
import net.solarnetwork.node.test.AbstractNodeTransactionalTest;

/**
 * Unit tests for the {@link JdbcDatumUUIDSetDaoTests} class.
 * 
 * @author matt
 * @version 1.0
 */
public class JdbcDatumUUIDSetDaoTests extends AbstractNodeTransactionalTest {

	private static final Long TEST_CONFIG_ID = 123L;

	@Resource(name = "dataSource")
	private DataSource dataSource;

	private JdbcDatumUUIDSetDao dao;
	private BasicUUIDEntity lastEntity;

	@Before
	public void setup() {
		DatabaseSetup setup = new DatabaseSetup();
		setup.setDataSource(dataSource);
		setup.init();

		dao = new JdbcDatumUUIDSetDao();
		dao.setDataSource(dataSource);
		dao.init();
	}

	private BasicUUIDEntity createTestBasicUUIDEntity() {
		BasicUUIDEntity room = new BasicUUIDEntity();
		room.setUuid(UUID.randomUUID());
		room.setConfigId(TEST_CONFIG_ID);
		return room;
	}

	@Test
	public void insert() {
		BasicUUIDEntity room = createTestBasicUUIDEntity();
		dao.store(room);
		lastEntity = room;
	}

	@Test
	public void existsByPK() {
		insert();
		boolean found = dao.contains(TEST_CONFIG_ID, lastEntity.getUuid());
		Assert.assertTrue("BasicUUIDEntity found", found);
	}

	@Test
	public void update() {
		insert();
		dao.store(lastEntity);
		boolean found = dao.contains(TEST_CONFIG_ID, lastEntity.getUuid());
		Assert.assertTrue("BasicUUIDEntity found", found);
	}

	@Test
	public void deleteForConfig() {
		insert();
		int result = dao.deleteAllForConfig(TEST_CONFIG_ID);
		Assert.assertEquals("Deleted count", 1, result);
		boolean found = dao.contains(TEST_CONFIG_ID, lastEntity.getUuid());
		Assert.assertFalse("BasicUUIDEntity no longer found", found);
	}

	@Test
	public void deleteByPK() {
		insert();
		int result = dao.delete(TEST_CONFIG_ID, lastEntity.getUuid());
		Assert.assertEquals("Deleted count", 1, result);
		boolean found = dao.contains(TEST_CONFIG_ID, lastEntity.getUuid());
		Assert.assertFalse("BasicUUIDEntity no longer found", found);
	}

	@Test
	public void findForConfigNoMatch() {
		insert();
		List<BasicUUIDEntity> results = dao.findAllForConfig(-1L, null);
		Assert.assertNotNull(results);
		Assert.assertEquals("No matches", 0, results.size());
	}

	@Test
	public void findForConfigSingleMatch() {
		insert();
		List<BasicUUIDEntity> results = dao.findAllForConfig(TEST_CONFIG_ID, null);
		Assert.assertNotNull(results);
		Assert.assertEquals("Match count", 1, results.size());
		Assert.assertEquals("Found object", lastEntity.getUuid(), results.get(0).getUuid());
	}

	@Test
	public void findForConfigMultiMatch() {
		List<BasicUUIDEntity> all = new ArrayList<>(4);

		// insert rooms in descending order, to verify sort order of query
		for ( int i = 4; i > 0; i-- ) {
			BasicUUIDEntity r = createTestBasicUUIDEntity();
			dao.store(r);
			all.add(r);
		}

		// store some other rooms for a different config
		for ( int i = 0; i < 4; i++ ) {
			BasicUUIDEntity r = createTestBasicUUIDEntity();
			r.setConfigId(-1L);
			dao.store(r);
			all.add(r);
		}

		// verify query with default sort order applied (ascending by UUID)
		List<BasicUUIDEntity> results = dao.findAllForConfig(TEST_CONFIG_ID, null);
		Assert.assertNotNull(results);
		Assert.assertEquals("Match count", 4, results.size());
		UUID lastUUID = null;
		for ( int i = 0; i < 4; i++ ) {
			if ( lastUUID != null ) {
				Assert.assertTrue("UUID " + i + " greater than last",
						results.get(i).getUuid().compareTo(lastUUID) > 0);
			}
			lastUUID = results.get(i).getUuid();
		}
	}

	@Test
	public void manageSetPassNulls() {
		dao.updateSetForConfig(TEST_CONFIG_ID, null, null);
	}

	@Test
	public void manageSetAddOnly() {
		Set<UUID> add = Collections.singleton(UUID.randomUUID());
		dao.updateSetForConfig(TEST_CONFIG_ID, add, null);
		boolean found = dao.contains(TEST_CONFIG_ID, add.iterator().next());
		Assert.assertTrue("BasicUUIDEntity found", found);
	}

	@Test
	public void manageSetAddDuplicate() {
		insert();
		Set<UUID> add = Collections.singleton(lastEntity.getUuid());
		dao.updateSetForConfig(TEST_CONFIG_ID, add, null);
		boolean found = dao.contains(TEST_CONFIG_ID, add.iterator().next());
		Assert.assertTrue("BasicUUIDEntity found", found);
	}

	@Test
	public void manageSetRemove() {
		insert();
		Set<UUID> remove = Collections.singleton(lastEntity.getUuid());
		dao.updateSetForConfig(TEST_CONFIG_ID, null, remove);
		boolean found = dao.contains(TEST_CONFIG_ID, remove.iterator().next());
		Assert.assertFalse("BasicUUIDEntity deleted", found);
	}

	@Test
	public void manageSetAddAndRemoveSame() {
		insert();
		Set<UUID> uuids = Collections.singleton(lastEntity.getUuid());
		dao.updateSetForConfig(TEST_CONFIG_ID, uuids, uuids);
		boolean found = dao.contains(TEST_CONFIG_ID, uuids.iterator().next());
		Assert.assertFalse("BasicUUIDEntity deleted", found);
	}

	@Test
	public void manageSetAddAndRemove() {
		insert();
		Set<UUID> add = Collections.singleton(UUID.randomUUID());
		Set<UUID> remove = Collections.singleton(lastEntity.getUuid());
		dao.updateSetForConfig(TEST_CONFIG_ID, add, remove);
		boolean found = dao.contains(TEST_CONFIG_ID, add.iterator().next());
		Assert.assertTrue("BasicUUIDEntity added", found);
		found = dao.contains(TEST_CONFIG_ID, remove.iterator().next());
		Assert.assertFalse("BasicUUIDEntity deleted", found);
	}

}
