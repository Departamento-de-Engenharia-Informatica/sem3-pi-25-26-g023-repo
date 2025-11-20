-- =============================================
-- USBD07 - Segments by Owner
-- =============================================

SELECT
    ls.segment_id,
    ls.line_id,
    rl.name as line_name,
    ls.segment_order,
    ls.is_electrified,
    ls.max_weight_kg_m,
    ls.length_m,
    ls.number_tracks
FROM LINE_SEGMENT ls
         JOIN RAILWAY_LINE rl ON ls.line_id = rl.line_id
WHERE rl.owner_operator_id = '&owner_operator_id';