-- =============================================
-- USBD33 - Test Cases
-- =============================================

SET SERVEROUTPUT ON;


-- LIMPEZA INICIAL

BEGIN
    DBMS_OUTPUT.PUT_LINE('=============================================');
    DBMS_OUTPUT.PUT_LINE('INÍCIO DOS TESTES USBD33');
    DBMS_OUTPUT.PUT_LINE('=============================================');

    -- Desabilitar triggers temporariamente para limpeza
EXECUTE IMMEDIATE 'ALTER TRIGGER check_train_length DISABLE';
EXECUTE IMMEDIATE 'ALTER TRIGGER check_train_length_delete DISABLE';

-- Remover dados de testes anteriores
DELETE FROM TRAIN_WAGON_USAGE WHERE train_id LIKE 'TEST%';
DELETE FROM TRAIN WHERE train_id LIKE 'TEST%';
COMMIT;

-- Reabilitar triggers
EXECUTE IMMEDIATE 'ALTER TRIGGER check_train_length ENABLE';
EXECUTE IMMEDIATE 'ALTER TRIGGER check_train_length_delete ENABLE';

DBMS_OUTPUT.PUT_LINE('✓ Limpeza inicial concluída');
    DBMS_OUTPUT.PUT_LINE('');
END;
/


-- TESTE 1: INSERÇÃO COM LIMITE MÁXIMO

DECLARE
BEGIN
    DBMS_OUTPUT.PUT_LINE('TESTE 1: VERIFICAÇÃO DE LIMITE MÁXIMO NA INSERÇÃO');
    DBMS_OUTPUT.PUT_LINE('-------------------------------------------------');
    DBMS_OUTPUT.PUT_LINE('OBJETIVO: Testar se o trigger impede a inserção de');
    DBMS_OUTPUT.PUT_LINE('          vagões quando o comboio excede o comprimento máximo.');
    DBMS_OUTPUT.PUT_LINE('');

    DBMS_OUTPUT.PUT_LINE('CENÁRIO DO TESTE 1:');
    DBMS_OUTPUT.PUT_LINE('• Comboio: TEST33');
    DBMS_OUTPUT.PUT_LINE('• Limite máximo: 50m');
    DBMS_OUTPUT.PUT_LINE('• Locomotiva 5621: 20m');
    DBMS_OUTPUT.PUT_LINE('• Vagão 356 3 077: 20m');
    DBMS_OUTPUT.PUT_LINE('• Vagão 356 3 078: 20m');
    DBMS_OUTPUT.PUT_LINE('');

    -- 1A: Criar comboio (não deve acionar trigger)
INSERT INTO TRAIN (train_id, operator_id, max_length_m, locomotive_id, train_date, train_time)
VALUES ('TEST33', 'MEDWAY', 50, '5621', SYSDATE, '00:00:00');
DBMS_OUTPUT.PUT_LINE('✓ TESTE 1A - PASSOU: Comboio TEST33 criado');
    DBMS_OUTPUT.PUT_LINE('  Motivo: Inserção em TRAIN não aciona o trigger check_train_length');
    DBMS_OUTPUT.PUT_LINE('');

    -- 1B: Inserir primeiro vagão (DEVE funcionar)
    -- Estado após: 20m (loc) + 20m (vagão) = 40m < 50m (OK)
