CREATE TABLE solarnode.loxone_datumset (
	uuid_hi		BIGINT NOT NULL,
	uuid_lo		BIGINT NOT NULL,
	config_id	BIGINT NOT NULL,
	CONSTRAINT loxone_datumset_pk PRIMARY KEY (config_id, uuid_hi, uuid_lo)
);

INSERT INTO solarnode.sn_settings (skey, svalue) 
VALUES ('solarnode.loxone_datumset.version', '1');
