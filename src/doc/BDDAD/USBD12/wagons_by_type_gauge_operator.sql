-- =============================================
-- USBD12 - Wagons by Type, Gauge and Operator
-- =============================================

-- MAIN QUERY - Interactive Version
SELECT
    w.stock_id as "Wagon ID",
    o.name as "Operator",
    w.wagon_type as "Wagon Type",
    rs.gauge_mm as "Gauge (mm)",
    w.payload_capacity_kg as "Payload (kg)",
    w.volume_capacity_m3 as "Volume (m³)",
    rs.make as "Make",
    rs.model as "Model",
    rs.year_of_service as "Year",
    CASE
        WHEN w.is_refrigerated = 'Y' THEN 'Yes'
        ELSE 'No'
        END as "Refrigerated"
FROM WAGON w
         JOIN ROLLING_STOCK rs ON w.stock_id = rs.stock_id
         JOIN OPERATOR o ON rs.operator_id = o.operator_id
WHERE UPPER(w.wagon_type) LIKE UPPER('%&input_wagon_type%')
  AND rs.gauge_mm = &input_gauge_mm
  AND UPPER(o.name) LIKE UPPER('%&input_operator_name%')
ORDER BY w.stock_id;


SELECT 'USBD12 READY - Use &input_wagon_type, &input_gauge_mm, &input_operator_name' as status FROM DUAL;