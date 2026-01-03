-- =============================================
-- USBD27 - Grain Wagons Used in Every Train That Used Grain Wagons
-- =============================================
CREATE OR REPLACE FUNCTION getUniversalGrainWagons RETURN SYS_REFCURSOR
IS
    -- Declaração de Variáveis
    v_grain_wagons SYS_REFCURSOR; -- Variável do tipo SYS_REFCURSOR para armazenar o conjunto de resultados (cursor) que a função irá devolver.
BEGIN
    -- Abrir o cursor: Executa a consulta SQL e associa o resultado à variável v_grain_wagons.
OPEN v_grain_wagons FOR

-- Consulta Principal: Seleciona o identificador do vagão.
SELECT wagon_id
FROM TRAIN_WAGON_USAGE -- Tabela que regista o uso de vagões em comboios.

-- Cláusula de Agrupamento
GROUP BY wagon_id -- Agrupa todos os registos pelo mesmo ID de vagão, permitindo a contagem de comboios para cada vagão.

-- Cláusula de Filtragem por Grupo (O "Universal Join" ou Divisão SQL)
-- O HAVING restringe os vagões àqueles cuja contagem de comboios distintos
-- é IGUAL ao número total de comboios de referência (definido pela subconsulta).
HAVING COUNT(DISTINCT train_id) = (

    -- Subconsulta ESSENCIAL: Determina o NÚMERO TOTAL de comboios de referência.
    -- Este valor define a condição de totalidade que a consulta principal deve satisfazer.
    SELECT COUNT(DISTINCT train_id) -- Conta o número de IDs de comboio únicos.
    FROM TRAIN_WAGON_USAGE twu    -- Usa a tabela de uso de vagões para rastrear o comboio.

    -- Junção (JOINs) para obter o tipo de vagão (Wagon Type)
             JOIN WAGON w ON twu.wagon_id = w.stock_id       -- Liga o uso ao vagão para obter o ID do modelo.
             JOIN WAGON_MODEL wm ON w.model_id = wm.model_id -- Liga ao modelo para obter o tipo de vagão.

    -- Cláusulas de Filtragem (WHERE)
    WHERE wm.wagon_type = 'Cereal wagon' -- Filtra para incluir apenas vagões do tipo 'Cereal wagon'.
      AND twu.usage_date BETWEEN DATE '2025-10-01' AND DATE '2025-10-07' -- Restringe o uso à semana de referência.
);

-- Retorno da Função
RETURN v_grain_wagons; -- Devolve o cursor de referência contendo os IDs dos vagões "universais".

END getUniversalGrainWagons;
/ -- Comando para executar e compilar a função no ambiente Oracle.