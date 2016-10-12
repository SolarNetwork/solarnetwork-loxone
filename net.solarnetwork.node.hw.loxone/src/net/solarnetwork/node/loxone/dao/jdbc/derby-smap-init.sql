CREATE TABLE solarnode.loxone_smap (
	uuid_hi		BIGINT NOT NULL,
	uuid_lo		BIGINT NOT NULL,
	config_id	BIGINT NOT NULL,
	source_id	VARCHAR(32) NOT NULL,
	CONSTRAINT loxone_smap_pk PRIMARY KEY (config_id, uuid_hi, uuid_lo)
);

INSERT INTO solarnode.sn_settings (skey, svalue) 
VALUES ('solarnode.loxone_smap.version', '1');
