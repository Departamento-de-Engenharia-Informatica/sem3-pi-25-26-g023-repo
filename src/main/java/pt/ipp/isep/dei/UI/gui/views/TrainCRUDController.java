package pt.ipp.isep.dei.UI.gui.views;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import pt.ipp.isep.dei.DatabaseConnection.DatabaseConnection; // Necessário para getConnection()
import pt.ipp.isep.dei.UI.gui.MainController;
import pt.ipp.isep.dei.domain.RailwayNetworkService;
import pt.ipp.isep.dei.domain.Train;
import pt.ipp.isep.dei.repository.FacilityRepository;
import pt.ipp.isep.dei.repository.LocomotiveRepository;
import pt.ipp.isep.dei.repository.TrainRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javafx.util.StringConverter;


public class TrainCRUDController {

    @FXML private TableView<Train> trainTable;
    @FXML private TableColumn<Train, String> idColumn;
    @FXML private TableColumn<Train, String> operatorColumn;
    @FXML private TableColumn<Train, LocalDateTime> departureColumn;
    @FXML private TableColumn<Train, Integer> startFacilityColumn;
    @FXML private TableColumn<Train, Integer> endFacilityColumn;

    // Campos de Input
    @FXML private TextField trainIdField;
    @FXML private ComboBox<String> operatorIdCombo;
    @FXML private DatePicker departureDateField;
    @FXML private TextField departureTimeField;
    @FXML private ComboBox<Map.Entry<Integer, String>> startFacilityCombo;
    @FXML private ComboBox<Map.Entry<Integer, String>> endFacilityCombo;
    @FXML private ComboBox<String> locomotiveIdCombo;
    @FXML private ComboBox<String> routeIdCombo;
    @FXML private Button saveButton;
    @FXML private ProgressIndicator progressIndicator;

    private MainController mainController;
    private TrainRepository trainRepository;
    private FacilityRepository facilityRepository;
    private LocomotiveRepository locomotiveRepository;
    private RailwayNetworkService networkService;
    private ObservableList<Train> observableTrains;
    private Map<Integer, String> facilityMap;

    public void setDependencies(MainController mainController,
                                TrainRepository trainRepository,
                                FacilityRepository facilityRepository,
                                LocomotiveRepository locomotiveRepository,
                                RailwayNetworkService networkService) {
        this.mainController = mainController;
        this.trainRepository = trainRepository;
        this.facilityRepository = facilityRepository;
        this.locomotiveRepository = locomotiveRepository;
        this.networkService = networkService;
        this.facilityMap = facilityRepository.findAllFacilityNames();
    }

