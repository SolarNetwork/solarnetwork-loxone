/* ==================================================================
 * ValueEventDatumDataSourceTests.java - 2/10/2016 3:15:26 PM
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
import static org.junit.Assert.assertNotNull;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import net.solarnetwork.node.Setting;
import net.solarnetwork.node.dao.SettingDao;
import net.solarnetwork.node.domain.GeneralNodeDatum;
import net.solarnetwork.node.loxone.dao.ValueEventDao;
import net.solarnetwork.node.loxone.domain.BasicDatumUUIDEntityParameters;
import net.solarnetwork.node.loxone.domain.DatumUUIDEntityParameters;
import net.solarnetwork.node.loxone.domain.DatumValueType;
import net.solarnetwork.node.loxone.domain.UUIDEntityParametersPair;
import net.solarnetwork.node.loxone.domain.ValueEvent;
import net.solarnetwork.node.loxone.impl.ValueEventDatumDataSource;
import net.solarnetwork.node.support.KeyValuePair;

/**
 * Test cases for the {@link ValueEventDatumDataSource} class.
 * 
 * @author matt
 * @version 1.0
 */
public class ValueEventDatumDataSourceTests {

	private static final Long TEST_CONFIG_ID = 321L;

	private ValueEventDao valueEventDao;
	private SettingDao settingDao;
	private ValueEventDatumDataSource dataSource;

	@Before
	public void setup() {
		valueEventDao = EasyMock.createMock(ValueEventDao.class);
		settingDao = EasyMock.createMock(SettingDao.class);
		dataSource = new ValueEventDatumDataSource(TEST_CONFIG_ID, valueEventDao, settingDao);
	}

	@After
	public void teardown() {
		verifyAll();
	}

	private void replayAll() {
		EasyMock.replay(valueEventDao, settingDao);
	}

	private void verifyAll() {
		EasyMock.verify(valueEventDao, settingDao);
	}

	private String settingKey() {
		return ValueEventDatumDataSource.SETTING_KEY_PREFIX + Long.toString(TEST_CONFIG_ID, 16);
	}

	@Test
	public void produceInitialValue() {
		final String settingKey = settingKey();
		final long now = System.currentTimeMillis();
		final ValueEvent valueEvent = new ValueEvent(UUID.randomUUID(), TEST_CONFIG_ID, 456.7);
		final String sourceId = valueEvent.getSourceId();
		final BasicDatumUUIDEntityParameters datumParams = new BasicDatumUUIDEntityParameters(300,
				DatumValueType.Instantaneous);
		final List<UUIDEntityParametersPair<ValueEvent, DatumUUIDEntityParameters>> uuidSet = Arrays
				.asList(new UUIDEntityParametersPair<ValueEvent, DatumUUIDEntityParameters>(valueEvent,
						datumParams));
		expect(settingDao.getSettings(settingKey)).andReturn(Collections.emptyList());
		expect(valueEventDao.findAllForDatumUUIDEntities(TEST_CONFIG_ID)).andReturn(uuidSet);

		Capture<Setting> settingCapture = new Capture<>();
		settingDao.storeSetting(capture(settingCapture));

		replayAll();

		Collection<GeneralNodeDatum> results = dataSource.readMultipleDatum();

		verifyAll();

		assertNotNull("Result list", results);
		assertEquals("Result list size", 1, results.size());

		Setting savedSetting = settingCapture.getValue();
		assertEquals("Setting source ID", sourceId, savedSetting.getType());
		Assert.assertEquals("Setting value is now-ish",
				Long.parseLong(savedSetting.getValue(), 16) / 1000.0, now / 1000.0, 0.5);

		GeneralNodeDatum datum = results.iterator().next();
		assertEquals("Datum value", Double.valueOf(valueEvent.getValue()),
				datum.getInstantaneousSampleDouble("value"));
	}

