-- =============================================
-- USBD32 - Relational Model Data Population
-- =============================================

-- 1. INSERT OPERATOR DATA
INSERT ALL
  INTO OPERATOR (operator_id, name) VALUES ('IP', 'Infraestruturas de Portugal, SA')
  INTO OPERATOR (operator_id, name) VALUES ('MEDWAY', 'Medway - Operador Ferroviário de Mercadorias, S.A')
  INTO OPERATOR (operator_id, name) VALUES ('CAPTRAIN', 'Captrain Portugal S.A.')
SELECT * FROM DUAL;

---
-- 2. INSERT STATION DATA (27 Stations)
INSERT ALL
  INTO STATION (station_id, name, latitude, longitude) VALUES ('ST001', 'São Romão', 41.468, -8.539)
  INTO STATION (station_id, name, latitude, longitude) VALUES ('ST002', 'Tamel', 41.521, -8.487)
  INTO STATION (station_id, name, latitude, longitude) VALUES ('ST003', 'Senhora das Dores', 41.389, -8.452)
  INTO STATION (station_id, name, latitude, longitude) VALUES ('ST004', 'Lousado', 41.342, -8.429)
  INTO STATION (station_id, name, latitude, longitude) VALUES ('ST005', 'Porto Campanhã', 41.149, -8.585)
  INTO STATION (station_id, name, latitude, longitude) VALUES ('ST006', 'Leandro', 41.312, -8.398)
  INTO STATION (station_id, name, latitude, longitude) VALUES ('ST007', 'Porto São Bento', 41.146, -8.610)
  INTO STATION (station_id, name, latitude, longitude) VALUES ('ST008', 'Barcelos', 41.535, -8.615)
  INTO STATION (station_id, name, latitude, longitude) VALUES ('ST009', 'Vila Nova da Cerveira', 41.939, -8.742)
  INTO STATION (station_id, name, latitude, longitude) VALUES ('ST010', 'Midões', 41.267, -8.345)
  INTO STATION (station_id, name, latitude, longitude) VALUES ('ST011', 'Valença', 42.030, -8.633)
  INTO STATION (station_id, name, latitude, longitude) VALUES ('ST012', 'Darque', 41.684, -8.817)
  INTO STATION (station_id, name, latitude, longitude) VALUES ('ST013', 'Contumil', 41.171, -8.574)
  INTO STATION (station_id, name, latitude, longitude) VALUES ('ST014', 'Ermesinde', 41.220, -8.551)
  INTO STATION (station_id, name, latitude, longitude) VALUES ('ST015', 'São Frutuoso', 41.452, -8.512)
  INTO STATION (station_id, name, latitude, longitude) VALUES ('ST016', 'São Pedro da Torre', 41.987, -8.645)
  INTO STATION (station_id, name, latitude, longitude) VALUES ('ST017', 'Viana do Castelo', 41.697, -8.828)
  INTO STATION (station_id, name, latitude, longitude) VALUES ('ST018', 'Famalicão', 41.408, -8.521)
  INTO STATION (station_id, name, latitude, longitude) VALUES ('ST019', 'Barroselas', 41.645, -8.712)
  INTO STATION (station_id, name, latitude, longitude) VALUES ('ST020', 'Nine', 41.432, -8.598)
  INTO STATION (station_id, name, latitude, longitude) VALUES ('ST021', 'Caminha', 41.875, -8.838)
  INTO STATION (station_id, name, latitude, longitude) VALUES ('ST022', 'Carvalha', 41.723, -8.765)
  INTO STATION (station_id, name, latitude, longitude) VALUES ('ST023', 'Carreço', 41.762, -8.795)
  INTO STATION (station_id, name, latitude, longitude) VALUES ('ST024', 'Leixões', 41.182, -8.702)
  INTO STATION (station_id, name, latitude, longitude) VALUES ('ST025', 'São Mamede de Infesta', 41.183, -8.602)
  INTO STATION (station_id, name, latitude, longitude) VALUES ('ST026', 'Leça do Balio', 41.212, -8.635)
  INTO STATION (station_id, name, latitude, longitude) VALUES ('ST027', 'São Gemil', 41.176, -8.589)
SELECT * FROM DUAL;

---
-- 3. INSERT RAILWAY_LINE DATA (13 Lines)
INSERT ALL
  INTO RAILWAY_LINE (line_id, name, owner_operator_id) VALUES ('L001', 'Ramal São Bento - Campanhã', 'IP')
  INTO RAILWAY_LINE (line_id, name, owner_operator_id) VALUES ('L002', 'Ramal Camapanhã - Contumil', 'IP')
  INTO RAILWAY_LINE (line_id, name, owner_operator_id) VALUES ('L003', 'Ramal Contumil - Nine', 'IP')
  INTO RAILWAY_LINE (line_id, name, owner_operator_id) VALUES ('L004', 'Ramal Nine - Barcelos', 'IP')
  INTO RAILWAY_LINE (line_id, name, owner_operator_id) VALUES ('L005', 'Ramal Barcelos - Darque', 'IP')
  INTO RAILWAY_LINE (line_id, name, owner_operator_id) VALUES ('L006', 'Ramal Darque - Viana', 'IP')
  INTO RAILWAY_LINE (line_id, name, owner_operator_id) VALUES ('L007', 'Ramal Viana - Caminha', 'IP')
  INTO RAILWAY_LINE (line_id, name, owner_operator_id) VALUES ('L008', 'Ramal Caminha - Torre', 'IP')
  INTO RAILWAY_LINE (line_id, name, owner_operator_id) VALUES ('L009', 'Ramal Torre - Valença', 'IP')
  INTO RAILWAY_LINE (line_id, name, owner_operator_id) VALUES ('L010', 'Ramal Contumil - São Gemil', 'IP')
  INTO RAILWAY_LINE (line_id, name, owner_operator_id) VALUES ('L011', 'Ramal São Gemil - São Mamede de Infesta', 'IP')
  INTO RAILWAY_LINE (line_id, name, owner_operator_id) VALUES ('L012', 'Ramal São Mamede de Infesta - Leça do Balio', 'IP')
  INTO RAILWAY_LINE (line_id, name, owner_operator_id) VALUES ('L013', 'Ramal Leça do Balio - Leixões', 'IP')
