UPDATE solarnode.loxone_datumset
SET fsecs = ?
WHERE uuid_hi = ? AND uuid_lo = ? AND config_id = ?
