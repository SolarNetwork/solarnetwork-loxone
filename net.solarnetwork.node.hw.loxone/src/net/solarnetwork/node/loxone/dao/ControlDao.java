/* ==================================================================
 * ControlDao.java - 18/09/2016 1:19:21 PM
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

import java.util.List;
import java.util.UUID;
import net.solarnetwork.node.loxone.domain.Control;
import net.solarnetwork.node.loxone.domain.ControlDatumParameters;
import net.solarnetwork.node.loxone.domain.DatumPropertyUUIDEntity;
import net.solarnetwork.node.loxone.domain.UUIDEntityParametersPair;

/**
 * DAO API of {@link Control} entities.
 * 
 * @author matt
 * @version 1.2
 */
public interface ControlDao extends ConfigurationEntityDao<Control> {

	/**
	 * Get a list of persisted entities associated with
	 * {@link DatumPropertyUUIDEntity}.
	 * 
	 * @param configId
	 *        The config ID to get the count for.
	 * @return list of all persisted entities that have associated
	 *         {@link DatumPropertyUUIDEntity} values, or empty list if none
	 *         available
	 */
	List<UUIDEntityParametersPair<Control, ControlDatumParameters>> findAllForDatumPropertyUUIDEntities(
			Long configId);

	/**
	 * Get a count of persisted controls.
	 * 
	 * @param configId
	 *        The config ID to get the count for.
	 * @return count of available controls
	 * @since 1.1
	 */
	int countForConfig(Long configId);

	/**
	 * Get a control with a state matching a given UUID.
	 * 
	 * @param configId
	 *        The config ID to the the control for.
	 * @param stateUuid
	 *        The UUID of the state to get the control for.
	 * @return the control, or {@literal null} if not available
	 * @since 1.2
	 */
	Control getForConfigAndState(Long configId, UUID stateUuid);
}
