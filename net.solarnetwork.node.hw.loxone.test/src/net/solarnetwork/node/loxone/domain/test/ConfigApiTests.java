/* ==================================================================
 * ConfigApiTests.java - 4/04/2018 6:41:13 PM
 * 
 * Copyright 2018 SolarNetwork.net Dev Team
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

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import net.solarnetwork.node.loxone.domain.ConfigApi;

/**
 * Test cases for the {@link ConfigApi} class.
 * 
 * @author matt
 * @version 1.0
 */
public class ConfigApiTests {

	@Test
	public void majorVersionEqual() {
		ConfigApi config = new ConfigApi(null, null, "9.1.10.30");
		assertThat("Version at least", config.isVersionAtLeast(9), equalTo(true));
	}

	@Test
	public void majorVersionGreater() {
		ConfigApi config = new ConfigApi(null, null, "9.1.10.30");
		assertThat("Version at least", config.isVersionAtLeast(8), equalTo(true));
	}

	@Test
	public void majorVersionLess() {
		ConfigApi config = new ConfigApi(null, null, "9.1.10.30");
		assertThat("Version at least", config.isVersionAtLeast(10), equalTo(false));
	}

	@Test
	public void minorVersionEqual() {
		ConfigApi config = new ConfigApi(null, null, "9.1.10.30");
		assertThat("Version at least", config.isVersionAtLeast(9, 1), equalTo(true));
	}

	@Test
	public void minorVersionGreater() {
		ConfigApi config = new ConfigApi(null, null, "9.1.10.30");
		assertThat("Version at least", config.isVersionAtLeast(9, 0), equalTo(true));
	}

	@Test
	public void minorVersionLess() {
		ConfigApi config = new ConfigApi(null, null, "9.1.10.30");
		assertThat("Version at least", config.isVersionAtLeast(9, 2), equalTo(false));
	}

}