SELECT * FROM DUAL;

---
-- 4. INSERT FACILITY DATA
INSERT ALL
  INTO FACILITY (facility_id, name, station_id) VALUES (1, 'São Romão', 'ST001')
  INTO FACILITY (facility_id, name, station_id) VALUES (2, 'Tamel', 'ST002')
  INTO FACILITY (facility_id, name, station_id) VALUES (3, 'Senhora das Dores', 'ST003')
  INTO FACILITY (facility_id, name, station_id) VALUES (4, 'Lousado', 'ST004')
  INTO FACILITY (facility_id, name, station_id) VALUES (5, 'Porto Campanhã', 'ST005')
  INTO FACILITY (facility_id, name, station_id) VALUES (6, 'Leandro', 'ST006')
  INTO FACILITY (facility_id, name, station_id) VALUES (7, 'Porto São Bento', 'ST007')
  INTO FACILITY (facility_id, name, station_id) VALUES (8, 'Barcelos', 'ST008')
  INTO FACILITY (facility_id, name, station_id) VALUES (9, 'Vila Nova da Cerveira', 'ST009')
  INTO FACILITY (facility_id, name, station_id) VALUES (10, 'Midões', 'ST010')
  INTO FACILITY (facility_id, name, station_id) VALUES (11, 'Valença', 'ST011')
  INTO FACILITY (facility_id, name, station_id) VALUES (12, 'Darque', 'ST012')
  INTO FACILITY (facility_id, name, station_id) VALUES (13, 'Contumil', 'ST013')
  INTO FACILITY (facility_id, name, station_id) VALUES (14, 'Ermesinde', 'ST014')
  INTO FACILITY (facility_id, name, station_id) VALUES (15, 'São Frutuoso', 'ST015')
  INTO FACILITY (facility_id, name, station_id) VALUES (16, 'São Pedro da Torre', 'ST016')
  INTO FACILITY (facility_id, name, station_id) VALUES (17, 'Viana do Castelo', 'ST017')
  INTO FACILITY (facility_id, name, station_id) VALUES (18, 'Famalicão', 'ST018')
  INTO FACILITY (facility_id, name, station_id) VALUES (19, 'Barroselas', 'ST019')
  INTO FACILITY (facility_id, name, station_id) VALUES (20, 'Nine', 'ST020')
  INTO FACILITY (facility_id, name, station_id) VALUES (21, 'Caminha', 'ST021')
  INTO FACILITY (facility_id, name, station_id) VALUES (22, 'Carvalha', 'ST022')
  INTO FACILITY (facility_id, name, station_id) VALUES (23, 'Carreço', 'ST023')
  INTO FACILITY (facility_id, name, station_id) VALUES (50, 'Leixões', 'ST024')
  INTO FACILITY (facility_id, name, station_id) VALUES (45, 'São Mamede de Infesta', 'ST025')
  INTO FACILITY (facility_id, name, station_id) VALUES (48, 'Leça do Balio', 'ST026')
  INTO FACILITY (facility_id, name, station_id) VALUES (43, 'São Gemil', 'ST027')
SELECT * FROM DUAL;

---
-- 5. INSERT WAGON_MODEL DATA
INSERT ALL
  INTO WAGON_MODEL (model_id, model_name, maker, wagon_type, gauge_mm, length_m) VALUES (1245, 'Tadgs 32 94 082 3', 'Metalsines', 'Cereal wagon', 1668, 15)
  INTO WAGON_MODEL (model_id, model_name, maker, wagon_type, gauge_mm, length_m) VALUES (1278, 'Tdgs 41 94 074 1', 'Equimetal', 'Cereal wagon', 1668, 14)
  INTO WAGON_MODEL (model_id, model_name, maker, wagon_type, gauge_mm, length_m) VALUES (1325, 'Gabs 81 94 181 1', 'Sepsa Cometna', 'Covered wagon with sliding door', 1668, 18)
  INTO WAGON_MODEL (model_id, model_name, maker, wagon_type, gauge_mm, length_m) VALUES (1104, 'Regmms 32 94 356 3', 'Metalsines', 'Container wagon (max 40'' HC)', 1668, 20)
  INTO WAGON_MODEL (model_id, model_name, maker, wagon_type, gauge_mm, length_m) VALUES (1212, 'Kbs 41 94 333', 'Simmering', 'Wood wagon', 1668, 16)
SELECT * FROM DUAL;

