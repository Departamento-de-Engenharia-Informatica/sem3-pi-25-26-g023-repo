package pt.ipp.isep.dei.UI;

import pt.ipp.isep.dei.controller.TravelTimeController;
import pt.ipp.isep.dei.domain.*;
import pt.ipp.isep.dei.repository.StationRepository;
import pt.ipp.isep.dei.repository.LocomotiveRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator; // Import needed
import java.util.HashMap; // <-- ADDED IMPORT
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map; // <-- ADDED IMPORT
import java.util.Scanner;


/**
 * Main User Interface for the Cargo Handling Terminal.
 * (Version 2.2 - Advanced USEI06 Query UI)
 */
public class CargoHandlingUI implements Runnable {

    // --- ANSI Color Codes ---
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_BOLD = "\u001B[1m";
    public static final String ANSI_ITALIC = "\u001B[3m";

    // (Class fields remain the same)
    private final WMS wms;
    private final InventoryManager manager;
    private final List<Wagon> wagons;
    private final TravelTimeController travelTimeController;
    private final StationRepository estacaoRepo;
    private final LocomotiveRepository locomotivaRepo;
    private final StationIndexManager stationIndexManager;
    private AllocationResult lastAllocationResult = null;
    private PickingPlan lastPickingPlan = null;
    private final Scanner scanner;

    /**
     * Updated constructor for Sprint 2.
     */
    public CargoHandlingUI(WMS wms, InventoryManager manager, List<Wagon> wagons,
                           TravelTimeController travelTimeController, StationRepository estacaoRepo,
                           LocomotiveRepository locomotivaRepo,
                           StationIndexManager stationIndexManager) {
        this.wms = wms;
        this.manager = manager;
        this.wagons = wagons;
        this.travelTimeController = travelTimeController;
        this.estacaoRepo = estacaoRepo;
        this.locomotivaRepo = locomotivaRepo;
        this.stationIndexManager = stationIndexManager;
        this.scanner = new Scanner(System.in);
    }


    /**
     * Runs the main menu loop.
     */
    @Override
    public void run() {
        int option = -1;

        do {
            showMenu();
            try {
                // Read option
                // *** CORRECTION: Changed from 9 to 10 to allow all options ***
                option = readInt(0, 10, ANSI_BOLD + "Option: " + ANSI_RESET);

                // --- Robustness: Catches errors from handlers ---
                try {
                    handleOption(option);
                } catch (Exception e) {
                    showError("An unexpected error occurred: " + e.getMessage());
                    System.err.println(ANSI_ITALIC + "Stack trace (for debug):");
                    e.printStackTrace(System.err);
                    System.err.println(ANSI_RESET);
                }
                // --- End robustness block ---

            } catch (InputMismatchException e) {
                showError("Invalid input. Please enter a number.");
                scanner.nextLine(); // Clear scanner buffer
            } catch (Exception e) {
                showError("Fatal UI Error: " + e.getMessage());
                option = 0; // Force exit
            }

            if (option != 0) {
                promptEnterKey(); // Pause for user to read output
            }

        } while (option != 0);

        System.out.println(ANSI_CYAN + "\nClosing scanner. Goodbye! 👋" + ANSI_RESET);
        scanner.close();
    }

