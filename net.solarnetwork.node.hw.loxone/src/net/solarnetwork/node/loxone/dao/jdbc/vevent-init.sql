CREATE TABLE solarnode.loxone_vevent (
	uuid_hi		BIGINT NOT NULL,
	uuid_lo		BIGINT NOT NULL,
	config_id	BIGINT NOT NULL,
	created		TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	fvalue      DOUBLE PRECISION NOT NULL,
	CONSTRAINT loxone_vevent_pk PRIMARY KEY (config_id, uuid_hi, uuid_lo)
);

INSERT INTO solarnode.sn_settings (skey, svalue) 
VALUES ('solarnode.loxone_vevent.version', '1');
