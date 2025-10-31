# 📖 Business Glossary (USLP01 & USLP05)

This document provides an alphabetical list of the key business terms and concepts for the "Logistics On Rails" project, covering Sprints 1 and 2, and aligned with the official Domain Model.

| Term | Definition |
| :--- | :--- |
| **2D-Tree (K-D Tree)** | A spatial index structure (where k=2) built using station (latitude, longitude) coordinates. It is used to enable fast geographic queries (USEI07). |
| **Aisle** | A pathway between rows of bays in a warehouse, providing access for picking. The "main corridor" (Entrance) is at Aisle 0. |
| **Allocation** | The logical process of reserving stock from specific boxes to fulfill order lines, following FEFO/FIFO rules. This happens *before* picking (USEI02). |
| **Allocation Mode (Strict/Partial)** | A rule for allocation (USEI02). **Strict** mode only allocates if the *entire* line quantity is available (otherwise, it allocates 0). **Partial** mode allocates whatever stock is available. |
| **Audit Log** | An external file that records every inspection action (e.g., Restocked, Discarded) for traceability of returns (USEI05). |
| **AVL Tree** | A self-balancing Binary Search Tree (BST). Used in USEI06 to build efficient, deterministic indexes on station data. |
| **Bay** | A specific storage location (identified by a number) within a warehouse aisle where boxes are physically stored. |
| **Best Fit Decreasing (BFD)** | A packing heuristic (USEI03). Allocations are sorted by weight (largest first) and then placed into the trolley where they fit *most tightly* (leaving the least remaining space). |
| **Box** | The base storage unit in the warehouse, representing a "lot fragment" of a single SKU with a specific quantity and optional expiry date. |
| **Boxcar** | A type of wagon that is enclosed and used for general goods. |
| **BST (Binary Search Tree)** | A data structure used in ESINF (Sprint 2) to index stations by various criteria (e.g., latitude, time zone) for fast lookups. |
| **Building** | An abstract concept for a physical structure at a Station, which can be a `Warehouse`, `RefrigeratedArea`, or `GrainSilo`. |
| **Bulk Build** | A strategy to construct a balanced 2D-tree efficiently, using pre-sorted data (like from the BST/AVL trees) rather than inserting one by one (USEI07). |
| **Caesar Cipher** | An encryption method (USAC01) that shifts capital letters by a fixed `key` (e.g., A -> C, B -> D for key=2). |
| **Capacity (Trolley)** | The maximum weight (in kg) that a trolley can hold, as defined by the planner in USEI03. |
| **Circular Buffer (Ring Buffer)** | An array of constant length that stores data in a continuous loop, using `head` and `tail` pointers to manage data (USAC04, USAC05). |
| **Customer Remorse** | A reason for a return (USEI05). The item is in good condition and can be restocked. |
| **CustomerOrder** | A customer's order for products, which contains one or more Order Lines. It has a priority and a due date used for processing in USEI02. |
| **Cycle Count** | A reason for a return (USEI05). An inventory audit found a mismatch. The item is typically restockable. |
| **Damaged** | A reason for a return (USEI05). The item is broken and must be discarded. |
| **Dequeue** | An operation (USAC05) to remove the *oldest* element from the circular buffer (from the `tail`). |
| **Deterministic Sweep (Strategy A)** | A pick path sequencing strategy (USEI04). The picker visits all required aisles in ascending order (Aisle 1, then Aisle 2, etc.). |
| **Discarded** | The status of a returned item that is deemed unsuitable (e.g., damaged, expired) and cannot be put back into inventory. |
| **Distance Function** | The formula used to calculate travel distance in the warehouse (USEI04). `D = b1 + |a1 - a2| * 3 + b2` for different aisles. |
| **Effective Speed** | The speed used for travel time calculation (USLP03). It is the *minimum* of the locomotive's max speed and the line segment's max speed. |
| **Eligibility (Status)** | The status of an order line after an allocation attempt (USEI02). Can be `ELIGIBLE` (fully allocatable), `PARTIAL` (partially allocatable), or `UNDISPATCHABLE` (no stock). |
| **Electrified** | A characteristic of a line segment, indicating if it allows the circulation of electric locomotives. |
| **Endpoint (Stop)** | A station in a planned route. The BDDAD (Sprint 2) data must record whether the train stops at that endpoint or just passes through. See `RouteStop`. |
| **Enqueue** | An operation (USAC04) to add a *new* element to the circular buffer (at the `head`). If the buffer is full, it overwrites the oldest element. |
| **Entrance (Warehouse)** | The starting point for all pick paths (USEI04), represented as coordinate (0,0) (Aisle 0, Bay 0). |
| **EuropeanStationDataset** | The large dataset (~64k records) of European railway stations used for spatial queries in ESINF (Sprint 2). |
| **Expired** | A reason for a return (USEI05). The item has passed its expiry date and must be discarded. |
| **Expiry Date** | The date on which a perishable product is no longer considered usable. Used for FEFO sorting. |
| **Facility** | An abstract concept for a logical railway location, which can be a `Station`, `FreightYard`, `Terminal`, or `Siding`. |
| **FEFO (First-Expired-First-Out)** | An inventory principle for perishable goods, ensuring items with the earliest expiry date are dispatched first. This has higher priority than FIFO. |
| **FF (First Fit)** | A packing heuristic (USEI03). Allocations are placed into the *first* available trolley that has enough capacity, in the order they are received. |
| **FFD (First Fit Decreasing)** | A packing heuristic (USEI03). Allocations are sorted by weight (largest first) and then the First Fit (FF) logic is applied. |
| **FIFO (First-In-First-Out)** | An inventory principle for non-perishable goods, ensuring items that were received first (oldest `receivedAt` date) are dispatched first. |
| **Flatcar** | A type of wagon with a flat, open bed, used for transporting containers or large machinery. |
| **Freight** | A set of wagons that must be transported from an origin station to a destination station. |
| **Freight Yard** | A facility, often part of a station, used for loading, unloading, sorting, and assembling trains. |
| **Gauge** | The width between the rails of a railway track (e.g., 1668mm). |
| **Geographical Area Search** | A query on the 2D-tree (USEI08) to find all stations within a specific latitude/longitude rectangle (e.g., `[latMin, latMax]`, `[lonMin, lonMax]`). |
| **Grain Silo** | A type of building at a station for storing bulk grain. |
| **Haversine Distance** | The formula used to calculate the "great-circle" distance (in km) between two (lat, lon) points on Earth (Used in USEI09, USEI10). |
| **Hopper car** | A type of wagon used for bulk commodities like grain or coal, which can be emptied from the bottom. |
| **Infrastructure (Railway)** | The physical components of the railway system, including tracks, stations, and terminals. |
| **Intermodal** | A type of terminal where cargo is transferred between different modes of transport (e.g., train, truck, ship). |
| **Item** | A record of a product type, identified by a `SKU`. Stores details like name, category, and weight. |
| **LIFO (Last-In-First-Out)** | The principle used for quarantine (USEI05), where the most recently returned items are inspected and processed first. |
S| **Light Sign** | The signal light (e.g., Red, Green, Yellow) associated with a `Track` in the ARQCP Station Management system. |
| **Line Owner** | The operator that owns the railway infrastructure (the `RailwayLine`s). |
| **Line Segment** | A component of a Railway Line connecting two stations. It has specific attributes like gauge, electrification, and speed limit. |
| **Locomotive** | The powered vehicle (diesel or electric) that pulls the freight wagons in a train. |
| **Nearest-Neighbour (Strategy B)** | A pick path sequencing strategy (USEI04). From the current location, the picker moves to the *closest* unvisited bay, repeating this process until all bays are visited. |
| **Order Line** | A single item (SKU) and its requested quantity within a `CustomerOrder`. |
| **Packing Heuristic** | An algorithm (e.g., FF, FFD, BFD) used in USEI03 to pack allocations (items) into trolleys (bins) based on weight capacity. |
| **Path (Railway)** | The ordered list of stations a train must pass through to travel from its origin to its destination. See `PlannedRoute`. |
| **Payload** | The maximum carrying capacity (e.g., in kg) of a wagon. |
| **Pick Path** | The optimized sequence of warehouse bays a picker must visit to fulfill a picking plan, calculated in USEI04 to minimize walking distance. |
| **Picker** | A warehouse worker responsible for physically collecting items from bays. |
| **Picking** | The physical process of collecting products from their warehouse locations (bays) to fulfill allocated orders. |
| **Picking Assignment** | A single task for a picker, created from an Allocation. It details what item (SKU), how much (qty), and from where (aisle, bay, boxId) to pick. |
| **Picking Plan** | The output of USEI03; a set of trolleys, each containing the specific picking assignments (allocations) that fit its weight capacity. |
| **Picking Status** | The state of a `PickingAssignment`. Can be `PENDING` (created), `ASSIGNED` (put in a trolley), or `PICKED` (completed). |
| **Planned Train Route** | A new concept in Sprint 2 (BDDAD). The defined *template* or *schedule* of line segments and endpoints (stations) for a specific train. |
| **Planner** | A system user (e.g., Warehouse Planner, Scheduler) responsible for allocation (USEI02), packing (USEI03), and route planning. |
| **Priority (Order)** | A value on an Order used to determine processing sequence in USEI02. Lower numbers are processed first. |
| **Proximity Search (Nearest-N)** | A query on the 2D-tree (USEI09) to find the *N* closest stations to a target (lat, lon) coordinate. |
| **Quality Operator** | A system user responsible for managing returns and quarantine (USEI05). |
| **Quarantine** | The temporary holding area for returned products awaiting inspection. Items are processed in LIFO order (USEI05). |
| **Radius Search** | A query on the 2D-tree (USEI10) to find all stations within a specific radius *R* (in km) of a target (lat, lon) coordinate. |
| **Railway Line** | The complete route connecting two endpoints (e.g., stations), which is composed of one or more Line Segments. |
| **Railway System** | The complete network of infrastructure, rolling stock, operations, and management for rail freight transport. |
| **ReceivedAt** | The timestamp when a box was received into the warehouse. Used for FIFO sorting. |
| **Refrigerated Area** | A type of building at a station for cold storage. |
| **Refrigerated car** | A type of wagon (also called "Reefer") used for perishable goods that require temperature control. |
| **Relational Model** | The logical design of the database, defining tables, columns, and relationships (USBD02, USBD21). |
| **Relocation** | A warehouse operation (mentioned in USEI01) to move a box from one bay to another, maintaining its FEFO/FIFO position. |
| **Restocked** | The status of a returned item that has been inspected, approved, and placed back into inventory (USEI05). |
| **Return** | A product sent back to the warehouse for reasons such as "Damaged," "Expired," or "Customer Remorse." |
| **Rolling Stock** | All vehicles that move on the railway, including locomotives and wagons. Belongs to a Train Operator. |
| **RouteStop** | A specific stop on a `PlannedTrainRoute`. It defines the `sequenceNumber` and whether it is a stop (`isStop = Y`) or just passes through (`isStop = N`). |
| **Scheduler** | A system user responsible for planning train timetables. |
| **Sensor** | An ARQCP device (Sprint 2) at a station that reads `Temperature` or `Humidity`. |
| **Siding** | A designated area or track, often at a station, where trains can pass each other on a single-track line or wait. |
| **SKU (Stock Keeping Unit)** | A unique identifier for a specific product type in the inventory (e.g., "SKU0001"). |
| **Station** | A facility on the railway network for loading, unloading, or sorting freight, which may include warehouses. In Sprint 2 (ESINF), this also refers to the ~64k dataset of European stations. |
| **Station Log** | A log file for the ARQCP system (Sprint 2) that records all timely instructions from the Station Operator. |
| **Station Master** | A system user who oversees station operations, arrivals, and departures, using the `StationMngtSystem`. |
| **StationMngtSystem** | The ARQCP component (Sprint 2) used by a Station Master to manage tracks, signals, and sensors at a specific station. |
| **Storage Manager** | A system user (e.g., Station Storage Manager) who manages warehouse operations, likely using the ESINF functionalities. |
| **SYS\_REF\_CURSOR** | The required return type for all PL/SQL functions developed in Sprint 2 (BDDAD), allowing a result set to be passed to the application. |
| **Tank car** | A type of wagon designed to transport liquids and gases. |
| **Terminal** | A type of station, often intermodal, where cargo is transferred between trains, trucks, or ships. |
| **Terminal Operator** | A system user responsible for warehouse operations, specifically wagon unloading (USEI01). |
| **Time Zone Group** | A classification for time zones (e.g., 'CET', 'WET/GMT') used as an index for the 64k European stations dataset (USEI06). |
| **Track** | A railway track *within* a station (numbered 1-99) managed by the ARQCP `StationMngtSystem`. |
| **Train** | A series of rolling stock (locomotives and wagons) coupled together. See `TrainJourney`. |
| **Train Driver** | A system user who operates the train. |
| **Train Event** | A record (e.g., arrival, departure) with a timestamp, associated with a `TrainJourney` (BDDAD Sprint 2). |
| **TrainJourney** | A specific *execution* of a `PlannedRoute` by a train, starting at a specific date/time (BDDAD Sprint 2). |
| **Train Operator** | The operator that owns the `RollingStock` (locomotives and wagons). |
| **Trolley** | A cart with a defined maximum weight capacity, used by warehouse pickers to collect items for orders (USEI03). |
| **Wagon (Freight Car)** | The non-powered rolling stock used to carry cargo, such as boxcars, flatcars (for containers), or tank cars. |
| **Warehouse** | The storage facility, connected to the unloading docks, organized into aisles and bays, where product boxes are stored. |
| **WMS (Warehouse Management System)** | The software system (ESINF) used to log and manage all warehouse operations, including inventory, dispatch, and traceability. |