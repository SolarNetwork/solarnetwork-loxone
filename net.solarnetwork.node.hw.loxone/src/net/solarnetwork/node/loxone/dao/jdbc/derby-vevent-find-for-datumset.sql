SELECT ve.uuid_hi, ve.uuid_lo, ve.config_id, ve.created, ve.fvalue, ds.fsecs, ds.dtype
FROM  solarnode.loxone_vevent ve
INNER JOIN solarnode.loxone_datumset ds
	ON ve.uuid_hi = ds.uuid_hi AND ve.uuid_lo = ds.uuid_lo AND ve.config_id = ds.config_id
WHERE ve.config_id = ?
ORDER BY ve.created DESC, ve.uuid_hi ASC, ve.uuid_lo ASC
