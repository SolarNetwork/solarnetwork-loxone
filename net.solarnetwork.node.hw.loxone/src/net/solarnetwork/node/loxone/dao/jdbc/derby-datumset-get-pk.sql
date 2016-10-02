SELECT uuid_hi, uuid_lo, config_id, fsecs, dtype
FROM  solarnode.loxone_datumset
WHERE uuid_hi = ? AND uuid_lo = ? AND config_id = ?
