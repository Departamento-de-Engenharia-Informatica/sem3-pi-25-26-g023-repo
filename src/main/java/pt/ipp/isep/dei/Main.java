package pt.ipp.isep.dei;

import pt.ipp.isep.dei.UI.CargoHandlingUI;
import pt.ipp.isep.dei.controller.TravelTimeController;
import pt.ipp.isep.dei.domain.*;
import pt.ipp.isep.dei.repository.StationRepository;
import pt.ipp.isep.dei.repository.LocomotiveRepository;
import pt.ipp.isep.dei.repository.SegmentLineRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Main entry point for the Logistics on Rails application.
 * Version 2.3 - Integrated USEI08 with KD-Tree spatial queries
 * Features clean startup logging, 100% controlled by Main.
 */
public class Main {

    // --- ANSI Color Codes ---
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_BOLD = "\u001B[1m";

    /**
     * Main method that starts the application.
     * Initializes all components, loads data, and launches the UI.
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        try {
            // 1️⃣ Initialize Components (Silent Mode)
            InventoryManager manager = new InventoryManager();
            Inventory inventory = manager.getInventory();
            Quarantine quarantine = new Quarantine();
            AuditLog auditLog = new AuditLog("audit.log");

            // --- Data Loading Block (Controlled) ---
            System.out.println(ANSI_BOLD + "Loading system data... Please wait." + ANSI_RESET);

            // 2️⃣ Load ESINF (Sprint 1)
            printLoadStep("Loading ESINF (Sprint 1) data...");
            manager.loadItems("src/main/java/pt/ipp/isep/dei/FicheirosCSV/items.csv");
            printLoadStep(String.format("  > Loaded %d items", manager.getItemsCount()), true);

            manager.loadBays("src/main/java/pt/ipp/isep/dei/FicheirosCSV/bays.csv");
            printLoadStep(String.format("  > Loaded %d bays across %d warehouses", manager.getBaysCount(), manager.getWarehouseCount()), true);

            List<Wagon> wagons = manager.loadWagons("src/main/java/pt/ipp/isep/dei/FicheirosCSV/wagons.csv");
            printLoadStep(String.format("  > Loaded %d wagons", manager.getWagonsCount()), true);

            // 3️⃣ Create WMS and Load Wagons
            WMS wms = new WMS(quarantine, inventory, auditLog, manager.getWarehouses());

            printLoadStep("Unloading wagons into inventory...");
            // --- MODIFICATION: Capture silent result ---
            WMS.UnloadResult unloadResult = wms.unloadWagons(wagons);
            // Print concise summary
            printLoadStep(String.format("  > Unloaded %d wagons (%d boxes). (Full: %d, Partial: %d, Failed: %d)",
                    unloadResult.totalProcessed, unloadResult.totalBoxes,
                    unloadResult.fullyUnloaded, unloadResult.partiallyUnloaded, unloadResult.notUnloaded), true);

            // 4️⃣ Load Returns
            List<Return> returns = manager.loadReturns("src/main/java/pt/ipp/isep/dei/FicheirosCSV/returns.csv");
            for (Return r : returns) {
                quarantine.addReturn(r);
            }
            printLoadStep(String.format("  > Loaded %d returns into quarantine", manager.getReturnsCount()), true);

            // 5️⃣ Load Orders
            List<Order> orders = manager.loadOrders(
                    "src/main/java/pt/ipp/isep/dei/FicheirosCSV/orders.csv",
                    "src/main/java/pt/ipp/isep/dei/FicheirosCSV/order_lines.csv"
            );
            printLoadStep(String.format("  > Loaded %d orders with lines", manager.getOrdersCount()), true);

            // 6️⃣ Load LAPR3 (Sprint 1)
            printLoadStep("Loading LAPR3 (Sprint 1) components...");
            StationRepository estacaoRepo = new StationRepository();
            LocomotiveRepository locomotivaRepo = new LocomotiveRepository();
            SegmentLineRepository segmentoRepo = new SegmentLineRepository();
            RailwayNetworkService networkService = new RailwayNetworkService(estacaoRepo, segmentoRepo);
            TravelTimeController travelTimeController = new TravelTimeController(
                    estacaoRepo, locomotivaRepo, networkService, segmentoRepo
            );
            printLoadStep("  > LAPR3 components initialized.", true);

            // 7️⃣ Load ESINF (Sprint 2)
            printLoadStep("Loading ESINF (Sprint 2) components...");
            StationIndexManager stationIndexManager = new StationIndexManager();

            // Call silent method
            List<EuropeanStation> europeanStations = manager.loadEuropeanStations("src/main/java/pt/ipp/isep/dei/FicheirosCSV/train_stations_europe.csv");

            // Print "pretty" summary using getters
            String summary = String.format("  > Loaded %d valid stations", manager.getValidStationCount());
            if (manager.getInvalidStationCount() > 0) {
                // Show error summary, but not the errors themselves
                summary += ANSI_YELLOW + String.format(" (%d invalid rows rejected)", manager.getInvalidStationCount()) + ANSI_GREEN;
            }
            printLoadStep(summary, true);

            printLoadStep("Building station indexes (USEI06)...");
            stationIndexManager.buildIndexes(europeanStations); // Call silent method
            printLoadStep("  > All station indexes built.", true); // Main reports success

            // 8️⃣ ✅ NEW: Build KD-Tree for USEI08 Spatial Queries
            printLoadStep("Building balanced KD-Tree for spatial queries (USEI08)...");
            KDTree spatialKDTree = buildSpatialKDTree(europeanStations);
            printLoadStep(String.format("  > KD-Tree built: %d nodes, height: %d, bucket distribution: %s",
                    spatialKDTree.size(), spatialKDTree.height(), spatialKDTree.getBucketSizes()), true);

            // 9️⃣ Launch UI
            System.out.println(ANSI_BOLD + "\nSystem loaded successfully. Launching UI..." + ANSI_RESET);
            Thread.sleep(1000); // Dramatic pause

            CargoHandlingUI cargoMenu = new CargoHandlingUI(
                    wms, manager, wagons,
                    travelTimeController, estacaoRepo, locomotivaRepo,
                    stationIndexManager,
                    spatialKDTree  // ✅ NEW: Pass KD-Tree for USEI08
            );
            cargoMenu.run();

            System.out.println("\nSystem terminated normally.");

        } catch (Exception e) {
            // Fatal startup error
            System.out.println(ANSI_RED + ANSI_BOLD + "❌ FATAL ERROR DURING STARTUP" + ANSI_RESET);
            System.out.println(ANSI_RED + e.getMessage() + ANSI_RESET);
            e.printStackTrace();
        }
    }

    /**
     * Builds a balanced KD-Tree for spatial queries (USEI07-08).
     * Uses pre-sorted lists by latitude and longitude for optimal construction.
     *
     * @param stations list of European stations to build the tree from
     * @return balanced KD-Tree ready for spatial queries
     */
    private static KDTree buildSpatialKDTree(List<EuropeanStation> stations) {
        // Create sorted lists for balanced construction
        List<EuropeanStation> stationsByLat = new ArrayList<>(stations);
        List<EuropeanStation> stationsByLon = new ArrayList<>(stations);

        // Sort by respective coordinates
        stationsByLat.sort(Comparator.comparingDouble(EuropeanStation::getLatitude));
        stationsByLon.sort(Comparator.comparingDouble(EuropeanStation::getLongitude));

        // Build balanced tree
        KDTree tree = new KDTree();
        tree.buildBalanced(stationsByLat, stationsByLon);
        return tree;
    }

    /**
     * Pretty helper for printing loading status with success/failure indicators.
     *
     * @param message the status message to display
     * @param success true for success, false for failure
     */
    private static void printLoadStep(String message, boolean success) {
        String color = success ? ANSI_GREEN : ANSI_RED;
        String symbol = success ? "✅" : "❌";
        System.out.println(color + " " + symbol + " " + message + ANSI_RESET);
    }

    /**
     * Overload for "loading..." messages (without success/failure).
     *
     * @param message the loading message to display
     */
    private static void printLoadStep(String message) {
        System.out.println(ANSI_CYAN + " ⚙️  " + message + ANSI_RESET);
    }
}