CREATE TABLE solarnode.loxone_control (
	uuid_hi		BIGINT NOT NULL,
	uuid_lo		BIGINT NOT NULL,
	config_id	BIGINT NOT NULL,
	name		VARCHAR(255) NOT NULL,
	sort		INTEGER NOT NULL DEFAULT 0,
	ctype		SMALLINT NOT NULL DEFAULT -1,
	room_hi		BIGINT,
	room_lo		BIGINT,
	cat_hi		BIGINT,
	cat_lo		BIGINT,
	CONSTRAINT loxone_control_pk PRIMARY KEY (config_id, uuid_hi, uuid_lo)
);

CREATE INDEX loxone_control_name_idx ON solarnode.loxone_control (name);

CREATE TABLE solarnode.loxone_control_state (
	uuid_hi		BIGINT NOT NULL,
	uuid_lo		BIGINT NOT NULL,
	config_id	BIGINT NOT NULL,
	name		VARCHAR(255) NOT NULL,
	event_hi	BIGINT NOT NULL,
	event_lo	BIGINT NOT NULL,
	CONSTRAINT loxone_control_state_control_fk FOREIGN KEY (config_id, uuid_hi, uuid_lo)
		REFERENCES solarnode.loxone_control (config_id, uuid_hi, uuid_lo)
		ON DELETE CASCADE
);

CREATE UNIQUE INDEX loxone_control_state_idx 
ON solarnode.loxone_control_state (config_id, uuid_hi, uuid_lo, name);

CREATE INDEX loxone_control_state_name_idx ON solarnode.loxone_control_state (name);

INSERT INTO solarnode.sn_settings (skey, svalue) 
VALUES ('solarnode.loxone_control.version', '2');
