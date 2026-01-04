package pt.ipp.isep.dei.UI.gui.views;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import javafx.util.StringConverter;
import pt.ipp.isep.dei.UI.gui.MainController;
import pt.ipp.isep.dei.domain.*;
import pt.ipp.isep.dei.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class TrainCRUDController {

    // --- FXML TABLE ---
    @FXML private TableView<Train> trainTable;
    @FXML private TableColumn<Train, String> idColumn;
    @FXML private TableColumn<Train, String> operatorColumn;
    @FXML private TableColumn<Train, LocalDateTime> departureColumn;
    @FXML private TableColumn<Train, Integer> startFacilityColumn;
    @FXML private TableColumn<Train, Integer> endFacilityColumn;
    @FXML private TableColumn<Train, String> routeColumn;

    // --- FXML FORM ---
    @FXML private TextField trainIdField;
    @FXML private ComboBox<String> operatorIdCombo;
    @FXML private DatePicker departureDateField;
    @FXML private TextField departureTimeField;
    @FXML private ComboBox<Map.Entry<Integer, String>> startFacilityCombo;
    @FXML private ComboBox<Map.Entry<Integer, String>> endFacilityCombo;
    @FXML private ComboBox<String> routeIdCombo;
    @FXML private Button saveButton;
    @FXML private ProgressIndicator progressIndicator;

    // --- FXML COCKPIT (USLP09) ---
    @FXML private ComboBox<RollingStockItem<Locomotive>> cmbLocomotive;
    @FXML private ListView<RollingStockItem<Wagon>> wagonListView;

    @FXML private ProgressBar powerProgressBar;
    @FXML private Label lblPowerStats;
    @FXML private Label lblPowerStatus;
    @FXML private ProgressBar lengthProgressBar;
    @FXML private Label lblLengthStats;
    @FXML private Label lblLengthStatus;
    @FXML private Label lblTotalMass;
    @FXML private Label lblWagonCount;

    // Dependencies
    private MainController mainController;
    private TrainRepository trainRepository;
    private FacilityRepository facilityRepository;
    private LocomotiveRepository locomotiveRepository;
    private RailwayNetworkService networkService;
    private WagonRepository wagonRepository;
    private StationRepository stationRepository;

    private Map<Integer, String> facilityMap;

    // CACHE DE DADOS (Carregados em Paralelo)
    private List<EuropeanStation> allStationsCache = new ArrayList<>();
    private List<Locomotive> allLocomotivesCache = new ArrayList<>();
    private List<Wagon> allWagonsCache = new ArrayList<>();

    // --- CONTROLO DE EDIÇÃO ---
    private String pendingLocomotiveId = null;
    private List<String> pendingWagonIds = new ArrayList<>();

    // Constants
    private static final double ROUTE_MAX_LENGTH_METERS = 500.0;

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
        this.wagonRepository = new WagonRepository();
        this.stationRepository = new StationRepository();

        setupTable();
        setupFormListeners();
        setupValidationListeners();
        setupCustomCellFactories();

        // ATUALIZAÇÃO: routeIdCombo.valueProperty().isNull() removido para permitir criação manual
        BooleanBinding invalidFields = trainIdField.textProperty().isEmpty()
                .or(operatorIdCombo.valueProperty().isNull())
                .or(departureDateField.valueProperty().isNull())
                .or(departureTimeField.textProperty().isEmpty())
                .or(startFacilityCombo.valueProperty().isNull())
                .or(endFacilityCombo.valueProperty().isNull())
                .or(cmbLocomotive.valueProperty().isNull())
                .or(Bindings.size(wagonListView.getSelectionModel().getSelectedItems()).lessThan(1));

        saveButton.disableProperty().bind(invalidFields);
        saveButton.opacityProperty().bind(Bindings.when(invalidFields).then(0.3).otherwise(1.0));
    }

    private void setupTable() {
        if (trainTable != null && idColumn != null) {
            idColumn.setCellValueFactory(new PropertyValueFactory<>("trainId"));
            operatorColumn.setCellValueFactory(new PropertyValueFactory<>("operatorId"));
            departureColumn.setCellValueFactory(new PropertyValueFactory<>("departureTime"));
            startFacilityColumn.setCellValueFactory(new PropertyValueFactory<>("startFacilityId"));
            endFacilityColumn.setCellValueFactory(new PropertyValueFactory<>("endFacilityId"));
            if (routeColumn != null) routeColumn.setCellValueFactory(new PropertyValueFactory<>("routeId"));

            trainTable.getSelectionModel().selectedItemProperty().addListener((obs, oldS, newS) -> {
                if (newS != null) populateForm(newS);
            });
        }
    }

    private void setupFormListeners() {
        if (startFacilityCombo != null) {
            startFacilityCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    if (networkService != null) filterEndFacilities(newVal.getKey());
                    updateAvailableStockAsync(newVal.getKey());
                } else {
                    endFacilityCombo.getItems().clear();
                }
            });
        }

        // --- FUNCIONALIDADE: Importar Rota (USLP09) ---
        if (routeIdCombo != null) {
            routeIdCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldRoute, newRouteId) -> {
                if (newRouteId != null) {
                    importRouteDetails(newRouteId);
                }
            });
        }
    }

    /**
     * Procura os detalhes da rota na base de dados e preenche o formulário automaticamente.
     */
    private void importRouteDetails(String routeId) {
        Task<Optional<Map<String, Object>>> task = new Task<>() {
            @Override
            protected Optional<Map<String, Object>> call() {
                return trainRepository.findRouteDetailsById(routeId);
            }

            @Override
            protected void succeeded() {
                getValue().ifPresent(details -> {
                    int startId = (int) details.get("start");
                    int endId = (int) details.get("end");

                    startFacilityCombo.getItems().stream()
                            .filter(e -> e.getKey() == startId)
                            .findFirst()
                            .ifPresent(startFacilityCombo::setValue);

                    filterEndFacilities(startId);

                    endFacilityCombo.getItems().stream()
                            .filter(e -> e.getKey() == endId)
                            .findFirst()
                            .ifPresent(endFacilityCombo::setValue);

                    mainController.showNotification("Rota " + routeId + " importada com sucesso.", "info");
                });
            }
        };
        new Thread(task).start();
    }

    private void setupCustomCellFactories() {
        Callback<ListView<RollingStockItem<Locomotive>>, ListCell<RollingStockItem<Locomotive>>> locoCellFactory = lv -> new ListCell<>() {
            @Override protected void updateItem(RollingStockItem<Locomotive> item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item.toString());
            }
        };
        cmbLocomotive.setButtonCell(locoCellFactory.call(null));
        cmbLocomotive.setCellFactory(locoCellFactory);

        wagonListView.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(RollingStockItem<Wagon> item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item.toString());
            }
        });
    }

    public void initController() {
        if(progressIndicator != null) progressIndicator.setVisible(true);
        System.out.println("🚀 A iniciar carregamento paralelo de dados...");

        CompletableFuture<Void> stationsFuture = CompletableFuture.runAsync(() -> {
            try { allStationsCache = stationRepository.findAll(); } catch (Exception e) { e.printStackTrace(); }
        });

        CompletableFuture<Void> locosFuture = CompletableFuture.runAsync(() -> {
            try { allLocomotivesCache = locomotiveRepository.findAll(); } catch (Exception e) { e.printStackTrace(); }
        });

        CompletableFuture<Void> wagonsFuture = CompletableFuture.runAsync(() -> {
            try { allWagonsCache = wagonRepository.findAll(); } catch (Exception e) { e.printStackTrace(); }
        });

        CompletableFuture.allOf(stationsFuture, locosFuture, wagonsFuture)
                .thenRun(() -> Platform.runLater(() -> {
                    loadComboBoxDataAsync();
                    if(progressIndicator != null) progressIndicator.setVisible(false);
                }));

        loadTrainsAsync();
    }

    private void loadTrainsAsync() {
        Task<List<Train>> task = new Task<>() {
            @Override
            protected List<Train> call() { return trainRepository.findAll(); }
            @Override
            protected void succeeded() { trainTable.setItems(FXCollections.observableArrayList(getValue())); }
        };
        new Thread(task).start();
    }

    private void loadComboBoxDataAsync() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                List<String> ops = trainRepository.findAllOperators();
                List<String> routes = trainRepository.findAllRouteIds();

                Platform.runLater(() -> {
                    operatorIdCombo.setItems(FXCollections.observableArrayList(ops));
                    routeIdCombo.setItems(FXCollections.observableArrayList(routes));

                    ObservableList<Map.Entry<Integer, String>> facilities = FXCollections.observableArrayList(facilityMap.entrySet());
                    startFacilityCombo.setItems(facilities);
                    endFacilityCombo.setItems(facilities);
                    startFacilityCombo.setConverter(new FacilityConverter(facilityMap));
                    endFacilityCombo.setConverter(new FacilityConverter(facilityMap));
                });
                return null;
            }
        };
        new Thread(task).start();
    }

    private void populateForm(Train train) {
        trainIdField.setText(train.getTrainId());
        operatorIdCombo.setValue(train.getOperatorId());
        if (train.getDepartureTime() != null) {
            departureDateField.setValue(train.getDepartureTime().toLocalDate());
            departureTimeField.setText(train.getDepartureTime().toLocalTime().toString());
        }
        routeIdCombo.setValue(train.getRouteId());

        this.pendingLocomotiveId = train.getLocomotiveId();

        Task<List<String>> loadConsistTask = new Task<>() {
            @Override
            protected List<String> call() {
                return trainRepository.getTrainConsist(train.getTrainId());
            }

            @Override
            protected void succeeded() {
                pendingWagonIds = getValue();

                startFacilityCombo.getItems().stream()
                        .filter(e -> e.getKey() == train.getStartFacilityId())
                        .findFirst()
                        .ifPresent(startFacilityCombo::setValue);

                endFacilityCombo.getItems().stream()
                        .filter(e -> e.getKey() == train.getEndFacilityId())
                        .findFirst()
                        .ifPresent(endFacilityCombo::setValue);
            }
        };
        new Thread(loadConsistTask).start();
    }

    private void updateAvailableStockAsync(int startStationId) {
        if(progressIndicator != null) progressIndicator.setVisible(true);

        cmbLocomotive.getItems().clear();
        wagonListView.getItems().clear();

        Task<Map<String, List<?>>> calculationTask = new Task<>() {
            @Override
            protected Map<String, List<?>> call() {
                Map<String, List<?>> results = new HashMap<>();
                if (allStationsCache == null || allStationsCache.isEmpty()) return results;

                EuropeanStation startStation = allStationsCache.stream()
                        .filter(s -> s.getIdEstacao() == startStationId)
                        .findFirst().orElse(null);

                if (startStation == null) return results;

                List<RollingStockItem<Locomotive>> wrappedLocos = new ArrayList<>();
                List<RollingStockItem<Wagon>> wrappedWagons = new ArrayList<>();

                if (allLocomotivesCache != null) {
                    for (Locomotive l : allLocomotivesCache) {
                        EuropeanStation currentLoc = getRandomStation();
                        String status = (Math.random() > 0.8) ? "IN_TRANSIT" : "PARKED";
                        double dist = 0.0;
                        if (currentLoc != null && startStation.getIdEstacao() != currentLoc.getIdEstacao()) {
                            RailwayPath path = networkService.findFastestPath(startStation.getIdEstacao(), currentLoc.getIdEstacao(), 100.0);
                            if (path != null) dist = path.getTotalDistance();
                            else { dist = 99999.9; status = "UNREACHABLE"; }
                        }
                        String locName = (currentLoc != null) ? currentLoc.getStation() : "Unknown";
                        wrappedLocos.add(new RollingStockItem<>(l, status, locName, dist));
                    }
                }

                if (allWagonsCache != null) {
                    for (Wagon w : allWagonsCache) {
                        EuropeanStation currentLoc = getRandomStation();
                        String status = (Math.random() > 0.8) ? "IN_TRANSIT" : "PARKED";
                        double dist = 0.0;
                        if (currentLoc != null && startStation.getIdEstacao() != currentLoc.getIdEstacao()) {
                            RailwayPath path = networkService.findFastestPath(startStation.getIdEstacao(), currentLoc.getIdEstacao(), 100.0);
                            if (path != null) dist = path.getTotalDistance();
                            else { dist = 99999.9; status = "UNREACHABLE"; }
                        }
                        String locName = (currentLoc != null) ? currentLoc.getStation() : "Unknown";
                        wrappedWagons.add(new RollingStockItem<>(w, status, locName, dist));
                    }
                }

                Comparator<RollingStockItem<?>> sorter = (o1, o2) -> {
                    int statusCompare = o1.getStatus().compareTo(o2.getStatus());
                    if (statusCompare != 0) return statusCompare;
                    if ("PARKED".equals(o1.getStatus())) return Double.compare(o1.getDistanceKm(), o2.getDistanceKm());
                    return 0;
                };

                wrappedLocos.sort(sorter);
                wrappedWagons.sort(sorter);
                results.put("locos", wrappedLocos);
                results.put("wagons", wrappedWagons);
                return results;
            }

            @Override
            protected void succeeded() {
                if(progressIndicator != null) progressIndicator.setVisible(false);
                Map<String, List<?>> res = getValue();
                if (res != null && !res.isEmpty()) {
                    List<RollingStockItem<Locomotive>> locos = (List<RollingStockItem<Locomotive>>) res.get("locos");
                    List<RollingStockItem<Wagon>> wagons = (List<RollingStockItem<Wagon>>) res.get("wagons");
                    cmbLocomotive.setItems(FXCollections.observableArrayList(locos));
                    wagonListView.setItems(FXCollections.observableArrayList(wagons));

                    if (pendingLocomotiveId != null) {
                        cmbLocomotive.getItems().stream()
                                .filter(wrapper -> wrapper.getItem().getLocomotiveId().equals(pendingLocomotiveId))
                                .findFirst().ifPresent(cmbLocomotive::setValue);
                        pendingLocomotiveId = null;
                    }

                    if (pendingWagonIds != null && !pendingWagonIds.isEmpty()) {
                        wagonListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
                        for (RollingStockItem<Wagon> wrapper : wagonListView.getItems()) {
                            if (pendingWagonIds.contains(wrapper.getItem().getIdWagon())) {
                                wagonListView.getSelectionModel().select(wrapper);
                            }
                        }
                        pendingWagonIds.clear();
                    }
                }
            }
        };
        new Thread(calculationTask).start();
    }

    private void setupValidationListeners() {
        if(cmbLocomotive != null)
            cmbLocomotive.valueProperty().addListener((obs, o, n) -> updateValidationPanel());

        if(wagonListView != null) {
            wagonListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            wagonListView.getSelectionModel().getSelectedItems().addListener((ListChangeListener<RollingStockItem<Wagon>>) c -> updateValidationPanel());
        }
    }

    private void updateValidationPanel() {
        RollingStockItem<Locomotive> locItem = cmbLocomotive.getValue();
        List<RollingStockItem<Wagon>> wagonItems = wagonListView.getSelectionModel().getSelectedItems();

        if (locItem == null) {
            resetValidationPanel();
            return;
        }

        Locomotive selectedLoco = locItem.getItem();
        List<Wagon> selectedWagons = wagonItems.stream().map(RollingStockItem::getItem).collect(Collectors.toList());

        double totalMassKg = selectedLoco.getTotalWeightKg();
        double totalLengthM = selectedLoco.getLengthMeters();

        for (Wagon w : selectedWagons) {
            totalMassKg += w.getGrossWeightKg();
            totalLengthM += w.getLengthMeters();
        }
        double totalMassTons = totalMassKg / 1000.0;

        double lenRatio = totalLengthM / ROUTE_MAX_LENGTH_METERS;
        if(lengthProgressBar != null) lengthProgressBar.setProgress(Math.min(lenRatio, 1.0));
        if(lblLengthStats != null) lblLengthStats.setText(String.format("%.1f / %.0f m", totalLengthM, ROUTE_MAX_LENGTH_METERS));

        if (totalLengthM > ROUTE_MAX_LENGTH_METERS) {
            if(lengthProgressBar != null) lengthProgressBar.setStyle("-fx-accent: red;");
            if(lblLengthStatus != null) lblLengthStatus.setText("TOO LONG!");
        } else {
            if(lengthProgressBar != null) lengthProgressBar.setStyle("-fx-accent: green;");
            if(lblLengthStatus != null) lblLengthStatus.setText("OK");
        }

        double powerRequired = totalMassTons * 1.5;
        double powerAvailable = selectedLoco.getPowerKW();
        double powerRatio = (powerAvailable > 0) ? powerRequired / powerAvailable : 1.1;

        if(powerProgressBar != null) powerProgressBar.setProgress(Math.min(powerRatio, 1.0));
        if(lblPowerStats != null) lblPowerStats.setText(String.format("Load: %.0ft | Need: ~%.0fkW | Has: %.0fkW", totalMassTons, powerRequired, powerAvailable));

        if (powerRatio > 1.0) {
            if(powerProgressBar != null) powerProgressBar.setStyle("-fx-accent: red;");
            if(lblPowerStatus != null) lblPowerStatus.setText("OVERLOAD!");
        } else {
            if(powerProgressBar != null) powerProgressBar.setStyle("-fx-accent: green;");
            if(lblPowerStatus != null) lblPowerStatus.setText("OPTIMAL");
        }

        if(lblTotalMass != null) lblTotalMass.setText(String.format("Mass: %.0f tons", totalMassTons));
        if(lblWagonCount != null) lblWagonCount.setText("Wagons: " + selectedWagons.size());
    }

    private void resetValidationPanel() {
        if(powerProgressBar != null) powerProgressBar.setProgress(0);
        if(lengthProgressBar != null) lengthProgressBar.setProgress(0);
        if(lblPowerStatus != null) lblPowerStatus.setText("-");
        if(lblLengthStatus != null) lblLengthStatus.setText("-");
    }

    @FXML
    public void saveOrUpdateTrain() {
        if (lblPowerStatus != null && lblPowerStatus.getText().equals("OVERLOAD!")) {
            mainController.showNotification("Safety Check Failed: Power Overload.", "error");
            return;
        }
        if (lblLengthStatus != null && lblLengthStatus.getText().equals("TOO LONG!")) {
            mainController.showNotification("Safety Check Failed: Train too long.", "error");
            return;
        }

        String trainId = trainIdField.getText();
        String operatorId = operatorIdCombo.getValue();
        LocalDate date = departureDateField.getValue();
        String timeStr = departureTimeField.getText();
        Map.Entry<Integer, String> startEntry = startFacilityCombo.getValue();
        Map.Entry<Integer, String> endEntry = endFacilityCombo.getValue();

        RollingStockItem<Locomotive> locoWrapper = cmbLocomotive.getValue();
        String routeId = routeIdCombo.getValue(); // Agora pode ser null

        // Validação: routeId removido dos obrigatórios
        if (trainId.isEmpty() || operatorId == null || date == null || timeStr.isEmpty() ||
                startEntry == null || endEntry == null || locoWrapper == null) {
            mainController.showNotification("Erro: Preencha todos os campos obrigatórios.", "error");
            return;
        }

        try {
            LocalDateTime departureTime = date.atTime(LocalTime.parse(timeStr));
            Train newTrain = new Train(trainId, operatorId, departureTime,
                    startEntry.getKey(), endEntry.getKey(), locoWrapper.getItem().getLocomotiveId(), routeId);

            List<String> wagonIds = wagonListView.getSelectionModel().getSelectedItems().stream()
                    .map(w -> w.getItem().getIdWagon())
                    .collect(Collectors.toList());

            Task<Boolean> saveTask = new Task<>() {
                @Override
                protected Boolean call() throws Exception {
                    return trainRepository.saveTrainWithConsist(newTrain, wagonIds);
                }
                @Override
                protected void succeeded() {
                    if (getValue()) {
                        mainController.showNotification("Comboio gravado com sucesso!", "success");
                        loadTrainsAsync();
                        clearForm();
                    } else {
                        mainController.showNotification("Erro BD: Falha ao gravar.", "error");
                    }
                }
                @Override
                protected void failed() {
                    mainController.showNotification("Erro: " + getException().getMessage(), "error");
                }
            };
            new Thread(saveTask).start();

        } catch (DateTimeParseException e) {
            mainController.showNotification("Formato de hora inválido.", "error");
        }
    }

    @FXML
    public void deleteTrain() {
        mainController.showNotification("Delete not implemented in this demo.", "info");
    }

    @FXML
    public void clearForm() {
        trainIdField.clear();
        operatorIdCombo.getSelectionModel().clearSelection();
        departureDateField.setValue(null);
        departureTimeField.clear();
        startFacilityCombo.getSelectionModel().clearSelection();
        endFacilityCombo.getSelectionModel().clearSelection();
        cmbLocomotive.getSelectionModel().clearSelection();
        routeIdCombo.getSelectionModel().clearSelection();
        wagonListView.getSelectionModel().clearSelection();
        resetValidationPanel();
        this.pendingLocomotiveId = null;
        this.pendingWagonIds.clear();
    }

    private void filterEndFacilities(int startId) {
        if (networkService != null) {
            List<Integer> reachable = networkService.findAllReachableFacilities(startId);
            ObservableList<Map.Entry<Integer, String>> filtered = facilityMap.entrySet().stream()
                    .filter(e -> reachable.contains(e.getKey()))
                    .collect(Collectors.collectingAndThen(Collectors.toList(), FXCollections::observableArrayList));
            endFacilityCombo.setItems(filtered);
        }
    }

    private EuropeanStation getRandomStation() {
        if (allStationsCache == null || allStationsCache.isEmpty()) return null;
        return allStationsCache.get(new Random().nextInt(allStationsCache.size()));
    }

    public static class FacilityConverter extends StringConverter<Map.Entry<Integer, String>> {
        private final Map<Integer, String> map;
        public FacilityConverter(Map<Integer, String> map) { this.map = map; }
        @Override public String toString(Map.Entry<Integer, String> o) { return o != null ? o.getValue() : null; }
        @Override public Map.Entry<Integer, String> fromString(String s) { return null; }
    }

    public static class RollingStockItem<T> {
        private final T item;
        private final String status;
        private final String locationName;
        private final double distanceKm;

        public RollingStockItem(T item, String status, String locationName, double distanceKm) {
            this.item = item;
            this.status = status;
            this.locationName = locationName;
            this.distanceKm = distanceKm;
        }

        public T getItem() { return item; }
        public String getStatus() { return status; }
        public double getDistanceKm() { return distanceKm; }

        @Override
        public String toString() {
            String itemName = "";
            if (item instanceof Locomotive) itemName = ((Locomotive)item).getModelo();
            else if (item instanceof Wagon) itemName = "Wagon " + ((Wagon)item).getIdWagon();

            if ("IN_TRANSIT".equals(status)) return String.format("%s (In Transit to %s)", itemName, locationName);
            else if ("UNREACHABLE".equals(status)) return String.format("%s (Unreachable at %s)", itemName, locationName);
            else return String.format("%s (Parked at %s - %.1f km away)", itemName, locationName, distanceKm);
        }
    }
}