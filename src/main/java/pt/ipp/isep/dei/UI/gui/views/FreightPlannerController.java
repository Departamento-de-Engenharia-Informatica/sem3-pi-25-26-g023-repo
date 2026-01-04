package pt.ipp.isep.dei.UI.gui.views;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import pt.ipp.isep.dei.UI.gui.MainController;
import pt.ipp.isep.dei.controller.RoutePlannerController;
import pt.ipp.isep.dei.domain.FreightRequest;
import pt.ipp.isep.dei.domain.Station;
import pt.ipp.isep.dei.repository.FacilityRepository;
import pt.ipp.isep.dei.domain.RailwayNetworkService;
import pt.ipp.isep.dei.repository.SegmentLineRepository;
import pt.ipp.isep.dei.repository.StationRepository;

import java.util.ArrayList;
import java.util.List;

public class FreightPlannerController {

    @FXML private RadioButton rbSimple;
    @FXML private RadioButton rbComplex;

    @FXML private TextField descField;
    @FXML private TextField weightField;
    @FXML private ComboBox<Station> originCombo;
    @FXML private ComboBox<Station> destCombo;
    @FXML private ListView<String> freightList;

    @FXML private ComboBox<Station> startStationCombo;
    @FXML private Button calculateButton;
    @FXML private TextArea resultArea;
    @FXML private Label statusLabel;

    private RoutePlannerController controller;
    private final List<FreightRequest> pendingFreights = new ArrayList<>();
    private MainController mainController;
    private ToggleGroup routeTypeGroup;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        // 1. Instanciar Repositórios
        FacilityRepository facilityRepo = new FacilityRepository();
        StationRepository stationRepo = new StationRepository();
        SegmentLineRepository segmentRepo = new SegmentLineRepository();

        // 2. Instanciar Serviço de Rede
        RailwayNetworkService networkService = new RailwayNetworkService(stationRepo, segmentRepo);

        // 3. Instanciar o Controller (CORREÇÃO: Agora recebe os 3 argumentos)
        this.controller = new RoutePlannerController(networkService, facilityRepo, segmentRepo);

        // Configuração da UI
        routeTypeGroup = new ToggleGroup();
        rbSimple.setToggleGroup(routeTypeGroup);
        rbComplex.setToggleGroup(routeTypeGroup);

        routeTypeGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (rbSimple.isSelected() && pendingFreights.size() > 1) {
                clearList();
                statusLabel.setText("Mode changed to Simple. List cleared.");
            }
        });

        loadStations();
        setupOriginListener();
    }

    private void loadStations() {
        // Usa o método getActiveStations para filtrar apenas estações com carris
        List<Station> stations = controller.getActiveStations();

        if (stations.isEmpty()) {
            statusLabel.setText("⚠️ No connected stations found via Repositories.");
            resultArea.setText("Warning: Facility or Segment repositories returned no data.\nPlease ensure database is populated.");
        }

        ObservableList<Station> obsStations = FXCollections.observableArrayList(stations);

        StringConverter<Station> converter = new StringConverter<>() {
            @Override
            public String toString(Station s) {
                return s == null ? "" : s.nome() + " (ID:" + s.idEstacao() + ")";
            }
            @Override
            public Station fromString(String string) { return null; }
        };

        originCombo.setItems(obsStations);
        originCombo.setConverter(converter);

        destCombo.setConverter(converter);

        startStationCombo.setItems(obsStations);
        startStationCombo.setConverter(converter);
    }

    private void setupOriginListener() {
        originCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                // Filtra destinos usando o grafo em memória
                List<Station> possible = controller.getReachableDestinations(newVal.idEstacao());
                destCombo.setItems(FXCollections.observableArrayList(possible));
                destCombo.setDisable(false);

                if (possible.isEmpty()) {
                    statusLabel.setText("⚠️ Selected origin is isolated.");
                } else {
                    statusLabel.setText("Select destination.");
                }
            } else {
                destCombo.setItems(FXCollections.emptyObservableList());
                destCombo.setDisable(true);
            }
        });
    }

    @FXML
    public void addFreight() {
        try {
            if (rbSimple.isSelected() && !pendingFreights.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Limit Reached", "Simple Route allows only 1 freight order.");
                return;
            }

            String desc = descField.getText();
            Station origin = originCombo.getValue();
            Station dest = destCombo.getValue();
            String weightStr = weightField.getText();

            if (desc.isEmpty() || origin == null || dest == null) {
                showAlert(Alert.AlertType.WARNING, "Missing Data", "Please fill description, origin and destination.");
                return;
            }

            double weight = 0.0;
            if (!weightStr.isEmpty()) weight = Double.parseDouble(weightStr);

            FreightRequest req = controller.createFreightRequest(
                    "FR-" + (pendingFreights.size() + 1),
                    origin.idEstacao(),
                    dest.idEstacao(),
                    desc,
                    weight
            );

            pendingFreights.add(req);
            updateList();

            descField.clear();
            weightField.clear();
            originCombo.getSelectionModel().clearSelection();
            statusLabel.setText("Freight added.");

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    private void updateList() {
        ObservableList<String> items = FXCollections.observableArrayList();
        for (FreightRequest f : pendingFreights) {
            items.add(String.format("%s | %d -> %d | %.1ft", f.getDescription(), f.getOriginStationId(), f.getDestinationStationId(), f.getWeightTons()));
        }
        freightList.setItems(items);
    }

    @FXML
    public void clearList() {
        pendingFreights.clear();
        updateList();
        resultArea.clear();
        statusLabel.setText("List cleared.");
    }

    @FXML
    public void calculateRoute() {
        Station start = startStationCombo.getValue();
        if (start == null) {
            showAlert(Alert.AlertType.WARNING, "Start Station", "Select where the locomotive starts.");
            return;
        }
        if (pendingFreights.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Freights", "Add freights first.");
            return;
        }

        try {
            List<Station> reachableFromStart = controller.getReachableDestinations(start.idEstacao());
            int firstPickup = pendingFreights.get(0).getOriginStationId();

            // Verifica se a locomotiva consegue chegar à primeira carga
            boolean canReach = (start.idEstacao() == firstPickup) ||
                    reachableFromStart.stream().anyMatch(s -> s.idEstacao() == firstPickup);

            if (!canReach) {
                showAlert(Alert.AlertType.ERROR, "Impossible Route",
                        "Locomotive at " + start.nome() + " cannot reach the first freight origin (ID: " + firstPickup + ").");
                return;
            }

            statusLabel.setText("Calculating...");
            boolean isSimple = rbSimple.isSelected();

            RoutePlannerController.PlannedRoute route = controller.planRoute(start.idEstacao(), pendingFreights, isSimple);

            StringBuilder sb = new StringBuilder();
            sb.append("=== ROUTE MANIFEST ===\n");
            sb.append("Type: ").append(isSimple ? "Simple" : "Complex").append("\n");
            sb.append("Total Distance: ").append(String.format("%.2f km", route.getTotalDistance())).append("\n\n");

            for (String line : route.manifest()) {
                sb.append(line).append("\n");
            }

            resultArea.setText(sb.toString());
            statusLabel.setText("Calculation complete.");

        } catch (Exception e) {
            resultArea.setText("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        if (getClass().getResource("/style.css") != null) {
            alert.getDialogPane().getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        }
        alert.showAndWait();
    }
}