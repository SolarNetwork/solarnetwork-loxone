/* ==================================================================
 * JdbcControlDaoTests.java - 18/09/2016 8:50:38 AM
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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Resource;
import javax.sql.DataSource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import net.solarnetwork.node.dao.jdbc.DatabaseSetup;
import net.solarnetwork.node.loxone.dao.DatumPropertyUUIDSetDao;
import net.solarnetwork.node.loxone.dao.DatumUUIDSetDao;
import net.solarnetwork.node.loxone.dao.SourceMappingDao;
import net.solarnetwork.node.loxone.dao.ValueEventDao;
import net.solarnetwork.node.loxone.dao.jdbc.JdbcControlDao;
import net.solarnetwork.node.loxone.dao.jdbc.JdbcDatumPropertyUUIDSetDao;
import net.solarnetwork.node.loxone.dao.jdbc.JdbcDatumUUIDSetDao;
import net.solarnetwork.node.loxone.dao.jdbc.JdbcSourceMappingDao;
import net.solarnetwork.node.loxone.dao.jdbc.JdbcValueEventDao;
import net.solarnetwork.node.loxone.domain.BasicDatumPropertyUUIDEntity;
import net.solarnetwork.node.loxone.domain.BasicDatumPropertyUUIDEntityParameters;
import net.solarnetwork.node.loxone.domain.BasicDatumUUIDEntity;
import net.solarnetwork.node.loxone.domain.BasicDatumUUIDEntityParameters;
import net.solarnetwork.node.loxone.domain.Control;
import net.solarnetwork.node.loxone.domain.ControlDatumParameters;
import net.solarnetwork.node.loxone.domain.ControlType;
import net.solarnetwork.node.loxone.domain.DatumValueType;
import net.solarnetwork.node.loxone.domain.SourceMapping;
import net.solarnetwork.node.loxone.domain.UUIDEntityParametersPair;
import net.solarnetwork.node.loxone.domain.ValueEvent;
import net.solarnetwork.node.loxone.domain.ValueEventDatumParameters;
import net.solarnetwork.node.test.AbstractNodeTransactionalTest;

/**
 * Unit tests for the {@link JdbcControlDao} class.
 * 
 * @author matt
 * @version 1.2
 */
public class JdbcControlDaoTests extends AbstractNodeTransactionalTest {

	private static final Long TEST_CONFIG_ID = 123L;
	private static final String TEST_NAME = "Test Name";
	private static final Integer TEST_DEFAULT_RATING = 1;
	private static final String TEST_SOURCE_ID = "Test Source";

	@Resource(name = "dataSource")
	private DataSource dataSource;
	private DatumUUIDSetDao datumSetDao;
	private DatumPropertyUUIDSetDao datumPropSetDao;
	private ValueEventDao valueEventDao;
	private SourceMappingDao smapDao;

	private JdbcControlDao dao;
	private Control lastControl;

	@Before
	public void setup() {
		DatabaseSetup setup = new DatabaseSetup();
		setup.setDataSource(dataSource);
		setup.init();

		JdbcDatumUUIDSetDao uuidSetDao = new JdbcDatumUUIDSetDao();
		uuidSetDao.setDataSource(dataSource);
		uuidSetDao.init();
		this.datumSetDao = uuidSetDao;

		JdbcDatumPropertyUUIDSetDao propSetDao = new JdbcDatumPropertyUUIDSetDao();
		propSetDao.setDataSource(dataSource);
		propSetDao.init();
		this.datumPropSetDao = propSetDao;

		JdbcValueEventDao eventDao = new JdbcValueEventDao();
		eventDao.setDataSource(dataSource);
		eventDao.init();
		this.valueEventDao = eventDao;

		JdbcSourceMappingDao smapDao = new JdbcSourceMappingDao();
		smapDao.setDataSource(dataSource);
		smapDao.init();
		this.smapDao = smapDao;

		dao = new JdbcControlDao();
		dao.setDataSource(dataSource);
		dao.init();
	}

