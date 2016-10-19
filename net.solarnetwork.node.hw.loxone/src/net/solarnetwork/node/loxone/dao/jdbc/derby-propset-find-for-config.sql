SELECT uuid_hi, uuid_lo, config_id, dtype
FROM  solarnode.loxone_propset
WHERE config_id = ?
ORDER BY uuid_hi ASC, uuid_lo ASC
