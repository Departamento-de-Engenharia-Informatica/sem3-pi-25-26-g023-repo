-- =============================================
-- USBD04 - Railway System Data Population
-- =============================================

-- 1. INSERT OPERATOR DATA
INSERT INTO OPERATOR (operator_id, name, type, contact_email, phone) VALUES
                                                                         ('IP', 'Infraestruturas de Portugal, SA', 'Infrastructure Owner', 'info@ip.pt', '+351 218 112 345'),
                                                                         ('MEDWAY', 'Medway - Operador Ferroviário de Mercadorias, S.A', 'Train Operator', 'contact@medway.pt', '+351 210 456 789');

-- 2. INSERT STATION DATA (23 Stations)
INSERT INTO STATION (station_id, name, type, has_warehouse, has_refrigerated, latitude, longitude) VALUES
                                                                                                       ('ST001', 'São Romão', 'Station', 'Y', 'N', 41.468, -8.539),
                                                                                                       ('ST002', 'Tamel', 'Station', 'Y', 'N', 41.521, -8.487),
                                                                                                       ('ST003', 'Senhora das Dores', 'Station', 'N', 'N', 41.389, -8.452),
                                                                                                       ('ST004', 'Lousado', 'Station', 'Y', 'Y', 41.342, -8.429),
                                                                                                       ('ST005', 'Porto Campanhã', 'Station', 'Y', 'N', 41.149, -8.585),
                                                                                                       ('ST006', 'Leandro', 'Station', 'N', 'N', 41.312, -8.398),
                                                                                                       ('ST007', 'Porto São Bento', 'Station', 'N', 'N', 41.146, -8.610),
                                                                                                       ('ST008', 'Barcelos', 'Station', 'Y', 'N', 41.535, -8.615),
                                                                                                       ('ST009', 'Vila Nova da Cerveira', 'Station', 'N', 'N', 41.939, -8.742),
                                                                                                       ('ST010', 'Midões', 'Station', 'N', 'N', 41.267, -8.345),
                                                                                                       ('ST011', 'Valença', 'Station', 'Y', 'N', 42.030, -8.633),
                                                                                                       ('ST012', 'Darque', 'Station', 'Y', 'Y', 41.684, -8.817),
                                                                                                       ('ST013', 'Contumil', 'Station', 'N', 'N', 41.171, -8.574),
                                                                                                       ('ST014', 'Ermesinde', 'Station', 'Y', 'N', 41.220, -8.551),
                                                                                                       ('ST015', 'São Frutuoso', 'Station', 'N', 'N', 41.452, -8.512),
                                                                                                       ('ST016', 'São Pedro da Torre', 'Station', 'N', 'N', 41.987, -8.645),
                                                                                                       ('ST017', 'Viana do Castelo', 'Station', 'Y', 'Y', 41.697, -8.828),
                                                                                                       ('ST018', 'Famalicão', 'Station', 'Y', 'N', 41.408, -8.521),
                                                                                                       ('ST019', 'Barroselas', 'Station', 'N', 'N', 41.645, -8.712),
                                                                                                       ('ST020', 'Nine', 'Station', 'Y', 'N', 41.432, -8.598),
                                                                                                       ('ST021', 'Caminha', 'Station', 'Y', 'N', 41.875, -8.838),
                                                                                                       ('ST022', 'Carvalha', 'Station', 'N', 'N', 41.723, -8.765),
                                                                                                       ('ST023', 'Carreço', 'Station', 'N', 'N', 41.762, -8.795);

