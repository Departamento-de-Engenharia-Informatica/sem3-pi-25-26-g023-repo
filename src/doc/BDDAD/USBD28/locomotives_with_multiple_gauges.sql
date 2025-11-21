-- =============================================
-- USBD28 - Locomotives with Multiple Gauges
-- =============================================

CREATE OR REPLACE FUNCTION getMultiGaugeLocomotives(
    p_operator_id IN OPERATOR.operator_id%TYPE DEFAULT 'MEDWAY'
) RETURN SYS_REFCURSOR
IS
    v_locomotives SYS_REFCURSOR;
BEGIN
OPEN v_locomotives FOR
SELECT
    l.stock_id as "Locomotive ID",
    o.name as "Operator",
    l.locomotive_type as "Type",
    l.power_kw as "Power (kW)",
    CASE
        WHEN l.supports_multiple_gauges = 'Y' THEN 'Yes'
        ELSE 'No'
        END as "Supports Multiple Gauges"
FROM LOCOMOTIVE l
         JOIN ROLLING_STOCK rs ON l.stock_id = rs.stock_id
         JOIN OPERATOR o ON rs.operator_id = o.operator_id
WHERE l.supports_multiple_gauges = 'Y'
  AND o.operator_id = p_operator_id;

RETURN v_locomotives;
END getMultiGaugeLocomotives;
/