# USLP03 - Travel Time Calculation & Database Integration

## User Story Logic (USLP03)

This feature allows a Traffic Dispatcher to calculate the estimated fastest travel time between two railway stations. The core logic involves user interaction, data fetching from the database, shortest path calculation, and result presentation.

### 1. User Interaction Flow (`TravelTimeUI`)

The process begins in the `TravelTimeUI` class:

1.  **Display Departure Stations:** Fetch all relevant stations from the database using `EstacaoRepository.findAll()` and display them to the user.
2.  **Get Departure Input:** Prompt the user to select the ID of the departure station.
3.  **Display Connected Destinations:** Call `TravelTimeController.getDirectlyConnectedStations()` to get only the stations directly linked to the chosen departure station. Display this filtered list.
4.  **Get Arrival Input:** Prompt the user to select the ID of the arrival station *from the filtered list*, ensuring it's a valid direct connection and different from the departure.
5.  **Display Locomotives:** Fetch all locomotives from the database using `LocomotivaRepository.findAll()` and display them.
6.  **Get Locomotive Input:** Prompt the user to select the ID of the locomotive.
7.  **Trigger Calculation:** Call `TravelTimeController.calculateTravelTime()` with the selected IDs.
8.  **Display Result:** Show the formatted report string returned by the controller.

### 2. Coordination and Calculation (`TravelTimeController` & `RailwayNetworkService`)

The `TravelTimeController` orchestrates the process:

1.  **`getDirectlyConnectedStations`:** Queries the `SegmentoLinhaRepository` (which reads `RAILWAY_LINE`) to find adjacent stations for the UI filtering step.
2.  **`calculateTravelTime`:**
    * Validates that the provided station and locomotive IDs exist (using `EstacaoRepository` and `LocomotivaRepository`).
    * Calls `RailwayNetworkService.findFastestPath()` to perform the core calculation.
    * Calls `formatPathResult()` to prepare the output string.

The `RailwayNetworkService` contains the shortest path logic:

* **Graph Representation:** It treats the railway network as a graph:
    * **Nodes:** Stations (`Estacao` objects, sourced from `FACILITY` table).
    * **Edges:** Direct connections between stations (`SegmentoLinha` objects, representing bidirectional links derived from `RAILWAY_LINE`).
* **Dijkstra's Algorithm:** It applies Dijkstra's algorithm to find the path with the minimum cumulative weight from the departure to the arrival node.
* **Edge Weight Calculation:** The weight used by Dijkstra is the **estimated travel time** for each segment:
    ```
    Time = Distance / Speed
    ```
    * `Distance`: Obtained from `SegmentoLinha.getComprimento()` (which is the aggregated length in km from `LINE_SEGMENT` table for the corresponding `RAILWAY_LINE`).
    * `Speed`: A **default maximum speed** (`DEFAULT_MAX_SPEED`) defined in `SegmentoLinhaRepository` is used, as this data is not available per line/segment in the current database schema.
* **Result:** The algorithm returns a `RailwayPath` object containing the list of `SegmentoLinha` in the fastest sequence, the total calculated distance, and the total calculated time.
