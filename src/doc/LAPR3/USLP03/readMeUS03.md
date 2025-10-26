# USLP03 - Travel Time Calculation & Database Integration

## User Story Logic (USLP03)

This feature allows a Traffic Dispatcher to calculate the estimated fastest travel time between two railway stations, considering the specific locomotive selected. The core logic involves user interaction, data fetching from the database, shortest path calculation using Dijkstra's algorithm, and result presentation.

### 1. User Interaction Flow (`TravelTimeUI`)

The process begins in the `TravelTimeUI` class:

1.  **Display Departure Stations:** Fetch all relevant stations from the database using `StationRepository.findAll()` and display them to the user.
2.  **Get Departure Input:** Prompt the user to select the ID of the departure station.
3.  **Display Connected Destinations:** Call `TravelTimeController.getDirectlyConnectedStations()` to get only the stations directly linked to the chosen departure station. Display this filtered list.
4.  **Get Arrival Input:** Prompt the user to select the ID of the arrival station *from the filtered list*, ensuring it's a valid direct connection and different from the departure.
5.  **Display Locomotives:** Fetch all locomotives (including their max speed) from the database using `LocomotiveRepository.findAll()` and display them.
6.  **Get Locomotive Input:** Prompt the user to select the ID of the locomotive.
7.  **Trigger Calculation:** Call `TravelTimeController.calculateTravelTime()` with the selected IDs.
8.  **Display Result:** Show the formatted report string returned by the controller, which includes segment details, effective speeds, segment times, and cumulative time.

### 2. Coordination and Calculation (`TravelTimeController` & `RailwayNetworkService`)

The `TravelTimeController` orchestrates the process:

1.  **`getDirectlyConnectedStations`:** Queries the `SegmentLineRepository` to find adjacent stations for the UI filtering step.
2.  **`calculateTravelTime`:**
    * Validates that the provided station and locomotive IDs exist (using `StationRepository` and `LocomotiveRepository`).
    * Retrieves the selected `Locomotive` object (which now includes its `maxSpeed`).
    * Calls `RailwayNetworkService.findFastestPath()`, passing the departure ID, arrival ID, and the selected `Locomotive` object, to perform the core calculation.
    * Calls `formatPathResult()` to prepare the output string.

The `RailwayNetworkService` contains the shortest path logic:

* **Graph Representation:** It treats the railway network as a graph:
    * **Nodes:** Stations (`Station` objects).
    * **Edges:** Direct connections between stations (`LineSegment` objects, representing bidirectional links).
* **Dijkstra's Algorithm:** It applies Dijkstra's algorithm to find the path with the minimum cumulative weight (travel time) from the departure to the arrival node.
* **Edge Weight Calculation:** The weight used by Dijkstra is the **estimated travel time** for each segment, calculated considering both the infrastructure and the locomotive limits:
    ```
    // 1. Determine the maximum possible speed on the segment with the given locomotive
    effectiveSpeed = Math.min(segment.getVelocidadeMaxima(), locomotive.getMaxSpeed());

    // 2. Calculate time using the effective speed
    Time (hours) = segment.getComprimento() / effectiveSpeed;
    ```
    * `Distance`: Obtained from `LineSegment.getComprimento()` (length in km).
    * `Speed`: The `effectiveSpeed` is the **lower** of the segment's maximum speed (`segment.getVelocidadeMaxima()`) and the locomotive's maximum speed (`locomotive.getMaxSpeed()`). This ensures neither limit is exceeded.
* **Result:** The algorithm returns a `RailwayPath` object containing the list of `LineSegment` objects in the fastest sequence, the total calculated distance, and the total calculated time (in hours).