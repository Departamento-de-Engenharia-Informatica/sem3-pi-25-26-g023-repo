package pt.ipp.isep.dei.UI.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import pt.ipp.isep.dei.UI.gui.views.DashboardController;
import pt.ipp.isep.dei.UI.gui.views.TravelTimeGUIController;
import pt.ipp.isep.dei.UI.gui.views.Usei01Controller;
import pt.ipp.isep.dei.controller.TravelTimeController;
import pt.ipp.isep.dei.domain.InventoryManager;
import pt.ipp.isep.dei.domain.KDTree;
import pt.ipp.isep.dei.domain.StationIndexManager;
import pt.ipp.isep.dei.domain.WMS;

import java.io.IOException;
import java.net.URL;

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

    // --- Global Status Flags ---
    private boolean isAllocationsRun = false;
    private boolean isPickingRun = false;


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
     * Carrega uma nova vista FXML na área de conteúdo central.
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

            // --- INJEÇÃO DE DEPENDÊNCIA (Atualizada) ---

            if (backendService instanceof TravelTimeController && controller instanceof TravelTimeGUIController) {
                ((TravelTimeGUIController) controller).setBackend((TravelTimeController) backendService);
            }

            else if (controller instanceof DashboardController) {
                ((DashboardController) controller).setServices(this.wms, this.manager);
            }

            else if (controller instanceof Usei01Controller) {
                ((Usei01Controller) controller).setServices(this, this.wms, this.manager);
            }


            // --- FIM DO NOVO BLOCO ---

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

    // --- MÉTODOS PÚBLICOS PARA ATUALIZAR O STATUS ---

    public void updateStatusAllocations(boolean hasRun) {
        this.isAllocationsRun = hasRun;
        updateStatusHeader();
    }

    public void updateStatusPicking(boolean hasRun) {
        this.isPickingRun = hasRun;
        updateStatusHeader();
    }


    private void updateStatusHeader() {
        if (statusAllocations == null || statusPicking == null) {
            return;
        }

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

    // --- HANDLERS DE NAVEGAÇÃO ---

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

    // --- NOVO HANDLER ---
    @FXML
    public void handleShowUSEI02(ActionEvent event) {
        statusLabel.setText("Allocate Orders [USEI02]");
        loadView("esinf-usei02-view.fxml", null);
    }
    // --- FIM DO NOVO HANDLER ---

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

    @FXML
    public void handleShowLAPR3(ActionEvent event) {
        statusLabel.setText("LAPR3 Features");
        loadView("lapr3-travel-time-view.fxml", this.travelTimeController);
    }

    @FXML
    public void handleShowBDDAD(ActionEvent event) {
        statusLabel.setText("BDDAD Features");
        centerContentPane.getChildren().clear();

        Label t = new Label("BDDAD Screen (Under Construction)");
        t.getStyleClass().add("view-title");
        centerContentPane.getChildren().add(t);
        AnchorPane.setTopAnchor(t, 25.0);
        AnchorPane.setLeftAnchor(t, 25.0);
    }
}