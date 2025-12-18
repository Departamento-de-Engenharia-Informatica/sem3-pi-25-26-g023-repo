SET SERVEROUTPUT ON;

BEGIN
    DBMS_OUTPUT.PUT_LINE('=== INICIO DOS TESTES DE REMOÇÃO ===');

    -- TESTE 1 (SUCESSO)

    DBMS_OUTPUT.PUT_LINE('Cenário 1: Removendo frete válido (ID 2001) do comboio 5421');
    prc_remove_freight_from_train('5421', 2001);

    DBMS_OUTPUT.PUT_LINE('-------------------------------------------');

    -- TESTE 2 (NÃO PASSA)

    DBMS_OUTPUT.PUT_LINE('Cenário 2: Tentando remover frete inexistente (ID 99999)');
    prc_remove_freight_from_train('5421', 99999);

    DBMS_OUTPUT.PUT_LINE('=== FIM DOS TESTES ===');


    ROLLBACK;
    DBMS_OUTPUT.PUT_LINE('Rollback executado para manter o dataset original.');
END;
/