# USEI09 - Proximity Search

## 1. Requirements Engineering

### 1.1. User Story Description

As an analyst, I want to find the nearest N stations to a given(lat, lon) using the 2D-tree, with optional filters, so I can get accurate nearby results efficiently.

### 1.3. Acceptance Criteria

* **AC1:** Use Haversine distance (km) with Earth radius between two (lat,lon) points.
* **AC2:** The search should enable optional filtering based on the time zone criteria.

### 1.4. Found out Dependencies

* USEI07- KD-Tree is required to obtain nearest N.
* USEI08- It shares the traversal/pruning algorithm and optional filters.

### 1.5. Input and Output Data

**Input Data:**

* targetLat
* targetLon
* N(number of stations)
* timezone

**Output Data:**

* nearestStations
* stationId
* name
* lat
* lon
* timezone
* distanceKm

### 1.6. System Sequence Diagram (SSD)

![System Sequence Diagram](svg/USEI08-SSD.svg)
