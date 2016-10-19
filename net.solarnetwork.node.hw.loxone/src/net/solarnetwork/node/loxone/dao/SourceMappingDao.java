/* ==================================================================
 * SourceMappingDao.java - 12/10/2016 4:08:10 PM
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

package net.solarnetwork.node.loxone.dao;

import java.util.UUID;
import net.solarnetwork.node.loxone.domain.SourceMapping;

/**
 * DAO API for {@link SourceMapping} entities.
 * 
 * @author matt
 * @version 1.0
 * @since 0.2
 */
public interface SourceMappingDao extends ConfigurationEntityDao<SourceMapping> {

	/**
	 * Delete an entity.
	 * 
	 * @param configId
	 *        The config ID to match.
	 * @param uuid
	 *        The UUID of the entity to remove.
	 */
	int delete(Long configId, UUID uuid);

}
