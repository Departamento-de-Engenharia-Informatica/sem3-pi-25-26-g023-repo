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
// --- NEW NECESSARY IMPORTS ---
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

    // --- Backend Services ---
    private WMS wms;
    private InventoryManager manager;
    private TravelTimeController travelTimeController;
    private StationIndexManager stationIndexManager;
    private KDTree spatialKDTree;

    // -------------------------------------------------------------
    // --- CORRECTION OF COMPLEX DEPENDENCY INITIALIZATION ---
    // 1. Initialize base repositories
    private TrainRepository trainRepository = new TrainRepository();
    private FacilityRepository facilityRepository = new FacilityRepository();
    private LocomotiveRepository locomotiveRepository = new LocomotiveRepository();
    private StationRepository stationRepository = new StationRepository(); // <-- NEW!
    private SegmentLineRepository segmentLineRepository = new SegmentLineRepository(); // <-- NEW!

    // 2. Initialize services that depend on repositories

    // SchedulerService requires 2 arguments: StationRepository and FacilityRepository
    private SchedulerService schedulerService = new SchedulerService(
            this.stationRepository,
            this.facilityRepository
    );

    // RailwayNetworkService requires 2 arguments: StationRepository and SegmentLineRepository
    private RailwayNetworkService networkService = new RailwayNetworkService(
            this.stationRepository,
            this.segmentLineRepository
    );

    // 3. Initialize DispatcherService with 5 arguments (Repos and Services)
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

    // --- GLOBAL STATE (as in CargoHandlingUI) ---
    private AllocationResult lastAllocationResult = null;
    private PickingPlan lastPickingPlan = null;


    /**
     * Sets the core backend services (WMS, InventoryManager, Controllers) to this controller.
     * Called by the application's main entry point (App.java).
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

    /**
     * Initializes the controller after the FXML has been loaded.
     */
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
     *
     * @param fxmlFileName The name of the FXML file (e.g., "dashboard-view.fxml").
     * @param backendService The main dependency to inject into the new view's controller.
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

            // --- DEPENDENCY INJECTION (Updated) ---

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
                } else {
                    System.err.println("Injection Error: Usei02Controller expected an InventoryManager.");
                }
            }

            // ✅ --- BLOCK FOR USEI03 ---
            else if (controller instanceof Usei03Controller) {
                if (backendService instanceof InventoryManager) {
                    ((Usei03Controller) controller).setServices(this, (InventoryManager) backendService);
                } else {
                    System.err.println("Injection Error: Usei03Controller expected an InventoryManager.");
                }
            }
            // ✅ --- BLOCK FOR USEI04 ---
            else if (controller instanceof Usei04Controller) {
                if (backendService instanceof MainController) {
                    ((Usei04Controller) controller).setServices((MainController) backendService);
                } else {
                    System.err.println("Injection Error: Usei04Controller expected a MainController.");
                }
            }

            // ✅ --- BLOCK FOR USEI05 ---
            else if (controller instanceof Usei05Controller) {
                ((Usei05Controller) controller).setServices(this, this.wms, this.manager);
            }

            else if (controller instanceof Usei06Controller) {
                if (backendService instanceof StationIndexManager) {
                    ((Usei06Controller) controller).setServices(this, (StationIndexManager) backendService);
                } else {
                    System.err.println("Injection Error: Usei06Controller expected a StationIndexManager.");
                }
            }
            // ✅ --- BLOCK FOR USEI07 ---
            else if (controller instanceof Usei07Controller) {
                if (backendService instanceof StationIndexManager) {
                    ((Usei07Controller) controller).setServices(this, (StationIndexManager) backendService);
                } else {
                    System.err.println("Injection Error: Usei07Controller expected a StationIndexManager.");
                }
            }

            // ✅ --- BLOCK FOR USEI08 ---
            else if (controller instanceof Usei08Controller) {
                if (backendService instanceof KDTree) {
                    ((Usei08Controller) controller).setServices(this, (KDTree) backendService);
                } else {
                    System.err.println("Injection Error: Usei08Controller expected the KDTree.");
                }
            }

            // ✅ --- NEW BLOCK FOR USEI10 (Radius Search) ---
            else if (controller instanceof Usei10Controller) {
                // USEI10 needs the StationIndexManager to obtain the RadiusSearchEngine
                if (backendService instanceof StationIndexManager) {
                    ((Usei10Controller) controller).setServices(this, (StationIndexManager) backendService);
                } else {
                    System.err.println("Injection Error: Usei10Controller expected a StationIndexManager.");
                }
            }
            // --- END OF NEW INJECTION BLOCK ---


            // ✅ --- BLOCK FOR BDDADMainController ---
            else if (controller instanceof BDDADMainController) {
                if (backendService instanceof MainController) {
                    ((BDDADMainController) controller).setServices((MainController) backendService);
                } else {
                    System.err.println("Injection Error: BDDADMainController expected a MainController.");
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

    /**
     * Updates the status flag for allocations and refreshes the header.
     */
    public void updateStatusAllocations(boolean hasRun) {
        this.isAllocationsRun = hasRun;
        updateStatusHeader();
    }

    /**
     * Updates the status flag for picking and refreshes the header.
     */
    public void updateStatusPicking(boolean hasRun) {
        this.isPickingRun = hasRun;
        updateStatusHeader();
    }


    /**
     * Refreshes the style and text of the status labels in the main header.
     */
    private void updateStatusHeader() {
        if (statusAllocations == null || statusPicking == null) {
            return;
        }

        updateLabelStyle(statusAllocations, isAllocationsRun);
        updateLabelStyle(statusPicking, isPickingRun);
    }


    /**
     * Helper to apply CSS classes based on the run status.
     */
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

    /** Returns the result of the last order allocation operation. */
    public AllocationResult getLastAllocationResult() {
        return lastAllocationResult;
    }

    /** Sets the result of the last order allocation operation. */
    public void setLastAllocationResult(AllocationResult lastAllocationResult) {
        this.lastAllocationResult = lastAllocationResult;
    }

    /** Returns the result of the last picking plan generation. */
    public PickingPlan getLastPickingPlan() {
        return lastPickingPlan;
    }

    /** Sets the result of the last picking plan generation. */
    public void setLastPickingPlan(PickingPlan lastPickingPlan) {
        this.lastPickingPlan = lastPickingPlan;
    }


    // --- NAVIGATION HANDLERS ---

    @FXML
    private VBox notificationPane;

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
        // USEI09 does not use the traditional 'loadView', so the code is kept separate
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

    // ✅ --- INTEGRATED HANDLER FOR USEI10 ---
    @FXML
    public void handleShowUSEI10(ActionEvent event) {
        statusLabel.setText("Radius Search & Density Summary [USEI10]");
        // Pass the StationIndexManager, which contains the RadiusSearchEngine
        loadView("esinf-usei10-view.fxml", this.stationIndexManager);
    }
    // ----------------------------------------


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

    // ... [Other Handlers and CRUD Methods omitted for brevity, but should be kept]

    // --- PUBLIC METHODS FOR CRUDS (KEPT FROM PREVIOUS STRUCTURE) ---

    @FXML
    public void handleShowSimulation(ActionEvent event) throws IOException {
        if (this.statusLabel != null) {
            this.statusLabel.setText("USLP07 - Full Simulation and Conflicts");
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/lapr3-simulation-view.fxml"));
        TrainSimulationController controller = new TrainSimulationController();

        controller.setDependencies(
                this,
                this.facilityRepository,
                this.dispatcherService,
                this.locomotiveRepository
        );

        loader.setController(controller);
        Parent root = loader.load();

        controller.initController();

        // Manually associate button action (optional)
        controller.runButton.setOnAction(e -> controller.runSimulation());

        this.centerContentPane.getChildren().clear();
        this.centerContentPane.getChildren().add(root);

        AnchorPane.setTopAnchor(root, 0.0);
        AnchorPane.setBottomAnchor(root, 0.0);
        AnchorPane.setLeftAnchor(root, 0.0);
        AnchorPane.setRightAnchor(root, 0.0);
    }

    /** Loads the Train CRUD view. */
    public void loadTrainCrudView() {
        statusLabel.setText("BDDAD Features: Train CRUD");
        centerContentPane.getChildren().clear();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/train-crud-view.fxml"));
            Parent root = loader.load();
            TrainCRUDController controller = loader.getController();

            if (controller != null) {
                controller.setDependencies(
                        this,
                        this.trainRepository,
                        this.facilityRepository,
                        this.locomotiveRepository,
                        this.networkService
                );
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

    /** Loads the Operator CRUD view. */
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

    /** Loads the Locomotive CRUD view. */
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

    /**
     * Shows a transient notification pop-up in the corner of the application.
     *
     * @param message The message to display.
     * @param type The type of notification ("success", "error", or other for info).
     */
    public void showNotification(String message, String type) {
        // Notification logic
        Label notificationLabel = new Label(message);
        notificationLabel.getStyleClass().add("notification-label");

        if ("success".equals(type)) {
            notificationLabel.getStyleClass().add("notification-success");
        } else if ("error".equals(type)) {
            notificationLabel.getStyleClass().add("notification-error");
        } else {
            notificationLabel.getStyleClass().add("notification-info");
        }

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), notificationLabel);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        PauseTransition delay = new PauseTransition(Duration.seconds(4));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), notificationLabel);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        fadeOut.setOnFinished(e -> notificationPane.getChildren().remove(notificationLabel));

        fadeIn.setOnFinished(e -> {
            delay.play();
        });
        delay.setOnFinished(e -> {
            fadeOut.play();
        });

        notificationPane.getChildren().add(notificationLabel);
        fadeIn.play();
    }

    /** Loads the Wagon CRUD view. */
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

}