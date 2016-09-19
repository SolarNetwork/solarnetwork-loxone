CREATE TABLE solarnode.loxone_category (
	uuid_hi		BIGINT NOT NULL,
	uuid_lo		BIGINT NOT NULL,
	config_id	BIGINT NOT NULL,
	name		VARCHAR(255) NOT NULL,
	sort		INTEGER NOT NULL WITH DEFAULT 0,
	ctype		SMALLINT NOT NULL WITH DEFAULT -1,
	CONSTRAINT loxone_category_pk PRIMARY KEY (uuid_hi, uuid_lo)
);

INSERT INTO solarnode.sn_settings (skey, svalue) 
VALUES ('solarnode.loxone_category.version', '1');
