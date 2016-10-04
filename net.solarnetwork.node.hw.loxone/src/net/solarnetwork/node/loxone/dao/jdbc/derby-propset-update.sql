UPDATE solarnode.loxone_propset
SET dtype = ?
WHERE uuid_hi = ? AND uuid_lo = ? AND config_id = ?