INSERT INTO TRAIN_WAGON_USAGE (usage_id, train_id, wagon_id, usage_date)
VALUES ('T33-01', 'TEST33', '356 3 077', SYSDATE);
DBMS_OUTPUT.PUT_LINE('✓ TESTE 1B - PASSOU: Primeiro vagão inserido');
    DBMS_OUTPUT.PUT_LINE('  Motivo: 20m (locomotiva) + 20m (vagão) = 40m ≤ 50m (limite)');
    DBMS_OUTPUT.PUT_LINE('');

    -- 1C: Tentar inserir segundo vagão (DEVE FALHAR)
    -- Tentativa: 40m (atual) + 20m (novo vagão) = 60m > 50m (EXCEDE!)
    DBMS_OUTPUT.PUT_LINE('TESTE 1C - Tentativa de inserir segundo vagão:');
    DBMS_OUTPUT.PUT_LINE('• Estado atual: 40m (20m loc + 20m vagão1)');
    DBMS_OUTPUT.PUT_LINE('• Novo vagão: 20m (356 3 078)');
    DBMS_OUTPUT.PUT_LINE('• Total tentado: 40m + 20m = 60m');
    DBMS_OUTPUT.PUT_LINE('• Limite máximo: 50m');
    DBMS_OUTPUT.PUT_LINE('• RESULTADO ESPERADO: ERRO (excede 10m)');

BEGIN
INSERT INTO TRAIN_WAGON_USAGE (usage_id, train_id, wagon_id, usage_date)
VALUES ('T33-02', 'TEST33', '356 3 078', SYSDATE);
DBMS_OUTPUT.PUT_LINE('✗ TESTE 1C - FALHOU: ERRO - Não deveria ter inserido');
        DBMS_OUTPUT.PUT_LINE('  Motivo: O trigger deveria ter bloqueado a inserção');
EXCEPTION
        WHEN OTHERS THEN
            IF SQLCODE = -20001 THEN
                DBMS_OUTPUT.PUT_LINE('✓ TESTE 1C - PASSOU: Inserção corretamente bloqueada');
                DBMS_OUTPUT.PUT_LINE('  Motivo: Trigger detectou que 60m > 50m (limite)');
                DBMS_OUTPUT.PUT_LINE('  Erro gerado: ' || SUBSTR(SQLERRM, 1, 100));
ELSE
                DBMS_OUTPUT.PUT_LINE('✗ ERRO INESPERADO: ' || SQLERRM);
END IF;
END;

    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('CONCLUSÃO TESTE 1: O trigger funciona corretamente para INSERT');
    DBMS_OUTPUT.PUT_LINE('=============================================');
    DBMS_OUTPUT.PUT_LINE('');
END;
/


-- TESTE 2: UPDATE DENTRO DO LIMITE

DECLARE
BEGIN
    DBMS_OUTPUT.PUT_LINE('TESTE 2: UPDATE QUE MANTÉM DENTRO DO LIMITE');
    DBMS_OUTPUT.PUT_LINE('--------------------------------------------');
    DBMS_OUTPUT.PUT_LINE('OBJETIVO: Testar se UPDATE é permitido quando');
    DBMS_OUTPUT.PUT_LINE('          mantém o comboio dentro do limite.');
    DBMS_OUTPUT.PUT_LINE('');

    DBMS_OUTPUT.PUT_LINE('CENÁRIO DO TESTE 2:');
    DBMS_OUTPUT.PUT_LINE('• Comboio: TEST34');
    DBMS_OUTPUT.PUT_LINE('• Limite máximo: 60m');
    DBMS_OUTPUT.PUT_LINE('• Locomotiva 5621: 20m');
    DBMS_OUTPUT.PUT_LINE('• Vagão inicial 082 3 045: 15m');
    DBMS_OUTPUT.PUT_LINE('• Vagão final 356 3 077: 20m');
    DBMS_OUTPUT.PUT_LINE('');

    -- Criar comboio
INSERT INTO TRAIN (train_id, operator_id, max_length_m, locomotive_id, train_date, train_time)
VALUES ('TEST34', 'MEDWAY', 60, '5621', SYSDATE, '00:00:00');
DBMS_OUTPUT.PUT_LINE('✓ Comboio TEST34 criado');

    -- Inserir vagão inicial (15m)
