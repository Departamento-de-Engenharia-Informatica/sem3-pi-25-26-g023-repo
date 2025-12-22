-- =============================================
-- USBD38 - Anonymous Block Tests
-- =============================================

DECLARE
v_result VARCHAR2(500);
BEGIN
    v_result := add_new_gauge(1000, 'METRIC_GAUGE', 'Metric gauge');
    DBMS_OUTPUT.PUT_LINE('Test 1: ' || v_result);
END;
/

DECLARE
v_result VARCHAR2(500);
BEGIN
    v_result := add_new_gauge(1668, 'NEW_NAME', 'Duplicate value');
    DBMS_OUTPUT.PUT_LINE('Test 2: ' || v_result);
END;
/

DECLARE
v_result VARCHAR2(500);
BEGIN
    v_result := add_new_gauge(1500, 'STANDARD', 'Duplicate name');
    DBMS_OUTPUT.PUT_LINE('Test 3: ' || v_result);
END;
/

DECLARE
v_result VARCHAR2(500);
BEGIN
    v_result := add_new_gauge(300, 'NARROW', 'Too small');
    DBMS_OUTPUT.PUT_LINE('Test 4: ' || v_result);
END;
/

DECLARE
v_result VARCHAR2(500);
BEGIN
    v_result := add_new_gauge(NULL, 'TEST', 'NULL value');
    DBMS_OUTPUT.PUT_LINE('Test 5: ' || v_result);
END;
/

DECLARE
v_result VARCHAR2(500);
BEGIN
    v_result := add_new_gauge(2000, NULL, 'NULL name');
    DBMS_OUTPUT.PUT_LINE('Test 6: ' || v_result);
END;
/

DECLARE
v_result VARCHAR2(500);
BEGIN
    v_result := add_new_gauge(1520, 'RUSSIAN', NULL);
    DBMS_OUTPUT.PUT_LINE('Test 7: ' || v_result);
END;
/