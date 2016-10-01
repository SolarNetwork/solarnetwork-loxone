/* ==================================================================
 * DatumUUIDPatchSetTests.java - 1/10/2016 3:48:43 PM
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

package net.solarnetwork.node.setup.web.loxone.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.solarnetwork.node.loxone.domain.UUIDDeserializer;
import net.solarnetwork.node.setup.web.loxone.DatumUUIDPatchSet;
import net.solarnetwork.node.setup.web.loxone.LoxoneUUIDSetController;
import net.solarnetwork.node.test.AbstractNodeTest;
import net.solarnetwork.util.ObjectMapperFactoryBean;

/**
 * Test cases for the {@link LoxoneUUIDSetController} class.
 * 
 * @author matt
 * @version 1.0
 */
public class DatumUUIDPatchSetTests extends AbstractNodeTest {

	private ObjectMapper objectMapper;

	@Before
	public void setup() throws Exception {
		ObjectMapperFactoryBean factory = new ObjectMapperFactoryBean();
		factory.setDeserializers(Arrays.asList(new UUIDDeserializer()));
		objectMapper = factory.getObject();
	}

	@Test
	public void parseUpdateDatumUUIDSetJSON() throws IOException {
		DatumUUIDPatchSet patch = objectMapper.readValue(
				getClass().getResourceAsStream("update-datum-uuid-set-01.json"),
				DatumUUIDPatchSet.class);
		assertNotNull("Parsed object", patch);
		assertNotNull("Add list", patch.getAdd());
		assertNotNull("Remove list", patch.getRemove());
		assertNotNull("Parameters map", patch.getParameters());
		assertEquals("Add UUID",
				Collections.singletonList(UUID.fromString("0d717dd5-02f4-a7bc-ffff-a1b98ee6c71d")),
				patch.getAdd());
		assertEquals("Remove UUID",
				Collections.singletonList(UUID.fromString("0d717dd5-02f4-a7bc-ffff-a1b98ee6c71e")),
				patch.getRemove());
		assertEquals("Parameters key", UUID.fromString("0d717dd5-02f4-a7bc-ffff-a1b98ee6c71f"),
				patch.getParameters().keySet().iterator().next());
		assertEquals("Parameters saveFrequencySeconds", 500,
				patch.getParameters().values().iterator().next().getSaveFrequencySeconds());
	}

}
