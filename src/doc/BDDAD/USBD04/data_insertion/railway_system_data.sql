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

-- 4. INSERT FACILITY DATA
INSERT INTO FACILITY (facility_id, name) VALUES
                                             (1, 'Terminal de Carga Norte'),
                                             (2, 'Terminal de Carga Sul'),
                                             (3, 'Porto de Leixões'),
                                             (4, 'Zona Industrial de Nine');

-- 5. INSERT WAGON_MODEL DATA
INSERT INTO WAGON_MODEL (model_id, model_name, maker, wagon_type, gauge_mm) VALUES
                                                                                (1, 'Regmms 32 94 356 3', 'Manufacturer A', 'Container Wagon', 1668),
                                                                                (2, 'Tadgs 32 94 082 3', 'Manufacturer B', 'Cereal Wagon', 1668),
                                                                                (3, 'Tdgs 41 94 074 1', 'Manufacturer C', 'Cereal Wagon', 1668),
                                                                                (4, 'Gabs 81 94 181 1', 'Manufacturer D', 'Covered Wagon', 1668),
                                                                                (5, 'Kbs 41 94 333', 'Manufacturer E', 'Wood Wagon', 1668);

-- 6. INSERT ROLLING_STOCK DATA (8 Locomotives + 41 Wagons)
INSERT INTO ROLLING_STOCK (stock_id, operator_id, model, gauge_mm) VALUES
-- Locomotives (8)
('5621', 'MEDWAY', 'Eurosprinter', 1668),
('5623', 'MEDWAY', 'Eurosprinter', 1668),
('5630', 'MEDWAY', 'Eurosprinter', 1668),
('1903', 'MEDWAY', 'CP 1900', 1668),
('5034', 'MEDWAY', 'E4000', 1668),
('5036', 'MEDWAY', 'E4000', 1668),
('335.001', 'CAPTRAIN', 'E4000', 1668),
('335.003', 'CAPTRAIN', 'E4000', 1668),

-- Container Wagons Regmms (16)
('356 3 077', 'MEDWAY', 'Regmms 32 94 356 3', 1668),
('356 3 078', 'MEDWAY', 'Regmms 32 94 356 3', 1668),
('356 3 079', 'MEDWAY', 'Regmms 32 94 356 3', 1668),
('356 3 080', 'MEDWAY', 'Regmms 32 94 356 3', 1668),
('356 3 081', 'MEDWAY', 'Regmms 32 94 356 3', 1668),
('356 3 082', 'MEDWAY', 'Regmms 32 94 356 3', 1668),
('356 3 083', 'MEDWAY', 'Regmms 32 94 356 3', 1668),
('356 3 084', 'MEDWAY', 'Regmms 32 94 356 3', 1668),
('356 3 085', 'MEDWAY', 'Regmms 32 94 356 3', 1668),
('356 3 086', 'MEDWAY', 'Regmms 32 94 356 3', 1668),
('356 3 087', 'MEDWAY', 'Regmms 32 94 356 3', 1668),
('356 3 088', 'MEDWAY', 'Regmms 32 94 356 3', 1668),
('356 3 089', 'MEDWAY', 'Regmms 32 94 356 3', 1668),
('356 3 090', 'MEDWAY', 'Regmms 32 94 356 3', 1668),
('356 3 091', 'MEDWAY', 'Regmms 32 94 356 3', 1668),
('356 3 092', 'MEDWAY', 'Regmms 32 94 356 3', 1668),

-- Cereal Wagons Tadgs (4)
('082 3 045', 'MEDWAY', 'Tadgs 32 94 082 3', 1668),
('082 3 046', 'MEDWAY', 'Tadgs 32 94 082 3', 1668),
('082 3 047', 'MEDWAY', 'Tadgs 32 94 082 3', 1668),
('082 3 048', 'MEDWAY', 'Tadgs 32 94 082 3', 1668),

-- Cereal Wagons Tdgs (6)
('074 1 001', 'MEDWAY', 'Tdgs 41 94 074 1', 1668),
('074 1 002', 'MEDWAY', 'Tdgs 41 94 074 1', 1668),
('074 1 003', 'MEDWAY', 'Tdgs 41 94 074 1', 1668),
('074 1 004', 'MEDWAY', 'Tdgs 41 94 074 1', 1668),
('074 1 005', 'MEDWAY', 'Tdgs 41 94 074 1', 1668),
('074 1 006', 'MEDWAY', 'Tdgs 41 94 074 1', 1668),

