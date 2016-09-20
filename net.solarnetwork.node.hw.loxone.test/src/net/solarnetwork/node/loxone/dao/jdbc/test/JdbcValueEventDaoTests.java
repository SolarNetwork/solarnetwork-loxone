/* ==================================================================
 * JdbcValueEventDaoTests.java - 19/09/2016 7:36:45 AM
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
import net.solarnetwork.node.loxone.dao.jdbc.JdbcValueEventDao;
import net.solarnetwork.node.loxone.domain.ValueEvent;
import net.solarnetwork.node.test.AbstractNodeTransactionalTest;

/**
 * Unit tests for the {@link JdbcValueEventDao} class.
 * 
 * @author matt
 * @version 1.0
 */
public class JdbcValueEventDaoTests extends AbstractNodeTransactionalTest {

	private static final Long TEST_CONFIG_ID = 123L;
	private static final double TEST_DOUBLE = 123.4;

	@Resource(name = "dataSource")
	private DataSource dataSource;

	private JdbcValueEventDao dao;
	private ValueEvent lastValueEvent;

	@Before
	public void setup() {
		DatabaseSetup setup = new DatabaseSetup();
		setup.setDataSource(dataSource);
		setup.init();

		dao = new JdbcValueEventDao();
		dao.setDataSource(dataSource);
		dao.init();
	}

	@Test
	public void insert() {
		ValueEvent event = new ValueEvent(UUID.randomUUID(), TEST_CONFIG_ID, TEST_DOUBLE);
		dao.storeEvent(event);
		lastValueEvent = event;
	}

	@Test
	public void getByPK() {
		insert();
		ValueEvent event = dao.loadEvent(lastValueEvent.getUuid());
		Assert.assertNotNull("ValueEvent inserted", event);
		Assert.assertEquals("UUID", lastValueEvent.getUuid(), event.getUuid());
		Assert.assertEquals("Value", lastValueEvent.getValue(), event.getValue(), 0.1);
	}

	@Test
	public void update() {
		insert();
		ValueEvent orig = dao.loadEvent(lastValueEvent.getUuid());
		ValueEvent modified = new ValueEvent(orig.getUuid(), TEST_CONFIG_ID, 234.5);
		dao.storeEvent(modified);
		ValueEvent updated = dao.loadEvent(orig.getUuid());
		Assert.assertEquals("Same UUID", orig.getUuid(), updated.getUuid());
		Assert.assertEquals("Updated value", modified.getValue(), updated.getValue(), 0.1);
	}

}
