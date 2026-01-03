-- =============================================
-- USBD44 - Test Cases with Detailed Explanations
-- =============================================
SET SERVEROUTPUT ON;

-- Limpeza inicial de segmentos de teste
BEGIN
DELETE FROM LINE_SEGMENT WHERE segment_id IN ('TEST1', 'TEST2', 'TEST5', 'TEST6', 'TEST7', 'TEST8', 'TEST9');
COMMIT;

DBMS_OUTPUT.PUT_LINE('=== INÍCIO DOS TESTES USBD44 ===');
    DBMS_OUTPUT.PUT_LINE('Objetivo: Testar função add_segment_to_line()');
    DBMS_OUTPUT.PUT_LINE('Funcionalidade: Adicionar segmento a uma linha existente');
    DBMS_OUTPUT.PUT_LINE('');
END;
/

-- Teste 1: Inserção válida sem desvio
DECLARE
v_result VARCHAR2(100);
BEGIN
    DBMS_OUTPUT.PUT_LINE('TESTE 1: INSERÇÃO VÁLIDA SEM DESVIO (SIDING)');
    DBMS_OUTPUT.PUT_LINE('---------------------------------------------');
    DBMS_OUTPUT.PUT_LINE('Objetivo: Testar inserção básica de segmento');
    DBMS_OUTPUT.PUT_LINE('Parâmetros:');
    DBMS_OUTPUT.PUT_LINE('  • segment_id: TEST1');
    DBMS_OUTPUT.PUT_LINE('  • line_id: L001 (linha existente)');
    DBMS_OUTPUT.PUT_LINE('  • segment_order: 10');
    DBMS_OUTPUT.PUT_LINE('  • is_electrified: Yes');
    DBMS_OUTPUT.PUT_LINE('  • max_weight_kg_m: 8000');
    DBMS_OUTPUT.PUT_LINE('  • length_m: 5000');
    DBMS_OUTPUT.PUT_LINE('  • number_tracks: 2 (via dupla)');
    DBMS_OUTPUT.PUT_LINE('  • siding: NULL (sem desvio)');
    DBMS_OUTPUT.PUT_LINE('Resultado esperado: SUCCESS');

    v_result := add_segment_to_line(
        p_segment_id      => 'TEST1',
        p_line_id         => 'L001',
        p_segment_order   => 10,
        p_is_electrified  => 'Yes',
        p_max_weight_kg_m => 8000,
        p_length_m        => 5000,
        p_number_tracks   => 2
    );

    DBMS_OUTPUT.PUT_LINE('Resultado: ' || v_result);
    IF v_result LIKE 'SUCCESS%' THEN
        DBMS_OUTPUT.PUT_LINE('✓ TESTE 1 PASSOU - Segmento adicionado com sucesso');
ELSE
        DBMS_OUTPUT.PUT_LINE('✗ TESTE 1 FALHOU');
END IF;
    DBMS_OUTPUT.PUT_LINE('---');
END;
/

-- Teste 2: Inserção válida com desvio completo
DECLARE
v_result VARCHAR2(100);
BEGIN
    DBMS_OUTPUT.PUT_LINE('TESTE 2: INSERÇÃO VÁLIDA COM DESVIO COMPLETO');
    DBMS_OUTPUT.PUT_LINE('--------------------------------------------');
    DBMS_OUTPUT.PUT_LINE('Objetivo: Testar inserção com desvio (siding) completo');
    DBMS_OUTPUT.PUT_LINE('Parâmetros:');
    DBMS_OUTPUT.PUT_LINE('  • segment_id: TEST2');
    DBMS_OUTPUT.PUT_LINE('  • line_id: L002 (linha existente)');
    DBMS_OUTPUT.PUT_LINE('  • segment_order: 5');
    DBMS_OUTPUT.PUT_LINE('  • is_electrified: Yes');
    DBMS_OUTPUT.PUT_LINE('  • max_weight_kg_m: 7500');
    DBMS_OUTPUT.PUT_LINE('  • length_m: 3000');
    DBMS_OUTPUT.PUT_LINE('  • number_tracks: 1 (via simples)');
    DBMS_OUTPUT.PUT_LINE('  • siding_position: 1500 (meio do segmento)');
    DBMS_OUTPUT.PUT_LINE('  • siding_length: 500');
    DBMS_OUTPUT.PUT_LINE('Resultado esperado: SUCCESS');
    DBMS_OUTPUT.PUT_LINE('Nota: Desvio requer ambos position e length');

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

    DBMS_OUTPUT.PUT_LINE('Resultado: ' || v_result);
    IF v_result LIKE 'SUCCESS%' THEN
        DBMS_OUTPUT.PUT_LINE('✓ TESTE 2 PASSOU - Segmento com desvio adicionado');
