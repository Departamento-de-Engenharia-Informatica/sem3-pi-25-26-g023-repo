-- =============================================
-- USBD07 - Segments by Owner
-- =============================================

SELECT
    ls.segment_id as "Segment ID",
    rl.name as "Line Name",
    s1.name as "Start Station",
    s2.name as "End Station",
    ls.segment_length_km as "Length (km)",
    ls.track_type as "Track Type",
    ls.gauge_mm as "Gauge (mm)"
FROM LINE_SEGMENT ls
         JOIN RAILWAY_LINE rl ON ls.line_id = rl.line_id
         JOIN STATION s1 ON ls.start_station_id = s1.station_id
         JOIN STATION s2 ON ls.end_station_id = s2.station_id
WHERE rl.owner_operator_id = 'IP'
ORDER BY rl.line_id, ls.segment_id;