-- 3. INSERT RAILWAY_LINE DATA (7 Lines)
INSERT INTO RAILWAY_LINE (line_id, name, owner_operator_id, total_length_km) VALUES
                                                                                 ('L001', 'Ramal São Bento - Campanhã', 'IP', 2.6),
                                                                                 ('L002', 'Ramal Campanhã - Nine', 'IP', 39.0),
                                                                                 ('L003', 'Ramal Nine - Barcelos', 'IP', 11.3),
                                                                                 ('L004', 'Ramal Barcelos - Viana', 'IP', 30.4),
                                                                                 ('L005', 'Ramal Viana - Caminha', 'IP', 24.0),
                                                                                 ('L006', 'Ramal Caminha - Torre', 'IP', 20.8),
                                                                                 ('L007', 'Ramal Torre - Valença', 'IP', 4.3);

-- 4. INSERT LINE_SEGMENT DATA (13 Segments)
INSERT INTO LINE_SEGMENT (segment_id, line_id, start_station_id, end_station_id, segment_length_km, track_type, gauge_mm, is_electrified, max_weight_kg_per_m, max_speed_kmh) VALUES
                                                                                                                                                                                  ('SEG001', 'L001', 'ST007', 'ST005', 2.6, 'Multiple', 1668, 'Y', 8000, 80),
                                                                                                                                                                                  ('SEG002', 'L002', 'ST005', 'ST020', 29.0, 'Double', 1668, 'Y', 8000, 120),
                                                                                                                                                                                  ('SEG003', 'L002', 'ST020', 'ST018', 10.0, 'Double', 1668, 'Y', 8000, 120),
                                                                                                                                                                                  ('SEG004', 'L003', 'ST020', 'ST008', 5.3, 'Double', 1668, 'Y', 8000, 100),
                                                                                                                                                                                  ('SEG005', 'L003', 'ST008', 'ST015', 6.0, 'Double', 1668, 'Y', 8000, 100),
                                                                                                                                                                                  ('SEG006', 'L004', 'ST008', 'ST017', 10.4, 'Double', 1668, 'Y', 8000, 100),
                                                                                                                                                                                  ('SEG007', 'L004', 'ST017', 'ST019', 12.0, 'Double', 1668, 'Y', 8000, 100),
                                                                                                                                                                                  ('SEG008', 'L004', 'ST019', 'ST012', 8.0, 'Double', 1668, 'Y', 6400, 80),
                                                                                                                                                                                  ('SEG009', 'L005', 'ST017', 'ST021', 6.0, 'Double', 1668, 'Y', 8000, 100),
                                                                                                                                                                                  ('SEG010', 'L005', 'ST021', 'ST022', 3.0, 'Double', 1668, 'Y', 8000, 100),
                                                                                                                                                                                  ('SEG011', 'L005', 'ST022', 'ST023', 15.0, 'Double', 1668, 'Y', 8000, 100),
                                                                                                                                                                                  ('SEG012', 'L006', 'ST021', 'ST016', 20.8, 'Double', 1668, 'Y', 8000, 100),
                                                                                                                                                                                  ('SEG013', 'L007', 'ST016', 'ST011', 4.3, 'Double', 1668, 'Y', 8000, 100);