---
-- 6. INSERT ROLLING_STOCK DATA (8 Locomotives + 41 Wagons)
INSERT ALL
-- Locomotives (8)
  INTO ROLLING_STOCK (stock_id, operator_id, model, gauge_mm) VALUES ('5621', 'MEDWAY', 'Eurosprinter', 1668)
  INTO ROLLING_STOCK (stock_id, operator_id, model, gauge_mm) VALUES ('5623', 'MEDWAY', 'Eurosprinter', 1668)
  INTO ROLLING_STOCK (stock_id, operator_id, model, gauge_mm) VALUES ('5630', 'MEDWAY', 'Eurosprinter', 1668)
  INTO ROLLING_STOCK (stock_id, operator_id, model, gauge_mm) VALUES ('1903', 'MEDWAY', 'CP 1900', 1668)
  INTO ROLLING_STOCK (stock_id, operator_id, model, gauge_mm) VALUES ('5034', 'MEDWAY', 'E4000', 1668)
  INTO ROLLING_STOCK (stock_id, operator_id, model, gauge_mm) VALUES ('5036', 'MEDWAY', 'E4000', 1668)
  INTO ROLLING_STOCK (stock_id, operator_id, model, gauge_mm) VALUES ('335.001', 'CAPTRAIN', 'E4000', 1668)
  INTO ROLLING_STOCK (stock_id, operator_id, model, gauge_mm) VALUES ('335.003', 'CAPTRAIN', 'E4000', 1668)
-- Container Wagons Regmms (16)
  INTO ROLLING_STOCK (stock_id, operator_id, model, gauge_mm) VALUES ('356 3 077', 'MEDWAY', 'Regmms 32 94 356 3', 1668)
  INTO ROLLING_STOCK (stock_id, operator_id, model, gauge_mm) VALUES ('356 3 078', 'MEDWAY', 'Regmms 32 94 356 3', 1668)
  INTO ROLLING_STOCK (stock_id, operator_id, model, gauge_mm) VALUES ('356 3 079', 'MEDWAY', 'Regmms 32 94 356 3', 1668)
  INTO ROLLING_STOCK (stock_id, operator_id, model, gauge_mm) VALUES ('356 3 080', 'MEDWAY', 'Regmms 32 94 356 3', 1668)
  INTO ROLLING_STOCK (stock_id, operator_id, model, gauge_mm) VALUES ('356 3 081', 'MEDWAY', 'Regmms 32 94 356 3', 1668)
  INTO ROLLING_STOCK (stock_id, operator_id, model, gauge_mm) VALUES ('356 3 082', 'MEDWAY', 'Regmms 32 94 356 3', 1668)
  INTO ROLLING_STOCK (stock_id, operator_id, model, gauge_mm) VALUES ('356 3 083', 'MEDWAY', 'Regmms 32 94 356 3', 1668)
  INTO ROLLING_STOCK (stock_id, operator_id, model, gauge_mm) VALUES ('356 3 084', 'MEDWAY', 'Regmms 32 94 356 3', 1668)
  INTO ROLLING_STOCK (stock_id, operator_id, model, gauge_mm) VALUES ('356 3 085', 'MEDWAY', 'Regmms 32 94 356 3', 1668)
  INTO ROLLING_STOCK (stock_id, operator_id, model, gauge_mm) VALUES ('356 3 086', 'MEDWAY', 'Regmms 32 94 356 3', 1668)
  INTO ROLLING_STOCK (stock_id, operator_id, model, gauge_mm) VALUES ('356 3 087', 'MEDWAY', 'Regmms 32 94 356 3', 1668)
  INTO ROLLING_STOCK (stock_id, operator_id, model, gauge_mm) VALUES ('356 3 088', 'MEDWAY', 'Regmms 32 94 356 3', 1668)
  INTO ROLLING_STOCK (stock_id, operator_id, model, gauge_mm) VALUES ('356 3 089', 'MEDWAY', 'Regmms 32 94 356 3', 1668)
  INTO ROLLING_STOCK (stock_id, operator_id, model, gauge_mm) VALUES ('356 3 090', 'MEDWAY', 'Regmms 32 94 356 3', 1668)
  INTO ROLLING_STOCK (stock_id, operator_id, model, gauge_mm) VALUES ('356 3 091', 'MEDWAY', 'Regmms 32 94 356 3', 1668)
  INTO ROLLING_STOCK (stock_id, operator_id, model, gauge_mm) VALUES ('356 3 092', 'MEDWAY', 'Regmms 32 94 356 3', 1668)
-- Cereal Wagons Tadgs (4)
  INTO ROLLING_STOCK (stock_id, operator_id, model, gauge_mm) VALUES ('082 3 045', 'MEDWAY', 'Tadgs 32 94 082 3', 1668)
  INTO ROLLING_STOCK (stock_id, operator_id, model, gauge_mm) VALUES ('082 3 046', 'MEDWAY', 'Tadgs 32 94 082 3', 1668)
  INTO ROLLING_STOCK (stock_id, operator_id, model, gauge_mm) VALUES ('082 3 047', 'MEDWAY', 'Tadgs 32 94 082 3', 1668)
  INTO ROLLING_STOCK (stock_id, operator_id, model, gauge_mm) VALUES ('082 3 048', 'MEDWAY', 'Tadgs 32 94 082 3', 1668)
-- Cereal Wagons Tdgs (6)
  INTO ROLLING_STOCK (stock_id, operator_id, model, gauge_mm) VALUES ('074 1 001', 'MEDWAY', 'Tdgs 41 94 074 1', 1668)
  INTO ROLLING_STOCK (stock_id, operator_id, model, gauge_mm) VALUES ('074 1 002', 'MEDWAY', 'Tdgs 41 94 074 1', 1668)
  INTO ROLLING_STOCK (stock_id, operator_id, model, gauge_mm) VALUES ('074 1 003', 'MEDWAY', 'Tdgs 41 94 074 1', 1668)
  INTO ROLLING_STOCK (stock_id, operator_id, model, gauge_mm) VALUES ('074 1 004', 'MEDWAY', 'Tdgs 41 94 074 1', 1668)
  INTO ROLLING_STOCK (stock_id, operator_id, model, gauge_mm) VALUES ('074 1 005', 'MEDWAY', 'Tdgs 41 94 074 1', 1668)
  INTO ROLLING_STOCK (stock_id, operator_id, model, gauge_mm) VALUES ('074 1 006', 'MEDWAY', 'Tdgs 41 94 074 1', 1668)
