-- CLEAN UP EXISTING TABLES
BEGIN
FOR t IN (
    SELECT table_name FROM user_tables WHERE table_name IN (
        'TRAIN_WAGON_USAGE', 'TRAIN', 'FACILITY', 'FREIGHT_WAGON', 'FREIGHT',
        'OPERATOR', 'STATION', 'RAILWAY_LINE', 'LINE_SEGMENT',
        'ROLLING_STOCK', 'LOCOMOTIVE', 'WAGON', 'WAGON_MODEL',
        'TRAIN_ROUTE', 'ROUTE_SEGMENT', 'TRAIN_PASSAGE'
    )
) LOOP
    EXECUTE IMMEDIATE 'DROP TABLE ' || t.table_name || ' CASCADE CONSTRAINTS';
END LOOP;
END;
/

CREATE TABLE OPERATOR (
                          operator_id VARCHAR2(10) PRIMARY KEY,
                          name VARCHAR2(100)
);

CREATE TABLE FACILITY (
                          facility_id NUMBER PRIMARY KEY,
                          name VARCHAR2(100),
                          station_id VARCHAR2(10)
);

CREATE TABLE STATION (
                         station_id VARCHAR2(10) PRIMARY KEY,
                         name VARCHAR2(100),
                         latitude NUMBER(10,6),
                         longitude NUMBER(10,6)
);

CREATE TABLE RAILWAY_LINE (
                              line_id VARCHAR2(10) PRIMARY KEY,
                              name VARCHAR2(100),
                              owner_operator_id VARCHAR2(10)
);

CREATE TABLE LINE_SEGMENT (
                              segment_id VARCHAR2(10) PRIMARY KEY,
                              line_id VARCHAR2(10),
                              segment_order NUMBER,
                              is_electrified VARCHAR2(3),
                              max_weight_kg_m NUMBER,
                              length_m NUMBER,
                              number_tracks NUMBER,
                              siding_position NUMBER,
                              siding_length NUMBER
);

CREATE TABLE ROLLING_STOCK (
                               stock_id VARCHAR2(20) PRIMARY KEY,
                               operator_id VARCHAR2(10),
                               model VARCHAR2(50),
                               gauge_mm NUMBER(5,1)
);

CREATE TABLE LOCOMOTIVE (
                            stock_id VARCHAR2(20) PRIMARY KEY,
                            locomotive_type VARCHAR2(20),
                            power_kw NUMBER(6),
                            supports_multiple_gauges CHAR(1) DEFAULT 'N'
);

CREATE TABLE WAGON_MODEL (
                             model_id NUMBER PRIMARY KEY,
                             model_name VARCHAR2(50),
                             maker VARCHAR2(100),
                             number_bogies NUMBER,
                             bogies VARCHAR2(20),
                             length_mm NUMBER,
                             width_mm NUMBER,
                             height_mm NUMBER,
                             weight_t NUMBER,
                             max_speed NUMBER,
                             payload_t NUMBER,
                             volume_m3 NUMBER,
                             wagon_type VARCHAR2(50),
                             gauge_mm NUMBER
);

CREATE TABLE WAGON (
                       stock_id VARCHAR2(20) PRIMARY KEY,
                       model_id NUMBER,
                       operator_id VARCHAR2(10),
                       service_year NUMBER
);

CREATE TABLE FREIGHT (
                         freight_id NUMBER PRIMARY KEY,
                         freight_date DATE,
                         origin_facility_id NUMBER,
                         destination_facility_id NUMBER
);

CREATE TABLE FREIGHT_WAGON (
                               freight_id NUMBER,
                               wagon_id VARCHAR2(20),
                               PRIMARY KEY (freight_id, wagon_id)
);

CREATE TABLE TRAIN_ROUTE (
                             route_id VARCHAR2(10) PRIMARY KEY,
                             route_name VARCHAR2(100),
                             description VARCHAR2(500)
);

CREATE TABLE ROUTE_SEGMENT (
                               route_id VARCHAR2(10),
                               segment_order NUMBER,
                               facility_id NUMBER,
                               is_stop CHAR(1) DEFAULT 'Y',
                               stop_duration_min NUMBER DEFAULT 0,
                               PRIMARY KEY (route_id, segment_order)
);

CREATE TABLE TRAIN (
                       train_id VARCHAR2(10) PRIMARY KEY,
                       operator_id VARCHAR2(10),
                       train_date DATE,
                       train_time VARCHAR2(10),
                       start_facility_id NUMBER,
                       end_facility_id NUMBER,
                       locomotive_id VARCHAR2(20),
                       route_id VARCHAR2(10)
);

CREATE TABLE TRAIN_PASSAGE (
                               passage_id VARCHAR2(15) PRIMARY KEY,
                               train_id VARCHAR2(10),
                               facility_id NUMBER,
                               planned_arrival TIMESTAMP,
                               actual_arrival TIMESTAMP,
                               planned_departure TIMESTAMP,
                               actual_departure TIMESTAMP
);

