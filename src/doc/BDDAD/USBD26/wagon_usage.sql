SELECT rs.stock_id
FROM ROLLING_STOCK rs
         JOIN WAGON w ON rs.stock_id = w.stock_id
         LEFT JOIN TRAIN_WAGON_USAGE u
                   ON rs.stock_id = u.wagon_id
                       AND u.usage_date BETWEEN :start_date AND :end_date
WHERE u.wagon_id IS NULL;

# start date 2025-10-01 - end date 2025-10-02
# start date 2025-10-04 - end date 2025-10-05