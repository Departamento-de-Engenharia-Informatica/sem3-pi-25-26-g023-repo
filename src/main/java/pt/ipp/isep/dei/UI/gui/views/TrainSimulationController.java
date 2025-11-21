package pt.ipp.isep.dei.UI.gui.views;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import pt.ipp.isep.dei.UI.gui.MainController;
import pt.ipp.isep.dei.domain.*;
import pt.ipp.isep.dei.repository.FacilityRepository;
import pt.ipp.isep.dei.repository.LocomotiveRepository;
import pt.ipp.isep.dei.repository.TrainRepository;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class TrainSimulationController {

    @FXML private TableView<TrainWrapper> trainTable;
    @FXML private TableColumn<TrainWrapper, String> idColumn;
    @FXML private TableColumn<TrainWrapper, String> departureColumn;
    @FXML private TableColumn<TrainWrapper, String> routeColumn;
    @FXML public Button runButton;
    @FXML private TextArea resultTextArea;
    @FXML private ProgressIndicator progressIndicator;

    // Dependências
    private TrainRepository trainRepository;
    private FacilityRepository facilityRepository;
    private DispatcherService dispatcherService;
    private LocomotiveRepository locomotiveRepository;
    private MainController mainController;

    private ObservableList<TrainWrapper> observableTrains;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    // Construtor vazio necessário
    public TrainSimulationController() { }

    // Injeção de dependências
    public void setDependencies(MainController mainController,
                                TrainRepository trainRepository,
                                FacilityRepository facilityRepository,
                                DispatcherService dispatcherService,
                                LocomotiveRepository locomotiveRepository) {
        this.mainController = mainController;
        this.trainRepository = trainRepository;
        this.facilityRepository = facilityRepository;
        this.dispatcherService = dispatcherService;
        this.locomotiveRepository = locomotiveRepository;
    }

    @FXML
    public void initialize() {
        if (idColumn != null) {
            idColumn.setCellValueFactory(new PropertyValueFactory<>("trainId"));
            departureColumn.setCellValueFactory(new PropertyValueFactory<>("departureTime"));
            routeColumn.setCellValueFactory(new PropertyValueFactory<>("routeDescription"));
            trainTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        }
    }

    // Inicialização manual após injetar dependências
    public void initController() {
        loadTrains();
    }

    private void loadTrains() {
        if (trainRepository == null || facilityRepository == null) return;

        List<Train> allTrains = trainRepository.findAll();
        List<TrainWrapper> wrappers = allTrains.stream()
                .map(t -> new TrainWrapper(t, facilityRepository))
                .collect(Collectors.toList());

        observableTrains = FXCollections.observableArrayList(wrappers);
        trainTable.setItems(observableTrains);
    }

    @FXML
    public void runSimulation() {
        if (trainTable.getSelectionModel().getSelectedItems().isEmpty()) {
            mainController.showNotification("Nenhum comboio selecionado.", "error");
            return;
        }

        // TODO: lógica da simulação
        resultTextArea.setText("Simulação executada com sucesso!");
    }

    public static class TrainWrapper {
        private final Train train;
        private final String trainId;
        private final String departureTime;
        private final String routeDescription;

        public TrainWrapper(Train t, FacilityRepository facilityRepository) {
            this.train = t;
            this.trainId = t.getTrainId();
            this.departureTime = t.getDepartureTime().toLocalTime().format(TIME_FORMATTER);

            String startName = facilityRepository.findNameById(t.getStartFacilityId()).orElse("F" + t.getStartFacilityId());
            String endName = facilityRepository.findNameById(t.getEndFacilityId()).orElse("F" + t.getEndFacilityId());
            this.routeDescription = startName + " -> " + endName + " | Loco: " + t.getLocomotiveId();
        }

        public Train getTrain() { return train; }
        public String getTrainId() { return trainId; }
        public String getDepartureTime() { return departureTime; }
        public String getRouteDescription() { return routeDescription; }
    }
}
