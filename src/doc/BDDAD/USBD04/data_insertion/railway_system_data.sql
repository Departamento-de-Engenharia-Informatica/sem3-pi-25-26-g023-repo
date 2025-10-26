-- =============================================
-- USBD04 - Railway System Data Population
-- =============================================

-- 1. INSERT OPERATOR DATA
INSERT INTO OPERATOR (operator_id, name, type, contact_email, phone) VALUES
    ('OP001', 'Infraestruturas de Portugal, SA', 'Infrastructure Owner', 'info@ip.pt', '+351 218 112 345');
INSERT INTO OPERATOR (operator_id, name, type, contact_email, phone) VALUES
    ('OP002', 'Medway - Operador Ferroviário de Mercadorias, S.A', 'Train Operator', 'contact@medway.pt', '+351 210 456 789');

-- 2. INSERT STATION DATA
INSERT INTO STATION (station_id, name, type, has_warehouse, has_refrigerated, latitude, longitude) VALUES
    ('ST001', 'São Romão', 'Station', 'Y', 'N', 41.468, -8.539);
INSERT INTO STATION (station_id, name, type, has_warehouse, has_refrigerated, latitude, longitude) VALUES
    ('ST002', 'Tamel', 'Station', 'Y', 'N', 41.521, -8.487);
INSERT INTO STATION (station_id, name, type, has_warehouse, has_refrigerated, latitude, longitude) VALUES
    ('ST005', 'Porto Campanhã', 'Station', 'Y', 'N', 41.149, -8.585);
INSERT INTO STATION (station_id, name, type, has_warehouse, has_refrigerated, latitude, longitude) VALUES
    ('ST007', 'Porto São Bento', 'Station', 'N', 'N', 41.146, -8.610);
INSERT INTO STATION (station_id, name, type, has_warehouse, has_refrigerated, latitude, longitude) VALUES
    ('ST008', 'Barcelos', 'Station', 'Y', 'N', 41.535, -8.615);
INSERT INTO STATION (station_id, name, type, has_warehouse, has_refrigerated, latitude, longitude) VALUES
    ('ST011', 'Valença', 'Station', 'Y', 'N', 42.030, -8.633);
INSERT INTO STATION (station_id, name, type, has_warehouse, has_refrigerated, latitude, longitude) VALUES
    ('ST012', 'Darque', 'Station', 'Y', 'Y', 41.684, -8.817);
INSERT INTO STATION (station_id, name, type, has_warehouse, has_refrigerated, latitude, longitude) VALUES
    ('ST017', 'Viana do Castelo', 'Station', 'Y', 'Y', 41.697, -8.828);
INSERT INTO STATION (station_id, name, type, has_warehouse, has_refrigerated, latitude, longitude) VALUES
    ('ST018', 'Famalicão', 'Station', 'Y', 'N', 41.408, -8.521);
INSERT INTO STATION (station_id, name, type, has_warehouse, has_refrigerated, latitude, longitude) VALUES
    ('ST020', 'Nine', 'Station', 'Y', 'N', 41.432, -8.598);

-- 3. INSERT RAILWAY_LINE DATA
INSERT INTO RAILWAY_LINE (line_id, name, owner_operator_id, total_length_km) VALUES
    ('LN001', 'Ramal São Bento - Campanhã', 'OP001', 2.6);
INSERT INTO RAILWAY_LINE (line_id, name, owner_operator_id, total_length_km) VALUES
    ('LN002', 'Ramal Campanhã - Nine', 'OP001', 39.0);
INSERT INTO RAILWAY_LINE (line_id, name, owner_operator_id, total_length_km) VALUES
    ('LN003', 'Ramal Nine - Barcelos', 'OP001', 11.3);
INSERT INTO RAILWAY_LINE (line_id, name, owner_operator_id, total_length_km) VALUES
    ('LN004', 'Ramal Barcelos - Viana', 'OP001', 30.4);

-- 4. INSERT LINE_SEGMENT DATA
INSERT INTO LINE_SEGMENT (segment_id, line_id, start_station_id, end_station_id, segment_length_km, track_type, gauge_mm, is_electrified, max_weight_kg_per_m, max_speed_kmh) VALUES
    ('SEG001', 'LN001', 'ST007', 'ST005', 2.6, 'Multiple', 1668, 'Y', 8000, 80);
