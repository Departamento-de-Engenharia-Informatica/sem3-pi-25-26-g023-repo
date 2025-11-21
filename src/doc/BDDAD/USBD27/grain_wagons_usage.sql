-- =============================================
-- USBD27 - Grain Wagons Used in Every Train That Used Grain Wagons
-- =============================================

CREATE OR REPLACE FUNCTION getUniversalGrainWagons RETURN SYS_REFCURSOR
IS
    v_grain_wagons SYS_REFCURSOR;
BEGIN
OPEN v_grain_wagons FOR
SELECT wagon_id
FROM TRAIN_WAGON_USAGE
WHERE usage_date BETWEEN DATE '2025-10-01' AND DATE '2025-10-07'
  AND train_id IN (
    SELECT DISTINCT train_id
    FROM TRAIN_WAGON_USAGE twu
             JOIN WAGON w ON twu.wagon_id = w.stock_id
             JOIN WAGON_MODEL wm ON w.model_id = wm.model_id
    WHERE wm.wagon_type = 'Cereal wagon'
      AND twu.usage_date BETWEEN DATE '2025-10-01' AND DATE '2025-10-07'
)
GROUP BY wagon_id
HAVING COUNT(DISTINCT train_id) = (
    SELECT COUNT(DISTINCT train_id)
    FROM TRAIN_WAGON_USAGE twu
             JOIN WAGON w ON twu.wagon_id = w.stock_id
             JOIN WAGON_MODEL wm ON w.model_id = wm.model_id
    WHERE wm.wagon_type = 'Cereal wagon'
      AND twu.usage_date BETWEEN DATE '2025-10-01' AND DATE '2025-10-07'
);

RETURN v_grain_wagons;
END getUniversalGrainWagons;
/