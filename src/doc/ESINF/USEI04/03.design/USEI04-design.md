# US004 - Create a Map

## 3. Design

### 3.1. Rationale

This table outlines the distribution of responsibilities among the different software classes involved in this use case, based on GRASP patterns.

| Interaction ID | Question: Which class is responsible for... | Answer                  | Justification (with patterns)                                                                                                                                                              |
| :------------- | :------------------------------------------ | :---------------------- | :----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Step 1** | ... interacting with the actor (Picker)?    | `GetPickingPathUI`        | **Pure Fabrication**: This class handles all UI interactions, separating presentation from business logic.                                                                                  |
|                | ... coordinating the path sequencing request? | `GetPickingPathController` | **Controller**: It acts as the intermediary, receiving UI events and delegating tasks to the appropriate backend services.                                                                 |
|                | ... instantiating the sequencing process?   | `GetPickingPathController` | **Creator (Rule 1)**: As the entry point for the use case, the controller is the logical place to initiate the workflow.                                                                     |
| **Step 2** | ... managing the input data (the picking plan)? | `GetPickingPathUI`        | **Information Expert (IE)**: The UI is the first component to "know" the user's input before sending it to the controller.                                                               |
|                | ... validating the picking plan input?      | `GetPickingPathController` | **Low Coupling**: The controller performs initial validation (e.g., checking for an empty plan) to prevent invalid data from entering the domain layer.                                    |
| **Step 3** | ... merging duplicate bay locations from the plan? | `PickingPlan`             | **Information Expert (IE)**: The `PickingPlan` domain object contains the list of bays and is the expert on its own data.                                                                |
|                | ... calculating the path for Strategy A & B? | `PathSequencingService`   | **High Cohesion / Pure Fabrication**: A dedicated service encapsulates the complex sequencing algorithms. This keeps the logic cohesive and separates it from the controller and domain objects. |
| **Step 4** | ... creating the `PickingPath` result object? | `PathSequencingService`   | **Creator (Rule 2)**: This service performs the calculations and has all the necessary information (the final sequence, total distance) to properly instantiate the `PickingPath` object.     |
| **Step 5** | ... persisting the generated picking path?  | `PickingPathRepository`   | **Information Expert (IE)**: This class encapsulates all logic related to the storage and retrieval of `PickingPath` instances, abstracting the persistence mechanism.                      |
| **Step 6** | ... notifying the Picker with the final paths? | `GetPickingPathUI`        | **Information Expert (IE)**: The UI is responsible for presenting all information and results back to the actor.                                                                        |

---

### 3.2. Systematization

According to the rationale, the conceptual classes from the domain model that are promoted to software classes are:

* **PickingPlan**
* **Bay**
* **PickingPath**
* **Picker**

Other software classes (i.e., Pure Fabrication, Controllers, etc.) identified for this use case are:

* `GetPickingPathUI`
* `GetPickingPathController`
* `PathSequencingService`
* **Repositories**
    * `PickingPlanRepository`
    * `PickingPathRepository`
* `UserSession`

## 3.2. Sequence Diagram (SD)

### Full Diagram

This diagram shows the full sequence of interactions between the classes involved in the realization of this user story.

![Sequence Diagram - Full](svg/USEI04-SD-full.svg)

### Split Diagrams

The following diagram shows the same sequence of interactions between the classes involved in the realization of this user story, but it is split in partial diagrams to better illustrate the interactions between the classes.

It uses Interaction Occurrence (a.k.a. Interaction Use).


## 3.3. Class Diagram (CD)

![Class Diagram](svg/USEI04-CD.svg)
