-- =============================================
-- USBD07 - Segments by Owner
-- =============================================

-- MAIN QUERY - Interactive Version
SELECT
    o.name as "Owner",
    rl.line_id as "Line ID",
    rl.name as "Line Name",
    ls.segment_id as "Segment ID",
    s1.name as "Start Station",
    s2.name as "End Station",
    ls.segment_length_km as "Length (km)",
    ls.track_type as "Track Type",
    ls.gauge_mm as "Gauge (mm)",
    ls.is_electrified as "Electrified",
    ls.max_speed_kmh as "Max Speed (km/h)"
FROM LINE_SEGMENT ls
         JOIN RAILWAY_LINE rl ON ls.line_id = rl.line_id
         JOIN OPERATOR o ON rl.owner_operator_id = o.operator_id
         JOIN STATION s1 ON ls.start_station_id = s1.station_id
         JOIN STATION s2 ON ls.end_station_id = s2.station_id
WHERE UPPER(o.operator_id) = UPPER('&input_operator_id')
ORDER BY rl.line_id, ls.segment_id;

SELECT 'USBD07 READY - Use &input_operator_id (OP001 or OP002)' as status FROM DUAL;