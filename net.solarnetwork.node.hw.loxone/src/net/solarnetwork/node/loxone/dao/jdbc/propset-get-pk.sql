SELECT uuid_hi, uuid_lo, config_id, dtype
FROM  solarnode.loxone_propset
WHERE uuid_hi = ? AND uuid_lo = ? AND config_id = ?
