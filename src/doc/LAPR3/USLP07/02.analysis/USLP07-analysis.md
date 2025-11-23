# 🚂 USLP07 - Freight Dispatching and Conflict Resolution

## 2. Analysis

### 2.1. Relevant Domain Model Excerpt
![Domain Model](svg/USLP07-DM.svg)

The analysis focuses on the interaction between the scheduled journey (`TrainTrip`), the path segments (`LineSegment`), and the conflict resolution mechanism.



***

### 2.2. Relevant Entities and Properties

The scheduling process fundamentally relies on these entity properties:

* **TrainTrip**: Represents the specific journey instance and holds dynamic state.
    * **Input**: `DepartureTime`, `Route` (list of `LineSegment` IDs), `Locomotives`, `Wagons`.
    * **Output**: Stores results like `MaxTrainSpeed`, `TotalTravelTimeHours`, and the detailed list of **`SimulationSegmentEntry`** (which contains `EntryTime` and `ExitTime`).
* **Locomotive & Wagon**: Used to determine the **dynamic performance** of the train.
    * **Locomotive**: Provides `PowerKW`.
    * **Wagon**: Determines **total weight** ($W_{total} = \sum (\text{tare} + \text{load})$).
* **LineSegment**: Defines the physical constraints of the network.
    * **Key Properties**: `length_km`, `MaxSpeedAllowedKmh`, and **`NumberTracks`** (critical for single-track detection).
    * **Identification**: Must be identifiable as the *same physical track* regardless of direction (e.g., segment ID normalization by removing the `INV_` prefix).
* **Conflict**: DTO storing the result of a conflict resolution: `delayMinutes`, and `safeWaitFacilityId`.

### 2.3. Operational Remarks and Conflict Logic

* **Sorting Priority**: All train trips are processed by their **planned departure time** (ascending order). The earlier train has priority in conflict resolution.
* **Effective Speed Calculation**: The effective speed for any segment is constrained by the most limiting factor: $V_{\text{effective}} = \min (V_{\text{line\_limit}}, V_{\text{max\_train}})$.
* **Conflict Condition**: A conflict occurs when two trains occupy a **single-track segment** simultaneously. Detection is based on the comparison of precise times: $\text{TripA}.ExitTime > \text{TripB}.EntryTime$.
* **Conflict Resolution**: The system imposes the **minimum required delay** on the subsequent train (`Trip B`). The delayed train is held at the **last safe waiting point** (a facility with $\text{NumberTracks} > 1$) before the conflict segment, and its subsequent schedule is entirely recalculated.