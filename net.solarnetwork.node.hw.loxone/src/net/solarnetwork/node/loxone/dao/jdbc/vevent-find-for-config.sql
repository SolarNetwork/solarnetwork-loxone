SELECT uuid_hi, uuid_lo, config_id, created, fvalue
FROM  solarnode.loxone_vevent
WHERE config_id = ?
ORDER BY created DESC, uuid_hi ASC, uuid_lo ASC
