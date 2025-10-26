# USEI05 — Returns & Quarantine

## 1. User Story
**As a quality operator**,  
I want returned goods to be placed in **quarantine**,  
so that I can inspect them in the **reverse order they arrived (latest first)**,  
process them (either discard or restock),  
and create an **audit log** with the actions taken on each product.

## 2. Context
Returned products cannot immediately re-enter inventory.  
They must first be placed in **quarantine** to ensure quality and safety.  
This procedure avoids damaged or expired goods being mixed with usable stock.

Once quarantined, items are inspected following **LIFO (Last-In-First-Out)** order — the most recent returns are processed first.  
After inspection, they are either **discarded** (if damaged/expired) or **restocked** (if usable).  
Every inspection must be logged to guarantee full traceability.

## 3. Actors
| Actor | Type | Description |
|--------|------|-------------|
| **Quality Operator** | Primary | Performs the inspection and triggers the quarantine process. |
| **Warehouse Management System (WMS)** | System | Validates, stores, and processes returns; manages quarantine, inventory, and audit log. |

## 4. Pre-conditions
- The file `returns.csv` is successfully loaded and validated in the system.
- Each return record contains: `returnId`, `sku`, `qty`, `reason`, `timestamp`, and optional `expiryDate`.
- The warehouse inventory (`Inventory`) and quarantine structures (`Quarantine`) are initialized.

## 5. Main Flow of Events
| Step | Actor / System Action | Description |
|------|------------------------|-------------|
| 1 | **Quality Operator** uploads `returns.csv`. | The operator imports customer returns into the system. |
| 2 | **System** validates the records. | Invalid entries (e.g., negative qty, missing SKU) are rejected with error messages. |
| 3 | **System** places valid returns into **quarantine** (stack), ordered by descending `timestamp`. |
| 4 | **Quality Operator** triggers the inspection process. | The operator requests to process the quarantine queue. |
| 5 | **System** pops the latest return (top of stack) and inspects it. | Inspection determines if the product is usable. |
| 6a | **If restockable:** The system creates a **new box** with `boxId = "RET-" + returnId`, assigns a location, and inserts it into the inventory using FEFO/FIFO rules. |
| 6b | **If not restockable:** The system flags the return as **Discarded**. |
| 7 | **System** logs the action into an **audit file**, including timestamp, returnId, sku, action (Restocked/Discarded), and qty. |
| 8 | **System** repeats until quarantine is empty. |
| 9 | **System** produces a summary report of processed returns. |

## 6. Alternative / Exception Flows
| # | Condition | Description |
|---|------------|-------------|
| A1 | Invalid data in `returns.csv` | The system reports the specific error and skips the record. |
| A2 | Unknown SKU | Return is rejected and flagged for manual review. |
| A3 | Expired product | Automatically marked as *Discarded*. |
| A4 | Partial restock | If only part of the quantity is usable, both quantities (restocked and discarded) are logged separately. |

## 7. Post-conditions
- All valid returns have been either **restocked** or **discarded**.
- An **audit log file** is generated (or updated), ensuring full traceability.
- The inventory reflects all restocked items.
- The quarantine stack is empty after processing.

## 8. Acceptance Criteria, Non-functional Requirements and References
1. Returns are processed in **descending timestamp order** (latest first).
2. The system supports both **full** and **partial restocks**.
3. Each processed return generates a log line in the format:

4. Restocked items are inserted as new boxes (`RET-<returnId>`) following **FEFO/FIFO** insertion rules (based on expiryDate and receivedAt).
5. Discarded items are flagged and never reintroduced into stock.
6. All operations must be deterministic and produce consistent, traceable outputs.
7. The system must ensure data integrity during restock/discard operations and guarantee that every action is recorded in the audit log.
8. The module must be easy to maintain and integrate with the main warehouse inventory system.
9. Quarantine processing must handle large datasets efficiently, using stack operations (LIFO).
10. All processing steps must conform to the requirements described in the official project specification.

