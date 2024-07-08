/* ==================================================================
 * Category.java - 18/09/2016 6:23:00 AM
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
 * Logical grouping of controls.
 *
 * @author matt
 * @version 1.0
 */
public class Category extends BaseConfigurationEntity {

	private String color;
	private CategoryType type;
	private String image;

	/**
	 * Constructor.
	 */
	public Category() {
		super();
	}

	/**
	 * Get the color.
	 *
	 * @return the color
	 */
	public String getColor() {
		return color;
	}

	/**
	 * Set the color.
	 *
	 * @param color
	 *        the color to set
	 */
	public void setColor(String color) {
		this.color = color;
	}

	/**
	 * Get the category type.
	 *
	 * @return the type
	 */
	public CategoryType getType() {
		return type;
	}

	/**
	 * Set the category type.
	 *
	 * @param type
	 *        the type
	 */
	public void setType(CategoryType type) {
		this.type = type;
	}

	/**
	 * Get the image.
	 *
	 * @return the image
	 */
	public String getImage() {
		return image;
	}

	/**
	 * Set the image.
	 *
	 * @param image
	 *        the image to set
	 */
	public void setImage(String image) {
		this.image = image;
	}

}
