CREATE TABLE solarnode.loxone_category (
	uuid_hi		BIGINT NOT NULL,
	uuid_lo		BIGINT NOT NULL,
	config_id	BIGINT NOT NULL,
	name		VARCHAR(255) NOT NULL,
	sort		INTEGER NOT NULL DEFAULT 0,
	image		VARCHAR(48),
	ctype		SMALLINT NOT NULL DEFAULT -1,
	CONSTRAINT loxone_category_pk PRIMARY KEY (config_id, uuid_hi, uuid_lo)
);

CREATE INDEX loxone_category_name_idx ON solarnode.loxone_category (name);

INSERT INTO solarnode.sn_settings (skey, svalue) 
VALUES ('solarnode.loxone_category.version', '2');
