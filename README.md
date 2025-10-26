#  Integrated Project - Semester 3 - 2025/2026 - G023 REPO 

Welcome to the central repository for the **Sprint 1** development of the integrated project, encompassing the **ESINF**, **LAPR3**, and **BDDAD** curricular units.

---

## 📚 Project Documentation Hub

Navigate through the documentation for each User Story developed during Sprint 1.

---

### 💻 ESINF 

Detailed documentation for warehouse management functionalities:

* **USEI01: Wagons Unloading (Inventory Replenishment)** 📦
    * [📜 Requirements](src/doc/ESINF/USEI01/01.requirements-engineering/USEI01-requirements.md)
    * [📊 Analysis](src/doc/ESINF/USEI01/02.analysis/USEI01-analysis.md)
    * [🛠️ Design](src/doc/ESINF/USEI01/03.design/USEI01-design.md)

* **USEI02: Order Fulfillment Allocation** ✅
    * [📜 Requirements](src/doc/ESINF/USEI02/01.requirements-engineering/USEI02-requirements.md)
    * *(Analysis & Design documentation links TBD)*

* **USEI03: Pack Allocation Rows into Trolleys** 🛒
    * [📜 Requirements](src/doc/ESINF/USEI03/01.requirements-engineering/USEI03-requirements.md)
    * [📊 Analysis](src/doc/ESINF/USEI03/02.analysis/US001-analysis.md) * [🛠️ Design](src/doc/ESINF/USEI03/03.design/US001-design.md)
    *

* **USEI04: Pick Path Sequencing** 🗺️
    * [📜 Requirements](src/doc/ESINF/USEI04/01.requirements-engineering/US004-requirements.md) * [📊 Analysis](src/doc/ESINF/USEI04/02.analysis/USEI04-analysis.md)
    * [🛠️ Design](src/doc/ESINF/USEI04/03.design/USEI04-design.md)

* **USEI05: Returns & Quarantine** ↩️
    * [📜 Requirements](src/doc/ESINF/USEI05/01.requirements-engineering/USEI05-requirements.md)
    * [📊 Analysis](src/doc/ESINF/USEI05/02.analysis/USEI05-analysis.md)
    * [🛠️ Design](src/doc/ESINF/USEI05/03.design/USEI05-design.md)

---

### 💾 BDDAD 

Database structure and related artifacts:

* **USBD01:**
    * [📖 Glossary / Data Dictionary](src/doc/BDDAD/USBD01/Glossary.md)
* **USBD02:**
    * [📄 SQL Script](src/doc/BDDAD/USBD02/US2BDDAD.sql)

---

### 🚆 LAPR3 

Application development and railway network logic:

* **USLP01:**
    * [🏗️ Domain Model (PlantUML)](src/doc/LAPR3/USLP01/domain%20model.puml)

* **USLP02:** *Console UI* 🖥️
    * This User Story is implemented as the main **console application menu**, providing access to various functionalities.
    * [Main UI Class](src/main/java/pt/ipp/isep/dei/UI/CargoHandlingUI.java)

* **USLP03:** *Calculate Travel Time* ⏱️
    * [📄 README / Documentation](src/doc/LAPR3/USLP03/readMeUS03LP.md)
    * Source code:
        * [Controller](src/main/java/pt/ipp/isep/dei/controller/TravelTimeController.java)
        * [UI](src/main/java/pt/ipp/isep/dei/UI/TravelTimeUI.java)
        * [Service](src/main/java/pt/ipp/isep/dei/domain/RailwayNetworkService.java)

---
