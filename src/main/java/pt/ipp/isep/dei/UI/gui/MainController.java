package pt.ipp.isep.dei.UI.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import pt.ipp.isep.dei.UI.gui.views.*;
import pt.ipp.isep.dei.UI.gui.views.TrainCRUDController;
import pt.ipp.isep.dei.controller.TravelTimeController;
import pt.ipp.isep.dei.domain.*;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import pt.ipp.isep.dei.repository.FacilityRepository;
import pt.ipp.isep.dei.repository.LocomotiveRepository;
import pt.ipp.isep.dei.repository.TrainRepository;
import pt.ipp.isep.dei.repository.StationRepository;
import pt.ipp.isep.dei.repository.SegmentLineRepository;

import java.io.IOException;
import java.net.URL;

/**
 * Main Controller for the JavaFX application.
 * Manages view navigation, dependency injection into sub-controllers,
 * and maintains global application state (e.g., last results, run status).
 */
public class MainController {

    @FXML
    private BorderPane mainPane;
    @FXML
    private AnchorPane centerContentPane;
    @FXML
    private Label statusLabel;

    @FXML
    private Label statusAllocations;
    @FXML
    private Label statusPicking;

    @FXML
    private VBox notificationPane;

    // --- Backend Services ---
    private WMS wms;
    private InventoryManager manager;
    private TravelTimeController travelTimeController;
    private StationIndexManager stationIndexManager;
    private KDTree spatialKDTree;

    // -------------------------------------------------------------
    // --- DEPENDENCY INITIALIZATION ---
    private TrainRepository trainRepository = new TrainRepository();
    private FacilityRepository facilityRepository = new FacilityRepository();
    private LocomotiveRepository locomotiveRepository = new LocomotiveRepository();
    private StationRepository stationRepository = new StationRepository();
    private SegmentLineRepository segmentLineRepository = new SegmentLineRepository();

    // Services
    private SchedulerService schedulerService = new SchedulerService(
            this.stationRepository,
            this.facilityRepository
    );

    private RailwayNetworkService networkService = new RailwayNetworkService(
            this.stationRepository,
            this.segmentLineRepository
    );

    private DispatcherService dispatcherService = new DispatcherService(
            this.trainRepository,
            this.networkService,
            this.facilityRepository,
            this.locomotiveRepository,
            this.schedulerService
    );
    // -------------------------------------------------------------

    // --- Global Status Flags ---
    private boolean isAllocationsRun = false;
    private boolean isPickingRun = false;

    // --- GLOBAL STATE ---
    private AllocationResult lastAllocationResult = null;
    private PickingPlan lastPickingPlan = null;


    /**
     * Sets the core backend services (WMS, InventoryManager, Controllers).
     */
    public void setBackendServices(WMS wms, InventoryManager manager,
                                   TravelTimeController ttc,
                                   StationIndexManager sim, KDTree kdt) {
        this.wms = wms;
        this.manager = manager;
        this.travelTimeController = ttc;
        this.stationIndexManager = sim;
        this.spatialKDTree = kdt;

        statusLabel.setText(String.format("Welcome! %d items, %d boxes in inventory.",
                manager.getItemsCount(), wms.getInventory().getBoxes().size()));
    }

    @FXML
    public void initialize() {
        statusLabel.setText("Loading services...");
        updateStatusHeader();

        javafx.application.Platform.runLater(() -> {
            handleShowDashboard(null);
        });
    }

