/* ==================================================================
 * JdbcDatumPropertyUUIDSetDaoTests.java - 27/09/2016 4:35:40 PM
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
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.context.transaction.BeforeTransaction;
import net.solarnetwork.node.dao.jdbc.DatabaseSetup;
import net.solarnetwork.node.loxone.dao.jdbc.JdbcDatumPropertyUUIDSetDao;
import net.solarnetwork.node.loxone.domain.BasicDatumPropertyUUIDEntity;
import net.solarnetwork.node.loxone.domain.BasicDatumPropertyUUIDEntityParameters;
import net.solarnetwork.node.loxone.domain.DatumPropertyUUIDEntity;
import net.solarnetwork.node.loxone.domain.DatumPropertyUUIDEntityParameters;
import net.solarnetwork.node.loxone.domain.DatumValueType;
import net.solarnetwork.node.test.AbstractNodeTransactionalTest;

/**
 * Unit tests for the {@link JdbcDatumPropertyUUIDSetDaoTests} class.
 *
 * @author matt
 * @version 1.1
 */
public class JdbcDatumPropertyUUIDSetDaoTests extends AbstractNodeTransactionalTest {

	private static final Long TEST_CONFIG_ID = 234L;

	private JdbcDatumPropertyUUIDSetDao dao;
	private BasicDatumPropertyUUIDEntity lastEntity;

	@BeforeTransaction
	public void setup() {
		DatabaseSetup setup = new DatabaseSetup();
		setup.setDataSource(dataSource);
		setup.init();

		dao = new JdbcDatumPropertyUUIDSetDao();
		dao.setDataSource(dataSource);
		dao.init();
	}

	private BasicDatumPropertyUUIDEntity createTestBasicDatumPropertyUUIDEntity() {
		BasicDatumPropertyUUIDEntity entity = new BasicDatumPropertyUUIDEntity();
		entity.setUuid(UUID.randomUUID());
		entity.setConfigId(TEST_CONFIG_ID);
		return entity;
	}

	private void addParameters(BasicDatumPropertyUUIDEntity e) {
		BasicDatumPropertyUUIDEntityParameters p = new BasicDatumPropertyUUIDEntityParameters();
		p.setDatumValueType(DatumValueType.Instantaneous);
		e.setParameters(p);
	}

	@Test
	public void insert() {
		BasicDatumPropertyUUIDEntity entity = createTestBasicDatumPropertyUUIDEntity();
		dao.store(entity);
		lastEntity = entity;
	}

	@Test
	public void insertWithParameters() {
		BasicDatumPropertyUUIDEntity entity = createTestBasicDatumPropertyUUIDEntity();
		addParameters(entity);
		dao.store(entity);
		lastEntity = entity;
	}

	@Test
	public void existsByPK() {
		insert();
		boolean found = dao.contains(TEST_CONFIG_ID, lastEntity.getUuid());
		Assert.assertTrue("BasicDatumPropertyUUIDEntity found", found);
	}

	@Test
	public void update() {
		insert();
		dao.store(lastEntity);
		boolean found = dao.contains(TEST_CONFIG_ID, lastEntity.getUuid());
		Assert.assertTrue("BasicDatumPropertyUUIDEntity found", found);
	}

	@Test
	public void updateWithParameters() {
		insertWithParameters();
		dao.store(lastEntity);
		DatumPropertyUUIDEntity found = dao.load(lastEntity.getConfigId(), lastEntity.getUuid());
		Assert.assertNotNull("BasicDatumPropertyUUIDEntity found", found);
		Assert.assertNotNull("Parameters returned", found.getParameters());
		Assert.assertEquals("Save frequency value", lastEntity.getParameters().getDatumValueType(),
				found.getParameters().getDatumValueType());
	}

	@Test
	public void deleteForConfig() {
		insert();
		int result = dao.deleteAllForConfig(TEST_CONFIG_ID);
		Assert.assertEquals("Deleted count", 1, result);
		boolean found = dao.contains(TEST_CONFIG_ID, lastEntity.getUuid());
		Assert.assertFalse("BasicDatumPropertyUUIDEntity no longer found", found);
	}

