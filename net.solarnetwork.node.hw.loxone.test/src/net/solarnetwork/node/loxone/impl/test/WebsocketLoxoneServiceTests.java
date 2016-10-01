/* ==================================================================
 * WebsocketLoxoneServiceTests.java - 1/10/2016 7:46:32 PM
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

package net.solarnetwork.node.loxone.impl.test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import net.solarnetwork.node.loxone.dao.DatumUUIDSetDao;
import net.solarnetwork.node.loxone.domain.BasicDatumUUIDEntity;
import net.solarnetwork.node.loxone.domain.BasicDatumUUIDEntityParameters;
import net.solarnetwork.node.loxone.domain.Config;
import net.solarnetwork.node.loxone.domain.DatumUUIDEntity;
import net.solarnetwork.node.loxone.domain.DatumUUIDEntityParameters;
import net.solarnetwork.node.loxone.impl.WebsocketLoxoneService;

/**
 * Test cases for the {@link WebsocketLoxoneService} class.
 * 
 * @author matt
 * @version 1.0
 */
public class WebsocketLoxoneServiceTests {

	private static final Long TEST_CONFIG_ID = 1234L;

	private DatumUUIDSetDao datumUuidSetDao;
	private WebsocketLoxoneService service;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Before
	public void setup() {
		datumUuidSetDao = EasyMock.createMock(DatumUUIDSetDao.class);

		service = new TestWebsocketLoxoneService(new Config(TEST_CONFIG_ID));
		service.setUuidSetDaos((List) Arrays.asList(datumUuidSetDao));
	}

	@After
	public void teardown() {
		verifyAll();
	}

	private void replayAll() {
		EasyMock.replay(datumUuidSetDao);
	}

	private void verifyAll() {
		EasyMock.verify(datumUuidSetDao);
	}

	@Test
	public void getDatumUUIDSet() {
		BasicDatumUUIDEntity e1 = new BasicDatumUUIDEntity(TEST_CONFIG_ID, UUID.randomUUID(),
				new BasicDatumUUIDEntityParameters(500));
		BasicDatumUUIDEntity e2 = new BasicDatumUUIDEntity(TEST_CONFIG_ID, UUID.randomUUID(),
				new BasicDatumUUIDEntityParameters(1000));
		List<DatumUUIDEntity> uuidEntities = Arrays.asList(e1, e2);

		EasyMock.expect(datumUuidSetDao.entityClass()).andReturn(DatumUUIDEntity.class).anyTimes();
		EasyMock.expect(datumUuidSetDao.findAllForConfig(TEST_CONFIG_ID, null)).andReturn(uuidEntities);

		replayAll();

		Map<UUID, DatumUUIDEntityParameters> uuidSet = service.getUUIDSet(DatumUUIDEntity.class, null);
		Assert.assertNotNull("Result set", uuidSet);
		Assert.assertEquals("Result count", 2, uuidSet.size());
		Assert.assertSame("E1 parameters", e1.getParameters(), uuidSet.get(e1.getUuid()));
		Assert.assertSame("E2 parameters", e2.getParameters(), uuidSet.get(e2.getUuid()));
	}

	private static final class TestWebsocketLoxoneService extends WebsocketLoxoneService {

		private final Config testConfig;

		private TestWebsocketLoxoneService(Config testConfig) {
			super();
			this.testConfig = testConfig;
		}

		@Override
		public Config getConfiguration() {
			return testConfig;
		}

	}

}
