SET SERVEROUTPUT ON;

BEGIN
DELETE FROM BUILDING WHERE building_id IN (1001, 1002, 1003);
COMMIT;

INSERT INTO STATION (station_id, name, latitude, longitude)
SELECT 'ST100', 'Test Station', 0, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM STATION WHERE station_id = 'ST100');

INSERT INTO FACILITY (facility_id, name, station_id)
SELECT 100, 'Test Facility', 'ST100' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM FACILITY WHERE facility_id = 100);

COMMIT;
END;
/

DECLARE
v_result NUMBER;
BEGIN
    v_result := add_building_to_facility(1001, 'Warehouse A', 'Warehouse', 100);
    IF v_result = 1 THEN
        DBMS_OUTPUT.PUT_LINE('Test 1 PASSED');
COMMIT;
END IF;
END;
/

DECLARE
v_result NUMBER;
BEGIN
    v_result := add_building_to_facility(1002, 'Building B', 'Office', 99999);
    DBMS_OUTPUT.PUT_LINE('Test 2 FAILED: ' || v_result);
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Test 2 PASSED');
END;
/

DECLARE
v_result NUMBER;
BEGIN
    v_result := add_building_to_facility(1001, 'Building C', 'Office', 100);
    DBMS_OUTPUT.PUT_LINE('Test 3 FAILED: ' || v_result);
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Test 3 PASSED');
END;
/

DECLARE
v_result NUMBER;
BEGIN
    v_result := add_building_to_facility(1003, 'Office D', 'Office', 100);
    IF v_result = 1 THEN
        DBMS_OUTPUT.PUT_LINE('Test 4 PASSED');
COMMIT;
END IF;
END;
/

DECLARE
v_count NUMBER;
BEGIN
SELECT COUNT(*) INTO v_count
FROM BUILDING
WHERE building_id IN (1001, 1003);

DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('Buildings created: ' || v_count);
    DBMS_OUTPUT.PUT_LINE('');

    IF v_count = 2 THEN
        DBMS_OUTPUT.PUT_LINE('✅ ALL TESTS PASSED');
ELSE
        DBMS_OUTPUT.PUT_LINE('❌ TESTS FAILED');
END IF;
END;
/