-- 5. INSERT ROLLING_STOCK DATA (4 Locomotives + 34 Wagons)
INSERT INTO ROLLING_STOCK (stock_id, operator_id, make, model, year_of_service, gauge_mm, length_m, width_m, height_m, tare_weight_kg, number_of_bogies) VALUES
                                                                                                                                                             -- Locomotives (4)
                                                                                                                                                             ('RS001', 'MEDWAY', 'Siemens', 'Eurosprinter', 1995, 1668, 19.2, 3.0, 4.375, 87000, 2),
                                                                                                                                                             ('RS002', 'MEDWAY', 'Siemens', 'Eurosprinter', 1995, 1668, 19.2, 3.0, 4.375, 87000, 2),
                                                                                                                                                             ('RS003', 'MEDWAY', 'Siemens', 'Eurosprinter', 1996, 1668, 19.2, 3.0, 4.375, 87000, 2),
                                                                                                                                                             ('RS004', 'MEDWAY', 'Sorefame - Alsthom', 'CP 1900', 1981, 1668, 19.084, 3.062, 4.31, 117000, 2),

                                                                                                                                                             -- Container Wagons Regmms (16)
                                                                                                                                                             ('WG001', 'MEDWAY', 'Metalsines', 'Regmms 32 94 356 3', 1987, 1668, 14.04, 3.104, 2.535, 21200, 2),
                                                                                                                                                             ('WG002', 'MEDWAY', 'Metalsines', 'Regmms 32 94 356 3', 1987, 1668, 14.04, 3.104, 2.535, 21200, 2),
                                                                                                                                                             ('WG003', 'MEDWAY', 'Metalsines', 'Regmms 32 94 356 3', 1987, 1668, 14.04, 3.104, 2.535, 21200, 2),
                                                                                                                                                             ('WG004', 'MEDWAY', 'Metalsines', 'Regmms 32 94 356 3', 1987, 1668, 14.04, 3.104, 2.535, 21200, 2),
                                                                                                                                                             ('WG005', 'MEDWAY', 'Metalsines', 'Regmms 32 94 356 3', 1987, 1668, 14.04, 3.104, 2.535, 21200, 2),
                                                                                                                                                             ('WG006', 'MEDWAY', 'Metalsines', 'Regmms 32 94 356 3', 1987, 1668, 14.04, 3.104, 2.535, 21200, 2),
                                                                                                                                                             ('WG007', 'MEDWAY', 'Metalsines', 'Regmms 32 94 356 3', 1987, 1668, 14.04, 3.104, 2.535, 21200, 2),
                                                                                                                                                             ('WG008', 'MEDWAY', 'Metalsines', 'Regmms 32 94 356 3', 1987, 1668, 14.04, 3.104, 2.535, 21200, 2),
                                                                                                                                                             ('WG009', 'MEDWAY', 'Metalsines', 'Regmms 32 94 356 3', 1987, 1668, 14.04, 3.104, 2.535, 21200, 2),
                                                                                                                                                             ('WG010', 'MEDWAY', 'Metalsines', 'Regmms 32 94 356 3', 1987, 1668, 14.04, 3.104, 2.535, 21200, 2),
                                                                                                                                                             ('WG011', 'MEDWAY', 'Metalsines', 'Regmms 32 94 356 3', 1987, 1668, 14.04, 3.104, 2.535, 21200, 2),
                                                                                                                                                             ('WG012', 'MEDWAY', 'Metalsines', 'Regmms 32 94 356 3', 1987, 1668, 14.04, 3.104, 2.535, 21200, 2),
                                                                                                                                                             ('WG013', 'MEDWAY', 'Metalsines', 'Regmms 32 94 356 3', 1987, 1668, 14.04, 3.104, 2.535, 21200, 2),
                                                                                                                                                             ('WG014', 'MEDWAY', 'Metalsines', 'Regmms 32 94 356 3', 1987, 1668, 14.04, 3.104, 2.535, 21200, 2),
                                                                                                                                                             ('WG015', 'MEDWAY', 'Metalsines', 'Regmms 32 94 356 3', 1987, 1668, 14.04, 3.104, 2.535, 21200, 2),
                                                                                                                                                             ('WG016', 'MEDWAY', 'Metalsines', 'Regmms 32 94 356 3', 1987, 1668, 14.04, 3.104, 2.535, 21200, 2),

                                                                                                                                                             -- Cereal Wagons Tadgs (4)
                                                                                                                                                             ('WG017', 'MEDWAY', 'Metalsines', 'Tadgs 32 94 082 3', 1990, 1668, 17.24, 3.072, 4.27, 24000, 2),
                                                                                                                                                             ('WG018', 'MEDWAY', 'Metalsines', 'Tadgs 32 94 082 3', 1990, 1668, 17.24, 3.072, 4.27, 24000, 2),
                                                                                                                                                             ('WG019', 'MEDWAY', 'Metalsines', 'Tadgs 32 94 082 3', 1990, 1668, 17.24, 3.072, 4.27, 24000, 2),
                                                                                                                                                             ('WG020', 'MEDWAY', 'Metalsines', 'Tadgs 32 94 082 3', 1990, 1668, 17.24, 3.072, 4.27, 24000, 2),

                                                                                                                                                             -- Cereal Wagons Tdgs (6)
                                                                                                                                                             ('WG021', 'MEDWAY', 'Equimetal', 'Tdgs 41 94 074 1', 1977, 1668, 9.64, 3.12, 4.1655, 13800, 2),
                                                                                                                                                             ('WG022', 'MEDWAY', 'Equimetal', 'Tdgs 41 94 074 1', 1977, 1668, 9.64, 3.12, 4.1655, 13800, 2),
                                                                                                                                                             ('WG023', 'MEDWAY', 'Equimetal', 'Tdgs 41 94 074 1', 1977, 1668, 9.64, 3.12, 4.1655, 13800, 2),
                                                                                                                                                             ('WG024', 'MEDWAY', 'Equimetal', 'Tdgs 41 94 074 1', 1977, 1668, 9.64, 3.12, 4.1655, 13800, 2),
                                                                                                                                                             ('WG025', 'MEDWAY', 'Equimetal', 'Tdgs 41 94 074 1', 1977, 1668, 9.64, 3.12, 4.1655, 13800, 2),
                                                                                                                                                             ('WG026', 'MEDWAY', 'Equimetal', 'Tdgs 41 94 074 1', 1977, 1668, 9.64, 3.12, 4.1655, 13800, 2),

                                                                                                                                                             -- Covered Wagons Gabs (5)
                                                                                                                                                             ('WG027', 'MEDWAY', 'Sepsa Cometna', 'Gabs 81 94 181 1', 1977, 1668, 21.70, 3.18, 4.17, 29800, 2),
                                                                                                                                                             ('WG028', 'MEDWAY', 'Sepsa Cometna', 'Gabs 81 94 181 1', 1977, 1668, 21.70, 3.18, 4.17, 29800, 2),
                                                                                                                                                             ('WG029', 'MEDWAY', 'Sepsa Cometna', 'Gabs 81 94 181 1', 1977, 1668, 21.70, 3.18, 4.17, 29800, 2),
                                                                                                                                                             ('WG030', 'MEDWAY', 'Sepsa Cometna', 'Gabs 81 94 181 1', 1977, 1668, 21.70, 3.18, 4.17, 29800, 2),
                                                                                                                                                             ('WG031', 'MEDWAY', 'Sepsa Cometna', 'Gabs 81 94 181 1', 1977, 1668, 21.70, 3.18, 4.17, 29800, 2),

                                                                                                                                                             -- Container Wagon Lgs (1)
                                                                                                                                                             ('WG032', 'MEDWAY', 'Metalsines', 'Lgs 22 94 441 6', NULL, 1668, 13.86, 2.85, 1.06, 11900, 1),

                                                                                                                                                             -- Container Wagons Sgnss (2 - uma com bitola diferente)
                                                                                                                                                             ('WG033', 'MEDWAY', 'Emef', 'Sgnss 12 94 455 2', NULL, 1668, 18.116, 2.95, 1.03, 21600, 2),
                                                                                                                                                             ('WG034', 'MEDWAY', 'Emef', 'Sgnss 12 94 455 2', NULL, 1435, 18.116, 2.95, 1.03, 21600, 2);