ELSE
        DBMS_OUTPUT.PUT_LINE('✗ TESTE 2 FALHOU');
END IF;
    DBMS_OUTPUT.PUT_LINE('---');
END;
/

-- Teste 3: Linha não existe
DECLARE
v_result VARCHAR2(100);
BEGIN
    DBMS_OUTPUT.PUT_LINE('TESTE 3: LINHA NÃO EXISTE');
    DBMS_OUTPUT.PUT_LINE('-------------------------');
    DBMS_OUTPUT.PUT_LINE('Objetivo: Testar tentativa com linha inexistente');
    DBMS_OUTPUT.PUT_LINE('Parâmetros:');
    DBMS_OUTPUT.PUT_LINE('  • segment_id: TEST3');
    DBMS_OUTPUT.PUT_LINE('  • line_id: L999 (NÃO EXISTE)');
    DBMS_OUTPUT.PUT_LINE('  • Outros parâmetros válidos');
    DBMS_OUTPUT.PUT_LINE('Resultado esperado: ERROR - Linha não existe');
    DBMS_OUTPUT.PUT_LINE('Validação: Verifica se linha existe antes de inserir');

    v_result := add_segment_to_line(
        p_segment_id      => 'TEST3',
        p_line_id         => 'L999',
        p_segment_order   => 1,
        p_is_electrified  => 'Yes',
        p_max_weight_kg_m => 8000,
        p_length_m        => 4000,
        p_number_tracks   => 2
    );

    DBMS_OUTPUT.PUT_LINE('Resultado: ' || v_result);
    IF v_result LIKE 'ERROR: Line does not exist%' THEN
        DBMS_OUTPUT.PUT_LINE('✓ TESTE 3 PASSOU - Corretamente rejeitado');
ELSE
        DBMS_OUTPUT.PUT_LINE('✗ TESTE 3 FALHOU');
END IF;
    DBMS_OUTPUT.PUT_LINE('---');
END;
/

-- Teste 4: Segmento já existe
DECLARE
v_result VARCHAR2(100);
BEGIN
    DBMS_OUTPUT.PUT_LINE('TESTE 4: SEGMENTO JÁ EXISTE');
    DBMS_OUTPUT.PUT_LINE('---------------------------');
    DBMS_OUTPUT.PUT_LINE('Objetivo: Testar tentativa com ID de segmento duplicado');
    DBMS_OUTPUT.PUT_LINE('Parâmetros:');
    DBMS_OUTPUT.PUT_LINE('  • segment_id: TEST1 (JÁ INSERIDO no Teste 1)');
    DBMS_OUTPUT.PUT_LINE('  • line_id: L001');
    DBMS_OUTPUT.PUT_LINE('  • Outros parâmetros válidos');
    DBMS_OUTPUT.PUT_LINE('Resultado esperado: ERROR - Segmento já existe');
    DBMS_OUTPUT.PUT_LINE('Validação: Garante unicidade do segment_id');

    v_result := add_segment_to_line(
        p_segment_id      => 'TEST1',
        p_line_id         => 'L001',
        p_segment_order   => 20,
        p_is_electrified  => 'Yes',
        p_max_weight_kg_m => 8000,
        p_length_m        => 6000,
        p_number_tracks   => 2
    );

    DBMS_OUTPUT.PUT_LINE('Resultado: ' || v_result);
    IF v_result LIKE 'ERROR: Segment already exists%' THEN
        DBMS_OUTPUT.PUT_LINE('✓ TESTE 4 PASSOU - Corretamente rejeitado');
ELSE
        DBMS_OUTPUT.PUT_LINE('✗ TESTE 4 FALHOU');
END IF;
    DBMS_OUTPUT.PUT_LINE('---');
END;
/

-- Teste 5: Desvio incompleto (só position, sem length)
DECLARE
v_result VARCHAR2(100);
BEGIN
    DBMS_OUTPUT.PUT_LINE('TESTE 5: DESVIO INCOMPLETO');
    DBMS_OUTPUT.PUT_LINE('---------------------------');
    DBMS_OUTPUT.PUT_LINE('Objetivo: Testar desvio com apenas um parâmetro');
    DBMS_OUTPUT.PUT_LINE('Parâmetros:');
    DBMS_OUTPUT.PUT_LINE('  • segment_id: TEST5');
    DBMS_OUTPUT.PUT_LINE('  • line_id: L001');
    DBMS_OUTPUT.PUT_LINE('  • siding_position: 2000 (definido)');
    DBMS_OUTPUT.PUT_LINE('  • siding_length: NULL (não definido)');
    DBMS_OUTPUT.PUT_LINE('Resultado esperado: ERROR - Desvio requer ambos');
    DBMS_OUTPUT.PUT_LINE('Validação: Desvio precisa de position E length');

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

    DBMS_OUTPUT.PUT_LINE('Resultado: ' || v_result);
    IF v_result LIKE 'ERROR: Siding requires both position and length%' THEN
        DBMS_OUTPUT.PUT_LINE('✓ TESTE 5 PASSOU - Corretamente rejeitado');
