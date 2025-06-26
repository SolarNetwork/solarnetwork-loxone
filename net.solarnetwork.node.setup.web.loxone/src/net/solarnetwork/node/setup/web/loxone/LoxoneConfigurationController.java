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

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;
import net.solarnetwork.domain.SortDescriptor;
import net.solarnetwork.node.loxone.LoxoneService;
import net.solarnetwork.node.loxone.LoxoneSourceMappingParser;
import net.solarnetwork.node.loxone.LoxoneXMLSourceMappingParser;
import net.solarnetwork.node.loxone.domain.Category;
import net.solarnetwork.node.loxone.domain.ConfigurationEntity;
import net.solarnetwork.node.loxone.domain.Control;
import net.solarnetwork.node.loxone.domain.Room;
import net.solarnetwork.node.loxone.domain.SourceMapping;
import net.solarnetwork.web.jakarta.domain.Response;

/**
 * Controller for Loxone configuration data.
 * 
 * @author matt
 * @version 1.1
 */
@RestController
@RequestMapping("/a/loxone/{configId}")
public class LoxoneConfigurationController extends BaseLoxoneWebServiceController {

	public LoxoneConfigurationController() {
		super();
	}

	@RequestMapping(value = "/categories", method = RequestMethod.GET)
	public Response<Collection<Category>> allCategories(@PathVariable("configId") String configId,
			WebRequest webRequest) {
		return getAllForConfigId(webRequest, configId, Category.class, null);
	}

	@RequestMapping(value = "/controls", method = RequestMethod.GET)
	public Response<Collection<Control>> allControls(@PathVariable("configId") String configId,
			WebRequest webRequest) {
		return getAllForConfigId(webRequest, configId, Control.class, null);
	}

	@RequestMapping(value = "/rooms", method = RequestMethod.GET)
	public Response<Collection<Room>> allRooms(@PathVariable("configId") String configId,
			WebRequest webRequest) {
		return getAllForConfigId(webRequest, configId, Room.class, null);
	}

	/**
	 * Get all available source mappings.
	 * 
	 * @param configId
	 *        The config ID.
	 * @param webRequest
	 *        The request.
	 * @return The mappings.
	 * @since 1.1
	 */
	@RequestMapping(value = "/sources", method = RequestMethod.GET)
	public Response<Collection<SourceMapping>> allSources(@PathVariable("configId") String configId,
			WebRequest webRequest) {
		return getAllForConfigId(webRequest, configId, SourceMapping.class, null);
	}

	/**
	 * Update source mappings.
	 * 
	 * @param configId
	 *        The config ID.
	 * @param patchSet
	 *        The mappings to add, update, or remove.
	 * @return A success indicator.
	 */
	@RequestMapping(value = "/sources", method = RequestMethod.PATCH)
	public Response<Object> updateSources(@PathVariable("configId") String configId,
			@RequestBody SourceMappingPatchSet patchSet) {
		LoxoneService service = serviceForConfigId(configId);
		if ( service == null ) {
			return new Response<Object>(Boolean.FALSE, "404", "Service not available", null);
		}
		service.updateSourceMappings(patchSet.getStore(), patchSet.getRemove());
		return Response.response(null);
	}

	/**
	 * Import a source mapping file via a {@code multipart/form-data} file
	 * upload.
	 * 
	 * @param configId
	 *        The config ID.
	 * @param file
	 *        The file to upload.
	 * @return A success indicator.
	 */
	@RequestMapping(value = "/sources", method = RequestMethod.POST, consumes = "multipart/form-data")
	public Response<Object> importSources(@PathVariable("configId") String configId,
			@RequestPart MultipartFile file) {
		LoxoneService service = serviceForConfigId(configId);
		if ( service == null ) {
			return new Response<Object>(Boolean.FALSE, "404", "Service not available", null);
		}
		LoxoneSourceMappingParser parser = new LoxoneXMLSourceMappingParser();
		try {
			service.importSourceMappings(file.getInputStream(), parser);
			return Response.response(null);
		} catch ( IOException e ) {
			return new Response<Object>(Boolean.FALSE, "500", "IO error: " + e.getMessage(), null);
		}
	}

	private <T extends ConfigurationEntity> Response<Collection<T>> getAllForConfigId(
			WebRequest webRequest, String configId, Class<T> type,
			List<SortDescriptor> sortDescriptors) {
		LoxoneService service = serviceForConfigId(configId);
		if ( service == null ) {
			return new Response<Collection<T>>(Boolean.FALSE, "404", "Service not available", null);
		}
		Date lastModified = service.getConfiguration().getLastModified();
		if ( lastModified != null && webRequest.checkNotModified(lastModified.getTime()) ) {
			return null;
		}
		Collection<T> result = service.getAllConfiguration(type, null);
		return Response.response(result);
	}

}