-- Covered Wagons Gabs (5)
('181 1 010', 'MEDWAY', 'Gabs 81 94 181 1', 1668),
('181 1 011', 'MEDWAY', 'Gabs 81 94 181 1', 1668),
('181 1 012', 'MEDWAY', 'Gabs 81 94 181 1', 1668),
('181 1 013', 'MEDWAY', 'Gabs 81 94 181 1', 1668),
('181 1 014', 'MEDWAY', 'Gabs 81 94 181 1', 1668),

-- Wood Wagons Kbs (10)
('333 0 001', 'MEDWAY', 'Kbs 41 94 333', 1668),
('333 0 002', 'MEDWAY', 'Kbs 41 94 333', 1668),
('333 0 003', 'MEDWAY', 'Kbs 41 94 333', 1668),
('333 0 004', 'MEDWAY', 'Kbs 41 94 333', 1668),
('333 0 005', 'MEDWAY', 'Kbs 41 94 333', 1668),
('333 0 006', 'MEDWAY', 'Kbs 41 94 333', 1668),
('333 0 007', 'MEDWAY', 'Kbs 41 94 333', 1668),
('333 0 008', 'MEDWAY', 'Kbs 41 94 333', 1668),
('333 0 009', 'MEDWAY', 'Kbs 41 94 333', 1668),
('333 0 010', 'MEDWAY', 'Kbs 41 94 333', 1668);

-- 7. INSERT LOCOMOTIVE DATA
INSERT INTO LOCOMOTIVE (stock_id, locomotive_type, power_kw, supports_multiple_gauges) VALUES
                                                                                           ('5621', 'Electric', 5600, 'Y'),
                                                                                           ('5623', 'Electric', 5600, 'Y'),
                                                                                           ('5630', 'Electric', 5600, 'Y'),
                                                                                           ('1903', 'Diesel', 1623, 'N'),
                                                                                           ('5034', 'Diesel', 3178, 'N'),
                                                                                           ('5036', 'Diesel', 3178, 'N'),
                                                                                           ('335.001', 'Diesel', 3178, 'N'),
                                                                                           ('335.003', 'Diesel', 3178, 'N');

-- 8. INSERT WAGON DATA (41 Wagons)
INSERT INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES
-- Container Wagons Regmms (16)
('356 3 077', 1, 'MEDWAY', 2010),
('356 3 078', 1, 'MEDWAY', 2010),
('356 3 079', 1, 'MEDWAY', 2010),
('356 3 080', 1, 'MEDWAY', 2010),
('356 3 081', 1, 'MEDWAY', 2010),
('356 3 082', 1, 'MEDWAY', 2010),
('356 3 083', 1, 'MEDWAY', 2010),
('356 3 084', 1, 'MEDWAY', 2010),
('356 3 085', 1, 'MEDWAY', 2010),
('356 3 086', 1, 'MEDWAY', 2010),
('356 3 087', 1, 'MEDWAY', 2010),
('356 3 088', 1, 'MEDWAY', 2010),
('356 3 089', 1, 'MEDWAY', 2010),
('356 3 090', 1, 'MEDWAY', 2010),
('356 3 091', 1, 'MEDWAY', 2010),
('356 3 092', 1, 'MEDWAY', 2010),

-- Cereal Wagons Tadgs (4)
('082 3 045', 2, 'MEDWAY', 2012),
('082 3 046', 2, 'MEDWAY', 2012),
('082 3 047', 2, 'MEDWAY', 2012),
('082 3 048', 2, 'MEDWAY', 2012),

-- Cereal Wagons Tdgs (6)
('074 1 001', 3, 'MEDWAY', 2015),
('074 1 002', 3, 'MEDWAY', 2015),
('074 1 003', 3, 'MEDWAY', 2015),
('074 1 004', 3, 'MEDWAY', 2015),
('074 1 005', 3, 'MEDWAY', 2015),
('074 1 006', 3, 'MEDWAY', 2015),

-- Covered Wagons Gabs (5)
('181 1 010', 4, 'MEDWAY', 2018),
('181 1 011', 4, 'MEDWAY', 2018),
('181 1 012', 4, 'MEDWAY', 2018),
('181 1 013', 4, 'MEDWAY', 2018),
('181 1 014', 4, 'MEDWAY', 2018),

