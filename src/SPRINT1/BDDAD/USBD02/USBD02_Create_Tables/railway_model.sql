-- =============================================
-- USBD02 - Railway System Relational Model
-- =============================================

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
    has_warehouse CHAR(1),
    has_refrigerated CHAR(1),
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
    is_eletrified CHAR(1),
    max_weigth_kg_per_m NUMBER(8,2),
    max_speed_kmh NUMBER,
    PRIMARY KEY (segment_id)
);

CREATE TABLE ROLLING_STOCK (
    stock_id VARCHAR2(10) NOT NULL,
    operator_id VARCHAR2(10) NOT NULL,
    make VARCHAR2(50),
    model VARCHAR2(50),
    year_of_service NUMBER,
    gauge_mm NUMBER(5,1),
    length_m NUMBER(5,2),
    width_m NUMBER(5,2),
    height_m NUMBER(5,2),
    tare_weight_kg NUMBER(8,2),
    number_of_bogies NUMBER,
    PRIMARY KEY (stock_id)
);

CREATE TABLE LOCOMOTIVE (
    stock_id VARCHAR2(10) NOT NULL,
    locomotive_type VARCHAR2(20),
    power_kw NUMBER,
    acceleration_kmh_s NUMBER(3,2),
    max_total_weight_kg NUMBER(8,2),
    fuel_capacity_l NUMBER,
    supports_multiple_gauges CHAR(1),
    PRIMARY KEY (stock_id)
);

CREATE TABLE WAGON (
    stock_id VARCHAR2(10) NOT NULL,
    wagon_type VARCHAR2(30),
    payload_capacity_kg NUMBER(8,2),
    volume_capacity_m3 NUMBER(8,2),
    container_supported VARCHAR2(50),
    is_refrigerated CHAR(1),
    max_pressure_bar NUMBER(5,2),
    PRIMARY KEY (stock_id)
);

-- Warehouse Management Tables
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
    received_at TIMESTAMP(0),
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
    timestamp TIMESTAMP(0),
    expiry_date DATE,
    PRIMARY KEY (return_id)
);

-- =============================================
-- FOREIGN KEY CONSTRAINTS
-- =============================================

-- Railway Constraints
ALTER TABLE RAILWAY_LINE ADD CONSTRAINT "An operator has several railway lines" 
    FOREIGN KEY (owner_operator_id) REFERENCES OPERATOR(operator_id);

ALTER TABLE LINE_SEGMENT ADD CONSTRAINT "A line has several segments" 
    FOREIGN KEY (line_id) REFERENCES RAILWAY_LINE(line_id);

ALTER TABLE LINE_SEGMENT ADD CONSTRAINT "A station can be the start of several segments" 
    FOREIGN KEY (start_station_id) REFERENCES STATION(station_id);

ALTER TABLE LINE_SEGMENT ADD CONSTRAINT "A station can be the end of several segments" 
    FOREIGN KEY (end_station_id) REFERENCES STATION(station_id);

ALTER TABLE ROLLING_STOCK ADD CONSTRAINT "An operator has several rolling stocks" 
    FOREIGN KEY (operator_id) REFERENCES OPERATOR(operator_id);

ALTER TABLE LOCOMOTIVE ADD CONSTRAINT "A locomotive is a rolling stock" 
    FOREIGN KEY (stock_id) REFERENCES ROLLING_STOCK(stock_id);

ALTER TABLE WAGON ADD CONSTRAINT "A wagon is a rolling stock" 
    FOREIGN KEY (stock_id) REFERENCES ROLLING_STOCK(stock_id);

-- Warehouse Constraints
ALTER TABLE BAY ADD CONSTRAINT "A warehouse has several bays" 
    FOREIGN KEY (warehouse_id) REFERENCES WAREHOUSE(warehouse_id);

ALTER TABLE BOX ADD CONSTRAINT "A box is located in a bay" 
    FOREIGN KEY (warehouse_id, aisle, bay) REFERENCES BAY(warehouse_id, aisle, bay_number);

ALTER TABLE BOX ADD CONSTRAINT "An item can be in multiple boxes" 
    FOREIGN KEY (sku) REFERENCES ITEM(sku);

ALTER TABLE ORDER_LINE ADD CONSTRAINT "An order has multiple lines" 
    FOREIGN KEY (order_id) REFERENCES CUSTOMER_ORDER(order_id);

ALTER TABLE ORDER_LINE ADD CONSTRAINT "An item can appear on multiple order lines" 
    FOREIGN KEY (sku) REFERENCES ITEM(sku);

ALTER TABLE RETURN_ITEM ADD CONSTRAINT "An item can have multiple returns" 
    FOREIGN KEY (sku) REFERENCES ITEM(sku);

-- =============================================