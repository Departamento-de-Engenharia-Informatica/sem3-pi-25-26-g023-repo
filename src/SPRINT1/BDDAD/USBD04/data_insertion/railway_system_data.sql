-- =============================================
-- USBD04 - Railway System Data Population
-- Complete dataset insertion from Excel provided
-- =============================================

-- 1. INSERT OWNER DATA
INSERT INTO OWNER (owner_id, name, short_name, vat_number) VALUES 
('IP', 'Infraestruturas de Portugal, SA', 'IP', 'PT503933813');

-- 2. INSERT OPERATOR DATA
INSERT INTO OPERATOR (operator_id, name, short_name, vat_number) VALUES 
('MEDWAY', 'Medway - Operador Ferroviário de Mercadorias, S.A', 'Medway', 'PT509017800');

-- 3. INSERT FACILITY DATA (23 Facilities)
INSERT INTO FACILITY (facility_id, name) VALUES (1, 'São Romão');
INSERT INTO FACILITY (facility_id, name) VALUES (2, 'Tamel');
INSERT INTO FACILITY (facility_id, name) VALUES (3, 'Senhora das Dores');
INSERT INTO FACILITY (facility_id, name) VALUES (4, 'Lousado');
INSERT INTO FACILITY (facility_id, name) VALUES (5, 'Porto Campanhã');
INSERT INTO FACILITY (facility_id, name) VALUES (6, 'Leandro');
INSERT INTO FACILITY (facility_id, name) VALUES (7, 'Porto São Bento');
INSERT INTO FACILITY (facility_id, name) VALUES (8, 'Barcelos');
INSERT INTO FACILITY (facility_id, name) VALUES (9, 'Vila Nova da Cerveira');
INSERT INTO FACILITY (facility_id, name) VALUES (10, 'Midões');
INSERT INTO FACILITY (facility_id, name) VALUES (11, 'Valença');
INSERT INTO FACILITY (facility_id, name) VALUES (12, 'Darque');
INSERT INTO FACILITY (facility_id, name) VALUES (13, 'Contumil');
INSERT INTO FACILITY (facility_id, name) VALUES (14, 'Ermesinde');
INSERT INTO FACILITY (facility_id, name) VALUES (15, 'São Frutuoso');
INSERT INTO FACILITY (facility_id, name) VALUES (16, 'São Pedro da Torre');
INSERT INTO FACILITY (facility_id, name) VALUES (17, 'Viana do Castelo');
INSERT INTO FACILITY (facility_id, name) VALUES (18, 'Famalicão');
INSERT INTO FACILITY (facility_id, name) VALUES (19, 'Barroselas');
INSERT INTO FACILITY (facility_id, name) VALUES (20, 'Nine');
INSERT INTO FACILITY (facility_id, name) VALUES (21, 'Caminha');
INSERT INTO FACILITY (facility_id, name) VALUES (22, 'Carvalha');
INSERT INTO FACILITY (facility_id, name) VALUES (23, 'Carreço');

-- 4. INSERT RAILWAY_LINE DATA (7 Lines)
INSERT INTO RAILWAY_LINE (line_id, name, owner_id, start_facility_id, end_facility_id, gauge) VALUES 
(1, 'Ramal São Bento - Campanhã', 'IP', 7, 5, 1668);
INSERT INTO RAILWAY_LINE (line_id, name, owner_id, start_facility_id, end_facility_id, gauge) VALUES 
(2, 'Ramal Camapanhã - Nine', 'IP', 5, 20, 1668);
INSERT INTO RAILWAY_LINE (line_id, name, owner_id, start_facility_id, end_facility_id, gauge) VALUES 
(3, 'Ramal Nine - Barcelos', 'IP', 20, 8, 1668);
INSERT INTO RAILWAY_LINE (line_id, name, owner_id, start_facility_id, end_facility_id, gauge) VALUES 
(4, 'Ramal Barcelos - Viana', 'IP', 8, 17, 1668);
INSERT INTO RAILWAY_LINE (line_id, name, owner_id, start_facility_id, end_facility_id, gauge) VALUES 
(5, 'Ramal viana - Caminha', 'IP', 17, 21, 1668);
INSERT INTO RAILWAY_LINE (line_id, name, owner_id, start_facility_id, end_facility_id, gauge) VALUES 
(6, 'Ramal Caminha - Torre', 'IP', 21, 16, 1668);
INSERT INTO RAILWAY_LINE (line_id, name, owner_id, start_facility_id, end_facility_id, gauge) VALUES 
(7, 'Ramal Torre - Valença', 'IP', 16, 11, 1668);

