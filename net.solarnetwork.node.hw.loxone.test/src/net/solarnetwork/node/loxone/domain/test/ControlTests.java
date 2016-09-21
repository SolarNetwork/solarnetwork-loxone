/* ==================================================================
 * ControlTests.java - 20/09/2016 7:10:49 AM
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.solarnetwork.node.loxone.domain.Control;
import net.solarnetwork.node.loxone.domain.ControlType;
import net.solarnetwork.node.loxone.domain.UUIDDeserializer;
import net.solarnetwork.node.loxone.domain.UUIDSerializer;
import net.solarnetwork.util.ObjectMapperFactoryBean;

/**
 * Unit tests for the {@link Control} class.
 * 
 * @author matt
 * @version 1.0
 */
public class ControlTests {

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
	public void jsonDeserialize() throws IOException {
		InputStream in = getClass().getResourceAsStream("control-01.json");
		Control control = objectMapper.readValue(in, Control.class);
		assertNotNull(control);
		assertEquals("UUID", UUID.fromString("0c89ebff-037f-15d6-ffff-a1b98ee6c71d"), control.getUuid());
		assertEquals("Name", "Generator status", control.getName());
		assertEquals("Default rating", Integer.valueOf(0), control.getDefaultRating());
		assertEquals("Category", UUID.fromString("0c89ebac-0031-030a-ffff-a1b98ee6c71d"),
				control.getCategory());
		assertEquals("Room", UUID.fromString("0c89ebac-0041-0390-ffff-a1b98ee6c71d"), control.getRoom());
		assertEquals("States", Collections.singletonMap("active",
				UUID.fromString("0c89ebff-037f-15d6-ffff-a1b98ee6c71d")), control.getStates());
		assertEquals("Type", ControlType.Switch, control.getType());
	}

	@Test
	public void jsonDeserializeWithNativeUUID() throws IOException {
		InputStream in = getClass().getResourceAsStream("control-03.json");
		Control control = objectMapper.readValue(in, Control.class);
		assertNotNull(control);
		assertEquals("UUID", UUID.fromString("0c89ebff-037f-15d6-ffff-a1b98ee6c71d"), control.getUuid());
		assertEquals("Name", "Generator status", control.getName());
		assertEquals("Default rating", Integer.valueOf(0), control.getDefaultRating());
		assertEquals("Category", UUID.fromString("0c89ebac-0031-030a-ffff-a1b98ee6c71d"),
				control.getCategory());
		assertEquals("Room", UUID.fromString("0c89ebac-0041-0390-ffff-a1b98ee6c71d"), control.getRoom());
		assertEquals("States", Collections.singletonMap("active",
				UUID.fromString("0c89ebff-037f-15d6-ffff-a1b98ee6c71d")), control.getStates());
		assertEquals("Type", ControlType.Switch, control.getType());
	}

	@Test
	public void jsonSerialize() throws IOException {
		InputStream in = getClass().getResourceAsStream("control-01.json");
		Control control = objectMapper.readValue(in, Control.class);
		assertNotNull(control);

		// write out to JSON
		String json = objectMapper.writeValueAsString(control);

		// read back as tree
		JsonNode tree = objectMapper.readTree(json);
		assertEquals("UUID", "0c89ebff-037f-15d6-ffffa1b98ee6c71d", tree.path("uuid").textValue());
		assertNull("uuidAction", tree.get("uuidAction"));
		assertEquals("Name", "Generator status", tree.path("name").textValue());
		assertEquals("Default rating", 0, tree.path("defaultRating").intValue());
		assertEquals("Category", "0c89ebac-0031-030a-ffffa1b98ee6c71d", tree.path("cat").textValue());
		assertEquals("Room", "0c89ebac-0041-0390-ffffa1b98ee6c71d", tree.path("room").textValue());
		assertEquals("States", "0c89ebff-037f-15d6-ffffa1b98ee6c71d",
				tree.path("states").path("active").textValue());
		assertEquals("Type", "Switch", tree.path("type").textValue());
	}

	@Test
	public void jsonDeserializeWithDetails() throws IOException {
		InputStream in = getClass().getResourceAsStream("control-02.json");
		Control control = objectMapper.readValue(in, Control.class);
		assertNotNull(control);
		assertNotNull("Details", control.getDetails());
		JsonNode tree = objectMapper.readTree(control.getDetails());
		assertNotNull("Detail json", tree);
		assertTrue("Detail json object", tree.isObject());
		assertEquals("Color on", "#73BA1B", tree.path("color").path("on").textValue());
		assertEquals("Color on", "On", tree.path("text").path("on").textValue());
	}

}
