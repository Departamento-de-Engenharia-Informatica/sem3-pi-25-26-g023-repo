package pt.ipp.isep.dei.UI;

import pt.ipp.isep.dei.controller.TravelTimeController;
import pt.ipp.isep.dei.domain.*;
import pt.ipp.isep.dei.repository.StationRepository;
import pt.ipp.isep.dei.repository.LocomotiveRepository;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Main User Interface for the Cargo Handling Terminal.
 * (Version 2.0 - "User-Friendly Console")
 */
public class CargoHandlingUI implements Runnable {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_BOLD = "\u001B[1m";
    public static final String ANSI_ITALIC = "\u001B[3m";

    // --- Sprint 1 Components (WMS) ---
    private final WMS wms;
    private final InventoryManager manager;
    private final List<Wagon> wagons;

    // --- Sprint 1 Components (LAPR3) ---
    private final TravelTimeController travelTimeController;
    private final StationRepository estacaoRepo;
    private final LocomotiveRepository locomotivaRepo;

    // --- Sprint 2 Components (ESINF) ---
    private final StationIndexManager stationIndexManager;

    // --- UI State ---
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
     * Now with a try-catch block for robustness.
     */
    @Override
    public void run() {
        int option = -1;

        do {
            showMenu();
            try {
                // Read option
                option = readInt(0, 9, ANSI_BOLD + "Option: " + ANSI_RESET);

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
        // Add more S2 options (22-27) here as needed

        // --- Info ---
        System.out.println("\n" + ANSI_BOLD + ANSI_PURPLE + "--- System Information ---" + ANSI_RESET);
        System.out.println(ANSI_GREEN + " 8. " + ANSI_RESET + "View Current Inventory");
        System.out.println(ANSI_GREEN + " 9. " + ANSI_RESET + "View Warehouse Info");

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
     * (Kept your original logic)
     *
     * @param option The integer option selected by the user.
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
                handleViewInventory();
                break;
            case 9:
                handleViewWarehouseInfo();
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


    // --- [USEI01] Handler ---
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

    // --- [USEI05] Handler ---
    private void handleProcessReturns() {
        showInfo("--- [USEI05] Process Quarantine Returns (LIFO) ---");
        wms.processReturns();
        showSuccess("Return processing complete.");
        showInfo("Check 'audit.log' for details.");
    }

    // --- [USEI02] Handler ---
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

        // Get Mode from user
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

        // Run allocation and store the result
        this.lastAllocationResult = allocator.allocateOrders(orders, currentInventoryState, mode);
        // Invalidate the next step's plan
        this.lastPickingPlan = null;

        showSuccess("USEI02 executed successfully!");
        System.out.printf("📊 Results: %d allocations generated, %d lines processed%n",
                lastAllocationResult.allocations.size(), lastAllocationResult.eligibilityList.size());
    }

    // --- [USEI03] Handler ---
    private void handlePackTrolleys() {
        showInfo("--- [USEI03] Pack Allocations into Trolleys ---");

        // DEPENDENCY CHECK
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

        // Run packing and store the result
        this.lastPickingPlan = service.generatePickingPlan(
                this.lastAllocationResult.allocations,
                capacity,
                heuristic
        );

        System.out.println("\n" + ANSI_BOLD + "=".repeat(60) + ANSI_RESET);
        System.out.println(ANSI_BOLD + "           📊 RESULTS USEI03 - Picking Plan" + ANSI_RESET);
        System.out.println(ANSI_BOLD + "=".repeat(60) + ANSI_RESET);
        System.out.println(lastPickingPlan.getSummary()); // Make sure .getSummary() is formatted nicely
    }

    // --- [USEI04] Handler ---
    private void handleCalculatePickingPath() {
        showInfo("--- [USEI04] Calculate Picking Path ---");

        // DEPENDENCY CHECK
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
                    System.out.println(result); // Make sure PathResult.toString() is formatted nicely
                    System.out.println("-".repeat(40));
                });
                showSuccess("USEI04 completed successfully!");
            }

        } catch (Exception e) {
            showError("Error calculating picking paths (USEI04): " + e.getMessage());
        }
    }

    // --- [USLP03] Handler ---
    private void handleCalculateTravelTime() {
        showInfo("--- [USLP03] Calculate Travel Time ---");
        // This is a sub-UI, so we call its .run() method
        // You can apply the same "pretty" upgrade to TravelTimeUI.java
        TravelTimeUI travelTimeUI = new TravelTimeUI(travelTimeController, estacaoRepo, locomotivaRepo);
        travelTimeUI.run();
        showSuccess("Module [USLP03] complete.");
    }

    // --- [USEI06] Handler (CORRECTED) ---
    // This now calls the methods from *your* repository's StationIndexManager
    private void handleQueryStationIndex() {
        showInfo("--- [USEI06] Query European Station Index ---");
        System.out.println("The 64k station index is loaded.");
        System.out.println("What type of query would you like to perform?");
        System.out.println(ANSI_GREEN + " 1. " + ANSI_RESET + "By Time Zone Group");
        System.out.println(ANSI_GREEN + " 2. " + ANSI_RESET + "By Time Zone Window (Range)");
        System.out.println(ANSI_YELLOW + " 0. " + ANSI_RESET + "Cancel");

        int choice = readInt(0, 2, "Option: ");

        switch (choice) {
            case 1:
                String tzg = readString(ANSI_BOLD + "➡️  Enter Time Zone Group (e.g., CET, WET/GMT) [c=Cancel]: " + ANSI_RESET);
                if (isCancel(tzg)) {
                    showInfo("Query cancelled.");
                    break;
                }

                // --- CORRECTED METHOD CALL ---
                List<EuropeanStation> stations = stationIndexManager.getStationsByTimeZoneGroup(tzg.toUpperCase());

                if (stations.isEmpty()) {
                    showInfo(String.format("No stations found for group '%s'.", tzg));
                } else {
                    showSuccess(String.format("Found %d stations for group '%s' (sorted by country and name):", stations.size(), tzg.toUpperCase()));
                    for (EuropeanStation s : stations) {
                        System.out.printf("  -> %s [%s]\n", s.getStation(), s.getCountry());
                    }
                }
                break;

            case 2:
                String tzgMin = readString(ANSI_BOLD + "➡️  Enter minimum TZG (e.g., CET) [c=Cancel]: " + ANSI_RESET);
                if (isCancel(tzgMin)) {
                    showInfo("Query cancelled.");
                    break;
                }
                String tzgMax = readString(ANSI_BOLD + "➡️  Enter maximum TZG (e.g., WET/GMT) [c=Cancel]: " + ANSI_RESET);
                if (isCancel(tzgMax)) {
                    showInfo("Query cancelled.");
                    break;
                }

                // --- CORRECTED METHOD CALL ---
                List<EuropeanStation> stationsWindow = stationIndexManager.getStationsInTimeZoneWindow(tzgMin.toUpperCase(), tzgMax.toUpperCase());

                if (stationsWindow.isEmpty()) {
                    showInfo(String.format("No stations found in range ['%s', '%s'].", tzgMin.toUpperCase(), tzgMax.toUpperCase()));
                } else {
                    showSuccess(String.format("Found %d stations in range ['%s', '%s']:", stationsWindow.size(), tzgMin.toUpperCase(), tzgMax.toUpperCase()));
                    for (EuropeanStation s : stationsWindow) {
                        System.out.printf("  -> [%s] %s (%s)\n", s.getTimeZoneGroup(), s.getStation(), s.getCountry());
                    }
                }
                break;

            case 0:
                showInfo("Query cancelled.");
                break;
        }
    }


    // --- Info Handlers ---
    private void handleViewInventory() {
        showInfo("--- Current Inventory Contents ---");
        List<Box> boxes = manager.getInventory().getBoxes();
        if (boxes.isEmpty()) {
            showInfo("Inventory is empty.");
        } else {
            System.out.printf(ANSI_BOLD + "Displaying %d boxes (Sorted by FEFO/FIFO):%n" + ANSI_RESET, boxes.size());
            for (Box b : boxes) {
                System.out.println("  " + b); // Relies on Box.toString()
            }
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
        }
    }

    // --- Robust Input Helpers (from your file, with colored errors) ---
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
}