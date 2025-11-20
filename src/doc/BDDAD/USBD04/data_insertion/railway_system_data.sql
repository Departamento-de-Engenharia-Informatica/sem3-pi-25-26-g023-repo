-- =============================================
-- USBD04 - Railway System Data Population
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
  INTO FACILITY (facility_id, name) VALUES (1, 'Terminal de Carga Norte')
  INTO FACILITY (facility_id, name) VALUES (2, 'Terminal de Carga Sul')
  INTO FACILITY (facility_id, name) VALUES (3, 'Porto de Leixões')
  INTO FACILITY (facility_id, name) VALUES (4, 'Zona Industrial de Nine')
SELECT * FROM DUAL;

---
-- 5. INSERT WAGON_MODEL DATA
INSERT ALL
  INTO WAGON_MODEL (model_id, model_name, maker, wagon_type, gauge_mm) VALUES (1, 'Regmms 32 94 356 3', 'Manufacturer A', 'Container Wagon', 1668)
  INTO WAGON_MODEL (model_id, model_name, maker, wagon_type, gauge_mm) VALUES (2, 'Tadgs 32 94 082 3', 'Manufacturer B', 'Cereal Wagon', 1668)
  INTO WAGON_MODEL (model_id, model_name, maker, wagon_type, gauge_mm) VALUES (3, 'Tdgs 41 94 074 1', 'Manufacturer C', 'Cereal Wagon', 1668)
  INTO WAGON_MODEL (model_id, model_name, maker, wagon_type, gauge_mm) VALUES (4, 'Gabs 81 94 181 1', 'Manufacturer D', 'Covered Wagon', 1668)
  INTO WAGON_MODEL (model_id, model_name, maker, wagon_type, gauge_mm) VALUES (5, 'Kbs 41 94 333', 'Manufacturer E', 'Wood Wagon', 1668)
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
  INTO LOCOMOTIVE (stock_id, locomotive_type, power_kw, supports_multiple_gauges) VALUES ('5621', 'Electric', 5600, 'Y')
  INTO LOCOMOTIVE (stock_id, locomotive_type, power_kw, supports_multiple_gauges) VALUES ('5623', 'Electric', 5600, 'Y')
  INTO LOCOMOTIVE (stock_id, locomotive_type, power_kw, supports_multiple_gauges) VALUES ('5630', 'Electric', 5600, 'Y')
  INTO LOCOMOTIVE (stock_id, locomotive_type, power_kw, supports_multiple_gauges) VALUES ('1903', 'Diesel', 1623, 'N')
  INTO LOCOMOTIVE (stock_id, locomotive_type, power_kw, supports_multiple_gauges) VALUES ('5034', 'Diesel', 3178, 'N')
  INTO LOCOMOTIVE (stock_id, locomotive_type, power_kw, supports_multiple_gauges) VALUES ('5036', 'Diesel', 3178, 'N')
  INTO LOCOMOTIVE (stock_id, locomotive_type, power_kw, supports_multiple_gauges) VALUES ('335.001', 'Diesel', 3178, 'N')
  INTO LOCOMOTIVE (stock_id, locomotive_type, power_kw, supports_multiple_gauges) VALUES ('335.003', 'Diesel', 3178, 'N')
SELECT * FROM DUAL;

