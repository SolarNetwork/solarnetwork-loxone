/* ==================================================================
 * CacheUtils.java - 6/02/2018 8:54:30 AM
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

package net.solarnetwork.node.loxone.dao.jdbc.test;

import java.net.URISyntaxException;
import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.AccessedExpiryPolicy;
import javax.cache.expiry.Duration;

/**
 * Utility methods for cache support in tests.
 * 
 * @author matt
 * @version 1.0
 */
public class CacheUtils {

	/**
	 * Get a cache manager instance.
	 * 
	 * @return the cache manager
	 */
	public static CacheManager createCacheManager() {
		try {
			return Caching
					.getCachingProvider("org.ehcache.jsr107.EhcacheCachingProvider",
							CacheUtils.class.getClassLoader())
					.getCacheManager(CacheUtils.class.getResource("ehcache.xml").toURI(), null);
		} catch ( URISyntaxException e ) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Create a cache.
	 * 
	 * @param cacheManager
	 *        the manager
	 * @param name
	 *        the name
	 * @param keyType
	 *        the key type class
	 * @param valueType
	 *        the value type class
	 * @param expireDuration
	 *        the expiration duration, or {@literal null} for no expiration
	 * @param <K>
	 *        the key type
	 * @param <V>
	 *        the value type
	 * @return the cache
	 */
	public static <K, V> Cache<K, V> createCache(CacheManager cacheManager, String name,
			Class<K> keyType, Class<V> valueType, Duration expireDuration) {
		MutableConfiguration<K, V> configuration = new MutableConfiguration<K, V>()
				.setTypes(keyType, valueType).setStoreByValue(false).setStatisticsEnabled(false);
		if ( expireDuration != null ) {
			configuration.setExpiryPolicyFactory(AccessedExpiryPolicy.factoryOf(expireDuration));
		}
		return cacheManager.createCache(name, configuration);

	}

}
