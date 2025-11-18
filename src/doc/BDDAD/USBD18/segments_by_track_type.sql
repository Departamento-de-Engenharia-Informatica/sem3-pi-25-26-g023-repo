-- =============================================
-- USBD18 - Segments by Owner and Track Type
-- =============================================

SELECT
    o.name as "Owner",
    ls.track_type as "Track Type",
    COUNT(*) as "Segment Count",
    SUM(ls.segment_length_km) as "Total Length (km)"
FROM LINE_SEGMENT ls
         JOIN RAILWAY_LINE rl ON ls.line_id = rl.line_id
         JOIN OPERATOR o ON rl.owner_operator_id = o.operator_id
GROUP BY o.name, ls.track_type
ORDER BY o.name, ls.track_type;