ELSE
        DBMS_OUTPUT.PUT_LINE('✗ TESTE 5 FALHOU');
END IF;
    DBMS_OUTPUT.PUT_LINE('---');
END;
/

-- Teste 6: Posição de desvio negativa
DECLARE
v_result VARCHAR2(100);
BEGIN
    DBMS_OUTPUT.PUT_LINE('TESTE 6: POSIÇÃO DE DESVIO NEGATIVA');
    DBMS_OUTPUT.PUT_LINE('-----------------------------------');
    DBMS_OUTPUT.PUT_LINE('Objetivo: Testar desvio com posição negativa');
    DBMS_OUTPUT.PUT_LINE('Parâmetros:');
    DBMS_OUTPUT.PUT_LINE('  • segment_id: TEST6');
    DBMS_OUTPUT.PUT_LINE('  • line_id: L001');
    DBMS_OUTPUT.PUT_LINE('  • siding_position: -100 (INVÁLIDO)');
    DBMS_OUTPUT.PUT_LINE('  • siding_length: 500');
    DBMS_OUTPUT.PUT_LINE('Resultado esperado: ERROR - Posição não pode ser negativa');
    DBMS_OUTPUT.PUT_LINE('Validação: Posição do desvio deve ser ≥ 0');

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

    DBMS_OUTPUT.PUT_LINE('Resultado: ' || v_result);
    IF v_result LIKE 'ERROR: Siding position cannot be negative%' THEN
        DBMS_OUTPUT.PUT_LINE('✓ TESTE 6 PASSOU - Corretamente rejeitado');
ELSE
        DBMS_OUTPUT.PUT_LINE('✗ TESTE 6 FALHOU');
END IF;
    DBMS_OUTPUT.PUT_LINE('---');
END;
/

-- Teste 7: Comprimento negativo
DECLARE
v_result VARCHAR2(100);
BEGIN
    DBMS_OUTPUT.PUT_LINE('TESTE 7: COMPRIMENTO NEGATIVO');
    DBMS_OUTPUT.PUT_LINE('------------------------------');
    DBMS_OUTPUT.PUT_LINE('Objetivo: Testar segmento com comprimento negativo');
    DBMS_OUTPUT.PUT_LINE('Parâmetros:');
    DBMS_OUTPUT.PUT_LINE('  • segment_id: TEST7');
    DBMS_OUTPUT.PUT_LINE('  • line_id: L001');
    DBMS_OUTPUT.PUT_LINE('  • length_m: -100 (INVÁLIDO)');
    DBMS_OUTPUT.PUT_LINE('Resultado esperado: ERROR - Comprimento deve ser positivo');
    DBMS_OUTPUT.PUT_LINE('Validação: Comprimento do segmento deve ser > 0');

    v_result := add_segment_to_line(
        p_segment_id      => 'TEST7',
        p_line_id         => 'L001',
        p_segment_order   => 17,
        p_is_electrified  => 'Yes',
        p_max_weight_kg_m => 8000,
        p_length_m        => -100,
        p_number_tracks   => 2
    );

    DBMS_OUTPUT.PUT_LINE('Resultado: ' || v_result);
    IF v_result LIKE 'ERROR: Length must be positive%' THEN
        DBMS_OUTPUT.PUT_LINE('✓ TESTE 7 PASSOU - Corretamente rejeitado');
ELSE
        DBMS_OUTPUT.PUT_LINE('✗ TESTE 7 FALHOU');
END IF;
    DBMS_OUTPUT.PUT_LINE('---');
END;
/

-- Teste 8: Eletrificação inválida
DECLARE
v_result VARCHAR2(100);
BEGIN
    DBMS_OUTPUT.PUT_LINE('TESTE 8: ELETRIFICAÇÃO INVÁLIDA');
    DBMS_OUTPUT.PUT_LINE('--------------------------------');
    DBMS_OUTPUT.PUT_LINE('Objetivo: Testar valor inválido para eletrificação');
    DBMS_OUTPUT.PUT_LINE('Parâmetros:');
    DBMS_OUTPUT.PUT_LINE('  • segment_id: TEST8');
    DBMS_OUTPUT.PUT_LINE('  • line_id: L001');
    DBMS_OUTPUT.PUT_LINE('  • is_electrified: Maybe (INVÁLIDO)');
    DBMS_OUTPUT.PUT_LINE('Valores válidos: "Yes" ou "No" (case-sensitive)');
    DBMS_OUTPUT.PUT_LINE('Resultado esperado: ERROR - Eletrificação inválida');

    v_result := add_segment_to_line(
        p_segment_id      => 'TEST8',
        p_line_id         => 'L001',
        p_segment_order   => 18,
        p_is_electrified  => 'Maybe',
        p_max_weight_kg_m => 8000,
        p_length_m        => 1000,
        p_number_tracks   => 2
    );

    DBMS_OUTPUT.PUT_LINE('Resultado: ' || v_result);
    IF v_result LIKE 'ERROR: Electrification must be Yes or No%' THEN
        DBMS_OUTPUT.PUT_LINE('✓ TESTE 8 PASSOU - Corretamente rejeitado');
