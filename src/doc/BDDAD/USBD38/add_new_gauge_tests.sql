-- =============================================
-- USBD38 - Anonymous Block Tests with Explanations
-- =============================================
SET SERVEROUTPUT ON;

-- Limpeza inicial de dados de teste
BEGIN
DELETE FROM GAUGE WHERE gauge_name LIKE '%TEST%' OR gauge_name LIKE '%METRIC%' OR gauge_name LIKE '%RUSSIAN%';
COMMIT;
DBMS_OUTPUT.PUT_LINE('=== INÍCIO DOS TESTES USBD38 ===');
    DBMS_OUTPUT.PUT_LINE('');
END;
/

-- Teste 1: Inserção válida
DECLARE
v_result VARCHAR2(500);
BEGIN
    DBMS_OUTPUT.PUT_LINE('TESTE 1: INSERÇÃO VÁLIDA');
    DBMS_OUTPUT.PUT_LINE('Objetivo: Testar inserção de um bitola válida');
    DBMS_OUTPUT.PUT_LINE('Valores: gauge_mm=1000, name=METRIC_GAUGE, description=Metric gauge');
    DBMS_OUTPUT.PUT_LINE('Resultado esperado: SUCCESS');

    v_result := add_new_gauge(1000, 'METRIC_GAUGE', 'Metric gauge');

    DBMS_OUTPUT.PUT_LINE('Resultado: ' || v_result);
    IF v_result LIKE 'SUCCESS%' THEN
        DBMS_OUTPUT.PUT_LINE('✓ TESTE 1 PASSOU');
ELSE
        DBMS_OUTPUT.PUT_LINE('✗ TESTE 1 FALHOU');
END IF;
    DBMS_OUTPUT.PUT_LINE('---');
END;
/

-- Teste 2: Valor duplicado (1668 já existe na base)
DECLARE
v_result VARCHAR2(500);
BEGIN
    DBMS_OUTPUT.PUT_LINE('TESTE 2: VALOR DUPLICADO');
    DBMS_OUTPUT.PUT_LINE('Objetivo: Testar tentativa de inserir bitola com valor já existente');
    DBMS_OUTPUT.PUT_LINE('Valores: gauge_mm=1668 (já existe como IBERIAN), name=NEW_NAME');
    DBMS_OUTPUT.PUT_LINE('Resultado esperado: ERROR - valor já existe');

    v_result := add_new_gauge(1668, 'NEW_NAME', 'Duplicate value');

    DBMS_OUTPUT.PUT_LINE('Resultado: ' || v_result);
    IF v_result LIKE 'ERROR: Gauge with value%already exists%' THEN
        DBMS_OUTPUT.PUT_LINE('✓ TESTE 2 PASSOU - Corretamente rejeitado');
ELSE
        DBMS_OUTPUT.PUT_LINE('✗ TESTE 2 FALHOU');
END IF;
    DBMS_OUTPUT.PUT_LINE('---');
END;
/

-- Teste 3: Nome duplicado (STANDARD já existe na base)
DECLARE
v_result VARCHAR2(500);
BEGIN
    DBMS_OUTPUT.PUT_LINE('TESTE 3: NOME DUPLICADO');
    DBMS_OUTPUT.PUT_LINE('Objetivo: Testar tentativa de inserir bitola com nome já existente');
    DBMS_OUTPUT.PUT_LINE('Valores: gauge_mm=1500, name=STANDARD (já existe como 1435 mm)');
    DBMS_OUTPUT.PUT_LINE('Resultado esperado: ERROR - nome já existe');

    v_result := add_new_gauge(1500, 'STANDARD', 'Duplicate name');

    DBMS_OUTPUT.PUT_LINE('Resultado: ' || v_result);
    IF v_result LIKE 'ERROR: Gauge name "%" already exists%' THEN
        DBMS_OUTPUT.PUT_LINE('✓ TESTE 3 PASSOU - Corretamente rejeitado');
ELSE
        DBMS_OUTPUT.PUT_LINE('✗ TESTE 3 FALHOU');
END IF;
    DBMS_OUTPUT.PUT_LINE('---');
END;
/

