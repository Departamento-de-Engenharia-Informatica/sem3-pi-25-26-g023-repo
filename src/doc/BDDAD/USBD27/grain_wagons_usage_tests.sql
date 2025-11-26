-- =============================================
-- Test for USBD27 - Grain Wagons Used in Every Train That Used Grain Wagons
-- =============================================

DECLARE
-- Declare cursor variable to hold the function result
v_grain_wagons_cursor SYS_REFCURSOR;

    -- Variable to store each grain wagon ID from the cursor
    v_wagon_id TRAIN_WAGON_USAGE.wagon_id%TYPE;

    -- Counter to track number of universal grain wagons
    v_counter NUMBER := 0;
BEGIN
    DBMS_OUTPUT.PUT_LINE('=== TEST USBD27: Universal Grain Wagons (used in all grain trains) ===');
    DBMS_OUTPUT.PUT_LINE('Period: 2025-10-01 to 2025-10-07');

    -- Call the function
    v_grain_wagons_cursor := getUniversalGrainWagons();

    -- Loop through all results from the cursor
    LOOP
FETCH v_grain_wagons_cursor INTO v_wagon_id;
        EXIT WHEN v_grain_wagons_cursor%NOTFOUND;  -- Exit when no more rows

        DBMS_OUTPUT.PUT_LINE('Universal Grain Wagon ID: ' || v_wagon_id);
        v_counter := v_counter + 1;
END LOOP;

    -- Display summary
    IF v_counter = 0 THEN
        DBMS_OUTPUT.PUT_LINE('No universal grain wagons found.');

ELSE
        DBMS_OUTPUT.PUT_LINE('Total universal grain wagons: ' || v_counter);
        DBMS_OUTPUT.PUT_LINE('These wagons were used in EVERY train that transported grain.');
END IF;

    -- Always close the cursor
CLOSE v_grain_wagons_cursor;

DBMS_OUTPUT.PUT_LINE('--- End of USBD27 Test ---');

END;