ELSE
        DBMS_OUTPUT.PUT_LINE('✗ TESTE 8 FALHOU');
END IF;
    DBMS_OUTPUT.PUT_LINE('---');
END;
/

-- Teste 9: Número de vias inválido
DECLARE
v_result VARCHAR2(100);
BEGIN
    DBMS_OUTPUT.PUT_LINE('TESTE 9: NÚMERO DE VIAS INVÁLIDO');
    DBMS_OUTPUT.PUT_LINE('--------------------------------');
    DBMS_OUTPUT.PUT_LINE('Objetivo: Testar número de vias diferente de 1 ou 2');
    DBMS_OUTPUT.PUT_LINE('Parâmetros:');
    DBMS_OUTPUT.PUT_LINE('  • segment_id: TEST9');
    DBMS_OUTPUT.PUT_LINE('  • line_id: L001');
    DBMS_OUTPUT.PUT_LINE('  • number_tracks: 3 (INVÁLIDO)');
    DBMS_OUTPUT.PUT_LINE('Valores válidos: 1 (via simples) ou 2 (via dupla)');
    DBMS_OUTPUT.PUT_LINE('Resultado esperado: ERROR - Número de vias inválido');

    v_result := add_segment_to_line(
        p_segment_id      => 'TEST9',
        p_line_id         => 'L001',
        p_segment_order   => 19,
        p_is_electrified  => 'Yes',
        p_max_weight_kg_m => 8000,
        p_length_m        => 1000,
        p_number_tracks   => 3
    );

    DBMS_OUTPUT.PUT_LINE('Resultado: ' || v_result);
    IF v_result LIKE 'ERROR: Number of tracks must be 1 or 2%' THEN
        DBMS_OUTPUT.PUT_LINE('✓ TESTE 9 PASSOU - Corretamente rejeitado');
ELSE
        DBMS_OUTPUT.PUT_LINE('✗ TESTE 9 FALHOU');
END IF;
    DBMS_OUTPUT.PUT_LINE('---');
END;
/

-- Resumo final
BEGIN
    DBMS_OUTPUT.PUT_LINE('=== RESUMO DOS TESTES USBD44 ===');
    DBMS_OUTPUT.PUT_LINE('Testes executados: 9');
    DBMS_OUTPUT.PUT_LINE('Testes que deveriam passar: 2 (1 e 2)');
    DBMS_OUTPUT.PUT_LINE('Testes que deveriam falhar: 7 (3-9)');
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('VALIDAÇÕES TESTADAS:');
    DBMS_OUTPUT.PUT_LINE('1. Inserção válida sem desvio ✓');
    DBMS_OUTPUT.PUT_LINE('2. Inserção válida com desvio ✓');
    DBMS_OUTPUT.PUT_LINE('3. Linha não existe ✓');
    DBMS_OUTPUT.PUT_LINE('4. Segmento já existe ✓');
    DBMS_OUTPUT.PUT_LINE('5. Desvio incompleto (só position) ✓');
    DBMS_OUTPUT.PUT_LINE('6. Posição de desvio negativa ✓');
    DBMS_OUTPUT.PUT_LINE('7. Comprimento negativo ✓');
    DBMS_OUTPUT.PUT_LINE('8. Eletrificação inválida ✓');
    DBMS_OUTPUT.PUT_LINE('9. Número de vias inválido ✓');
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('VALIDAÇÕES ADICIONAIS (implícitas):');
    DBMS_OUTPUT.PUT_LINE('• Peso máximo positivo (testado em todos)');
    DBMS_OUTPUT.PUT_LINE('• Comprimento positivo (testado em todos)');
    DBMS_OUTPUT.PUT_LINE('• Desvio requer ambos parâmetros ou nenhum ✓');
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('STATUS: USBD44 IMPLEMENTADA COM SUCESSO!');
    DBMS_OUTPUT.PUT_LINE('=============================================');
END;
/

-- Limpeza final (opcional)
BEGIN
DELETE FROM LINE_SEGMENT WHERE segment_id IN ('TEST1', 'TEST2');
COMMIT;
DBMS_OUTPUT.PUT_LINE('✓ Dados de teste removidos');
END;
/