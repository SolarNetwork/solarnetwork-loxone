CREATE TABLE solarnode.loxone_propset (
	uuid_hi		BIGINT NOT NULL,
	uuid_lo		BIGINT NOT NULL,
	config_id	BIGINT NOT NULL,
	dtype       SMALLINT NOT NULL DEFAULT 0,
	CONSTRAINT loxone_propset_pk PRIMARY KEY (config_id, uuid_hi, uuid_lo)
);

INSERT INTO solarnode.sn_settings (skey, svalue) 
VALUES ('solarnode.loxone_propset.version', '1');
