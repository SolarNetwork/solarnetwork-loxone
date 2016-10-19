/* ==================================================================
 * LoxoneSourceMappingParser.java - 12/10/2016 9:00:12 PM
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

package net.solarnetwork.node.loxone;

import java.io.IOException;
import java.io.InputStream;
import net.solarnetwork.node.loxone.domain.SourceMapping;

/**
 * API for parsing source mapping data.
 * 
 * @author matt
 * @version 1.0
 * @since 0.2
 */
public interface LoxoneSourceMappingParser {

	/**
	 * Callback API.
	 */
	interface SourceMappingCallback {

		/**
		 * Handle a parsed source mapping.
		 * 
		 * @param mapping
		 *        The parsed source mapping.
		 */
		void parsedSourceMapping(SourceMapping mapping);
	}

	/**
	 * Parse an input stream with a callback handler.
	 * 
	 * @param in
	 *        The input stream to parse.
	 * @param callback
	 *        A callback handler.
	 * @throws IOException
	 *         If any IO error occurs.
	 */
	void parseInputStream(InputStream in, SourceMappingCallback callback) throws IOException;

}