	private Control createControl(UUID uuid) {
		Control control = new Control();
		control.setUuid(uuid);
		control.setConfigId(TEST_CONFIG_ID);
		control.setName(TEST_NAME);
		control.setDefaultRating(TEST_DEFAULT_RATING);
		control.setType(ControlType.Meter);
		control.setRoom(UUID.randomUUID());
		control.setCategory(UUID.randomUUID());
		return control;
	}

	@Test
	public void insert() {
		Control control = createControl(UUID.randomUUID());
		dao.store(control);
		lastControl = control;
	}

	private Map<String, UUID> getTestStatesMap() {
		Map<String, UUID> map = new LinkedHashMap<>(4);
		map.put("foo", UUID.randomUUID());
		map.put("bar", UUID.randomUUID());
		return map;
	}

	@Test
	public void insertWithStates() {
		Control control = createControl(UUID.randomUUID());
		control.setStates(getTestStatesMap());
		dao.store(control);
		lastControl = control;
	}

	@Test
	public void getByPKWithStates() {
		insertWithStates();
		Control control = dao.load(TEST_CONFIG_ID, lastControl.getUuid());
		Assert.assertNotNull("Control inserted", control);
		Assert.assertEquals("UUID", lastControl.getUuid(), control.getUuid());
		Assert.assertEquals("Config ID", lastControl.getConfigId(), lastControl.getConfigId());
		Assert.assertEquals("Name", lastControl.getName(), control.getName());
		Assert.assertEquals("Default rating", lastControl.getDefaultRating(),
				control.getDefaultRating());
		Assert.assertEquals("Type", lastControl.getType(), control.getType());
		Assert.assertEquals("Room", lastControl.getRoom(), control.getRoom());
		Assert.assertEquals("Category", lastControl.getCategory(), control.getCategory());
		Assert.assertEquals("State map", lastControl.getStates(), control.getStates());
	}

	@Test
	public void deleteForConfigWithStates() {
		insertWithStates();
		int result = dao.deleteAllForConfig(TEST_CONFIG_ID);
		Assert.assertEquals("Deleted count", 1, result);
		Control cat = dao.load(TEST_CONFIG_ID, lastControl.getUuid());
		Assert.assertNull("Control no longer available", cat);
	}

	@Test
	public void getByPK() {
		insert();
		Control control = dao.load(TEST_CONFIG_ID, lastControl.getUuid());
		Assert.assertNotNull("Control inserted", control);
		Assert.assertEquals("UUID", lastControl.getUuid(), control.getUuid());
		Assert.assertEquals("Config ID", lastControl.getConfigId(), lastControl.getConfigId());
		Assert.assertEquals("Name", lastControl.getName(), control.getName());
		Assert.assertEquals("Default rating", lastControl.getDefaultRating(),
				control.getDefaultRating());
		Assert.assertEquals("Type", lastControl.getType(), control.getType());
		Assert.assertEquals("Room", lastControl.getRoom(), control.getRoom());
		Assert.assertEquals("Category", lastControl.getCategory(), control.getCategory());
	}

	@Test
	public void update() {
		insert();
		Control orig = dao.load(TEST_CONFIG_ID, lastControl.getUuid());
		orig.setName("Updated Name");
		orig.setDefaultRating(2);
		orig.setType(ControlType.Dimmer);
		orig.setRoom(UUID.randomUUID());
		orig.setCategory(UUID.randomUUID());
		dao.store(orig);
		Control updated = dao.load(TEST_CONFIG_ID, lastControl.getUuid());
		Assert.assertEquals("Same UUID", orig.getUuid(), updated.getUuid());
		Assert.assertEquals("Same Config ID", orig.getConfigId(), updated.getConfigId());
		Assert.assertEquals("Updated name", orig.getName(), updated.getName());
		Assert.assertEquals("Updated default rating", orig.getDefaultRating(),
				updated.getDefaultRating());
		Assert.assertEquals("Updated type", orig.getType(), updated.getType());
		Assert.assertEquals("Updated room", orig.getRoom(), updated.getRoom());
		Assert.assertEquals("Updated category", orig.getCategory(), updated.getCategory());
	}

