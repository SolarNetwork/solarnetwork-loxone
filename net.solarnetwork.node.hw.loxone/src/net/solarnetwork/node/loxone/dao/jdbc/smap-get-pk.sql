SELECT uuid_hi, uuid_lo, config_id, source_id
FROM  solarnode.loxone_smap
WHERE uuid_hi = ? AND uuid_lo = ? AND config_id = ?
