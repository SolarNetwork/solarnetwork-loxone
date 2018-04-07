/* ==================================================================
 * SecurityUtilsTests.java - 5/04/2018 11:19:05 AM
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

package net.solarnetwork.node.loxone.util.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import java.security.PublicKey;
import org.junit.Test;
import net.solarnetwork.node.loxone.util.SecurityUtils;

/**
 * Test cases for the {@link SecurityUtils} class.
 * 
 * @author matt
 * @version 1.0
 */
public class SecurityUtilsTests {

	private static final String TEST_PUB_KEY = "-----BEGIN CERTIFICATE-----MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDTEBiUtYNiGlrRZm184J5buRR/MYPNMR0eIPfOseIskiJkvDqXQ75YlU+3M6/zEAy1IVunc5yPoVFMESg4C+mCXrtLnJxTuSucEmpGMgycoDCZio/maOKHRJQtoTAYQJ1C55N2OmFDAy5nHDfpQX1wDo79o1TZo7xa+mrWRBOjHQIDAQAB-----END CERTIFICATE-----";

	@Test
	public void parsePublicKey() {
		PublicKey key = SecurityUtils.parsePublicKey(TEST_PUB_KEY);
		assertThat("Key parsed", key, notNullValue());
		assertThat("RSA key", key.getAlgorithm(), equalTo("RSA"));
		assertThat("X.509 cert", key.getFormat(), equalTo("X.509"));
	}

}
