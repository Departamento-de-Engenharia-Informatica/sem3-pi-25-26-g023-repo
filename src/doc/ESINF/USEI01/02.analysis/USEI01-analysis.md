# US001 - Create a map 

## 2. Analysis

### 2.1. Relevant Domain Model Excerpt 

![Domain Model](svg/USEI01-DM.svg)

### 2.2. Other Remarks

* FEFO/FIFO Sorting: Inventory ensures that perishable boxes (with expiryDate) are sorted first by earliest expiry, then by receivedDate for non-perishables, and finally by boxId as tie-breaker.
* Validation Rules: Wagons are validated upon unloading for unknown SKUs, invalid quantities, missing receivedDate, duplicate boxId, or invalid expiry dates. Invalid records are rejected and logged.
* Traceability: Every unloaded box is logged with timestamp, wagon ID, aisle/bay, and SKU information.
* Bays and Aisles: Empty boxes are removed but bays remain; relocation or dispatch preserves FEFO/FIFO ordering.