-- =============================================
-- USBD18 - Segments by Owner and Track Type (OTIMIZADA)
-- =============================================

-- =============================================
-- MAIN QUERY - Count segments by owner and track type
-- =============================================
SELECT
    o.name as owner_name,
    ls.track_type,
    COUNT(*) as segment_count,
    SUM(ls.segment_length_km) as total_length_km,
    ROUND(AVG(ls.max_speed_kmh), 1) as avg_max_speed
FROM LINE_SEGMENT ls
         JOIN RAILWAY_LINE rl ON ls.line_id = rl.line_id
         JOIN OPERATOR o ON rl.owner_operator_id = o.operator_id
GROUP BY o.name, ls.track_type
ORDER BY o.name, ls.track_type;

-- =============================================
-- FUNCTION VERSION - For specific owner
-- =============================================
CREATE OR REPLACE FUNCTION get_segments_by_owner_tracktype(
    p_owner_name IN VARCHAR2
) RETURN SYS_REFCURSOR
IS
    v_result SYS_REFCURSOR;
BEGIN
OPEN v_result FOR
SELECT
    ls.track_type,
    COUNT(*) as segment_count,
    SUM(ls.segment_length_km) as total_length_km
FROM LINE_SEGMENT ls
         JOIN RAILWAY_LINE rl ON ls.line_id = rl.line_id
         JOIN OPERATOR o ON rl.owner_operator_id = o.operator_id
WHERE o.name = p_owner_name
GROUP BY ls.track_type
ORDER BY ls.track_type;

RETURN v_result;
END get_segments_by_owner_tracktype;
/

-- =============================================
-- PROCEDURE VERSION - With DBMS_OUTPUT
-- =============================================
CREATE OR REPLACE PROCEDURE show_segments_by_owner_tracktype(
    p_owner_name IN VARCHAR2
)
AS
BEGIN
    DBMS_OUTPUT.PUT_LINE('Number of segments for owner: ' || p_owner_name);
    DBMS_OUTPUT.PUT_LINE('============================================');

FOR rec IN (
        SELECT
            ls.track_type,
            COUNT(*) as segment_count,
            SUM(ls.segment_length_km) as total_length_km
        FROM LINE_SEGMENT ls
            JOIN RAILWAY_LINE rl ON ls.line_id = rl.line_id
            JOIN OPERATOR o ON rl.owner_operator_id = o.operator_id
        WHERE o.name = p_owner_name
        GROUP BY ls.track_type
        ORDER BY ls.track_type
    ) LOOP
        DBMS_OUTPUT.PUT_LINE(rec.track_type || ': ' || rec.segment_count ||
                           ' segments (' || rec.total_length_km || ' km)');
END LOOP;

    IF SQL%NOTFOUND THEN
        DBMS_OUTPUT.PUT_LINE('No segments found for owner: ' || p_owner_name);
END IF;
END show_segments_by_owner_tracktype;
/

-- =============================================
-- TEST QUERIES - VERIFICATION
-- =============================================

-- Test 1: Basic query for all owners
SELECT
    o.name as owner_name,
    ls.track_type,
    COUNT(*) as segment_count,
    ROUND(COUNT(*) * 100.0 / SUM(COUNT(*)) OVER (PARTITION BY o.name), 2) as percentage
FROM LINE_SEGMENT ls
         JOIN RAILWAY_LINE rl ON ls.line_id = rl.line_id
         JOIN OPERATOR o ON rl.owner_operator_id = o.operator_id
GROUP BY o.name, ls.track_type
ORDER BY o.name, ls.track_type;

-- Test 2: Test the procedure
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- SEGMENTS BY OWNER AND TRACK TYPE ---');
    show_segments_by_owner_tracktype('Infraestruturas de Portugal, SA');
END;
/

-- Test 3: Test with function
DECLARE
v_cursor SYS_REFCURSOR;
    v_track_type VARCHAR2(20);
    v_segment_count NUMBER;
    v_total_length NUMBER;
BEGIN
    v_cursor := get_segments_by_owner_tracktype('Infraestruturas de Portugal, SA');

    DBMS_OUTPUT.PUT_LINE('--- FUNCTION VERSION ---');
    LOOP
FETCH v_cursor INTO v_track_type, v_segment_count, v_total_length;
        EXIT WHEN v_cursor%NOTFOUND;
        DBMS_OUTPUT.PUT_LINE(v_track_type || ': ' || v_segment_count ||
                           ' segments (' || v_total_length || ' km)');
END LOOP;
CLOSE v_cursor;
END;
/

-- =============================================
-- SUMMARY VIEW (For reporting)
-- =============================================
CREATE OR REPLACE VIEW owner_tracktype_summary AS
SELECT
    o.name as owner_name,
    ls.track_type,
    COUNT(*) as segment_count,
    SUM(ls.segment_length_km) as total_length_km,
    ROUND(AVG(ls.max_speed_kmh), 2) as avg_max_speed,
    COUNT(DISTINCT ls.line_id) as lines_affected
FROM LINE_SEGMENT ls
         JOIN RAILWAY_LINE rl ON ls.line_id = rl.line_id
         JOIN OPERATOR o ON rl.owner_operator_id = o.operator_id
GROUP BY o.name, ls.track_type
ORDER BY o.name, ls.track_type;

-- Query the summary view
SELECT * FROM owner_tracktype_summary;

-- =============================================
-- FINAL VERIFICATION
-- =============================================

-- Verify data exists for testing
SELECT
    'Total Operators' as description, COUNT(*) as value FROM OPERATOR
UNION ALL
SELECT 'Total Railway Lines', COUNT(*) FROM RAILWAY_LINE
UNION ALL
SELECT 'Total Line Segments', COUNT(*) FROM LINE_SEGMENT
UNION ALL
SELECT 'Distinct Track Types', COUNT(DISTINCT track_type) FROM LINE_SEGMENT;