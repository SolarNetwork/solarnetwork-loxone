SELECT name, event_hi, event_lo
FROM solarnode.loxone_control_state
WHERE uuid_hi = ? AND uuid_lo = ? AND config_id = ?