    /**
     * Loads a new FXML view into the central content area.
     */
    private void loadView(String fxmlFileName, Object backendService) {
        try {
            URL fxmlUrl = getClass().getClassLoader().getResource(fxmlFileName);
            if (fxmlUrl == null) {
                System.err.println("Critical Error: Could not find FXML: " + fxmlFileName);
                statusLabel.setText("Error: Could not find view " + fxmlFileName);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent view = loader.load();
            Object controller = loader.getController();

            // --- DEPENDENCY INJECTION ---

            if (backendService instanceof TravelTimeController && controller instanceof TravelTimeGUIController) {
                ((TravelTimeGUIController) controller).setBackend((TravelTimeController) backendService);
            }
            else if (controller instanceof DashboardController) {
                ((DashboardController) controller).setServices(this.wms, this.manager);
            }
            else if (controller instanceof Usei01Controller) {
                ((Usei01Controller) controller).setServices(this, this.wms, this.manager);
            }
            else if (controller instanceof Usei02Controller) {
                if (backendService instanceof InventoryManager) {
                    ((Usei02Controller) controller).setServices(this, (InventoryManager) backendService);
                }
            }
            else if (controller instanceof Usei03Controller) {
                if (backendService instanceof InventoryManager) {
                    ((Usei03Controller) controller).setServices(this, (InventoryManager) backendService);
                }
            }
            else if (controller instanceof Usei04Controller) {
                if (backendService instanceof MainController) {
                    ((Usei04Controller) controller).setServices((MainController) backendService);
                }
            }
            else if (controller instanceof Usei05Controller) {
                ((Usei05Controller) controller).setServices(this, this.wms, this.manager);
            }
            else if (controller instanceof Usei06Controller) {
                if (backendService instanceof StationIndexManager) {
                    ((Usei06Controller) controller).setServices(this, (StationIndexManager) backendService);
                }
            }
            else if (controller instanceof Usei07Controller) {
                if (backendService instanceof StationIndexManager) {
                    ((Usei07Controller) controller).setServices(this, (StationIndexManager) backendService);
                }
            }
            else if (controller instanceof Usei08Controller) {
                if (backendService instanceof KDTree) {
                    ((Usei08Controller) controller).setServices(this, (KDTree) backendService);
                }
            }
            else if (controller instanceof Usei10Controller) {
                if (backendService instanceof StationIndexManager) {
                    ((Usei10Controller) controller).setServices(this, (StationIndexManager) backendService);
                }
            }
            // ✅ USEI12 and USEI14 Controllers are self-contained (they init their own services),
            // so no explicit injection is strictly needed here, unless you want to pass main controller.

            else if (controller instanceof BDDADMainController) {
                if (backendService instanceof MainController) {
                    ((BDDADMainController) controller).setServices((MainController) backendService);
                }
            }
            // --- END OF INJECTION ---

            centerContentPane.getChildren().clear();
            centerContentPane.getChildren().add(view);
            AnchorPane.setTopAnchor(view, 0.0);
            AnchorPane.setBottomAnchor(view, 0.0);
            AnchorPane.setLeftAnchor(view, 0.0);
            AnchorPane.setRightAnchor(view, 0.0);

        } catch (IOException e) {
            System.err.println("Failed to load view: " + fxmlFileName);
            e.printStackTrace();
            statusLabel.setText("Error loading screen.");
        }
    }


    // --- PUBLIC METHODS TO UPDATE STATUS ---

    public void updateStatusAllocations(boolean hasRun) {
        this.isAllocationsRun = hasRun;
        updateStatusHeader();
    }

    public void updateStatusPicking(boolean hasRun) {
        this.isPickingRun = hasRun;
        updateStatusHeader();
    }

    private void updateStatusHeader() {
        if (statusAllocations == null || statusPicking == null) return;
        updateLabelStyle(statusAllocations, isAllocationsRun);
        updateLabelStyle(statusPicking, isPickingRun);
    }

    private void updateLabelStyle(Label label, boolean hasRun) {
        if (hasRun) {
            label.setText("RUN");
            label.getStyleClass().remove("status-not-run");
            label.getStyleClass().add("status-run");
        } else {
            label.setText("NOT-RUN");
            label.getStyleClass().remove("status-run");
            label.getStyleClass().add("status-not-run");
        }
    }


    // --- PUBLIC METHODS FOR GLOBAL STATE ---

    public AllocationResult getLastAllocationResult() { return lastAllocationResult; }
    public void setLastAllocationResult(AllocationResult res) { this.lastAllocationResult = res; }
    public PickingPlan getLastPickingPlan() { return lastPickingPlan; }
    public void setLastPickingPlan(PickingPlan plan) { this.lastPickingPlan = plan; }


    // --- NAVIGATION HANDLERS ---

    @FXML
    public void handleShowDashboard(ActionEvent event) {
        statusLabel.setText("Showing Dashboard");
        loadView("dashboard-view.fxml", null);
    }

    @FXML
    public void handleShowUSEI01(ActionEvent event) {
        statusLabel.setText("Unload Wagons [USEI01]");
        loadView("esinf-usei01-view.fxml", null);
    }

    @FXML
    public void handleShowUSEI02(ActionEvent event) {
        statusLabel.setText("Allocate Orders [USEI02]");
        loadView("esinf-usei02-view.fxml", this.manager);
    }

    @FXML
    public void handleShowUSEI03(ActionEvent event) {
        statusLabel.setText("Pack Trolleys [USEI03]");
        loadView("esinf-usei03-view.fxml", this.manager);
    }

    @FXML
    public void handleShowUSEI04(ActionEvent event) {
        statusLabel.setText("Pick Path Sequencing [USEI04]");
        loadView("esinf-usei04-view.fxml", this);
    }
    @FXML
    public void handleShowUSEI05(ActionEvent event) {
        statusLabel.setText("Process Returns [USEI05]");
        loadView("esinf-usei05-view.fxml", null);
    }

    @FXML
    public void handleShowUSEI06(ActionEvent event) {
        statusLabel.setText("European Station Index [USEI06]");
        loadView("esinf-usei06-view.fxml", this.stationIndexManager);
    }

    @FXML
    public void handleShowUSEI07(ActionEvent event) {
        statusLabel.setText("Analyze 2D-Tree [USEI07]");
        loadView("esinf-usei07-view.fxml", this.stationIndexManager);
    }

    @FXML
    public void handleShowUSEI08(ActionEvent event) {
        statusLabel.setText("Spatial Queries [USEI08]");
        loadView("esinf-usei08-view.fxml", this.spatialKDTree);
    }

    @FXML
    void handleShowUSEI09(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/esinf-usei09-view.fxml"));
            BorderPane view = loader.load();
            Usei09Controller controller = loader.getController();
            controller.setServices(this, this.spatialKDTree);
            centerContentPane.getChildren().setAll(view);
            AnchorPane.setTopAnchor(view, 0.0);
            AnchorPane.setBottomAnchor(view, 0.0);
            AnchorPane.setLeftAnchor(view, 0.0);
            AnchorPane.setRightAnchor(view, 0.0);
            statusLabel.setText("Proximity Search [USEI09]");
        } catch (IOException e) {
            statusLabel.setText("❌ Error loading USEI09 view.");
        }
    }

