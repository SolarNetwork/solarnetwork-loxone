/* ==================================================================
 * UUIDEntityParametersPair.java - 2/10/2016 11:18:36 AM
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

package net.solarnetwork.node.loxone.domain;

/**
 * A pairing of an entity with associated parameters.
 * 
 * @author matt
 * @version 1.0
 */
public class UUIDEntityParametersPair<T extends UUIDEntity, P extends UUIDEntityParameters> {

	final T entity;
	final P parameters;

	public UUIDEntityParametersPair(T entity, P parameters) {
		super();
		this.entity = entity;
		this.parameters = parameters;
	}

	public T getEntity() {
		return entity;
	}

	public P getParameters() {
		return parameters;
	}

}
