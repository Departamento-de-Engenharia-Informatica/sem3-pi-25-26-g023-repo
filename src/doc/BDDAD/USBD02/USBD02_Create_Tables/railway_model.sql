-- =============================================
-- USBD02 - Railway System Relational Model
-- =============================================

-- CLEAN UP EXISTING TABLES
BEGIN
EXECUTE IMMEDIATE 'DROP TABLE RETURN_ITEM CASCADE CONSTRAINTS';
EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN
EXECUTE IMMEDIATE 'DROP TABLE ORDER_LINE CASCADE CONSTRAINTS';
EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN
EXECUTE IMMEDIATE 'DROP TABLE CUSTOMER_ORDER CASCADE CONSTRAINTS';
EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN
EXECUTE IMMEDIATE 'DROP TABLE BOX CASCADE CONSTRAINTS';
EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN
EXECUTE IMMEDIATE 'DROP TABLE BAY CASCADE CONSTRAINTS';
EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN
EXECUTE IMMEDIATE 'DROP TABLE ITEM CASCADE CONSTRAINTS';
EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN
EXECUTE IMMEDIATE 'DROP TABLE WAREHOUSE CASCADE CONSTRAINTS';
EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN
EXECUTE IMMEDIATE 'DROP TABLE WAGON CASCADE CONSTRAINTS';
EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN
EXECUTE IMMEDIATE 'DROP TABLE LOCOMOTIVE CASCADE CONSTRAINTS';
EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN
EXECUTE IMMEDIATE 'DROP TABLE ROLLING_STOCK CASCADE CONSTRAINTS';
EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN
EXECUTE IMMEDIATE 'DROP TABLE LINE_SEGMENT CASCADE CONSTRAINTS';
EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN
EXECUTE IMMEDIATE 'DROP TABLE RAILWAY_LINE CASCADE CONSTRAINTS';
EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN
EXECUTE IMMEDIATE 'DROP TABLE STATION CASCADE CONSTRAINTS';
EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN
EXECUTE IMMEDIATE 'DROP TABLE OPERATOR CASCADE CONSTRAINTS';
EXCEPTION WHEN OTHERS THEN NULL; END;
/

-- CREATE TABLES
CREATE TABLE OPERATOR (
                          operator_id VARCHAR2(10) NOT NULL,
                          name VARCHAR2(100),
                          type VARCHAR2(50),
                          contact_email VARCHAR2(100),
                          phone VARCHAR2(20),
                          PRIMARY KEY (operator_id)
);

CREATE TABLE STATION (
                         station_id VARCHAR2(10) NOT NULL,
                         name VARCHAR2(100),
                         type VARCHAR2(50),
                         has_warehouse CHAR(1) CHECK (has_warehouse IN ('Y','N')),
                         has_refrigerated CHAR(1) CHECK (has_refrigerated IN ('Y','N')),
                         latitude NUMBER(10,6),
                         longitude NUMBER(10,6),
                         PRIMARY KEY (station_id)
);

CREATE TABLE RAILWAY_LINE (
                              line_id VARCHAR2(10) NOT NULL,
                              name VARCHAR2(100),
                              owner_operator_id VARCHAR2(10) NOT NULL,
                              total_length_km NUMBER(8,2),
                              PRIMARY KEY (line_id)
);

CREATE TABLE LINE_SEGMENT (
                              segment_id VARCHAR2(10) NOT NULL,
                              line_id VARCHAR2(10) NOT NULL,
                              start_station_id VARCHAR2(10) NOT NULL,
                              end_station_id VARCHAR2(10) NOT NULL,
                              segment_length_km NUMBER(8,2),
                              track_type VARCHAR2(20),
                              gauge_mm NUMBER(5,1),
                              is_electrified CHAR(1) CHECK (is_electrified IN ('Y','N')),
                              max_weight_kg_per_m NUMBER(8,2),
                              max_speed_kmh NUMBER(4),
                              PRIMARY KEY (segment_id)
);

CREATE TABLE ROLLING_STOCK (
                               stock_id VARCHAR2(10) NOT NULL,
                               operator_id VARCHAR2(10) NOT NULL,
                               make VARCHAR2(50),
                               model VARCHAR2(50),
                               year_of_service NUMBER(4),
                               gauge_mm NUMBER(5,1),
                               length_m NUMBER(5,2),
                               width_m NUMBER(5,2),
                               height_m NUMBER(5,2),
                               tare_weight_kg NUMBER(8,2),
                               number_of_bogies NUMBER(2),
                               PRIMARY KEY (stock_id)
);

