/* ==================================================================
 * LoxoneHomeController.java - 22/09/2016 12:45:41 PM
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

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import net.solarnetwork.node.loxone.LoxoneService;
import net.solarnetwork.node.loxone.domain.Config;
import net.solarnetwork.web.domain.Response;

/**
 * Entry point for Loxone app.
 * 
 * @author matt
 * @version 1.0
 */
@Controller
@RequestMapping("/a/loxone")
public class LoxoneHomeController extends BaseLoxoneWebServiceController {

	@RequestMapping({ "", "/" })
	public String home(Model model) {
		StringBuilder allIds = new StringBuilder();
		if ( loxoneServices != null ) {
			for ( LoxoneService service : loxoneServices ) {
				Config config = service.getConfiguration();
				if ( config == null || config.getId() == null ) {
					continue;
				}
				if ( allIds.length() > 0 ) {
					allIds.append(',');
				}
				allIds.append(config.idToExternalForm());
			}
			model.addAttribute("configIds", allIds.toString());
		}
		return "a/loxone/home";
	}

	@RequestMapping("/{configId}")
	public String home(@PathVariable("configId") String configId, Model model) {
		model.addAttribute("configId", configId);
		return home(model);
	}

	@RequestMapping("/ping")
	@ResponseBody
	public Response<Object> ping() {
		return Response.response(null);
	}

}
