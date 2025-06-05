CREATE INDEX loxone_control_name_idx ON solarnode.loxone_control (name);

CREATE INDEX loxone_control_state_name_idx ON solarnode.loxone_control_state (name);

UPDATE solarnode.sn_settings SET svalue = '2'
WHERE skey = 'solarnode.loxone_control.version';
