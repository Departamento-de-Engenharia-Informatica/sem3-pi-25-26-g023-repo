-- =============================================
-- USBD18 - Segments by Track Type per Line
-- =============================================

-- =============================================
-- MAIN QUERY - Interactive Version
-- Prompts user for line_id input
-- =============================================
SELECT 
    l.line_id,
    l.name as line_name,
    ls.number_tracks as track_type,
    COUNT(*) as segment_count
FROM LINE_SEGMENT ls
JOIN RAILWAY_LINE l ON ls.line_id = l.line_id
WHERE ls.line_id = &input_line_id
GROUP BY l.line_id, l.name, ls.number_tracks
ORDER BY ls.number_tracks;

-- =============================================
-- ALTERNATIVE: Function Version
-- Returns results for any line_id parameter
-- =============================================
CREATE OR REPLACE FUNCTION get_segments_by_track_type(
    p_line_id IN NUMBER
) RETURN SYS_REFCURSOR
IS
    v_result SYS_REFCURSOR;
BEGIN
    OPEN v_result FOR
        SELECT 
            ls.number_tracks as track_type,
            COUNT(*) as segment_count,
            LISTAGG(ls.segment_id, ', ') WITHIN GROUP (ORDER BY ls.segment_id) as segment_ids
        FROM LINE_SEGMENT ls
        WHERE ls.line_id = p_line_id
        GROUP BY ls.number_tracks
        ORDER BY ls.number_tracks;
    
    RETURN v_result;
END get_segments_by_track_type;
/

-- =============================================
-- TEST QUERIES - Verification for all lines
-- =============================================

-- Test Query 1: Show all lines with their segments summary
SELECT 
    l.line_id,
    l.name as line_name,
    COUNT(ls.segment_id) as total_segments,
    LISTAGG(ls.number_tracks || ' tracks (' || COUNT(*) OVER (PARTITION BY l.line_id, ls.number_tracks) || ')', ', ') 
        WITHIN GROUP (ORDER BY ls.number_tracks) as track_type_summary
FROM RAILWAY_LINE l
LEFT JOIN LINE_SEGMENT ls ON l.line_id = ls.line_id
GROUP BY l.line_id, l.name
ORDER BY l.line_id;

-- Test Query 2: Detailed breakdown for ALL lines
SELECT 
    l.line_id,
    l.name as line_name,
    ls.number_tracks as track_type,
    COUNT(ls.segment_id) as segment_count,
    LISTAGG(ls.segment_id, ', ') WITHIN GROUP (ORDER BY ls.segment_id) as segment_ids
FROM RAILWAY_LINE l
JOIN LINE_SEGMENT ls ON l.line_id = ls.line_id
GROUP BY l.line_id, l.name, ls.number_tracks
ORDER BY l.line_id, ls.number_tracks;

-- =============================================
-- INDIVIDUAL LINE TESTS
-- =============================================

-- Test Line 1: Ramal São Bento - Campanhã
SELECT 'Line 1 - São Bento to Campanhã' as test_case FROM DUAL;
SELECT 
    number_tracks as track_type,
    COUNT(*) as segment_count,
    LISTAGG(segment_id, ', ') as segment_ids
FROM LINE_SEGMENT 
WHERE line_id = 1
GROUP BY number_tracks;

-- Test Line 2: Ramal Campanhã - Nine  
SELECT 'Line 2 - Campanhã to Nine' as test_case FROM DUAL;
SELECT 
    number_tracks as track_type,
    COUNT(*) as segment_count,
    LISTAGG(segment_id, ', ') as segment_ids
FROM LINE_SEGMENT 
WHERE line_id = 2
GROUP BY number_tracks;

-- Test Line 3: Ramal Nine - Barcelos
SELECT 'Line 3 - Nine to Barcelos' as test_case FROM DUAL;
SELECT 
    number_tracks as track_type,
    COUNT(*) as segment_count,
    LISTAGG(segment_id, ', ') as segment_ids
FROM LINE_SEGMENT 
WHERE line_id = 3
GROUP BY number_tracks;

-- Test Line 4: Ramal Barcelos - Viana
SELECT 'Line 4 - Barcelos to Viana' as test_case FROM DUAL;
SELECT 
    number_tracks as track_type,
    COUNT(*) as segment_count,
    LISTAGG(segment_id, ', ') as segment_ids
FROM LINE_SEGMENT 
WHERE line_id = 4
GROUP BY number_tracks;

-- Test Line 5: Ramal Viana - Caminha
SELECT 'Line 5 - Viana to Caminha' as test_case FROM DUAL;
SELECT 
    number_tracks as track_type,
    COUNT(*) as segment_count,
    LISTAGG(segment_id, ', ') as segment_ids
FROM LINE_SEGMENT 
WHERE line_id = 5
GROUP BY number_tracks;

-- Test Line 6: Ramal Caminha - Torre
SELECT 'Line 6 - Caminha to Torre' as test_case FROM DUAL;
SELECT 
    number_tracks as track_type,
    COUNT(*) as segment_count,
    LISTAGG(segment_id, ', ') as segment_ids
FROM LINE_SEGMENT 
WHERE line_id = 6
GROUP BY number_tracks;

-- Test Line 7: Ramal Torre - Valença
SELECT 'Line 7 - Torre to Valença' as test_case FROM DUAL;
SELECT 
    number_tracks as track_type,
    COUNT(*) as segment_count,
    LISTAGG(segment_id, ', ') as segment_ids
FROM LINE_SEGMENT 
WHERE line_id = 7
GROUP BY number_tracks;


-- =============================================