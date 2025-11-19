-- =============================================
-- USBD04 - Railway System Data Population
-- =============================================

-- 1. INSERT OPERATOR DATA
INSERT INTO OPERATOR (operator_id, name) VALUES
                                             ('IP', 'Infraestruturas de Portugal, SA'),
                                             ('MEDWAY', 'Medway - Operador Ferroviário de Mercadorias, S.A'),
                                             ('CAPTRAIN', 'Captrain Portugal S.A.');

-- 2. INSERT STATION DATA (27 Stations)
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
                                                                ('ST023', 'Carreço', 41.762, -8.795),
                                                                ('ST024', 'Leixões', 41.182, -8.702),
                                                                ('ST025', 'São Mamede de Infesta', 41.183, -8.602),
                                                                ('ST026', 'Leça do Balio', 41.212, -8.635),
                                                                ('ST027', 'São Gemil', 41.176, -8.589);

-- 3. INSERT RAILWAY_LINE DATA (13 Lines)
INSERT INTO RAILWAY_LINE (line_id, name, owner_operator_id) VALUES
                                                                ('L001', 'Ramal São Bento - Campanhã', 'IP'),
                                                                ('L002', 'Ramal Camapanhã - Contumil', 'IP'),
                                                                ('L003', 'Ramal Contumil - Nine', 'IP'),
                                                                ('L004', 'Ramal Nine - Barcelos', 'IP'),
                                                                ('L005', 'Ramal Barcelos - Darque', 'IP'),
                                                                ('L006', 'Ramal Darque - Viana', 'IP'),
                                                                ('L007', 'Ramal Viana - Caminha', 'IP'),
                                                                ('L008', 'Ramal Caminha - Torre', 'IP'),
                                                                ('L009', 'Ramal Torre - Valença', 'IP'),
                                                                ('L010', 'Ramal Contumil - São Gemil', 'IP'),
                                                                ('L011', 'Ramal São Gemil - São Mamede de Infesta', 'IP'),
                                                                ('L012', 'Ramal São Mamede de Infesta - Leça do Balio', 'IP'),
                                                                ('L013', 'Ramal Leça do Balio - Leixões', 'IP');

-- 4. INSERT LINE_SEGMENT DATA (22 Segments)
INSERT INTO LINE_SEGMENT (segment_id, line_id, start_station_id, end_station_id, segment_length_km, track_type, gauge_mm, is_electrified) VALUES
                                                                                                                                              ('SEG001', 'L001', 'ST007', 'ST005', 2.618, 'Quadruple', 1668, 'Y'),
                                                                                                                                              ('SEG002', 'L002', 'ST005', 'ST013', 2.443, 'Quadruple', 1668, 'Y'),
                                                                                                                                              ('SEG003', 'L003', 'ST013', 'ST020', 26.560, 'Double', 1668, 'Y'),
                                                                                                                                              ('SEG004', 'L003', 'ST020', 'ST018', 10.000, 'Double', 1668, 'Y'),
                                                                                                                                              ('SEG005', 'L004', 'ST020', 'ST008', 5.286, 'Double', 1668, 'Y'),
                                                                                                                                              ('SEG006', 'L004', 'ST008', 'ST015', 6.000, 'Double', 1668, 'Y'),
                                                                                                                                              ('SEG007', 'L005', 'ST008', 'ST012', 10.387, 'Double', 1668, 'Y'),
                                                                                                                                              ('SEG008', 'L005', 'ST012', 'ST017', 12.000, 'Double', 1668, 'Y'),
                                                                                                                                              ('SEG009', 'L005', 'ST017', 'ST019', 3.100, 'Double', 1668, 'Y'),
                                                                                                                                              ('SEG010', 'L006', 'ST012', 'ST017', 4.890, 'Double', 1668, 'Y'),
                                                                                                                                              ('SEG011', 'L007', 'ST017', 'ST021', 6.000, 'Single', 1668, 'Y'),
                                                                                                                                              ('SEG012', 'L007', 'ST021', 'ST022', 5.000, 'Single', 1668, 'Y'),
                                                                                                                                              ('SEG013', 'L007', 'ST022', 'ST023', 12.000, 'Single', 1668, 'Y'),
                                                                                                                                              ('SEG014', 'L008', 'ST021', 'ST016', 20.829, 'Single', 1668, 'Y'),
                                                                                                                                              ('SEG015', 'L009', 'ST016', 'ST011', 4.264, 'Single', 1668, 'Y'),
                                                                                                                                              ('SEG016', 'L010', 'ST013', 'ST027', 3.883, 'Double', 1668, 'Y'),
                                                                                                                                              ('SEG017', 'L011', 'ST027', 'ST025', 1.174, 'Double', 1668, 'Y'),
                                                                                                                                              ('SEG018', 'L011', 'ST025', 'ST026', 2.534, 'Double', 1668, 'Y'),
                                                                                                                                              ('SEG019', 'L012', 'ST025', 'ST026', 1.566, 'Double', 1668, 'Y'),
                                                                                                                                              ('SEG020', 'L012', 'ST026', 'ST024', 1.453, 'Double', 1668, 'Y'),
                                                                                                                                              ('SEG021', 'L013', 'ST026', 'ST024', 3.597, 'Double', 1668, 'Y'),
                                                                                                                                              ('SEG022', 'L013', 'ST024', 'ST026', 4.334, 'Double', 1668, 'Y');