-- 5. INSERT LINE_SEGMENT DATA (13 Segments)
INSERT INTO LINE_SEGMENT (segment_id, line_id, segment_order, electrified, max_weight_kg_m, length_m, number_tracks) VALUES 
(1, 1, 1, 'Yes', 8000, 2618, 4);
INSERT INTO LINE_SEGMENT (segment_id, line_id, segment_order, electrified, max_weight_kg_m, length_m, number_tracks) VALUES 
(10, 2, 1, 'Yes', 8000, 29003, 2);
INSERT INTO LINE_SEGMENT (segment_id, line_id, segment_order, electrified, max_weight_kg_m, length_m, number_tracks) VALUES 
(11, 2, 2, 'Yes', 8000, 10000, 2);
INSERT INTO LINE_SEGMENT (segment_id, line_id, segment_order, electrified, max_weight_kg_m, length_m, number_tracks) VALUES 
(15, 3, 1, 'Yes', 8000, 5286, 2);
INSERT INTO LINE_SEGMENT (segment_id, line_id, segment_order, electrified, max_weight_kg_m, length_m, number_tracks) VALUES 
(16, 3, 2, 'Yes', 8000, 6000, 2);
INSERT INTO LINE_SEGMENT (segment_id, line_id, segment_order, electrified, max_weight_kg_m, length_m, number_tracks) VALUES 
(14, 4, 1, 'Yes', 8000, 10387, 2);
INSERT INTO LINE_SEGMENT (segment_id, line_id, segment_order, electrified, max_weight_kg_m, length_m, number_tracks) VALUES 
(12, 4, 2, 'Yes', 8000, 12000, 2);
INSERT INTO LINE_SEGMENT (segment_id, line_id, segment_order, electrified, max_weight_kg_m, length_m, number_tracks) VALUES 
(13, 4, 3, 'Yes', 6400, 8000, 2);
INSERT INTO LINE_SEGMENT (segment_id, line_id, segment_order, electrified, max_weight_kg_m, length_m, number_tracks) VALUES 
(20, 5, 1, 'Yes', 8000, 6000, 2);
INSERT INTO LINE_SEGMENT (segment_id, line_id, segment_order, electrified, max_weight_kg_m, length_m, number_tracks) VALUES 
(21, 5, 2, 'Yes', 8000, 3000, 2);
INSERT INTO LINE_SEGMENT (segment_id, line_id, segment_order, electrified, max_weight_kg_m, length_m, number_tracks) VALUES 
(22, 5, 3, 'Yes', 8000, 15000, 2);
INSERT INTO LINE_SEGMENT (segment_id, line_id, segment_order, electrified, max_weight_kg_m, length_m, number_tracks) VALUES 
(25, 6, 1, 'Yes', 8000, 20829, 2);
INSERT INTO LINE_SEGMENT (segment_id, line_id, segment_order, electrified, max_weight_kg_m, length_m, number_tracks) VALUES 
(26, 7, 1, 'Yes', 8000, 4264, 2);

