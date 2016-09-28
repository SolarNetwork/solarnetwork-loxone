/* ==================================================================
 * WebsocketEventController.java - 24/09/2016 6:16:03 PM
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

import java.security.Principal;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;
import net.solarnetwork.node.loxone.LoxoneService;
import net.solarnetwork.node.loxone.domain.ValueEvent;
import net.solarnetwork.web.domain.Response;

/**
 * Controller for websocket event publishing.
 * 
 * @author matt
 * @version 1.0
 */
@Controller
public class WebsocketEventController extends BaseLoxoneWebServiceController {

	private final Logger log = LoggerFactory.getLogger(getClass());

	public WebsocketEventController() {
		super();
	}

	@MessageMapping("/test")
	public String testMessage(String msg) {
		return "Hello, " + msg;
	}

	@SubscribeMapping("/{configId}/events/values")
	public Response<Collection<ValueEvent>> subscribeToValueEvents(@DestinationVariable String configId,
			Principal principal) {
		log.info("Subscribing {} to {} value evnets", principal, configId);
		LoxoneService service = serviceForConfigId(configId);
		if ( service == null ) {
			return new Response<>(false, "404", "Configuration ID not available.", null);
		}
		Collection<ValueEvent> results = service.getAllEvents(ValueEvent.class, null);
		return Response.response(results);
	}

}