-- Covered Wagons Gabs (5)
  INTO ROLLING_STOCK (stock_id, operator_id, model, gauge_mm) VALUES ('181 1 010', 'MEDWAY', 'Gabs 81 94 181 1', 1668)
  INTO ROLLING_STOCK (stock_id, operator_id, model, gauge_mm) VALUES ('181 1 011', 'MEDWAY', 'Gabs 81 94 181 1', 1668)
  INTO ROLLING_STOCK (stock_id, operator_id, model, gauge_mm) VALUES ('181 1 012', 'MEDWAY', 'Gabs 81 94 181 1', 1668)
  INTO ROLLING_STOCK (stock_id, operator_id, model, gauge_mm) VALUES ('181 1 013', 'MEDWAY', 'Gabs 81 94 181 1', 1668)
  INTO ROLLING_STOCK (stock_id, operator_id, model, gauge_mm) VALUES ('181 1 014', 'MEDWAY', 'Gabs 81 94 181 1', 1668)
-- Wood Wagons Kbs (10)
  INTO ROLLING_STOCK (stock_id, operator_id, model, gauge_mm) VALUES ('333 0 001', 'MEDWAY', 'Kbs 41 94 333', 1668)
  INTO ROLLING_STOCK (stock_id, operator_id, model, gauge_mm) VALUES ('333 0 002', 'MEDWAY', 'Kbs 41 94 333', 1668)
  INTO ROLLING_STOCK (stock_id, operator_id, model, gauge_mm) VALUES ('333 0 003', 'MEDWAY', 'Kbs 41 94 333', 1668)
  INTO ROLLING_STOCK (stock_id, operator_id, model, gauge_mm) VALUES ('333 0 004', 'MEDWAY', 'Kbs 41 94 333', 1668)
  INTO ROLLING_STOCK (stock_id, operator_id, model, gauge_mm) VALUES ('333 0 005', 'MEDWAY', 'Kbs 41 94 333', 1668)
  INTO ROLLING_STOCK (stock_id, operator_id, model, gauge_mm) VALUES ('333 0 006', 'MEDWAY', 'Kbs 41 94 333', 1668)
  INTO ROLLING_STOCK (stock_id, operator_id, model, gauge_mm) VALUES ('333 0 007', 'MEDWAY', 'Kbs 41 94 333', 1668)
  INTO ROLLING_STOCK (stock_id, operator_id, model, gauge_mm) VALUES ('333 0 008', 'MEDWAY', 'Kbs 41 94 333', 1668)
  INTO ROLLING_STOCK (stock_id, operator_id, model, gauge_mm) VALUES ('333 0 009', 'MEDWAY', 'Kbs 41 94 333', 1668)
  INTO ROLLING_STOCK (stock_id, operator_id, model, gauge_mm) VALUES ('333 0 010', 'MEDWAY', 'Kbs 41 94 333', 1668)
SELECT * FROM DUAL;

---
-- 7. INSERT LOCOMOTIVE DATA
INSERT ALL
  INTO LOCOMOTIVE (stock_id, locomotive_type, power_kw, supports_multiple_gauges, length_m) VALUES ('5621', 'Electric', 5600, 'Y', 20)
  INTO LOCOMOTIVE (stock_id, locomotive_type, power_kw, supports_multiple_gauges, length_m) VALUES ('5623', 'Electric', 5600, 'Y', 20)
  INTO LOCOMOTIVE (stock_id, locomotive_type, power_kw, supports_multiple_gauges, length_m) VALUES ('5630', 'Electric', 5600, 'Y', 20)
  INTO LOCOMOTIVE (stock_id, locomotive_type, power_kw, supports_multiple_gauges, length_m) VALUES ('1903', 'Diesel', 1623, 'N', 18)
  INTO LOCOMOTIVE (stock_id, locomotive_type, power_kw, supports_multiple_gauges, length_m) VALUES ('5034', 'Diesel', 3178, 'N', 19)
  INTO LOCOMOTIVE (stock_id, locomotive_type, power_kw, supports_multiple_gauges, length_m) VALUES ('5036', 'Diesel', 3178, 'N', 19)
  INTO LOCOMOTIVE (stock_id, locomotive_type, power_kw, supports_multiple_gauges, length_m) VALUES ('335.001', 'Diesel', 3178, 'N', 19)
  INTO LOCOMOTIVE (stock_id, locomotive_type, power_kw, supports_multiple_gauges, length_m) VALUES ('335.003', 'Diesel', 3178, 'N', 19)
SELECT * FROM DUAL;