	@Test
	public void deleteByPK() {
		insert();
		int result = dao.delete(TEST_CONFIG_ID, lastEntity.getUuid());
		Assert.assertEquals("Deleted count", 1, result);
		boolean found = dao.contains(TEST_CONFIG_ID, lastEntity.getUuid());
		Assert.assertFalse("BasicDatumPropertyUUIDEntity no longer found", found);
	}

	@Test
	public void findForConfigNoMatch() {
		insert();
		List<DatumPropertyUUIDEntity> results = dao.findAllForConfig(-1L, null);
		Assert.assertNotNull(results);
		Assert.assertEquals("No matches", 0, results.size());
	}

	@Test
	public void findForConfigSingleMatch() {
		insert();
		List<DatumPropertyUUIDEntity> results = dao.findAllForConfig(TEST_CONFIG_ID, null);
		Assert.assertNotNull(results);
		Assert.assertEquals("Match count", 1, results.size());
		Assert.assertEquals("Found object", lastEntity.getUuid(), results.get(0).getUuid());
	}

	@Test
	public void findForConfigMultiMatch() {
		List<BasicDatumPropertyUUIDEntity> all = new ArrayList<>(4);

		// insert entitys in descending order, to verify sort order of query
		for ( int i = 4; i > 0; i-- ) {
			BasicDatumPropertyUUIDEntity r = createTestBasicDatumPropertyUUIDEntity();
			dao.store(r);
			all.add(r);
		}

		// store some other entitys for a different config
		for ( int i = 0; i < 4; i++ ) {
			BasicDatumPropertyUUIDEntity r = createTestBasicDatumPropertyUUIDEntity();
			r.setConfigId(-1L);
			dao.store(r);
			all.add(r);
		}

		// verify query with default sort order applied (ascending by UUID)
		List<DatumPropertyUUIDEntity> results = dao.findAllForConfig(TEST_CONFIG_ID, null);
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
		dao.updateSetForConfig(TEST_CONFIG_ID, null, null, null);
	}

	@Test
	public void manageSetAddOnly() {
		Set<UUID> add = Collections.singleton(UUID.randomUUID());
		dao.updateSetForConfig(TEST_CONFIG_ID, add, null, null);
		boolean found = dao.contains(TEST_CONFIG_ID, add.iterator().next());
		Assert.assertTrue("BasicDatumPropertyUUIDEntity found", found);
	}

	@Test
	public void manageSetAddDuplicate() {
		insert();
		Set<UUID> add = Collections.singleton(lastEntity.getUuid());
		dao.updateSetForConfig(TEST_CONFIG_ID, add, null, null);
		boolean found = dao.contains(TEST_CONFIG_ID, add.iterator().next());
		Assert.assertTrue("BasicDatumPropertyUUIDEntity found", found);
	}

	@Test
	public void manageSetRemove() {
		insert();
		Set<UUID> remove = Collections.singleton(lastEntity.getUuid());
		dao.updateSetForConfig(TEST_CONFIG_ID, null, remove, null);
		boolean found = dao.contains(TEST_CONFIG_ID, remove.iterator().next());
		Assert.assertFalse("BasicDatumPropertyUUIDEntity deleted", found);
	}

	@Test
	public void manageSetAddAndRemoveSame() {
		insert();
		Set<UUID> uuids = Collections.singleton(lastEntity.getUuid());
		dao.updateSetForConfig(TEST_CONFIG_ID, uuids, uuids, null);
		boolean found = dao.contains(TEST_CONFIG_ID, uuids.iterator().next());
		Assert.assertFalse("BasicDatumPropertyUUIDEntity deleted", found);
	}

	@Test
	public void manageSetAddAndRemove() {
		insert();
		Set<UUID> add = Collections.singleton(UUID.randomUUID());
		Set<UUID> remove = Collections.singleton(lastEntity.getUuid());
		dao.updateSetForConfig(TEST_CONFIG_ID, add, remove, null);
		boolean found = dao.contains(TEST_CONFIG_ID, add.iterator().next());
		Assert.assertTrue("BasicDatumPropertyUUIDEntity added", found);
		found = dao.contains(TEST_CONFIG_ID, remove.iterator().next());
		Assert.assertFalse("BasicDatumPropertyUUIDEntity deleted", found);
	}

