/* ==================================================================
 * CommandTypeTests.java - 19/09/2016 12:57:25 PM
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

package net.solarnetwork.node.loxone.protocol.ws.test;

import org.junit.Assert;
import org.junit.Test;
import net.solarnetwork.node.loxone.protocol.ws.CommandType;

/**
 * Unit tests for {@link CommandType}.
 * 
 * @author matt
 * @version 1.0
 */
public class CommandTypeTests {

	@Test
	public void structureFileLastModDateParse() {
		// the request command is jdev/sps/LoxAPPversion3 but the response is dev/sps/LoxAPPversion3!
		CommandType result = CommandType.forControlValue("dev/sps/LoxAPPversion3");
		Assert.assertEquals(CommandType.StructureFileLastModifiedDate, result);
	}

}
