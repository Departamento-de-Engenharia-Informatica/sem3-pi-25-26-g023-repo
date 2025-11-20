SELECT wagon_id
FROM TRAIN_WAGON_USAGE
WHERE usage_date BETWEEN DATE '2024-01-01' AND DATE '2024-12-31'
  AND wagon_id IN (SELECT stock_id FROM WAGON WHERE wagon_type = 'Cereal Wagon')
GROUP BY wagon_id
HAVING COUNT(DISTINCT train_id) = (
    SELECT COUNT(DISTINCT train_id)
    FROM TRAIN_WAGON_USAGE
    WHERE usage_date BETWEEN DATE '2024-01-01' AND DATE '2024-12-31'
      AND wagon_id IN (SELECT stock_id FROM WAGON WHERE wagon_type = 'Cereal Wagon')
);