SELECT wagon_id
FROM TRAIN_WAGON_USAGE
WHERE usage_date BETWEEN TO_DATE('&start_date', 'YYYY-MM-DD') AND TO_DATE('&end_date', 'YYYY-MM-DD')
  AND wagon_id IN (
    SELECT w.stock_id
    FROM WAGON w
             JOIN WAGON_MODEL wm ON w.model_id = wm.model_id
    WHERE wm.wagon_type = 'Cereal Wagon'
)
GROUP BY wagon_id
HAVING COUNT(DISTINCT train_id) = (
    SELECT COUNT(DISTINCT train_id)
    FROM TRAIN_WAGON_USAGE
    WHERE usage_date BETWEEN TO_DATE('&start_date', 'YYYY-MM-DD') AND TO_DATE('&end_date', 'YYYY-MM-DD')
      AND wagon_id IN (
        SELECT w.stock_id
        FROM WAGON w
                 JOIN WAGON_MODEL wm ON w.model_id = wm.model_id
        WHERE wm.wagon_type = 'Cereal Wagon'
    )
);

# start_date : 2025-10-01
# end_date : 2025-10-07