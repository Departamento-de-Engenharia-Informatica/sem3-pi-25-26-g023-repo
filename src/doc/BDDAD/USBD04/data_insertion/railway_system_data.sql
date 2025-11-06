-- =============================================
-- USBD04 - Railway System Data Population
-- =============================================

-- 1. INSERT OPERATOR DATA
INSERT INTO OPERATOR (operator_id, name) VALUES
                                             ('IP', 'Infraestruturas de Portugal, SA'),
                                             ('MEDWAY', 'Medway - Operador Ferroviário de Mercadorias, S.A');

-- 2. INSERT STATION DATA (23 Stations)
INSERT INTO STATION (station_id, name, latitude, longitude) VALUES
                                                                ('ST001', 'São Romão', 41.468, -8.539),
                                                                ('ST002', 'Tamel', 41.521, -8.487),
                                                                ('ST003', 'Senhora das Dores', 41.389, -8.452),
                                                                ('ST004', 'Lousado', 41.342, -8.429),
                                                                ('ST005', 'Porto Campanhã', 41.149, -8.585),
                                                                ('ST006', 'Leandro', 41.312, -8.398),
                                                                ('ST007', 'Porto São Bento', 41.146, -8.610),
                                                                ('ST008', 'Barcelos', 41.535, -8.615),
                                                                ('ST009', 'Vila Nova da Cerveira', 41.939, -8.742),
                                                                ('ST010', 'Midões', 41.267, -8.345),
                                                                ('ST011', 'Valença', 42.030, -8.633),
                                                                ('ST012', 'Darque', 41.684, -8.817),
                                                                ('ST013', 'Contumil', 41.171, -8.574),
                                                                ('ST014', 'Ermesinde', 41.220, -8.551),
                                                                ('ST015', 'São Frutuoso', 41.452, -8.512),
                                                                ('ST016', 'São Pedro da Torre', 41.987, -8.645),
                                                                ('ST017', 'Viana do Castelo', 41.697, -8.828),
                                                                ('ST018', 'Famalicão', 41.408, -8.521),
                                                                ('ST019', 'Barroselas', 41.645, -8.712),
                                                                ('ST020', 'Nine', 41.432, -8.598),
                                                                ('ST021', 'Caminha', 41.875, -8.838),
                                                                ('ST022', 'Carvalha', 41.723, -8.765),
                                                                ('ST023', 'Carreço', 41.762, -8.795);

-- 3. INSERT RAILWAY_LINE DATA (7 Lines)
INSERT INTO RAILWAY_LINE (line_id, name, owner_operator_id) VALUES
                                                                ('L001', 'Ramal São Bento - Campanhã', 'IP'),
                                                                ('L002', 'Ramal Campanhã - Nine', 'IP'),
                                                                ('L003', 'Ramal Nine - Barcelos', 'IP'),
                                                                ('L004', 'Ramal Barcelos - Viana', 'IP'),
                                                                ('L005', 'Ramal Viana - Caminha', 'IP'),
                                                                ('L006', 'Ramal Caminha - Torre', 'IP'),
                                                                ('L007', 'Ramal Torre - Valença', 'IP');

