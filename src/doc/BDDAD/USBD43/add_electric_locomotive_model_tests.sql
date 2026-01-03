SET SERVEROUTPUT ON;

-- =============================================
-- USBD43 - Anonymous Block Tests
-- =============================================

-- Limpeza prévia para permitir reexecução do ficheiro
DELETE FROM LOCOMOTIVE WHERE stock_id IN ('EL_TEST_1', 'EL_TEST_2');
DELETE FROM ROLLING_STOCK WHERE stock_id IN ('EL_TEST_1', 'EL_TEST_2');
COMMIT;

-- =============================================
-- Test 1: Criação válida de locomotiva elétrica
-- =============================================
DECLARE
v_stock_id   VARCHAR2(20) := 'EL_TEST_1';
    v_check_type VARCHAR2(20);
    v_counter    NUMBER := 0;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- USBD43 Test 1: Valid Electric Locomotive Creation ---');

    add_electric_locomotive_model(
        p_stock_id      => v_stock_id,
        p_operator_id   => 'MEDWAY',   -- operador existente
        p_model         => 'Siemens Vectron',
        p_gauge_mm      => 1435,
        p_power_kw      => 6400,
        p_length_m      => 19
    );

    -- Verificação do resultado
SELECT locomotive_type
INTO v_check_type
FROM LOCOMOTIVE
WHERE stock_id = v_stock_id;

IF v_check_type = 'Electric' THEN
        DBMS_OUTPUT.PUT_LINE('Success: Electric locomotive created correctly');
        v_counter := 1;
ELSE
        DBMS_OUTPUT.PUT_LINE('Failure: Locomotive type is incorrect');
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
-- Test 2: Operador inexistente
-- =============================================
DECLARE
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- USBD43 Test 2: Operator Does Not Exist ---');

    add_electric_locomotive_model(
        p_stock_id      => 'EL_TEST_2',
        p_operator_id   => 'OP999',     -- operador inexistente
        p_model         => 'Alstom Prima',
        p_gauge_mm      => 1435,
        p_power_kw      => 5000,
        p_length_m      => 18
    );

    DBMS_OUTPUT.PUT_LINE('Failure: Locomotive should not have been created');

EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Success: Correctly rejected -> ' || SQLERRM);
END;
/

-- =============================================
-- Test 3: Stock ID duplicado
-- =============================================
DECLARE
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- USBD43 Test 3: Duplicate Stock ID ---');

    add_electric_locomotive_model(
        p_stock_id      => 'EL_TEST_1', -- já criado no Teste 1
        p_operator_id   => 'MEDWAY',
        p_model         => 'Bombardier TRAXX',
        p_gauge_mm      => 1435,
        p_power_kw      => 5600,
        p_length_m      => 19
    );

    DBMS_OUTPUT.PUT_LINE('Failure: Duplicate stock_id should not be allowed');

EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Success: Duplicate correctly rejected -> ' || SQLERRM);
END;
/
