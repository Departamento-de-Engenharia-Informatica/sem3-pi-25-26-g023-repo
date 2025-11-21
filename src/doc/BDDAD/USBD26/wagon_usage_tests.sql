-- =============================================
-- Test for USBD26 - Wagons Not Used in Any Train in Given Period
-- =============================================

DECLARE
-- Declare cursor variable to hold the function result
v_wagons_cursor SYS_REFCURSOR;

    -- Variable to store each wagon ID from the cursor
    v_wagon_id ROLLING_STOCK.stock_id%TYPE;

    -- Counter to track number of results
    v_counter NUMBER := 0;
BEGIN
    DBMS_OUTPUT.PUT_LINE('=== TEST USBD26: Wagons not used between 2025-10-01 and 2025-10-02 ===');

    -- Call the function with test period
    v_wagons_cursor := getUnusedWagonsInPeriod('2025-10-01', '2025-10-02');

    -- Loop through all results from the cursor
    LOOP
FETCH v_wagons_cursor INTO v_wagon_id;
        EXIT WHEN v_wagons_cursor%NOTFOUND;  -- Exit when no more rows

        DBMS_OUTPUT.PUT_LINE('Unused Wagon ID: ' || v_wagon_id);
        v_counter := v_counter + 1;
END LOOP;

    -- Display summary
    IF v_counter = 0 THEN
        DBMS_OUTPUT.PUT_LINE('No unused wagons found in the specified period.');
ELSE
        DBMS_OUTPUT.PUT_LINE('Total unused wagons: ' || v_counter);
END IF;

    -- Always close the cursor
CLOSE v_wagons_cursor;

DBMS_OUTPUT.PUT_LINE('--- End of USBD26 Test ---');

EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Error during USBD26 test: ' || SQLERRM);
        -- Ensure cursor is closed even if error occurs
        IF v_wagons_cursor%ISOPEN THEN
            CLOSE v_wagons_cursor;
END IF;
END;
/