    @FXML
    public void initialize() {
        if (trainTable != null) {
            if (idColumn != null) {
                idColumn.setCellValueFactory(new PropertyValueFactory<>("trainId"));
                operatorColumn.setCellValueFactory(new PropertyValueFactory<>("operatorId"));
                departureColumn.setCellValueFactory(new PropertyValueFactory<>("departureTime"));
                startFacilityColumn.setCellValueFactory(new PropertyValueFactory<>("startFacilityId"));
                endFacilityColumn.setCellValueFactory(new PropertyValueFactory<>("endFacilityId"));

                trainTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        populateForm(newSelection);
                    }
                });
            }
        }

        if (startFacilityCombo != null) {
            startFacilityCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && networkService != null) {
                    filterEndFacilities(newVal.getKey());
                } else {
                    endFacilityCombo.getItems().clear();
                }
            });
        }

        if (departureTimeField != null) {
            departureTimeField.setPromptText("HH:mm:ss");
        }

        if (progressIndicator != null) {
            progressIndicator.setVisible(false);
        }
    }

    public void initController() {
        if (trainTable != null) {
            loadTrainsAsync();
            loadComboBoxDataAsync();
        } else {
            System.err.println("❌ Fatal: initController aborted. trainTable is null. (FX:ID mismatch likely)");
            if (mainController != null) {
                mainController.showNotification("Error: Table initialization failed (FX:ID mismatch).", "error");
            }
        }
    }

    // =================================================================
    // LÓGICA DE PERSISTÊNCIA (CREATE / UPDATE / DELETE)
    // =================================================================

    @FXML
    public void saveOrUpdateTrain() {
        String trainId = trainIdField.getText();
        String operatorId = operatorIdCombo.getValue();
        LocalDate date = departureDateField.getValue();
        String timeStr = departureTimeField.getText();

        Map.Entry<Integer, String> startFacilityEntry = startFacilityCombo.getValue();
        Map.Entry<Integer, String> endFacilityEntry = endFacilityCombo.getValue();

        String locomotiveId = locomotiveIdCombo.getValue();
        String routeId = routeIdCombo.getValue();

        if (trainId.isEmpty() || operatorId == null || date == null || timeStr.isEmpty() || startFacilityEntry == null || endFacilityEntry == null || routeId == null || locomotiveId == null) {
            mainController.showNotification("Error: All required fields (IDs, Date/Time, Route, Loco) must be selected.", "error");
            return;
        }

        try {
            LocalDateTime departureTime = date.atTime(LocalTime.parse(timeStr));
            int startFacilityId = startFacilityEntry.getKey();
            int endFacilityId = endFacilityEntry.getKey();

            Train newTrain = new Train(trainId, operatorId, departureTime,
                    startFacilityId, endFacilityId, locomotiveId, routeId);

            boolean isUpdate = trainRepository.findById(trainId).isPresent();

            // Lógica de Save/Update (Assíncrona para DB)
            Task<Void> saveTask = new Task<>() {
                @Override
                protected Void call() throws Exception {

                    if (isUpdate) {
                        performUpdate(newTrain); // DML REAL
                    } else {
                        performInsert(newTrain); // DML REAL
                    }
                    return null;
                }

                @Override
                protected void succeeded() {
                    mainController.showNotification(
                            (isUpdate ? "Train Updated: " : "Train Created: ") + trainId, "success");
                    loadTrainsAsync();
                    clearForm();
                }
                @Override
                protected void failed() {
                    mainController.showNotification("Operation Failed: " + getException().getMessage(), "error");
                    getException().printStackTrace();
                }
            };
            new Thread(saveTask).start();

        } catch (DateTimeParseException e) {
            mainController.showNotification("Error: Invalid time format. Use HH:mm:ss.", "error");
        }
    }

    @FXML
    public void deleteTrain() {
        Train selectedTrain = trainTable.getSelectionModel().getSelectedItem();
        if (selectedTrain == null) {
            mainController.showNotification("Error: Select a train to delete.", "error");
            return;
        }

        // Lógica de Delete (Assíncrona para DB)
        Task<Void> deleteTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                performDelete(selectedTrain.getTrainId()); // DML REAL
                return null;
            }

            @Override
            protected void succeeded() {
                mainController.showNotification("Train Deleted: " + selectedTrain.getTrainId(), "success");
                loadTrainsAsync();
                clearForm();
            }
            @Override
            protected void failed() {
                mainController.showNotification("Delete Failed: " + getException().getMessage(), "error");
                getException().printStackTrace();
            }
        };
        new Thread(deleteTask).start();
    }

    // =================================================================
    // MÉTODOS DE PERSISTÊNCIA JDBC REAIS
    // =================================================================

    private void performInsert(Train train) throws SQLException {
        // Assume que a tabela TRAIN existe com as colunas corretas e que as FKs são válidas (Route, Loco, Facility)
        String sql = "INSERT INTO TRAIN (train_id, operator_id, train_date, train_time, start_facility_id, end_facility_id, locomotive_id, route_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection(); // Obtém a conexão
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, train.getTrainId());
            stmt.setString(2, train.getOperatorId());
            // Conversão de LocalDate/LocalTime para tipos SQL (Oracle)
            stmt.setDate(3, java.sql.Date.valueOf(train.getDepartureTime().toLocalDate()));
            stmt.setString(4, train.getDepartureTime().toLocalTime().toString()); // Time como String (HH:MM:SS)
            stmt.setInt(5, train.getStartFacilityId());
            stmt.setInt(6, train.getEndFacilityId());
            stmt.setString(7, train.getLocomotiveId());
            stmt.setString(8, train.getRouteId());

            stmt.executeUpdate();
            System.out.println("DB Action: INSERT successful for Train " + train.getTrainId());
        }
    }

    private void performUpdate(Train train) throws SQLException {
        String sql = "UPDATE TRAIN SET " +
                "operator_id = ?, train_date = ?, train_time = ?, start_facility_id = ?, end_facility_id = ?, locomotive_id = ?, route_id = ? " +
                "WHERE train_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, train.getOperatorId());
            stmt.setDate(2, java.sql.Date.valueOf(train.getDepartureTime().toLocalDate()));
            stmt.setString(3, train.getDepartureTime().toLocalTime().toString());
            stmt.setInt(4, train.getStartFacilityId());
            stmt.setInt(5, train.getEndFacilityId());
            stmt.setString(6, train.getLocomotiveId());
            stmt.setString(7, train.getRouteId());
            stmt.setString(8, train.getTrainId()); // Where clause

            stmt.executeUpdate();
            System.out.println("DB Action: UPDATE successful for Train " + train.getTrainId());
        }
    }

    private void performDelete(String trainId) throws SQLException {
        String sql = "DELETE FROM TRAIN WHERE train_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, trainId);

            stmt.executeUpdate();
            System.out.println("DB Action: DELETE successful for Train " + trainId);
        }
    }

    // =================================================================
    // MÉTODOS AUXILIARES (LOAD e FORMATTING)
    // =================================================================

    public void loadTrainsAsync() {
        if (trainRepository == null) return;

        if (progressIndicator != null) progressIndicator.setVisible(true);

        Task<List<Train>> loadTask = new Task<>() {
            @Override
            protected List<Train> call() throws Exception {
                return trainRepository.findAll();
            }
            @Override
            protected void succeeded() {
                if (trainTable != null) {
                    observableTrains = FXCollections.observableArrayList(getValue());
                    trainTable.setItems(observableTrains);
                } else {
                    System.err.println("❌ ERROR: trainTable is NULL in succeeded() block.");
                }

                if (progressIndicator != null) progressIndicator.setVisible(false);
            }
            @Override
            protected void failed() {
                mainController.showNotification("Failed to load trains for CRUD.", "error");
                if (progressIndicator != null) progressIndicator.setVisible(false);
                getException().printStackTrace();
            }
        };
        new Thread(loadTask).start();
    }

    private void loadComboBoxDataAsync() {
        Task<Void> loadDataTask = new Task<>() {
            @Override
            protected Void call() throws Exception {

                List<Map.Entry<Integer, String>> facilities = facilityMap.entrySet().stream()
                        .collect(Collectors.toList());

                List<String> locomotiveIds = locomotiveRepository.findAll().stream()
                        .map(l -> l.getLocomotiveId())
                        .collect(Collectors.toList());

                List<String> operatorIds = trainRepository.findAllOperators();
                List<String> routeIds = trainRepository.findAllRouteIds();

                javafx.application.Platform.runLater(() -> {
                    ObservableList<Map.Entry<Integer, String>> observableFacilities = FXCollections.observableArrayList(facilities);
                    startFacilityCombo.setItems(observableFacilities);
                    endFacilityCombo.setItems(observableFacilities);

                    locomotiveIdCombo.setItems(FXCollections.observableArrayList(locomotiveIds));
                    operatorIdCombo.setItems(FXCollections.observableArrayList(operatorIds));
                    routeIdCombo.setItems(FXCollections.observableArrayList(routeIds));

                    startFacilityCombo.setConverter(new FacilityConverter(facilityMap));
                    endFacilityCombo.setConverter(new FacilityConverter(facilityMap));
                });
                return null;
            }
            @Override
            protected void failed() {
                mainController.showNotification("Failed to load domain data for ComboBoxes.", "error");
                getException().printStackTrace();
            }
        };
        new Thread(loadDataTask).start();
    }

    private void populateForm(Train train) {
        trainIdField.setText(train.getTrainId());
        operatorIdCombo.setValue(train.getOperatorId());

        LocalDateTime departure = train.getDepartureTime();
        if (departure != null) {
            departureDateField.setValue(departure.toLocalDate());
            departureTimeField.setText(departure.toLocalTime().toString());
        } else {
            departureDateField.setValue(null);
            departureTimeField.setText("");
        }

        int startId = train.getStartFacilityId();
        int endId = train.getEndFacilityId();

        startFacilityCombo.getItems().stream()
                .filter(e -> e.getKey().equals(startId))
                .findFirst()
                .ifPresent(startFacilityCombo::setValue);

        filterEndFacilities(startId);
        endFacilityCombo.getItems().stream()
                .filter(e -> e.getKey().equals(endId))
                .findFirst()
                .ifPresent(endFacilityCombo::setValue);

        locomotiveIdCombo.setValue(train.getLocomotiveId());
        routeIdCombo.setValue(train.getRouteId());

        trainIdField.setDisable(true);
        saveButton.setText("Update Train");
    }

    @FXML
    public void clearForm() {
        trainIdField.clear();
        operatorIdCombo.getSelectionModel().clearSelection();
        departureDateField.setValue(null);
        departureTimeField.clear();
        startFacilityCombo.getSelectionModel().clearSelection();
        endFacilityCombo.getSelectionModel().clearSelection();
        locomotiveIdCombo.getSelectionModel().clearSelection();
        routeIdCombo.getSelectionModel().clearSelection();

        trainIdField.setDisable(false);
        saveButton.setText("Create Train");
        if (trainTable != null) {
            trainTable.getSelectionModel().clearSelection();
        }
    }

    private void filterEndFacilities(int startFacilityId) {
        if (networkService == null || facilityMap.isEmpty()) return;

        List<Integer> reachableIds = networkService.findAllReachableFacilities(startFacilityId);

        ObservableList<Map.Entry<Integer, String>> filteredDestinations = facilityMap.entrySet().stream()
                .filter(entry -> reachableIds.contains(entry.getKey()))
                .collect(Collectors.collectingAndThen(Collectors.toList(), FXCollections::observableArrayList));

        endFacilityCombo.setItems(filteredDestinations);
        endFacilityCombo.getSelectionModel().clearSelection();
    }

    public static class FacilityConverter extends StringConverter<Map.Entry<Integer, String>> {
        private final Map<Integer, String> map;
        public FacilityConverter(Map<Integer, String> map) { this.map = map; }

        @Override
        public String toString(Map.Entry<Integer, String> object) {
            return object != null ? object.getValue() + " (ID: " + object.getKey() + ")" : null;
        }

        @Override
        public Map.Entry<Integer, String> fromString(String string) {
            return null;
        }
    }
}