---
-- 8. INSERT WAGON DATA (41 Wagons)
INSERT ALL
-- Container Wagons Regmms (16)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('356 3 077', 1104, 'MEDWAY', 1987)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('356 3 078', 1104, 'MEDWAY', 1987)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('356 3 079', 1104, 'MEDWAY', 1987)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('356 3 080', 1104, 'MEDWAY', 1987)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('356 3 081', 1104, 'MEDWAY', 1987)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('356 3 082', 1104, 'MEDWAY', 1987)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('356 3 083', 1104, 'MEDWAY', 1987)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('356 3 084', 1104, 'MEDWAY', 1987)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('356 3 085', 1104, 'MEDWAY', 1987)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('356 3 086', 1104, 'MEDWAY', 1987)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('356 3 087', 1104, 'MEDWAY', 1987)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('356 3 088', 1104, 'MEDWAY', 1987)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('356 3 089', 1104, 'MEDWAY', 1987)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('356 3 090', 1104, 'MEDWAY', 1987)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('356 3 091', 1104, 'MEDWAY', 1987)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('356 3 092', 1104, 'MEDWAY', 1987)
-- Cereal Wagons Tadgs (4)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('082 3 045', 1245, 'MEDWAY', 1990)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('082 3 046', 1245, 'MEDWAY', 1990)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('082 3 047', 1245, 'MEDWAY', 1990)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('082 3 048', 1245, 'MEDWAY', 1990)
-- Cereal Wagons Tdgs (6)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('074 1 001', 1278, 'MEDWAY', 1977)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('074 1 002', 1278, 'MEDWAY', 1977)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('074 1 003', 1278, 'MEDWAY', 1977)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('074 1 004', 1278, 'MEDWAY', 1977)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('074 1 005', 1278, 'MEDWAY', 1977)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('074 1 006', 1278, 'MEDWAY', 1977)
-- Covered Wagons Gabs (5)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('181 1 010', 1325, 'MEDWAY', 1977)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('181 1 011', 1325, 'MEDWAY', 1977)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('181 1 012', 1325, 'MEDWAY', 1977)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('181 1 013', 1325, 'MEDWAY', 1977)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('181 1 014', 1325, 'MEDWAY', 1977)
-- Wood Wagons Kbs (10)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('333 0 001', 1212, 'MEDWAY', 2005)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('333 0 002', 1212, 'MEDWAY', 2005)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('333 0 003', 1212, 'MEDWAY', 2005)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('333 0 004', 1212, 'MEDWAY', 2005)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('333 0 005', 1212, 'MEDWAY', 2005)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('333 0 006', 1212, 'MEDWAY', 2005)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('333 0 007', 1212, 'MEDWAY', 2005)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('333 0 008', 1212, 'MEDWAY', 2005)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('333 0 009', 1212, 'MEDWAY', 2005)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('333 0 010', 1212, 'MEDWAY', 2005)
SELECT * FROM DUAL;

---
-- 9. INSERT TRAIN_ROUTE DATA
INSERT ALL
  INTO TRAIN_ROUTE (route_id, route_name, description) VALUES ('R001', 'Rota Leixões-Valença', 'Rota do comboio 5421 - Leixões para Valença')
  INTO TRAIN_ROUTE (route_id, route_name, description) VALUES ('R002', 'Rota Valença-Leixões', 'Rota dos comboios 5435 e 5437 - Valença para Leixões')
SELECT * FROM DUAL;

---
-- 10. INSERT ROUTE_SEGMENT DATA
INSERT ALL
  INTO ROUTE_SEGMENT (route_id, segment_order, facility_id, is_stop) VALUES ('R001', 1, 50, 'Y')
  INTO ROUTE_SEGMENT (route_id, segment_order, facility_id, is_stop) VALUES ('R001', 2, 48, 'Y')
  INTO ROUTE_SEGMENT (route_id, segment_order, facility_id, is_stop) VALUES ('R001', 3, 45, 'Y')
  INTO ROUTE_SEGMENT (route_id, segment_order, facility_id, is_stop) VALUES ('R001', 4, 43, 'Y')
  INTO ROUTE_SEGMENT (route_id, segment_order, facility_id, is_stop) VALUES ('R001', 5, 13, 'Y')
  INTO ROUTE_SEGMENT (route_id, segment_order, facility_id, is_stop) VALUES ('R001', 6, 20, 'Y')
  INTO ROUTE_SEGMENT (route_id, segment_order, facility_id, is_stop) VALUES ('R001', 7, 8, 'Y')
  INTO ROUTE_SEGMENT (route_id, segment_order, facility_id, is_stop) VALUES ('R001', 8, 12, 'Y')
  INTO ROUTE_SEGMENT (route_id, segment_order, facility_id, is_stop) VALUES ('R001', 9, 17, 'Y')
  INTO ROUTE_SEGMENT (route_id, segment_order, facility_id, is_stop) VALUES ('R001', 10, 21, 'Y')
  INTO ROUTE_SEGMENT (route_id, segment_order, facility_id, is_stop) VALUES ('R001', 11, 16, 'Y')
  INTO ROUTE_SEGMENT (route_id, segment_order, facility_id, is_stop) VALUES ('R001', 12, 11, 'Y')
  INTO ROUTE_SEGMENT (route_id, segment_order, facility_id, is_stop) VALUES ('R002', 1, 11, 'Y')
  INTO ROUTE_SEGMENT (route_id, segment_order, facility_id, is_stop) VALUES ('R002', 2, 16, 'Y')
  INTO ROUTE_SEGMENT (route_id, segment_order, facility_id, is_stop) VALUES ('R002', 3, 21, 'Y')
  INTO ROUTE_SEGMENT (route_id, segment_order, facility_id, is_stop) VALUES ('R002', 4, 17, 'Y')
  INTO ROUTE_SEGMENT (route_id, segment_order, facility_id, is_stop) VALUES ('R002', 5, 12, 'Y')
  INTO ROUTE_SEGMENT (route_id, segment_order, facility_id, is_stop) VALUES ('R002', 6, 8, 'Y')
  INTO ROUTE_SEGMENT (route_id, segment_order, facility_id, is_stop) VALUES ('R002', 7, 20, 'Y')
  INTO ROUTE_SEGMENT (route_id, segment_order, facility_id, is_stop) VALUES ('R002', 8, 13, 'Y')
  INTO ROUTE_SEGMENT (route_id, segment_order, facility_id, is_stop) VALUES ('R002', 9, 43, 'Y')
  INTO ROUTE_SEGMENT (route_id, segment_order, facility_id, is_stop) VALUES ('R002', 10, 45, 'Y')
  INTO ROUTE_SEGMENT (route_id, segment_order, facility_id, is_stop) VALUES ('R002', 11, 48, 'Y')
  INTO ROUTE_SEGMENT (route_id, segment_order, facility_id, is_stop) VALUES ('R002', 12, 50, 'Y')
