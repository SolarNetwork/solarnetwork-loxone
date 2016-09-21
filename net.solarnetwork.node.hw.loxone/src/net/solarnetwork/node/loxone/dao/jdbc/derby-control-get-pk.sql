SELECT uuid_hi, uuid_lo, config_id, name, sort, ctype, room_hi, room_lo, cat_hi, cat_lo
FROM  solarnode.loxone_control
WHERE uuid_hi = ? AND uuid_lo = ? AND config_id = ?
