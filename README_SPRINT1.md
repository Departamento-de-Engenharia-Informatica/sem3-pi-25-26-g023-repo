# 📦 Project Documentation Hub - SPRINT 1 (Logística & Modelagem Inicial)

Bem-vindo ao repositório central para o desenvolvimento do **Sprint 1**, focado na **gestão básica do armazém**, **modelo de dados inicial (DDL)** e **lógica fundamental da rede ferroviária**.

---

## 📚 Documentação por Unidade Curricular

---

### 💻 ESINF (Gestão de Armazém e Logística Básica)

Documentação detalhada para as funcionalidades de gestão do armazém:

* **USEI01: Wagons Unloading (Inventory Replenishment)** 📦
    * [📜 Requirements](src/doc/ESINF/USEI01/01.requirements-engineering/USEI01-requirements.md)
    * [📊 Analysis](src/doc/ESINF/USEI01/02.analysis/USEI01-analysis.md)
    * [🛠️ Design](src/doc/ESINF/USEI01/03.design/USEI01-design.md)
* **USEI02: Order Fulfillment Allocation** ✅
    * [📜 Requirements](src/doc/ESINF/USEI02/01.requirements-engineering/USEI02-requirements.md)
    * *(Analysis & Design documentation links TBD)*
* **USEI03: Pack Allocation Rows into Trolleys** 🛒
    * [📜 Requirements](src/doc/ESINF/USEI03/01.requirements-engineering/USEI03-requirements.md)
    * [📊 Analysis](src/doc/ESINF/USEI03/02.analysis/USEI03-analysis.md)
    * [🛠️ Design](src/doc/ESINF/USEI03/03.design/USEI03-design.md)
* **USEI04: Pick Path Sequencing** 🗺️
    * [📜 Requirements](src/doc/ESINF/USEI04/01.requirements-engineering/US004-requirements.md)
    * [📊 Analysis](src/doc/ESINF/USEI04/02.analysis/USEI04-analysis.md)
    * [🛠️ Design](src/doc/ESINF/USEI04/03.design/USEI04-design.md)
* **USEI05: Returns & Quarantine** ↩️
    * [📜 Requirements](src/doc/ESINF/USEI05/01.requirements-engineering/USEI05-requirements.md)
    * [📊 Analysis](src/doc/ESINF/USEI05/02.analysis/USEI05-analysis.md)
    * [🛠️ Design](src/doc/ESINF/USEI05/03.design/USEI05-design.md)

---

### 💾 BDDAD (Modelagem de Dados Inicial - DDL)

Foco na criação do modelo de dados inicial do sistema.

* **USBD01: Dicionário de Dados**
    * [📖 Glossary / Data Dictionary](src/doc/BDDAD/USBD01/Glossary.md)
* **USBD02: Implementação do Modelo de Dados**
    * [📄 SQL Script](src/doc/BDDAD/USBD02/US2BDDAD.sql)

---

### 🚆 LAPR3 (Interface e Viagem Inicial)

Lógica inicial de rede e interface:

* **USLP01: Core Domain Model**
    * [🏗️ Domain Model (PlantUML)](src/doc/LAPR3/USLP01/domain model.puml)
* **USLP02: Console UI** 🖥️
    * *Implementada como o menu principal da aplicação.*
    * [Main UI Class](src/main/java/pt/ipp/isep/dei/UI/CargoHandlingUI.java)
* **USLP03: Calculate Travel Time** ⏱️
    * [📄 README / Documentation](src/doc/LAPR3/USLP03/readMeUS03.md)
    * [Controller](src/main/java/pt/ipp/isep/dei/controller/TravelTimeController.java)
