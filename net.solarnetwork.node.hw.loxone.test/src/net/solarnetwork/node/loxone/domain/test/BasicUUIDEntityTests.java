/* ==================================================================
 * BasicUUIDEntityTests.java - 12/10/2016 4:57:33 PM
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

package net.solarnetwork.node.loxone.domain.test;

import static org.junit.Assert.assertEquals;
import java.util.Arrays;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.solarnetwork.node.loxone.domain.BasicUUIDEntity;
import net.solarnetwork.node.loxone.domain.UUIDDeserializer;
import net.solarnetwork.node.loxone.domain.UUIDEntity;
import net.solarnetwork.node.loxone.domain.UUIDSerializer;
import net.solarnetwork.util.ObjectMapperFactoryBean;

/**
 * Test cases for the {@link BasicUUIDEntity} class.
 * 
 * @author matt
 * @version 1.0
 */
public class BasicUUIDEntityTests {

	private static final UUID TEST_UUID = new UUID(0xDEDEDEDEDEDEDEDEL, 0xEDEDEDEDEDEDEDEDL);
	private static final String TEST_UUID_STRING = "dededede-dede-dede-edededededededed";

	private ObjectMapper objectMapper;

	@Before
	public void setup() throws Exception {
		ObjectMapperFactoryBean factory = new ObjectMapperFactoryBean();
		factory.setDeserializers(Arrays.asList(new UUIDDeserializer()));
		factory.setSerializers(Arrays.asList(new UUIDSerializer()));
		factory.setFeaturesToDisable(Arrays.asList(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
		objectMapper = factory.getObject();
	}

	@Test
	public void derivedSourceId() {
		BasicUUIDEntity entity = new BasicUUIDEntity();
		entity.setConfigId(Long.MAX_VALUE);
		entity.setUuid(TEST_UUID);
		assertEquals("Source ID value", UUIDEntity.sourceIdForUUIDEntity(entity),
				entity.getSourceIdValue());
	}

	@Test
	public void explicitSourceId() {
		BasicUUIDEntity entity = new BasicUUIDEntity();
		entity.setConfigId(TEST_UUID.getMostSignificantBits());
		entity.setUuid(TEST_UUID);
		entity.setSourceId("test");
		assertEquals("Source ID value", "/dedededede/test", entity.getSourceIdValue());
	}

	@Test
	public void jsonSerializeDerivedSourceId() throws Exception {
		BasicUUIDEntity entity = new BasicUUIDEntity();
		entity.setConfigId(Long.MAX_VALUE);
		entity.setUuid(TEST_UUID);
		String json = objectMapper.writeValueAsString(entity);
		String expectedSourceId = UUIDEntity.sourceIdForUUIDEntity(entity);
		assertEquals("JSON",
				"{\"uuid\":\"" + TEST_UUID_STRING + "\",\"sourceId\":\"" + expectedSourceId + "\"}",
				json);
	}

	@Test
	public void jsonSerializeExplicitSourceId() throws Exception {
		BasicUUIDEntity entity = new BasicUUIDEntity();
		entity.setConfigId(TEST_UUID.getMostSignificantBits());
		entity.setUuid(TEST_UUID);
		entity.setSourceId("test");
		String json = objectMapper.writeValueAsString(entity);
		assertEquals("JSON", "{\"uuid\":\"" + TEST_UUID_STRING + "\",\"sourceId\":\"/dedededede/test\"}",
				json);
	}
}
