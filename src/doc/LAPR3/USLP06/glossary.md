# 📖 Business Glossary (USLP06)

This document provides an alphabetical list of the key business terms and concepts for the **"Logistics On Rails"** project, covering **Sprints 1, 2, and 3**. It focuses on operational language rather than technical implementation details.

| Term | Definition |
| :--- | :--- |
| **Aisle** | A pathway between rows of bays in a warehouse, providing access for picking goods. The "main corridor" (Entrance) is located at Aisle 0. |
| **Allocation** | The operational process of reserving stock from specific boxes to fulfill customer orders, following specific rules (FEFO or FIFO) before the physical picking begins. |
| **Audit Log** | A secure record of every inspection action (e.g., "Restocked" or "Discarded") taken on returned goods, ensuring traceability. |
| **Backbone Network (Minimal)** | The minimum set of railway connections required to connect all reachable stations with the least possible total track length. Used to estimate baseline maintenance costs. |
| **Bay** | A specific storage section within a warehouse aisle where boxes of products are physically stored. |
| **Best Fit Decreasing (BFD)** | A smart packing strategy where heavy items are prioritized and placed into the trolley that has the tightest remaining space to maximize capacity usage. |
| **Bottleneck (Capacity)** | A section of the railway network where the capacity is limited, restricting the maximum flow of trains between hubs. |
| **Box** | The base storage unit in the warehouse. Each box contains a quantity of a specific product (SKU) and may have an expiry date. |
| **Capacity (Line/Edge)** | The maximum number of trains that a specific railway connection can handle per planning day. |
| **Capacity (Trolley)** | The maximum weight limit (in kg) that a warehouse trolley can carry, which limits how many orders can be picked at once. |
| **Complex Route** | A logistics route that involves multiple shipments (e.g., transporting Freight A to one location, then Freight B to another). |
| **Cost (Connection)** | A business metric for a railway line that combines physical distance, line capacity, and penalties/bonuses (e.g., due to construction works). |
| **Crossing** | An operation where two trains pass each other. On single-track lines, this must occur at designated stations or sidings. |
| **Customer Order** | A request from a client to purchase products, which contains a list of items and quantities to be dispatched from the warehouse. |
| **Cycle** | A situation in the network where a sequence of dependencies loops back on itself (e.g., Station A depends on B, which depends on A). This prevents proper upgrade planning. |
| **Directed Dependency** | A rule in upgrade planning where one station must be upgraded before its neighbor to ensure compatibility (e.g., signaling direction). |
| **Effective Speed** | The calculated travel speed of a train, considering the track's speed limit, the locomotive's power, and the total weight of the wagons. |
| **Endpoint (Stop)** | A station included in a planned route where the train stops or passes through. If it stops, arrival and departure times are recorded. |
| **Expiry Date** | The date after which a perishable product is no longer usable. Critical for the FEFO inventory strategy. |
| **FEFO** | **First-Expired-First-Out**. An inventory rule ensuring that perishable goods with the earliest expiry dates are shipped first to minimize waste. |
| **FIFO** | **First-In-First-Out**. An inventory rule for non-perishable goods ensuring that the oldest stock in the warehouse is shipped first. |
| **Freight** | A specific cargo consisting of a set of wagons that must be transported from an origin station to a destination station. |
| **Freight Manager** | The user responsible for managing cargos, defining logical routes, and overseeing the dispatch of trains. |
| **Gauge** | The width between the railway tracks (e.g., Iberian gauge vs. European gauge). Trains and tracks must have compatible gauges. |
| **HubScore** | A calculated score (0 to 1) used to identify the most important "Hub" stations in the network based on their connectivity and strategic location. |
| **Intermodal Terminal** | A facility where cargo can be transferred between trains and other transport modes like trucks or ships. |
| **Line Segment** | A physical section of track connecting two stations. It has specific properties like length, speed limit, and electrification status. |
| **Locomotive** | The engine vehicle (Electric or Diesel) used to pull the wagons in a train. |
| **Maximum Throughput (Flow)** | The theoretical maximum amount of traffic that can be sent between two major hubs given the capacity limits of the connecting lines. |
| **Packing Heuristic** | A strategy (like First Fit or Best Fit) used to decide how to group customer orders into trolleys efficiently without exceeding weight limits. |
| **Path (Physical)** | The actual ordered list of stations a train passes through to complete a logical route. |
| **Pick Path** | The optimized walking route a warehouse worker takes through aisles and bays to collect items, minimizing walking distance. |
| **Picker** | A warehouse employee responsible for physically collecting items from the shelves (bays). |
| **Planned Train Route** | A schedule assigned to a train, defining its path, start time, and stops. |
| **Quarantine** | A holding area where returned products are kept before being inspected. Items are processed starting with the most recently returned (LIFO). |
| **Radius Search** | An operational query to find all railway stations located within a specific distance (e.g., 50 km) of a target location. |
| **Relocation** | Moving a product box from one location in the warehouse to another without changing its inventory status. |
| **Rolling Stock** | A collective term for all vehicles that move on the railway, including locomotives and wagons. |
| **Route (Logical)** | The high-level logistic plan to move freight from point A to point B. It can be simple (one cargo) or composite (multiple cargos). |
| **Scheduled Trip** | The assignment of a physical Train to a Route for a specific date and time. This distinguishes the physical asset from the service it performs. |
| **Scheduler** | The system (or user) responsible for calculating timetables, train crossings, and travel times. |
| **Siding** | A side track used to allow trains to pass each other on single-track lines or for temporary parking. |
| **SKU** | **Stock Keeping Unit**. A unique identifier code for a specific type of product in the inventory. |
| **Station** | A facility on the network for operations. It may act as a terminal, freight yard, or simple stop. |
| **Topological Sort** | A planning method to determine the correct order of station upgrades so that dependencies (e.g., Station A before Station B) are respected. |
| **Track** | The physical rails within a station or line segment. |
| **Train** | A physical composition of locomotives and wagons coupled together. |
| **Train Operator** | The company that owns the rolling stock and operates the train services. |
| **Trolley** | A cart used by pickers to collect goods. It has a specific weight capacity that must be respected during planning. |
| **Upgrade Plan** | A schedule for improving railway lines where certain sections depend on others being finished first (Directed Dependencies). |
| **Wagon (Freight Car)** | A vehicle designed to carry goods (containers, liquids, grain, etc.) and is pulled by a locomotive. |
| **Warehouse** | A storage facility at a terminal, organized into aisles and bays, used to store product boxes. |
| **WMS** | **Warehouse Management System**. The system used to track inventory, manage locations, and record all warehouse operations. |
