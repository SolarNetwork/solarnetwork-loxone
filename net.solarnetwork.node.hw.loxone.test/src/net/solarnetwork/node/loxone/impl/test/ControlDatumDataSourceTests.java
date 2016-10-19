/* ==================================================================
 * ControlDatumDataSourceTests.java - 2/10/2016 3:15:26 PM
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
import net.solarnetwork.node.loxone.dao.ControlDao;
import net.solarnetwork.node.loxone.domain.BasicControlDatumParameters;
import net.solarnetwork.node.loxone.domain.BasicDatumUUIDEntityParameters;
import net.solarnetwork.node.loxone.domain.BasicValueEventDatumParameters;
import net.solarnetwork.node.loxone.domain.Config;
import net.solarnetwork.node.loxone.domain.Control;
import net.solarnetwork.node.loxone.domain.ControlDatumParameters;
import net.solarnetwork.node.loxone.domain.DatumValueType;
import net.solarnetwork.node.loxone.domain.UUIDEntityParametersPair;
import net.solarnetwork.node.loxone.domain.ValueEvent;
import net.solarnetwork.node.loxone.impl.ControlDatumDataSource;
import net.solarnetwork.node.support.KeyValuePair;

/**
 * Test cases for the {@link ControlDatumDataSource} class.
 * 
 * @author matt
 * @version 1.0
 */
public class ControlDatumDataSourceTests {

	private static final Long TEST_CONFIG_ID = 321L;

	private ControlDao controlDao;
	private SettingDao settingDao;
	private ControlDatumDataSource dataSource;

	@Before
	public void setup() {
		controlDao = EasyMock.createMock(ControlDao.class);
		settingDao = EasyMock.createMock(SettingDao.class);
		dataSource = new ControlDatumDataSource(TEST_CONFIG_ID, controlDao, settingDao);
	}

	@After
	public void teardown() {
		verifyAll();
	}

	private void replayAll() {
		EasyMock.replay(controlDao, settingDao);
	}

	private void verifyAll() {
		EasyMock.verify(controlDao, settingDao);
	}

	private String settingKey() {
		return String.format(ControlDatumDataSource.SETTING_KEY_TEMPLATE,
				Config.idToExternalForm(TEST_CONFIG_ID));
	}

