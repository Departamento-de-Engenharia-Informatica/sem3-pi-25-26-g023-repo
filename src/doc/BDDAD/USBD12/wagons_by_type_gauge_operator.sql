-- =============================================
-- USBD12 - Wagons by Type, Gauge and Operator
-- =============================================

SELECT
    w.stock_id as "Wagon ID",
    o.name as "Operator",
    wm.wagon_type as "Wagon Type",
    rs.gauge_mm as "Gauge (mm)",
    wm.payload_t as "Payload (t)"
FROM WAGON w
         JOIN WAGON_MODEL wm ON w.model_id = wm.model_id
         JOIN ROLLING_STOCK rs ON w.stock_id = rs.stock_id
         JOIN OPERATOR o ON rs.operator_id = o.operator_id
WHERE wm.wagon_type = 'Container Wagon'      -- ALTERAR: 'Cereal Wagon' ou 'Covered Wagon'
  AND rs.gauge_mm = 1668                     -- ALTERAR: 1435 para bitola diferente
  AND o.operator_id = 'MEDWAY'               -- ALTERAR: 'IP' para outro operador
ORDER BY w.stock_id;