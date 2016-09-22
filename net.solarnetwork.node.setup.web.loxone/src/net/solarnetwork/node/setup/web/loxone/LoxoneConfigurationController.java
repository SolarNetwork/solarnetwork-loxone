/* ==================================================================
 * LoxoneConfigurationController.java - 21/09/2016 4:24:07 PM
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

package net.solarnetwork.node.setup.web.loxone;

import java.util.Collection;
import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import net.solarnetwork.domain.SortDescriptor;
import net.solarnetwork.node.loxone.LoxoneService;
import net.solarnetwork.node.loxone.domain.Category;
import net.solarnetwork.node.loxone.domain.ConfigurationEntity;
import net.solarnetwork.node.loxone.domain.Control;
import net.solarnetwork.node.loxone.domain.Room;
import net.solarnetwork.web.domain.Response;

/**
 * Controller for Loxone configuration data.
 * 
 * @author matt
 * @version 1.0
 */
@RestController
@RequestMapping("/a/loxone/{configId}")
public class LoxoneConfigurationController extends BaseLoxoneWebServiceController {

	public LoxoneConfigurationController() {
		super();
	}

	@RequestMapping(value = "/categories", method = RequestMethod.GET)
	public Response<Collection<Category>> allCategories(@PathVariable("configId") Long configId) {
		return getAllForConfigId(configId, Category.class, null);
	}

	@RequestMapping(value = "/controls", method = RequestMethod.GET)
	public Response<Collection<Control>> allControls(@PathVariable("configId") Long configId) {
		return getAllForConfigId(configId, Control.class, null);
	}

	@RequestMapping(value = "/rooms", method = RequestMethod.GET)
	public Response<Collection<Room>> allRooms(@PathVariable("configId") Long configId) {
		return getAllForConfigId(configId, Room.class, null);
	}

	private <T extends ConfigurationEntity> Response<Collection<T>> getAllForConfigId(Long configId,
			Class<T> type, List<SortDescriptor> sortDescriptors) {
		LoxoneService service = serviceForConfigId(configId);
		if ( service == null ) {
			return new Response<Collection<T>>(Boolean.FALSE, "404", "Service not available", null);
		}
		Collection<T> result = service.getAllConfiguration(type, null);
		return Response.response(result);
	}

}
