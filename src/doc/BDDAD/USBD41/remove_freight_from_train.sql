CREATE OR REPLACE PROCEDURE prc_remove_freight_from_train (
    p_train_id   IN VARCHAR2,
    p_freight_id IN NUMBER
) IS
BEGIN

    DELETE FROM TRAIN_WAGON_USAGE
    WHERE train_id = p_train_id
    AND wagon_id IN (
        SELECT wagon_id FROM FREIGHT_WAGON WHERE freight_id = p_freight_id
    );

    -- Mensagem de confirmação
    IF SQL%ROWCOUNT > 0 THEN
        DBMS_OUTPUT.PUT_LINE('Sucesso: O frete foi removido do comboio.');
        COMMIT;
    ELSE
        DBMS_OUTPUT.PUT_LINE('Aviso: Nao foram encontrados dados para remover.');
    END IF;
END;
/