SELECT * FROM DUAL;

---
-- 11. INSERT TRAIN DATA
INSERT ALL
  INTO TRAIN (train_id, operator_id, train_date, train_time, start_facility_id, end_facility_id, locomotive_id, route_id, max_length_m) VALUES ('5421', 'MEDWAY', DATE '2025-10-03', '09:45:00', 50, 11, '5621', 'R001', 500)
  INTO TRAIN (train_id, operator_id, train_date, train_time, start_facility_id, end_facility_id, locomotive_id, route_id, max_length_m) VALUES ('5435', 'MEDWAY', DATE '2025-10-03', '18:00:00', 11, 50, '5623', 'R002', 500)
  INTO TRAIN (train_id, operator_id, train_date, train_time, start_facility_id, end_facility_id, locomotive_id, route_id, max_length_m) VALUES ('5437', 'MEDWAY', DATE '2025-10-06', '10:00:00', 11, 50, '5621', 'R002', 500)
SELECT * FROM DUAL;

---
-- 12. INSERT TRAIN_PASSAGE DATA
INSERT ALL
  INTO TRAIN_PASSAGE (passage_id, train_id, facility_id, planned_arrival, planned_departure) VALUES ('P001', '5421', 50, TIMESTAMP '2025-10-03 09:45:00', TIMESTAMP '2025-10-03 10:00:00')
  INTO TRAIN_PASSAGE (passage_id, train_id, facility_id, planned_arrival, planned_departure) VALUES ('P002', '5421', 13, TIMESTAMP '2025-10-03 11:30:00', TIMESTAMP '2025-10-03 11:45:00')
  INTO TRAIN_PASSAGE (passage_id, train_id, facility_id, planned_arrival, planned_departure) VALUES ('P003', '5421', 11, TIMESTAMP '2025-10-03 14:00:00', TIMESTAMP '2025-10-03 14:00:00')
  INTO TRAIN_PASSAGE (passage_id, train_id, facility_id, planned_arrival, planned_departure) VALUES ('P004', '5435', 11, TIMESTAMP '2025-10-03 18:00:00', TIMESTAMP '2025-10-03 18:15:00')
  INTO TRAIN_PASSAGE (passage_id, train_id, facility_id, planned_arrival, planned_departure) VALUES ('P005', '5435', 13, TIMESTAMP '2025-10-03 20:30:00', TIMESTAMP '2025-10-03 20:45:00')
  INTO TRAIN_PASSAGE (passage_id, train_id, facility_id, planned_arrival, planned_departure) VALUES ('P006', '5435', 50, TIMESTAMP '2025-10-03 22:00:00', TIMESTAMP '2025-10-03 22:00:00')
SELECT * FROM DUAL;

---
-- 13. INSERT FREIGHT DATA
INSERT ALL
  INTO FREIGHT (freight_id, freight_date, origin_facility_id, destination_facility_id) VALUES (2001, DATE '2025-10-03', 50, 12)
  INTO FREIGHT (freight_id, freight_date, origin_facility_id, destination_facility_id) VALUES (2002, DATE '2025-10-03', 13, 11)
  INTO FREIGHT (freight_id, freight_date, origin_facility_id, destination_facility_id) VALUES (2003, DATE '2025-10-03', 50, 11)
  INTO FREIGHT (freight_id, freight_date, origin_facility_id, destination_facility_id) VALUES (2004, DATE '2025-10-03', 50, 21)
  INTO FREIGHT (freight_id, freight_date, origin_facility_id, destination_facility_id) VALUES (2005, DATE '2025-10-03', 20, 11)
  INTO FREIGHT (freight_id, freight_date, origin_facility_id, destination_facility_id) VALUES (2006, DATE '2025-10-03', 12, 50)
  INTO FREIGHT (freight_id, freight_date, origin_facility_id, destination_facility_id) VALUES (2007, DATE '2025-10-03', 50, 5)
  INTO FREIGHT (freight_id, freight_date, origin_facility_id, destination_facility_id) VALUES (2050, DATE '2025-10-06', 12, 50)
  INTO FREIGHT (freight_id, freight_date, origin_facility_id, destination_facility_id) VALUES (2051, DATE '2025-10-06', 11, 50)
SELECT * FROM DUAL;