-- 6. INSERT LOCOMOTIVE DATA
INSERT INTO LOCOMOTIVE (stock_id, locomotive_type, power_kw, acceleration_kmh_s, max_total_weight_kg, fuel_capacity_l, supports_multiple_gauges) VALUES
                                                                                                                                                     ('RS001', 'Electric', 5600, 0.7, 180000, NULL, 'N'),
                                                                                                                                                     ('RS002', 'Electric', 5600, 0.7, 180000, NULL, 'N'),
                                                                                                                                                     ('RS003', 'Electric', 5600, 0.7, 180000, NULL, 'N'),
                                                                                                                                                     ('RS004', 'Diesel', 1623, 0.4, 200000, 4882, 'N');

-- 7. INSERT WAGON DATA (34 Wagons - 3 tipos diferentes)
INSERT INTO WAGON (stock_id, wagon_type, payload_capacity_kg, volume_capacity_m3, container_supported, is_refrigerated, max_pressure_bar) VALUES
                                                                                                                                              -- Container Wagons Regmms (16)
                                                                                                                                              ('WG001', 'Container Wagon', 60600, 76.3, '20ft,40ft,45ft', 'N', NULL),
                                                                                                                                              ('WG002', 'Container Wagon', 60600, 76.3, '20ft,40ft,45ft', 'N', NULL),
                                                                                                                                              ('WG003', 'Container Wagon', 60600, 76.3, '20ft,40ft,45ft', 'N', NULL),
                                                                                                                                              ('WG004', 'Container Wagon', 60600, 76.3, '20ft,40ft,45ft', 'N', NULL),
                                                                                                                                              ('WG005', 'Container Wagon', 60600, 76.3, '20ft,40ft,45ft', 'N', NULL),
                                                                                                                                              ('WG006', 'Container Wagon', 60600, 76.3, '20ft,40ft,45ft', 'N', NULL),
                                                                                                                                              ('WG007', 'Container Wagon', 60600, 76.3, '20ft,40ft,45ft', 'N', NULL),
                                                                                                                                              ('WG008', 'Container Wagon', 60600, 76.3, '20ft,40ft,45ft', 'N', NULL),
                                                                                                                                              ('WG009', 'Container Wagon', 60600, 76.3, '20ft,40ft,45ft', 'N', NULL),
                                                                                                                                              ('WG010', 'Container Wagon', 60600, 76.3, '20ft,40ft,45ft', 'N', NULL),
                                                                                                                                              ('WG011', 'Container Wagon', 60600, 76.3, '20ft,40ft,45ft', 'N', NULL),
                                                                                                                                              ('WG012', 'Container Wagon', 60600, 76.3, '20ft,40ft,45ft', 'N', NULL),
                                                                                                                                              ('WG013', 'Container Wagon', 60600, 76.3, '20ft,40ft,45ft', 'N', NULL),
                                                                                                                                              ('WG014', 'Container Wagon', 60600, 76.3, '20ft,40ft,45ft', 'N', NULL),
                                                                                                                                              ('WG015', 'Container Wagon', 60600, 76.3, '20ft,40ft,45ft', 'N', NULL),
                                                                                                                                              ('WG016', 'Container Wagon', 60600, 76.3, '20ft,40ft,45ft', 'N', NULL),

                                                                                                                                              -- Cereal Wagons Tadgs (4)
                                                                                                                                              ('WG017', 'Cereal Wagon', 56000, 75.0, NULL, 'N', NULL),
                                                                                                                                              ('WG018', 'Cereal Wagon', 56000, 75.0, NULL, 'N', NULL),
                                                                                                                                              ('WG019', 'Cereal Wagon', 56000, 75.0, NULL, 'N', NULL),
                                                                                                                                              ('WG020', 'Cereal Wagon', 56000, 75.0, NULL, 'N', NULL),

                                                                                                                                              -- Cereal Wagons Tdgs (6)
                                                                                                                                              ('WG021', 'Cereal Wagon', 26200, 38.0, NULL, 'N', NULL),
                                                                                                                                              ('WG022', 'Cereal Wagon', 26200, 38.0, NULL, 'N', NULL),
                                                                                                                                              ('WG023', 'Cereal Wagon', 26200, 38.0, NULL, 'N', NULL),
                                                                                                                                              ('WG024', 'Cereal Wagon', 26200, 38.0, NULL, 'N', NULL),
                                                                                                                                              ('WG025', 'Cereal Wagon', 26200, 38.0, NULL, 'N', NULL),
                                                                                                                                              ('WG026', 'Cereal Wagon', 26200, 38.0, NULL, 'N', NULL),

                                                                                                                                              -- Covered Wagons Gabs (5)
                                                                                                                                              ('WG027', 'Covered Wagon', 50200, 110.0, NULL, 'N', NULL),
                                                                                                                                              ('WG028', 'Covered Wagon', 50200, 110.0, NULL, 'N', NULL),
                                                                                                                                              ('WG029', 'Covered Wagon', 50200, 110.0, NULL, 'N', NULL),
                                                                                                                                              ('WG030', 'Covered Wagon', 50200, 110.0, NULL, 'N', NULL),
                                                                                                                                              ('WG031', 'Covered Wagon', 50200, 110.0, NULL, 'N', NULL),

                                                                                                                                              -- Container Wagon Lgs (1)
                                                                                                                                              ('WG032', 'Container Wagon', 28100, 76.3, '20ft,40ft', 'N', NULL),

                                                                                                                                              -- Container Wagons Sgnss (2)
                                                                                                                                              ('WG033', 'Container Wagon', 68400, 76.3, '20ft,40ft,45ft', 'N', NULL),
                                                                                                                                              ('WG034', 'Container Wagon', 68400, 76.3, '20ft,40ft,45ft', 'N', NULL);