-- 5. INSERT ROLLING_STOCK DATA (8 Locomotives + 41 Wagons)
INSERT INTO ROLLING_STOCK (stock_id, operator_id, model, gauge_mm) VALUES
-- Locomotives (8)
('RS001', 'MEDWAY', 'Eurosprinter', 1668),
('RS002', 'MEDWAY', 'Eurosprinter', 1668),
('RS003', 'MEDWAY', 'Eurosprinter', 1668),
('RS004', 'MEDWAY', 'CP 1900', 1668),
('RS005', 'MEDWAY', 'E4000', 1668),
('RS006', 'MEDWAY', 'E4000', 1668),
('RS007', 'CAPTRAIN', 'E4000', 1668),
('RS008', 'CAPTRAIN', 'E4000', 1668),

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

-- Wood Wagons Kbs (10)
('WG032', 'MEDWAY', 'Kbs 41 94 333', 1668),
('WG033', 'MEDWAY', 'Kbs 41 94 333', 1668),
('WG034', 'MEDWAY', 'Kbs 41 94 333', 1668),
('WG035', 'MEDWAY', 'Kbs 41 94 333', 1668),
('WG036', 'MEDWAY', 'Kbs 41 94 333', 1668),
('WG037', 'MEDWAY', 'Kbs 41 94 333', 1668),
('WG038', 'MEDWAY', 'Kbs 41 94 333', 1668),
('WG039', 'MEDWAY', 'Kbs 41 94 333', 1668),
('WG040', 'MEDWAY', 'Kbs 41 94 333', 1668),
('WG041', 'MEDWAY', 'Kbs 41 94 333', 1668);

-- 6. INSERT LOCOMOTIVE DATA
INSERT INTO LOCOMOTIVE (stock_id, locomotive_type, power_kw, supports_multiple_gauges) VALUES
                                                                                           ('RS001', 'Electric', 5600, 'Y'),
                                                                                           ('RS002', 'Electric', 5600, 'Y'),
                                                                                           ('RS003', 'Electric', 5600, 'Y'),
                                                                                           ('RS004', 'Diesel', 1623, 'N'),
                                                                                           ('RS005', 'Diesel', 3178, 'N'),
                                                                                           ('RS006', 'Diesel', 3178, 'N'),
                                                                                           ('RS007', 'Diesel', 3178, 'N'),
                                                                                           ('RS008', 'Diesel', 3178, 'N');

-- 7. INSERT WAGON DATA (41 Wagons)
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

-- Wood Wagons Kbs (10)
('WG032', 'Wood Wagon', 25800),
('WG033', 'Wood Wagon', 25800),
('WG034', 'Wood Wagon', 25800),
('WG035', 'Wood Wagon', 25800),
('WG036', 'Wood Wagon', 25800),
('WG037', 'Wood Wagon', 25800),
('WG038', 'Wood Wagon', 25800),
('WG039', 'Wood Wagon', 25800),
('WG040', 'Wood Wagon', 25800),
('WG041', 'Wood Wagon', 25800);

COMMIT;

-- Verification
SELECT 'USBD04 COMPLETED - Database populated with 8 locomotives and 41 wagons' as status FROM DUAL;