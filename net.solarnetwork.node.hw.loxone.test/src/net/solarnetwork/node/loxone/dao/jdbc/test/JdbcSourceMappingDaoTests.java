/* ==================================================================
 * JdbcSourceMappingDaoTests.java - 18/09/2016 8:50:38 AM
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
import java.util.UUID;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.context.transaction.BeforeTransaction;
import net.solarnetwork.domain.SimpleSortDescriptor;
import net.solarnetwork.domain.SortDescriptor;
import net.solarnetwork.node.dao.jdbc.DatabaseSetup;
import net.solarnetwork.node.loxone.dao.jdbc.JdbcSourceMappingDao;
import net.solarnetwork.node.loxone.domain.SourceMapping;
import net.solarnetwork.node.test.AbstractNodeTransactionalTest;

/**
 * Unit tests for the {@link JdbcSourceMappingDao} class.
 *
 * @author matt
 * @version 1.1
 */
public class JdbcSourceMappingDaoTests extends AbstractNodeTransactionalTest {

	private static final Long TEST_CONFIG_ID = 123L;
	private static final String TEST_SOURCE_ID = "Test Source";

	private JdbcSourceMappingDao dao;
	private SourceMapping lastSourceMapping;

	@BeforeTransaction
	public void setup() {
		DatabaseSetup setup = new DatabaseSetup();
		setup.setDataSource(dataSource);
		setup.init();

		dao = new JdbcSourceMappingDao();
		dao.setDataSource(dataSource);
		dao.init();
	}

	private SourceMapping createTestSourceMapping(String sourceId) {
		SourceMapping sourceMapping = new SourceMapping();
		sourceMapping.setUuid(UUID.randomUUID());
		sourceMapping.setConfigId(TEST_CONFIG_ID);
		sourceMapping.setSourceId(sourceId);
		return sourceMapping;
	}

	@Test
	public void insert() {
		SourceMapping sourceMapping = createTestSourceMapping(TEST_SOURCE_ID);
		dao.store(sourceMapping);
		lastSourceMapping = sourceMapping;
	}

	@Test
	public void getByPK() {
		insert();
		SourceMapping sourceMapping = dao.load(TEST_CONFIG_ID, lastSourceMapping.getUuid());
		Assert.assertNotNull("SourceMapping inserted", sourceMapping);
		Assert.assertEquals("UUID", lastSourceMapping.getUuid(), sourceMapping.getUuid());
		Assert.assertEquals("Config ID", lastSourceMapping.getConfigId(), sourceMapping.getConfigId());
		Assert.assertEquals("Source ID", lastSourceMapping.getSourceId(), sourceMapping.getSourceId());
		Assert.assertEquals("Default rating", lastSourceMapping.getDefaultRating(),
				sourceMapping.getDefaultRating());
	}

	@Test
	public void update() {
		insert();
		SourceMapping orig = dao.load(TEST_CONFIG_ID, lastSourceMapping.getUuid());
		orig.setSourceId("Updated SourceId");
		dao.store(orig);
		SourceMapping updated = dao.load(TEST_CONFIG_ID, lastSourceMapping.getUuid());
		Assert.assertEquals("Same UUID", orig.getUuid(), updated.getUuid());
		Assert.assertEquals("Same Config ID", orig.getConfigId(), updated.getConfigId());
		Assert.assertEquals("Updated source ID", orig.getSourceId(), updated.getSourceId());
	}

	@Test
	public void deleteForConfig() {
		insert();
		int result = dao.deleteAllForConfig(TEST_CONFIG_ID);
		Assert.assertEquals("Deleted count", 1, result);
		SourceMapping sourceMapping = dao.load(TEST_CONFIG_ID, lastSourceMapping.getUuid());
		Assert.assertNull("SourceMapping no longer available", sourceMapping);
	}

