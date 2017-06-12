SELECT uuid_hi, uuid_lo, config_id, name, sort, image
FROM  solarnode.loxone_room
WHERE config_id = ? AND name = ?
ORDER BY sort DESC, lower(name) ASC