-- 4. INSERT LINE_SEGMENT DATA (13 Segments)
INSERT INTO LINE_SEGMENT (segment_id, line_id, start_station_id, end_station_id, segment_length_km, track_type, gauge_mm, is_electrified) VALUES
                                                                                                                                              ('SEG001', 'L001', 'ST007', 'ST005', 2.6, 'Multiple', 1668, 'Y'),
                                                                                                                                              ('SEG002', 'L002', 'ST005', 'ST020', 29.0, 'Double', 1668, 'Y'),
                                                                                                                                              ('SEG003', 'L002', 'ST020', 'ST018', 10.0, 'Double', 1668, 'Y'),
                                                                                                                                              ('SEG004', 'L003', 'ST020', 'ST008', 5.3, 'Double', 1668, 'Y'),
                                                                                                                                              ('SEG005', 'L003', 'ST008', 'ST015', 6.0, 'Double', 1668, 'Y'),
                                                                                                                                              ('SEG006', 'L004', 'ST008', 'ST017', 10.4, 'Double', 1668, 'Y'),
                                                                                                                                              ('SEG007', 'L004', 'ST017', 'ST019', 12.0, 'Double', 1668, 'Y'),
                                                                                                                                              ('SEG008', 'L004', 'ST019', 'ST012', 8.0, 'Double', 1668, 'Y'),
                                                                                                                                              ('SEG009', 'L005', 'ST017', 'ST021', 6.0, 'Double', 1668, 'Y'),
                                                                                                                                              ('SEG010', 'L005', 'ST021', 'ST022', 3.0, 'Double', 1668, 'Y'),
                                                                                                                                              ('SEG011', 'L005', 'ST022', 'ST023', 15.0, 'Double', 1668, 'Y'),
                                                                                                                                              ('SEG012', 'L006', 'ST021', 'ST016', 20.8, 'Double', 1668, 'Y'),
                                                                                                                                              ('SEG013', 'L007', 'ST016', 'ST011', 4.3, 'Double', 1668, 'Y');

-- 5. INSERT ROLLING_STOCK DATA (4 Locomotives + 34 Wagons)
INSERT INTO ROLLING_STOCK (stock_id, operator_id, model, gauge_mm) VALUES
-- Locomotives (4)
('RS001', 'MEDWAY', 'Eurosprinter', 1668),
('RS002', 'MEDWAY', 'Eurosprinter', 1668),
('RS003', 'MEDWAY', 'Eurosprinter', 1668),
('RS004', 'MEDWAY', 'CP 1900', 1668),

-- Container Wagons Regmms (16)
('WG001', 'MEDWAY', 'Regmms 32 94 356 3', 1668),
('WG002', 'MEDWAY', 'Regmms 32 94 356 3', 1668),
('WG003', 'MEDWAY', 'Regmms 32 94 356 3', 1668),
('WG004', 'MEDWAY', 'Regmms 32 94 356 3', 1668),
('WG005', 'MEDWAY', 'Regmms 32 94 356 3', 1668),
('WG006', 'MEDWAY', 'Regmms 32 94 356 3', 1668),
('WG007', 'MEDWAY', 'Regmms 32 94 356 3', 1668),
('WG008', 'MEDWAY', 'Regmms 32 94 356 3', 1668),
('WG009', 'MEDWAY', 'Regmms 32 94 356 3', 1668),
('WG010', 'MEDWAY', 'Regmms 32 94 356 3', 1668),
('WG011', 'MEDWAY', 'Regmms 32 94 356 3', 1668),
('WG012', 'MEDWAY', 'Regmms 32 94 356 3', 1668),
('WG013', 'MEDWAY', 'Regmms 32 94 356 3', 1668),
('WG014', 'MEDWAY', 'Regmms 32 94 356 3', 1668),
('WG015', 'MEDWAY', 'Regmms 32 94 356 3', 1668),
('WG016', 'MEDWAY', 'Regmms 32 94 356 3', 1668),

-- Cereal Wagons Tadgs (4)
('WG017', 'MEDWAY', 'Tadgs 32 94 082 3', 1668),
('WG018', 'MEDWAY', 'Tadgs 32 94 082 3', 1668),
('WG019', 'MEDWAY', 'Tadgs 32 94 082 3', 1668),
('WG020', 'MEDWAY', 'Tadgs 32 94 082 3', 1668),

-- Cereal Wagons Tdgs (6)
('WG021', 'MEDWAY', 'Tdgs 41 94 074 1', 1668),
('WG022', 'MEDWAY', 'Tdgs 41 94 074 1', 1668),
('WG023', 'MEDWAY', 'Tdgs 41 94 074 1', 1668),
('WG024', 'MEDWAY', 'Tdgs 41 94 074 1', 1668),
('WG025', 'MEDWAY', 'Tdgs 41 94 074 1', 1668),
('WG026', 'MEDWAY', 'Tdgs 41 94 074 1', 1668),