	@Test
	public void updateWithStates() {
		insertWithStates();
		Control orig = dao.load(TEST_CONFIG_ID, lastControl.getUuid());
		orig.setName("Updated Name");
		orig.setDefaultRating(2);
		orig.setType(ControlType.Dimmer);
		orig.setRoom(UUID.randomUUID());
		orig.setCategory(UUID.randomUUID());
		orig.setStates(getTestStatesMap());
		dao.store(orig);
		Control updated = dao.load(TEST_CONFIG_ID, lastControl.getUuid());
		Assert.assertEquals("Same UUID", orig.getUuid(), updated.getUuid());
		Assert.assertEquals("Same Config ID", orig.getConfigId(), updated.getConfigId());
		Assert.assertEquals("Updated name", orig.getName(), updated.getName());
		Assert.assertEquals("Updated default rating", orig.getDefaultRating(),
				updated.getDefaultRating());
		Assert.assertEquals("Updated type", orig.getType(), updated.getType());
		Assert.assertEquals("Updated room", orig.getRoom(), updated.getRoom());
		Assert.assertEquals("Updated category", orig.getCategory(), updated.getCategory());
		Assert.assertEquals("Updated states", orig.getStates(), updated.getStates());
	}

	@Test
	public void deleteForConfig() {
		insert();
		int result = dao.deleteAllForConfig(TEST_CONFIG_ID);
		Assert.assertEquals("Deleted count", 1, result);
		Control cat = dao.load(TEST_CONFIG_ID, lastControl.getUuid());
		Assert.assertNull("Control no longer available", cat);
	}

	@Test
	public void insertWithoutRoomOrCategory() {
		Control control = new Control();
		control.setUuid(UUID.randomUUID());
		control.setConfigId(TEST_CONFIG_ID);
		control.setName(TEST_NAME);
		dao.store(control);
		lastControl = control;
	}

	@Test
	public void getByPKWithoutRoomOrCategory() {
		insertWithoutRoomOrCategory();
		Control control = dao.load(TEST_CONFIG_ID, lastControl.getUuid());
		Assert.assertNotNull("Control inserted", control);
		Assert.assertEquals("UUID", lastControl.getUuid(), control.getUuid());
		Assert.assertEquals("Config ID", lastControl.getConfigId(), lastControl.getConfigId());
		Assert.assertEquals("Name", lastControl.getName(), control.getName());
		Assert.assertEquals("Default rating", Integer.valueOf(0), control.getDefaultRating());
		Assert.assertEquals("Default type", ControlType.Unknown, control.getType());
		Assert.assertNull("No room", control.getRoom());
		Assert.assertNull("No category", control.getCategory());
	}

	@Test
	public void findForConfigNoMatch() {
		insert();
		List<Control> results = dao.findAllForConfig(-1L, null);
		Assert.assertNotNull(results);
		Assert.assertEquals("No matches", 0, results.size());
	}

	@Test
	public void findForConfigSingleMatch() {
		insert();
		List<Control> results = dao.findAllForConfig(TEST_CONFIG_ID, null);
		Assert.assertNotNull(results);
		Assert.assertEquals("Match count", 1, results.size());
		Assert.assertEquals("Found object", lastControl.getUuid(), results.get(0).getUuid());
		Assert.assertNull("No source ID", lastControl.getSourceId());
	}

	@Test
	public void findForConfigSingleMatchAndSourceId() {
		insert();

		SourceMapping smap = new SourceMapping();
		smap.setConfigId(TEST_CONFIG_ID);
		smap.setUuid(lastControl.getUuid());
		smap.setSourceId(TEST_SOURCE_ID);
		smapDao.store(smap);

		List<Control> results = dao.findAllForConfig(TEST_CONFIG_ID, null);
		Assert.assertNotNull(results);
		Assert.assertEquals("Match count", 1, results.size());
		Assert.assertEquals("Found object", lastControl.getUuid(), results.get(0).getUuid());
		Assert.assertEquals("Source ID", TEST_SOURCE_ID, results.get(0).getSourceId());
	}

