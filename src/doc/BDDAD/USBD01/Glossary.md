# 📖 Data Dictionary (USBD01)
This data dictionary describes all entities and attributes of the railway system in a consolidated table format.
| Term | Definition |
| :--- | :--- |
| **Aisle** | A pathway between rows of bays in a warehouse, providing access for picking. The "main corridor" (Entrance) is at Aisle 0. |
| **Allocation** | The logical process of reserving stock from specific boxes to fulfill order lines, following FEFO/FIFO rules. This happens *before* picking. |
| **Allocation Mode (Strict/Partial)** | A rule for allocation (USEI02). **Strict** mode only allocates if the *entire* line quantity is available (otherwise, it allocates 0). **Partial** mode allocates whatever stock is available. |
| **Audit Log** | An external file that records every inspection action (e.g., Restocked, Discarded) for traceability of returns. |
| **Bay** | A specific storage location (identified by a number) within a warehouse aisle where boxes are physically stored. |
| **BFD (Best Fit Decreasing)** | A packing heuristic (USEI03). Allocations are sorted by weight (largest first) and then placed into the trolley where they fit *most tightly* (leaving the least remaining space). |
| **Box** | The base storage unit in the warehouse, representing a "lot fragment" of a single SKU with a specific quantity and optional expiry date. |
| **Boxcar** | A type of wagon that is enclosed and used for general goods. |
| **Capacity (Trolley)** | The maximum weight (in kg) that a trolley can hold, as defined by the planner in USEI03. |
| **Customer Remorse** | A reason for a return (USEI05). The item is in good condition and can be restocked. |
| **Cycle Count** | A reason for a return (USEI05). An inventory audit found a mismatch. The item is typically restockable. |
| **Damaged** | A reason for a return (USEI05). The item is broken and must be discarded. |
| **Deterministic Sweep (Strategy A)** | A pick path sequencing strategy (USEI04). The picker visits all required aisles in ascending order (Aisle 1, then Aisle 2, etc.). |
| **Discarded** | The status of a returned item that is deemed unsuitable (e.g., damaged, expired) and cannot be put back into inventory. |
| **Distance Function** | The formula used to calculate travel distance in the warehouse (USEI04). `D = b1 + |a1 - a2| * 3 + b2` for different aisles. |
| **Effective Speed** | The speed used for travel time calculation (USLP03). It is the *minimum* of the locomotive's max speed and the line segment's max speed. |
| **Eligibility (Status)** | The status of an order line after an allocation attempt (USEI02). Can be `ELIGIBLE` (fully allocatable), `PARTIAL` (partially allocatable), or `UNDISPATCHABLE` (no stock). |
| **Electrified** | A characteristic of a line segment, indicating if it allows the circulation of electric locomotives. |
| **Entrance (Warehouse)** | The starting point for all pick paths, represented as coordinate (0,0) (Aisle 0, Bay 0). |
| **Expired** | A reason for a return (USEI05). The item has passed its expiry date and must be discarded. |
| **Expiry Date** | The date on which a perishable product is no longer considered usable. Used for FEFO sorting. |
| **FEFO (First-Expired-First-Out)** | An inventory principle for perishable goods, ensuring items with the earliest expiry date are dispatched first. This has higher priority than FIFO. |
| **FF (First Fit)** | A packing heuristic (USEI03). Allocations are placed into the *first* available trolley that has enough capacity, in the order they are received. |
| **FFD (First Fit Decreasing)** | A packing heuristic (USEI03). Allocations are sorted by weight (largest first) and then the First Fit (FF) logic is applied. |
| **FIFO (First-In-First-Out)** | An inventory principle for non-perishable goods, ensuring items that were received first (oldest `receivedAt` date) are dispatched first. |
| **Flatcar** | A type of wagon with a flat, open bed, used for transporting containers or large machinery. |
| **Freight** | A set of wagons that must be transported from an origin station to a destination station. |
| **Freight Yard** | A facility used for loading, unloading, sorting, and assembling trains. |
| **Gauge** | The width between the rails of a railway track (e.g., 1668mm). |
| **Hopper car** | A type of wagon used for bulk commodities like grain or coal, which can be emptied from the bottom. |
| **Infrastructure (Railway)** | The physical components of the railway system, including tracks, stations, and terminals. |
| **Intermodal** | A type of terminal where cargo is transferred between different modes of transport (e.g., train, truck, ship). |
| **LIFO (Last-In-First-Out)** | The principle used for quarantine (USEI05), where the most recently returned items are inspected and processed first. |
| **Line Segment** | A component of a Railway Line connecting two stations. It has specific attributes like gauge, electrification, and speed limit. |
| **Locomotive** | The powered vehicle (diesel or electric) that pulls the freight wagons in a train. |
| **Nearest-Neighbour (Strategy B)** | A pick path sequencing strategy (USEI04). From the current location, the picker moves to the *closest* unvisited bay, repeating this process until all bays are visited. |
| **Operator** | The entity responsible for the railway. Can be an "Infrastructure Owner" (owns the tracks) or a "Train Operator" (owns the rolling stock). |
| **Order** | A customer order for products, which contains one or more Order Lines. It has a priority and a due date used for processing in USEI02. |
| **Order Line** | A single item (SKU) and its requested quantity within a customer Order. |
| **Packing Heuristic** | An algorithm (e.g., FF, FFD, BFD) used in USEI03 to pack allocations (items) into trolleys (bins) based on weight capacity. |
| **Path (Railway)** | The ordered list of stations a train must pass through to travel from its origin to its destination. |
| **Payload** | The maximum carrying capacity (e.g., in kg) of a wagon. |
| **Pick Path** | The optimized sequence of warehouse bays a picker must visit to fulfill a picking plan, calculated in USEI04 to minimize walking distance. |
| **Picker** | A warehouse worker responsible for physically collecting items from bays. |
| **Picking** | The physical process of collecting products from their warehouse locations (bays) to fulfill allocated orders. |
| **Picking Assignment** | A single task for a picker, created from an Allocation. It details what item (SKU), how much (qty), and from where (aisle, bay, boxId) to pick. |
| **Picking Plan** | The output of USEI03; a set of trolleys, each containing the specific picking assignments (allocations) that fit its weight capacity. |
| **Picking Status** | The state of a `PickingAssignment`. Can be `PENDING` (created), `ASSIGNED` (put in a trolley), or `PICKED` (completed). |
| **Planner** | A system user (e.g., Warehouse Planner) responsible for running allocation (USEI02) and packing (USEI03). |
| **Priority (Order)** | A value on an Order used to determine processing sequence in USEI02. Lower numbers are processed first. |
| **Quality Operator** | A system user responsible for managing returns and quarantine (USEI05). |
| **Quarantine** | The temporary holding area for returned products awaiting inspection. Items are processed in LIFO order. |
| **Railway Line** | The complete route connecting two endpoints (e.g., stations), which is composed of one or more Line Segments. |
| **Railway System** | The complete network of infrastructure, rolling stock, operations, and management for rail freight transport. |
| **ReceivedAt** | The timestamp when a box was received into the warehouse. Used for FIFO sorting. |
| **Refrigerated car** | A type of wagon (also called "Reefer") used for perishable goods that require temperature control. |
| **Relational Model** | The logical design of the database, defining tables, columns, and relationships (USBD02). |
| **Relocation** | A warehouse operation (mentioned in USEI01) to move a box from one bay to another, maintaining its FEFO/FIFO position. |
| **Restocked** | The status of a returned item that has been inspected, approved, and placed back into inventory. |
| **Return** | A product sent back to the warehouse for reasons such as "Damaged," "Expired," or "Customer Remorse." |
| **Rolling Stock** | All vehicles that move on the railway, including locomotives and wagons. Belongs to a Train Operator. |
| **Route** | A plan for a freight movement. Can be "simple" (one freight) or "complex" (multiple freights). |
| **Scheduler** | A system user responsible for planning train timetables. |
| **Siding** | A designated area or track, often at a station, where trains can pass each other on a single-track line or wait. |
| **SKU (Stock Keeping Unit)** | A unique identifier for a specific product type in the inventory (e.g., "SKU0001"). |
| **Station** | A facility on the railway network for loading, unloading, or sorting freight, which may include warehouses. |
| **Station Master** | A system user who oversees station operations, arrivals, and departures. |
| **Storage Manager** | A system user (e.g., Station Storage Manager) who manages warehouse operations, likely using the ESINF functionalities. |
| **Tank car** | A type of wagon designed to transport liquids and gases. |
| **Terminal** | A type of station, often intermodal, where cargo is transferred between trains, trucks, or ships. |
| **Terminal Operator** | A system user responsible for warehouse operations, specifically wagon unloading (USEI01). |
| **Train** | A series of rolling stock (locomotives and wagons) coupled together. |
| **Train Driver** | A system user who operates the train. |
| **Trolley** | A cart with a defined maximum weight capacity, used by warehouse pickers to collect items for orders. |
| **Wagon (Freight Car)** | The non-powered rolling stock used to carry cargo, such as boxcars, flatcars (for containers), or tank cars. |
| **Warehouse** | The storage facility, connected to the unloading docks, organized into aisles and bays, where product boxes are stored. |
| **WMS (Warehouse Management System)** | The software system used to log and manage all warehouse operations, including inventory, dispatch, and traceability. |