	@Test
	public void findForConfigNoMatch() {
		insert();
		List<SourceMapping> results = dao.findAllForConfig(-1L, null);
		Assert.assertNotNull(results);
		Assert.assertEquals("No matches", 0, results.size());
	}

	@Test
	public void findForConfigSingleMatch() {
		insert();
		List<SourceMapping> results = dao.findAllForConfig(TEST_CONFIG_ID, null);
		Assert.assertNotNull(results);
		Assert.assertEquals("Match count", 1, results.size());
		Assert.assertEquals("Found object", lastSourceMapping.getUuid(), results.get(0).getUuid());
	}

	@Test
	public void findForConfigMultiMatch() {
		List<SourceMapping> all = new ArrayList<>(4);

		// insert sourceMappings in descending order, to verify sort order of query
		for ( int i = 4; i > 0; i-- ) {
			SourceMapping r = createTestSourceMapping("SourceMapping " + i);
			dao.store(r);
			all.add(r);
		}

		// store some other sourceMappings for a different config
		for ( int i = 0; i < 4; i++ ) {
			SourceMapping r = createTestSourceMapping("SourceMapping " + i);
			r.setConfigId(-1L);
			dao.store(r);
			all.add(r);
		}

		// verify query with default sort order applied (ascending by name)
		List<SourceMapping> results = dao.findAllForConfig(TEST_CONFIG_ID, null);
		Assert.assertNotNull(results);
		Assert.assertEquals("Match count", 4, results.size());
		for ( int i = 0; i < 4; i++ ) {
			Assert.assertEquals("Found object " + (i + 1), all.get(3 - i).getUuid(),
					results.get(i).getUuid());
		}
	}

	@Test
	public void findForConfigDefaultSourceIdSort() {
		List<SourceMapping> all = new ArrayList<>(4);

		// insert sourceMappings in descending order, to verify sort order of query
		for ( int i = 4; i > 0; i-- ) {
			SourceMapping r = createTestSourceMapping("SourceMapping " + i);
			dao.store(r);
			all.add(r);
		}

		// store some other sourceMappings for a different config
		for ( int i = 0; i < 4; i++ ) {
			SourceMapping r = createTestSourceMapping("SourceMapping " + i);
			r.setConfigId(-1L);
			dao.store(r);
			all.add(r);
		}

		// verify query with default sort order applied (ascending by source ID)
		List<SourceMapping> results = dao.findAllForConfig(TEST_CONFIG_ID, null);
		Assert.assertNotNull(results);
		Assert.assertEquals("Match count", 4, results.size());
		for ( int i = 0; i < 4; i++ ) {
			Assert.assertEquals("Found object " + (i + 1), all.get(3 - i).getUuid(),
					results.get(i).getUuid());
		}
	}

	@Test
	public void findForConfigCustomSourceIdSort() {
		List<SortDescriptor> sorts = Collections
				.singletonList(new SimpleSortDescriptor("sourceId", true));
		List<SourceMapping> all = new ArrayList<>(4);

		// insert sourceMappings with two at same defaultRating, so name sort applied
		all.add(createTestSourceMapping("SourceMapping A"));
		all.add(createTestSourceMapping("SourceMapping B"));
		all.add(createTestSourceMapping("SourceMapping C"));
		for ( SourceMapping r : all ) {
			dao.store(r);
		}

		List<SourceMapping> results = dao.findAllForConfig(TEST_CONFIG_ID, sorts);
		Assert.assertNotNull(results);
		Assert.assertEquals("Match count", 3, results.size());
		Assert.assertEquals("Found object 1", all.get(2).getUuid(), results.get(0).getUuid());
		Assert.assertEquals("Found object 2", all.get(1).getUuid(), results.get(1).getUuid());
		Assert.assertEquals("Found object 3", all.get(0).getUuid(), results.get(2).getUuid());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void findForName() {
		insert();
		dao.findAllForConfigAndName(TEST_CONFIG_ID, "foo", null);
	}

}
