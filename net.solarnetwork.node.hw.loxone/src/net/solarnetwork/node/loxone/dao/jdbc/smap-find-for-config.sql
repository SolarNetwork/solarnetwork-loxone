SELECT uuid_hi, uuid_lo, config_id, source_id
FROM  solarnode.loxone_smap
WHERE config_id = ?
ORDER BY lower(source_id) ASC
