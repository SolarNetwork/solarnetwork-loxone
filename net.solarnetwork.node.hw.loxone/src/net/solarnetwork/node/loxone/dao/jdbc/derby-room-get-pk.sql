SELECT uuid_hi, uuid_lo, config_id, name, sort, image
FROM  solarnode.loxone_room
WHERE uuid_hi = ? AND uuid_lo = ? AND config_id = ?
