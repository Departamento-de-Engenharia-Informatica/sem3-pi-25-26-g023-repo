-- =============================================
-- USBD07 - Segments by Owner
-- =============================================

-- Main Query: List all line segments of a given owner
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
    CASE WHEN ls.is_electrified = 'Y' THEN 'Yes' ELSE 'No' END as "Electrified",
    ls.max_speed_kmh as "Max Speed (km/h)",
    ls.max_weight_kg_per_m as "Max Weight (kg/m)"
FROM LINE_SEGMENT ls
         JOIN RAILWAY_LINE rl ON ls.line_id = rl.line_id
         JOIN OPERATOR o ON rl.owner_operator_id = o.operator_id
         JOIN STATION s1 ON ls.start_station_id = s1.station_id
         JOIN STATION s2 ON ls.end_station_id = s2.station_id
WHERE o.operator_id = 'IP'
ORDER BY rl.line_id, ls.segment_id;

-- Summary Query
SELECT
    o.operator_id as "Operator ID",
    o.name as "Owner",
    COUNT(ls.segment_id) as "Segment Count",
    ROUND(SUM(ls.segment_length_km), 1) as "Total Length (km)",
    LISTAGG(rl.line_id, ', ') WITHIN GROUP (ORDER BY rl.line_id) as "Line IDs"
FROM OPERATOR o
    JOIN RAILWAY_LINE rl ON o.operator_id = rl.owner_operator_id
    JOIN LINE_SEGMENT ls ON rl.line_id = ls.line_id
GROUP BY o.operator_id, o.name
ORDER BY o.operator_id;

SELECT 'USBD07 COMPLETED - Segments by owner displayed successfully' as status FROM DUAL;