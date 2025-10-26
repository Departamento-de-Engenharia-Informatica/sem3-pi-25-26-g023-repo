-- =============================================
-- USBD18 - Segments by Owner and Track Type
-- =============================================

-- MAIN QUERY - Count segments by owner and track type
SELECT
    o.name as owner_name,
    ls.track_type,
    COUNT(*) as segment_count,
    SUM(ls.segment_length_km) as total_length_km,
    ROUND(AVG(ls.max_speed_kmh), 1) as avg_max_speed,
    MIN(ls.max_speed_kmh) as min_speed,
    MAX(ls.max_speed_kmh) as max_speed
FROM LINE_SEGMENT ls
         JOIN RAILWAY_LINE rl ON ls.line_id = rl.line_id
         JOIN OPERATOR o ON rl.owner_operator_id = o.operator_id
GROUP BY o.name, ls.track_type
ORDER BY o.name, ls.track_type;

-- DETAILED BREAKDOWN BY LINE
SELECT
    o.name as owner_name,
    rl.line_id,
    rl.name as line_name,
    ls.track_type,
    COUNT(*) as segment_count,
    SUM(ls.segment_length_km) as total_length_km
FROM LINE_SEGMENT ls
         JOIN RAILWAY_LINE rl ON ls.line_id = rl.line_id
         JOIN OPERATOR o ON rl.owner_operator_id = o.operator_id
GROUP BY o.name, rl.line_id, rl.name, ls.track_type
ORDER BY o.name, rl.line_id, ls.track_type;

-- SUMMARY STATISTICS
SELECT
    'Total Segments by Track Type' as summary,
    track_type,
    COUNT(*) as segment_count,
    ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM LINE_SEGMENT), 2) as percentage,
    SUM(segment_length_km) as total_length_km
FROM LINE_SEGMENT
GROUP BY track_type
ORDER BY segment_count DESC;

SELECT 'USBD18 COMPLETED - Segments by owner and track type analysis' as status FROM DUAL;