-- 8. INSERT WAREHOUSE DATA
INSERT INTO WAREHOUSE (warehouse_id, name) VALUES
                                               ('WH001', 'Porto Main Warehouse'),
                                               ('WH002', 'Viana Logistics Center'),
                                               ('WH003', 'Barcelos Storage');

-- 9. INSERT BAY DATA
INSERT INTO BAY (warehouse_id, aisle, bay_number, capacity_boxes) VALUES
                                                                      ('WH001', 1, 1, 50), ('WH001', 1, 2, 50), ('WH001', 2, 1, 40),
                                                                      ('WH002', 1, 1, 60), ('WH002', 1, 2, 60), ('WH002', 2, 1, 35),
                                                                      ('WH003', 1, 1, 30), ('WH003', 1, 2, 30);

-- 10. INSERT ITEM DATA
INSERT INTO ITEM (sku, name, category, unit, volume, unit_weight) VALUES
                                                                      ('SKU001', 'Electronics Components', 'Electronics', 'units', 0.1, 0.5),
                                                                      ('SKU002', 'Cereal Grains', 'Food', 'kg', 1.0, 1.2),
                                                                      ('SKU003', 'Construction Materials', 'Building', 'units', 2.5, 3.0),
                                                                      ('SKU004', 'Automotive Parts', 'Automotive', 'units', 1.2, 2.1);

-- 11. INSERT BOX DATA
INSERT INTO BOX (box_id, qty_available, expiry_date, received_at, sku, aisle, bay, warehouse_id) VALUES
                                                                                                     ('BOX001', 100, NULL, TIMESTAMP '2024-01-15 09:00:00', 'SKU001', 1, 1, 'WH001'),
                                                                                                     ('BOX002', 500, DATE '2024-12-31', TIMESTAMP '2024-01-16 10:30:00', 'SKU002', 1, 2, 'WH001'),
                                                                                                     ('BOX003', 75, NULL, TIMESTAMP '2024-01-17 14:15:00', 'SKU003', 2, 1, 'WH001'),
                                                                                                     ('BOX004', 200, NULL, TIMESTAMP '2024-01-18 11:45:00', 'SKU004', 1, 1, 'WH002');

COMMIT;

-- Verification
SELECT 'USBD04 COMPLETED - Database populated with 4 locomotives and 34 wagons (3 types)' as status FROM DUAL;