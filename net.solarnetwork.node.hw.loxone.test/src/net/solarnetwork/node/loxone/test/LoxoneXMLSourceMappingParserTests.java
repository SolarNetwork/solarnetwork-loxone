/* ==================================================================
 * LoxoneXMLSourceMappingParserTests.java - 12/10/2016 9:07:35 PM
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

package net.solarnetwork.node.loxone.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;
import net.solarnetwork.node.loxone.LoxoneXMLSourceMappingParser;
import net.solarnetwork.node.loxone.LoxoneSourceMappingParser.SourceMappingCallback;
import net.solarnetwork.node.loxone.domain.SourceMapping;

/**
 * Test cases for the {@link LoxoneXMLSourceMappingParser} class.
 * 
 * @author matt
 * @version 1.0
 */
public class LoxoneXMLSourceMappingParserTests {

	private LoxoneXMLSourceMappingParser parser;

	private int count = 0;

	@Before
	public void setup() {
		parser = new LoxoneXMLSourceMappingParser();
		count = 0;
	}

	@Test
	public void parse() throws Exception {
		parser.parseInputStream(getClass().getResourceAsStream("program-file-01.xml"),
				new SourceMappingCallback() {

					@Override
					public void parsedSourceMapping(SourceMapping mapping) {
						assertNotNull("SourceMapping object", mapping);
						assertNotNull("UUID", mapping.getUuid());
						assertNotNull("Source ID", mapping.getSourceId());
						count++;
					}
				});
		assertEquals("Parsed source mapping count", 447, count);
	}

}