CREATE TABLE TRAIN_WAGON_USAGE (
                                   usage_id VARCHAR2(12) PRIMARY KEY,
                                   train_id VARCHAR2(10),
                                   wagon_id VARCHAR2(20),
                                   usage_date DATE
);

-- =============================================
-- FOREIGN KEY CONSTRAINTS
-- =============================================

ALTER TABLE RAILWAY_LINE ADD CONSTRAINT FK_RAILWAY_LINE_OPERATOR
    FOREIGN KEY (owner_operator_id) REFERENCES OPERATOR(operator_id);

ALTER TABLE LINE_SEGMENT ADD CONSTRAINT FK_LINE_SEGMENT_RAILWAY_LINE
    FOREIGN KEY (line_id) REFERENCES RAILWAY_LINE(line_id);

ALTER TABLE ROLLING_STOCK ADD CONSTRAINT FK_ROLLING_STOCK_OPERATOR
    FOREIGN KEY (operator_id) REFERENCES OPERATOR(operator_id);

ALTER TABLE LOCOMOTIVE ADD CONSTRAINT FK_LOCOMOTIVE_ROLLING_STOCK
    FOREIGN KEY (stock_id) REFERENCES ROLLING_STOCK(stock_id);

ALTER TABLE WAGON ADD CONSTRAINT FK_WAGON_ROLLING_STOCK
    FOREIGN KEY (stock_id) REFERENCES ROLLING_STOCK(stock_id);

ALTER TABLE WAGON ADD CONSTRAINT FK_WAGON_MODEL
    FOREIGN KEY (model_id) REFERENCES WAGON_MODEL(model_id);

ALTER TABLE WAGON ADD CONSTRAINT FK_WAGON_OPERATOR
    FOREIGN KEY (operator_id) REFERENCES OPERATOR(operator_id);

ALTER TABLE FREIGHT ADD CONSTRAINT FK_FREIGHT_ORIGIN
    FOREIGN KEY (origin_facility_id) REFERENCES FACILITY(facility_id);

ALTER TABLE FREIGHT ADD CONSTRAINT FK_FREIGHT_DESTINATION
    FOREIGN KEY (destination_facility_id) REFERENCES FACILITY(facility_id);

ALTER TABLE FREIGHT_WAGON ADD CONSTRAINT FK_FREIGHT_WAGON_FREIGHT
    FOREIGN KEY (freight_id) REFERENCES FREIGHT(freight_id);
ALTER TABLE FREIGHT_WAGON ADD CONSTRAINT FK_FREIGHT_WAGON_WAGON
    FOREIGN KEY (wagon_id) REFERENCES WAGON(stock_id);

ALTER TABLE FACILITY ADD CONSTRAINT FK_FACILITY_STATION
    FOREIGN KEY (station_id) REFERENCES STATION(station_id);

ALTER TABLE TRAIN ADD CONSTRAINT FK_TRAIN_OPERATOR
    FOREIGN KEY (operator_id) REFERENCES OPERATOR(operator_id);

ALTER TABLE TRAIN ADD CONSTRAINT FK_TRAIN_START_FACILITY
    FOREIGN KEY (start_facility_id) REFERENCES FACILITY(facility_id);

ALTER TABLE TRAIN ADD CONSTRAINT FK_TRAIN_END_FACILITY
    FOREIGN KEY (end_facility_id) REFERENCES FACILITY(facility_id);

ALTER TABLE TRAIN ADD CONSTRAINT FK_TRAIN_LOCOMOTIVE
    FOREIGN KEY (locomotive_id) REFERENCES LOCOMOTIVE(stock_id);

ALTER TABLE TRAIN ADD CONSTRAINT FK_TRAIN_ROUTE
    FOREIGN KEY (route_id) REFERENCES TRAIN_ROUTE(route_id);

ALTER TABLE ROUTE_SEGMENT ADD CONSTRAINT FK_ROUTE_SEGMENT_ROUTE
    FOREIGN KEY (route_id) REFERENCES TRAIN_ROUTE(route_id);

ALTER TABLE ROUTE_SEGMENT ADD CONSTRAINT FK_ROUTE_SEGMENT_FACILITY
    FOREIGN KEY (facility_id) REFERENCES FACILITY(facility_id);

ALTER TABLE TRAIN_PASSAGE ADD CONSTRAINT FK_PASSAGE_TRAIN
    FOREIGN KEY (train_id) REFERENCES TRAIN(train_id);

ALTER TABLE TRAIN_PASSAGE ADD CONSTRAINT FK_PASSAGE_FACILITY
    FOREIGN KEY (facility_id) REFERENCES FACILITY(facility_id);

ALTER TABLE TRAIN_WAGON_USAGE ADD CONSTRAINT FK_USAGE_TRAIN
    FOREIGN KEY (train_id) REFERENCES TRAIN(train_id);

ALTER TABLE TRAIN_WAGON_USAGE ADD CONSTRAINT FK_USAGE_WAGON
    FOREIGN KEY (wagon_id) REFERENCES WAGON(stock_id);

-- =============================================
-- VERIFICATION
-- =============================================

SELECT 'USBD21 COMPLETED - Database created successfully' AS status FROM DUAL;

COMMIT;