/* ==================================================================
 * JdbcCategoryDaoTests.java - 18/09/2016 8:50:38 AM
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

import java.util.List;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import net.solarnetwork.node.dao.jdbc.DatabaseSetup;
import net.solarnetwork.node.loxone.dao.jdbc.JdbcCategoryDao;
import net.solarnetwork.node.loxone.domain.Category;
import net.solarnetwork.node.loxone.domain.CategoryType;
import net.solarnetwork.node.test.AbstractNodeTransactionalTest;

/**
 * Unit tests for the {@link JdbcCategoryDao} class.
 * 
 * @author matt
 * @version 1.2
 */
public class JdbcCategoryDaoTests extends AbstractNodeTransactionalTest {

	private static final Long TEST_CONFIG_ID = 123L;
	private static final String TEST_NAME = "Test Name";
	private static final Integer TEST_DEFAULT_RATING = 1;

	private JdbcCategoryDao dao;
	private Category lastCategory;

	@Before
	public void setup() {
		DatabaseSetup setup = new DatabaseSetup();
		setup.setDataSource(dataSource);
		setup.init();

		dao = new JdbcCategoryDao();
		dao.setDataSource(dataSource);
		dao.init();
	}

	@Test
	public void insert() {
		Category category = new Category();
		category.setUuid(UUID.randomUUID());
		category.setConfigId(TEST_CONFIG_ID);
		category.setName(TEST_NAME);
		category.setDefaultRating(TEST_DEFAULT_RATING);
		category.setType(CategoryType.Lights);
		dao.store(category);
		lastCategory = category;
	}

	@Test
	public void getByPK() {
		insert();
		Category category = dao.load(TEST_CONFIG_ID, lastCategory.getUuid());
		Assert.assertNotNull("Category inserted", category);
		Assert.assertEquals("UUID", lastCategory.getUuid(), category.getUuid());
		Assert.assertEquals("Config ID", lastCategory.getConfigId(), category.getConfigId());
		Assert.assertEquals("Name", lastCategory.getName(), category.getName());
		Assert.assertEquals("Default rating", lastCategory.getDefaultRating(),
				category.getDefaultRating());
		Assert.assertEquals("Type", lastCategory.getType(), category.getType());
	}

	@Test
	public void update() {
		insert();
		Category orig = dao.load(TEST_CONFIG_ID, lastCategory.getUuid());
		orig.setName("Updated Name");
		orig.setDefaultRating(2);
		orig.setType(CategoryType.Shading);
		dao.store(orig);
		Category updated = dao.load(TEST_CONFIG_ID, lastCategory.getUuid());
		Assert.assertEquals("Same UUID", orig.getUuid(), updated.getUuid());
		Assert.assertEquals("Same Config ID", orig.getConfigId(), updated.getConfigId());
		Assert.assertEquals("Updated name", orig.getName(), updated.getName());
		Assert.assertEquals("Updated default rating", orig.getDefaultRating(),
				updated.getDefaultRating());
		Assert.assertEquals("Updated type", orig.getType(), updated.getType());
	}

	@Test
	public void deleteForConfig() {
		insert();
		int result = dao.deleteAllForConfig(TEST_CONFIG_ID);
		Assert.assertEquals("Deleted count", 1, result);
		Category cat = dao.load(TEST_CONFIG_ID, lastCategory.getUuid());
		Assert.assertNull("Category no longer available", cat);
	}

	@Test
	public void findForName() {
		insert();
		List<Category> results = dao.findAllForConfigAndName(TEST_CONFIG_ID, lastCategory.getName(),
				null);
		Assert.assertNotNull("Results", results);
		Assert.assertEquals("Result count", 1, results.size());
		Assert.assertEquals("Category", lastCategory, results.get(0));
	}

}
