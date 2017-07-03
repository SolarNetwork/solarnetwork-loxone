CREATE INDEX loxone_room_name_idx ON solarnode.loxone_room (name);

UPDATE solarnode.sn_settings SET svalue = '2'
WHERE skey = 'solarnode.loxone_room.version';
