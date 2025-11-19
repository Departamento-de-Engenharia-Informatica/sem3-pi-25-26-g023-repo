SELECT w.stock_id
FROM WAGON w
LEFT JOIN TRAIN_WAGON_USAGE u
       ON w.stock_id = u.wagon_id
       AND u.usage_date BETWEEN :start_date AND :end_date
WHERE u.wagon_id IS NULL;
