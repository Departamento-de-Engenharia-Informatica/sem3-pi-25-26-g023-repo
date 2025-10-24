-- =============================================
-- USBD02 - Railway System Relational Model
-- =============================================

-- CLEAN UP
DROP TABLE WAGON CASCADE CONSTRAINTS;
DROP TABLE LOCOMOTIVE CASCADE CONSTRAINTS;
DROP TABLE ROLLING_STOCK CASCADE CONSTRAINTS;
DROP TABLE LINE_SEGMENT CASCADE CONSTRAINTS;
DROP TABLE RAILWAY_LINE CASCADE CONSTRAINTS;
DROP TABLE FACILITY CASCADE CONSTRAINTS;
DROP TABLE OPERATOR CASCADE CONSTRAINTS;
DROP TABLE OWNER CASCADE CONSTRAINTS;

-- CREATE TABLES
CREATE TABLE OWNER (
                       owner_id VARCHAR2(10) NOT NULL,
                       name VARCHAR2(100),
                       short_name VARCHAR2(20),
                       vat_number VARCHAR2(20),
                       PRIMARY KEY (owner_id)
);

CREATE TABLE OPERATOR (
                          operator_id VARCHAR2(10) NOT NULL,
                          name VARCHAR2(100),
                          short_name VARCHAR2(20),
                          vat_number VARCHAR2(20),
                          PRIMARY KEY (operator_id)
);

CREATE TABLE FACILITY (
                          facility_id NUMBER NOT NULL,
                          name VARCHAR2(100) NOT NULL,
                          PRIMARY KEY (facility_id)
);

CREATE TABLE RAILWAY_LINE (
                              line_id NUMBER NOT NULL,
                              name VARCHAR2(100),
                              owner_id VARCHAR2(10) NOT NULL,
                              start_facility_id NUMBER NOT NULL,
                              end_facility_id NUMBER NOT NULL,
                              gauge NUMBER,
                              PRIMARY KEY (line_id)
);

CREATE TABLE LINE_SEGMENT (
                              segment_id NUMBER NOT NULL,
                              line_id NUMBER NOT NULL,
                              segment_order NUMBER NOT NULL,
                              electrified VARCHAR2(3) CHECK (electrified IN ('Yes', 'No')),
                              max_weight_kg_m NUMBER,
                              length_m NUMBER,
                              number_tracks NUMBER,
                              PRIMARY KEY (segment_id)
);

CREATE TABLE ROLLING_STOCK (
                               stock_id VARCHAR2(20) NOT NULL,
                               operator_id VARCHAR2(10) NOT NULL,
                               name VARCHAR2(100),
                               make VARCHAR2(50),
                               model VARCHAR2(50),
                               service_year NUMBER,
                               number_bogies NUMBER,
                               bogies_type VARCHAR2(20),
                               power NUMBER,
                               length_m NUMBER(5,2),
                               width_m NUMBER(5,2),
                               height_m NUMBER(5,2),
                               weight_t NUMBER(8,2),
                               max_speed NUMBER,
                               operational_speed NUMBER,
                               traction_kn NUMBER,
                               type VARCHAR2(20),
                               voltage VARCHAR2(20),
                               frequency VARCHAR2(20),
                               gauge NUMBER,
                               fuel_l NUMBER,
                               PRIMARY KEY (stock_id)
);

CREATE TABLE WAGON_MODEL (
                             model_id VARCHAR2(50) NOT NULL,
                             maker VARCHAR2(50),
                             number_bogies NUMBER,
                             bogies VARCHAR2(20),
                             length_mm NUMBER,
                             width_mm NUMBER,
                             height_mm NUMBER,
                             weight_t NUMBER(8,2),
                             max_speed NUMBER,
                             payload_t NUMBER(8,2),
                             volume_m3 NUMBER(8,2),
                             type VARCHAR2(100),
                             gauge NUMBER,
                             PRIMARY KEY (model_id)
);

CREATE TABLE WAGON (
                       wagon_number VARCHAR2(20) NOT NULL,
                       model_id VARCHAR2(50) NOT NULL,
                       operator_id VARCHAR2(10) NOT NULL,
                       service_year NUMBER,
                       PRIMARY KEY (wagon_number)
);

-- =============================================
-- FOREIGN KEY CONSTRAINTS
-- =============================================

ALTER TABLE RAILWAY_LINE ADD CONSTRAINT fk_line_owner
    FOREIGN KEY (owner_id) REFERENCES OWNER(owner_id);

ALTER TABLE RAILWAY_LINE ADD CONSTRAINT fk_line_start_facility
    FOREIGN KEY (start_facility_id) REFERENCES FACILITY(facility_id);

ALTER TABLE RAILWAY_LINE ADD CONSTRAINT fk_line_end_facility
    FOREIGN KEY (end_facility_id) REFERENCES FACILITY(facility_id);

ALTER TABLE LINE_SEGMENT ADD CONSTRAINT fk_segment_line
    FOREIGN KEY (line_id) REFERENCES RAILWAY_LINE(line_id);

ALTER TABLE ROLLING_STOCK ADD CONSTRAINT fk_stock_operator
    FOREIGN KEY (operator_id) REFERENCES OPERATOR(operator_id);

ALTER TABLE WAGON ADD CONSTRAINT fk_wagon_model
    FOREIGN KEY (model_id) REFERENCES WAGON_MODEL(model_id);

ALTER TABLE WAGON ADD CONSTRAINT fk_wagon_operator
    FOREIGN KEY (operator_id) REFERENCES OPERATOR(operator_id);