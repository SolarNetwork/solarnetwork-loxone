/* ==================================================================
 * ConfigTests.java - 14/10/2016 5:47:25 PM
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

import org.junit.Assert;
import org.junit.Test;
import net.solarnetwork.node.loxone.domain.Config;

/**
 * Test cases for the {@link Config} class.
 * 
 * @author matt
 * @version 1.0
 */
public class ConfigTests {

	// Long.parseUnsignedLong(Hex.encodeHexString(friendly.getBytes("US-ASCII")), 16);
	private static final Long HELLO_ID = 0x48656C6C6FL;

	private static final Long TEST_ID = 0x5FDFFAF32FF2BCFFL;
	private static final String TEST_ID_EXTERNAL = "5fdffaf32ff2bcff";

	@Test
	public void idToExternalFormFriendly() throws Exception {
		String friendly = "Hello";
		String result = Config.idToExternalForm(HELLO_ID);
		Assert.assertEquals("Friendly config ID", friendly, result);
	}

	@Test
	public void externalFormToIdFriendly() throws Exception {
		Long result = Config.idFromExternalForm("Hello");
		Assert.assertEquals("Parsed friendly ID", HELLO_ID, result);
	}

	@Test
	public void idToExternalForm() throws Exception {
		String result = Config.idToExternalForm(TEST_ID);
		Assert.assertEquals("Config ID", TEST_ID_EXTERNAL, result);
	}

	@Test
	public void externalFormToId() throws Exception {
		Long result = Config.idFromExternalForm(TEST_ID_EXTERNAL);
		Assert.assertEquals("Parsed ID", TEST_ID, result);
	}

}