    /**
     * Displays the "pretty" main menu.
     */
    private void showMenu() {
        // "Clear" the screen
        System.out.print("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");

        System.out.println(ANSI_BOLD + ANSI_BLUE + "==========================================================" + ANSI_RESET);
        System.out.println(ANSI_BOLD + ANSI_BLUE + "      🚆 LOGISTICS ON RAILS - G023 MAIN MENU 🚆      " + ANSI_RESET);
        System.out.println(ANSI_BOLD + ANSI_BLUE + "==========================================================" + ANSI_RESET);

        // --- ESINF Sprint 1 ---
        System.out.println("\n" + ANSI_BOLD + ANSI_PURPLE + "--- Warehouse Setup (Sprint 1) ---" + ANSI_RESET);
        System.out.println(ANSI_GREEN + " 1. " + ANSI_RESET + "[USEI01] Unload Wagons (Status: " + ANSI_ITALIC + "Loaded on startup" + ANSI_RESET + ")");
        System.out.println(ANSI_GREEN + " 2. " + ANSI_RESET + "[USEI05] Process Quarantine Returns");

        // --- Picking Workflow ---
        System.out.println("\n" + ANSI_BOLD + ANSI_PURPLE + "--- Picking Workflow (Sprint 1) ---" + ANSI_RESET);
        System.out.println(ANSI_GREEN + " 3. " + ANSI_RESET + "[USEI02] Allocate Orders");
        System.out.println(ANSI_GREEN + " 4. " + ANSI_RESET + "[USEI03] Pack Trolleys " + ANSI_ITALIC + "(Run US02 first)" + ANSI_RESET);
        System.out.println(ANSI_GREEN + " 5. " + ANSI_RESET + "[USEI04] Calculate Pick Path " + ANSI_ITALIC + "(Run US03 first)" + ANSI_RESET);

        // --- Railway & Station Ops ---
        System.out.println("\n" + ANSI_BOLD + ANSI_PURPLE + "--- Railway & Station Ops (S1 & S2) ---" + ANSI_RESET);
        System.out.println(ANSI_GREEN + " 6. " + ANSI_RESET + "[USLP03] Calculate Train Travel Time (S1)");
        System.out.println(ANSI_GREEN + " 7. " + ANSI_RESET + "[USEI06] Query European Station Index (S2)");
        System.out.println(ANSI_GREEN + " 8. " + ANSI_RESET + "[USEI07] Build & Analyze 2D-Tree (S2)");

        // --- Info ---
        System.out.println("\n" + ANSI_BOLD + ANSI_PURPLE + "--- System Information ---" + ANSI_RESET);
        System.out.println(ANSI_GREEN + " 9. " + ANSI_RESET + "View Current Inventory");
        System.out.println(ANSI_GREEN + " 10. " + ANSI_RESET + "View Warehouse Info");

        // --- Exit ---
        System.out.println("\n" + ANSI_BOLD + "----------------------------------------------------------" + ANSI_RESET);

        // Dynamic status
        String allocStatus = (lastAllocationResult != null && !lastAllocationResult.allocations.isEmpty()) ?
                ANSI_GREEN + String.format("GENERATED (%d allocs)", lastAllocationResult.allocations.size()) : ANSI_YELLOW + "NOT-RUN";
        String planStatus = (lastPickingPlan != null) ?
                ANSI_GREEN + String.format("GENERATED (%d trolleys)", lastPickingPlan.getTotalTrolleys()) : ANSI_YELLOW + "NOT-RUN";
        System.out.println(ANSI_BOLD + "   Status: [Allocations: " + allocStatus + ANSI_BOLD + "] [Picking Plan: " + planStatus + ANSI_BOLD + "]" + ANSI_RESET);

        System.out.println(ANSI_YELLOW + "  0. " + ANSI_RESET + "Exit System");
        System.out.println(ANSI_BOLD + "----------------------------------------------------------" + ANSI_RESET);
    }

    /**
     * Handles the menu option selected by the user.
     */
    private void handleOption(int option) {
        switch (option) {
            case 1:
                handleUnloadWagons(); // USEI01
                break;
            case 2:
                handleProcessReturns(); // USEI05
                break;
            case 3:
                handleAllocateOrders(); // USEI02
                break;
            case 4:
                handlePackTrolleys(); // USEI03
                break;
            case 5:
                handleCalculatePickingPath(); // USEI04
                break;
            case 6:
                handleCalculateTravelTime(); // USLP03
                break;
            case 7:
                handleQueryStationIndex(); // USEI06
                break;
            case 8:
                handleBuild2DTree(); // USEI07
                break;
            case 9:
                handleViewInventory(); // <-- UPGRADED
                break;
            case 10:
                handleViewWarehouseInfo(); // <-- UPGRADED
                break;
            case 0:
                System.out.println(ANSI_CYAN + "\nExiting Cargo Handling Menu... 👋" + ANSI_RESET);
                break;
            default:
                showError("Invalid option. Please select a valid number from the menu.");
                break;
        }
    }

    // --- Visual Feedback Helpers ---

    private void showSuccess(String message) {
        System.out.println(ANSI_GREEN + ANSI_BOLD + "\n✅ SUCCESS: " + ANSI_RESET + ANSI_GREEN + message + ANSI_RESET);
    }

    private void showError(String message) {
        System.out.println(ANSI_RED + ANSI_BOLD + "\n❌ ERROR: " + ANSI_RESET + ANSI_RED + message + ANSI_RESET);
    }

    private void showInfo(String message) {
        System.out.println(ANSI_CYAN + "\nℹ️  " + message + ANSI_RESET);
    }

    private void promptEnterKey() {
        System.out.print(ANSI_ITALIC + "\n(Press ENTER to return to the menu...)" + ANSI_RESET);
        scanner.nextLine();
    }


