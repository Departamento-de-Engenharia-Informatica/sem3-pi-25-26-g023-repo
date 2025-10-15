# US001 - Pack the allocation rows into trolleys

## 1. Requirements Engineering

### 1.1. User Story Description

As a planner, I want to pack the allocation rows produced by USEI02 into capacity-bounded trolleys, choosing one of the packing heuristics. So pickers can complete runs without overloading trolleys.

### 1.2. Customer Specifications and Clarifications 

**From the specifications document:**

>	All those who wish to use the application must be authenticated with a password of seven alphanumeric characters, including three capital letters and two digits.

>	The Map Editor allows you to create rectangular maps and place static elements on the map. Elements can be cities or industries.

**From the client clarifications:**

> **Question:** Are there minimum and maximum values for size?
>
> **Answer:** The number needs to be a positive on; there is no maximum, it's up to the editor to decide.

> **Question:** Is there a predefined list of sizes, or should users be able to input custom dimensions?
>
> **Answer:** Custom dimensions but suggesting predefined sizes could be a good idea.
 
> **Question:** Are there any requirements or restrictions for the map's name (e.g., character limit, allowed/disallowed characters)?
>
> **Answer:** File name like restrictions.

> **Question:** Should map names be unique within the system?
>
> **Answer:** Yes.


### 1.3. Acceptance Criteria

* **AC1:** The maps dimensions are positive integers.
* **AC2:** Map name should be a valid file name.

### 1.4. Found out Dependencies

* There is no dependency.

### 1.5 Input and Output Data

**Input Data:**

* Typed data:
    * maps dimensions
    * maps name
    * editors name
    * editors password
    * editors email

**Output Data:**

* confirmation of the login
* validation of the maps name
* display of the map

### 1.6. System Sequence Diagram (SSD)

![System Sequence Diagram](svg/US001-SSD.svg)

**_Other alternatives might exist._**

### 1.7 Other Relevant Remarks

* The created task stays in a "not published" state in order to distinguish from "published" tasks.