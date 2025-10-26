package pt.ipp.isep.dei;

import pt.ipp.isep.dei.UI.CargoHandlingUI;
import pt.ipp.isep.dei.controller.TravelTimeController;
import pt.ipp.isep.dei.domain.*;
import pt.ipp.isep.dei.repository.StationRepository;
import pt.ipp.isep.dei.repository.LocomotiveRepository;
import pt.ipp.isep.dei.repository.SegmentLineRepository;

import java.util.List;

/**
 * Main entry point for the Railway Cargo Handling Terminal System application.
 * This class initializes all necessary components, loads data from CSV files,
 * sets up repositories and controllers for both ESINF (Warehouse Management)
 * and LAPR3 (Railway Network) domains, and launches the main
 * command-line user interface ({@link CargoHandlingUI}).
 */
public class Main {

    /**
     * The main method that starts the application.
     * It handles the sequential loading of data (items, bays, wagons, returns, orders),
     * initialization of services (WMS, InventoryManager, Repositories),
     * and launching the main UI.
     *
     * @param args Command-line arguments (not used in this application).
     */
    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("🚆 Railway Cargo Handling Terminal System");
        System.out.println("=========================================");

        try {
            // 1️⃣ ESINF Components
            InventoryManager manager = new InventoryManager();
            Inventory inventory = manager.getInventory();
            Quarantine quarantine = new Quarantine();
            AuditLog auditLog = new AuditLog("audit.log");

            // 2️⃣ Load ESINF CSVs
            System.out.println("Loading product items...");
            manager.loadItems("src/main/java/pt/ipp/isep/dei/FicheirosCSV/items.csv");
            System.out.println("Loading warehouse bays...");
            var bays = manager.loadBays("src/main/java/pt/ipp/isep/dei/FicheirosCSV/bays.csv");
            System.out.printf("Loaded %d bays across %d warehouses.%n", bays.size(), manager.getWarehouses().size());
            System.out.println("Loading wagons and boxes...");
            var wagons = manager.loadWagons("src/main/java/pt/ipp/isep/dei/FicheirosCSV/wagons.csv");
            System.out.printf("Loaded %d wagons.%n", wagons.size());

            // 3️⃣ Create WMS (Warehouse Management System)
            WMS wms = new WMS(quarantine, inventory, auditLog, manager.getWarehouses());

            // 4️⃣ USEI01 - Unload Wagons
            System.out.println("Unloading wagons into warehouses and inventory...");
            wms.unloadWagons(wagons);

            // 5️⃣ USEI05 - Process Returns
            System.out.println("Loading returns...");
            List<Return> returns = manager.loadReturns("src/main/java/pt/ipp/isep/dei/FicheirosCSV/returns.csv");
            for (Return r : returns) {
                quarantine.addReturn(r);
            }
            wms.processReturns();

            // 6️⃣ Load ESINF Orders
            System.out.println("Loading orders...");
            var orders = manager.loadOrders(
                    "src/main/java/pt/ipp/isep/dei/FicheirosCSV/orders.csv",
                    "src/main/java/pt/ipp/isep/dei/FicheirosCSV/order_lines.csv"
            );
            System.out.printf("Loaded %d orders.%n", orders.size());


            // 7️⃣ *** MODIFIED LAPR3 COMPONENTS ***
            System.out.println("Initializing LAPR3/BDDAD Mock Repositories...");
            StationRepository estacaoRepo = new StationRepository();
            LocomotiveRepository locomotivaRepo = new LocomotiveRepository();
            SegmentLineRepository segmentoRepo = new SegmentLineRepository();

            // New Network Service
            RailwayNetworkService networkService = new RailwayNetworkService(estacaoRepo, segmentoRepo);

            // *** CHANGE HERE: Pass the segmentoRepo as well ***
            TravelTimeController travelTimeController = new TravelTimeController(
                    estacaoRepo,
                    locomotivaRepo,
                    networkService,
                    segmentoRepo // Passing the extra dependency needed for getDirectConnectionsInfo
            );
            System.out.println("LAPR3 components initialized.");


            // 8️⃣ Launch Textual Interface
            CargoHandlingUI cargoMenu = new CargoHandlingUI(wms, manager, wagons,
                    travelTimeController, estacaoRepo, locomotivaRepo);
            cargoMenu.run();

            System.out.println("\nSystem terminated normally.");
        } catch (Exception e) {
            System.err.println("❌ Fatal error during startup: " + e.getMessage());
            e.printStackTrace();
        }
    }
}