---
-- 14. INSERT FREIGHT_WAGON DATA
INSERT ALL
  INTO FREIGHT_WAGON (freight_id, wagon_id) VALUES (2001, '333 0 001')
  INTO FREIGHT_WAGON (freight_id, wagon_id) VALUES (2001, '333 0 002')
  INTO FREIGHT_WAGON (freight_id, wagon_id) VALUES (2001, '333 0 004')
  INTO FREIGHT_WAGON (freight_id, wagon_id) VALUES (2001, '333 0 005')
  INTO FREIGHT_WAGON (freight_id, wagon_id) VALUES (2001, '333 0 006')
  INTO FREIGHT_WAGON (freight_id, wagon_id) VALUES (2002, '356 3 089')
  INTO FREIGHT_WAGON (freight_id, wagon_id) VALUES (2003, '181 1 011')
  INTO FREIGHT_WAGON (freight_id, wagon_id) VALUES (2003, '181 1 012')
  INTO FREIGHT_WAGON (freight_id, wagon_id) VALUES (2004, '181 1 013')
  INTO FREIGHT_WAGON (freight_id, wagon_id) VALUES (2005, '356 3 077')
  INTO FREIGHT_WAGON (freight_id, wagon_id) VALUES (2005, '356 3 078')
  INTO FREIGHT_WAGON (freight_id, wagon_id) VALUES (2005, '356 3 079')
  INTO FREIGHT_WAGON (freight_id, wagon_id) VALUES (2005, '356 3 080')
  INTO FREIGHT_WAGON (freight_id, wagon_id) VALUES (2006, '333 0 003')
  INTO FREIGHT_WAGON (freight_id, wagon_id) VALUES (2006, '333 0 007')
  INTO FREIGHT_WAGON (freight_id, wagon_id) VALUES (2007, '356 3 090')
  INTO FREIGHT_WAGON (freight_id, wagon_id) VALUES (2007, '356 3 091')
  INTO FREIGHT_WAGON (freight_id, wagon_id) VALUES (2007, '356 3 092')
  INTO FREIGHT_WAGON (freight_id, wagon_id) VALUES (2050, '333 0 001')
  INTO FREIGHT_WAGON (freight_id, wagon_id) VALUES (2050, '333 0 002')
  INTO FREIGHT_WAGON (freight_id, wagon_id) VALUES (2050, '333 0 004')
  INTO FREIGHT_WAGON (freight_id, wagon_id) VALUES (2050, '333 0 005')
  INTO FREIGHT_WAGON (freight_id, wagon_id) VALUES (2050, '333 0 006')
  INTO FREIGHT_WAGON (freight_id, wagon_id) VALUES (2051, '181 1 011')
  INTO FREIGHT_WAGON (freight_id, wagon_id) VALUES (2051, '181 1 012')
  INTO FREIGHT_WAGON (freight_id, wagon_id) VALUES (2051, '356 3 077')
  INTO FREIGHT_WAGON (freight_id, wagon_id) VALUES (2051, '356 3 078')
  INTO FREIGHT_WAGON (freight_id, wagon_id) VALUES (2051, '356 3 079')
  INTO FREIGHT_WAGON (freight_id, wagon_id) VALUES (2051, '356 3 080')
SELECT * FROM DUAL;

---
-- 15. INSERT TRAIN_WAGON_USAGE DATA
INSERT ALL
  INTO TRAIN_WAGON_USAGE (usage_id, train_id, wagon_id, usage_date) VALUES ('USG001', '5421', '082 3 045', DATE '2025-10-03')
  INTO TRAIN_WAGON_USAGE (usage_id, train_id, wagon_id, usage_date) VALUES ('USG002', '5421', '082 3 046', DATE '2025-10-03')
  INTO TRAIN_WAGON_USAGE (usage_id, train_id, wagon_id, usage_date) VALUES ('USG003', '5421', '074 1 001', DATE '2025-10-03')
  INTO TRAIN_WAGON_USAGE (usage_id, train_id, wagon_id, usage_date) VALUES ('USG004', '5421', '074 1 002', DATE '2025-10-03')
  INTO TRAIN_WAGON_USAGE (usage_id, train_id, wagon_id, usage_date) VALUES ('USG005', '5421', '356 3 077', DATE '2025-10-03')
  INTO TRAIN_WAGON_USAGE (usage_id, train_id, wagon_id, usage_date) VALUES ('USG006', '5435', '082 3 045', DATE '2025-10-03')
  INTO TRAIN_WAGON_USAGE (usage_id, train_id, wagon_id, usage_date) VALUES ('USG007', '5435', '074 1 001', DATE '2025-10-03')
  INTO TRAIN_WAGON_USAGE (usage_id, train_id, wagon_id, usage_date) VALUES ('USG008', '5435', '074 1 003', DATE '2025-10-03')
  INTO TRAIN_WAGON_USAGE (usage_id, train_id, wagon_id, usage_date) VALUES ('USG009', '5435', '356 3 078', DATE '2025-10-03')
  INTO TRAIN_WAGON_USAGE (usage_id, train_id, wagon_id, usage_date) VALUES ('USG010', '5437', '082 3 045', DATE '2025-10-06')
  INTO TRAIN_WAGON_USAGE (usage_id, train_id, wagon_id, usage_date) VALUES ('USG011', '5437', '074 1 001', DATE '2025-10-06')
  INTO TRAIN_WAGON_USAGE (usage_id, train_id, wagon_id, usage_date) VALUES ('USG012', '5437', '074 1 004', DATE '2025-10-06')
  INTO TRAIN_WAGON_USAGE (usage_id, train_id, wagon_id, usage_date) VALUES ('USG013', '5437', '082 3 047', DATE '2025-10-06')
SELECT * FROM DUAL;

