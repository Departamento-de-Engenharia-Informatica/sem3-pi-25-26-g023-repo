# USEI02 - A warehouse planner must fulfill the orders it receives

## 1. Requirements Engineering

### 1.1. User Story Description

USEI02- As a warehouse planner, when I receive open orders, I want the
system to examine current inventory and allocate quantities from boxes in FEFO
order, and produce per-line statuses (ELIGIBLE, PARTIAL, UNDISPATCHABLE)
and a list of allocation rows with box and bay information.


### 1.2. Customer Specifications and Clarifications 

**From the specifications document:**

>	A warehouse planner must fulfill the orders it receives by examining current inventory and allocating quantities from boxes in FEFO order.
>   A warehouse planner must produce per-line statuses (ELIGIBLE, PARTIAL, UNDISPATCHABLE) and a list of allocation rows with box and bay information.
>   Orders are processed by: priority ASC, dueDate ASC, orderId ASC. Within an order, lines are processed by lineNo ASC (input order).

**From the client clarifications:**

* N/A

### 1.3. Acceptance Criteria

* **AC1:** Orders are processed by: priority ASC, dueDate ASC, orderId ASC. Within an order, lines are processed by lineNo ASC (input order).
* **AC2:** For each line, allocation walks the SKU’s boxes in FEFO/FIFO order.
* **AC3:** For each visited bay, allocate take min(remainingQty, box.qtyAvailable), reduce remainingQty, and continue until the request is satisfied or boxes end. The available
box quantity never goes below zero during planning.
* **AC4:** Line status is ELIGIBLE if fully allocated, PARTIAL if partially allocated, UNDISPATCHABLE if not allocated at all.


### 1.4. Found out Dependencies

* There is a dependency on "USEI01" since that there would be no stock to allocate if the wagons had not been loaded in the system.

### 1.5 Input and Output Data

**Input Data:**

* Typed data:
    * orderId
    * lineNo
    * sku
    * qtyRequested
    * priority
    * dueDate

**Output Data:**

* Typed data:
    * orderId
    * lineNo
    * status (ELIGIBLE, PARTIAL, UNDISPATCHABLE)
    * allocations (list of allocation rows with box and bay information)



### 1.6. System Sequence Diagram (SSD)

![System Sequence Diagram](svg/US001-SSD.svg)

**_Other alternatives might exist._**

### 1.7 Other Relevant Remarks

* The created task stays in a "not published" state in order to distinguish from "published" tasks.