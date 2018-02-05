SELECT 
	co.uuid_hi, co.uuid_lo, co.config_id, sm.source_id, co.name, co.sort, co.ctype, 
	co.room_hi, co.room_lo, co.cat_hi, co.cat_lo,
	ds.fsecs,
	st.event_hi, st.event_lo, st.name AS event_name, 
	ps.dtype,
	ve.fvalue
FROM solarnode.loxone_propset ps
INNER JOIN solarnode.loxone_vevent ve
	ON ve.uuid_hi = ps.uuid_hi AND ve.uuid_lo = ps.uuid_lo AND ve.config_id = ps.config_id
INNER JOIN solarnode.loxone_control_state st
	ON st.event_hi = ve.uuid_hi AND st.event_lo = ve.uuid_lo AND st.config_id = ve.config_id
INNER JOIN solarnode.loxone_control co
	ON co.uuid_hi = st.uuid_hi AND co.uuid_lo = st.uuid_lo AND co.config_id = st.config_id
LEFT OUTER JOIN solarnode.loxone_datumset ds
	ON ds.uuid_hi = co.uuid_hi AND ds.uuid_lo = co.uuid_lo AND ds.config_id = co.config_id
LEFT OUTER JOIN solarnode.loxone_smap sm
	ON sm.uuid_hi = co.uuid_hi AND sm.uuid_lo = co.uuid_lo AND sm.config_id = co.config_id
WHERE ps.config_id = ?
ORDER BY co.uuid_hi, co.uuid_lo, st.name, ps.uuid_hi, ps.uuid_lo
