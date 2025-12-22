-- =============================================
-- USBD33 - CORRECTED Test Cases
-- =============================================

DECLARE
v_result VARCHAR2(200);
BEGIN
INSERT INTO TRAIN (train_id, operator_id, max_length_m, locomotive_id)
VALUES ('TEST33', 'MEDWAY', 50, '5621');

DBMS_OUTPUT.PUT_LINE('Teste 1: Comboio criado (máx 50m, locomotiva 20m)');

INSERT INTO TRAIN_WAGON_USAGE (usage_id, train_id, wagon_id, usage_date)
VALUES ('T33-01', 'TEST33', '356 3 077', SYSDATE);
DBMS_OUTPUT.PUT_LINE('✓ 1º vagão adicionado (Total: 40m)');

BEGIN
INSERT INTO TRAIN_WAGON_USAGE (usage_id, train_id, wagon_id, usage_date)
VALUES ('T33-02', 'TEST33', '356 3 078', SYSDATE);
DBMS_OUTPUT.PUT_LINE('✗ ERRO: Deveria ter falhado!');
EXCEPTION
        WHEN OTHERS THEN
            IF SQLERRM LIKE '%exceed maximum length%' THEN
                DBMS_OUTPUT.PUT_LINE('✓ Corretamente rejeitado: ' || SUBSTR(SQLERRM, 1, 100));
ELSE
                DBMS_OUTPUT.PUT_LINE('✗ Erro diferente: ' || SQLERRM);
END IF;
END;

DELETE FROM TRAIN_WAGON_USAGE WHERE train_id = 'TEST33';
DELETE FROM TRAIN WHERE train_id = 'TEST33';
END;
/

DECLARE
BEGIN
INSERT INTO TRAIN (train_id, operator_id, max_length_m, locomotive_id)
VALUES ('TEST34', 'MEDWAY', 60, '5621');

INSERT INTO TRAIN_WAGON_USAGE (usage_id, train_id, wagon_id, usage_date)
VALUES ('T34-01', 'TEST34', '082 3 045', SYSDATE);

DBMS_OUTPUT.PUT_LINE('Teste 2: UPDATE de vagão');
    DBMS_OUTPUT.PUT_LINE('Comboio: 20m (loc) + 15m (vagão) = 35m (máx 60m)');

BEGIN
UPDATE TRAIN_WAGON_USAGE
SET wagon_id = '356 3 077'
WHERE usage_id = 'T34-01';
DBMS_OUTPUT.PUT_LINE('✓ UPDATE permitido (ainda dentro do limite)');
EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Erro: ' || SQLERRM);
END;

DELETE FROM TRAIN_WAGON_USAGE WHERE train_id = 'TEST34';
DELETE FROM TRAIN WHERE train_id = 'TEST34';
END;
/

DECLARE
BEGIN
    DBMS_OUTPUT.PUT_LINE('Teste 3: UPDATE que excede limite');

INSERT INTO TRAIN (train_id, operator_id, max_length_m, locomotive_id)
VALUES ('TEST35', 'MEDWAY', 55, '5621');

INSERT INTO TRAIN_WAGON_USAGE (usage_id, train_id, wagon_id, usage_date)
VALUES ('T35-01', 'TEST35', '082 3 045', SYSDATE);
INSERT INTO TRAIN_WAGON_USAGE (usage_id, train_id, wagon_id, usage_date)
VALUES ('T35-02', 'TEST35', '356 3 077', SYSDATE);

DBMS_OUTPUT.PUT_LINE('Comboio: 20m (loc) + 15m + 20m = 55m (máx 55m)');

BEGIN
UPDATE TRAIN_WAGON_USAGE
SET wagon_id = '356 3 078'
WHERE usage_id = 'T35-01';
DBMS_OUTPUT.PUT_LINE('✗ UPDATE deveria ter falhado!');
EXCEPTION
        WHEN OTHERS THEN
            IF SQLERRM LIKE '%exceed maximum length%' THEN
                DBMS_OUTPUT.PUT_LINE('✓ UPDATE corretamente rejeitado');
ELSE
                DBMS_OUTPUT.PUT_LINE('Erro: ' || SQLERRM);
END IF;
END;

DELETE FROM TRAIN_WAGON_USAGE WHERE train_id = 'TEST35';
DELETE FROM TRAIN WHERE train_id = 'TEST35';
END;
/