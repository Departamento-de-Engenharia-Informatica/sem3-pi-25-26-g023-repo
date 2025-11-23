# 🚂 USLP07 - Freight Dispatching and Conflict Resolution

## 3. Design

### 3.1. Rationale (GRASP Patterns)

The design allocates responsibilities for dynamic simulation, performance calculation, and conflict detection/resolution, ensuring **high cohesion** and **low coupling**.

| Interaction ID | Question: Which class is responsible for... | Answer | Justification (GRASP Pattern) |
|:---|:---|:---|:---|
| Step 1 | ... interacting with the actor (Freight Manager)? | TrainSimulationUI | **Pure Fabrication**: Handles UI/console input and output for the simulation. |
| | ... coordinating the entire dispatch process? | DispatcherService | **Controller**: Mediates between the UI and the core domain logic (`SchedulerService`). |
| | ... instantiating the scheduler workflow? | DispatcherService | **Creator (Rule 1)**: Initiates the complex workflow by calling `SchedulerService.dispatchTrains()`. |
| Step 2 | ... managing the core conflict resolution logic? | SchedulerService | **Pure Fabrication / High Cohesion**: Centralizes all non-domain logic (sorting, simulation math, conflict detection rules). |
| | ... calculating the train's dynamic performance (Vmax)? | SchedulerService | **Pure Fabrication**: Uses data from `Locomotive` and `Wagon` to perform specialized calculations. |
| Step 3 | ... accessing the route and segment properties? | TrainTrip | **Information Expert (IE)**: Owns the `Route` collection (`LineSegment` list) and knows the current state. |
| | ... storing entry/exit times for each segment? | TrainTrip | **Information Expert (IE)**: Acts as a container for its own simulation results (`SimulationSegmentEntry`). |
| Step 4 | ... identifying the physical track ID (e.g., handling INV_)? | SegmentLineRepository | **Information Expert (IE)**: Knows the network structure and how direct/inverse segments map to a physical track. |
| Step 5 | ... storing the final result (scheduled trips, conflicts)? | SchedulerResult | **Pure Fabrication**: A container DTO to bundle the output of the scheduling run. |
| Step 6 | ... presenting the final schedule to the actor? | TrainSimulationUI | **Information Expert (IE)**: Responsible for displaying the updated schedule and conflict list. |

---

### 3.2. Systematization

Conceptual classes promoted to software classes:

* **TrainTrip** (Domain Expert/State Container)
* **LineSegment** (Domain Expert/Constraint Data)
* **Locomotive, Wagon** (Domain Experts/Performance Data)
* **Conflict, SimulationSegmentEntry, SchedulerResult** (DTOs)

Other software classes (i.e., Pure Fabrication) identified:

* **SchedulerService** (Core Algorithm/Math)
* **DispatcherService** (System Controller/Dependency Management)
* **TrainSimulationUI** (Presentation)
* **SegmentLineRepository** (Persistence/Network Structure IE)

---

### 3.3. Sequence Diagram (SD)

#### Full Diagram (Conflict Resolution Flow)

This diagram illustrates the process of dispatching trains, calculating times, and resolving a conflict by recalculating the route of the delayed train.



#### Interaction Highlights (Example Flow for Conflict)

1.  **Actor Interaction:** `Freight Manager` initiates `dispatchTrains(trips)` via `TrainSimulationUI`.
2.  **Coordination:** `DispatcherService` calls `SchedulerService.dispatchTrains(trips)`.
3.  **Simulation Loop:** `SchedulerService` iterates over sorted trips (Trip A, Trip B).
4.  **Performance Calculation:** `SchedulerService` calculates $V_{max\_train}$ using data from `Locomotive` and `Wagon`.
5.  **Time Calculation:** `SchedulerService` runs Trip A and Trip B through segment time calculation, populating their `SimulationSegmentEntry` lists.
6.  **Conflict Check:** `SchedulerService` compares `TripA.ExitTime` with `TripB.EntryTime` on the same physical single-track segment.
7.  **Resolution:** If conflict, `SchedulerService` creates a `Conflict` object with the calculated `delayMinutes` and `safeWaitFacilityId`.
8.  **Recalculation:** `SchedulerService` creates a **new** delayed `TrainTrip` for Trip B and **re-runs** the simulation for this delayed trip.
9.  **Feedback:** `DispatcherService` receives the `SchedulerResult` and returns the final data to the `TrainSimulationUI`.

---

### 3.4. Class Diagram (CD)

This class diagram shows the relationships crucial for performance calculation and conflict detection/storage:



**Highlights:**

* **Simulation Core:** The central relationship is the **DispatcherService** orchestrating the **SchedulerService**, which modifies the dynamic state of the **TrainTrip** (adding `SimulationSegmentEntry` and generating `Conflict` data).
* **Performance Dependency:** `TrainTrip` relies on `Locomotive` and `Wagon` data (Power/Weight) to execute the simulation logic within the `SchedulerService`.