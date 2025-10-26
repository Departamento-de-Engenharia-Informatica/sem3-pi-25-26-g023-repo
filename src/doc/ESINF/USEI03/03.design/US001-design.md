# US001 - Create a Map

## 3. Design

### 3.1. Rationale

| Interaction ID      | Question: Which class is responsible for...  | Answer         | Justification (with patterns)                                      |
|:--------------------|:---------------------------------------------|:---------------|:-------------------------------------------------------------------|
| Step 1              | ... interacting with the actor (Editor)?     | CreateMapUI    | Pure Fabrication: Handles UI interactions without domain coupling. |
|                     | ... coordinating map creation?               | MapController  | Controller: Mediates between UI and domain logic.                  |
|                     | ... instantiating the creation process?      | MapController  | Creator (Rule 1): Triggers the workflow (create()).                |
| Step 2              | ... managing form data (name/width/height)?  | Editor         | IE: Owns user input until submission.                              |
|                     | ... requesting map data (name/width/height)? | CreateMapUI    | IE: responsible for user interactions.                             |
|                     | ... validating map dimensions/name?          | MapController  | Low Coupling: Centralizes validation logic.                        |
| Step 3              | ... instantiating a new Map?                 | MapRepository  | Creator: in the DM MapRepository manages Maps.                     |
| Step 4              | ... creating the Map domain object?          | Map            | Creator (Rule 2): Initializes its own state (createNewMap()).      |
| Step 5              | ... persisting the new map?                  | MapRepository  | IE: Manages storage/retrieval of maps.                             |
| Step 6              | ... validating all data (global validation)? | MapController  | IE: knows all its data (name, dimensions)                          | 
|                     | ... saving the created Map?                  | MapRepository  | IE: responsible for managing the persistence of Map instances.     | 
| Step 7              | ... notifying the Editor of success?         | CreateMapUI    | IE: is responsible for user interactions.                          | 

### Systematization ##

According to the taken rationale, the conceptual classes promoted to software classes are: 

* Organization
* Task
* TaskCategory
* Employee

Other software classes (i.e. Pure Fabrication) identified: 

* CreateTaskUI  
* CreateTaskController
* Repositories
* TaskCategoryRepository
* OrganizationRepository
* ApplicationSession
* UserSession


## 3.2. Sequence Diagram (SD)

### Full Diagram

This diagram shows the full sequence of interactions between the classes involved in the realization of this user story.

![Sequence Diagram - Full](svg/US001-SD-full.svg)

### Split Diagrams

The following diagram shows the same sequence of interactions between the classes involved in the realization of this user story, but it is split in partial diagrams to better illustrate the interactions between the classes.

It uses Interaction Occurrence (a.k.a. Interaction Use).


## 3.3. Class Diagram (CD)

![Class Diagram](svg/US001-CD.svg)