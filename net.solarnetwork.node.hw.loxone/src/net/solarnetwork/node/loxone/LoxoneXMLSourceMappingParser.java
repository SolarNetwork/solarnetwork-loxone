/* ==================================================================
 * LoxoneXMLSourceMappingParser.java - 12/10/2016 8:14:57 PM
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

package net.solarnetwork.node.loxone;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.solarnetwork.node.loxone.domain.SourceMapping;
import net.solarnetwork.node.loxone.domain.UUIDDeserializer;

/**
 * Parses Loxone XML configuration for source mappings.
 * 
 * @author matt
 * @version 1.0
 */
public class LoxoneXMLSourceMappingParser implements LoxoneSourceMappingParser {

	// we're only interested in UUID values for controls, which live inside <C Type=X"> elements of these values
	private final Set<String> containerTypes = new HashSet<>(Arrays.asList("LoxLIVE", "Program"));

	private final Logger log = LoggerFactory.getLogger(getClass());

	private boolean isContainer(XMLStreamReader r) {
		return ("C".equals(r.getLocalName())
				&& containerTypes.contains(r.getAttributeValue(null, "Type")));
	}

	@Override
	public void parseInputStream(InputStream in,
			LoxoneSourceMappingParser.SourceMappingCallback callback) throws IOException {
		XMLInputFactory f = XMLInputFactory.newInstance();
		f.setProperty(XMLInputFactory.IS_VALIDATING, false);
		f.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
		XMLStreamReader r = null;
		try {
			r = f.createXMLStreamReader(in);

			boolean inContainer = false;
			int containerDepth = 0;
			while ( r.hasNext() ) {
				int event = r.next();
				if ( inContainer && event == XMLStreamConstants.END_ELEMENT && --containerDepth < 1 ) {
					// we have left the container
					inContainer = false;
				}
				if ( event != XMLStreamConstants.START_ELEMENT ) {
					continue;
				}
				if ( !inContainer ) {
					// skip until we get to a <C Type="X"> where X is in containerTypes
					if ( isContainer(r) ) {
						containerDepth++;
						inContainer = true;
					}
					continue;
				}
				containerDepth++;
				if ( "C".equals(r.getLocalName()) ) {
					String title = r.getAttributeValue(null, "Title");
					if ( title != null ) {
						String uuidVal = r.getAttributeValue(null, "U");
						if ( uuidVal != null ) {
							try {
								UUID uuid = UUIDDeserializer.deserializeUUID(uuidVal);
								log.trace("Parsed source mapping {} -> {}", uuidVal, title);
								callback.parsedSourceMapping(new SourceMapping(uuid, title));
							} catch ( IOException e ) {
								log.debug("Ignoring UUID parsing error {} at {}", e.getMessage(),
										r.getLocation());
							}
						}
					}
				}
			}
		} catch ( XMLStreamException e ) {
			if ( r != null ) {
				log.error("Error parsing Loxone XML: {} at {}", e.getMessage(), r.getLocation());
			}
			throw new IOException(e);
		} finally {
			if ( r != null ) {
				try {
					r.close();
				} catch ( XMLStreamException e ) {
					// ignore
				}
			}
		}
	}

}
