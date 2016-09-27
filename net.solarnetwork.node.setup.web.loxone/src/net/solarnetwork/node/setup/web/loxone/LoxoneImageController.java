/* ==================================================================
 * LoxoneImageController.java - 23/09/2016 8:45:55 PM
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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import net.solarnetwork.node.loxone.LoxoneService;

/**
 * Controller for serving up Loxone images.
 * 
 * @author matt
 * @version 1.0
 */
@Controller
@RequestMapping("/a/loxone/{configId}/image")
public class LoxoneImageController extends BaseLoxoneWebServiceController {

	@RequestMapping("/{name:.+}")
	@ResponseBody
	public ResponseEntity<Resource> getImage(@PathVariable("configId") Long configId,
			@PathVariable("name") String name) {
		LoxoneService service = serviceForConfigId(configId);
		if ( service == null ) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		final Future<Resource> future = service.getImage(name);
		try {
			final Resource r = future.get(30, TimeUnit.SECONDS);
			return new ResponseEntity<>(r, HttpStatus.OK);
		} catch ( InterruptedException e ) {
			return new ResponseEntity<>(HttpStatus.GATEWAY_TIMEOUT);
		} catch ( ExecutionException e ) {
			return new ResponseEntity<>(HttpStatus.BAD_GATEWAY);
		} catch ( TimeoutException e ) {
			return new ResponseEntity<>(HttpStatus.GATEWAY_TIMEOUT);
		}
	}

}
