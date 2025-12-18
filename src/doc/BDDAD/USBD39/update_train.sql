CREATE OR REPLACE PROCEDURE prc_associate_locomotive_to_train (
    p_train_id      IN VARCHAR2,
    p_locomotive_id IN VARCHAR2
) IS
    v_count_train NUMBER;
    v_count_loco  NUMBER;
BEGIN
    -- 1. Verifica se o comboio existe
    SELECT COUNT(*) INTO v_count_train FROM TRAIN WHERE train_id = p_train_id;

    -- 2. Verifica se a locomotiva existe
    SELECT COUNT(*) INTO v_count_loco FROM LOCOMOTIVE WHERE stock_id = p_locomotive_id;

    -- 3. Lógica de decisão
    IF v_count_train = 0 THEN
        DBMS_OUTPUT.PUT_LINE('Aviso: O comboio ' || p_train_id || ' nao existe.');

    ELSIF v_count_loco = 0 THEN
        DBMS_OUTPUT.PUT_LINE('Aviso: A locomotiva ' || p_locomotive_id || ' nao existe.');

    ELSE
        -- Se ambos existem, faz a associação
        UPDATE TRAIN
        SET locomotive_id = p_locomotive_id
        WHERE train_id = p_train_id;

        COMMIT;
        DBMS_OUTPUT.PUT_LINE('Sucesso: Locomotiva ' || p_locomotive_id || ' associada ao comboio ' || p_train_id || '.');
    END IF;

END;
/
