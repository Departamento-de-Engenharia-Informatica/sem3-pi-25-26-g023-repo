# 🚀 Project Documentation Hub - SPRINT 3 (Simulation, Flow & DB Procedures)

Welcome to the **Sprint 3** documentation hub, focused on **Graph Flow Algorithms**, **Resource Allocation**, **Database Procedures/Triggers (PL/SQL)**, **Low-Level Sensor Integration**, and **GUI Visualization**.

---

## 📚 Curricular Unit Documentation

---

### 💻 ESINF (Graph Algorithms, Flow & Optimization)

Implementation of complex graph algorithms for network flow and topology analysis.

* **[📊 Global Complexity Analysis (All US)](src/doc/ESINF/USEI%20COMPLEXIDADES.pdf)** 📉

* **USEI11: Subgraph & Connectivity Analysis** 🕸️
    * *Objective: Analyze the connectivity of the railway network and identify subgraphs.*
    * [📜 Requirements](src/doc/ESINF/USEI11/01.requirements-engineering/USEI11-requirements.md)
    * [📊 Analysis](src/doc/ESINF/USEI11/02.analysis/USEI11-analysis.md)
    * [🛠️ Design](src/doc/ESINF/USEI11/03.design/USEI11-design.md)
* **USEI12: Minimal Backbone Network** 📉
    * *Objective: Identify the minimum set of tracks to keep the network connected (MST).*
    * [📜 Requirements](src/doc/ESINF/USEI12/01.requirements-engineering/USEI12-requirements.md)
    * [📊 Analysis](src/doc/ESINF/USEI12/02.analysis/USEI12-analysis.md)
    * [🛠️ Design](src/doc/ESINF/USEI12/03.design/USEI12-design.md)
* **USEI13: Network Metrics** 🛤️
    * *Objective: Calculate centrality metrics (Betweenness, Closeness) to identify network hubs.*
    * [📜 Requirements](src/doc/ESINF/USEI13/01.requirements-engineering/USEI13-requirements.md)
    * [📊 Analysis](src/doc/ESINF/USEI13/02.analysis/USEI11-analysis.md)
* **USEI14: Max Flow Calculation** 🌊
    * *Objective: Calculate the maximum flow of goods between a source and a sink in the network.*
    * [📜 Requirements](src/doc/ESINF/USEI14/01.requirements-engineering/USEI14-requirements.md)
    * [📊 Analysis](src/doc/ESINF/USEI14/02.analysis/USEI14-analysis.md)
    * [🛠️ Design](src/doc/ESINF/USEI14/03.design/USEI14-design.md)
* **USEI15: Optimal Upgrade Plan** ✂️
    * *Objective: Identify critical segments (bottlenecks) and determine the most economical upgrade plan.*
    * [📜 Requirements](src/doc/ESINF/USEI15/01.requirements-engineering/USEI15-requirements.md)

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
    * [📜 Requirements](src/doc/LAPR3/USLP08/01.requirements-engineering/USLP08-requirements.md)
    * [📊 Analysis](src/doc/LAPR3/USLP08/02.analysis/USLP08-analysis.md)
    * [🛠️ Design](src/doc/LAPR3/USLP08/03.design/USLP08-design.md)
* **USLP09: C/Assembly Integration (Sensors)** 🌡️
    * [📜 Requirements](src/doc/LAPR3/USLP09/01.requirements-engineering/USLP09-requirements.md)
    * [📊 Analysis](src/doc/LAPR3/USLP09/02.analysis/USLP09-analysis.md)
    * [🛠️ Design](src/doc/LAPR3/USLP09/03.design/USLP09-design.md)
* **USLP10: Graphical User Interface (Network Visualization)** 🖥️
    * [📜 Requirements](src/doc/LAPR3/USLP10/01.requirements-engineering/USLP10-requirements.md)
    * [📊 Analysis](src/doc/LAPR3/USLP10/02.analysis/USLP10-analysis.md)
    * [🛠️ Design](src/doc/LAPR3/USLP10/03.design/USLP10-design.md)

---

### 🖥️ ARQCP (Low-Level Optimization & Control)

Advanced C and Assembly routines for hardware control, sensor management, and system monitoring.

* **USAC08: Fan Control System** 💨
    * [📄 C Source](src/doc/ARQCP/USAC08/main.c) | [⚙️ Assembly](src/doc/ARQCP/USAC08/asm.s)
* **USAC09: Checksum Optimization (SIMD)** ⚡
    * [📄 C Source](src/doc/ARQCP/USAC09/main.c) | [⚙️ Assembly](src/doc/ARQCP/USAC09/asm.s)
* **USAC10: Station Control Hardware** 🔌
    * [🔧 Arduino Source](src/doc/ARQCP/USAC10/hardware/station_ctrl/station_ctrl.ino)
* **USAC11: Dynamic Config Loader** ⚙️
    * [📄 Implementation](src/doc/ARQCP/USAC11/config_loader.c)
* **USAC12: Log Creator System** 📝
    * [📄 Implementation](src/doc/ARQCP/USAC12/usac12_log_creator.c)
* **USAC13: Sensor Data Handler** 📡
    * [📄 Implementation](src/doc/ARQCP/USAC13/usac13_sensor_handler.c)
* **USAC14: Light Signs Control** 🚥
    * [📄 Implementation](src/doc/ARQCP/USAC14/usac14_lightsigns.c)
* **USAC15: Station Board Display** 📟
    * [📄 Implementation](src/doc/ARQCP/USAC15/usac15_board_display.c)
* **USAC16: Track Management System** 🛤️
    * [📄 Implementation](src/doc/ARQCP/USAC16/usac16_track_manager.c)
