package pt.ipp.isep.dei;

import pt.ipp.isep.dei.UI.CargoHandlingUI;
import pt.ipp.isep.dei.controller.TravelTimeController;
import pt.ipp.isep.dei.domain.*;
import pt.ipp.isep.dei.repository.StationRepository;
import pt.ipp.isep.dei.repository.LocomotiveRepository;
import pt.ipp.isep.dei.repository.SegmentLineRepository;
import pt.ipp.isep.dei.controller.SchedulerController;
import pt.ipp.isep.dei.domain.SchedulerService;
import pt.ipp.isep.dei.repository.WagonRepository;
import pt.ipp.isep.dei.repository.TrainRepository;
import pt.ipp.isep.dei.repository.FacilityRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Main entry point for the Logistics on Rails application.
 * Integrates all components from ESINF, LAPR3, and BDDAD modules.
 * Provides comprehensive railway logistics management including warehouse operations,
 * spatial queries, and train scheduling simulations.
 *
 * @version 2.3
 */
public class Main {

    // ANSI Color Codes for console output formatting
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_BOLD = "\u001B[1m";

    /**
     * Main method that initializes and starts the Logistics on Rails application.
     * Coordinates the loading of all system components and data repositories.
     * Handles the complete startup sequence including error management.
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        try {
            // Initialize core components
            InventoryManager manager = new InventoryManager();
            Inventory inventory = manager.getInventory();
            Quarantine quarantine = new Quarantine();
            AuditLog auditLog = new AuditLog("audit.log");

            // Data loading sequence
            System.out.println(ANSI_BOLD + "Loading system data... Please wait." + ANSI_RESET);

            // Load ESINF Sprint 1 data
            printLoadStep("Loading ESINF (Sprint 1) data...");
            manager.loadItems("src/main/java/pt/ipp/isep/dei/FicheirosCSV/items.csv");
            printLoadStep(String.format("  > Loaded %d items", manager.getItemsCount()), true);

            manager.loadBays("src/main/java/pt/ipp/isep/dei/FicheirosCSV/bays.csv");
            printLoadStep(String.format("  > Loaded %d bays across %d warehouses", manager.getBaysCount(), manager.getWarehouseCount()), true);

            List<Wagon> wagons = manager.loadWagons("src/main/java/pt/ipp/isep/dei/FicheirosCSV/wagons.csv");
            printLoadStep(String.format("  > Loaded %d wagons", manager.getWagonsCount()), true);

            // Initialize Warehouse Management System
            WMS wms = new WMS(quarantine, inventory, auditLog, manager.getWarehouses());

            printLoadStep("Unloading wagons into inventory...");
            WMS.UnloadResult unloadResult = wms.unloadWagons(wagons);
            printLoadStep(String.format("  > Unloaded %d wagons (%d boxes). (Full: %d, Partial: %d, Failed: %d)",
                    unloadResult.totalProcessed, unloadResult.totalBoxes,
                    unloadResult.fullyUnloaded, unloadResult.partiallyUnloaded, unloadResult.notUnloaded), true);

            // Load returns data
            List<Return> returns = manager.loadReturns("src/main/java/pt/ipp/isep/dei/FicheirosCSV/returns.csv");
            for (Return r : returns) {
                quarantine.addReturn(r);
            }
            printLoadStep(String.format("  > Loaded %d returns into quarantine", manager.getReturnsCount()), true);

            // Load orders data
            List<Order> orders = manager.loadOrders(
                    "src/main/java/pt/ipp/isep/dei/FicheirosCSV/orders.csv",
                    "src/main/java/pt/ipp/isep/dei/FicheirosCSV/order_lines.csv"
            );
            printLoadStep(String.format("  > Loaded %d orders with lines", manager.getOrdersCount()), true);

            // Initialize LAPR3 components and USLP07 scheduler
            printLoadStep("Loading LAPR3 (Sprint 1) components...");
            StationRepository estacaoRepo = new StationRepository();
            LocomotiveRepository locomotivaRepo = new LocomotiveRepository();
            SegmentLineRepository segmentoRepo = new SegmentLineRepository();
            RailwayNetworkService networkService = new RailwayNetworkService(estacaoRepo, segmentoRepo);
            TravelTimeController travelTimeController = new TravelTimeController(
                    estacaoRepo, locomotivaRepo, networkService, segmentoRepo
            );
            printLoadStep("  > LAPR3 components initialized.", true);

            // Initialize high-level repositories
            printLoadStep("Initializing Dispatcher dependencies...");
            WagonRepository wagonRepo = new WagonRepository();
            TrainRepository trainRepo = new TrainRepository();
            FacilityRepository facilityRepo = new FacilityRepository();

            // Initialize scheduler components
            printLoadStep("Initializing Scheduler components (USLP07)...");
            SchedulerService schedulerService = new SchedulerService(estacaoRepo, facilityRepo);

            SchedulerController schedulerController = new SchedulerController(
                    schedulerService,
                    segmentoRepo,
                    locomotivaRepo,
                    wagonRepo,
                    networkService
            );
            printLoadStep("  > USLP07 Scheduler controller ready.", true);

            // Initialize dispatcher service
            printLoadStep("Initializing Dispatcher Service (USLP07 Simulation)...");
            DispatcherService dispatcherService = new DispatcherService(
                    trainRepo,
                    networkService,
                    facilityRepo,
                    locomotivaRepo,
                    schedulerService
            );
            printLoadStep("  > USLP07 Dispatcher Service ready.", true);

            // Load ESINF Sprint 2 components
            printLoadStep("Loading ESINF (Sprint 2) components...");
            StationIndexManager stationIndexManager = new StationIndexManager();

            List<EuropeanStation> europeanStations =
                    manager.loadEuropeanStations("src/main/java/pt/ipp/isep/dei/FicheirosCSV/train_stations_europe.csv");

            String summary = String.format("  > Loaded %d valid stations", manager.getValidStationCount());
            if (manager.getInvalidStationCount() > 0) {
                summary += ANSI_YELLOW + String.format(" (%d invalid rows rejected)", manager.getInvalidStationCount()) + ANSI_GREEN;
            }
            printLoadStep(summary, true);

            printLoadStep("Building station indexes (USEI06)...");
            stationIndexManager.buildIndexes(europeanStations);
            printLoadStep("  > All station indexes built.", true);

            // Initialize KD-Tree for spatial queries
            printLoadStep("Building balanced KD-Tree for spatial queries (USEI08)...");
            KDTree spatialKDTree = buildSpatialKDTree(europeanStations);
            String bucketInfo = spatialKDTree.getBucketSizes().toString();
            printLoadStep(String.format("  > KD-Tree built: %d nodes, height: %d, bucket distribution: %s",
                    spatialKDTree.size(), spatialKDTree.height(), bucketInfo), true);

            printLoadStep("Initializing Spatial Search Engine (USEI08)...");
            SpatialSearch spatialSearchEngine = new SpatialSearch(spatialKDTree);
            printLoadStep("  > USEI08 Spatial Search ready! Complexity: O(log n) average case", true);

            printLoadStep("Initializing Radius Search Engine (USEI10)...");
            printLoadStep("  > USEI10 Radius Search ready! Complexity: O(sqrt(N) + K log K) average case", true);

            // Launch user interface
            System.out.println(ANSI_BOLD + "\nSystem loaded successfully. Launching UI..." + ANSI_RESET);
            Thread.sleep(1000);

            CargoHandlingUI cargoMenu = new CargoHandlingUI(
                    wms, manager, wagons,
                    travelTimeController, estacaoRepo, locomotivaRepo,
                    stationIndexManager,
                    spatialKDTree,
                    spatialSearchEngine,
                    schedulerController,
                    dispatcherService,
                    facilityRepo
            );
            cargoMenu.run();

            System.out.println("\nSystem terminated normally.");

        } catch (Exception e) {
            System.out.println(ANSI_RED + ANSI_BOLD + "❌ FATAL ERROR DURING STARTUP" + ANSI_RESET);
            System.out.println(ANSI_RED + e.getMessage() + ANSI_RESET);
            e.printStackTrace();
        }
    }

    /**
     * Builds a balanced KD-Tree for spatial queries from European station data.
     * Uses pre-sorted lists by latitude and longitude for optimal tree construction.
     *
     * @param stations list of European stations to build the tree from
     * @return balanced KD-Tree instance
     */
    private static KDTree buildSpatialKDTree(List<EuropeanStation> stations) {
        List<EuropeanStation> stationsByLat = new ArrayList<>(stations);
        List<EuropeanStation> stationsByLon = new ArrayList<>(stations);

        stationsByLat.sort(Comparator.comparingDouble(EuropeanStation::getLatitude));
        stationsByLon.sort(Comparator.comparingDouble(EuropeanStation::getLongitude));

        KDTree tree = new KDTree();
        tree.buildBalanced(stationsByLat, stationsByLon);
        return tree;
    }

    /**
     * Prints a loading step message with success/failure indication.
     *
     * @param message the message to display
     * @param success true for success indication, false for failure
     */
    private static void printLoadStep(String message, boolean success) {
        String color = success ? ANSI_GREEN : ANSI_RED;
        String symbol = success ? "✅" : "❌";
        System.out.println(color + " " + symbol + " " + message + ANSI_RESET);
    }

    /**
     * Prints a loading step message without success/failure indication.
     *
     * @param message the message to display
     */
    private static void printLoadStep(String message) {
        System.out.println(ANSI_CYAN + " ⚙️  " + message + ANSI_RESET);
    }
}