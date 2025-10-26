# USEI05 — Analysis

## 1. Objective
The goal of this analysis phase is to define the **domain model** for the *Returns & Quarantine* functionality of the Warehouse Management System (WMS).  
This model represents the main **entities**, **relationships**, and **responsibilities** involved in handling product returns, placing them in quarantine, inspecting them, and either restocking or discarding them according to the business rules.

The analysis focuses on the logical structure of the system, independent of implementation details.

---

## 2. Overview
When returned goods arrive at the terminal, they cannot be directly placed back into inventory.  
They must first be held in **quarantine**, where they are inspected following **LIFO (Last-In-First-Out)** order — most recent returns are processed first.

After inspection:
- If the item is **restockable**, a new box is created and inserted into the **Inventory** following FEFO/FIFO rules.
- If the item is **not restockable**, it is **discarded** and flagged as such.
- Each decision is recorded in an **Audit Log**, ensuring traceability.

This process ensures that damaged or expired products never contaminate usable stock and that all actions are properly tracked.

---

## 3. Domain Model Description
The following domain classes were identified and modeled in `USEI05-DM.puml`:

| Class | Description |
|--------|-------------|
| **Return** | Represents a single product return. Contains identification data (returnId, sku, qty), reason for return, timestamp of arrival, and an optional expiry date. |
| **Quarantine** | Represents the temporary holding area for returns awaiting inspection. Implemented conceptually as a *stack* (LIFO). Provides operations `push()`, `pop()`, and `isEmpty()`. |
| **Inventory** | Represents the warehouse’s active stock management. Handles reinsertion of restockable products into available bays, creating new `Box` records according to FEFO/FIFO logic. |
| **Box** | Represents an individual storage unit or container within the warehouse. Each restocked item creates a new `Box` entry with its location, SKU, and tracking information. |
| **AuditLog** | Handles all logging of inspection results (Restocked/Discarded) with details such as returnId, sku, quantity, and timestamp. Provides operation `writeLog()`. |

---

## 4. Relationships Between Classes

| Relationship | Description |
|---------------|-------------|
| **Quarantine 1..* → Return** | A quarantine contains multiple returns to be processed. |
| **Inventory 1..* → Box** | The inventory manages multiple boxes of goods. |
| **Inventory → AuditLog** | The inventory module communicates with the audit log to register inspection outcomes. |
| **Quarantine → Inventory** | When a return is inspected and approved, the quarantine interacts with the inventory to restock it. |
| **Return → Box** | A restocked return originates a new box entry with updated information. |

---

## 5. Key Analysis Decisions

1. **Data Structure for Quarantine:**  
   A stack (LIFO) was chosen to satisfy the requirement “inspect returns in reverse order of arrival”.

2. **Reuse of FEFO/FIFO Rules:**  
   The same inventory rules defined in USEI01 (Wagons Unloading) are reused to guarantee consistency in stock management.

3. **Audit Logging as a Persistent Trace:**  
   A dedicated component `AuditLog` ensures each inspection action is recorded in a deterministic, traceable way.

4. **Encapsulation of Responsibilities:**
    - `Quarantine` handles temporary storage and access order.
    - `Inventory` manages physical stock updates.
    - `AuditLog` is responsible only for traceability.
    - `Return` and `Box` are data entities.

5. **Extensibility Considerations:**  
   The design allows future extensions (e.g., automated inspection rules, quarantine time limits, or integration with reporting systems).

---

## 6. Domain Model Diagram
The domain model is represented in **PlantUML** format in the file `USEI05-DM.puml`.  
An SVG version (`USEI05-DM.svg`) should also be exported for documentation.

**Diagram Summary:**
- Shows the main entities (`Return`, `Quarantine`, `Inventory`, `Box`, `AuditLog`).
- Illustrates the logical relationships between them.
- Focuses on *what* data and relationships exist, not *how* they are implemented.

---

## 7. Traceability and Alignment
This model is derived directly from the requirements specified in:

- *Project Assignment - Sprint 1_v1.pdf*, section **3.2.1 – USEI05 (Returns & Quarantine)**
- *USEI01 – Wagons Unloading*, for FEFO/FIFO consistency in restock operations

The domain model ensures conceptual continuity between the **requirements phase** and the **design phase**, serving as the foundation for the following diagrams:
- Class Diagram (`USEI05-CD.puml`)
- Sequence Diagram (`USEI05-SD-full.puml`)

---

## 8. Conclusion
The analysis phase defines the conceptual structure of the Returns & Quarantine functionality.  
It establishes clear separation between temporary quarantine management, inventory control, and audit logging.  
This model guarantees compliance with traceability, safety, and quality control requirements, forming the logical basis for the detailed design and implementation phases.