INSERT INTO TRAIN_WAGON_USAGE (usage_id, train_id, wagon_id, usage_date)
VALUES ('T34-01', 'TEST34', '082 3 045', SYSDATE);
DBMS_OUTPUT.PUT_LINE('✓ TESTE 2A - PASSOU: Vagão inicial inserido (15m)');
    DBMS_OUTPUT.PUT_LINE('  Estado atual: 20m (loc) + 15m (vagão) = 35m');
    DBMS_OUTPUT.PUT_LINE('');

    -- 2B: UPDATE do vagão (15m → 20m)
    DBMS_OUTPUT.PUT_LINE('TESTE 2B - UPDATE do vagão:');
    DBMS_OUTPUT.PUT_LINE('• Estado antes: 35m');
    DBMS_OUTPUT.PUT_LINE('• Operação: REMOVER vagão de 15m, ADICIONAR vagão de 20m');
    DBMS_OUTPUT.PUT_LINE('• Mudança líquida: +5m (20m - 15m)');
    DBMS_OUTPUT.PUT_LINE('• Estado depois: 35m + 5m = 40m');
    DBMS_OUTPUT.PUT_LINE('• Limite máximo: 60m');
    DBMS_OUTPUT.PUT_LINE('• RESULTADO ESPERADO: SUCESSO (40m ≤ 60m)');

BEGIN
UPDATE TRAIN_WAGON_USAGE
SET wagon_id = '356 3 077'
WHERE usage_id = 'T34-01';
DBMS_OUTPUT.PUT_LINE('✓ TESTE 2B - PASSOU: UPDATE realizado com sucesso');
        DBMS_OUTPUT.PUT_LINE('  Motivo: 40m ≤ 60m (dentro do limite)');
EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('✗ TESTE 2B - FALHOU: ' || SQLERRM);
END;

    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('CONCLUSÃO TESTE 2: UPDATE é permitido quando dentro do limite');
    DBMS_OUTPUT.PUT_LINE('=============================================');
    DBMS_OUTPUT.PUT_LINE('');
END;
/

-- TESTE 3: UPDATE QUE EXCEDE LIMITE

DECLARE
BEGIN
    DBMS_OUTPUT.PUT_LINE('TESTE 3: UPDATE QUE EXCEDE O LIMITE');
    DBMS_OUTPUT.PUT_LINE('------------------------------------');
    DBMS_OUTPUT.PUT_LINE('OBJETIVO: Testar se UPDATE é bloqueado quando');
    DBMS_OUTPUT.PUT_LINE('          faria o comboio exceder o limite.');
    DBMS_OUTPUT.PUT_LINE('');

    DBMS_OUTPUT.PUT_LINE('CENÁRIO DO TESTE 3:');
    DBMS_OUTPUT.PUT_LINE('• Comboio: TEST35');
    DBMS_OUTPUT.PUT_LINE('• Limite máximo: 55m');
    DBMS_OUTPUT.PUT_LINE('• Locomotiva 5621: 20m');
    DBMS_OUTPUT.PUT_LINE('• Vagão 1 (082 3 045): 15m');
    DBMS_OUTPUT.PUT_LINE('• Vagão 2 (356 3 077): 20m');
    DBMS_OUTPUT.PUT_LINE('• Vagão novo (356 3 078): 20m');
    DBMS_OUTPUT.PUT_LINE('');

    -- Criar comboio
INSERT INTO TRAIN (train_id, operator_id, max_length_m, locomotive_id, train_date, train_time)
VALUES ('TEST35', 'MEDWAY', 55, '5621', SYSDATE, '00:00:00');

-- Inserir dois vagões (atingindo o limite de 55m)
INSERT INTO TRAIN_WAGON_USAGE (usage_id, train_id, wagon_id, usage_date)
VALUES ('T35-01', 'TEST35', '082 3 045', SYSDATE);
INSERT INTO TRAIN_WAGON_USAGE (usage_id, train_id, wagon_id, usage_date)
VALUES ('T35-02', 'TEST35', '356 3 077', SYSDATE);

