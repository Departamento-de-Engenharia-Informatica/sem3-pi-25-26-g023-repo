SET SERVEROUTPUT ON;

-- =============================================
-- USBD36 - Anonymous Block Tests
-- =============================================

-- Limpeza prévia para permitir reexecução do ficheiro
DELETE FROM BUILDING WHERE building_id IN (1001, 1002);
COMMIT;

-- =============================================
-- Test 1: Criação válida de Building
-- =============================================
DECLARE
v_building_id NUMBER := 1001;
    v_facility_id NUMBER := 1;  -- Facility existente (ajustar se necessário)
    v_check_fac   NUMBER;
    v_counter     NUMBER := 0;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- USBD36 Test 1: Valid Building Creation ---');

    -- Executar função
    add_building_to_facility(
        p_building_id   => v_building_id,
        p_name          => 'Main Warehouse',
        p_building_type => 'Warehouse',
        p_facility_id   => v_facility_id
    );

    -- Verificação do resultado
SELECT facility_id
INTO v_check_fac
FROM BUILDING
WHERE building_id = v_building_id;

IF v_check_fac = v_facility_id THEN
        DBMS_OUTPUT.PUT_LINE('Success: Building correctly associated to Facility ' || v_facility_id);
        v_counter := 1;
ELSE
        DBMS_OUTPUT.PUT_LINE('Failure: Incorrect facility association');
END IF;

    IF v_counter = 1 THEN
        DBMS_OUTPUT.PUT_LINE('Test Result: PASSED');
ELSE
        DBMS_OUTPUT.PUT_LINE('Test Result: FAILED');
END IF;

    DBMS_OUTPUT.PUT_LINE('--- End of Test 1 ---');

EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Error during Test 1: ' || SQLERRM);
ROLLBACK;
END;
/

-- =============================================
-- Test 2: Facility inexistente
-- =============================================
DECLARE
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- USBD36 Test 2: Facility Does Not Exist ---');

    add_building_to_facility(
        p_building_id   => 1002,
        p_name          => 'Invalid Building',
        p_building_type => 'Terminal',
        p_facility_id   => 9999  -- Facility inexistente
    );

    DBMS_OUTPUT.PUT_LINE('Failure: Building should not have been created');

EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Success: Correctly rejected -> ' || SQLERRM);
END;
/

-- =============================================
-- Test 3: Building duplicado (PK)
-- =============================================
DECLARE
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- USBD36 Test 3: Duplicate Building ID ---');

    add_building_to_facility(
        p_building_id   => 1001, -- Já criado no Teste 1
        p_name          => 'Duplicate Building',
        p_building_type => 'Warehouse',
        p_facility_id   => 1
    );

    DBMS_OUTPUT.PUT_LINE('Failure: Duplicate building ID should not be allowed');

EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Success: Duplicate correctly rejected -> ' || SQLERRM);
END;
/
