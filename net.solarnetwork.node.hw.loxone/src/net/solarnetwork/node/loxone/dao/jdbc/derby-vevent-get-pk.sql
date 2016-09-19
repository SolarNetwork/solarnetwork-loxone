SELECT uuid_hi, uuid_lo, config_id, created, fvalue
FROM  solarnode.loxone_vevent
WHERE uuid_hi = ? AND uuid_lo = ?