	@Test
	public void findForConfigWithStatesSingleMatch() {
		insertWithStates();
		List<Control> results = dao.findAllForConfig(TEST_CONFIG_ID, null);
		Assert.assertNotNull(results);
		Assert.assertEquals("Match count", 1, results.size());
		Assert.assertEquals("Found object", lastControl.getUuid(), results.get(0).getUuid());
		Assert.assertEquals("State map", lastControl.getStates(), results.get(0).getStates());
	}

	@Test
	public void findForConfigAndStateSingleMatch() {
		insertWithStates();
		Control result = dao.getForConfigAndStateName(TEST_CONFIG_ID,
				lastControl.getStates().get("bar"));
		Assert.assertNotNull(result);
		Assert.assertEquals("Found object", lastControl.getUuid(), result.getUuid());
		Assert.assertEquals("State map", lastControl.getStates(), result.getStates());
	}

	@Test
	public void countForConfigNoMatch() {
		insert();
		int result = dao.countForConfig(-1L);
		Assert.assertEquals("No matches", 0, result);
	}

	@Test
	public void countForConfigSingleMatch() {
		insert();
		int result = dao.countForConfig(TEST_CONFIG_ID);
		Assert.assertEquals("Count", 1, result);
	}

	@Test
	public void findForDatumsNoMatch() {
		insert();
		List<UUIDEntityParametersPair<Control, ControlDatumParameters>> results = dao
				.findAllForDatumPropertyUUIDEntities(TEST_CONFIG_ID);
		Assert.assertNotNull(results);
		Assert.assertEquals("Match count", 0, results.size());
	}

	private ValueEvent insertValueEvent(UUID uuid, double value) {
		ValueEvent event = new ValueEvent(uuid, TEST_CONFIG_ID, value);
		valueEventDao.storeEvent(event);
		return event;
	}

	@Test
	public void findForDatumsOneControlMultiMatch() {
		// insert control with "foo" state
		insertWithStates();

		UUID fooUUID = lastControl.getStates().get("foo");
		UUID barUUID = lastControl.getStates().get("bar");

		// insert datum UUID to enable control
		datumSetDao.store(new BasicDatumUUIDEntity(TEST_CONFIG_ID, lastControl.getUuid(),
				new BasicDatumUUIDEntityParameters(500)));

		// insert prop UUID to enable property foo and bar
		datumPropSetDao.store(new BasicDatumPropertyUUIDEntity(TEST_CONFIG_ID, fooUUID,
				new BasicDatumPropertyUUIDEntityParameters(DatumValueType.Accumulating)));
		datumPropSetDao.store(new BasicDatumPropertyUUIDEntity(TEST_CONFIG_ID, barUUID,
				new BasicDatumPropertyUUIDEntityParameters(DatumValueType.Instantaneous)));

		// insert a value for the "foo" and "bar" states
		ValueEvent fooEvent = insertValueEvent(fooUUID, 123);
		ValueEvent barEvent = insertValueEvent(barUUID, 234);

		List<UUIDEntityParametersPair<Control, ControlDatumParameters>> results = dao
				.findAllForDatumPropertyUUIDEntities(TEST_CONFIG_ID);
		Assert.assertNotNull(results);
		Assert.assertEquals("Match count", 1, results.size());
		Assert.assertEquals("Same control", lastControl, results.get(0).getEntity());
		Assert.assertNull("No source ID", results.get(0).getEntity().getSourceId());
		Assert.assertEquals("Save frequency", Integer.valueOf(500),
				results.get(0).getParameters().getDatumParameters().getSaveFrequencySeconds());

		ValueEventDatumParameters propParams = results.get(0).getParameters()
				.getDatumPropertyParameters().get(fooUUID);
		Assert.assertNotNull(propParams);
		Assert.assertEquals("Foo type", DatumValueType.Accumulating, propParams.getDatumValueType());
		Assert.assertEquals("Foo name", "foo", propParams.getName());
		Assert.assertEquals("Foo value", Double.valueOf(fooEvent.getValue()), propParams.getValue());

		propParams = results.get(0).getParameters().getDatumPropertyParameters().get(barUUID);
		Assert.assertNotNull(propParams);
		Assert.assertEquals("Bar type", DatumValueType.Instantaneous, propParams.getDatumValueType());
		Assert.assertEquals("Bar name", "bar", propParams.getName());
		Assert.assertEquals("Bar value", Double.valueOf(barEvent.getValue()), propParams.getValue());
	}

