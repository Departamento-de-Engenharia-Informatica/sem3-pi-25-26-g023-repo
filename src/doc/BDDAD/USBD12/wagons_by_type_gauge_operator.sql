-- =============================================
-- USBD12 - Wagons by Type, Gauge and Operator
-- =============================================

-- =============================================
-- MAIN QUERY - Interactive Version
-- Prompts user for type_name, gauge_name and operator_name input
-- =============================================
SELECT
    w.wagon_id,
    w.wagon_number,
    o.operator_name,
    wt.type_name,
    g.gauge_name,
    w.payload,
    w.volume_capacity,
    w.dimensions,
    w.build_year,
    w.status
FROM WAGON w
         JOIN OPERATOR o ON w.operator_id = o.operator_id
         JOIN WAGON_TYPE wt ON w.type_id = wt.type_id
         JOIN GAUGE g ON w.gauge_id = g.gauge_id
WHERE UPPER(wt.type_name) = UPPER('&input_type_name')
  AND UPPER(g.gauge_name) = UPPER('&input_gauge_name')
  AND UPPER(o.operator_name) = UPPER('&input_operator_name')
ORDER BY w.wagon_number;

-- =============================================
-- ALTERNATIVE: Function Version
-- Returns results for type, gauge and operator parameters
-- =============================================
CREATE OR REPLACE FUNCTION get_wagons_by_type_gauge_operator(
    p_type_name IN VARCHAR2,
    p_gauge_name IN VARCHAR2,
    p_operator_name IN VARCHAR2
) RETURN SYS_REFCURSOR
IS
    v_result SYS_REFCURSOR;
BEGIN
OPEN v_result FOR
SELECT
    w.wagon_id,
    w.wagon_number,
    o.operator_name,
    wt.type_name,
    g.gauge_name,
    w.payload,
    w.volume_capacity,
    w.dimensions,
    w.build_year,
    w.status
FROM WAGON w
         JOIN OPERATOR o ON w.operator_id = o.operator_id
         JOIN WAGON_TYPE wt ON w.type_id = wt.type_id
         JOIN GAUGE g ON w.gauge_id = g.gauge_id
WHERE UPPER(wt.type_name) = UPPER(p_type_name)
  AND UPPER(g.gauge_name) = UPPER(p_gauge_name)
  AND UPPER(o.operator_name) = UPPER(p_operator_name)
ORDER BY w.wagon_number;

RETURN v_result;
END get_wagons_by_type_gauge_operator;
/

-- =============================================
-- TEST QUERIES - Verification for all combinations
-- =============================================

-- Test Query 1: Show all operators with their wagons summary
SELECT
    o.operator_name,
    COUNT(w.wagon_id) as total_wagons,
    LISTAGG(DISTINCT wt.type_name, ', ') WITHIN GROUP (ORDER BY wt.type_name) as wagon_types,
    LISTAGG(DISTINCT g.gauge_name, ', ') WITHIN GROUP (ORDER BY g.gauge_name) as gauges
FROM OPERATOR o
    LEFT JOIN WAGON w ON o.operator_id = w.operator_id
    LEFT JOIN WAGON_TYPE wt ON w.type_id = wt.type_id
    LEFT JOIN GAUGE g ON w.gauge_id = g.gauge_id
GROUP BY o.operator_id, o.operator_name
ORDER BY o.operator_name;

-- Test Query 2: Detailed breakdown for ALL wagon type/gauge/operator combinations
SELECT
    o.operator_name,
    wt.type_name,
    g.gauge_name,
    COUNT(w.wagon_id) as wagon_count,
    LISTAGG(w.wagon_number, ', ') WITHIN GROUP (ORDER BY w.wagon_number) as wagon_numbers
FROM OPERATOR o
    JOIN WAGON w ON o.operator_id = w.operator_id
    JOIN WAGON_TYPE wt ON w.type_id = wt.type_id
    JOIN GAUGE g ON w.gauge_id = g.gauge_id
GROUP BY o.operator_name, wt.type_name, g.gauge_name
ORDER BY o.operator_name, wt.type_name, g.gauge_name;

-- =============================================
-- SPECIFIC COMBINATION TESTS
-- =============================================

-- Test 1: Boxcar + Ibérica + Medway Portugal
SELECT 'Test 1: Boxcar, Ibérica, Medway Portugal' as test_case FROM DUAL;
SELECT
    w.wagon_number,
    w.payload,
    w.volume_capacity,
    w.build_year,
    w.status
