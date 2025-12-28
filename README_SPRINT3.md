# 🚀 Project Documentation Hub - SPRINT 3 (Simulation, Flow & DB Procedures)

Welcome to the **Sprint 3** documentation hub, focused on **Graph Flow Algorithms**, **Resource Allocation**, **Database Procedures/Triggers (PL/SQL)**, **Low-Level Sensor Integration**, and **GUI Visualization**.

---

## 📚 Curricular Unit Documentation

---

### 💻 ESINF (Graph Algorithms, Flow & Optimization)

Implementation of complex graph algorithms for network flow and topology analysis.

* **USEI11: Subgraph & Connectivity Analysis** 🕸️
    * *Objective: Analyze the connectivity of the railway network and identify subgraphs.*
    * [📜 Requirements](src/doc/ESINF/USEI11/01.requirements-engineering/USEI11-requirements.md)
    * [📊 Analysis](src/doc/ESINF/USEI11/02.analysis/USEI11-analysis.md)
    * [🛠️ Design](src/doc/ESINF/USEI11/03.design/USEI11-design.md)
* **USEI12: Algorithm Complexity Analysis** 📉
    * *Objective: Theoretical analysis of the time and space complexity of the implemented algorithms.*
    * [📜 Requirements](src/doc/ESINF/USEI12/01.requirements-engineering/USEI12-requirements.md)
    * [📊 Analysis](src/doc/ESINF/USEI12/02.analysis/USEI12-analysis.md)
    * [🛠️ Design](src/doc/ESINF/USEI12/03.design/USEI12-design.md)
* **USEI13: Best Route Calculation** 🛤️
    * *Objective: Find the optimal path between stations considering specific constraints (weight, distance).*
    * [📜 Requirements](src/doc/ESINF/USEI13/01.requirements-engineering/USEI13-requirements.md)
    * [📊 Analysis](src/doc/ESINF/USEI13/02.analysis/USEI11-analysis.md)
    * [puml] Class Diagram](src/doc/ESINF/USEI13/03.design/puml/USEI11-CD.puml)
* **USEI14: Max Flow Calculation** 🌊
    * *Objective: Calculate the maximum flow of goods between a source and a sink in the network.*
    * [📜 Requirements](src/doc/ESINF/USEI14/01.requirements-engineering/USEI14-requirements.md)
    * [📊 Analysis](src/doc/ESINF/USEI14/02.analysis/USEI14-analysis.md)
    * [🛠️ Design](src/doc/ESINF/USEI14/03.design/USEI14-design.md)
    * [📊 Complexity Analysis](src/doc/ESINF/USEI14/Analise_Complexidade_USEI14.pdf)
* **USEI15: Network Bottlenecks (Min-Cut)** ✂️
    * *Objective: Identify critical segments (bottlenecks) in the network capacity.*
    * [📜 Requirements](src/doc/ESINF/USEI15/01.requirements-engineering/USEI15-requirements.md)
    * [svg] Sequence Diagram](src/doc/ESINF/USEI15/01.requirements-engineering/svg/USEI15-SSD.svg)

---

### 💾 BDDAD (PL/SQL Development - Procedures, Functions & Triggers)

Advanced database logic using PL/SQL for data integrity and automation.

* **USBD31: Automatic Train Composition** 🧩
    * [📄 SQL Script](src/doc/BDDAD/USBD31/relational_model.sql)
* **USBD32: Trigger for Capacity Validation** 🚦
    * [📄 SQL Script](src/doc/BDDAD/USBD32/relational_model_data.sql)
* **USBD33: Function: Train Max Length** 📏
    * [📄 SQL Script](src/doc/BDDAD/USBD33/train_max_length.sql)
* **USBD38: Procedure: Add New Gauge** 🛠️
    * [📄 SQL Script](src/doc/BDDAD/USBD38/add_new_gauge.sql)
* **USBD39: Procedure: Update Train Logistics** 🔄
    * [📄 SQL Script](src/doc/BDDAD/USBD39/update_train.sql)
* **USBD41: Procedure: Remove Freight** 🗑️
    * [📄 SQL Script](src/doc/BDDAD/USBD41/remove_freight_from_train.sql)
* **USBD44: Procedure: Add Segment to Line** ➕
    * [📄 SQL Script](src/doc/BDDAD/USBD44/add_segment_to_line.sql)

---

### 🚆 LAPR3 (Simulation, Integration & UI)

Integration of low-level modules, detailed simulation, and Graphical User Interface.

* **USLP08: Detailed Simulation Report** 📋
    * *Objective: Generate a detailed report of the train run, including arrival times and energy consumption.*
    * [📜 Requirements](src/doc/LAPR3/USLP08/01.requirements-engineering/USLP08-requirements.md)
    * [📊 Analysis](src/doc/LAPR3/USLP08/02.analysis/USLP08-analysis.md)
    * [🛠️ Design](src/doc/LAPR3/USLP08/03.design/USLP08-design.md)
* **USLP09: C/Assembly Integration (Sensors)** 🌡️
    * *Objective: Integrate the C/Assembly modules to receive real-time data from station sensors.*
    * [📜 Requirements](src/doc/LAPR3/USLP09/01.requirements-engineering/USLP09-requirements.md)
    * [📊 Analysis](src/doc/LAPR3/USLP09/02.analysis/USLP09-analysis.md)
    * [🛠️ Design](src/doc/LAPR3/USLP09/03.design/USLP09-design.md)
* **USLP10: Graphical User Interface (Network Visualization)** 🖥️
    * *Objective: Visualize the railway network topology and train positions on a GUI.*
    * [📜 Requirements](src/doc/LAPR3/USLP10/01.requirements-engineering/USLP10-requirements.md)
    * [📊 Analysis](src/doc/LAPR3/USLP10/02.analysis/USLP10-analysis.md)
    * [🛠️ Design](src/doc/LAPR3/USLP10/03.design/USLP10-design.md)

---

### 🖥️ ARQCP (Low-Level Optimization & Control)

Advanced C and Assembly routines for hardware control and optimization.

* **USAC08: Fan Control System (C)** 💨
    * *Objective: Control the cooling fan rotation based on temperature readings.*
    * [📄 C Source](src/doc/ARQCP/USAC08/main.c)
    * [⚙️ Assembly Source](src/doc/ARQCP/USAC08/asm.s)
* **USAC09: Checksum Optimization (SIMD)** ⚡
    * *Objective: Optimize data integrity checks using vector instructions.*
    * [📄 C Source](src/doc/ARQCP/USAC09/main.c)
    * [⚙️ Assembly Source](src/doc/ARQCP/USAC09/asm.s)
