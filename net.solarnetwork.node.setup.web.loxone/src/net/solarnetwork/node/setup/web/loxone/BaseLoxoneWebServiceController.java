/* ==================================================================
 * BaseLoxoneWebServiceController.java - 21/09/2016 4:20:43 PM
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

import java.util.List;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import net.solarnetwork.node.loxone.LoxoneService;
import net.solarnetwork.node.loxone.domain.Config;
import net.solarnetwork.web.domain.Response;

/**
 * Base class for web service support.
 * 
 * @author matt
 * @version 1.0
 */
public class BaseLoxoneWebServiceController {

	/** A class-level logger. */
	protected final Logger log = LoggerFactory.getLogger(getClass());

	@Resource
	protected List<LoxoneService> loxoneServices;

	/**
	 * Get a specific {@link LoxoneService} based on its UID.
	 * 
	 * @param configId
	 *        The configuration ID of the service to get.
	 * @return the service
	 */
	protected LoxoneService serviceForConfigId(String configId) {
		if ( loxoneServices == null ) {
			return null;
		}
		for ( LoxoneService service : loxoneServices ) {
			Config config = service.getConfiguration();
			if ( config != null && configId.equalsIgnoreCase(config.idToExternalForm()) ) {
				return service;
			}
		}
		return null;
	}

	/**
	 * Handle an {@link AuthenticationException}.
	 * 
	 * @param e
	 *        the exception
	 * @param response
	 *        the response
	 * @return an error response object
	 */
	@ExceptionHandler(AuthenticationException.class)
	@ResponseBody
	public Response<?> handleAuthorizationException(AuthenticationException e,
			HttpServletResponse response) {
		log.debug("AuthenticationException in {} controller: {}", getClass().getSimpleName(),
				e.getMessage());
		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		return new Response<Object>(Boolean.FALSE, null, e.getMessage(), null);
	}

	/**
	 * Handle an {@link IllegalArgumentException}.
	 * 
	 * @param e
	 *        the exception
	 * @param response
	 *        the response
	 * @return an error response object
	 */
	@ExceptionHandler(IllegalArgumentException.class)
	@ResponseBody
	public Response<?> handleIllegalArgument(IllegalArgumentException e, HttpServletResponse response) {
		log.error("IllegalArgumentException in {} controller: {}", getClass().getSimpleName(),
				e.getMessage());
		response.setStatus(422);
		return new Response<Object>(Boolean.FALSE, null, "Illegal argument: " + e.getMessage(), null);
	}

	/**
	 * Handle an {@link HttpMessageNotReadableException}.
	 * 
	 * @param e
	 *        the exception
	 * @param response
	 *        the response
	 * @return an error response object
	 */
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public Response<?> handleMalformedMessage(HttpMessageNotReadableException e,
			HttpServletResponse response) {
		log.error("HttpMessageNotReadableException in {} controller: {}", getClass().getSimpleName(),
				e.getMessage());
		response.setStatus(422);
		return new Response<Object>(Boolean.FALSE, null, "Illegal request: " + e.getMessage(), null);
	}

	/**
	 * Handle a {@link RuntimeException}.
	 * 
	 * @param e
	 *        the exception
	 * @param response
	 *        the response
	 * @return an error response object
	 */
	@ExceptionHandler(RuntimeException.class)
	@ResponseBody
	public Response<?> handleRuntimeException(RuntimeException e, HttpServletResponse response) {
		log.error("RuntimeException in {} controller", getClass().getSimpleName(), e);
		response.setStatus(500);
		return new Response<Object>(Boolean.FALSE, null, "Internal error", null);
	}

	public void setLoxoneServices(List<LoxoneService> loxoneServices) {
		this.loxoneServices = loxoneServices;
	}

}
