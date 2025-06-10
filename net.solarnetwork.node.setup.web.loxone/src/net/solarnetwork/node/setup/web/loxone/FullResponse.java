/* ==================================================================
 * FullResponse.java - 17/04/2018 3:18:47 PM
 * 
 * Copyright 2018 SolarNetwork.net Dev Team
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

import com.fasterxml.jackson.annotation.JsonInclude;
import net.solarnetwork.web.jakarta.domain.Response;

/**
 * Extension of {@link Response} so that {@literal null} values are always
 * included, even in returned {@code Map} objects with {@literal null} values.
 * 
 * @param <T>
 *        the response value type
 * @author matt
 * @version 1.0
 * @since 1.1.1
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
public class FullResponse<T> extends Response<T> {

	/**
	 * Default constructor.
	 */
	public FullResponse() {
		super();
	}

	/**
	 * Construct with data.
	 * 
	 * @param data
	 *        the data
	 */
	public FullResponse(T data) {
		super(data);
	}

	/**
	 * Constructor.
	 * 
	 * @param success
	 *        flag of success
	 * @param code
	 *        optional code, e.g. error code
	 * @param message
	 *        optional descriptive message
	 * @param data
	 *        optional data in the response
	 */
	public FullResponse(Boolean success, String code, String message, T data) {
		super(success, code, message, data);
	}

	@JsonInclude(JsonInclude.Include.ALWAYS)
	@Override
	public T getData() {
		return super.getData();
	}

	/**
	 * Helper method to construct instance using generic return type inference.
	 * 
	 * <p>
	 * If you import this static method, then in your code you can write
	 * {@code return response(myData)} instead of
	 * {@code new Response&lt;Object&gt;(myData)}.
	 * </p>
	 * 
	 * @param <V>
	 *        the response value type
	 * @param data
	 *        the data
	 * @return the response
	 */
	public static <V> Response<V> response(V data) {
		return new FullResponse<V>(data);
	}
}