---
-- 16. INSERT LINE_SEGMENT DATA
INSERT ALL
  INTO LINE_SEGMENT (segment_id, line_id, segment_order, is_electrified, max_weight_kg_m, length_m, number_tracks, siding_position, siding_length) VALUES (1, 'L001', 1, 'Yes', 8000, 2618, 4, NULL, NULL)
  INTO LINE_SEGMENT (segment_id, line_id, segment_order, is_electrified, max_weight_kg_m, length_m, number_tracks, siding_position, siding_length) VALUES (3, 'L002', 1, 'Yes', 8000, 2443, 4, NULL, NULL)
  INTO LINE_SEGMENT (segment_id, line_id, segment_order, is_electrified, max_weight_kg_m, length_m, number_tracks, siding_position, siding_length) VALUES (10, 'L003', 1, 'Yes', 8000, 26560, 2, NULL, NULL)
  INTO LINE_SEGMENT (segment_id, line_id, segment_order, is_electrified, max_weight_kg_m, length_m, number_tracks, siding_position, siding_length) VALUES (11, 'L003', 2, 'Yes', 8000, 10000, 2, NULL, NULL)
  INTO LINE_SEGMENT (segment_id, line_id, segment_order, is_electrified, max_weight_kg_m, length_m, number_tracks, siding_position, siding_length) VALUES (15, 'L004', 1, 'Yes', 8000, 5286, 2, NULL, NULL)
  INTO LINE_SEGMENT (segment_id, line_id, segment_order, is_electrified, max_weight_kg_m, length_m, number_tracks, siding_position, siding_length) VALUES (16, 'L004', 2, 'Yes', 8000, 6000, 2, NULL, NULL)
  INTO LINE_SEGMENT (segment_id, line_id, segment_order, is_electrified, max_weight_kg_m, length_m, number_tracks, siding_position, siding_length) VALUES (14, 'L005', 1, 'Yes', 8000, 10387, 2, NULL, NULL)
  INTO LINE_SEGMENT (segment_id, line_id, segment_order, is_electrified, max_weight_kg_m, length_m, number_tracks, siding_position, siding_length) VALUES (12, 'L005', 2, 'Yes', 8000, 12000, 2, NULL, NULL)
  INTO LINE_SEGMENT (segment_id, line_id, segment_order, is_electrified, max_weight_kg_m, length_m, number_tracks, siding_position, siding_length) VALUES (13, 'L005', 3, 'Yes', 8000, 3100, 2, NULL, NULL)
  INTO LINE_SEGMENT (segment_id, line_id, segment_order, is_electrified, max_weight_kg_m, length_m, number_tracks, siding_position, siding_length) VALUES (20, 'L006', 1, 'Yes', 6400, 4890, 2, NULL, NULL)
  INTO LINE_SEGMENT (segment_id, line_id, segment_order, is_electrified, max_weight_kg_m, length_m, number_tracks, siding_position, siding_length) VALUES (18, 'L007', 1, 'Yes', 8000, 6000, 1, NULL, NULL)
  INTO LINE_SEGMENT (segment_id, line_id, segment_order, is_electrified, max_weight_kg_m, length_m, number_tracks, siding_position, siding_length) VALUES (21, 'L007', 2, 'Yes', 8000, 5000, 1, 2000, 864)
  INTO LINE_SEGMENT (segment_id, line_id, segment_order, is_electrified, max_weight_kg_m, length_m, number_tracks, siding_position, siding_length) VALUES (22, 'L007', 3, 'Yes', 8000, 12000, 1, NULL, NULL)
  INTO LINE_SEGMENT (segment_id, line_id, segment_order, is_electrified, max_weight_kg_m, length_m, number_tracks, siding_position, siding_length) VALUES (25, 'L008', 1, 'Yes', 8000, 20829, 1, 11000, 266)
  INTO LINE_SEGMENT (segment_id, line_id, segment_order, is_electrified, max_weight_kg_m, length_m, number_tracks, siding_position, siding_length) VALUES (26, 'L009', 1, 'Yes', 8000, 4264, 1, NULL, NULL)
  INTO LINE_SEGMENT (segment_id, line_id, segment_order, is_electrified, max_weight_kg_m, length_m, number_tracks, siding_position, siding_length) VALUES (30, 'L010', 1, 'Yes', 8000, 3883, 2, NULL, NULL)
  INTO LINE_SEGMENT (segment_id, line_id, segment_order, is_electrified, max_weight_kg_m, length_m, number_tracks, siding_position, siding_length) VALUES (31, 'L011', 1, 'Yes', 8400, 1174, 2, NULL, NULL)
  INTO LINE_SEGMENT (segment_id, line_id, segment_order, is_electrified, max_weight_kg_m, length_m, number_tracks, siding_position, siding_length) VALUES (32, 'L011', 2, 'Yes', 8000, 2534, 2, NULL, NULL)
  INTO LINE_SEGMENT (segment_id, line_id, segment_order, is_electrified, max_weight_kg_m, length_m, number_tracks, siding_position, siding_length) VALUES (33, 'L012', 1, 'Yes', 8000, 1566, 2, NULL, NULL)
  INTO LINE_SEGMENT (segment_id, line_id, segment_order, is_electrified, max_weight_kg_m, length_m, number_tracks, siding_position, siding_length) VALUES (34, 'L012', 2, 'Yes', 8000, 1453, 2, NULL, NULL)
  INTO LINE_SEGMENT (segment_id, line_id, segment_order, is_electrified, max_weight_kg_m, length_m, number_tracks, siding_position, siding_length) VALUES (35, 'L013', 1, 'Yes', 8100, 3597, 2, NULL, NULL)
  INTO LINE_SEGMENT (segment_id, line_id, segment_order, is_electrified, max_weight_kg_m, length_m, number_tracks, siding_position, siding_length) VALUES (36, 'L013', 2, 'Yes', 8000, 4334, 2, NULL, NULL)
SELECT * FROM DUAL;

---
COMMIT;

-- Verification
SELECT 'USBD32 COMPLETED - Database populated successfully' as status FROM DUAL;