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

    // --- CAMPOS DO BACKEND (Trazidos de Main.java) ---
    private InventoryManager manager;
    private WMS wms;
    private StationIndexManager stationIndexManager;
    private KDTree spatialKDTree;
    private TravelTimeController travelTimeController;

    /**
     * Carrega todo o backend (copiado de Main.java) ANTES da janela abrir.
     */
    @Override
    public void init() throws Exception {
        super.init();
        try {
            System.out.println("A carregar dados do sistema para a GUI...");
            // Lógica de Main.java
            manager = new InventoryManager();
            Inventory inventory = manager.getInventory();
            Quarantine quarantine = new Quarantine();
            AuditLog auditLog = new AuditLog("audit.log");
            manager.loadItems("src/main/java/pt/ipp/isep/dei/FicheirosCSV/items.csv");
            manager.loadBays("src/main/java/pt/ipp/isep/dei/FicheirosCSV/bays.csv");
            List<Wagon> wagons = manager.loadWagons("src/main/java/pt/ipp/isep/dei/FicheirosCSV/wagons.csv");
            wms = new WMS(quarantine, inventory, auditLog, manager.getWarehouses());
            wms.unloadWagons(wagons);
            List<Return> returns = manager.loadReturns("src/main/java/pt/ipp/isep/dei/FicheirosCSV/returns.csv");
            for (Return r : returns) quarantine.addReturn(r);
            StationRepository estacaoRepo = new StationRepository();
            LocomotiveRepository locomotivaRepo = new LocomotiveRepository();
            SegmentLineRepository segmentoRepo = new SegmentLineRepository();
            RailwayNetworkService networkService = new RailwayNetworkService(estacaoRepo, segmentoRepo);
            travelTimeController = new TravelTimeController(estacaoRepo, locomotivaRepo, networkService, segmentoRepo);
            stationIndexManager = new StationIndexManager();
            List<EuropeanStation> europeanStations = manager.loadEuropeanStations("src/main/java/pt/ipp/isep/dei/FicheirosCSV/train_stations_europe.csv");
            stationIndexManager.buildIndexes(europeanStations);
            spatialKDTree = buildSpatialKDTree(europeanStations);
            System.out.println("Dados carregados com sucesso.");
        } catch (Exception e) {
            System.err.println("❌ ERRO FATAL AO INICIAR A GUI: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        // Esta é a TUA lógica de carregamento (da raiz) - Está CORRETA
        String fxmlPath = "main-view.fxml";
        String cssPath = "style.css";
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        URL fxmlUrl = classLoader.getResource(fxmlPath);
        if (fxmlUrl == null) {
            System.err.println("ERRO CRÍTICO: Não foi possível encontrar: " + fxmlPath);
            throw new IOException("Location is not set: " + fxmlPath);
        }

        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Parent root = loader.load();

        // --- INJEÇÃO DE DEPENDÊNCIA (A parte nova) ---
        MainController mainController = loader.getController();
        if (mainController == null) {
            throw new IOException("Erro fatal: fx:controller não encontrado. Verifica main-view.fxml.");
        }

        // Passa todos os serviços de backend para o controlador
        mainController.setBackendServices(
                wms,
                manager,
                travelTimeController,
                stationIndexManager,
                spatialKDTree
        );
        // --- FIM DA INJEÇÃO ---

        URL cssUrl = classLoader.getResource(cssPath);
        if (cssUrl != null) {
            root.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("AVISO: Ficheiro CSS 'style.css' não encontrado.");
        }

        primaryStage.setTitle("Gestão Integrada (PI - G023)");
        primaryStage.setScene(new Scene(root, 1200, 800));
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(700);
        primaryStage.show();
    }

    /**
     * Helper copiado de Main.java para construir a KDTree.
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