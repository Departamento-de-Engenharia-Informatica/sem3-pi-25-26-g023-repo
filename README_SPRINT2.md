# 🚀 Project Documentation Hub - SPRINT 2 (Spatial Search & Graphs)

Welcome to the **Sprint 2** documentation hub, focused on **Advanced Data Structures and Algorithms**, **Spatial Search (KD-Tree)**, **Database Population (DML)**, and **Complex Graph Algorithms**.

---

## 📚 Curricular Unit Documentation

---

### 💻 ESINF (Spatial Search, KD-Tree, and Performance)

New functionality focused on high efficiency and advanced data structures.

* **USEI06: Station Index Creation (Hash/BST)** 📊
    * *Objective: Implement the initial indexing structures for fast station searching.*
    * [📜 Requirements](src/doc/ESINF/USEI06/01.requirements-engineering/USEI06-requirements.md)
    * [📊 Analysis](src/doc/ESINF/USEI06/02.analysis/USEI06-analysis.md)
    * [🛠️ Design](src/doc/ESINF/USEI06/03.design/USEI06-design.md)
    * [📊  Análise de Complexidade](src/doc/ESINF/USEI06/AnaliseComplexidadeUSEI06.pdf)
* **USEI07: KD-Tree Construction** 🌳
    * *Objective: Build a **balanced KD-Tree** from coordinates to optimize geographical searches.*
    * [📜 Requirements](src/doc/ESINF/USEI07/01.requirements-engineering/USEI07-requirements.md)
    * [📊 Analysis](src/doc/ESINF/USEI07/02.analysis/USEI07-analysis.md)
    * [🛠️ Design](src/doc/ESINF/USEI07/03.design/USEI07-design.md)
    * [📊  Análise de Complexidade](src/doc/ESINF/USEI07/AnaliseComplexidadeUSEI07.pdf)
* **USEI08: Range Search (Geographical Area)** 🗺️
    * *Objective: Find all stations within a defined geographical rectangle, leveraging **KD-Tree Pruning**.*
    * [📜 Requirements](src/doc/ESINF/USEI08/01.requirements-engineering/USEI08-requirements.md)
    * [📊 Analysis](src/doc/ESINF/USEI08/02.analysis/USEI08-analysis.md)
    * [🛠️ Design](src/doc/ESINF/USEI08/03.design/USEI08-design.md)
    *  [📊  Análise de Complexidade](src/doc/ESINF/USEI08/02.analysis/complexity_usei08.pdf)
* **USEI09: k-Nearest Neighbor Search** 📍
    * *Objective: Find the **N nearest stations** to a point, using **KD-Tree and Max-Heap** for $O(\log n)$ average time.*
    * [📜 Requirements](src/doc/ESINF/USEI09/01.requirements-engineering/USEI09-requirements.md)
    * [📊 Analysis](src/doc/ESINF/USEI09/02.analysis/USEI09-analysis.md)
    * [🛠️ Design](src/doc/ESINF/USEI09/03.design/USEI09-design.md)
    *  [📊  Análise de Complexidade](src/doc/ESINF/USEI09/Analise_Complexidade_USEI09.pdf)
* **USEI10: Radius Search & Density Summary** 🌐
    * *Objective: Find stations within a specific radius and generate a statistical summary.*
    * [📜 Requirements](src/doc/ESINF/USEI10/01.requirements-engineering/USEI10-requirements.md)
    * [📊 Analysis](src/doc/ESINF/USEI10/02.analysis/USEI10-analysis.md)
    * [🛠️ Design](src/doc/ESINF/USEI10/03.design/USEI10-design.md)
    *  [📊  Análise de Complexidade](src/doc/ESINF/USEI10/AnaliseComplexidadeUSEI10.pdf)

---

### 💾 BDDAD (Data Population and Analytical Queries - DML/DQL)

Focus on populating the database and executing complex analytical queries.

* **USBD03: Railway Model Table Creation (DDL)** 📑
    * [📄 SQL Script](src/doc/BDDAD/USBD03/USBD03_Create_Tables/railway_model.sql)
* **USBD04: Data Insertion (DML)** 📥
    * [📄 SQL Script](src/doc/BDDAD/USBD04/data_insertion/railway_system_data.sql)
* **USBD07: Query: Segments by Owner**
    * [📄 SQL Script](src/doc/BDDAD/USBD07/segments_by_owner.sql)
* **USBD12: Query: Wagons by Type, Gauge, and Operator**
    * [📄 SQL Script](src/doc/BDDAD/USBD12/wagons_by_type_gauge_operator.sql)
* **USBD18: Query: Segments by Track Type**
    * [📄 SQL Script](src/doc/BDDAD/USBD18/segments_by_track_type.sql)
* **USBD26: Query: Wagon Utilization**
    * [📄 SQL Script](src/doc/BDDAD/USBD26/wagon_usage.sql)
* **USBD27: Query: Grain Wagons Usage**
    * [📄 SQL Script](src/doc/BDDAD/USBD27/grain_wagons_usage.sql)
* **USBD28: Query: Locomotives with Multiple Gauges**
    * [📄 SQL Script](src/doc/BDDAD/USBD28/locomotives_with_multiple_gauges.sql)

---

### 🚆 LAPR3 (Domain Evolution & Scheduling)

Implementation of the conceptual model updates and the core scheduling functionality.

* **USLP05: Domain Model Update** 🏗️
    * *Objective: Update the conceptual domain model according to the changed requirements (Graph Integration).*
    * [🏗️ Domain Model Update (PlantUML)](src/doc/LAPR3/USLP05/domainmodel.svg)
* **USLP06: Data Dictionary/Glossary Update** 📖
    * *Objective: Update the data dictionary/glossary according to the changed requirements.*
    * [📖 Glossary Update](src/doc/LAPR3/USLP06/glossary.md)
* **USLP07: Train Dispatch Scheduler** ⏱️
    * *Objective: Implement a scheduler that allows dispatching a list of trains (simple or complex routes) with a planned departure date/time.*
    * [📜 Requirements](src/doc/LAPR3/USLP07/01.requirements-engineering/USLP07-requirements.md)
    * [📊 Analysis](src/doc/LAPR3/USLP07/02.analysis/USLP07-analysis.md)
    * [🛠️ Design](src/doc/LAPR3/USLP07/03.design/USLP07-design.md)

---

### 🖥️ ARQCP (C and Assembly Integrations)

Low-level development focusing on performance optimization and interaction with external devices/stations (C/Assembly).

* **USAC01: [Function Description TBD]**
    * [📄 C Source](src/doc/ARQCP/USAC01/main.c)
    * [⚙️ Assembly Source](src/doc/ARQCP/USAC01/asm.s)
* **USAC02: [Function Description TBD]**
    * [📄 C Source](src/doc/ARQCP/USAC02/main.c)
    * [⚙️ Assembly Source](src/doc/ARQCP/USAC02/asm.s)
* **USAC07: [Function Description TBD]**
    * [📄 C Source](src/doc/ARQCP/USAC07/main.c)
    * [⚙️ Assembly Source](src/doc/ARQCP/USAC07/asm.s)
