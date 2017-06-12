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

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Future;
import org.springframework.core.io.Resource;
import net.solarnetwork.domain.SortDescriptor;
import net.solarnetwork.node.Identifiable;
import net.solarnetwork.node.loxone.domain.Config;
import net.solarnetwork.node.loxone.domain.ConfigurationEntity;
import net.solarnetwork.node.loxone.domain.EventEntity;
import net.solarnetwork.node.loxone.domain.SourceMapping;
import net.solarnetwork.node.loxone.domain.UUIDEntityParameters;
import net.solarnetwork.node.loxone.domain.UUIDSetEntity;
import net.solarnetwork.node.loxone.domain.ValueEvent;

/**
 * API for a Loxone device.
 * 
 * @author matt
 * @version 1.2
 */
public interface LoxoneService extends Identifiable {

	/**
	 * Get the configuration associated with this service.
	 * 
	 * @return The configuration, or <em>null</em> if not known.
	 */
	Config getConfiguration();

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
	 * Get all available event entities of a specific type.
	 * 
	 * @param type
	 *        The type of events to get.
	 * @param sortDescriptors
	 *        The optional sort descriptors. If not provided, a default sort
	 *        will be used.
	 * @return The event entities.
	 */
	<T extends EventEntity> Collection<T> getAllEvents(Class<T> type,
			List<SortDescriptor> sortDescriptors);

	/**
	 * Asynchronously get an image based on its name.
	 * 
	 * @param name
	 *        The name of the image to get.
	 * @return A future {@link Resource} for the image.
	 */
	Future<Resource> getImage(String name);

	/**
	 * Get all available UUID values of a specific {@code UUIDEntity} set type,
	 * and their associated parameters.
	 * 
	 * @param type
	 *        The type of UUID to get.
	 * @param sortDescriptors
	 *        The optional sort descriptors. If not provided, a default sort
	 *        will be used.
	 * @return The UUIDs.
	 */
	<T extends UUIDSetEntity<P>, P extends UUIDEntityParameters> Map<UUID, P> getUUIDSet(Class<T> type,
			List<SortDescriptor> sortDescriptors);

	/**
	 * Update a "UUID set" associated with a given {@code UUIDEntity}.
	 * 
	 * If a UUID exists in both {@code add} and {@code remove}, it will be
	 * removed.
	 * 
	 * @param type
	 *        The set type to update.
	 * @param add
	 *        An optional set of UUIDs that should be added to the UUID set.
	 * @param remove
	 *        An optional set of UUIDs that should be removed from the UUID set.
	 * @param parameters
	 *        An optional map of UUID to parameter objects to apply to any UUID
	 *        value.
	 */
	<T extends UUIDSetEntity<P>, P extends UUIDEntityParameters> void updateUUIDSet(Class<T> type,
			Collection<UUID> add, Collection<UUID> remove, Map<UUID, P> parameters);

	/**
	 * Add or remove source mappings.
	 * 
	 * @param store
	 *        A collection of source mappings to update or add.
	 * @param remove
	 *        A collection of source mapping UUIDs to remove.
	 * @since 1.1
	 */
	void updateSourceMappings(Collection<SourceMapping> store, Collection<UUID> remove);

	/**
	 * Import source mappings using a parser.
	 * 
	 * @param in
	 *        The input stream of the data.
	 * @param parser
	 *        The parser to use.
	 * @throws IOException
	 *         If parsing has an IO error.
	 * @since 1.1
	 */
	void importSourceMappings(InputStream in, LoxoneSourceMappingParser parser) throws IOException;

	/**
	 * Asynchronously set the state of a control.
	 * 
	 * @param name
	 *        the name of the control to set the state of
	 * @param state
	 *        the control value to set
	 * @return the resulting control state
	 * @since 1.2
	 */
	Future<List<ValueEvent>> setControlState(UUID uuid, String state);

}
