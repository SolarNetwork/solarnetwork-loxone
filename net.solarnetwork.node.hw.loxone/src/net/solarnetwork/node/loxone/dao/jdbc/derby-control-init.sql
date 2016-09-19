CREATE TABLE solarnode.loxone_control (
	uuid_hi		BIGINT NOT NULL,
	uuid_lo		BIGINT NOT NULL,
	config_id	BIGINT NOT NULL,
	name		VARCHAR(255) NOT NULL,
	sort		INTEGER NOT NULL WITH DEFAULT 0,
	ctype		SMALLINT NOT NULL WITH DEFAULT -1,
	room_hi		BIGINT,
	room_lo		BIGINT,
	cat_hi		BIGINT,
	cat_lo		BIGINT,
	CONSTRAINT loxone_control_pk PRIMARY KEY (uuid_hi, uuid_lo, config_id)
);

CREATE TABLE solarnode.loxone_control_state (
	uuid_hi		BIGINT NOT NULL,
	uuid_lo		BIGINT NOT NULL,
	config_id	BIGINT NOT NULL,
	name		VARCHAR(255) NOT NULL,
	event_hi	BIGINT NOT NULL,
	event_lo	BIGINT NOT NULL,
	CONSTRAINT loxone_control_state_control_fk FOREIGN KEY (uuid_hi, uuid_lo, config_id)
		REFERENCES solarnode.loxone_control (uuid_hi, uuid_lo, config_id)
		ON DELETE CASCADE
);

CREATE UNIQUE INDEX loxone_control_state_idx 
ON solarnode.loxone_control_state (uuid_hi, uuid_lo, config_id, name);

INSERT INTO solarnode.sn_settings (skey, svalue) 
VALUES ('solarnode.loxone_control.version', '1');