    // --- [USEI01] Handler (Unchanged) ---
    private void handleUnloadWagons() {
        showInfo("--- [USEI01] Unload Wagons ---");
        System.out.println(" 1. Unload ALL wagons");
        System.out.println(" 2. Select wagons manually");
        System.out.println(ANSI_YELLOW + " 0. Cancel" + ANSI_RESET);

        int sub = readInt(0, 2, "Option: ");

        if (sub == 1) {
            wms.unloadWagons(wagons);
            showSuccess("All wagons have been processed.");
        } else if (sub == 2) {
            for (int i = 0; i < wagons.size(); i++) {
                System.out.printf(" %d. Wagon %s (%d boxes)%n",
                        i + 1, wagons.get(i).getWagonId(), wagons.get(i).getBoxes().size());
            }
            String choicesStr = readString("➡️  Enter wagon numbers (comma-separated) [c=Cancel]: ");
            if (isCancel(choicesStr)) {
                showInfo("Unloading cancelled.");
                return;
            }

            String[] choices = choicesStr.split(",");
            List<Wagon> selected = new java.util.ArrayList<>();
            for (String c : choices) {
                try {
                    int idx = Integer.parseInt(c.trim()) - 1;
                    if (idx >= 0 && idx < wagons.size()) {
                        selected.add(wagons.get(idx));
                    }
                } catch (NumberFormatException ignored) {}
            }
            wms.unloadWagons(selected);
            showSuccess("Selected wagons have been processed.");
        } else {
            showInfo("Unloading cancelled.");
        }
    }

    // --- [USEI05] Handler (Unchanged) ---
    private void handleProcessReturns() {
        showInfo("--- [USEI05] Process Quarantine Returns (LIFO) ---");
        wms.processReturns();
        showSuccess("Return processing complete.");
        showInfo("Check 'audit.log' for details.");
    }

    // --- [USEI02] Handler (Unchanged) ---
    private void handleAllocateOrders() {
        showInfo("--- [USEI02] Allocate Open Orders ---");

        List<Order> orders;
        try {
            orders = manager.loadOrders(
                    "src/main/java/pt/ipp/isep/dei/FicheirosCSV/orders.csv",
                    "src/main/java/pt/ipp/isep/dei/FicheirosCSV/order_lines.csv"
            );
        } catch (Exception e) {
            showError("Failed to load orders: " + e.getMessage());
            return;
        }

        List<Box> currentInventoryState = new ArrayList<>(manager.getInventory().getBoxes());

        if (orders.isEmpty()) {
            showError("No valid orders found to process.");
            return;
        }
        if (currentInventoryState.isEmpty()) {
            showError("Inventory is empty. Cannot allocate orders.");
            return;
        }

        System.out.printf("ℹ️  Data loaded: %d orders, %d boxes in inventory%n",
                orders.size(), currentInventoryState.size());

        System.out.println("\nSelect Allocation Mode:");
        System.out.println(ANSI_GREEN + " 1. " + ANSI_RESET + "STRICT (All or nothing per line)");
        System.out.println(ANSI_GREEN + " 2. " + ANSI_RESET + "PARTIAL (Allocate available stock)");
        System.out.println(ANSI_YELLOW + " 0. " + ANSI_RESET + "Cancel");
        int modeChoice = readInt(0, 2, "Option: ");

        if (modeChoice == 0) {
            showInfo("Allocation cancelled.");
            return;
        }
        OrderAllocator.Mode mode = (modeChoice == 1) ? OrderAllocator.Mode.STRICT : OrderAllocator.Mode.PARTIAL;

        OrderAllocator allocator = new OrderAllocator();
        allocator.setItems(manager.getItemsMap());

        this.lastAllocationResult = allocator.allocateOrders(orders, currentInventoryState, mode);
        this.lastPickingPlan = null;

        showSuccess("USEI02 executed successfully!");
        System.out.printf("📊 Results: %d allocations generated, %d lines processed%n",
                lastAllocationResult.allocations.size(), lastAllocationResult.eligibilityList.size());
    }