INSERT INTO LINE_SEGMENT (segment_id, line_id, start_station_id, end_station_id, segment_length_km, track_type, gauge_mm, is_electrified, max_weight_kg_per_m, max_speed_kmh) VALUES
    ('SEG002', 'LN002', 'ST005', 'ST020', 29.0, 'Double', 1668, 'Y', 8000, 120);
INSERT INTO LINE_SEGMENT (segment_id, line_id, start_station_id, end_station_id, segment_length_km, track_type, gauge_mm, is_electrified, max_weight_kg_per_m, max_speed_kmh) VALUES
    ('SEG003', 'LN002', 'ST020', 'ST018', 10.0, 'Double', 1668, 'Y', 8000, 120);
INSERT INTO LINE_SEGMENT (segment_id, line_id, start_station_id, end_station_id, segment_length_km, track_type, gauge_mm, is_electrified, max_weight_kg_per_m, max_speed_kmh) VALUES
    ('SEG004', 'LN003', 'ST020', 'ST008', 5.3, 'Double', 1668, 'Y', 8000, 100);
INSERT INTO LINE_SEGMENT (segment_id, line_id, start_station_id, end_station_id, segment_length_km, track_type, gauge_mm, is_electrified, max_weight_kg_per_m, max_speed_kmh) VALUES
    ('SEG005', 'LN003', 'ST008', 'ST018', 6.0, 'Double', 1668, 'Y', 8000, 100);
INSERT INTO LINE_SEGMENT (segment_id, line_id, start_station_id, end_station_id, segment_length_km, track_type, gauge_mm, is_electrified, max_weight_kg_per_m, max_speed_kmh) VALUES
    ('SEG006', 'LN004', 'ST008', 'ST017', 10.4, 'Double', 1668, 'Y', 8000, 100);
INSERT INTO LINE_SEGMENT (segment_id, line_id, start_station_id, end_station_id, segment_length_km, track_type, gauge_mm, is_electrified, max_weight_kg_per_m, max_speed_kmh) VALUES
    ('SEG007', 'LN004', 'ST017', 'ST012', 12.0, 'Single', 1668, 'Y', 6400, 80);

-- 5. INSERT ROLLING_STOCK DATA
INSERT INTO ROLLING_STOCK (stock_id, operator_id, make, model, year_of_service, gauge_mm, length_m, width_m, height_m, tare_weight_kg, number_of_bogies) VALUES
    ('RS001', 'OP002', 'Siemens', 'Eurosprinter', 1995, 1668, 19.2, 3.0, 4.375, 87000, 2);
INSERT INTO ROLLING_STOCK (stock_id, operator_id, make, model, year_of_service, gauge_mm, length_m, width_m, height_m, tare_weight_kg, number_of_bogies) VALUES
    ('RS002', 'OP002', 'Siemens', 'Eurosprinter', 1995, 1668, 19.2, 3.0, 4.375, 87000, 2);
INSERT INTO ROLLING_STOCK (stock_id, operator_id, make, model, year_of_service, gauge_mm, length_m, width_m, height_m, tare_weight_kg, number_of_bogies) VALUES
    ('WG001', 'OP002', 'Wagon Manufacturer', 'Container Model', 2020, 1668, 14.0, 2.8, 2.9, 22000, 2);
INSERT INTO ROLLING_STOCK (stock_id, operator_id, make, model, year_of_service, gauge_mm, length_m, width_m, height_m, tare_weight_kg, number_of_bogies) VALUES
    ('WG002', 'OP002', 'Wagon Manufacturer', 'Container Model', 2020, 1668, 14.0, 2.8, 2.9, 22000, 2);
INSERT INTO ROLLING_STOCK (stock_id, operator_id, make, model, year_of_service, gauge_mm, length_m, width_m, height_m, tare_weight_kg, number_of_bogies) VALUES
    ('WG003', 'OP002', 'Wagon Manufacturer', 'Tank Model', 2019, 1668, 12.5, 2.8, 3.2, 18000, 2);
INSERT INTO ROLLING_STOCK (stock_id, operator_id, make, model, year_of_service, gauge_mm, length_m, width_m, height_m, tare_weight_kg, number_of_bogies) VALUES
    ('WG004', 'OP002', 'Wagon Manufacturer', 'Hopper Model', 2021, 1435, 15.0, 2.9, 3.5, 25000, 2);

