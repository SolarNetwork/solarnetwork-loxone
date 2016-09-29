/* ==================================================================
 * ValueEventGeneratorTests.java - 29/09/2016 7:09:33 AM
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

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import net.solarnetwork.node.dao.DatumDao;
import net.solarnetwork.node.domain.Datum;
import net.solarnetwork.node.domain.GeneralNodeDatum;
import net.solarnetwork.node.loxone.dao.DatumUUIDSetDao;
import net.solarnetwork.node.loxone.domain.ValueEvent;
import net.solarnetwork.node.loxone.impl.ValueEventDatumGenerator;
import net.solarnetwork.util.StaticOptionalService;

/**
 * Test cases for the {@link ValueEventDatumGenerator} class.
 * 
 * @author matt
 * @version 1.0
 */
public class ValueEventGeneratorTests {

	private static final Long TEST_CONFIG_ID = 123L;

	private DatumUUIDSetDao uuidSetDao;
	private DatumDao<Datum> datumDao;

	private ValueEventDatumGenerator service;

	@SuppressWarnings("unchecked")
	@Before
	public void setup() {
		uuidSetDao = EasyMock.createMock(DatumUUIDSetDao.class);
		datumDao = EasyMock.createMock(DatumDao.class);

		service = new ValueEventDatumGenerator(uuidSetDao,
				new StaticOptionalService<DatumDao<Datum>>(datumDao));
	}

	@After
	public void teardown() {
		verifyAll();
	}

	private void replayAll() {
		EasyMock.replay(uuidSetDao, datumDao);
	}

	private void verifyAll() {
		EasyMock.verify(uuidSetDao, datumDao);
	}

	@Test
	public void generateNull() {
		replayAll();
		int result = service.handleValueEvents(null, null);
		assertEquals("Processed", 0, result);
	}

	@Test
	public void generateEmpty() {
		List<ValueEvent> events = Collections.emptyList();
		replayAll();
		int result = service.handleValueEvents(events, null);
		assertEquals("Processed", 0, result);
	}

	@Test
	public void generateSingle() {
		ValueEvent event = new ValueEvent(UUID.randomUUID(), TEST_CONFIG_ID, 1.23);
		List<ValueEvent> events = Collections.singletonList(event);
		Date now = new Date();

		expect(uuidSetDao.contains(event.getConfigId(), event.getUuid())).andReturn(true);

		Capture<GeneralNodeDatum> datumCapture = new Capture<>();
		datumDao.storeDatum(capture(datumCapture));

		replayAll();

		int result = service.handleValueEvents(events, now);

		verifyAll();

		assertEquals("Processed", 1, result);

		GeneralNodeDatum gnd = datumCapture.getValue();

		assertEquals("Source ID in Base64(SHA1(configId+uuid)) form", "AAAAAAAAAHusQzH_laZAN7Qd9Lq8vBKp",
				gnd.getSourceId());
		Map<String, ?> sampleData = gnd.getSamples().getSampleData();
		Assert.assertNotNull("Sample data created", sampleData);
		assertEquals("Sample data count", 1, sampleData.size());
		assertEquals("Sample value", event.getValue(), sampleData.get("value"));
	}

}
