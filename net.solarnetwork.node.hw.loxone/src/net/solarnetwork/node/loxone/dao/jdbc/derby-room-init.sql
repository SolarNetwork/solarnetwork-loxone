CREATE TABLE solarnode.loxone_room (
	uuid_hi		BIGINT NOT NULL,
	uuid_lo		BIGINT NOT NULL,
	config_id	BIGINT NOT NULL,
	name		VARCHAR(255) NOT NULL,
	sort		INTEGER NOT NULL WITH DEFAULT 0,
	image		VARCHAR(48),
	CONSTRAINT loxone_room_pk PRIMARY KEY (config_id, uuid_hi, uuid_lo)
);

INSERT INTO solarnode.sn_settings (skey, svalue) 
VALUES ('solarnode.loxone_room.version', '1');
