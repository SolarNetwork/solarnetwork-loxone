/* ==================================================================
 * ConfigApi.java - 4/04/2018 3:30:58 PM
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

package net.solarnetwork.node.loxone.domain;

import java.net.URI;
import java.util.Arrays;

/**
 * Configuration information about the Loxone API.
 * 
 * @author matt
 * @version 1.0
 * @since 1.3
 */
public class ConfigApi {

	private final URI websocketUri;
	private final String snr;
	private final String version;
	private final int[] versionComponents;

	/**
	 * @param websocketUri
	 * @param snr
	 * @param version
	 */
	public ConfigApi(URI websocketUri, String snr, String version) {
		super();
		this.websocketUri = websocketUri;
		this.snr = snr;
		this.version = version;
		this.versionComponents = versionArray(version);
	}

	/**
	 * Get the URI to use for the websocket connection.
	 * 
	 * @return the URI, e.g. {@literal ws://example.com/ws/rfc6455}
	 */
	public URI getWebsocketUri() {
		return websocketUri;
	}

	/**
	 * Get the SNR.
	 * 
	 * @return the SNR, e.g. {@literal 50:44:44:10:44:FF}
	 */
	public String getSnr() {
		return snr;
	}

	/**
	 * Get the API version.
	 * 
	 * <p>
	 * Version strings are composed as
	 * <b>major</b>.<b>minor</b>.<b>patch</b>.<b>build</b>.
	 * </p>
	 * 
	 * @return the API version, e.g. {@literal 9.1.10.30}
	 */
	public String getVersion() {
		return version;
	}

	private static int[] versionArray(String version) {
		if ( version == null ) {
			return null;
		}
		String[] components = version.split("\\.");
		if ( components == null || components.length < 1 ) {
			return null;
		}
		return Arrays.stream(components).mapToInt(Integer::parseInt).toArray();
	}

	/**
	 * Test if the API major version is at least a given value.
	 * 
	 * <p>
	 * This tests just the major version, for example if the version is
	 * {@literal 9.1.10.30} then passing in {@literal 9} will return
	 * {@literal true} while passing in {@literal 8} will return
	 * {@literal false}.
	 * </p>
	 * 
	 * @param major
	 *        the major version to compare
	 * @return {@literal true} if {@code version} is at least the given value
	 * @see #getVersion()
	 */
	public boolean isVersionAtLeast(int major) {
		return isVersionAtLeast(major, 0);
	}

	/**
	 * Test if the API major version is at least a given value.
	 * 
	 * <p>
	 * This tests just the major version, for example if the version is
	 * {@literal 9.1.10.30} then passing in major {@literal 9} and minor
	 * {@literal 1} will return {@literal true} while passing in major
	 * {@literal 9} and minor {@literal 0} will return {@literal false}.
	 * </p>
	 * 
	 * @param major
	 *        the major version to compare
	 * @param minor
	 *        the minor version to compare
	 * @return {@literal true} if {@code version} is at least the given value
	 * @see #getVersion()
	 */
	public boolean isVersionAtLeast(int major, int minor) {
		return (versionComponents != null && versionComponents.length > 1
				&& versionComponents[0] >= major && versionComponents[1] >= minor);
	}

}