-- 6. INSERT LOCOMOTIVE DATA (4 Locomotives)
INSERT INTO ROLLING_STOCK (stock_id, operator_id, name, make, model, service_year, number_bogies, bogies_type, power, length_m, width_m, height_m, weight_t, max_speed, operational_speed, traction_kn, type, voltage, frequency, gauge, fuel_l) VALUES 
('5621', 'MEDWAY', 'Inês', 'Siemens', 'Eurosprinter', 1995, 2, 'Bo-Bo', 5600, 19.2, 3, 4.375, 87, 220, 70, 300, 'Electric', '25 KV', '50 Hz', 1668, NULL);
INSERT INTO ROLLING_STOCK (stock_id, operator_id, name, make, model, service_year, number_bogies, bogies_type, power, length_m, width_m, height_m, weight_t, max_speed, operational_speed, traction_kn, type, voltage, frequency, gauge, fuel_l) VALUES 
('5623', 'MEDWAY', 'Paz', 'Siemens', 'Eurosprinter', 1995, 2, 'Bo-Bo', 5600, 19.2, 3, 4.375, 87, 220, 70, 300, 'Electric', '25 KV', '50 Hz', 1668, NULL);
INSERT INTO ROLLING_STOCK (stock_id, operator_id, name, make, model, service_year, number_bogies, bogies_type, power, length_m, width_m, height_m, weight_t, max_speed, operational_speed, traction_kn, type, voltage, frequency, gauge, fuel_l) VALUES 
('5630', 'MEDWAY', 'Helena', 'Siemens', 'Eurosprinter', 1996, 2, 'Bo-Bo', 5600, 19.2, 3, 4.375, 87, 220, 70, 300, 'Electric', '25 KV', '50 Hz', 1668, NULL);
INSERT INTO ROLLING_STOCK (stock_id, operator_id, name, make, model, service_year, number_bogies, bogies_type, power, length_m, width_m, height_m, weight_t, max_speed, operational_speed, traction_kn, type, voltage, frequency, gauge, fuel_l) VALUES 
('1903', 'MEDWAY', 'Eva', 'Sorefame - Alsthom', 'CP 1900', 1981, 2, 'Co-Co', 1623, 19.084, 3.062, 4.31, 117, 100, 42.5, 396, 'Diesel', NULL, NULL, 1668, 4882);

-- 7. INSERT WAGON_MODEL DATA (7 Models)
INSERT INTO WAGON_MODEL (model_id, maker, number_bogies, bogies, length_mm, width_mm, height_mm, weight_t, max_speed, payload_t, volume_m3, type, gauge) VALUES 
('Tadgs 32 94 082 3', 'Metalsines', 2, 'Duplo', 17240, 3072, 4270, 24, 120, 56, 75, 'Cereal wagon', 1668);
INSERT INTO WAGON_MODEL (model_id, maker, number_bogies, bogies, length_mm, width_mm, height_mm, weight_t, max_speed, payload_t, volume_m3, type, gauge) VALUES 
('Tdgs 41 94 074 1', 'Equimetal', 2, 'Duplo', 9640, 3120, 4165.5, 13.8, 100, 26.2, 38, 'Cereal wagon', 1668);
INSERT INTO WAGON_MODEL (model_id, maker, number_bogies, bogies, length_mm, width_mm, height_mm, weight_t, max_speed, payload_t, volume_m3, type, gauge) VALUES 
('Gabs 81 94 181 1', 'Sepsa Cometna', 2, 'Duplo', 21700, 3180, 4170, 29.8, 100, 50.2, 110, 'Covered wagon with sliding door', 1668);
INSERT INTO WAGON_MODEL (model_id, maker, number_bogies, bogies, length_mm, width_mm, height_mm, weight_t, max_speed, payload_t, volume_m3, type, gauge) VALUES 
('Regmms 32 94 356 3', 'Metalsines', 2, 'Duplo', 14040, 3104, 2535, 21.2, 120, 60.6, 76.3, 'Container wagon (max 40'' HC)', 1668);
INSERT INTO WAGON_MODEL (model_id, maker, number_bogies, bogies, length_mm, width_mm, height_mm, weight_t, max_speed, payload_t, volume_m3, type, gauge) VALUES 
('Lgs 22 94 441 6', 'Metalsines', 1, 'Simples', 13860, 2850, 1060, 11.9, 120, 28.1, 76.3, 'Container wagon (max 40'' HC)', 1668);
INSERT INTO WAGON_MODEL (model_id, maker, number_bogies, bogies, length_mm, width_mm, height_mm, weight_t, max_speed, payload_t, volume_m3, type, gauge) VALUES 
('Sgnss 12 94 455 2', 'Emef', 2, 'Duplo', 18116, 2950, 1030, 21.6, 120, 68.4, 76.3, 'Container wagon (max 40'' HC)', 1668);
INSERT INTO WAGON_MODEL (model_id, maker, number_bogies, bogies, length_mm, width_mm, height_mm, weight_t, max_speed, payload_t, volume_m3, type, gauge) VALUES 
('Sgnss 12 94 455 2-1435', 'Emef', 2, 'Duplo', 18116, 2950, 1030, 21.6, 120, 68.4, 76.3, 'Container wagon (max 40'' HC)', 1435);