-- Covered Wagons Gabs (5)
('WG027', 'MEDWAY', 'Gabs 81 94 181 1', 1668),
('WG028', 'MEDWAY', 'Gabs 81 94 181 1', 1668),
('WG029', 'MEDWAY', 'Gabs 81 94 181 1', 1668),
('WG030', 'MEDWAY', 'Gabs 81 94 181 1', 1668),
('WG031', 'MEDWAY', 'Gabs 81 94 181 1', 1668),

-- Container Wagon Lgs (1)
('WG032', 'MEDWAY', 'Lgs 22 94 441 6', 1668),

-- Container Wagons Sgnss (2 - uma com bitola diferente)
('WG033', 'MEDWAY', 'Sgnss 12 94 455 2', 1668),
('WG034', 'MEDWAY', 'Sgnss 12 94 455 2', 1435);

-- 6. INSERT LOCOMOTIVE DATA
INSERT INTO LOCOMOTIVE (stock_id, locomotive_type, power_kw, supports_multiple_gauges) VALUES
                                                                                           ('RS001', 'Electric', 5600, 'Y'),
                                                                                           ('RS002', 'Electric', 5600, 'N'),
                                                                                           ('RS003', 'Electric', 5600, 'Y'),
                                                                                           ('RS004', 'Diesel', 1623, 'N');

-- 7. INSERT WAGON DATA (34 Wagons - 3 tipos diferentes)
INSERT INTO WAGON (stock_id, wagon_type, payload_capacity_kg) VALUES
-- Container Wagons Regmms (16)
('WG001', 'Container Wagon', 60600),
('WG002', 'Container Wagon', 60600),
('WG003', 'Container Wagon', 60600),
('WG004', 'Container Wagon', 60600),
('WG005', 'Container Wagon', 60600),
('WG006', 'Container Wagon', 60600),
('WG007', 'Container Wagon', 60600),
('WG008', 'Container Wagon', 60600),
('WG009', 'Container Wagon', 60600),
('WG010', 'Container Wagon', 60600),
('WG011', 'Container Wagon', 60600),
('WG012', 'Container Wagon', 60600),
('WG013', 'Container Wagon', 60600),
('WG014', 'Container Wagon', 60600),
('WG015', 'Container Wagon', 60600),
('WG016', 'Container Wagon', 60600),

-- Cereal Wagons Tadgs (4)
('WG017', 'Cereal Wagon', 56000),
('WG018', 'Cereal Wagon', 56000),
('WG019', 'Cereal Wagon', 56000),
('WG020', 'Cereal Wagon', 56000),

-- Cereal Wagons Tdgs (6)
('WG021', 'Cereal Wagon', 26200),
('WG022', 'Cereal Wagon', 26200),
('WG023', 'Cereal Wagon', 26200),
('WG024', 'Cereal Wagon', 26200),
('WG025', 'Cereal Wagon', 26200),
('WG026', 'Cereal Wagon', 26200),

-- Covered Wagons Gabs (5)
('WG027', 'Covered Wagon', 50200),
('WG028', 'Covered Wagon', 50200),
('WG029', 'Covered Wagon', 50200),
('WG030', 'Covered Wagon', 50200),
('WG031', 'Covered Wagon', 50200),

-- Container Wagon Lgs (1)
('WG032', 'Container Wagon', 28100),

-- Container Wagons Sgnss (2)
('WG033', 'Container Wagon', 68400),
('WG034', 'Container Wagon', 68400);

COMMIT;

-- Verification
SELECT 'USBD04 COMPLETED - Database populated with 4 locomotives and 34 wagons (3 types)' as status FROM DUAL;