UPDATE solarnode.loxone_datumset
SET fsecs = ?, dtype = ?
WHERE uuid_hi = ? AND uuid_lo = ? AND config_id = ?
