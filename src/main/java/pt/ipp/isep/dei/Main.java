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
     */
    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("🚆 Railway Cargo Handling Terminal System");
        System.out.println("=========================================");

        try {
            // 1️⃣ ESINF (Sprint 1) Components
            InventoryManager manager = new InventoryManager();
            Inventory inventory = manager.getInventory();
            Quarantine quarantine = new Quarantine();
            AuditLog auditLog = new AuditLog("audit.log");

            // 2️⃣ Load ESINF (Sprint 1) CSVs
            System.out.println("Loading product items (Sprint 1)...");
            manager.loadItems("src/main/java/pt/ipp/isep/dei/FicheirosCSV/items.csv");
            System.out.println("Loading warehouse bays (Sprint 1)...");
            var bays = manager.loadBays("src/main/java/pt/ipp/isep/dei/FicheirosCSV/bays.csv");
            System.out.printf("Loaded %d bays across %d warehouses.%n", bays.size(), manager.getWarehouses().size());
            System.out.println("Loading wagons and boxes (Sprint 1)...");
            var wagons = manager.loadWagons("src/main/java/pt/ipp/isep/dei/FicheirosCSV/wagons.csv");
            System.out.printf("Loaded %d wagons.%n", wagons.size());

            // 3️⃣ Create WMS (Warehouse Management System)
            WMS wms = new WMS(quarantine, inventory, auditLog, manager.getWarehouses());

            // 4️⃣ USEI01 - Unload Wagons (Sprint 1)
            System.out.println("Unloading wagons into warehouses and inventory...");
            wms.unloadWagons(wagons);

            // 5️⃣ USEI05 - Load Returns (Sprint 1)
            System.out.println("Loading returns into quarantine...");
            List<Return> returns = manager.loadReturns("src/main/java/pt/ipp/isep/dei/FicheirosCSV/returns.csv");
            for (Return r : returns) {
                quarantine.addReturn(r);
            }
            System.out.printf("%d returns loaded into quarantine.%n", quarantine.size());


            // 6️⃣ Load ESINF Orders (Sprint 1)
            System.out.println("Loading orders...");
            // (Não é preciso guardar, a UI carrega-os quando precisa)
            manager.loadOrders(
                    "src/main/java/pt/ipp/isep/dei/FicheirosCSV/orders.csv",
                    "src/main/java/pt/ipp/isep/dei/FicheirosCSV/order_lines.csv"
            );


            // 7️⃣ *** LAPR3 (Sprint 1) COMPONENTS ***
            System.out.println("Initializing LAPR3/BDDAD Mock Repositories...");
            StationRepository estacaoRepo = new StationRepository();
            LocomotiveRepository locomotivaRepo = new LocomotiveRepository();
            SegmentLineRepository segmentoRepo = new SegmentLineRepository();

            RailwayNetworkService networkService = new RailwayNetworkService(estacaoRepo, segmentoRepo);

            TravelTimeController travelTimeController = new TravelTimeController(
                    estacaoRepo,
                    locomotivaRepo,
                    networkService,
                    segmentoRepo
            );
            System.out.println("LAPR3 components initialized.");


            // 8️⃣ *** ESINF (SPRINT 2) COMPONENTS ***
            System.out.println("\nInitializing ESINF (Sprint 2) Components...");
            StationIndexManager stationIndexManager = new StationIndexManager();

            // Carrega o novo CSV de estações europeias
            // *** LINHA CORRIGIDA ***
            List<EuropeanStation> europeanStations = manager.loadEuropeanStations("src/main/java/pt/ipp/isep/dei/FicheirosCSV/train_stations_europe.csv");

            // Constrói os índices da USEI06 no arranque
            stationIndexManager.buildIndexes(europeanStations);


            // 9️⃣ Launch Textual Interface
            CargoHandlingUI cargoMenu = new CargoHandlingUI(
                    wms, manager, wagons,
                    travelTimeController, estacaoRepo, locomotivaRepo,
                    stationIndexManager // Passa o novo manager para a UI
            );
            cargoMenu.run();

            System.out.println("\nSystem terminated normally.");
        } catch (Exception e) {
            System.err.println("❌ Fatal error during startup: " + e.getMessage());
            e.printStackTrace();
        }
    }
}