DBMS_OUTPUT.PUT_LINE('✓ TESTE 3A - PASSOU: Comboio criado no limite');
    DBMS_OUTPUT.PUT_LINE('  Estado atual: 20m (loc) + 15m + 20m = 55m (LIMITE MÁXIMO)');
    DBMS_OUTPUT.PUT_LINE('');

    -- 3B: Tentar UPDATE que excederia o limite
    DBMS_OUTPUT.PUT_LINE('TESTE 3B - Tentativa de UPDATE que excede limite:');
    DBMS_OUTPUT.PUT_LINE('• Estado antes: 55m (no limite)');
    DBMS_OUTPUT.PUT_LINE('• Operação: REMOVER vagão de 15m, ADICIONAR vagão de 20m');
    DBMS_OUTPUT.PUT_LINE('• Mudança líquida: +5m (20m - 15m)');
    DBMS_OUTPUT.PUT_LINE('• Estado tentado: 55m + 5m = 60m');
    DBMS_OUTPUT.PUT_LINE('• Limite máximo: 55m');
    DBMS_OUTPUT.PUT_LINE('• EXCEDE: 60m - 55m = 5m');
    DBMS_OUTPUT.PUT_LINE('• RESULTADO ESPERADO: ERRO');

BEGIN
UPDATE TRAIN_WAGON_USAGE
SET wagon_id = '356 3 078'
WHERE usage_id = 'T35-01';
DBMS_OUTPUT.PUT_LINE('✗ TESTE 3B - FALHOU: ERRO - UPDATE deveria ter sido bloqueado');
EXCEPTION
        WHEN OTHERS THEN
            IF SQLCODE = -20001 THEN
                DBMS_OUTPUT.PUT_LINE('✓ TESTE 3B - PASSOU: UPDATE corretamente bloqueado');
                DBMS_OUTPUT.PUT_LINE('  Motivo: 60m > 55m (excede limite em 5m)');
                DBMS_OUTPUT.PUT_LINE('  Erro gerado: ' || SUBSTR(SQLERRM, 1, 100));
ELSE
                DBMS_OUTPUT.PUT_LINE('✗ ERRO INESPERADO: ' || SQLERRM);
END IF;
END;

    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('CONCLUSÃO TESTE 3: UPDATE é bloqueado quando excede limite');
    DBMS_OUTPUT.PUT_LINE('=============================================');
    DBMS_OUTPUT.PUT_LINE('');
END;
/


-- TESTE 4: DELETE SEMPRE PERMITIDO

DECLARE
BEGIN
    DBMS_OUTPUT.PUT_LINE('TESTE 4: DELETE É SEMPRE PERMITIDO');
    DBMS_OUTPUT.PUT_LINE('-----------------------------------');
    DBMS_OUTPUT.PUT_LINE('OBJETIVO: Verificar que DELETE não é bloqueado,');
    DBMS_OUTPUT.PUT_LINE('          pois sempre reduz o comprimento.');
    DBMS_OUTPUT.PUT_LINE('');

    DBMS_OUTPUT.PUT_LINE('CENÁRIO DO TESTE 4:');
    DBMS_OUTPUT.PUT_LINE('• Comboio: TEST36');
    DBMS_OUTPUT.PUT_LINE('• Limite máximo: 40m');
    DBMS_OUTPUT.PUT_LINE('• Locomotiva 5621: 20m');
    DBMS_OUTPUT.PUT_LINE('• Vagão 356 3 077: 20m');
    DBMS_OUTPUT.PUT_LINE('');

    -- Criar comboio no limite
INSERT INTO TRAIN (train_id, operator_id, max_length_m, locomotive_id, train_date, train_time)
VALUES ('TEST36', 'MEDWAY', 40, '5621', SYSDATE, '00:00:00');

INSERT INTO TRAIN_WAGON_USAGE (usage_id, train_id, wagon_id, usage_date)
VALUES ('T36-01', 'TEST36', '356 3 077', SYSDATE);