	@Test
	public void manageSetAddAndRemoveWithParameters() {
		insertWithParameters();
		Set<UUID> add = Collections.singleton(UUID.randomUUID());
		Set<UUID> remove = Collections.singleton(lastEntity.getUuid());
		BasicDatumPropertyUUIDEntityParameters params = new BasicDatumPropertyUUIDEntityParameters();
		params.setDatumValueType(DatumValueType.Accumulating);
		Map<UUID, DatumPropertyUUIDEntityParameters> parameters = Collections
				.singletonMap(add.iterator().next(), params);
		dao.updateSetForConfig(TEST_CONFIG_ID, add, remove, parameters);

		DatumPropertyUUIDEntity found = dao.load(lastEntity.getConfigId(), add.iterator().next());
		Assert.assertNotNull("BasicDatumPropertyUUIDEntity found", found);
		Assert.assertNotNull("Parameters returned", found.getParameters());

		Assert.assertEquals("Datum type value", params.getDatumValueType(),
				found.getParameters().getDatumValueType());

		boolean exists = dao.contains(TEST_CONFIG_ID, remove.iterator().next());
		Assert.assertFalse("BasicDatumPropertyUUIDEntity deleted", exists);
	}

	@Test
	public void manageSetUpdateParameters() {
		insertWithParameters();
		BasicDatumPropertyUUIDEntityParameters params = new BasicDatumPropertyUUIDEntityParameters();
		params.setDatumValueType(DatumValueType.Accumulating);
		Map<UUID, DatumPropertyUUIDEntityParameters> parameters = Collections
				.singletonMap(lastEntity.getUuid(), params);
		dao.updateSetForConfig(TEST_CONFIG_ID, null, null, parameters);

		DatumPropertyUUIDEntity found = dao.load(lastEntity.getConfigId(), lastEntity.getUuid());
		Assert.assertNotNull("BasicDatumPropertyUUIDEntity found", found);
		Assert.assertNotNull("Parameters returned", found.getParameters());
		Assert.assertEquals("Datum type value", params.getDatumValueType(),
				found.getParameters().getDatumValueType());
	}

	@Test
	public void manageSetAddDuplicateAndUpdateParameters() {
		insertWithParameters();
		Set<UUID> add = Collections.singleton(lastEntity.getUuid());
		BasicDatumPropertyUUIDEntityParameters params = new BasicDatumPropertyUUIDEntityParameters();
		params.setDatumValueType(DatumValueType.Accumulating);
		Map<UUID, DatumPropertyUUIDEntityParameters> parameters = Collections
				.singletonMap(lastEntity.getUuid(), params);
		dao.updateSetForConfig(TEST_CONFIG_ID, add, null, parameters);

		DatumPropertyUUIDEntity found = dao.load(lastEntity.getConfigId(), lastEntity.getUuid());
		Assert.assertNotNull("BasicDatumPropertyUUIDEntity found", found);
		Assert.assertNotNull("Parameters returned", found.getParameters());
		Assert.assertEquals("Datum type value", params.getDatumValueType(),
				found.getParameters().getDatumValueType());
	}

	@Test
	public void manageSetAddDuplicateAndUpdateOnlySaveFrequencyParameter() {
		insertWithParameters();
		Set<UUID> add = Collections.singleton(lastEntity.getUuid());
		BasicDatumPropertyUUIDEntityParameters params = new BasicDatumPropertyUUIDEntityParameters();
		Map<UUID, DatumPropertyUUIDEntityParameters> parameters = Collections
				.singletonMap(lastEntity.getUuid(), params);
		dao.updateSetForConfig(TEST_CONFIG_ID, add, null, parameters);

		DatumPropertyUUIDEntity found = dao.load(lastEntity.getConfigId(), lastEntity.getUuid());
		Assert.assertNotNull("BasicDatumPropertyUUIDEntity found", found);
		Assert.assertNotNull("Parameters returned", found.getParameters());
		Assert.assertEquals("Datum type value unchanged", lastEntity.getParameters().getDatumValueType(),
				found.getParameters().getDatumValueType());
	}

}
