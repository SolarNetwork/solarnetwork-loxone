/* ==================================================================
 * ValueEventDatumGenerator.java - 28/09/2016 5:13:05 PM
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

package net.solarnetwork.node.loxone.impl;

import java.util.Collection;
import java.util.Date;
import java.util.UUID;
import net.solarnetwork.node.dao.DatumDao;
import net.solarnetwork.node.domain.Datum;
import net.solarnetwork.node.domain.GeneralNodeDatum;
import net.solarnetwork.node.loxone.dao.DatumUUIDSetDao;
import net.solarnetwork.node.loxone.dao.UUIDSetDao;
import net.solarnetwork.node.loxone.domain.Config;
import net.solarnetwork.node.loxone.domain.ValueEvent;
import net.solarnetwork.util.OptionalService;

/**
 * Generate {@link Datum} objects from {@link ValueEvent} objects and persist
 * them.
 * 
 * @author matt
 * @version 1.0
 */
public class ValueEventDatumGenerator {

	/** The default sample key to use. */
	public static final String DEFAULT_SAMPLE_KEY = "value";

	// TODO: need some sort of way to resolve accumulating/instantaneous mode from UUID 

	private final DatumUUIDSetDao uuidSetDao;
	private final OptionalService<DatumDao<Datum>> datumDao;

	/**
	 * Construct with a UUIDSetDao.
	 * 
	 * @param uuidSetDao
	 *        The UUIDSetDao to use. Only value events whose UUID is contained
	 *        in the set managed by this DAO will be translated into
	 *        {@link Datum} instances.
	 * @param datumDao
	 *        The DatumDao to use to persist translated value events to.
	 */
	public ValueEventDatumGenerator(DatumUUIDSetDao uuidSetDao,
			OptionalService<DatumDao<Datum>> datumDao) {
		super();
		this.uuidSetDao = uuidSetDao;
		this.datumDao = datumDao;
	}

	/**
	 * Process a collection of value events.
	 * 
	 * The configured {@link UUIDSetDao} will be used to test each
	 * {@link ValueEvent#getUuid()} value. If
	 * {@link UUIDSetDao#contains(Long, UUID)} returns {@code true} then the
	 * event will be translated into a {@link GeneralNodeDatum} and persisted
	 * using the configured {@link DatumDao}.
	 * 
	 * @param valueEvents
	 *        The value events to process.
	 * @param created
	 *        An optional creation date to assign to all generated
	 *        {@link Datum}. If not provided, the current system time will be
	 *        used.
	 * @return The number of {@code Datum} objects persisted.
	 */
	public int handleValueEvents(Collection<ValueEvent> valueEvents, Date created) {
		DatumDao<Datum> dao = datumDao.service();
		if ( valueEvents == null || dao == null ) {
			return 0;
		}
		if ( created == null ) {
			created = new Date();
		}
		int processed = 0;
		for ( ValueEvent ve : valueEvents ) {
			if ( uuidSetDao.contains(ve.getConfigId(), ve.getUuid()) ) {
				GeneralNodeDatum datum = new GeneralNodeDatum();
				datum.setSourceId(sourceIdForValueEvent(ve));
				datum.setCreated(created);
				datum.putInstantaneousSampleValue(sampleKeyForValueEvent(ve), ve.getValue());
				dao.storeDatum(datum);
				processed++;
			}
		}
		return processed;
	}

	/**
	 * Get a {@link Datum#getSourceId()} value for a value event.
	 * 
	 * @param valueEvent
	 *        The event to get the source ID for.
	 * @return The source ID.
	 * @throws IllegalArgumentException
	 *         if {@code valueEvent} or any necessary property on it are
	 *         {@code null}
	 */
	private String sourceIdForValueEvent(ValueEvent valueEvent) {
		Long configId = (valueEvent != null ? valueEvent.getConfigId() : null);
		UUID uuid = (valueEvent != null ? valueEvent.getUuid() : null);
		if ( configId == null || uuid == null ) {
			throw new IllegalArgumentException(
					"Both configId and uuid values are required from " + valueEvent);
		}
		return String.format("%s/%s", Config.idFromExternalForm(configId), uuid);
	}

	private String sampleKeyForValueEvent(ValueEvent valueEvent) {
		return DEFAULT_SAMPLE_KEY;
	}

}