-- 8. INSERT WAGON DATA (30 Wagons)
INSERT INTO WAGON (wagon_number, model_id, operator_id, service_year) VALUES ('356 3 077', 'Regmms 32 94 356 3', 'MEDWAY', 1987);
INSERT INTO WAGON (wagon_number, model_id, operator_id, service_year) VALUES ('356 3 078', 'Regmms 32 94 356 3', 'MEDWAY', 1987);
INSERT INTO WAGON (wagon_number, model_id, operator_id, service_year) VALUES ('356 3 079', 'Regmms 32 94 356 3', 'MEDWAY', 1987);
INSERT INTO WAGON (wagon_number, model_id, operator_id, service_year) VALUES ('356 3 080', 'Regmms 32 94 356 3', 'MEDWAY', 1987);
INSERT INTO WAGON (wagon_number, model_id, operator_id, service_year) VALUES ('356 3 081', 'Regmms 32 94 356 3', 'MEDWAY', 1987);
INSERT INTO WAGON (wagon_number, model_id, operator_id, service_year) VALUES ('356 3 082', 'Regmms 32 94 356 3', 'MEDWAY', 1987);
INSERT INTO WAGON (wagon_number, model_id, operator_id, service_year) VALUES ('356 3 083', 'Regmms 32 94 356 3', 'MEDWAY', 1987);
INSERT INTO WAGON (wagon_number, model_id, operator_id, service_year) VALUES ('356 3 084', 'Regmms 32 94 356 3', 'MEDWAY', 1987);
INSERT INTO WAGON (wagon_number, model_id, operator_id, service_year) VALUES ('356 3 085', 'Regmms 32 94 356 3', 'MEDWAY', 1987);
INSERT INTO WAGON (wagon_number, model_id, operator_id, service_year) VALUES ('356 3 086', 'Regmms 32 94 356 3', 'MEDWAY', 1987);
INSERT INTO WAGON (wagon_number, model_id, operator_id, service_year) VALUES ('356 3 087', 'Regmms 32 94 356 3', 'MEDWAY', 1987);
INSERT INTO WAGON (wagon_number, model_id, operator_id, service_year) VALUES ('356 3 088', 'Regmms 32 94 356 3', 'MEDWAY', 1987);
INSERT INTO WAGON (wagon_number, model_id, operator_id, service_year) VALUES ('356 3 089', 'Regmms 32 94 356 3', 'MEDWAY', 1987);
INSERT INTO WAGON (wagon_number, model_id, operator_id, service_year) VALUES ('356 3 090', 'Regmms 32 94 356 3', 'MEDWAY', 1987);
INSERT INTO WAGON (wagon_number, model_id, operator_id, service_year) VALUES ('356 3 091', 'Regmms 32 94 356 3', 'MEDWAY', 1987);
INSERT INTO WAGON (wagon_number, model_id, operator_id, service_year) VALUES ('356 3 092', 'Regmms 32 94 356 3', 'MEDWAY', 1987);
INSERT INTO WAGON (wagon_number, model_id, operator_id, service_year) VALUES ('082 3 045', 'Tadgs 32 94 082 3', 'MEDWAY', 1990);
INSERT INTO WAGON (wagon_number, model_id, operator_id, service_year) VALUES ('082 3 046', 'Tadgs 32 94 082 3', 'MEDWAY', 1990);
INSERT INTO WAGON (wagon_number, model_id, operator_id, service_year) VALUES ('082 3 047', 'Tadgs 32 94 082 3', 'MEDWAY', 1990);
INSERT INTO WAGON (wagon_number, model_id, operator_id, service_year) VALUES ('082 3 048', 'Tadgs 32 94 082 3', 'MEDWAY', 1990);
INSERT INTO WAGON (wagon_number, model_id, operator_id, service_year) VALUES ('074 1 001', 'Tdgs 41 94 074 1', 'MEDWAY', 1977);
INSERT INTO WAGON (wagon_number, model_id, operator_id, service_year) VALUES ('074 1 002', 'Tdgs 41 94 074 1', 'MEDWAY', 1977);
INSERT INTO WAGON (wagon_number, model_id, operator_id, service_year) VALUES ('074 1 003', 'Tdgs 41 94 074 1', 'MEDWAY', 1977);
INSERT INTO WAGON (wagon_number, model_id, operator_id, service_year) VALUES ('074 1 004', 'Tdgs 41 94 074 1', 'MEDWAY', 1977);
INSERT INTO WAGON (wagon_number, model_id, operator_id, service_year) VALUES ('074 1 005', 'Tdgs 41 94 074 1', 'MEDWAY', 1977);
INSERT INTO WAGON (wagon_number, model_id, operator_id, service_year) VALUES ('074 1 006', 'Tdgs 41 94 074 1', 'MEDWAY', 1977);
INSERT INTO WAGON (wagon_number, model_id, operator_id, service_year) VALUES ('181 1 010', 'Gabs 81 94 181 1', 'MEDWAY', 1977);
INSERT INTO WAGON (wagon_number, model_id, operator_id, service_year) VALUES ('181 1 011', 'Gabs 81 94 181 1', 'MEDWAY', 1977);
INSERT INTO WAGON (wagon_number, model_id, operator_id, service_year) VALUES ('181 1 012', 'Gabs 81 94 181 1', 'MEDWAY', 1977);
INSERT INTO WAGON (wagon_number, model_id, operator_id, service_year) VALUES ('181 1 013', 'Gabs 81 94 181 1', 'MEDWAY', 1977);
INSERT INTO WAGON (wagon_number, model_id, operator_id, service_year) VALUES ('181 1 014', 'Gabs 81 94 181 1', 'MEDWAY', 1977);

COMMIT;

-- =============================================
-- DATA VERIFICATION QUERIES
-- =============================================

-- Count records per table
SELECT 'OWNER' as table_name, COUNT(*) as count FROM OWNER
UNION ALL SELECT 'OPERATOR', COUNT(*) FROM OPERATOR
UNION ALL SELECT 'FACILITY', COUNT(*) FROM FACILITY
UNION ALL SELECT 'RAILWAY_LINE', COUNT(*) FROM RAILWAY_LINE
UNION ALL SELECT 'LINE_SEGMENT', COUNT(*) FROM LINE_SEGMENT
UNION ALL SELECT 'ROLLING_STOCK', COUNT(*) FROM ROLLING_STOCK
UNION ALL SELECT 'WAGON_MODEL', COUNT(*) FROM WAGON_MODEL
UNION ALL SELECT 'WAGON', COUNT(*) FROM WAGON;

-- =============================================
