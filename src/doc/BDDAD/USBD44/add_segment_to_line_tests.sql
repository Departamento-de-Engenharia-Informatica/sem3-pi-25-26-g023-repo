-- =============================================
-- USBD44 - Anonymous Block Tests
-- =============================================

DELETE FROM LINE_SEGMENT WHERE segment_id IN ('TEST1', 'TEST2', 'TEST5', 'TEST6');
COMMIT;

DECLARE
v_result VARCHAR2(100);
BEGIN
    v_result := add_segment_to_line(
        p_segment_id      => 'TEST1',
        p_line_id         => 'L001',
        p_segment_order   => 10,
        p_is_electrified  => 'Yes',
        p_max_weight_kg_m => 8000,
        p_length_m        => 5000,
        p_number_tracks   => 2
    );
    DBMS_OUTPUT.PUT_LINE('Test 1 (sem desvio): ' || v_result);
END;
/

DECLARE
v_result VARCHAR2(100);
BEGIN
    v_result := add_segment_to_line(
        p_segment_id      => 'TEST2',
        p_line_id         => 'L002',
        p_segment_order   => 5,
        p_is_electrified  => 'Yes',
        p_max_weight_kg_m => 7500,
        p_length_m        => 3000,
        p_number_tracks   => 1,
        p_siding_position => 1500,
        p_siding_length   => 500
    );
    DBMS_OUTPUT.PUT_LINE('Test 2 (com desvio): ' || v_result);
END;
/

DECLARE
v_result VARCHAR2(100);
BEGIN
    v_result := add_segment_to_line(
        p_segment_id      => 'TEST3',
        p_line_id         => 'L999',
        p_segment_order   => 1,
        p_is_electrified  => 'Yes',
        p_max_weight_kg_m => 8000,
        p_length_m        => 4000,
        p_number_tracks   => 2
    );
    DBMS_OUTPUT.PUT_LINE('Test 3 (linha não existe): ' || v_result);
END;
/

DECLARE
v_result VARCHAR2(100);
BEGIN
    v_result := add_segment_to_line(
        p_segment_id      => 'TEST1',
        p_line_id         => 'L001',
        p_segment_order   => 20,
        p_is_electrified  => 'Yes',
        p_max_weight_kg_m => 8000,
        p_length_m        => 6000,
        p_number_tracks   => 2
    );
    DBMS_OUTPUT.PUT_LINE('Test 4 (segmento já existe): ' || v_result);
END;
/

DECLARE
v_result VARCHAR2(100);
BEGIN
    v_result := add_segment_to_line(
        p_segment_id      => 'TEST5',
        p_line_id         => 'L001',
        p_segment_order   => 15,
        p_is_electrified  => 'Yes',
        p_max_weight_kg_m => 8000,
        p_length_m        => 4000,
        p_number_tracks   => 1,
        p_siding_position => 2000,
        p_siding_length   => NULL
    );
    DBMS_OUTPUT.PUT_LINE('Test 5 (desvio incompleto): ' || v_result);
END;
/

DECLARE
v_result VARCHAR2(100);
BEGIN
    v_result := add_segment_to_line(
        p_segment_id      => 'TEST6',
        p_line_id         => 'L001',
        p_segment_order   => 16,
        p_is_electrified  => 'Yes',
        p_max_weight_kg_m => 8000,
        p_length_m        => 4000,
        p_number_tracks   => 1,
        p_siding_position => -100,
        p_siding_length   => 500
    );
    DBMS_OUTPUT.PUT_LINE('Test 6 (desvio negativo): ' || v_result);
END;
/

DECLARE
v_result VARCHAR2(100);
BEGIN
    v_result := add_segment_to_line(
        p_segment_id      => 'TEST7',
        p_line_id         => 'L001',
        p_segment_order   => 17,
        p_is_electrified  => 'Yes',
        p_max_weight_kg_m => 8000,
        p_length_m        => -100,
        p_number_tracks   => 2,
        p_siding_position => NULL,
        p_siding_length   => NULL
    );
    DBMS_OUTPUT.PUT_LINE('Test 7 (comprimento negativo): ' || v_result);
END;
/

DECLARE
v_result VARCHAR2(100);
BEGIN
    v_result := add_segment_to_line(
        p_segment_id      => 'TEST8',
        p_line_id         => 'L001',
        p_segment_order   => 18,
        p_is_electrified  => 'Maybe',
        p_max_weight_kg_m => 8000,
        p_length_m        => 1000,
        p_number_tracks   => 2,
        p_siding_position => NULL,
        p_siding_length   => NULL
    );
    DBMS_OUTPUT.PUT_LINE('Test 8 (eletrificação inválida): ' || v_result);
END;
/

DECLARE
v_result VARCHAR2(100);
BEGIN
    v_result := add_segment_to_line(
        p_segment_id      => 'TEST9',
        p_line_id         => 'L001',
        p_segment_order   => 19,
        p_is_electrified  => 'Yes',
        p_max_weight_kg_m => 8000,
        p_length_m        => 1000,
        p_number_tracks   => 3,
        p_siding_position => NULL,
        p_siding_length   => NULL
    );
    DBMS_OUTPUT.PUT_LINE('Test 9 (número de vias inválido): ' || v_result);
END;
/