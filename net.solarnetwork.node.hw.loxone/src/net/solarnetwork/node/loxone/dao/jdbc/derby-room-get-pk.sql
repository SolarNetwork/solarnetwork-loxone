SELECT uuid_hi, uuid_lo, config_id, name, sort
FROM  solarnode.loxone_room
WHERE uuid_hi = ? AND uuid_lo = ?
