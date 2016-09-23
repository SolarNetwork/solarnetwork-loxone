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

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
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
	public Callable<ResponseEntity<StreamingResponseBody>> getImage(
			@PathVariable("configId") Long configId, @PathVariable("name") String name) {
		LoxoneService service = serviceForConfigId(configId);
		if ( service == null ) {
			return new Callable<ResponseEntity<StreamingResponseBody>>() {

				@Override
				public ResponseEntity<StreamingResponseBody> call() throws Exception {
					return new ResponseEntity<>(HttpStatus.NOT_FOUND);
				}
			};
		}
		final Future<Resource> future = service.getImage(name);
		return new Callable<ResponseEntity<StreamingResponseBody>>() {

			@Override
			public ResponseEntity<StreamingResponseBody> call() throws Exception {
				final Resource r = future.get();
				HttpHeaders headers = new HttpHeaders();
				headers.setContentLength(r.contentLength());
				ResponseEntity<StreamingResponseBody> result = new ResponseEntity<StreamingResponseBody>(
						new StreamingResponseBody() {

							@Override
							public void writeTo(OutputStream out) throws IOException {
								FileCopyUtils.copy(r.getInputStream(), out);
							}
						}, headers, HttpStatus.OK);
				return result;
			}
		};
	}

}
