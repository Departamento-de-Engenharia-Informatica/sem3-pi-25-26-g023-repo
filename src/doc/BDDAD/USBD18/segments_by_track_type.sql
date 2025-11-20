-- =============================================
-- USBD18 - Segments by Owner and Track Type
-- =============================================

SELECT
    o.name as "Owner",
    CASE
        WHEN ls.number_tracks = 1 THEN 'Single Track'
        WHEN ls.number_tracks = 2 THEN 'Double Track'
        WHEN ls.number_tracks = 4 THEN 'Quadruple Track'
        ELSE 'Other'
        END as "Track Type",
    COUNT(*) as "Segment Count",
    ROUND(SUM(ls.length_m) / 1000, 2) as "Total Length (km)"
FROM LINE_SEGMENT ls
         JOIN RAILWAY_LINE rl ON ls.line_id = rl.line_id
         JOIN OPERATOR o ON rl.owner_operator_id = o.operator_id
GROUP BY o.name,
         CASE
             WHEN ls.number_tracks = 1 THEN 'Single Track'
             WHEN ls.number_tracks = 2 THEN 'Double Track'
             WHEN ls.number_tracks = 4 THEN 'Quadruple Track'
             ELSE 'Other'
             END
ORDER BY o.name, "Track Type";