	@Test
	public void findForDatumsOneControlMultiMatchAndSourceId() {
		// insert control with "foo" state
		insertWithStates();

		SourceMapping smap = new SourceMapping();
		smap.setConfigId(TEST_CONFIG_ID);
		smap.setUuid(lastControl.getUuid());
		smap.setSourceId(TEST_SOURCE_ID);
		smapDao.store(smap);

		UUID fooUUID = lastControl.getStates().get("foo");
		UUID barUUID = lastControl.getStates().get("bar");

		// insert datum UUID to enable control
		datumSetDao.store(new BasicDatumUUIDEntity(TEST_CONFIG_ID, lastControl.getUuid(),
				new BasicDatumUUIDEntityParameters(500)));

		// insert prop UUID to enable property foo and bar
		datumPropSetDao.store(new BasicDatumPropertyUUIDEntity(TEST_CONFIG_ID, fooUUID,
				new BasicDatumPropertyUUIDEntityParameters(DatumValueType.Accumulating)));
		datumPropSetDao.store(new BasicDatumPropertyUUIDEntity(TEST_CONFIG_ID, barUUID,
				new BasicDatumPropertyUUIDEntityParameters(DatumValueType.Instantaneous)));

		// insert a value for the "foo" and "bar" states
		ValueEvent fooEvent = insertValueEvent(fooUUID, 123);
		ValueEvent barEvent = insertValueEvent(barUUID, 234);

		List<UUIDEntityParametersPair<Control, ControlDatumParameters>> results = dao
				.findAllForDatumPropertyUUIDEntities(TEST_CONFIG_ID);
		Assert.assertNotNull(results);
		Assert.assertEquals("Match count", 1, results.size());
		Assert.assertEquals("Same control", lastControl, results.get(0).getEntity());
		Assert.assertEquals("Source ID", TEST_SOURCE_ID, results.get(0).getEntity().getSourceId());
		Assert.assertEquals("Save frequency", Integer.valueOf(500),
				results.get(0).getParameters().getDatumParameters().getSaveFrequencySeconds());

		ValueEventDatumParameters propParams = results.get(0).getParameters()
				.getDatumPropertyParameters().get(fooUUID);
		Assert.assertNotNull(propParams);
		Assert.assertEquals("Foo type", DatumValueType.Accumulating, propParams.getDatumValueType());
		Assert.assertEquals("Foo name", "foo", propParams.getName());
		Assert.assertEquals("Foo value", Double.valueOf(fooEvent.getValue()), propParams.getValue());

		propParams = results.get(0).getParameters().getDatumPropertyParameters().get(barUUID);
		Assert.assertNotNull(propParams);
		Assert.assertEquals("Bar type", DatumValueType.Instantaneous, propParams.getDatumValueType());
		Assert.assertEquals("Bar name", "bar", propParams.getName());
		Assert.assertEquals("Bar value", Double.valueOf(barEvent.getValue()), propParams.getValue());
	}

