CREATE TABLE solarnode.loxone_vevent (
	uuid_hi		BIGINT NOT NULL,
	uuid_lo		BIGINT NOT NULL,
	config_id	BIGINT NOT NULL,
	created		TIMESTAMP NOT NULL WITH DEFAULT CURRENT_TIMESTAMP,
	fvalue      DOUBLE NOT NULL,
	CONSTRAINT loxone_vevent_pk PRIMARY KEY (uuid_hi, uuid_lo)
);

INSERT INTO solarnode.sn_settings (skey, svalue) 
VALUES ('solarnode.loxone_vevent.version', '1');