	@Test
	public void produceInitialAccumulatingValue() {
		final String settingKey = settingKey();
		final long now = System.currentTimeMillis();
		final ValueEvent valueEvent = new ValueEvent(UUID.randomUUID(), TEST_CONFIG_ID, 456.7);
		final String sourceId = valueEvent.getSourceId();
		final BasicDatumUUIDEntityParameters datumParams = new BasicDatumUUIDEntityParameters(300,
				DatumValueType.Accumulating);
		final List<UUIDEntityParametersPair<ValueEvent, DatumUUIDEntityParameters>> uuidSet = Arrays
				.asList(new UUIDEntityParametersPair<ValueEvent, DatumUUIDEntityParameters>(valueEvent,
						datumParams));
		expect(settingDao.getSettings(settingKey)).andReturn(Collections.emptyList());
		expect(valueEventDao.findAllForDatumUUIDEntities(TEST_CONFIG_ID)).andReturn(uuidSet);

		Capture<Setting> settingCapture = new Capture<>();
		settingDao.storeSetting(capture(settingCapture));

		replayAll();

		Collection<GeneralNodeDatum> results = dataSource.readMultipleDatum();

		verifyAll();

		assertNotNull("Result list", results);
		assertEquals("Result list size", 1, results.size());

		Setting savedSetting = settingCapture.getValue();
		assertEquals("Setting source ID", sourceId, savedSetting.getType());
		Assert.assertEquals("Setting value is now-ish",
				Long.parseLong(savedSetting.getValue(), 16) / 1000.0, now / 1000.0, 0.5);

		GeneralNodeDatum datum = results.iterator().next();
		assertEquals("Datum value", Double.valueOf(valueEvent.getValue()),
				datum.getAccumulatingSampleDouble("value"));
	}

	@Test
	public void skipValueTooSoon() {
		final String settingKey = settingKey();
		final long now = System.currentTimeMillis();
		final ValueEvent valueEvent = new ValueEvent(UUID.randomUUID(), TEST_CONFIG_ID, 456.7);
		final String sourceId = valueEvent.getSourceId();
		final BasicDatumUUIDEntityParameters datumParams = new BasicDatumUUIDEntityParameters(300,
				DatumValueType.Instantaneous);
		final List<UUIDEntityParametersPair<ValueEvent, DatumUUIDEntityParameters>> uuidSet = Arrays
				.asList(new UUIDEntityParametersPair<ValueEvent, DatumUUIDEntityParameters>(valueEvent,
						datumParams));
		final List<KeyValuePair> saveSettings = Arrays
				.asList(new KeyValuePair(sourceId, Long.toString(now - 100000, 16)));
		expect(settingDao.getSettings(settingKey)).andReturn(saveSettings);
		expect(valueEventDao.findAllForDatumUUIDEntities(TEST_CONFIG_ID)).andReturn(uuidSet);

		replayAll();

		Collection<GeneralNodeDatum> results = dataSource.readMultipleDatum();

		assertNotNull("Result list", results);
		assertEquals("Result list size", 0, results.size());
	}

	@Test
	public void produceAdditionalValue() {
		final String settingKey = settingKey();
		final long now = System.currentTimeMillis();
		final ValueEvent valueEvent = new ValueEvent(UUID.randomUUID(), TEST_CONFIG_ID, 456.7);
		final String sourceId = valueEvent.getSourceId();
		final BasicDatumUUIDEntityParameters datumParams = new BasicDatumUUIDEntityParameters(300,
				DatumValueType.Instantaneous);
		final List<UUIDEntityParametersPair<ValueEvent, DatumUUIDEntityParameters>> uuidSet = Arrays
				.asList(new UUIDEntityParametersPair<ValueEvent, DatumUUIDEntityParameters>(valueEvent,
						datumParams));
		final List<KeyValuePair> saveSettings = Arrays
				.asList(new KeyValuePair(sourceId, Long.toString(now - 300001, 16)));
		expect(settingDao.getSettings(settingKey)).andReturn(saveSettings);
		expect(valueEventDao.findAllForDatumUUIDEntities(TEST_CONFIG_ID)).andReturn(uuidSet);

		Capture<Setting> settingCapture = new Capture<>();
		settingDao.storeSetting(capture(settingCapture));

		replayAll();

		Collection<GeneralNodeDatum> results = dataSource.readMultipleDatum();

		verifyAll();

		assertNotNull("Result list", results);
		assertEquals("Result list size", 1, results.size());

		Setting savedSetting = settingCapture.getValue();
		assertEquals("Setting source ID", sourceId, savedSetting.getType());
		Assert.assertEquals("Setting value is now-ish",
				Long.parseLong(savedSetting.getValue(), 16) / 1000.0, now / 1000.0, 0.5);

		GeneralNodeDatum datum = results.iterator().next();
		assertEquals("Datum value", Double.valueOf(valueEvent.getValue()),
				datum.getInstantaneousSampleDouble("value"));
	}

}