    @FXML
    public void handleShowUSEI10(ActionEvent event) {
        statusLabel.setText("Radius Search & Density Summary [USEI10]");
        loadView("esinf-usei10-view.fxml", this.stationIndexManager);
    }

    // ✅ --- NEW HANDLER FOR USEI12 ---
    @FXML
    public void handleShowUSEI12(ActionEvent event) {
        statusLabel.setText("Minimal Backbone Network [USEI12]");
        loadView("esinf-usei12-view.fxml", null);
    }

    // ✅ --- NEW HANDLER FOR USEI14 ---
    @FXML
    public void handleShowUSEI14(ActionEvent event) {
        statusLabel.setText("Max Throughput Analysis [USEI14]");
        loadView("esinf-usei14-view.fxml", null);
    }

    @FXML
    public void handleShowESINF(ActionEvent event) {
        statusLabel.setText("ESINF Feature (Under Construction)");
        centerContentPane.getChildren().clear();
        Label t = new Label("ESINF Screen (Under Construction)");
        t.getStyleClass().add("view-title");
        centerContentPane.getChildren().add(t);
        AnchorPane.setTopAnchor(t, 25.0);
        AnchorPane.setLeftAnchor(t, 25.0);
    }

    // --- PUBLIC METHODS FOR CRUDS ---

    @FXML
    public void handleShowSimulation(ActionEvent event) throws IOException {
        statusLabel.setText("USLP07 - Full Simulation and Conflicts");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/lapr3-simulation-view.fxml"));
        TrainSimulationController controller = new TrainSimulationController();
        controller.setDependencies(this, this.facilityRepository, this.dispatcherService, this.locomotiveRepository);
        loader.setController(controller);
        Parent root = loader.load();
        controller.initController();
        controller.runButton.setOnAction(e -> controller.runSimulation());
        this.centerContentPane.getChildren().clear();
        this.centerContentPane.getChildren().add(root);
        AnchorPane.setTopAnchor(root, 0.0);
        AnchorPane.setBottomAnchor(root, 0.0);
        AnchorPane.setLeftAnchor(root, 0.0);
        AnchorPane.setRightAnchor(root, 0.0);
    }

