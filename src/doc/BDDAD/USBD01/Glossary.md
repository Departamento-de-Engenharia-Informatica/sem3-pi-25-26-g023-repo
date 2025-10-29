# 📖 Data Dictionary (USBD01)
This data dictionary describes all entities and attributes of the railway system in a consolidated table format.

---

| Attribute | Data Type | Description |
|-----------|-----------|-------------|
| **OPERATOR** | | |
| operator_id | VARCHAR2(10) | Unique identifier for the railway operator |
| name | VARCHAR2(100) | Operator name |
| type | VARCHAR2(50) | Type of operator (Infrastructure Owner, Train Operator) |
| contact_email | VARCHAR2(100) | Contact email |
| phone | VARCHAR2(20) | Phone number |
| **STATION** | | |
| station_id | VARCHAR2(10) | Unique identifier for the station |
| name | VARCHAR2(100) | Station name |
| type | VARCHAR2(50) | Station type |
| has_warehouse | CHAR(1) | Whether the station has a warehouse |
| has_refrigerated | CHAR(1) | Whether the station has refrigerated storage |
| latitude | NUMBER(10,6) | Geographic latitude |
| longitude | NUMBER(10,6) | Geographic longitude |
| **RAILWAY_LINE** | | |
| line_id | VARCHAR2(10) | Unique identifier for the railway line |
| name | VARCHAR2(100) | Line name |
| owner_operator_id | VARCHAR2(10) | Operator responsible for the line |
| total_length_km | NUMBER(8,2) | Total length of the line in kilometers |
| **LINE_SEGMENT** | | |
| segment_id | VARCHAR2(10) | Unique identifier for the line segment |
| line_id | VARCHAR2(10) | Line to which the segment belongs |
| start_station_id | VARCHAR2(10) | Starting station of the segment |
| end_station_id | VARCHAR2(10) | Ending station of the segment |
| segment_length_km | NUMBER(8,2) | Segment length in kilometers |
| track_type | VARCHAR2(20) | Track type: Single, Double, Multiple |
| gauge_mm | NUMBER(5,1) | Track gauge in millimeters |
| is_electrified | CHAR(1) | Whether the segment is electrified |
| max_weight_kg_per_m | NUMBER(8,2) | Maximum weight supported (kg/m) |
| max_speed_kmh | NUMBER(4) | Maximum speed allowed (km/h) |
| **ROLLING_STOCK** | | |
| stock_id | VARCHAR2(10) | Unique identifier for rolling stock |
| operator_id | VARCHAR2(10) | Owner operator |
| make | VARCHAR2(50) | Manufacturer |
| model | VARCHAR2(50) | Model name |
| year_of_service | NUMBER(4) | Year entered into service |
| gauge_mm | NUMBER(5,1) | Compatible gauge |
| length_m | NUMBER(5,2) | Length in meters |
| width_m | NUMBER(5,2) | Width in meters |
| height_m | NUMBER(5,2) | Height in meters |
| tare_weight_kg | NUMBER(8,2) | Empty weight in kg |
| number_of_bogies | NUMBER(2) | Number of bogies |
| **LOCOMOTIVE** | | |
| stock_id | VARCHAR2(10) | Locomotive identifier |
| locomotive_type | VARCHAR2(20) | Type: Diesel, Electric |
| power_kw | NUMBER(6) | Power in kilowatts |
| acceleration_kmh_s | NUMBER(3,2) | Acceleration (km/h per second) |
| max_total_weight_kg | NUMBER(8,2) | Maximum total weight |
| fuel_capacity_l | NUMBER(6) | Fuel capacity in liters (for diesel) |
| supports_multiple_gauges | CHAR(1) | Whether it supports multiple gauges |
| **WAGON** | | |
| stock_id | VARCHAR2(10) | Wagon identifier |
| wagon_type | VARCHAR2(30) | Type: Container, Tank, Flatcar, etc. |
| payload_capacity_kg | NUMBER(8,2) | Maximum payload capacity in kg |
| volume_capacity_m3 | NUMBER(8,2) | Volume capacity in cubic meters |
| container_supported | VARCHAR2(50) | Supported container sizes |
| is_refrigerated | CHAR(1) | Whether it's refrigerated |
| max_pressure_bar | NUMBER(5,2) | Maximum pressure (for tank cars) |
| **WAREHOUSE** | | |
| warehouse_id | VARCHAR2(20) | Unique warehouse identifier |
| name | VARCHAR2(100) | Warehouse name |
| **BAY** | | |
| warehouse_id | VARCHAR2(20) | Warehouse identifier |
| aisle | NUMBER(4) | Aisle number |
| bay_number | NUMBER(4) | Bay number |
| capacity_boxes | NUMBER(4) | Maximum box capacity |
| **ITEM** | | |
| sku | VARCHAR2(20) | Stock Keeping Unit |
| name | VARCHAR2(100) | Product name |
| category | VARCHAR2(50) | Product category |
| unit | VARCHAR2(20) | Measurement unit |
| volume | NUMBER(8,2) | Unit volume |
| unit_weight | NUMBER(8,2) | Unit weight |
| **BOX** | | |
| box_id | VARCHAR2(20) | Unique box identifier |
| qty_available | NUMBER(6) | Available quantity |
| expiry_date | DATE | Expiration date (for perishable goods) |
| received_at | TIMESTAMP | Receipt timestamp |
| sku | VARCHAR2(20) | Product SKU |
| aisle | NUMBER(4) | Aisle location |
| bay | NUMBER(4) | Bay location |
| warehouse_id | VARCHAR2(20) | Warehouse identifier |
| **CUSTOMER_ORDER** | | |
| order_id | VARCHAR2(20) | Order identifier |
| due_date | DATE | Due date |
| priority | NUMBER(2) | Priority level |
| **ORDER_LINE** | | |
| order_id | VARCHAR2(20) | Order identifier |
| line_no | NUMBER(3) | Line number |
| sku | VARCHAR2(20) | Product SKU |
| qty | NUMBER(6) | Quantity ordered |
| **RETURN_ITEM** | | |
| return_id | VARCHAR2(20) | Return identifier |
| sku | VARCHAR2(20) | Product SKU |
| qty | NUMBER(6) | Return quantity |
| reason | VARCHAR2(50) | Return reason |
| timestamp | TIMESTAMP | Return timestamp |
| expiry_date | DATE | Expiration date |