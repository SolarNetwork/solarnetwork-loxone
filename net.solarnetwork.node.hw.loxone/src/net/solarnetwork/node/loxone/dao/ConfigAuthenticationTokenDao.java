/* ==================================================================
 * ConfigAuthenticationTokenDao.java - 6/04/2018 8:32:51 AM
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

package net.solarnetwork.node.loxone.dao;

import net.solarnetwork.node.loxone.domain.ConfigAuthenticationToken;

/**
 * DAO API for {@link ConfigAuthenticationToken} data.
 * 
 * <p>
 * This DAO assumes just one token is allowed per Config ID.
 * </p>
 * 
 * @author matt
 * @version 1.0
 * @since 1.3
 */
public interface ConfigAuthenticationTokenDao {

	/**
	 * Store (create or update) a token.
	 * 
	 * @param token
	 *        the token to store
	 */
	void storeConfigAuthenticationToken(ConfigAuthenticationToken token);

	/**
	 * Delete the token for a Config ID.
	 * 
	 * @param id
	 *        The Config ID of the token to delete.
	 */
	void deleteConfigAuthenticationToken(Long configId);

	/**
	 * Get a token.
	 * 
	 * @param id
	 *        The Config ID of the token to get.
	 * @return the token, or {@literal null} if not available
	 */
	ConfigAuthenticationToken getConfigAuthenticationToken(Long id);

}