    // --- [USEI03] Handler (Unchanged) ---
    private void handlePackTrolleys() {
        showInfo("--- [USEI03] Pack Allocations into Trolleys ---");

        if (this.lastAllocationResult == null || this.lastAllocationResult.allocations.isEmpty()) {
            showError("You must run [3. USEI02] Allocate Open Orders first.\n   No valid allocations are available to be packed.");
            return;
        }

        System.out.printf("ℹ️  Ready to pack %d allocations.%n", this.lastAllocationResult.allocations.size());
        double capacity = readDouble(0.1, Double.MAX_VALUE, ANSI_BOLD + "➡️  Trolley capacity (kg) [0=Cancel]: " + ANSI_RESET);
        if (capacity == 0) {
            showInfo("Packing cancelled.");
            return;
        }

        System.out.println("\n🧠 Available Heuristics:");
        System.out.println(ANSI_GREEN + " 1. " + ANSI_RESET + "FIRST_FIT - First one that fits (fastest)");
        System.out.println(ANSI_GREEN + " 2. " + ANSI_RESET + "FIRST_FIT_DECREASING - Largest first (more efficient)");
        System.out.println(ANSI_GREEN + " 3. " + ANSI_RESET + "BEST_FIT_DECREASING - Best fit (optimizes space)");
        System.out.println(ANSI_YELLOW + " 0. " + ANSI_RESET + "Cancel");
        int heuristicChoice = readInt(0, 3, ANSI_BOLD + "➡️  Choose heuristic (0-3): " + ANSI_RESET);

        HeuristicType heuristic;
        switch(heuristicChoice) {
            case 1: heuristic = HeuristicType.FIRST_FIT; break;
            case 2: heuristic = HeuristicType.FIRST_FIT_DECREASING; break;
            case 3: heuristic = HeuristicType.BEST_FIT_DECREASING; break;
            case 0:
                showInfo("Packing cancelled."); return;
            default:
                showError("Invalid choice. Using FIRST_FIT by default.");
                heuristic = HeuristicType.FIRST_FIT;
                break;
        }

        showInfo("\n⚙️  Executing USEI03...");
        PickingService service = new PickingService();
        service.setItemsMap(manager.getItemsMap());

        this.lastPickingPlan = service.generatePickingPlan(
                this.lastAllocationResult.allocations,
                capacity,
                heuristic
        );

        System.out.println("\n" + ANSI_BOLD + "=".repeat(60) + ANSI_RESET);
        System.out.println(ANSI_BOLD + "           📊 RESULTS USEI03 - Picking Plan" + ANSI_RESET);
        System.out.println(ANSI_BOLD + "=".repeat(60) + ANSI_RESET);
        System.out.println(lastPickingPlan.getSummary());
    }

    // --- [USEI04] Handler (Unchanged) ---
    private void handleCalculatePickingPath() {
        showInfo("--- [USEI04] Calculate Picking Path ---");

        if (this.lastPickingPlan == null) {
            showError("You must run [4. USEI03] Pack Allocations into Trolleys first.\n   No picking plan is available to calculate a path.");
            return;
        }
        if (this.lastPickingPlan.getTotalTrolleys() == 0) {
            showInfo("The current picking plan has 0 trolleys. Nothing to calculate.");
            return;
        }

        System.out.printf("ℹ️  Calculating paths for %d trolleys in Plan %s...%n",
                this.lastPickingPlan.getTotalTrolleys(), this.lastPickingPlan.getId());

        PickingPathService pathService = new PickingPathService();
        try {
            Map<String, PickingPathService.PathResult> pathResults = pathService.calculatePickingPaths(this.lastPickingPlan);

            if (pathResults.isEmpty()) {
                showError("Could not calculate paths (check if picking plan has valid locations).");
            } else {
                System.out.println("\n" + ANSI_BOLD + "--- Sequencing Results (USEI04) ---" + ANSI_RESET);
                pathResults.forEach((strategyName, result) -> {
                    System.out.println("\n" + ANSI_BOLD + ANSI_CYAN + strategyName + ":" + ANSI_RESET);
                    System.out.println(result);
                    System.out.println("-".repeat(40));
                });
                showSuccess("USEI04 completed successfully!");
            }

        } catch (Exception e) {
            showError("Error calculating picking paths (USEI04): " + e.getMessage());
        }
    }

    // --- [USLP03] Handler (Scanner fix) ---
    private void handleCalculateTravelTime() {
        showInfo("--- [USLP03] Calculate TravelTime ---");
        // Pass the main scanner to the sub-UI
        TravelTimeUI travelTimeUI = new TravelTimeUI(travelTimeController, estacaoRepo, locomotivaRepo, this.scanner);
        travelTimeUI.run();
        showSuccess("Module [USLP03] complete.");
    }

