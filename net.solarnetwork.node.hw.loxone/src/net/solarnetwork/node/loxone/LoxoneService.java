/* ==================================================================
 * LoxoneService.java - 21/09/2016 4:26:33 PM
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

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;
import org.springframework.core.io.Resource;
import net.solarnetwork.domain.SortDescriptor;
import net.solarnetwork.node.Identifiable;
import net.solarnetwork.node.loxone.domain.ConfigurationEntity;

/**
 * API for a Loxone device.
 * 
 * @author matt
 * @version 1.0
 */
public interface LoxoneService extends Identifiable {

	/**
	 * Get the configuration ID associated with this service.
	 * 
	 * @return The configuration ID, or <em>null</em> if not known.
	 */
	public Long getConfigurationId();

	/**
	 * Get all available configuration entities of a specific type.
	 * 
	 * @param type
	 *        The type of configuration to get.
	 * @param sortDescriptors
	 *        The optional sort descriptors. If not provided, a default sort
	 *        will be used.
	 * @return The configuration entities.
	 */
	<T extends ConfigurationEntity> Collection<T> getAllConfiguration(Class<T> type,
			List<SortDescriptor> sortDescriptors);

	/**
	 * Asynchronously get an image based on its name.
	 * 
	 * @param name
	 *        The name of the image to get.
	 * @return A future {@link Resource} for the image.
	 */
	Future<Resource> getImage(String name);

}
