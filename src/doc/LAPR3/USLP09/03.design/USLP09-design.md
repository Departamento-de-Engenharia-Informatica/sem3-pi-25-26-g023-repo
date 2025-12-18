# USLP09 - Assemble and assign a train to a route

## 3. Design

### 3.1. Rationale (GRASP Patterns)

The design allocates responsibilities for assembling the aggregate `Train` object and validating its physical constraints against the `Route`.

| Interaction ID | Question: Which class is responsible for... | Answer | Justification (GRASP Pattern) |
|:---|:---|:---|:---|
| **Step 1** | ... interacting with the actor (Traffic Manager)? | `TrainCompositionUI` | **Pure Fabrication**: Handles user input (selection of rolling stock and route) and output. |
| | ... coordinating the creation request? | `TrainCompositionController` | **Controller**: Mediates between the UI and the application layer. |
| | ... orchestrating the assembly process? | `TrainCompositionService` | **Service**: Encapsulates the complex logic of fetching entities, assembling the train, and triggering validations. |
| **Step 2** | ... retrieving the Rolling Stock (Loco/Wagons)? | `RollingStockRepository` | **Repository**: Abstracts the database access for physical assets. |
| | ... creating the `Train` aggregate? | `TrainFactory` (or Service) | **Creator/Factory**: Handles the complexity of instantiating a valid `Train` object from a list of components. |
| **Step 3** | ... calculating the total mass and length? | `Train` | **Information Expert**: The Train aggregates the wagons and locomotive, so it has the information to sum their attributes. |
| | ... validating if the Locomotive can pull the Wagons? | `Train` | **Information Expert**: Knows the locomotive's `tractiveEffort` and the total `mass` to compare them. |
| | ... knowing the route constraints (max siding length)? | `Route` | **Information Expert**: The Route knows the physical characteristics of the line segments and stations it passes through. |
| **Step 4** | ... validating if the Train fits the Route? | `RouteService` (or `Route`) | **Expert/Service**: Verifies if `train.length <= route.maxSidingLength`. |
| **Step 5** | ... assigning the Train to the Route? | `ScheduledTrip` | **Creator**: A new `ScheduledTrip` object represents the association of a physical Train to a Route at a specific time. |
| **Step 6** | ... persisting the changes? | `TrainRepository` | **Repository**: Saves the new Train composition and the ScheduledTrip to the database. |

---

### 3.2. Systematization

**Conceptual Classes promoted to Software Classes:**
* **Train** (Aggregate Root)
* **Locomotive**
* **Wagon**
* **Route**
* **ScheduledTrip**

**Software Classes (Pure Fabrication / Patterns):**
* **TrainCompositionUI** (Boundary)
* **TrainCompositionController** (Controller)
* **TrainCompositionService** (Application Service)
* **RollingStockRepository** (Persistence)
* **RouteRepository** (Persistence)

---

### 3.3. Sequence Diagram (SD)

#### Full Assembly and Assignment Process

This diagram illustrates the Traffic Manager selecting the rolling stock, the system creating the Train aggregate, validating physical rules (Physics & Length), and assigning it to a Route.

![Sequence Diagram - Full](svg/USLP09-SD.svg)

---

### 3.4. Class Diagram (CD)

This diagram shows the dependencies between the Service layer and the Domain Entities, highlighting the Validation responsibilities.

![Class Diagram](svg/USLP09-CD.svg)