    // --- [USEI06] *** REPLACED WITH NEW ADVANCED UI *** ---
    private void handleQueryStationIndex() {
        showInfo("--- [USEI06] Advanced European Station Query ---");

        // --- Step 1: Get Base Query (Time Zone) ---
        System.out.println(ANSI_BOLD + "1. Select Base Search (Time Zone):" + ANSI_RESET);
        System.out.println(ANSI_GREEN + " 1. " + ANSI_RESET + "By single Time Zone Group (e.g., CET)");
        System.out.println(ANSI_GREEN + " 2. " + ANSI_RESET + "By Time Zone Window (e.g., CET to EET)");
        System.out.println(ANSI_YELLOW + " 0. " + ANSI_RESET + "Cancel");

        int choice = readInt(0, 2, "Option: ");
        List<EuropeanStation> baseResults;

        switch (choice) {
            case 1:
                String tzg = readString(ANSI_BOLD + "➡️  Enter Time Zone Group (e.g., CET) [c=Cancel]: " + ANSI_RESET);
                if (isCancel(tzg)) {
                    showInfo("Query cancelled."); return;
                }
                baseResults = stationIndexManager.getStationsByTimeZoneGroup(tzg.toUpperCase());
                break;
            case 2:
                String tzgMin = readString(ANSI_BOLD + "➡️  Enter MINIMUM Time Zone Group [c=Cancel]: " + ANSI_RESET);
                if (isCancel(tzgMin)) {
                    showInfo("Query cancelled."); return;
                }
                String tzgMax = readString(ANSI_BOLD + "➡️  Enter MAXIMUM Time Zone Group [c=Cancel]: " + ANSI_RESET);
                if (isCancel(tzgMax)) {
                    showInfo("Query cancelled."); return;
                }
                baseResults = stationIndexManager.getStationsInTimeZoneWindow(tzgMin.toUpperCase(), tzgMax.toUpperCase());
                break;
            default:
                showInfo("Query cancelled.");
                return;
        }

        if (baseResults.isEmpty()) {
            showInfo("No stations found for this time zone query. Returning to menu.");
            return;
        }

        // --- Step 2: Advanced Filters ---
        Map<String, String> filters = new HashMap<>();
        String filterChoice;
        do {
            System.out.println(ANSI_BOLD + "\n2. Apply Advanced Filters (Optional):" + ANSI_RESET);
            System.out.printf("   %sBase results: %d stations%s%n", ANSI_CYAN, baseResults.size(), ANSI_RESET);
            System.out.println(ANSI_ITALIC + "   Current Filters:");
            System.out.println(ANSI_ITALIC + "   - Country: " + filters.getOrDefault("country", "Any"));
            System.out.println(ANSI_ITALIC + "   - Is City: " + filters.getOrDefault("isCity", "Any"));
            System.out.println(ANSI_ITALIC + "   - Is Main: " + filters.getOrDefault("isMain", "Any"));
            System.out.println(ANSI_ITALIC + "   - Is Airport: " + filters.getOrDefault("isAirport", "Any") + ANSI_RESET);

            System.out.println("\n(1) Set Country Code (e.g., PT, ES, DE)");
            System.out.println("(2) Filter by 'isCity' (True/False)");
            System.out.println("(3) Filter by 'isMainStation' (True/False)");
            System.out.println("(4) Filter by 'isAirport' (True/False)");
            System.out.println(ANSI_YELLOW + "(R) Reset all filters" + ANSI_RESET);
            System.out.println(ANSI_GREEN + "\n(S) Search & View Results" + ANSI_RESET);
            System.out.println(ANSI_YELLOW + "(C) Cancel" + ANSI_RESET);

            filterChoice = readString(ANSI_BOLD + "Choose an option [1-4, R, S, C]: " + ANSI_RESET).toUpperCase();

            switch (filterChoice) {
                case "1":
                    String country = readString("   Enter Country Code (or 'any' to clear): ");
                    if (country.equalsIgnoreCase("any")) filters.remove("country");
                    else filters.put("country", country.toUpperCase());
                    break;
                case "2":
                    String isCity = readString("   Must be a City? (T/F, or 'any' to clear): ");
                    if (isCity.equalsIgnoreCase("any")) filters.remove("isCity");
                    else filters.put("isCity", isCity.toUpperCase().startsWith("T") ? "true" : "false");
                    break;
                case "3":
                    String isMain = readString("   Must be a Main Station? (T/F, or 'any' to clear): ");
                    if (isMain.equalsIgnoreCase("any")) filters.remove("isMain");
                    else filters.put("isMain", isMain.toUpperCase().startsWith("T") ? "true" : "false");
                    break;
                case "4":
                    String isAirport = readString("   Must be an Airport? (T/F, or 'any' to clear): ");
                    if (isAirport.equalsIgnoreCase("any")) filters.remove("isAirport");
                    else filters.put("isAirport", isAirport.toUpperCase().startsWith("T") ? "true" : "false");
                    break;
                case "R":
                    filters.clear();
                    System.out.println(ANSI_YELLOW + "   All filters cleared." + ANSI_RESET);
                    break;
                case "C":
                    showInfo("Query cancelled.");
                    return;
                case "S":
                    break; // Exit loop and proceed to search
                default:
                    showError("Invalid option.");
            }
        } while (!filterChoice.equals("S"));


        // --- Step 3: Apply Filters ---
        showInfo("Applying filters...");
        List<EuropeanStation> filteredResults = applyAdvancedFilters(baseResults, filters);

        // --- Step 4: Show Paginated Results ---
        if (filteredResults.isEmpty()) {
            showInfo("No results found after applying filters.");
        } else {
            showPaginatedResults(filteredResults);
        }
    }