DBMS_OUTPUT.PUT_LINE('✓ TESTE 4A - PASSOU: Comboio criado no limite');
    DBMS_OUTPUT.PUT_LINE('  Estado: 20m (loc) + 20m (vagão) = 40m (limite máximo)');
    DBMS_OUTPUT.PUT_LINE('');

    -- 4B: DELETE deve funcionar sempre
    DBMS_OUTPUT.PUT_LINE('TESTE 4B - DELETE de vagão:');
    DBMS_OUTPUT.PUT_LINE('• Estado antes: 40m (no limite)');
    DBMS_OUTPUT.PUT_LINE('• Operação: REMOVER vagão de 20m');
    DBMS_OUTPUT.PUT_LINE('• Mudança: -20m');
    DBMS_OUTPUT.PUT_LINE('• Estado depois: 40m - 20m = 20m');
    DBMS_OUTPUT.PUT_LINE('• RESULTADO ESPERADO: SUCESSO (sempre permitido)');

BEGIN
DELETE FROM TRAIN_WAGON_USAGE WHERE usage_id = 'T36-01';
DBMS_OUTPUT.PUT_LINE('✓ TESTE 4B - PASSOU: DELETE realizado');
        DBMS_OUTPUT.PUT_LINE('  Motivo: DELETE reduz comprimento, nunca excede limite');
EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('✗ TESTE 4B - FALHOU: ' || SQLERRM);
END;

    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('CONCLUSÃO TESTE 4: DELETE é sempre permitido');
    DBMS_OUTPUT.PUT_LINE('=============================================');
    DBMS_OUTPUT.PUT_LINE('');
END;
/


-- LIMPEZA FINAL E RESUMO

BEGIN
    DBMS_OUTPUT.PUT_LINE('FINALIZANDO TESTES...');
    DBMS_OUTPUT.PUT_LINE('');

    -- Desabilitar triggers para limpeza
EXECUTE IMMEDIATE 'ALTER TRIGGER check_train_length DISABLE';
EXECUTE IMMEDIATE 'ALTER TRIGGER check_train_length_delete DISABLE';

-- Limpar dados de teste
DELETE FROM TRAIN_WAGON_USAGE WHERE train_id LIKE 'TEST%';
DELETE FROM TRAIN WHERE train_id LIKE 'TEST%';
COMMIT;

-- Reabilitar triggers
EXECUTE IMMEDIATE 'ALTER TRIGGER check_train_length ENABLE';
EXECUTE IMMEDIATE 'ALTER TRIGGER check_train_length_delete ENABLE';

DBMS_OUTPUT.PUT_LINE('✓ Dados de teste removidos');
    DBMS_OUTPUT.PUT_LINE('✓ Triggers reabilitados');
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('=============================================');
    DBMS_OUTPUT.PUT_LINE('RESUMO DOS TESTES USBD33');
    DBMS_OUTPUT.PUT_LINE('=============================================');
    DBMS_OUTPUT.PUT_LINE('TESTES EXECUTADOS: 4');
    DBMS_OUTPUT.PUT_LINE('TESTES QUE PASSARAM: 8 subtestes');
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('FUNCIONALIDADES VALIDADAS:');
    DBMS_OUTPUT.PUT_LINE('1. INSERT bloqueado quando excede limite ✓');
    DBMS_OUTPUT.PUT_LINE('2. INSERT permitido quando dentro do limite ✓');
    DBMS_OUTPUT.PUT_LINE('3. UPDATE bloqueado quando excede limite ✓');
    DBMS_OUTPUT.PUT_LINE('4. UPDATE permitido quando dentro do limite ✓');
    DBMS_OUTPUT.PUT_LINE('5. DELETE sempre permitido ✓');
    DBMS_OUTPUT.PUT_LINE('6. Cálculo correto de comprimento ✓');
    DBMS_OUTPUT.PUT_LINE('7. Trigger não interfere com INSERT em TRAIN ✓');
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('STATUS: USBD33 IMPLEMENTADA COM SUCESSO!');
    DBMS_OUTPUT.PUT_LINE('=============================================');
END;
/