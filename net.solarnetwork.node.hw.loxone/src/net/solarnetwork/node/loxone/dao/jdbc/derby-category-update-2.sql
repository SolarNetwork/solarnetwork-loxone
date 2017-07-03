CREATE INDEX loxone_category_name_idx ON solarnode.loxone_category (name);

UPDATE solarnode.sn_settings SET svalue = '2'
WHERE skey = 'solarnode.loxone_category.version';
