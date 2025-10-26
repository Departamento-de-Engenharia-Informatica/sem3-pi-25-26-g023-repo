# USEI01 - Wagons Unloading (Inventory Replenishment)

## 1. Requirements Engineering

### 1.1. User Story Description

As a terminal operator, I want unloading operations of wagons to automatically store inventory using FEFO (First-Expired-First-Out) for perishable goods and FIFO (First-In-First-Out) for non-perishable goods, so that I can ensure correct dispatch order, minimize product spoilage, and maintain full traceability.

### 1.2. Customer Specifications and Clarifications 

**From the specifications document:**

>	Wagons arriving at the terminal contain several boxes of products, each representing a SKU with quantity and optional expiryDate. When unloading into warehouses, the sequence of storage must follow FEFO for perishable items and FIFO for non-perishable items.

>	Warehouses are organized in aisles and bays. Each box has a unique ID and a location in the warehouse (aisle/bay).

> The system must validate incoming wagon data: known SKUs, valid quantities, unique box IDs, valid dates, and report errors while still importing valid data.

**From the client clarifications:**

> **Question:** When the user wants to unload wagons, is it mandatory to unload all of them at once, or can they select specific wagons to process? Is it also possible to choose which warehouses will store the cargo, or are all warehouses automatically considered?
>
> **Answer:** The system supports both options: the user can choose to unload all wagons at once or select specific wagons to process. Regarding storage, the existing warehouses are used following a fixed precedence (e.g., sorted by warehouseId in ascending order), filling each warehouse to its capacity before moving on to the next one. It is not possible to manually select which warehouses will store the cargo.
> 
> **Question:** If you try to unload wagons at a terminal's warehouses and a warehouse does not have enough capacity to receive all the boxes, what should happen? Should the warehouse receive only part of the wagon's cargo and the rest be stored in another warehouse? And what if the terminal currently does not have enough warehouse space to store the wagons' content?
> 
> **Answer:** The boxes of a wagon can be stored across multiple warehouses at the same terminal. Existing warehouses are used following a fixed precedence (e.g., sorted by warehouseId in ascending order), filling each warehouse to its capacity before moving on to the next one. If all warehouses are full and some wagons cannot be unloaded, the system must log the wagons that could not be processed.
> 
> **Question:** Should boxes in a bay be physically ordered by FIFO/FEFO, or is it sufficient for the system to indicate which box of a given SKU should be used next, regardless of placement in the bay? How is relocation triggered and managed?
>
> **Answer:** Boxes in a bay do not need to be physically reordered. The bay maintains a logical list ordered by expiryDate, receivedAt, and boxId across all SKUs. FEFO/FIFO is used to determine which box to pick, not as a physical constraint. Relocation must be requested manually by the warehouse manager; it is not automatic.
### 1.3. Acceptance Criteria

* **AC1:** Boxes are inserted into inventory following FEFO/FIFO rules:
  * Perishable goods: earliest expiryDate first.
  * Non-perishable goods: earliest receivedDate first.
  * Tie-breaker: boxId ascending.
  
* **AC2:** Invalid wagon records (unknown SKU, negative qty, missing expiry/received dates, duplicated boxId) are rejected with clear error messages.

* **AC3:** After unloading, inventory reflects the new boxes in the correct order.

* **AC4:** Warehouse locations (aisle/bay) are preserved or assigned as per rules.

* **AC5:** All movements are logged for traceability.

### 1.4. Found out Dependencies

* Warehouse inventory must be initialized.
* Wagons data must be imported and validated.
* SKUs must exist in items.csv.
* Bays and aisles must exist and have capacity for new boxes.

### 1.5 Input and Output Data

**Input Data:**

* Wagon records: wagonId, boxId, SKU, qty, expiryDate (optional), receivedAt.
* Warehouse layout: bays.csv (warehouseId, aisle, bay, capacityBoxes).
* roduct data: items.csv (SKU, name, category, unit, volume, unitWeight).

**Output Data:**

* Updated inventory list (boxes sorted by FEFO/FIFO).
* Log of accepted and rejected boxes with error messages.
* Confirmation message for the operator: number of boxes successfully unloaded.

### 1.6. System Sequence Diagram (SSD)

![System Sequence Diagram](svg/USEI01-SSD.svg)

### 1.7 Other Relevant Remarks

* Unloading preserves FEFO/FIFO order per SKU.
* Boxes with invalid data do not block unloading of valid boxes.
* Empty boxes are removed from inventory, but bays remain.
* Every operation is logged for traceability.
* The Inventory class ensures automatic sorting and storage of boxes.