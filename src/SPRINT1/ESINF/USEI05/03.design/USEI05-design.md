# USEI05 — Design

## 1. Objective
The purpose of this design phase is to define the **implementation-level structure** and the **dynamic behaviour** of the *Returns & Quarantine* functionality.  
This section translates the conceptual model (from the Analysis phase) into concrete classes, methods, and interactions between system components.  
It ensures that the final implementation can meet the acceptance criteria defined in the requirements.

---

## 2. Overview
The design defines how the Warehouse Management System (WMS) handles the life cycle of returned goods:
1. Returns are kept in a **Quarantine** stack (LIFO).
2. Each return is evaluated by the **WMS**.
3. If usable, it is **restocked** into **Inventory** (a new `Box` is created using FEFO/FIFO rules).
4. If not usable, it is **discarded**.
5. Every action is recorded in the **AuditLog**.

This phase refines the domain model into an operational class design, providing:
- **Method-level detail** for each class.
- **Sequence diagrams** describing how components interact.
- **Traceability** between static (structure) and dynamic (behaviour) aspects.

---

## 3. Design Class Model
The class diagram (`USEI05-CD.puml`) represents the static structure of the system at design level.

### Main Classes

| Class | Responsibility | Key Methods |
|--------|----------------|-------------|
| **WMS** | Central controller for the Returns & Quarantine process. Coordinates the flow between quarantine, inventory, and logging. | `processReturns()` |
| **Return** | Represents a returned product. Evaluates its own reusability based on condition and expiry. | `isRestockable()` |
| **Quarantine** | Manages the temporary stack of returns awaiting inspection. Provides access to the next item to process. | `addReturn()`, `getNextReturn()`, `isEmpty()` |
| **Inventory** | Manages boxes in storage and handles reinsertion of restocked items. Applies FEFO/FIFO logic. | `restock()`, `createBoxFromReturn()`, `insertBoxFEFO()` |
| **Box** | Represents a container for products in the warehouse. Includes metadata to support FEFO sorting. | `compareTo()` |
| **AuditLog** | Handles audit trail creation for each processed return. Writes detailed log entries with timestamps. | `writeLog()`, `writePartialLog()` |

---

### Key Design Decisions
1. **Centralised Orchestration in WMS:**  
   The `WMS` class acts as the main entry point, ensuring a clean separation between process control, data storage, and logging.

2. **Encapsulation of Business Logic:**
    - `Return` encapsulates decision logic (`isRestockable`).
    - `Inventory` encapsulates reintegration logic and ordering rules.
    - `AuditLog` abstracts away file-handling details.

3. **Consistency with Previous Use Cases:**  
   The same FEFO/FIFO insertion logic defined in **USEI01 (Wagons Unloading)** is reused for restocking operations.

4. **Traceability & Auditability:**  
   Every operation (restock/discard) triggers a deterministic log entry, supporting traceability and debugging.

5. **Extendability:**  
   The design allows additional actions (e.g., quarantine expiry, automatic disposal) without breaking current logic.

---

## 4. Behavioural Model (Sequence Diagram)
The dynamic behaviour of the system is represented in the sequence diagram (`USEI05-SD-full.puml`).

### Flow Description

| Step | Description |
|------|-------------|
| 1 | The **Quality Operator** triggers the process (`processReturns()`). |
| 2 | The **WMS** checks if the quarantine is empty. |
| 3 | If not empty, the system iterates through all returns in **LIFO** order. |
| 4 | For each return, the **WMS** checks whether it is restockable (`isRestockable()`). |
| 5 | If restockable, a new `Box` is created and inserted into the inventory using FEFO/FIFO logic. |
| 6 | If not restockable, the return is marked as discarded. |
| 7 | Each action (restocked/discarded) is logged in the **AuditLog**. |
| 8 | The **WMS** continues until all items are processed. |
| 9 | Finally, a summary is returned to the operator. |

This sequence ensures:
- Deterministic execution (no randomness or data loss).
- Full traceability through the `AuditLog`.
- Proper encapsulation of behaviour within dedicated classes.

---

## 5. Diagram Summaries

### `USEI05-CD.puml` — Class Diagram
Shows the relationships between `WMS`, `Quarantine`, `Inventory`, `AuditLog`, and entities `Return` and `Box`.  
Highlights main attributes, public methods, and aggregation relationships.

### `USEI05-SD-full.puml` — Sequence Diagram
Illustrates the chronological order of method calls between the main components during the processing of returns, ensuring the correct use of LIFO and FEFO/FIFO rules.

---

## 6. Traceability
This design directly implements the requirements described in:

- *Project Assignment - Sprint 1_v1.pdf*, section **3.2.1 – USEI05 (Returns & Quarantine)**
- *USEI01 – Wagons Unloading*, for FEFO/FIFO consistency

The class and sequence diagrams provide traceability from:
- **Requirements → Analysis → Design → Implementation**

---

## 7. Conclusion
The design phase defines the concrete system structure and behaviour for the Returns & Quarantine process.  
It ensures that the implemented system will:
- Respect the LIFO quarantine policy,
- Guarantee accurate FEFO/FIFO restocking,
- Maintain complete auditability through log files,
- Be modular, testable, and extensible for future enhancements.

The next step after this phase is the implementation and testing of the corresponding classes in Java, following TDD principles.
