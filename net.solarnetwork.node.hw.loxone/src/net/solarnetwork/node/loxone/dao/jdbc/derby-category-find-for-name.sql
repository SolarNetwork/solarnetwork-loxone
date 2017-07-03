SELECT uuid_hi, uuid_lo, config_id, name, sort, image, ctype
FROM  solarnode.loxone_category
WHERE config_id = ? AND name = ?
ORDER BY sort DESC, lower(name) ASC