	@Test
	public void findForDatumsMultiControlMultiMatch() {
		// insert control with "foo" and "bar" states
		Control control1 = createControl(UUID.fromString("00000000-0000-0000-0000-00000001"));
		control1.setStates(getTestStatesMap());
		dao.store(control1);
		UUID fooUUID = control1.getStates().get("foo");
		UUID barUUID = control1.getStates().get("bar");

		// insert datum UUID to enable control
		datumSetDao.store(new BasicDatumUUIDEntity(TEST_CONFIG_ID, control1.getUuid(),
				new BasicDatumUUIDEntityParameters(500)));

		// insert prop UUID to enable property foo and bar
		datumPropSetDao.store(new BasicDatumPropertyUUIDEntity(TEST_CONFIG_ID, fooUUID,
				new BasicDatumPropertyUUIDEntityParameters(DatumValueType.Accumulating)));
		datumPropSetDao.store(new BasicDatumPropertyUUIDEntity(TEST_CONFIG_ID, barUUID,
				new BasicDatumPropertyUUIDEntityParameters(DatumValueType.Instantaneous)));

		// insert a value for the "foo" and "bar" states
		ValueEvent fooEvent = insertValueEvent(fooUUID, 123);
		ValueEvent barEvent = insertValueEvent(barUUID, 234);

		// insert control with "bam" and "pow" states
		Control control2 = createControl(UUID.fromString("00000000-0000-0000-0000-00000002"));
		Map<String, UUID> map = new LinkedHashMap<>(2);
		map.put("bam", UUID.randomUUID());
		map.put("pow", UUID.randomUUID());
		control2.setStates(map);
		dao.store(control2);
		UUID bamUUID = control2.getStates().get("bam");

		// insert datum UUID to enable control
		datumSetDao.store(new BasicDatumUUIDEntity(TEST_CONFIG_ID, control2.getUuid(),
				new BasicDatumUUIDEntityParameters(300)));

		// insert prop UUID to enable property "bam"
		datumPropSetDao.store(new BasicDatumPropertyUUIDEntity(TEST_CONFIG_ID, bamUUID,
				new BasicDatumPropertyUUIDEntityParameters(DatumValueType.Accumulating)));

		// insert a value for the "bam" states
		ValueEvent bamEvent = insertValueEvent(bamUUID, 345);

		List<UUIDEntityParametersPair<Control, ControlDatumParameters>> results = dao
				.findAllForDatumPropertyUUIDEntities(TEST_CONFIG_ID);
		Assert.assertNotNull(results);
		Assert.assertEquals("Match count", 2, results.size());
		Assert.assertEquals("Same control 1", control1, results.get(0).getEntity());
		Assert.assertEquals("Save frequency 1", Integer.valueOf(500),
				results.get(0).getParameters().getDatumParameters().getSaveFrequencySeconds());
		Assert.assertEquals("Same control 2", control2, results.get(1).getEntity());
		Assert.assertEquals("Save frequency 2", Integer.valueOf(300),
				results.get(1).getParameters().getDatumParameters().getSaveFrequencySeconds());

		ValueEventDatumParameters propParams = results.get(0).getParameters()
				.getDatumPropertyParameters().get(fooUUID);
		Assert.assertNotNull(propParams);
		Assert.assertEquals("Foo type", DatumValueType.Accumulating, propParams.getDatumValueType());
		Assert.assertEquals("Foo name", "foo", propParams.getName());
		Assert.assertEquals("Foo value", Double.valueOf(fooEvent.getValue()), propParams.getValue());

		propParams = results.get(0).getParameters().getDatumPropertyParameters().get(barUUID);
		Assert.assertNotNull(propParams);
		Assert.assertEquals("Bar type", DatumValueType.Instantaneous, propParams.getDatumValueType());
		Assert.assertEquals("Bar name", "bar", propParams.getName());
		Assert.assertEquals("Bar value", Double.valueOf(barEvent.getValue()), propParams.getValue());

		propParams = results.get(1).getParameters().getDatumPropertyParameters().get(bamUUID);
		Assert.assertNotNull(propParams);
		Assert.assertEquals("Bam type", DatumValueType.Accumulating, propParams.getDatumValueType());
		Assert.assertEquals("Bam name", "bam", propParams.getName());
		Assert.assertEquals("Bam value", Double.valueOf(bamEvent.getValue()), propParams.getValue());
	}

	@Test
	public void findForName() {
		insert();
		List<Control> results = dao.findAllForConfigAndName(TEST_CONFIG_ID, lastControl.getName(), null);
		Assert.assertNotNull("Results", results);
		Assert.assertEquals("Result count", 1, results.size());
		Assert.assertEquals("Category", lastControl, results.get(0));
	}

}