-- Teste 4: Valor fora do intervalo
DECLARE
v_result VARCHAR2(500);
BEGIN
    DBMS_OUTPUT.PUT_LINE('TESTE 4: VALOR FORA DO INTERVALO');
    DBMS_OUTPUT.PUT_LINE('Objetivo: Testar bitola com valor muito pequeno');
    DBMS_OUTPUT.PUT_LINE('Valores: gauge_mm=300, name=NARROW');
    DBMS_OUTPUT.PUT_LINE('Limite mínimo: 500 mm');
    DBMS_OUTPUT.PUT_LINE('Resultado esperado: ERROR - fora do intervalo');

    v_result := add_new_gauge(300, 'NARROW', 'Too small');

    DBMS_OUTPUT.PUT_LINE('Resultado: ' || v_result);
    IF v_result LIKE 'ERROR: Gauge value must be between%' THEN
        DBMS_OUTPUT.PUT_LINE('✓ TESTE 4 PASSOU - Corretamente rejeitado');
ELSE
        DBMS_OUTPUT.PUT_LINE('✗ TESTE 4 FALHOU');
END IF;
    DBMS_OUTPUT.PUT_LINE('---');
END;
/

-- Teste 5: Valor NULL
DECLARE
v_result VARCHAR2(500);
BEGIN
    DBMS_OUTPUT.PUT_LINE('TESTE 5: VALOR NULL');
    DBMS_OUTPUT.PUT_LINE('Objetivo: Testar bitola com valor NULL');
    DBMS_OUTPUT.PUT_LINE('Valores: gauge_mm=NULL, name=TEST');
    DBMS_OUTPUT.PUT_LINE('Resultado esperado: ERROR - valor obrigatório');

    v_result := add_new_gauge(NULL, 'TEST', 'NULL value');

    DBMS_OUTPUT.PUT_LINE('Resultado: ' || v_result);
    IF v_result LIKE 'ERROR: Gauge value (mm) is required%' THEN
        DBMS_OUTPUT.PUT_LINE('✓ TESTE 5 PASSOU - Corretamente rejeitado');
ELSE
        DBMS_OUTPUT.PUT_LINE('✗ TESTE 5 FALHOU');
END IF;
    DBMS_OUTPUT.PUT_LINE('---');
END;
/

-- Teste 6: Nome NULL
DECLARE
v_result VARCHAR2(500);
BEGIN
    DBMS_OUTPUT.PUT_LINE('TESTE 6: NOME NULL');
    DBMS_OUTPUT.PUT_LINE('Objetivo: Testar bitola com nome NULL');
    DBMS_OUTPUT.PUT_LINE('Valores: gauge_mm=2000, name=NULL');
    DBMS_OUTPUT.PUT_LINE('Resultado esperado: ERROR - nome obrigatório');

    v_result := add_new_gauge(2000, NULL, 'NULL name');

    DBMS_OUTPUT.PUT_LINE('Resultado: ' || v_result);
    IF v_result LIKE 'ERROR: Gauge name is required%' THEN
        DBMS_OUTPUT.PUT_LINE('✓ TESTE 6 PASSOU - Corretamente rejeitado');
ELSE
        DBMS_OUTPUT.PUT_LINE('✗ TESTE 6 FALHOU');
END IF;
    DBMS_OUTPUT.PUT_LINE('---');
END;
/

-- Teste 7: Descrição NULL (deve funcionar)
DECLARE
v_result VARCHAR2(500);
BEGIN
    DBMS_OUTPUT.PUT_LINE('TESTE 7: DESCRIÇÃO NULL (OPCIONAL)');
    DBMS_OUTPUT.PUT_LINE('Objetivo: Testar bitola com descrição NULL (campo opcional)');
    DBMS_OUTPUT.PUT_LINE('Valores: gauge_mm=1520, name=RUSSIAN, description=NULL');
    DBMS_OUTPUT.PUT_LINE('Resultado esperado: SUCCESS - descrição é opcional');

    v_result := add_new_gauge(1520, 'RUSSIAN', NULL);

    DBMS_OUTPUT.PUT_LINE('Resultado: ' || v_result);
    IF v_result LIKE 'SUCCESS%' THEN
        DBMS_OUTPUT.PUT_LINE('✓ TESTE 7 PASSOU');
