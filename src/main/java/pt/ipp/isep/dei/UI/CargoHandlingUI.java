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
 * (Atualizada para Sprint 2)
 */
public class CargoHandlingUI implements Runnable {

    // --- Componentes Sprint 1 (WMS) ---
    private final WMS wms;
    private final InventoryManager manager;
    private final List<Wagon> wagons;

    // --- Componentes Sprint 1 (LAPR3) ---
    private final TravelTimeController travelTimeController;
    private final StationRepository estacaoRepo;
    private final LocomotiveRepository locomotivaRepo;

    // --- Componentes Sprint 2 (ESINF) ---
    private final StationIndexManager stationIndexManager; // NOVO

    // --- Estado da UI ---
    private AllocationResult lastAllocationResult = null;
    private PickingPlan lastPickingPlan = null;
    private final Scanner scanner;

    /**
     * Construtor atualizado para o Sprint 2.
     */
    public CargoHandlingUI(WMS wms, InventoryManager manager, List<Wagon> wagons,
                           TravelTimeController travelTimeController, StationRepository estacaoRepo,
                           LocomotiveRepository locomotivaRepo,
                           StationIndexManager stationIndexManager /* NOVO */) {
        this.wms = wms;
        this.manager = manager;
        this.wagons = wagons;
        this.travelTimeController = travelTimeController;
        this.estacaoRepo = estacaoRepo;
        this.locomotivaRepo = locomotivaRepo;
        this.stationIndexManager = stationIndexManager; // NOVO
        this.scanner = new Scanner(System.in);
    }


    /**
     * Runs the main menu loop for the Cargo Handling Terminal.
     */
    @Override
    public void run() {
        int option = -1;

        do {
            showMenu();
            try {
                option = readInt(0, 9, "> Please choose an option: "); // Range atualizado para 0-9
                handleOption(option);

            } catch (Exception e) {
                System.out.println("\n❌ Unexpected error: " + e.getMessage());
            }

        } while (option != 0);

        System.out.println("Closing scanner. Goodbye!");
        scanner.close();
    }

    /**
     * Displays the main menu options to the console.
     */
    private void showMenu() {
        System.out.println("\n=========================================");
        System.out.println("   🚂 Cargo Handling Terminal Menu   ");
        System.out.println("=========================================");
        System.out.println("--- Warehouse Setup (Sprint 1) ---");
        System.out.println(" 1. [USEI01] Unload Wagons");
        System.out.println(" 2. [USEI05] Process Quarantine Returns");

        System.out.println("\n--- Picking Workflow (Sprint 1) ---");
        System.out.println(" 3. [USEI02] Allocate Orders");
        System.out.println(" 4. [USEI03] Pack Trolleys (Run US02 first)");
        System.out.println(" 5. [USEI04] Calculate Pick Path (Run US03 first)");

        System.out.println("\n--- Railway & Station Ops (S1 & S2) ---");
        System.out.println(" 6. [USLP03] Calculate Train Travel Time (S1)");
        System.out.println(" 7. [USEI06] Query European Station Index (S2)"); // NOVO

        System.out.println("\n--- (Warehouse Info) ---");
        System.out.println(" 8. View Current Inventory");
        System.out.println(" 9. View Warehouse Info");

        System.out.println("-----------------------------------------");
        System.out.println(" 0. Exit");
        System.out.println("=========================================");

        // Show status of dependencies
        String allocStatus = (lastAllocationResult != null && !lastAllocationResult.allocations.isEmpty()) ?
                String.format("GENERATED (%d allocations)", lastAllocationResult.allocations.size()) : "NOT-RUN";
        String planStatus = (lastPickingPlan != null) ?
                String.format("GENERATED (%d trolleys)", lastPickingPlan.getTotalTrolleys()) : "NOT-RUN";

        System.out.printf("   Status: [Allocations: %s] [Picking Plan: %s]%n", allocStatus, planStatus);
    }