FROM WAGON w
         JOIN OPERATOR o ON w.operator_id = o.operator_id
         JOIN WAGON_TYPE wt ON w.type_id = wt.type_id
         JOIN GAUGE g ON w.gauge_id = g.gauge_id
WHERE UPPER(wt.type_name) = 'BOXCAR'
  AND UPPER(g.gauge_name) = 'IBÉRICA'
  AND UPPER(o.operator_name) = 'MEDWAY PORTUGAL'
ORDER BY w.wagon_number;

-- Test 2: Flatcar + Standard + CP Carga
SELECT 'Test 2: Flatcar, Standard, CP Carga' as test_case FROM DUAL;
SELECT
    w.wagon_number,
    w.payload,
    w.volume_capacity,
    w.build_year,
    w.status
FROM WAGON w
         JOIN OPERATOR o ON w.operator_id = o.operator_id
         JOIN WAGON_TYPE wt ON w.type_id = wt.type_id
         JOIN GAUGE g ON w.gauge_id = g.gauge_id
WHERE UPPER(wt.type_name) = 'FLATCAR'
  AND UPPER(g.gauge_name) = 'STANDARD'
  AND UPPER(o.operator_name) = 'CP CARGA'
ORDER BY w.wagon_number;

-- Test 3: Tank Car + Ibérica + Medway Portugal
SELECT 'Test 3: Tank Car, Ibérica, Medway Portugal' as test_case FROM DUAL;
SELECT
    w.wagon_number,
    w.payload,
    w.volume_capacity,
    w.build_year,
    w.status
FROM WAGON w
         JOIN OPERATOR o ON w.operator_id = o.operator_id
         JOIN WAGON_TYPE wt ON w.type_id = wt.type_id
         JOIN GAUGE g ON w.gauge_id = g.gauge_id
WHERE UPPER(wt.type_name) = 'TANK CAR'
  AND UPPER(g.gauge_name) = 'IBÉRICA'
  AND UPPER(o.operator_name) = 'MEDWAY PORTUGAL'
ORDER BY w.wagon_number;

-- Test 4: Hopper Car + Standard + CP Carga
SELECT 'Test 4: Hopper Car, Standard, CP Carga' as test_case FROM DUAL;
SELECT
    w.wagon_number,
    w.payload,
    w.volume_capacity,
    w.build_year,
    w.status
FROM WAGON w
         JOIN OPERATOR o ON w.operator_id = o.operator_id
         JOIN WAGON_TYPE wt ON w.type_id = wt.type_id
         JOIN GAUGE g ON w.gauge_id = g.gauge_id
WHERE UPPER(wt.type_name) = 'HOPPER CAR'
  AND UPPER(g.gauge_name) = 'STANDARD'
  AND UPPER(o.operator_name) = 'CP CARGA'
ORDER BY w.wagon_number;

-- =============================================
-- COUNT SUMMARY BY COMBINATIONS
-- =============================================

-- Summary of wagons by type, gauge and operator
SELECT
    o.operator_name,
    wt.type_name,
    g.gauge_name,
    COUNT(*) as wagon_count
FROM WAGON w
         JOIN OPERATOR o ON w.operator_id = o.operator_id
         JOIN WAGON_TYPE wt ON w.type_id = wt.type_id
         JOIN GAUGE g ON w.gauge_id = g.gauge_id
GROUP BY o.operator_name, wt.type_name, g.gauge_name
ORDER BY o.operator_name, wt.type_name, g.gauge_name;

-- =============================================
-- EMPTY RESULT TEST (non-existing combination)
-- =============================================

-- Test for combination that should return no results
SELECT 'Empty Result Test: Refrigerated Car, Métrica, Medway Portugal' as test_case FROM DUAL;
SELECT
    w.wagon_number,
    w.payload,
    w.volume_capacity,
    w.build_year,
    w.status
FROM WAGON w
         JOIN OPERATOR o ON w.operator_id = o.operator_id
         JOIN WAGON_TYPE wt ON w.type_id = wt.type_id
         JOIN GAUGE g ON w.gauge_id = g.gauge_id
WHERE UPPER(wt.type_name) = 'REFRIGERATED CAR'
  AND UPPER(g.gauge_name) = 'MÉTRICA'
  AND UPPER(o.operator_name) = 'MEDWAY PORTUGAL'
ORDER BY w.wagon_number;