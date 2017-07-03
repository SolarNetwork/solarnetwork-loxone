/* ==================================================================
 * JdbcCategoryDao.java - 18/09/2016 7:47:48 AM
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

package net.solarnetwork.node.loxone.dao.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import net.solarnetwork.node.loxone.dao.CategoryDao;
import net.solarnetwork.node.loxone.domain.Category;
import net.solarnetwork.node.loxone.domain.CategoryType;

/**
 * JDBC implementation of {@link CategoryDao}.
 * 
 * @author matt
 * @version 1.1
 */
public class JdbcCategoryDao extends BaseConfigurationEntityDao<Category> implements CategoryDao {

	/** The default tables version. */
	public static final int TABLES_VERSION = 2;

	/**
	 * Constructor.
	 */
	public JdbcCategoryDao() {
		this(SQL_RESOURCE_PREFIX, TABLE_NAME_FORMAT);
	}

	/**
	 * Construct with custom SQL settings.
	 * 
	 * @param sqlResourcePrefixTemplate
	 *        a template with a single {@code %s} parameter for the SQL resource
	 *        prefix
	 * @param tableNameTemplate
	 *        a template with a single {@code %s} parameter for the SQL table
	 *        name
	 */
	public JdbcCategoryDao(String sqlResourcePrefixTemplate, String tableNameTemplate) {
		super(Category.class, "category", TABLES_VERSION, new CategoryRowMapper());
	}

	@Override
	protected void setStoreStatementValues(Category category, PreparedStatement ps) throws SQLException {
		// Row order is: uuid_hi, uuid_lo, config_id ,name, sort, image, ctype
		prepareUUID(1, category.getUuid(), ps);
		ps.setObject(3, category.getConfigId());
		ps.setString(4, category.getName());
		ps.setInt(5, (category.getDefaultRating() != null ? category.getDefaultRating().intValue() : 0));
		ps.setString(6, category.getImage());
		ps.setShort(7, category.getType() != null ? category.getType().getIndex()
				: CategoryType.Unknown.getIndex());
	}

	@Override
	protected void setUpdateStatementValues(Category category, PreparedStatement ps)
			throws SQLException {
		// cols: name, sort, image, ctype
		//       uuid_hi, uuid_lo
		ps.setString(1, category.getName());
		ps.setInt(2, (category.getDefaultRating() != null ? category.getDefaultRating().intValue() : 0));
		ps.setString(3, category.getImage());
		ps.setShort(4, category.getType() != null ? category.getType().getIndex()
				: CategoryType.Unknown.getIndex());
		prepareUUID(5, category.getUuid(), ps);
		ps.setObject(7, category.getConfigId());
	}

	private static final class CategoryRowMapper implements RowMapper<Category> {

		@Override
		public Category mapRow(ResultSet rs, int rowNum) throws SQLException {
			Category row = new Category();
			// Row order is: uuid_hi, uuid_lo, config_id, name, sort, ctype
			row.setUuid(readUUID(1, rs));
			row.setConfigId(rs.getLong(3));
			row.setName(rs.getString(4));
			row.setDefaultRating(rs.getInt(5));
			row.setImage(rs.getString(6));
			row.setType(CategoryType.forIndexValue(rs.getShort(7)));
			return row;
		}
	}

}
