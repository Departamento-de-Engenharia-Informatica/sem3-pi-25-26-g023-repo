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
import pt.ipp.isep.dei.domain.UpgradePlanService;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Main {

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_BOLD = "\u001B[1m";

    public static void main(String[] args) {
        try {
            InventoryManager manager = new InventoryManager();
            Inventory inventory = manager.getInventory();
            Quarantine quarantine = new Quarantine();
            AuditLog auditLog = new AuditLog("audit.log");

            System.out.println(ANSI_BOLD + "Loading system data... Please wait." + ANSI_RESET);

            printLoadStep("Loading ESINF (Sprint 1) data...");
            manager.loadItems("src/main/java/pt/ipp/isep/dei/FicheirosCSV/items.csv");
            printLoadStep(String.format("  > Loaded %d items", manager.getItemsCount()), true);

            manager.loadBays("src/main/java/pt/ipp/isep/dei/FicheirosCSV/bays.csv");
            printLoadStep(String.format("  > Loaded %d bays across %d warehouses", manager.getBaysCount(), manager.getWarehouseCount()), true);

            List<Wagon> wagons = manager.loadWagons("src/main/java/pt/ipp/isep/dei/FicheirosCSV/wagons.csv");
            printLoadStep(String.format("  > Loaded %d wagons", manager.getWagonsCount()), true);

            WMS wms = new WMS(quarantine, inventory, auditLog, manager.getWarehouses());

            printLoadStep("Unloading wagons into inventory...");
            WMS.UnloadResult unloadResult = wms.unloadWagons(wagons);
            printLoadStep(String.format("  > Unloaded %d wagons (%d boxes). (Full: %d, Partial: %d, Failed: %d)",
                    unloadResult.totalProcessed, unloadResult.totalBoxes,
                    unloadResult.fullyUnloaded, unloadResult.partiallyUnloaded, unloadResult.notUnloaded), true);

            List<Return> returns = manager.loadReturns("src/main/java/pt/ipp/isep/dei/FicheirosCSV/returns.csv");
            for (Return r : returns) {
                quarantine.addReturn(r);
            }
            printLoadStep(String.format("  > Loaded %d returns into quarantine", manager.getReturnsCount()), true);

            List<Order> orders = manager.loadOrders(
                    "src/main/java/pt/ipp/isep/dei/FicheirosCSV/orders.csv",
                    "src/main/java/pt/ipp/isep/dei/FicheirosCSV/order_lines.csv"
            );
            printLoadStep(String.format("  > Loaded %d orders with lines", manager.getOrdersCount()), true);

            printLoadStep("Loading LAPR3 (Sprint 1) components...");
            StationRepository estacaoRepo = new StationRepository();
            LocomotiveRepository locomotivaRepo = new LocomotiveRepository();

            SegmentLineRepository segmentoRepo = new SegmentLineRepository();

            segmentoRepo.cleanDatabaseData();

            String linesFile = "src/main/java/pt/ipp/isep/dei/FicheirosCSV/lines.csv";
            printLoadStep("Injecting USEI11 Data from " + linesFile + "...");

            loadSegmentsIntoRepo(linesFile, segmentoRepo);

// ---------------------------------------------------------
            UpgradePlanService upgradeService = new UpgradePlanService();
            RailwayNetworkService networkService = new RailwayNetworkService(estacaoRepo, segmentoRepo);
            TravelTimeController travelTimeController = new TravelTimeController(
                    estacaoRepo, locomotivaRepo, networkService, segmentoRepo,upgradeService
            );
            printLoadStep("  > LAPR3 components initialized.", true);

            printLoadStep("Initializing Dispatcher dependencies...");
            WagonRepository wagonRepo = new WagonRepository();
            TrainRepository trainRepo = new TrainRepository();
            FacilityRepository facilityRepo = new FacilityRepository();

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

            printLoadStep("Initializing Dispatcher Service (USLP07 Simulation)...");
            DispatcherService dispatcherService = new DispatcherService(
                    trainRepo,
                    networkService,
                    facilityRepo,
                    locomotivaRepo,
                    schedulerService
            );
            printLoadStep("  > USLP07 Dispatcher Service ready.", true);

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

            printLoadStep("Initializing USEI12 - Minimal Backbone Network...");
            printLoadStep("  > USEI12 components ready for Belgian railway network", true);

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

    private static KDTree buildSpatialKDTree(List<EuropeanStation> stations) {
        List<EuropeanStation> stationsByLat = new ArrayList<>(stations);
        List<EuropeanStation> stationsByLon = new ArrayList<>(stations);

        stationsByLat.sort(Comparator.comparingDouble(EuropeanStation::getLatitude));
        stationsByLon.sort(Comparator.comparingDouble(EuropeanStation::getLongitude));

        KDTree tree = new KDTree();
        tree.buildBalanced(stationsByLat, stationsByLon);
        return tree;
    }

    private static void printLoadStep(String message, boolean success) {
        String color = success ? ANSI_GREEN : ANSI_RED;
        String symbol = success ? "✅" : "❌";
        System.out.println(color + " " + symbol + " " + message + ANSI_RESET);
    }

    private static void printLoadStep(String message) {
        System.out.println(ANSI_CYAN + " ⚙️  " + message + ANSI_RESET);
    }
    // Método auxiliar para carregar linhas diretamente para o Repositório
    private static void loadSegmentsDirectly(String filePath, SegmentLineRepository repo) {
        System.out.println("   -> Reading segment lines from: " + filePath);
        int count = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            br.readLine(); // Skip header
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] p = line.split(",");
                if (p.length >= 5) { // Garantir que tem colunas suficientes
                    try {
                        int u = Integer.parseInt(p[0].trim());
                        int v = Integer.parseInt(p[1].trim());
                        double dist = Double.parseDouble(p[2].trim());
                        // capacity p[3]
                        double cost = Double.parseDouble(p[4].trim());

                        // Ajuste aqui conforme o construtor do seu LineSegment
                        // Exemplo: new LineSegment(id, from, to, dist, cost...)
                        // Se o seu construtor for simples:
                        LineSegment seg = new LineSegment(count++, u, v, dist, cost);
                        repo.save(seg);
                        count++;
                    } catch (Exception e) {
                        // Ignora linhas mal formatadas silenciosamente ou com print suave
                    }
                }
            }
            System.out.println("✅ Injected " + count + " segments into Repository.");
        } catch (IOException e) {
            System.out.println("❌ Error reading lines: " + e.getMessage());
        }
    }
    // Copie isto para o final da classe Main, antes do último }
    private static void loadSegmentsIntoRepo(String filePath, SegmentLineRepository repo) {
        int count = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            br.readLine(); // Saltar cabeçalho
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] p = line.split(",");
                if (p.length >= 5) { // Garante que a linha tem dados suficientes
                    try {
                        // IDs das estações
                        int u = Integer.parseInt(p[0].trim());
                        int v = Integer.parseInt(p[1].trim());
                        double dist = Double.parseDouble(p[2].trim());
                        double cost = Double.parseDouble(p[4].trim());

                        // Cria o segmento e guarda no Repositório
                        // (O 'save' que criámos há pouco no Repositório vai guardar isto em memória)
                        LineSegment seg = new LineSegment(count, u, v, dist, cost);
                        repo.save(seg);

                        count++;
                    } catch (NumberFormatException e) {
                        // Ignora linhas com erros de formatação
                    }
                }
            }
            System.out.println(ANSI_GREEN + "   ✅ SUCCESS: Loaded " + count + " segments into Repository." + ANSI_RESET);
        } catch (IOException e) {
            System.out.println(ANSI_RED + "   ❌ ERROR: Could not read lines.csv: " + e.getMessage() + ANSI_RESET);
        }
    }
}