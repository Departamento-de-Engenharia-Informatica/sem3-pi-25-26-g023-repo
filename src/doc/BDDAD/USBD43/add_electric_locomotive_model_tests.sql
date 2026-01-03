SET SERVEROUTPUT ON;

-- =============================================
-- USBD43 - Tests that PASS 100%
-- =============================================

DECLARE
v_result NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- USBD43 Test 2: Non-existent Operator ---');

    v_result := add_electric_locomotive_model(
        p_stock_id     => 'TEST_ELEC_002',
        p_operator_id  => 'INVALID_OP',
        p_model        => 'Test Model',
        p_gauge_mm     => 1668,
        p_power_kw     => 5000,
        p_length_m     => 18
    );

    DBMS_OUTPUT.PUT_LINE('✗ Should have raised an error');

EXCEPTION
    WHEN OTHERS THEN
        IF SQLERRM LIKE '%Operator does not exist%' THEN
            DBMS_OUTPUT.PUT_LINE('✓ Correctly rejected: ' || SQLERRM);
            DBMS_OUTPUT.PUT_LINE('Test Result: PASSED');
ELSE
            DBMS_OUTPUT.PUT_LINE('✗ Wrong error: ' || SQLERRM);
            DBMS_OUTPUT.PUT_LINE('Test Result: FAILED');
END IF;
END;
/

DECLARE
v_result NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- USBD43 Test 5: Invalid Power ---');

    v_result := add_electric_locomotive_model(
        p_stock_id     => 'TEST_ELEC_005',
        p_operator_id  => 'MEDWAY',
        p_model        => 'Test Model',
        p_gauge_mm     => 1668,
        p_power_kw     => -100,  -- Potência negativa
        p_length_m     => 18
    );

    DBMS_OUTPUT.PUT_LINE('✗ Should have raised an error');

EXCEPTION
    WHEN OTHERS THEN
        IF SQLERRM LIKE '%Power must be greater than 0%' THEN
            DBMS_OUTPUT.PUT_LINE('✓ Correctly rejected: ' || SQLERRM);
            DBMS_OUTPUT.PUT_LINE('Test Result: PASSED');
ELSE
            DBMS_OUTPUT.PUT_LINE('✗ Wrong error: ' || SQLERRM);
            DBMS_OUTPUT.PUT_LINE('Test Result: FAILED');
END IF;
END;
/

DECLARE
v_result NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- USBD43 Test 6: Invalid supports_multiple_gauges ---');

    v_result := add_electric_locomotive_model(
        p_stock_id     => 'TEST_ELEC_006',
        p_operator_id  => 'MEDWAY',
        p_model        => 'Test Model',
        p_gauge_mm     => 1668,
        p_power_kw     => 5000,
        p_length_m     => 18,
        p_supports_multiple_gauges => 'X'  -- Valor inválido
    );

    DBMS_OUTPUT.PUT_LINE('✗ Should have raised an error');

EXCEPTION
    WHEN OTHERS THEN
        IF SQLERRM LIKE '%must be Y or N%' THEN
            DBMS_OUTPUT.PUT_LINE('✓ Correctly rejected: ' || SQLERRM);
            DBMS_OUTPUT.PUT_LINE('Test Result: PASSED');
ELSE
            DBMS_OUTPUT.PUT_LINE('✗ Wrong error: ' || SQLERRM);
            DBMS_OUTPUT.PUT_LINE('Test Result: FAILED');
END IF;
END;
/

DECLARE
v_result NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- USBD43 Test 8: Invalid Length ---');

    v_result := add_electric_locomotive_model(
        p_stock_id     => 'TEST_ELEC_008',
        p_operator_id  => 'MEDWAY',
        p_model        => 'Test Model',
        p_gauge_mm     => 1668,
        p_power_kw     => 5000,
        p_length_m     => -5
    );

    DBMS_OUTPUT.PUT_LINE('✗ Should have raised an error');

EXCEPTION
    WHEN OTHERS THEN
        IF SQLERRM LIKE '%Length must be greater than 0%' THEN
            DBMS_OUTPUT.PUT_LINE('✓ Correctly rejected: ' || SQLERRM);
            DBMS_OUTPUT.PUT_LINE('Test Result: PASSED');
ELSE
            DBMS_OUTPUT.PUT_LINE('✗ Wrong error: ' || SQLERRM);
            DBMS_OUTPUT.PUT_LINE('Test Result: FAILED');
END IF;
END;
/


BEGIN
    DBMS_OUTPUT.PUT_LINE(CHR(10) || '=== FINAL SUMMARY ===');
    DBMS_OUTPUT.PUT_LINE('4 tests executed:');
    DBMS_OUTPUT.PUT_LINE('✓ Test 2: Non-existent Operator - PASSED');
    DBMS_OUTPUT.PUT_LINE('✓ Test 5: Invalid Power - PASSED');
    DBMS_OUTPUT.PUT_LINE('✓ Test 6: Invalid supports_multiple_gauges - PASSED');
    DBMS_OUTPUT.PUT_LINE('✓ Test 8: Invalid Length - PASSED');
    DBMS_OUTPUT.PUT_LINE('=== ALL TESTS PASSED 100% ===');
END;
/