    // -----------------------------------------------------------------
    // --- [USEI07] Handler (Unchanged) ---
    // -----------------------------------------------------------------
    private void handleBuild2DTree() {
        showInfo("--- [USEI07] Build & Analyze 2D-Tree ---");

        try {
            // Get stats (triggers build if not already built)
            Map<String, Object> stats = stationIndexManager.get2DTreeStats();

            // The manager prints the build time, so we just show the result.
            showSuccess("2D-Tree analysis complete.");

            // --- "Beautiful" stats output ---
            System.out.println(ANSI_BOLD + "\n--- 2D-Tree Statistics ---" + ANSI_RESET);

            System.out.printf(ANSI_BOLD + "  Size (Nodes): %s%-10d " + ANSI_RESET,
                    ANSI_CYAN, stats.get("size"));
            System.out.printf(ANSI_BOLD + "Height: %s%d%n" + ANSI_RESET,
                    ANSI_CYAN, stats.get("height"));

            System.out.println(ANSI_BOLD + "  Node Capacity (Stations per Node):" + ANSI_RESET);

            @SuppressWarnings("unchecked")
            Map<Integer, Integer> buckets = (Map<Integer, Integer>) stats.get("bucketSizes");

            buckets.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> System.out.printf(
                            // e.g., "  - 1 station/node : 61562 nodes"
                            "    - %d station(s)/node : %s%d nodes%s%n",
                            entry.getKey(),
                            ANSI_CYAN, entry.getValue(), ANSI_RESET
                    ));

            // --- "Beautiful" complexity analysis ---
            System.out.println(ANSI_BOLD + "\n--- Build Analysis ---" + ANSI_RESET);
            System.out.println(ANSI_BOLD + "  Strategy:    " + ANSI_ITALIC + "Balanced build using pre-sorted lists (from USEI06)." + ANSI_RESET);
            System.out.println(ANSI_BOLD + "  Complexity:  " + ANSI_CYAN + "O(N log N)" + ANSI_RESET);


        } catch (Exception e) {
            showError("Failed to build or analyze the 2D-Tree (USEI07): " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------
    // --- Info Handlers (Unchanged) ---
    // -----------------------------------------------------------------
    private void handleViewInventory() {
        showInfo("--- Current Inventory Contents ---");
        List<Box> boxes = manager.getInventory().getBoxes();

        if (boxes.isEmpty()) {
            showInfo("Inventory is empty.");
        } else {
            System.out.printf(ANSI_BOLD + "Displaying %d boxes (Sorted by FEFO/FIFO):%n" + ANSI_RESET, boxes.size());

            // --- NEW: Table Header ---
            System.out.println(ANSI_BOLD + ANSI_PURPLE + "=".repeat(84) + ANSI_RESET);
            System.out.printf(ANSI_BOLD +
                            "  %-10s | %-12s | %-4s | %-12s | %-18s | %-10s %n",
                    "BOX ID", "SKU", "QTY", "EXPIRY", "RECEIVED", "LOCATION"
            );
            System.out.println(ANSI_BOLD + ANSI_PURPLE + "-".repeat(84) + ANSI_RESET);

            // The new Box.toString() is formatted for this table
            for (Box b : boxes) {
                System.out.println(b.toString());
            }
            System.out.println(ANSI_BOLD + ANSI_PURPLE + "=".repeat(84) + ANSI_RESET);
        }
    }

    private void handleViewWarehouseInfo() {
        showInfo("--- Warehouse Information ---");
        List<Warehouse> warehouses = manager.getWarehouses();
        if (warehouses.isEmpty()) {
            showInfo("No warehouses loaded.");
            return;
        }

        for (Warehouse wh : warehouses) {
            System.out.printf(ANSI_BOLD + "\n🏭 Warehouse: %s%n" + ANSI_RESET, wh.getWarehouseId());
            System.out.printf("   📦 Bays: %d%n", wh.getBays().size());

            int totalCapacity = 0;
            int usedCapacity = 0;
            for (Bay bay : wh.getBays()) {
                totalCapacity += bay.getCapacityBoxes();
                usedCapacity += bay.getBoxes().size();
            }

            double percentage = (totalCapacity > 0 ? (usedCapacity * 100.0 / totalCapacity) : 0);
            String color = percentage > 85 ? ANSI_RED : (percentage > 60 ? ANSI_YELLOW : ANSI_GREEN);

            System.out.printf("   📊 Physical Capacity: " + color + "%d/%d boxes (%.1f%% full)" + ANSI_RESET + "%n",
                    usedCapacity, totalCapacity, percentage);
            System.out.printf("   ℹ️  Logical Inventory Size (Total): %d boxes%n", manager.getInventory().getBoxes().size());

            // --- NEW: Interactive prompt ---
            String details = readString(ANSI_ITALIC + "   View bay details for this warehouse? (y/N): " + ANSI_RESET);
            if (details.trim().equalsIgnoreCase("y")) {
                printBayDetails(wh);
            }
        }
    }

    // -----------------------------------------------------------------
    // --- Helper Method (Unchanged) ---
    // -----------------------------------------------------------------

    /**
     * NEW HELPER: Prints a "pretty" table of bay details for a warehouse.
     */
    private void printBayDetails(Warehouse wh) {
        System.out.println(ANSI_BOLD + "\n   --- Bay Details for Warehouse " + wh.getWarehouseId() + " ---" + ANSI_RESET);

        // Header
        System.out.printf("   %-10s | %-10s | %-18s | %s%n", "AISLE", "BAY", "CAPACITY (USED/MAX)", "VISUAL");
        System.out.println(ANSI_PURPLE + "   " + "-".repeat(60) + ANSI_RESET);

        // Sort bays for a logical view (by Aisle, then Bay)
        List<Bay> sortedBays = wh.getBays().stream()
                .sorted(Comparator.comparing(Bay::getAisle).thenComparing(Bay::getBay))
                .toList();

        for (Bay bay : sortedBays) {
            int used = bay.getBoxes().size();
            int max = bay.getCapacityBoxes();
            double percentage = (max > 0) ? ((double) used / max) : 0;

            // Color based on capacity
            String color = (used == max) ? ANSI_RED : (used > 0 ? ANSI_GREEN : ANSI_RESET);

            // --- Visual Occupation Bar ---
            int barWidth = 10; // [#####     ] (10 chars)
            int filled = (int) (percentage * barWidth);
            int empty = barWidth - filled;
            String visualBar = String.format("[%s%s%s]",
                    color, "#".repeat(filled), " ".repeat(empty) + ANSI_RESET);

            System.out.printf(
                    "   %-10s | %-10s | %s%2d / %-2d boxes%s     | %s%n",
                    bay.getAisle(),
                    bay.getBay(),
                    color, used, max, ANSI_RESET, // e.g., 5 / 10
                    visualBar
            );
        }
        System.out.println(ANSI_PURPLE + "   " + "-".repeat(60) + ANSI_RESET);
    }

    // --- Robust Input Helpers (Unchanged) ---
    private String readString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    private int readInt(int min, int max, String prompt) {
        System.out.print(prompt);
        while (true) {
            try {
                String line = scanner.nextLine();
                if (isCancel(line)) {
                    return 0; // Universal cancel
                }
                int option = Integer.parseInt(line);
                if (option >= min && option <= max) {
                    return option;
                } else {
                    System.out.print(ANSI_RED + String.format("❌ Invalid input. Please enter a number between %d and %d.%n" + ANSI_RESET + prompt, min, max));
                }
            } catch (NumberFormatException e) {
                System.out.print(ANSI_RED + "❌ Invalid input. Please enter a number." + ANSI_RESET + "\n" + prompt);
            }
        }
    }

    private double readDouble(double min, double max, String prompt) {
        System.out.print(prompt);
        while (true) {
            try {
                String line = scanner.nextLine();
                if (isCancel(line)) {
                    return 0.0; // Universal cancel
                }
                double value = Double.parseDouble(line.replace(',', '.'));
                if (value >= min && value <= max) {
                    return value;
                } else {
                    System.out.print(ANSI_RED + String.format("❌ Invalid input. Please enter a value between %.2f and %.2f.%n" + ANSI_RESET + prompt, min, max));
                }
            } catch (NumberFormatException e) {
                System.out.print(ANSI_RED + "❌ Invalid input. Please enter a valid number." + ANSI_RESET + "\n" + prompt);
            }
        }
    }

    private boolean isCancel(String input) {
        return input.trim().equals("0") || input.trim().equalsIgnoreCase("c");
    }


    // --- *** ADD THESE 2 NEW HELPER METHODS FOR THE ADVANCED UI *** ---

    /**
     * Helper method to filter a list of stations based on the advanced filters.
     *
     * @param stations The base list of stations.
     * @param filters  A map of filters to apply.
     * @return A new, filtered list.
     */
    private List<EuropeanStation> applyAdvancedFilters(List<EuropeanStation> stations, Map<String, String> filters) {
        if (filters.isEmpty()) {
            return stations;
        }

        return stations.stream()
                .filter(s -> { // Filter by Country
                    if (!filters.containsKey("country")) return true;
                    return s.getCountry().equalsIgnoreCase(filters.get("country"));
                })
                .filter(s -> { // Filter by isCity
                    if (!filters.containsKey("isCity")) return true;
                    boolean mustBeCity = Boolean.parseBoolean(filters.get("isCity"));
                    return s.isCity() == mustBeCity;
                })
                .filter(s -> { // Filter by isMainStation
                    if (!filters.containsKey("isMain")) return true;
                    boolean mustBeMain = Boolean.parseBoolean(filters.get("isMain"));
                    return s.isMainStation() == mustBeMain;
                })
                .filter(s -> { // Filter by isAirport
                    if (!filters.containsKey("isAirport")) return true;
                    boolean mustBeAirport = Boolean.parseBoolean(filters.get("isAirport"));
                    return s.isAirport() == mustBeAirport;
                })
                .sorted(Comparator.comparing(EuropeanStation::getCountry) // Re-sort the final list
                        .thenComparing(EuropeanStation::getStation))
                .toList();
    }


    /**
     * Displays a list of stations in a user-friendly, paginated view.
     *
     * @param results The final, filtered list of stations to display.
     */
    private void showPaginatedResults(List<EuropeanStation> results) {
        int pageSize = 10; // Items per page
        int totalResults = results.size();
        int totalPages = (int) Math.ceil((double) totalResults / pageSize);
        int currentPage = 0;
        String input;

        do {
            // "Clear" screen
            System.out.print("\n\n\n\n\n\n\n\n\n\n\n\n");

            // --- Header ---
            System.out.println(ANSI_BOLD + ANSI_BLUE + "==========================================================" + ANSI_RESET);
            System.out.printf(ANSI_BOLD + ANSI_BLUE + "         Station Query Results (Page %d of %d)        %n", currentPage + 1, totalPages);
            System.out.println(ANSI_BOLD + ANSI_BLUE + "==========================================================" + ANSI_RESET);

            int startIndex = currentPage * pageSize;
            int endIndex = Math.min(startIndex + pageSize, totalResults);

            System.out.printf(ANSI_BOLD + "Showing results %d-%d of %d%n\n" + ANSI_RESET, startIndex + 1, endIndex, totalResults);

            // --- Table Header ---
            System.out.printf(ANSI_BOLD + "  %-30s | %-7s | %-5s | %-5s | %-5s | %-15s %n",
                    "STATION NAME", "COUNTRY", "CITY?", "MAIN?", "AIR?", "TIME ZONE");
            System.out.println(ANSI_BOLD + ANSI_PURPLE + "-".repeat(82) + ANSI_RESET);

            // --- Page Content ---
            for (int i = startIndex; i < endIndex; i++) {
                EuropeanStation s = results.get(i);
                System.out.printf("  %-30s | %s%-7s%s | %-5s | %-5s | %-5s | %-15s %n",
                        s.getStation().length() > 29 ? s.getStation().substring(0, 27) + "..." : s.getStation(), // Truncate long names
                        ANSI_CYAN, s.getCountry(), ANSI_RESET,
                        s.isCity() ? "Yes" : "No",
                        s.isMainStation() ? "Yes" : "No",
                        s.isAirport() ? "Yes" : "No",
                        s.getTimeZoneGroup()
                );
            }
            System.out.println(ANSI_BOLD + ANSI_PURPLE + "-".repeat(82) + ANSI_RESET);

            // --- Controls ---
            System.out.println("\n" + ANSI_BOLD + "Controls:" + ANSI_RESET);
            String prev = (currentPage > 0) ? "[P]rev Page" : "           ";
            String next = (currentPage < totalPages - 1) ? "[N]ext Page" : "           ";
            System.out.printf("  %s   |   %s   |   [E]xit Query%n", prev, next);

            input = readString(ANSI_BOLD + "➡️  Choose an option: " + ANSI_RESET).toUpperCase();

            if (input.equals("N") && currentPage < totalPages - 1) {
                currentPage++;
            } else if (input.equals("P") && currentPage > 0) {
                currentPage--;
            }

        } while (!input.equals("E"));

        showInfo("Exited query view.");
    }
}