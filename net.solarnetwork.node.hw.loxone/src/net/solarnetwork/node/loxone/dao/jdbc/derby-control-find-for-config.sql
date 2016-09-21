SELECT uuid_hi, uuid_lo, config_id, name, sort, ctype, room_hi, room_lo, cat_hi, cat_lo
FROM  solarnode.loxone_control
WHERE config_id = ?
ORDER BY sort DESC, lower(name) ASC
