SELECT uuid_hi, uuid_lo, config_id, name, sort, image, ctype
FROM  solarnode.loxone_category
WHERE uuid_hi = ? AND uuid_lo = ? AND config_id = ?