---
-- 8. INSERT WAGON DATA (41 Wagons)
INSERT ALL
-- Container Wagons Regmms (16)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('356 3 077', 1, 'MEDWAY', 2010)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('356 3 078', 1, 'MEDWAY', 2010)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('356 3 079', 1, 'MEDWAY', 2010)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('356 3 080', 1, 'MEDWAY', 2010)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('356 3 081', 1, 'MEDWAY', 2010)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('356 3 082', 1, 'MEDWAY', 2010)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('356 3 083', 1, 'MEDWAY', 2010)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('356 3 084', 1, 'MEDWAY', 2010)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('356 3 085', 1, 'MEDWAY', 2010)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('356 3 086', 1, 'MEDWAY', 2010)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('356 3 087', 1, 'MEDWAY', 2010)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('356 3 088', 1, 'MEDWAY', 2010)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('356 3 089', 1, 'MEDWAY', 2010)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('356 3 090', 1, 'MEDWAY', 2010)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('356 3 091', 1, 'MEDWAY', 2010)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('356 3 092', 1, 'MEDWAY', 2010)
-- Cereal Wagons Tadgs (4)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('082 3 045', 2, 'MEDWAY', 2012)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('082 3 046', 2, 'MEDWAY', 2012)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('082 3 047', 2, 'MEDWAY', 2012)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('082 3 048', 2, 'MEDWAY', 2012)
-- Cereal Wagons Tdgs (6)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('074 1 001', 3, 'MEDWAY', 2015)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('074 1 002', 3, 'MEDWAY', 2015)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('074 1 003', 3, 'MEDWAY', 2015)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('074 1 004', 3, 'MEDWAY', 2015)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('074 1 005', 3, 'MEDWAY', 2015)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('074 1 006', 3, 'MEDWAY', 2015)
-- Covered Wagons Gabs (5)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('181 1 010', 4, 'MEDWAY', 2018)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('181 1 011', 4, 'MEDWAY', 2018)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('181 1 012', 4, 'MEDWAY', 2018)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('181 1 013', 4, 'MEDWAY', 2018)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('181 1 014', 4, 'MEDWAY', 2018)
-- Wood Wagons Kbs (10)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('333 0 001', 5, 'MEDWAY', 2020)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('333 0 002', 5, 'MEDWAY', 2020)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('333 0 003', 5, 'MEDWAY', 2020)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('333 0 004', 5, 'MEDWAY', 2020)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('333 0 005', 5, 'MEDWAY', 2020)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('333 0 006', 5, 'MEDWAY', 2020)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('333 0 007', 5, 'MEDWAY', 2020)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('333 0 008', 5, 'MEDWAY', 2020)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('333 0 009', 5, 'MEDWAY', 2020)
  INTO WAGON (stock_id, model_id, operator_id, service_year) VALUES ('333 0 010', 5, 'MEDWAY', 2020)
SELECT * FROM DUAL;

---
-- 9. INSERT TRAIN DATA
INSERT ALL
  INTO TRAIN (train_id, operator_id, train_date, train_time, start_facility_id, end_facility_id, locomotive_id) VALUES ('5421', 'MEDWAY', DATE '2025-10-03', '09:45:00', 3, 4, '5621')
  INTO TRAIN (train_id, operator_id, train_date, train_time, start_facility_id, end_facility_id, locomotive_id) VALUES ('5435', 'MEDWAY', DATE '2025-10-03', '18:00:00', 4, 3, '5623')
  INTO TRAIN (train_id, operator_id, train_date, train_time, start_facility_id, end_facility_id, locomotive_id) VALUES ('5437', 'MEDWAY', DATE '2025-10-06', '10:00:00', 4, 3, '5621')
SELECT * FROM DUAL;

---
-- 10. INSERT FREIGHT DATA
INSERT ALL
  INTO FREIGHT (freight_id, freight_date, origin_facility_id, destination_facility_id) VALUES (2001, DATE '2025-10-03', 3, 4)
  INTO FREIGHT (freight_id, freight_date, origin_facility_id, destination_facility_id) VALUES (2002, DATE '2025-10-03', 1, 4)
  INTO FREIGHT (freight_id, freight_date, origin_facility_id, destination_facility_id) VALUES (2003, DATE '2025-10-03', 3, 4)
  INTO FREIGHT (freight_id, freight_date, origin_facility_id, destination_facility_id) VALUES (2004, DATE '2025-10-03', 3, 2)
  INTO FREIGHT (freight_id, freight_date, origin_facility_id, destination_facility_id) VALUES (2005, DATE '2025-10-03', 4, 4)
  INTO FREIGHT (freight_id, freight_date, origin_facility_id, destination_facility_id) VALUES (2006, DATE '2025-10-03', 4, 3)
  INTO FREIGHT (freight_id, freight_date, origin_facility_id, destination_facility_id) VALUES (2007, DATE '2025-10-03', 3, 1)
  INTO FREIGHT (freight_id, freight_date, origin_facility_id, destination_facility_id) VALUES (2050, DATE '2025-10-06', 4, 3)
  INTO FREIGHT (freight_id, freight_date, origin_facility_id, destination_facility_id) VALUES (2051, DATE '2025-10-06', 4, 3)
SELECT * FROM DUAL;

---
-- 11. INSERT FREIGHT_WAGON DATA
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
-- 12. INSERT TRAIN_WAGON_USAGE DATA
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
COMMIT;

-- Verification
SELECT 'USBD04 COMPLETED - Database populated successfully' as status FROM DUAL;