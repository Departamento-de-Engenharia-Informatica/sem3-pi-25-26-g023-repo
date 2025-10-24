# USBD02 - Relational Model Documentation

## 📋 Overview
Complete relational database model for the Railway Cargo Handling System, supporting both railway infrastructure management and warehouse operations.

## 🗂️ Model Statistics
- **Total Tables:** 14
- **Railway Tables:** 7
- **Warehouse Tables:** 7
- **Relationships:** 13
- **One-to-Many Relationships:** 11
- **One-to-One Relationships:** 2

## 🏗️ Database Schema

### Railway Subsystem
- `OPERATOR` - Railway companies and infrastructure owners
- `STATION` - Stations, terminals, and freight yards
- `RAILWAY_LINE` - Railway lines connecting stations
- `LINE_SEGMENT` - Individual segments of railway lines
- `ROLLING_STOCK` - Base table for all railway vehicles
- `LOCOMOTIVE` - Train engines (inherits from ROLLING_STOCK)
- `WAGON` - Freight cars (inherits from ROLLING_STOCK)

### Warehouse Subsystem
- `WAREHOUSE` - Storage facilities
- `BAY` - Storage locations within warehouses
- `ITEM` - Products and goods
- `BOX` - Physical boxes containing items
- `CUSTOMER_ORDER` - Customer orders
- `ORDER_LINE` - Individual order items
- `RETURN_ITEM` - Product returns

## 🔗 Key Relationships

### Railway Relationships
- Operator → Railway Lines (1:N)
- Railway Line → Line Segments (1:N)
- Station → Line Segments (start/end) (1:N)
- Operator → Rolling Stock (1:N)
- Rolling Stock → Locomotive (1:1)
- Rolling Stock → Wagon (1:1)

### Warehouse Relationships
- Warehouse → Bays (1:N)
- Bay → Boxes (1:N)
- Item → Boxes (1:N)
- Item → Order Lines (1:N)
- Item → Returns (1:N)
- Customer Order → Order Lines (1:N)

## 📁 Files Included

- `railway_model_diagram.png` - Complete Entity-Relationship Diagram
- `USBD02_Create_Tables.sql` - SQL implementation script
- `README_USBD02.md` - This documentation file

## 🎯 Design Decisions

1. **Inheritance Pattern**: Used 1:1 relationships for LOCOMOTIVE and WAGON to represent inheritance from ROLLING_STOCK
2. **Composite Keys**: BAY table uses composite primary key (warehouse_id, aisle, bay_number)
3. **Normalization**: All tables are in 3rd normal form to minimize redundancy
4. **Domain Integrity**: Used CHECK constraints for enumerated types (Y/N fields, track types, etc.)

