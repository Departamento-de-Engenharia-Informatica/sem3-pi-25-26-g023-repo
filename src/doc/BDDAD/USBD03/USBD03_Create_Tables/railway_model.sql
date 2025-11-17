-- =============================================
-- USBD03 - Create Database from Visual Paradigm Model
-- =============================================

- - CLEAN UP EXISTING TABLES
BEGIN
FOR t IN (SELECT table_name FROM user_tables WHERE table_name IN (
        'OPERATOR', 'STATION', 'RAILWAY_LINE', 'LINE_SEGMENT',
        'ROLLING_STOCK', 'LOCOMOTIVE', 'WAGON'
    )) LOOP
        EXECUTE IMMEDIATE 'DROP TABLE ' || t.table_name || ' CASCADE CONSTRAINTS';
END LOOP;
END;
/

-- =============================================
-- CREATE TABLES
-- =============================================

CREATE TABLE OPERATOR (
                          operator_id VARCHAR2(10) PRIMARY KEY,
                          name VARCHAR2(100)
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
                              start_station_id VARCHAR2(10),
                              end_station_id VARCHAR2(10),
                              segment_length_km NUMBER(8,2),
                              track_type VARCHAR2(20),
                              gauge_mm NUMBER(5,1),
                              is_electrified CHAR(1)
);

CREATE TABLE ROLLING_STOCK (
                               stock_id VARCHAR2(10) PRIMARY KEY,
                               operator_id VARCHAR2(10),
                               model VARCHAR2(50),
                               gauge_mm NUMBER(5,1)
);

CREATE TABLE LOCOMOTIVE (
                            stock_id VARCHAR2(10) PRIMARY KEY,
                            locomotive_type VARCHAR2(20),
                            power_kw NUMBER(6),
                            supports_multiple_gauges CHAR(1) DEFAULT 'N'
);

CREATE TABLE WAGON (
                       stock_id VARCHAR2(10) PRIMARY KEY,
                       wagon_type VARCHAR2(30),
                       payload_capacity_kg NUMBER(8,2)
);

-- =============================================
-- FOREIGN KEY CONSTRAINTS
-- =============================================

ALTER TABLE RAILWAY_LINE ADD CONSTRAINT FK_RAILWAY_LINE_OPERATOR
    FOREIGN KEY (owner_operator_id) REFERENCES OPERATOR(operator_id);

ALTER TABLE LINE_SEGMENT ADD CONSTRAINT FK_LINE_SEGMENT_RAILWAY_LINE
    FOREIGN KEY (line_id) REFERENCES RAILWAY_LINE(line_id);

ALTER TABLE LINE_SEGMENT ADD CONSTRAINT FK_LINE_SEGMENT_START_STATION
    FOREIGN KEY (start_station_id) REFERENCES STATION(station_id);

ALTER TABLE LINE_SEGMENT ADD CONSTRAINT FK_LINE_SEGMENT_END_STATION
    FOREIGN KEY (end_station_id) REFERENCES STATION(station_id);

ALTER TABLE ROLLING_STOCK ADD CONSTRAINT FK_ROLLING_STOCK_OPERATOR
    FOREIGN KEY (operator_id) REFERENCES OPERATOR(operator_id);

ALTER TABLE LOCOMOTIVE ADD CONSTRAINT FK_LOCOMOTIVE_ROLLING_STOCK
    FOREIGN KEY (stock_id) REFERENCES ROLLING_STOCK(stock_id);

ALTER TABLE WAGON ADD CONSTRAINT FK_WAGON_ROLLING_STOCK
    FOREIGN KEY (stock_id) REFERENCES ROLLING_STOCK(stock_id);

-- =============================================
-- VERIFICATION
-- =============================================

SELECT 'USBD03 COMPLETED - Database created successfully' as status FROM DUAL;

COMMIT;