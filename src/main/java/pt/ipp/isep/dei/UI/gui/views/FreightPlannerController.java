package pt.ipp.isep.dei.UI.gui.views;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import pt.ipp.isep.dei.UI.gui.GuiUtils;
import pt.ipp.isep.dei.UI.gui.MainController;
import pt.ipp.isep.dei.controller.RoutePlannerController;
import pt.ipp.isep.dei.domain.FreightRequest;
import pt.ipp.isep.dei.domain.Order;
import pt.ipp.isep.dei.domain.Station;
import pt.ipp.isep.dei.domain.RailwayNetworkService;
import pt.ipp.isep.dei.repository.FacilityRepository;
import pt.ipp.isep.dei.repository.SegmentLineRepository;
import pt.ipp.isep.dei.repository.StationRepository;
import pt.ipp.isep.dei.domain.InventoryManager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FreightPlannerController {

    @FXML private RadioButton rbSimple;
    @FXML private RadioButton rbComplex;

    @FXML private ComboBox<Order> orderCombo;
    @FXML private TextField weightField;
    @FXML private ComboBox<Station> originCombo;
    @FXML private ComboBox<Station> destCombo;
    @FXML private ListView<String> freightList;

    @FXML private ComboBox<Station> startStationCombo;
    @FXML private Button calculateButton;
    @FXML private Button btnSaveRoute; // Novo botão para persistência
    @FXML private TextArea resultArea;
    @FXML private Label statusLabel;

    private RoutePlannerController controller;
    private final List<FreightRequest> pendingFreights = new ArrayList<>();
    private MainController mainController;
    private ToggleGroup routeTypeGroup;
    private RoutePlannerController.PlannedRoute lastCalculatedRoute; // Guarda a última rota gerada

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        FacilityRepository facilityRepo = new FacilityRepository();
        StationRepository stationRepo = new StationRepository();
        SegmentLineRepository segmentRepo = new SegmentLineRepository();

        RailwayNetworkService networkService = new RailwayNetworkService(stationRepo, segmentRepo);
        this.controller = new RoutePlannerController(networkService, facilityRepo, segmentRepo);

        routeTypeGroup = new ToggleGroup();
        rbSimple.setToggleGroup(routeTypeGroup);
        rbComplex.setToggleGroup(routeTypeGroup);

        routeTypeGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (rbSimple.isSelected() && pendingFreights.size() > 1) {
                clearList();
                statusLabel.setText("Modo Simples: Lista limpa.");
            }
        });

        if (btnSaveRoute != null) btnSaveRoute.setDisable(true); // Desativa gravação até haver cálculo

        loadStations();
        loadOrdersFromInventory();
        setupOriginListener();
    }

    private void loadStations() {
        List<Station> stations = controller.getActiveStations();
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

    private void loadOrdersFromInventory() {
        InventoryManager invManager = new InventoryManager();
        List<Order> orders = invManager.getOrders();

        if (orders != null && !orders.isEmpty()) {
            orderCombo.setItems(FXCollections.observableArrayList(orders));
            orderCombo.setConverter(new StringConverter<Order>() {
                @Override
                public String toString(Order o) {
                    return o == null ? "" : "Order ID: " + o.orderId + " (Prioridade: " + o.priority + ")";
                }
                @Override
                public Order fromString(String s) { return null; }
            });
        } else {
            statusLabel.setText("⚠️ Nenhuma order carregada no ItemRepository.");
        }
    }

    private void setupOriginListener() {
        originCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                List<Station> possible = controller.getReachableDestinations(newVal.idEstacao());
                destCombo.setItems(FXCollections.observableArrayList(possible));
                destCombo.setDisable(false);
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
                showAlert(Alert.AlertType.WARNING, "Limite", "Rota Simples permite apenas 1 order.");
                return;
            }

            Order selected = orderCombo.getValue();
            Station origin = originCombo.getValue();
            Station dest = destCombo.getValue();

            if (selected == null || origin == null || dest == null) {
                showAlert(Alert.AlertType.WARNING, "Dados em Falta", "Selecione a Order, Origem e Destino.");
                return;
            }

            double weight = 0.0;
            if (!weightField.getText().isEmpty()) weight = Double.parseDouble(weightField.getText());

            FreightRequest req = controller.createFreightRequest(
                    selected.orderId,
                    origin.idEstacao(),
                    dest.idEstacao(),
                    "Transporte Order " + selected.orderId,
                    weight
            );

            pendingFreights.add(req);
            updateList();

            orderCombo.getSelectionModel().clearSelection();
            weightField.clear();
            statusLabel.setText("Carga da Order " + selected.orderId + " adicionada.");
            if (btnSaveRoute != null) btnSaveRoute.setDisable(true); // Novo conteúdo exige novo cálculo

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erro", e.getMessage());
        }
    }

    private void updateList() {
        ObservableList<String> items = FXCollections.observableArrayList();
        for (FreightRequest f : pendingFreights) {
            items.add(String.format("%s | %d -> %d | %.1ft", f.getId(), f.getOriginStationId(), f.getDestinationStationId(), f.getWeightTons()));
        }
        freightList.setItems(items);
    }

    @FXML
    public void clearList() {
        pendingFreights.clear();
        lastCalculatedRoute = null;
        updateList();
        resultArea.clear();
        if (btnSaveRoute != null) btnSaveRoute.setDisable(true);
    }

    @FXML
    public void calculateRoute() {
        Station start = startStationCombo.getValue();
        if (start == null || pendingFreights.isEmpty()) return;

        try {
            boolean isSimple = rbSimple.isSelected();
            this.lastCalculatedRoute = controller.planRoute(start.idEstacao(), pendingFreights, isSimple);

            InventoryManager.savePlannedRoute(lastCalculatedRoute);

            StringBuilder sb = new StringBuilder();
            sb.append("=== ROUTE MANIFEST ===\n");
            sb.append("Type: ").append(isSimple ? "Simple" : "Complex").append("\n");
            sb.append("Total Distance: ").append(String.format("%.2f km", lastCalculatedRoute.getTotalDistance())).append("\n\n");

            for (String line : lastCalculatedRoute.manifest()) sb.append(line).append("\n");

            resultArea.setText(sb.toString());
            statusLabel.setText("Cálculo concluído. Pronto para registar na BD.");

            if (btnSaveRoute != null) btnSaveRoute.setDisable(false); // Ativa gravação

        } catch (Exception e) {
            resultArea.setText("Erro: " + e.getMessage());
        }
    }

    /**
     * EFETIVA A PERSISTÊNCIA NA BASE DE DADOS (USLP08)
     */
    @FXML
    public void handleSaveRoute() {
        if (lastCalculatedRoute == null) return;

        try {
            String customName = "Rota_" + System.currentTimeMillis();
            controller.savePlannedRoute(customName, lastCalculatedRoute, new ArrayList<>(pendingFreights));

            showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Rota registada na Base de Dados!");
            statusLabel.setText("Rota persistida com sucesso.");
            btnSaveRoute.setDisable(true); // Evita duplicados

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erro de BD", "Falha ao gravar na base de dados: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}