CREATE TABLE LOCOMOTIVE (
                            stock_id VARCHAR2(10) NOT NULL,
                            locomotive_type VARCHAR2(20),
                            power_kw NUMBER(6),
                            acceleration_kmh_s NUMBER(3,2),
                            max_total_weight_kg NUMBER(8,2),
                            fuel_capacity_l NUMBER(6),
                            supports_multiple_gauges CHAR(1) CHECK (supports_multiple_gauges IN ('Y','N')),
                            PRIMARY KEY (stock_id)
);

CREATE TABLE WAGON (
                       stock_id VARCHAR2(10) NOT NULL,
                       wagon_type VARCHAR2(30),
                       payload_capacity_kg NUMBER(8,2),
                       volume_capacity_m3 NUMBER(8,2),
                       container_supported VARCHAR2(50),
                       is_refrigerated CHAR(1) CHECK (is_refrigerated IN ('Y','N')),
                       max_pressure_bar NUMBER(5,2),
                       PRIMARY KEY (stock_id)
);

CREATE TABLE WAREHOUSE (
                           warehouse_id VARCHAR2(20) NOT NULL,
                           name VARCHAR2(100),
                           PRIMARY KEY (warehouse_id)
);

CREATE TABLE BAY (
                     warehouse_id VARCHAR2(20) NOT NULL,
                     aisle NUMBER(4) NOT NULL,
                     bay_number NUMBER(4) NOT NULL,
                     capacity_boxes NUMBER(4),
                     PRIMARY KEY (warehouse_id, aisle, bay_number)
);

CREATE TABLE ITEM (
                      sku VARCHAR2(20) NOT NULL,
                      name VARCHAR2(100),
                      category VARCHAR2(50),
                      unit VARCHAR2(20),
                      volume NUMBER(8,2),
                      unit_weight NUMBER(8,2),
                      PRIMARY KEY (sku)
);

CREATE TABLE BOX (
                     box_id VARCHAR2(20) NOT NULL,
                     qty_available NUMBER(6),
                     expiry_date DATE,
                     received_at TIMESTAMP,
                     sku VARCHAR2(20) NOT NULL,
                     aisle NUMBER(4) NOT NULL,
                     bay NUMBER(4) NOT NULL,
                     warehouse_id VARCHAR2(20) NOT NULL,
                     PRIMARY KEY (box_id)
);

CREATE TABLE CUSTOMER_ORDER (
                                order_id VARCHAR2(20) NOT NULL,
                                due_date DATE,
                                priority NUMBER(2),
                                PRIMARY KEY (order_id)
);

CREATE TABLE ORDER_LINE (
                            order_id VARCHAR2(20) NOT NULL,
                            line_no NUMBER(3) NOT NULL,
                            sku VARCHAR2(20) NOT NULL,
                            qty NUMBER(6),
                            PRIMARY KEY (order_id, line_no)
);

CREATE TABLE RETURN_ITEM (
                             return_id VARCHAR2(20) NOT NULL,
                             sku VARCHAR2(20) NOT NULL,
                             qty NUMBER(6),
                             reason VARCHAR2(50),
                             timestamp TIMESTAMP,
                             expiry_date DATE,
                             PRIMARY KEY (return_id)
);

-- FOREIGN KEY CONSTRAINTS
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

ALTER TABLE BAY ADD CONSTRAINT FK_BAY_WAREHOUSE
    FOREIGN KEY (warehouse_id) REFERENCES WAREHOUSE(warehouse_id);

ALTER TABLE BOX ADD CONSTRAINT FK_BOX_BAY
    FOREIGN KEY (warehouse_id, aisle, bay) REFERENCES BAY(warehouse_id, aisle, bay_number);

ALTER TABLE BOX ADD CONSTRAINT FK_BOX_ITEM
    FOREIGN KEY (sku) REFERENCES ITEM(sku);

ALTER TABLE ORDER_LINE ADD CONSTRAINT FK_ORDER_LINE_ORDER
    FOREIGN KEY (order_id) REFERENCES CUSTOMER_ORDER(order_id);

ALTER TABLE ORDER_LINE ADD CONSTRAINT FK_ORDER_LINE_ITEM
    FOREIGN KEY (sku) REFERENCES ITEM(sku);

ALTER TABLE RETURN_ITEM ADD CONSTRAINT FK_RETURN_ITEM_ITEM
    FOREIGN KEY (sku) REFERENCES ITEM(sku);

-- VERIFICATION
SELECT 'USBD02 COMPLETED - Database model created successfully' as status FROM DUAL;