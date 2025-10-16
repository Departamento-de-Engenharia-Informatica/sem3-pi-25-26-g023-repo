# 1. US 04 Requirements Engineering

## 1.1. User Story Description

**USEI04 — Pick Path Sequencing**

As a picker, I want to plan the task of picking up several boxes, sequenced using a 2D layout (aisles & bays), so I can minimize the walking distance starting from the entrance at the main corridor, with no return leg.

## 1.2. Business Rules and System Specifications

This section details the essential rules and formulas for the implementation of the User Story, extracted from the provided context.

#### Warehouse Geometry and Distances
- The warehouse entrance is at coordinate `(0, 0)`.
- A bay location is a coordinate pair `(aisle, bay)`.
- The distance `D` between two coordinates `c1 = (a1, b1)` and `c2 = (a2, b2)` is calculated as follows:
    - If they are in the **same aisle** (`a1 == a2`): `D = |b1 - b2|`
    - If they are in **different aisles** (`a1 != a2`): `D = b1 + |a1 - a2| * 3 + b2` (implies moving down bay `b1`, moving between aisles, and moving up bay `b2`).

#### Sequencing Strategies
The system must implement two strategies to determine the picking order:

1.  **Strategy A — Deterministic Sweep:**
    - The bays to be visited are sorted in ascending order by the aisle number.
    - The total path is calculated following this fixed order.

2.  **Strategy B — Nearest-Neighbour:**
    - From the current point (starting at `(0,0)`), the system calculates the distance to all unvisited bays.
    - The next bay to be visited is the one with the shortest distance (`D`) from the current point.
    - This process is repeated until all bays have been visited.

#### Total Route Distance Formula
The total distance (`D_Total`) for a route with `n` bays `c1, c2, ..., cn`, starting at `c0 = (0,0)`, is the sum of the distances between consecutive points:
$$
D_{Total} = D(c_0, c_1) + D(c_1, c_2) + ... + D(c_{n-1}, c_n) = \sum_{i=0}^{n-1} D(c_i, c_{i+1})
$$

## 1.3. Acceptance Criteria

**AC1: Merge Duplicate Bays:** The system must process the input list of bays and merge any duplicate locations. If the same bay `(aisle, bay)` appears multiple times in the picking plan, it must be treated as a single stop on the route.

**AC2: Mandatory Calculation for Both Strategies:** For a given picking plan, the system must perform the sequencing using both Strategy A (Deterministic Sweep) and Strategy B (Nearest-Neighbour).

**AC3: Output Format:** For each strategy, the output must include:
   a) The final path, which is the ordered sequence of bay coordinates to visit, always starting from `(0,0)`.
   b) The total calculated distance for that path.

**AC4: Fixed Starting Point:** All route calculations must start from the warehouse entrance point at coordinate `(0,0)`.

## 1.4. Found out Dependencies

This User Story has a direct dependency on **USEI03 — Picking Plan Generation**. The input for sequencing (the set of bays to visit
