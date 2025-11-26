-- =============================================
-- Test for USBD28 - Locomotives with Multiple Gauges
-- =============================================

DECLARE
-- Declare cursor variable to hold the function result
v_loc_cursor SYS_REFCURSOR;

    -- Variables to store locomotive details from the cursor
    v_loc_id LOCOMOTIVE.stock_id%TYPE;
    v_operator OPERATOR.name%TYPE;
    v_loc_type LOCOMOTIVE.locomotive_type%TYPE;
    v_power LOCOMOTIVE.power_kw%TYPE;
    v_supports_multi VARCHAR2(3);

    -- Counter to track number of multi-gauge locomotives
    v_counter NUMBER := 0;
BEGIN
    DBMS_OUTPUT.PUT_LINE('=== TEST USBD28: Multi-Gauge Locomotives for Operator MEDWAY ===');

    -- Call the function
    v_loc_cursor := getMultiGaugeLocomotives();

    -- Loop through all results from the cursor
    LOOP
FETCH v_loc_cursor INTO v_loc_id, v_operator, v_loc_type, v_power, v_supports_multi;
        EXIT WHEN v_loc_cursor%NOTFOUND;  -- Exit when no more rows

        -- Display detailed information for each locomotive
        DBMS_OUTPUT.PUT_LINE('Locomotive Details:');
        DBMS_OUTPUT.PUT_LINE('  ID: ' || v_loc_id);
        DBMS_OUTPUT.PUT_LINE('  Operator: ' || v_operator);
        DBMS_OUTPUT.PUT_LINE('  Type: ' || v_loc_type);
        DBMS_OUTPUT.PUT_LINE('  Power: ' || v_power || ' kW');
        DBMS_OUTPUT.PUT_LINE('  Multi-Gauge Support: ' || v_supports_multi);
        DBMS_OUTPUT.PUT_LINE('  ---');

        v_counter := v_counter + 1;
END LOOP;

    -- Display summary
    IF v_counter = 0 THEN
        DBMS_OUTPUT.PUT_LINE('No multi-gauge locomotives found for operator MEDWAY.');
ELSE
        DBMS_OUTPUT.PUT_LINE('Total multi-gauge locomotives for MEDWAY: ' || v_counter);
END IF;

    -- Always close the cursor
CLOSE v_loc_cursor;

DBMS_OUTPUT.PUT_LINE('--- End of USBD28 Test ---');
END;
/