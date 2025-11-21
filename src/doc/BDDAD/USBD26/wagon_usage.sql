-- =============================================
-- USBD26 - Wagons Not Used in Any Train in Given Period
-- =============================================

CREATE OR REPLACE FUNCTION getUnusedWagonsInPeriod(
    p_start_date IN VARCHAR2,
    p_end_date IN VARCHAR2
) RETURN SYS_REFCURSOR
IS
    v_unused_wagons SYS_REFCURSOR;
BEGIN
OPEN v_unused_wagons FOR
SELECT rs.stock_id as "Wagon ID"
FROM ROLLING_STOCK rs
         JOIN WAGON w ON rs.stock_id = w.stock_id
         LEFT JOIN TRAIN_WAGON_USAGE u
                   ON rs.stock_id = u.wagon_id
                       AND u.usage_date BETWEEN TO_DATE(p_start_date, 'YYYY-MM-DD') AND TO_DATE(p_end_date, 'YYYY-MM-DD')
WHERE u.wagon_id IS NULL;

RETURN v_unused_wagons;
END getUnusedWagonsInPeriod;
/