    public void loadTrainCrudView() {
        statusLabel.setText("BDDAD Features: Train CRUD");
        centerContentPane.getChildren().clear();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/train-crud-view.fxml"));
            Parent root = loader.load();
            TrainCRUDController controller = loader.getController();
            if (controller != null) {
                controller.setDependencies(this, this.trainRepository, this.facilityRepository, this.locomotiveRepository, this.networkService);
                controller.initController();
            }
            centerContentPane.getChildren().add(root);
            AnchorPane.setTopAnchor(root, 0.0);
            AnchorPane.setLeftAnchor(root, 0.0);
            AnchorPane.setRightAnchor(root, 0.0);
            AnchorPane.setBottomAnchor(root, 0.0);
        } catch (Exception e) {
            statusLabel.setText("❌ Error loading Train CRUD view.");
        }
    }

    @FXML
    public void handleShowBDADQueries(ActionEvent event) {
        statusLabel.setText("BDDAD Queries - User Stories");
        try {
            URL fxmlUrl = getClass().getClassLoader().getResource("usbd-queries-view.fxml");
            if (fxmlUrl == null) {
                this.showNotification("Error: Could not find FXML for BDDAD Queries.", "error");
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            BDADQueriesController controller = new BDADQueriesController();
            controller.setMainController(this);
            loader.setController(controller);
            Parent root = loader.load();
            this.centerContentPane.getChildren().clear();
            this.centerContentPane.getChildren().add(root);
            AnchorPane.setTopAnchor(root, 0.0);
            AnchorPane.setBottomAnchor(root, 0.0);
            AnchorPane.setLeftAnchor(root, 0.0);
            AnchorPane.setRightAnchor(root, 0.0);
        } catch (IOException e) {
            this.showNotification("Failed to load BDDAD Queries view: " + e.getMessage(), "error");
        }
    }

    @FXML
    public void handleShowBDDAD(ActionEvent event) {
        statusLabel.setText("BDDAD Features Menu");
        loadView("bdad-main-view.fxml", this);
    }

    public void loadOperatorCrudView() {
        statusLabel.setText("BDDAD Features: Operator CRUD");
        centerContentPane.getChildren().clear();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/database-crud-view.fxml"));
            Parent root = loader.load();
            DatabaseCRUDController controller = loader.getController();
            controller.setServices(this);
            centerContentPane.getChildren().add(root);
            AnchorPane.setTopAnchor(root, 0.0);
            AnchorPane.setLeftAnchor(root, 0.0);
            AnchorPane.setRightAnchor(root, 0.0);
            AnchorPane.setBottomAnchor(root, 0.0);
        } catch (Exception e) {
            statusLabel.setText("❌ Error loading BDDAD CRUD view.");
        }
    }

    public void loadLocomotiveCrudView() {
        statusLabel.setText("BDDAD Features: Locomotive CRUD");
        centerContentPane.getChildren().clear();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/locomotive-crud-view.fxml"));
            Parent root = loader.load();
            LocomotiveCRUDController controller = loader.getController();
            controller.setServices(this);
            centerContentPane.getChildren().add(root);
            AnchorPane.setTopAnchor(root, 0.0);
            AnchorPane.setLeftAnchor(root, 0.0);
            AnchorPane.setRightAnchor(root, 0.0);
            AnchorPane.setBottomAnchor(root, 0.0);
        } catch (Exception e) {
            statusLabel.setText("❌ Error loading Locomotive CRUD view.");
        }
    }

    public void loadWagonCrudView() {
        statusLabel.setText("BDDAD Features: Wagon CRUD");
        centerContentPane.getChildren().clear();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/wagon-crud-view.fxml"));
            Parent root = loader.load();
            WagonCRUDController controller = loader.getController();
            controller.setServices(this);
            centerContentPane.getChildren().add(root);
            AnchorPane.setTopAnchor(root, 0.0);
            AnchorPane.setLeftAnchor(root, 0.0);
            AnchorPane.setRightAnchor(root, 0.0);
            AnchorPane.setBottomAnchor(root, 0.0);
        } catch (Exception e) {
            statusLabel.setText("❌ Error loading Wagon CRUD view.");
        }
    }

    public void showNotification(String message, String type) {
        Label notificationLabel = new Label(message);
        notificationLabel.getStyleClass().add("notification-label");
        if ("success".equals(type)) notificationLabel.getStyleClass().add("notification-success");
        else if ("error".equals(type)) notificationLabel.getStyleClass().add("notification-error");
        else notificationLabel.getStyleClass().add("notification-info");

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), notificationLabel);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        PauseTransition delay = new PauseTransition(Duration.seconds(4));
        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), notificationLabel);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> notificationPane.getChildren().remove(notificationLabel));
        fadeIn.setOnFinished(e -> delay.play());
        delay.setOnFinished(e -> fadeOut.play());
        notificationPane.getChildren().add(notificationLabel);
        fadeIn.play();
    }
}