-- Wood Wagons Kbs (10)
('333 0 001', 5, 'MEDWAY', 2020),
('333 0 002', 5, 'MEDWAY', 2020),
('333 0 003', 5, 'MEDWAY', 2020),
('333 0 004', 5, 'MEDWAY', 2020),
('333 0 005', 5, 'MEDWAY', 2020),
('333 0 006', 5, 'MEDWAY', 2020),
('333 0 007', 5, 'MEDWAY', 2020),
('333 0 008', 5, 'MEDWAY', 2020),
('333 0 009', 5, 'MEDWAY', 2020),
('333 0 010', 5, 'MEDWAY', 2020);

-- 9. INSERT TRAIN DATA
INSERT INTO TRAIN (train_id, operator_id, train_date, train_time, start_facility_id, end_facility_id, locomotive_id) VALUES
                                                                                                                         ('5421', 'MEDWAY', DATE '2025-10-03', '09:45:00', 3, 4, '5621'),
                                                                                                                         ('5435', 'MEDWAY', DATE '2025-10-03', '18:00:00', 4, 3, '5623'),
                                                                                                                         ('5437', 'MEDWAY', DATE '2025-10-06', '10:00:00', 4, 3, '5621');

-- 10. INSERT FREIGHT DATA
INSERT INTO FREIGHT (freight_id, freight_date, origin_facility_id, destination_facility_id) VALUES
                                                                                                (2001, DATE '2025-10-03', 3, 4),
                                                                                                (2002, DATE '2025-10-03', 1, 4),
                                                                                                (2003, DATE '2025-10-03', 3, 4),
                                                                                                (2004, DATE '2025-10-03', 3, 2),
                                                                                                (2005, DATE '2025-10-03', 4, 4),
                                                                                                (2006, DATE '2025-10-03', 4, 3),
                                                                                                (2007, DATE '2025-10-03', 3, 1),
                                                                                                (2050, DATE '2025-10-06', 4, 3),
                                                                                                (2051, DATE '2025-10-06', 4, 3);

-- 11. INSERT FREIGHT_WAGON DATA
INSERT INTO FREIGHT_WAGON (freight_id, wagon_id) VALUES
                                                     (2001, '333 0 001'), (2001, '333 0 002'), (2001, '333 0 004'), (2001, '333 0 005'), (2001, '333 0 006'),
                                                     (2002, '356 3 089'),
                                                     (2003, '181 1 011'), (2003, '181 1 012'),
                                                     (2004, '181 1 013'),
                                                     (2005, '356 3 077'), (2005, '356 3 078'), (2005, '356 3 079'), (2005, '356 3 080'),
                                                     (2006, '333 0 003'), (2006, '333 0 007'),
                                                     (2007, '356 3 090'), (2007, '356 3 091'), (2007, '356 3 092'),
                                                     (2050, '333 0 001'), (2050, '333 0 002'), (2050, '333 0 004'), (2050, '333 0 005'), (2050, '333 0 006'),
                                                     (2051, '181 1 011'), (2051, '181 1 012'), (2051, '356 3 077'), (2051, '356 3 078'), (2051, '356 3 079'), (2051, '356 3 080');

-- 12. INSERT TRAIN_WAGON_USAGE DATA
INSERT INTO TRAIN_WAGON_USAGE (usage_id, train_id, wagon_id, usage_date) VALUES
                                                                             ('USG001', '5421', '082 3 045', DATE '2025-10-03'),
                                                                             ('USG002', '5421', '082 3 046', DATE '2025-10-03'),
                                                                             ('USG003', '5421', '074 1 001', DATE '2025-10-03'),
                                                                             ('USG004', '5421', '074 1 002', DATE '2025-10-03'),
                                                                             ('USG005', '5421', '356 3 077', DATE '2025-10-03'),
                                                                             ('USG006', '5435', '082 3 045', DATE '2025-10-03'),
                                                                             ('USG007', '5435', '074 1 001', DATE '2025-10-03'),
                                                                             ('USG008', '5435', '074 1 003', DATE '2025-10-03'),
                                                                             ('USG009', '5435', '356 3 078', DATE '2025-10-03'),
                                                                             ('USG010', '5437', '082 3 045', DATE '2025-10-06'),
                                                                             ('USG011', '5437', '074 1 001', DATE '2025-10-06'),
                                                                             ('USG012', '5437', '074 1 004', DATE '2025-10-06'),
                                                                             ('USG013', '5437', '082 3 047', DATE '2025-10-06');

COMMIT;

-- Verification
SELECT 'USBD04 COMPLETED - Database populated successfully' as status FROM DUAL;