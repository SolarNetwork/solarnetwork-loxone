UPDATE solarnode.loxone_smap
SET source_id = ?
WHERE uuid_hi = ? AND uuid_lo = ? AND config_id = ?
