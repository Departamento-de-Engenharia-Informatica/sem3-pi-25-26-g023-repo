SET SERVEROUTPUT ON;

DECLARE
    -- Variáveis para validação dos resultados
    v_train_id      VARCHAR2(10) := '5437';  -- ID de um comboio que existe no teu dataset
    v_loco_id       VARCHAR2(20) := '5034';  -- ID de uma locomotiva que existe
    v_check_loco    VARCHAR2(20);
    v_counter       NUMBER := 0;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- Starting USBD39 Test: Associate Locomotive ---');

    -- 1. Executa o procedimento que criamos
    prc_associate_locomotive_to_train(v_train_id, v_loco_id);

    -- 2. Verifica se a alteração foi gravada com sucesso na base de dados
    SELECT locomotive_id
    INTO v_check_loco
    FROM TRAIN
    WHERE train_id = v_train_id;

    -- 3. Validação do resultado (O "Assert" do teste)
    IF v_check_loco = v_loco_id THEN
        DBMS_OUTPUT.PUT_LINE('Success: Train ' || v_train_id || ' is now correctly linked to Loco ' || v_check_loco);
        v_counter := 1;
    ELSE
        DBMS_OUTPUT.PUT_LINE('Failure: The locomotive ID does not match after update.');
    END IF;

    -- Resumo final do teste
    IF v_counter = 1 THEN
        DBMS_OUTPUT.PUT_LINE('Test Result: PASSED');
    ELSE
        DBMS_OUTPUT.PUT_LINE('Test Result: FAILED');
    END IF;

    DBMS_OUTPUT.PUT_LINE('--- End of USBD39 Test ---');

EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Error during USBD39 test: ' || SQLERRM);
        ROLLBACK;
END;
/