package pt.ipp.isep.dei.UI.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

// Imports de todo o teu backend
import pt.ipp.isep.dei.controller.TravelTimeController;
import pt.ipp.isep.dei.domain.*;
import pt.ipp.isep.dei.repository.StationRepository;
import pt.ipp.isep.dei.repository.LocomotiveRepository;
import pt.ipp.isep.dei.repository.SegmentLineRepository;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MainApplication extends Application {

    // --- BACKEND FIELDS (Brought from Main.java) ---
    private InventoryManager manager;
    private WMS wms;
    private StationIndexManager stationIndexManager;
    private KDTree spatialKDTree;
    private TravelTimeController travelTimeController;

    /**
     * Loads all backend data (copied from Main.java) BEFORE the window opens.
     */
    @Override
    public void init() throws Exception {
        super.init();
        try {
            System.out.println("Loading system data for GUI...");
            // Logic from Main.java
            manager = new InventoryManager();
            Inventory inventory = manager.getInventory();
            Quarantine quarantine = new Quarantine();
            AuditLog auditLog = new AuditLog("audit.log");
            manager.loadItems("src/main/java/pt/ipp/isep/dei/FicheirosCSV/items.csv");
            manager.loadBays("src/main/java/pt/ipp/isep/dei/FicheirosCSV/bays.csv");

            // --- MODIFICATION ---
            // We no longer load or unload wagons on startup.
            // The USEI01 controller will handle this.
            wms = new WMS(quarantine, inventory, auditLog, manager.getWarehouses());
            // List<Wagon> wagons = manager.loadWagons("src/main/java/pt/ipp/isep/dei/FicheirosCSV/wagons.csv"); // <-- DISABLED
            // wms.unloadWagons(wagons); // <-- DISABLED

            List<Return> returns = manager.loadReturns("src/main/java/pt/ipp/isep/dei/FicheirosCSV/returns.csv");
            for (Return r : returns) quarantine.addReturn(r);
            StationRepository estacaoRepo = new StationRepository();
            LocomotiveRepository locomotivaRepo = new LocomotiveRepository();
            SegmentLineRepository segmentoRepo = new SegmentLineRepository();
            UpgradePlanService upgradeService = new UpgradePlanService();

            RailwayNetworkService networkService = new RailwayNetworkService(estacaoRepo, segmentoRepo);
            travelTimeController = new TravelTimeController(estacaoRepo, locomotivaRepo, networkService, segmentoRepo, upgradeService);
            stationIndexManager = new StationIndexManager();
            List<EuropeanStation> europeanStations = manager.loadEuropeanStations("src/main/java/pt/ipp/isep/dei/FicheirosCSV/train_stations_europe.csv");
            stationIndexManager.buildIndexes(europeanStations);
            spatialKDTree = buildSpatialKDTree(europeanStations);
            System.out.println("Data loaded successfully (wagons pending).");
        } catch (Exception e) {
            System.err.println("❌ FATAL ERROR INITIALIZING GUI: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        String fxmlPath = "main-view.fxml";
        String cssPath = "style.css";
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        URL fxmlUrl = classLoader.getResource(fxmlPath);
        if (fxmlUrl == null) {
            System.err.println("CRITICAL ERROR: Could not find: " + fxmlPath);
            throw new IOException("Location is not set: " + fxmlPath);
        }

        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Parent root = loader.load();

        // --- DEPENDENCY INJECTION ---
        MainController mainController = loader.getController();
        if (mainController == null) {
            throw new IOException("Fatal error: fx:controller not found. Check main-view.fxml.");
        }

        // Pass all backend services to the controller
        mainController.setBackendServices(
                wms,
                manager,
                travelTimeController,
                stationIndexManager,
                spatialKDTree
        );
        // --- END OF INJECTION ---

        URL cssUrl = classLoader.getResource(cssPath);
        if (cssUrl != null) {
            root.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("WARNING: CSS file 'style.css' not found.");
        }

        primaryStage.setTitle("Integrated Management (PI - G023)");
        primaryStage.setScene(new Scene(root, 1200, 800));
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(700);
        primaryStage.show();
    }

    /**
     * Helper copied from Main.java to build the KDTree.
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
}