    /**
     * Handles the menu option selected by the user.
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
                handleQueryStationIndex(); // NOVO (USEI06)
                break;
            case 8:
                handleViewInventory();
                break;
            case 9:
                handleViewWarehouseInfo();
                break;
            case 0:
                System.out.println("\nExiting Cargo Handling Menu... 👋");
                break;
            default:
                // This case should not be reachable due to readInt() validation
                System.out.println("\n❌ Invalid option. Please select a valid number from the menu.\n");
                break;
        }
    }

    // --- [USEI01] Handler ---
    private void handleUnloadWagons() {
        System.out.println("\n--- [USEI01] Unload Wagons ---");
        System.out.println("1. Unload ALL wagons");
        System.out.println("2. Select wagons manually");
        System.out.println("0. Cancel");

        int sub = readInt(0, 2, "> Option: ");

        if (sub == 1) {
            wms.unloadWagons(wagons);
        } else if (sub == 2) {
            for (int i = 0; i < wagons.size(); i++) {
                System.out.printf("%d. Wagon %s (%d boxes)%n",
                        i + 1, wagons.get(i).getWagonId(), wagons.get(i).getBoxes().size());
            }
            String choicesStr = readString("> Enter wagon numbers (comma-separated) [c=Cancel]: ");
            if (isCancel(choicesStr)) {
                System.out.println("Unloading cancelled.");
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
        } else {
            System.out.println("Unloading cancelled.");
        }
    }

    // --- [USEI05] Handler ---
    private void handleProcessReturns() {
        System.out.println("\n--- [USEI05] Process Quarantine Returns (LIFO) ---");
        wms.processReturns();
        System.out.println("✅ Return processing complete.");
    }

    // --- [USEI02] Handler ---
    private void handleAllocateOrders() {
        System.out.println("\n--- [USEI02] Allocate Open Orders ---");

        List<Order> orders;
        try {
            orders = manager.loadOrders(
                    "src/main/java/pt/ipp/isep/dei/FicheirosCSV/orders.csv",
                    "src/main/java/pt/ipp/isep/dei/FicheirosCSV/order_lines.csv"
            );
        } catch (Exception e) {
            System.out.println("❌ ERROR: Failed to load orders: " + e.getMessage());
            return;
        }

        List<Box> currentInventoryState = new ArrayList<>(manager.getInventory().getBoxes());

        if (orders.isEmpty()) {
            System.out.println("❌ ERROR: No valid orders found to process.");
            return;
        }
        if (currentInventoryState.isEmpty()) {
            System.out.println("❌ ERROR: Inventory is empty. Cannot allocate orders.");
            return;
        }

        System.out.printf("ℹ️  Data loaded: %d orders, %d boxes in inventory%n",
                orders.size(), currentInventoryState.size());

        // Get Mode from user
        System.out.println("\nSelect Allocation Mode:");
        System.out.println(" 1. STRICT (All or nothing per line)");
        System.out.println(" 2. PARTIAL (Allocate available stock)");
        System.out.println(" 0. Cancel");
        int modeChoice = readInt(0, 2, "> Option: ");

        if (modeChoice == 0) {
            System.out.println("Allocation cancelled.");
            return;
        }
        OrderAllocator.Mode mode = (modeChoice == 1) ? OrderAllocator.Mode.STRICT : OrderAllocator.Mode.PARTIAL;

        OrderAllocator allocator = new OrderAllocator();
        allocator.setItems(manager.getItemsMap());

        // Run allocation and store the result
        this.lastAllocationResult = allocator.allocateOrders(orders, currentInventoryState, mode);
        // Invalidate the next step's plan
        this.lastPickingPlan = null;

        System.out.println("\n✅ USEI02 executed successfully!");
        System.out.printf("📊 Results: %d allocations generated, %d lines processed%n",
                lastAllocationResult.allocations.size(), lastAllocationResult.eligibilityList.size());
    }

    // --- [USEI03] Handler ---
    private void handlePackTrolleys() {
        System.out.println("\n--- [USEI03] Pack Allocations into Trolleys ---");

        // DEPENDENCY CHECK
        if (this.lastAllocationResult == null || this.lastAllocationResult.allocations.isEmpty()) {
            System.out.println("❌ ERROR: You must run [3. USEI02] Allocate Open Orders first.");
            System.out.println("   No valid allocations are available to be packed.");
            return;
        }

        System.out.printf("ℹ️  Ready to pack %d allocations.%n", this.lastAllocationResult.allocations.size());

        double capacity = readDouble(0.1, Double.MAX_VALUE, "➡️  Trolley capacity (kg) [0=Cancel]: ");
        if (capacity == 0) {
            System.out.println("Packing cancelled.");
            return;
        }

        System.out.println("\n🧠 Available Heuristics:");
        System.out.println(" 1. FIRST_FIT - First one that fits (fastest)");
        System.out.println(" 2. FIRST_FIT_DECREASING - Largest first (more efficient)");
        System.out.println(" 3. BEST_FIT_DECREASING - Best fit (optimizes space)");
        System.out.println(" 0. Cancel");
        int heuristicChoice = readInt(0, 3, "➡️  Choose heuristic (0-3): ");

        HeuristicType heuristic;
        switch(heuristicChoice) {
            case 1: heuristic = HeuristicType.FIRST_FIT; break;
            case 2: heuristic = HeuristicType.FIRST_FIT_DECREASING; break;
            case 3: heuristic = HeuristicType.BEST_FIT_DECREASING; break;
            case 0:
                System.out.println("Packing cancelled."); return;
            default:
                System.out.println("⚠️  Invalid choice. Using FIRST_FIT.");
                heuristic = HeuristicType.FIRST_FIT;
                break;
        }

        System.out.println("\n⚙️  Executing USEI03...");
        PickingService service = new PickingService();
        service.setItemsMap(manager.getItemsMap());

        // Run packing and store the result
        this.lastPickingPlan = service.generatePickingPlan(
                this.lastAllocationResult.allocations,
                capacity,
                heuristic
        );

        System.out.println("\n" + "=".repeat(60));
        System.out.println("           📊 RESULTS USEI03 - Picking Plan");
        System.out.println("=".repeat(60));
        System.out.println(lastPickingPlan.getSummary());
    }

    // --- [USEI04] Handler ---
    private void handleCalculatePickingPath() {
        System.out.println("\n--- [USEI04] Calculate Picking Path ---");

        // DEPENDENCY CHECK
        if (this.lastPickingPlan == null) {
            System.out.println("❌ ERROR: You must run [4. USEI03] Pack Allocations into Trolleys first.");
            System.out.println("   No picking plan is available to calculate a path.");
            return;
        }
        if (this.lastPickingPlan.getTotalTrolleys() == 0) {
            System.out.println("ℹ️  The current picking plan has 0 trolleys. Nothing to calculate.");
            return;
        }

        System.out.printf("ℹ️  Calculating paths for %d trolleys in Plan %s...%n",
                this.lastPickingPlan.getTotalTrolleys(), this.lastPickingPlan.getId());

        PickingPathService pathService = new PickingPathService();
        try {
            Map<String, PickingPathService.PathResult> pathResults = pathService.calculatePickingPaths(this.lastPickingPlan);

            if (pathResults.isEmpty()) {
                System.out.println("Could not calculate paths (check if picking plan has valid locations).");
            } else {
                System.out.println("\n--- Sequencing Results (USEI04) ---");
                pathResults.forEach((strategyName, result) -> {
                    System.out.println("\n" + strategyName + ":");
                    System.out.println(result);
                    System.out.println("-".repeat(40));
                });
                System.out.println("\n✅ USEI04 completed successfully!");
            }

        } catch (Exception e) {
            System.out.println("❌ Error calculating picking paths (USEI04): " + e.getMessage());
        }
    }

    // --- [USLP03] Handler ---
    private void handleCalculateTravelTime() {
        System.out.println("\n--- [USLP03] Calculate Travel Time ---");
        TravelTimeUI travelTimeUI = new TravelTimeUI(travelTimeController, estacaoRepo, locomotivaRepo);
        travelTimeUI.run();
    }

    // --- [USEI06] Handler (NOVO) ---
    private void handleQueryStationIndex() {
        System.out.println("\n--- [USEI06] Query European Station Index ---");
        System.out.println("O índice de 64k estações está carregado.");
        System.out.println("Que tipo de query pretende fazer?");
        System.out.println(" 1. Por Fuso Horário (Time Zone Group)");
        System.out.println(" 2. Por Intervalo (Window) de Fusos Horários");
        System.out.println(" 0. Cancelar");

        int choice = readInt(0, 2, "> Opção: ");

        switch (choice) {
            case 1:
                String tzg = readString("  Insira o Time Zone Group (ex: CET, WET/GMT): ");
                if (isCancel(tzg)) break;

                List<EuropeanStation> stations = stationIndexManager.getStationsByTimeZoneGroup(tzg.toUpperCase());
                System.out.printf("✅ Encontradas %d estações para o grupo '%s' (ordenadas por país e nome):%n", stations.size(), tzg);
                for (EuropeanStation s : stations) {
                    System.out.printf("  - %s (%s)%n", s.getStation(), s.getCountry());
                }
                break;

            case 2:
                String tzgMin = readString("  Insira o TZG mínimo (ex: CET): ");
                if (isCancel(tzgMin)) break;
                String tzgMax = readString("  Insira o TZG máximo (ex: WET/GMT): ");
                if (isCancel(tzgMax)) break;

                List<EuropeanStation> stationsWindow = stationIndexManager.getStationsInTimeZoneWindow(tzgMin.toUpperCase(), tzgMax.toUpperCase());
                System.out.printf("✅ Encontradas %d estações no intervalo ['%s', '%s']:%n", stationsWindow.size(), tzgMin, tzgMax);
                for (EuropeanStation s : stationsWindow) {
                    System.out.printf("  - %s (%s, %s)%n", s.getStation(), s.getCountry(), s.getTimeZoneGroup());
                }
                break;

            case 0:
                System.out.println("Query cancelada.");
                break;
        }
    }


    // --- Info Handlers ---
    private void handleViewInventory() {
        System.out.println("\n--- Current Inventory Contents ---");
        List<Box> boxes = manager.getInventory().getBoxes();
        if (boxes.isEmpty()) {
            System.out.println("Inventory is empty.");
        } else {
            System.out.printf("Displaying %d boxes (Sorted by FEFO/FIFO):%n", boxes.size());
            for (Box b : boxes) {
                System.out.println("  " + b);
            }
        }
        System.out.println();
    }

    private void handleViewWarehouseInfo() {
        showWarehouseInfo(); // Re-use existing private method
    }

    private void showWarehouseInfo() {
        System.out.println("\n--- Warehouse Information ---");
        List<Warehouse> warehouses = manager.getWarehouses();
        if (warehouses.isEmpty()) {
            System.out.println("No warehouses loaded.");
            return;
        }

        for (Warehouse wh : warehouses) {
            System.out.printf("\n🏭 Warehouse: %s%n", wh.getWarehouseId());
            System.out.printf("   📦 Bays: %d%n", wh.getBays().size());

            int totalCapacity = 0;
            int usedCapacity = 0;

            for (Bay bay : wh.getBays()) {
                totalCapacity += bay.getCapacityBoxes();
                usedCapacity += bay.getBoxes().size();
            }

            System.out.printf("   📊 Physical Capacity: %d/%d boxes (%.1f%% full)%n",
                    usedCapacity, totalCapacity,
                    (totalCapacity > 0 ? (usedCapacity * 100.0 / totalCapacity) : 0));
            System.out.printf("   ℹ️  Logical Inventory Size (Total): %d boxes%n", manager.getInventory().getBoxes().size());
        }
        System.out.println();
    }

    // --- Robust Input Helpers ---
    private String readString(String prompt) {
        System.out.print(prompt);
        String line = scanner.nextLine();
        if (isCancel(line)) {
            return "0"; // Universal cancel signal
        }
        return line;
    }

    private int readInt(int min, int max, String prompt) {
        int option = -1;
        System.out.print(prompt);
        while (true) {
            try {
                String line = scanner.nextLine();
                if (isCancel(line)) {
                    return 0; // Universal cancel
                }
                option = Integer.parseInt(line);
                if (option >= min && option <= max) {
                    return option;
                } else {
                    System.out.printf("❌ Invalid input. Please enter a number between %d and %d.%n> ", min, max);
                }
            } catch (NumberFormatException e) {
                System.out.printf("❌ Invalid input. Please enter a number.%n> ");
            }
        }
    }

    private double readDouble(double min, double max, String prompt) {
        double value = -1;
        System.out.print(prompt);
        while (true) {
            try {
                String line = scanner.nextLine();
                if (isCancel(line)) {
                    return 0.0; // Universal cancel
                }
                value = Double.parseDouble(line.replace(',', '.'));
                if (value >= min && value <= max) {
                    return value;
                } else {
                    System.out.printf("❌ Invalid input. Please enter a value between %.2f and %.2f.%n> ", min, max);
                }
            } catch (NumberFormatException e) {
                System.out.printf("❌ Invalid input. Please enter a valid number.%n> ");
            }
        }
    }

    private boolean isCancel(String input) {
        return input.trim().equals("0") || input.trim().equalsIgnoreCase("c");
    }
}