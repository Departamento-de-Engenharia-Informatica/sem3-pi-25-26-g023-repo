-- =============================================
-- USBD07 - Segments by Owner
-- =============================================

-- =============================================
-- MAIN QUERY - Interactive Version
-- =============================================
SELECT 
    o.name as "Owner",
    l.line_id as "Line ID", 
    l.name as "Line Name",
    ls.segment_id as "Segment ID",
    ls.segment_order as "Order",
    ls.electrified as "Electrified",
    ls.length_m as "Length (m)",
    ls.number_tracks as "Tracks",
    ls.max_weight_kg_m as "Max Weight (kg/m)"
FROM LINE_SEGMENT ls
JOIN RAILWAY_LINE l ON ls.line_id = l.line_id
JOIN OWNER o ON l.owner_id = o.owner_id
WHERE UPPER(o.owner_id) = UPPER('&input_owner_id')
ORDER BY l.line_id, ls.segment_order;

-- =============================================
-- ALTERNATIVE: Function Version
-- Returns results for any owner parameter
-- =============================================
CREATE OR REPLACE FUNCTION get_segments_by_owner(
    p_owner_id IN VARCHAR2
) RETURN SYS_REFCURSOR
IS
    v_result SYS_REFCURSOR;
BEGIN
    OPEN v_result FOR
        SELECT 
            o.name as owner_name,
            l.line_id,
            l.name as line_name,
            ls.segment_id,
            ls.segment_order,
            ls.electrified,
            ls.max_weight_kg_m,
            ls.length_m,
            ls.number_tracks
        FROM LINE_SEGMENT ls
        JOIN RAILWAY_LINE l ON ls.line_id = l.line_id
        JOIN OWNER o ON l.owner_id = o.owner_id
        WHERE UPPER(o.owner_id) = UPPER(p_owner_id)
        ORDER BY l.line_id, ls.segment_order;
    
    RETURN v_result;
END get_segments_by_owner;
/

-- =============================================
-- TEST QUERIES - Verification
-- =============================================

-- Test 1: Show all owners and their segment counts
SELECT 
    o.owner_id,
    o.name as owner_name,
    COUNT(DISTINCT l.line_id) as line_count,
    COUNT(ls.segment_id) as segment_count,
    ROUND(SUM(ls.length_m) / 1000, 2) as total_length_km
FROM OWNER o
LEFT JOIN RAILWAY_LINE l ON o.owner_id = l.owner_id
LEFT JOIN LINE_SEGMENT ls ON l.line_id = ls.line_id
GROUP BY o.owner_id, o.name
ORDER BY o.owner_id;

-- Test 2: Detailed view of IP's segments
SELECT 
    l.line_id,
    l.name as line_name,
    ls.segment_id,
    ls.segment_order,
    ls.electrified,
    ls.length_m,
    ls.number_tracks,
    ls.max_weight_kg_m
FROM RAILWAY_LINE l
JOIN LINE_SEGMENT ls ON l.line_id = ls.line_id
WHERE l.owner_id = 'IP'
ORDER BY l.line_id, ls.segment_order;

-- Test 3: Summary by line for IP
SELECT 
    l.line_id,
    l.name as line_name,
    COUNT(ls.segment_id) as segment_count,
    SUM(ls.length_m) as total_length_m,
    ROUND(AVG(ls.max_weight_kg_m), 2) as avg_max_weight,
    LISTAGG(ls.segment_id, ', ') WITHIN GROUP (ORDER BY ls.segment_id) as segment_ids
FROM RAILWAY_LINE l
JOIN LINE_SEGMENT ls ON l.line_id = ls.line_id
WHERE l.owner_id = 'IP'
GROUP BY l.line_id, l.name
ORDER BY l.line_id;

-- =============================================
-- COMPREHENSIVE TEST SCRIPT
-- =============================================

SET SERVEROUTPUT ON
DECLARE
    v_segment_count NUMBER;
    v_line_count NUMBER;
    v_total_length NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('🧪 TESTING USBD07 - SEGMENTS BY OWNER');
    DBMS_OUTPUT.PUT_LINE('=====================================');
    
    -- Test Owner 'IP'
    DBMS_OUTPUT.PUT_LINE('1. Testing Owner: IP');
    
    SELECT COUNT(*), COUNT(DISTINCT l.line_id), SUM(ls.length_m)
    INTO v_segment_count, v_line_count, v_total_length
    FROM LINE_SEGMENT ls
    JOIN RAILWAY_LINE l ON ls.line_id = l.line_id
    WHERE l.owner_id = 'IP';
    
    DBMS_OUTPUT.PUT_LINE('   ✅ Segments: ' || v_segment_count || ' (expected: 13)');
    DBMS_OUTPUT.PUT_LINE('   ✅ Lines: ' || v_line_count || ' (expected: 7)');
    DBMS_OUTPUT.PUT_LINE('   ✅ Total Length: ' || ROUND(v_total_length/1000, 2) || ' km');
    
    IF v_segment_count = 13 AND v_line_count = 7 THEN
        DBMS_OUTPUT.PUT_LINE('   ✅ OWNER IP TEST PASSED');
    ELSE
        DBMS_OUTPUT.PUT_LINE('   ❌ OWNER IP TEST FAILED');
    END IF;
    
    -- Test Non-existent Owner
    DBMS_OUTPUT.PUT_LINE('2. Testing Non-existent Owner: XYZ');
    
    SELECT COUNT(*)
    INTO v_segment_count
    FROM LINE_SEGMENT ls
    JOIN RAILWAY_LINE l ON ls.line_id = l.line_id
    WHERE l.owner_id = 'XYZ';
    
    DBMS_OUTPUT.PUT_LINE('   ✅ Segments: ' || v_segment_count || ' (expected: 0)');
    
    IF v_segment_count = 0 THEN
        DBMS_OUTPUT.PUT_LINE('   ✅ NON-EXISTENT OWNER TEST PASSED');
    ELSE
        DBMS_OUTPUT.PUT_LINE('   ❌ NON-EXISTENT OWNER TEST FAILED');
    END IF;
    
    DBMS_OUTPUT.PUT_LINE('=====================================');
    DBMS_OUTPUT.PUT_LINE('🎯 USBD07 READY FOR DEMONSTRATION');
    DBMS_OUTPUT.PUT_LINE('   Run main query and enter: IP');
    DBMS_OUTPUT.PUT_LINE('   Should return 13 segments across 7 lines');
    
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('❌ ERROR: ' || SQLERRM);
END;
/

-- =============================================