	@Test
	public void produceInitialValue() {
		final String settingKey = settingKey();
		final long now = System.currentTimeMillis();
		final Control control = new Control(UUID.randomUUID(), TEST_CONFIG_ID);
		final ValueEvent valueEvent = new ValueEvent(UUID.randomUUID(), TEST_CONFIG_ID, 456.7);
		final BasicControlDatumParameters controlParams = new BasicControlDatumParameters();
		final String sourceId = control.getSourceIdValue();
		final BasicDatumUUIDEntityParameters datumParams = new BasicDatumUUIDEntityParameters(300);
		controlParams.setDatumParameters(datumParams);
		final BasicValueEventDatumParameters valueParams = new BasicValueEventDatumParameters();
		valueParams.setDatumValueType(DatumValueType.Instantaneous);
		valueParams.setName("foo");
		valueParams.setValue(valueEvent.getValue());
		controlParams
				.setDatumPropertyParameters(Collections.singletonMap(valueEvent.getUuid(), valueParams));
		final List<UUIDEntityParametersPair<Control, ControlDatumParameters>> uuidSet = Arrays.asList(
				new UUIDEntityParametersPair<Control, ControlDatumParameters>(control, controlParams));
		expect(settingDao.getSettings(settingKey)).andReturn(Collections.emptyList());
		expect(controlDao.findAllForDatumPropertyUUIDEntities(TEST_CONFIG_ID)).andReturn(uuidSet);

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
				datum.getInstantaneousSampleDouble("foo"));
	}

	@Test
	public void produceInitialAccumulatingValue() {
		final String settingKey = settingKey();
		final long now = System.currentTimeMillis();
		final Control control = new Control(UUID.randomUUID(), TEST_CONFIG_ID);
		final ValueEvent valueEvent = new ValueEvent(UUID.randomUUID(), TEST_CONFIG_ID, 456.7);
		final BasicControlDatumParameters controlParams = new BasicControlDatumParameters();
		final String sourceId = control.getSourceIdValue();
		final BasicDatumUUIDEntityParameters datumParams = new BasicDatumUUIDEntityParameters(300);
		controlParams.setDatumParameters(datumParams);
		final BasicValueEventDatumParameters valueParams = new BasicValueEventDatumParameters();
		valueParams.setDatumValueType(DatumValueType.Accumulating);
		valueParams.setName("foo");
		valueParams.setValue(valueEvent.getValue());
		controlParams
				.setDatumPropertyParameters(Collections.singletonMap(valueEvent.getUuid(), valueParams));
		final List<UUIDEntityParametersPair<Control, ControlDatumParameters>> uuidSet = Arrays.asList(
				new UUIDEntityParametersPair<Control, ControlDatumParameters>(control, controlParams));
		expect(settingDao.getSettings(settingKey)).andReturn(Collections.emptyList());
		expect(controlDao.findAllForDatumPropertyUUIDEntities(TEST_CONFIG_ID)).andReturn(uuidSet);

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
				datum.getAccumulatingSampleDouble("foo"));
	}

	@Test
	public void skipValueTooSoon() {
		final String settingKey = settingKey();
		final long now = System.currentTimeMillis();
		final Control control = new Control(UUID.randomUUID(), TEST_CONFIG_ID);
		final ValueEvent valueEvent = new ValueEvent(UUID.randomUUID(), TEST_CONFIG_ID, 456.7);
		final BasicControlDatumParameters controlParams = new BasicControlDatumParameters();
		final String sourceId = control.getSourceIdValue();
		final BasicDatumUUIDEntityParameters datumParams = new BasicDatumUUIDEntityParameters(300);
		controlParams.setDatumParameters(datumParams);
		final BasicValueEventDatumParameters valueParams = new BasicValueEventDatumParameters();
		valueParams.setDatumValueType(DatumValueType.Accumulating);
		valueParams.setName("foo");
		valueParams.setValue(valueEvent.getValue());
		controlParams
				.setDatumPropertyParameters(Collections.singletonMap(valueEvent.getUuid(), valueParams));
		final List<UUIDEntityParametersPair<Control, ControlDatumParameters>> uuidSet = Arrays.asList(
				new UUIDEntityParametersPair<Control, ControlDatumParameters>(control, controlParams));
		final List<KeyValuePair> saveSettings = Arrays
				.asList(new KeyValuePair(sourceId, Long.toString(now - 100000, 16)));
		expect(settingDao.getSettings(settingKey)).andReturn(saveSettings);
		expect(controlDao.findAllForDatumPropertyUUIDEntities(TEST_CONFIG_ID)).andReturn(uuidSet);

		replayAll();

		Collection<GeneralNodeDatum> results = dataSource.readMultipleDatum();

		assertNotNull("Result list", results);
		assertEquals("Result list size", 0, results.size());
	}

	@Test
	public void produceAdditionalValue() {
		final String settingKey = settingKey();
		final long now = System.currentTimeMillis();
		final Control control = new Control(UUID.randomUUID(), TEST_CONFIG_ID);
		final ValueEvent valueEvent = new ValueEvent(UUID.randomUUID(), TEST_CONFIG_ID, 456.7);
		final BasicControlDatumParameters controlParams = new BasicControlDatumParameters();
		final String sourceId = control.getSourceIdValue();
		final BasicDatumUUIDEntityParameters datumParams = new BasicDatumUUIDEntityParameters(300);
		controlParams.setDatumParameters(datumParams);
		final BasicValueEventDatumParameters valueParams = new BasicValueEventDatumParameters();
		valueParams.setDatumValueType(DatumValueType.Instantaneous);
		valueParams.setName("foo");
		valueParams.setValue(valueEvent.getValue());
		controlParams
				.setDatumPropertyParameters(Collections.singletonMap(valueEvent.getUuid(), valueParams));
		final List<UUIDEntityParametersPair<Control, ControlDatumParameters>> uuidSet = Arrays.asList(
				new UUIDEntityParametersPair<Control, ControlDatumParameters>(control, controlParams));
		final List<KeyValuePair> saveSettings = Arrays
				.asList(new KeyValuePair(sourceId, Long.toString(now - 300001, 16)));
		expect(settingDao.getSettings(settingKey)).andReturn(saveSettings);
		expect(controlDao.findAllForDatumPropertyUUIDEntities(TEST_CONFIG_ID)).andReturn(uuidSet);

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
				datum.getInstantaneousSampleDouble("foo"));
	}

}
