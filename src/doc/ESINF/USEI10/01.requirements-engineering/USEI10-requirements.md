# USEI10 - Radius search and density summary

## 1. Requirements Engineering

### 1.1. User Story Description

As an operations planner, I want to fetch all stations within a radius R (km) of a target (lat, lon) using the 2D-tree, and get a summary by country and by isCity, so I can assess local coverage.

### 1.2. Customer Specifications and Clarifications

**From the specifications document (sem3_pi_2025_26_en.pdf):**

* **Context:** Operational questions often ask, "what stations lie within R km of a point?" and "how are they distributed by country and is City?". The system must provide both the list of stations and a quick summary.
* **Dataset:** The system will use a dataset of approximately 64,000 European railway stations. This data includes `station`, `latitude`, `longitude`, `country`, and flags like `isCity`.
* **Data Structures:** The solution must be built using BST/AVL implementations (provided in classes) and a 2D-tree (built from scratch) for geographic queries.

### 1.3. Acceptance Criteria

* **AC1:** Must use **Haversine distance (km)** with Earth's radius for calculations between (lat, lon) points.
* **AC2:** The search must be performed using the 2D-tree spatial index.
* **AC3:** The primary return value must be a **BST/AVL tree**.
* **AC4:** This result tree must be sorted by **distance (ASC)**, and as a tie-breaker, by **station name (DESC)**.
* **AC5:** The implementation must also return a **summary** of the found stations, categorized by `country` and by `isCity`.
* **AC6:** A **temporal analysis complexity** report must be provided.

### 1.4. Found out Dependencies

* Depends on the availability of the European railway stations dataset (approx. 64k entries).
* Depends on the BST/AVL implementations "already made in classes".
* Depends on the successful implementation of the balanced 2D-tree from **USEI07**.

### 1.5. Input and Output Data

**Input Data:**

* A target coordinate (latitude, longitude).
* A search radius R (in km).
* The pre-built 2D-tree (from USEI07).

**Output Data:**

* A BST/AVL tree containing the stations found within the radius.
    * *Sorting:* By distance (ASC), then station name (DESC).
* A density summary report (e.g., a Map or other structure) detailing station counts by country and by the `isCity` flag.
* A temporal complexity analysis.

### 1.6. System Sequence Diagram (SSD)

![System Sequence Diagram](svg/USEI10-SSD.svg)

### 1.7. Other Relevant Remarks

* This User Story is part of Sprint 2.
* The use of Haversine distance is mandatory.
* The sorting order for the output tree (distance ASC, name DESC) is strictly defined.