-- 6. INSERT LOCOMOTIVE DATA
INSERT INTO LOCOMOTIVE (stock_id, locomotive_type, power_kw, acceleration_kmh_s, max_total_weight_kg, fuel_capacity_l, supports_multiple_gauges) VALUES
    ('RS001', 'Electric', 5600, 0.7, 180000, NULL, 'N');
INSERT INTO LOCOMOTIVE (stock_id, locomotive_type, power_kw, acceleration_kmh_s, max_total_weight_kg, fuel_capacity_l, supports_multiple_gauges) VALUES
    ('RS002', 'Electric', 5600, 0.7, 180000, NULL, 'N');

-- 7. INSERT WAGON DATA
INSERT INTO WAGON (stock_id, wagon_type, payload_capacity_kg, volume_capacity_m3, container_supported, is_refrigerated, max_pressure_bar) VALUES
    ('WG001', 'Container Wagon', 60600, 76.3, '20ft,40ft,45ft', 'N', NULL);
INSERT INTO WAGON (stock_id, wagon_type, payload_capacity_kg, volume_capacity_m3, container_supported, is_refrigerated, max_pressure_bar) VALUES
    ('WG002', 'Container Wagon', 60600, 76.3, '20ft,40ft,45ft', 'N', NULL);
INSERT INTO WAGON (stock_id, wagon_type, payload_capacity_kg, volume_capacity_m3, container_supported, is_refrigerated, max_pressure_bar) VALUES
    ('WG003', 'Tank Car', 45000, 60.5, NULL, 'N', 25.5);
INSERT INTO WAGON (stock_id, wagon_type, payload_capacity_kg, volume_capacity_m3, container_supported, is_refrigerated, max_pressure_bar) VALUES
    ('WG004', 'Hopper Car', 70000, 85.0, NULL, 'N', NULL);

-- 8. INSERT WAREHOUSE DATA
INSERT INTO WAREHOUSE (warehouse_id, name) VALUES
    ('WH001', 'Porto Main Warehouse');
INSERT INTO WAREHOUSE (warehouse_id, name) VALUES
    ('WH002', 'Viana Logistics Center');

-- 9. INSERT BAY DATA
INSERT INTO BAY (warehouse_id, aisle, bay_number, capacity_boxes) VALUES
    ('WH001', 1, 1, 50);
INSERT INTO BAY (warehouse_id, aisle, bay_number, capacity_boxes) VALUES
    ('WH001', 1, 2, 50);
INSERT INTO BAY (warehouse_id, aisle, bay_number, capacity_boxes) VALUES
    ('WH002', 1, 1, 60);

-- 10. INSERT ITEM DATA
INSERT INTO ITEM (sku, name, category, unit, volume, unit_weight) VALUES
    ('SKU001', 'Electronics Components', 'Electronics', 'units', 0.1, 0.5);
INSERT INTO ITEM (sku, name, category, unit, volume, unit_weight) VALUES
    ('SKU002', 'Cereal Grains', 'Food', 'kg', 1.0, 1.2);

-- 11. INSERT BOX DATA
INSERT INTO BOX (box_id, qty_available, expiry_date, received_at, sku, aisle, bay, warehouse_id) VALUES
    ('BOX001', 100, NULL, TIMESTAMP '2024-01-15 09:00:00', 'SKU001', 1, 1, 'WH001');
INSERT INTO BOX (box_id, qty_available, expiry_date, received_at, sku, aisle, bay, warehouse_id) VALUES
    ('BOX002', 500, DATE '2024-12-31', TIMESTAMP '2024-01-16 10:30:00', 'SKU002', 1, 2, 'WH001');

COMMIT;

-- VERIFICATION
SELECT 'USBD04 COMPLETED - Data population successful' as status FROM DUAL;
SELECT 'OPERATOR: ' || COUNT(*) || ' records' FROM OPERATOR
UNION ALL SELECT 'STATION: ' || COUNT(*) || ' records' FROM STATION
UNION ALL SELECT 'RAILWAY_LINE: ' || COUNT(*) || ' records' FROM RAILWAY_LINE
UNION ALL SELECT 'LINE_SEGMENT: ' || COUNT(*) || ' records' FROM LINE_SEGMENT
UNION ALL SELECT 'ROLLING_STOCK: ' || COUNT(*) || ' records' FROM ROLLING_STOCK
UNION ALL SELECT 'WAGON: ' || COUNT(*) || ' records' FROM WAGON;