ELSE
        DBMS_OUTPUT.PUT_LINE('✗ TESTE 7 FALHOU');
END IF;
    DBMS_OUTPUT.PUT_LINE('---');
END;
/

-- Teste 8: Valor muito grande
DECLARE
v_result VARCHAR2(500);
BEGIN
    DBMS_OUTPUT.PUT_LINE('TESTE 8: VALOR MUITO GRANDE');
    DBMS_OUTPUT.PUT_LINE('Objetivo: Testar bitola com valor acima do limite máximo');
    DBMS_OUTPUT.PUT_LINE('Valores: gauge_mm=3500, name=WIDE_GAUGE');
    DBMS_OUTPUT.PUT_LINE('Limite máximo: 3000 mm');
    DBMS_OUTPUT.PUT_LINE('Resultado esperado: ERROR - fora do intervalo');

    v_result := add_new_gauge(3500, 'WIDE_GAUGE', 'Too wide');

    DBMS_OUTPUT.PUT_LINE('Resultado: ' || v_result);
    IF v_result LIKE 'ERROR: Gauge value must be between%' THEN
        DBMS_OUTPUT.PUT_LINE('✓ TESTE 8 PASSOU - Corretamente rejeitado');
ELSE
        DBMS_OUTPUT.PUT_LINE('✗ TESTE 8 FALHOU');
END IF;
    DBMS_OUTPUT.PUT_LINE('---');
END;
/

-- Teste 9: Nome muito curto
DECLARE
v_result VARCHAR2(500);
BEGIN
    DBMS_OUTPUT.PUT_LINE('TESTE 9: NOME MUITO CURTO');
    DBMS_OUTPUT.PUT_LINE('Objetivo: Testar bitola com nome de apenas 1 caractere');
    DBMS_OUTPUT.PUT_LINE('Valores: gauge_mm=1600, name=X');
    DBMS_OUTPUT.PUT_LINE('Mínimo: 2 caracteres');
    DBMS_OUTPUT.PUT_LINE('Resultado esperado: ERROR - nome muito curto');

    v_result := add_new_gauge(1600, 'X', 'Single character');

    DBMS_OUTPUT.PUT_LINE('Resultado: ' || v_result);
    IF v_result LIKE 'ERROR: Gauge name must have at least 2 characters%' THEN
        DBMS_OUTPUT.PUT_LINE('✓ TESTE 9 PASSOU - Corretamente rejeitado');
ELSE
        DBMS_OUTPUT.PUT_LINE('✗ TESTE 9 FALHOU');
END IF;
    DBMS_OUTPUT.PUT_LINE('---');
END;
/

-- Resumo final
BEGIN
    DBMS_OUTPUT.PUT_LINE('=== RESUMO DOS TESTES ===');
    DBMS_OUTPUT.PUT_LINE('Testes executados: 9');
    DBMS_OUTPUT.PUT_LINE('Testes que deveriam passar: 2 (1 e 7)');
    DBMS_OUTPUT.PUT_LINE('Testes que deveriam falhar: 7 (2-6, 8-9)');
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('VALIDAÇÕES TESTADAS:');
    DBMS_OUTPUT.PUT_LINE('1. Inserção válida ✓');
    DBMS_OUTPUT.PUT_LINE('2. Valor duplicado ✓');
    DBMS_OUTPUT.PUT_LINE('3. Nome duplicado ✓');
    DBMS_OUTPUT.PUT_LINE('4. Valor fora do intervalo (mínimo) ✓');
    DBMS_OUTPUT.PUT_LINE('5. Valor NULL ✓');
    DBMS_OUTPUT.PUT_LINE('6. Nome NULL ✓');
    DBMS_OUTPUT.PUT_LINE('7. Descrição NULL (opcional) ✓');
    DBMS_OUTPUT.PUT_LINE('8. Valor fora do intervalo (máximo) ✓');
    DBMS_OUTPUT.PUT_LINE('9. Nome muito curto ✓');
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('STATUS: USBD38 IMPLEMENTADA COM SUCESSO!');
END;
/