package pt.ipp.isep.dei.UI.gui.views;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import pt.ipp.isep.dei.UI.gui.MainController;
import pt.ipp.isep.dei.DatabaseConnection.DatabaseConnection;
import pt.ipp.isep.dei.UI.gui.utils.RailMapVisualizer;
import pt.ipp.isep.dei.domain.*;
import pt.ipp.isep.dei.repository.FacilityRepository;
import pt.ipp.isep.dei.repository.LocomotiveRepository;
import pt.ipp.isep.dei.repository.WagonRepository;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class TrainSimulationController {

    // --- ELEMENTOS EXISTENTES ---
    @FXML private TableView<TrainWrapper> trainTable;
    @FXML private TableColumn<TrainWrapper, String> idColumn;
    @FXML private TableColumn<TrainWrapper, String> departureColumn;
    @FXML private TableColumn<TrainWrapper, String> routeColumn;
    @FXML public Button runButton;
    @FXML private TextArea resultTextArea;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Button selectAllButton;

    // --- ELEMENTOS PARA O GRÁFICO ---
    @FXML private BorderPane mapContainer;
    @FXML private Slider speedSlider;

    // Dependências
    private FacilityRepository facilityRepository;
    private DispatcherService dispatcherService;
    private LocomotiveRepository locomotiveRepository;
    private MainController mainController;
    private WagonRepository wagonRepository;

    // Visualizador
    private RailMapVisualizer railVisualizer;

    private ObservableList<TrainWrapper> observableTrains;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public TrainSimulationController() { }

    public void setDependencies(MainController mainController,
                                FacilityRepository facilityRepository,
                                DispatcherService dispatcherService,
                                LocomotiveRepository locomotiveRepository) {
        this.mainController = mainController;
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
            trainTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            runButton.getStyleClass().add("run-button");
        }
        if (progressIndicator != null) {
            progressIndicator.setVisible(false);
        }
        if (resultTextArea != null) {
            resultTextArea.getStyleClass().add("result-text-area");
            resultTextArea.setEditable(false);

            // [CORREÇÃO FINAL] Fonte Monospaced + Texto BRANCO + Fundo ESCURO
            resultTextArea.setFont(Font.font("Monospaced", FontWeight.BOLD, 12));
            resultTextArea.setStyle("-fx-font-family: 'Monospaced'; -fx-font-size: 12px; -fx-text-fill: white; -fx-control-inner-background: #0f172a;");
        }

        // --- Configuração do Visualizador Gráfico ---
        this.railVisualizer = new RailMapVisualizer();

        if (mapContainer != null) {
            mapContainer.setCenter(railVisualizer);
        }

        if (speedSlider != null) {
            speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (railVisualizer != null) {
                    railVisualizer.setSpeedFactor(newVal.doubleValue());
                }
            });
        }
    }

    public void initController() {
        loadTrainsAsync();
    }

    @FXML
    public void selectAllTrains() {
        if (trainTable != null) {
            trainTable.getSelectionModel().selectAll();
            mainController.showNotification("All trains selected.", "info");
        }
    }

    private void loadTrainsAsync() {
        if (facilityRepository == null || mainController == null) {
            System.err.println("Error: Essential dependencies not initialized.");
            if (mainController != null) {
                mainController.showNotification("Fatal Error: Simulation dependencies absent.", "error");
            }
            return;
        }

        if (progressIndicator != null && trainTable != null && runButton != null) {
            progressIndicator.setVisible(true);
            trainTable.setDisable(true);
            runButton.setDisable(true);
            mainController.showNotification("Loading trains... Please wait.", "info");
        }

        Task<List<TrainWrapper>> loadTask = new Task<>() {
            @Override
            protected List<TrainWrapper> call() throws Exception {
                List<Train> trains = new ArrayList<>();
                WagonRepository wagonRepo = new WagonRepository();

                String sql = "SELECT train_id, operator_id, train_date, train_time, start_facility_id, end_facility_id, locomotive_id, route_id FROM TRAIN ORDER BY train_id";

                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sql);
                     ResultSet rs = stmt.executeQuery()) {

                    while (rs.next()) {
                        try {
                            String trainId = rs.getString("train_id");
                            String operatorId = rs.getString("operator_id");
                            String locoId = rs.getString("locomotive_id");
                            String routeId = rs.getString("route_id");
                            int startFacilityId = rs.getInt("start_facility_id");
                            int endFacilityId = rs.getInt("end_facility_id");
                            Date date = rs.getDate("train_date");
                            String timeStr = rs.getString("train_time");

                            LocalDateTime departureTime = null;
                            if (date != null && timeStr != null && !timeStr.isEmpty()) {
                                String timePart = timeStr.length() >= 8 ? timeStr.substring(0, 8) : timeStr;
                                departureTime = date.toLocalDate().atTime(LocalTime.parse(timePart));
                            }

                            Train train = new Train(trainId, operatorId, departureTime, startFacilityId, endFacilityId, locoId, routeId);
                            train.setWagons(wagonRepo.findWagonsByTrainId(trainId));
                            trains.add(train);

                        } catch (Exception e) {
                            System.err.println("Error parsing train: " + e.getMessage());
                        }
                    }
                }
                return trains.stream().map(t -> new TrainWrapper(t, facilityRepository)).collect(Collectors.toList());
            }

            @Override
            protected void succeeded() {
                observableTrains = FXCollections.observableArrayList(getValue());
                trainTable.setItems(observableTrains);
                progressIndicator.setVisible(false);
                trainTable.setDisable(false);
                runButton.setDisable(false);
                mainController.showNotification("Trains loaded (" + getValue().size() + ").", "success");
            }

            @Override
            protected void failed() {
                progressIndicator.setVisible(false);
                trainTable.setDisable(false);
                runButton.setDisable(false);
                mainController.showNotification("Error loading trains.", "error");
            }
        };
        new Thread(loadTask).start();
    }

    @FXML
    public void runSimulation() {
        List<TrainWrapper> selectedWrappers = trainTable.getSelectionModel().getSelectedItems();
        if (selectedWrappers.isEmpty()) {
            mainController.showNotification("No trains selected.", "error");
            return;
        }

        resultTextArea.setText("Calculating physics and scheduling routes...");
        progressIndicator.setVisible(true);
        runButton.setDisable(true);

        if (railVisualizer != null) railVisualizer.stopAnimation();

        List<Train> trainsToSimulate = selectedWrappers.stream().map(TrainWrapper::getTrain).collect(Collectors.toList());

        Task<SchedulerResult> simulationTask = new Task<>() {
            @Override
            protected SchedulerResult call() throws Exception {
                return dispatcherService.scheduleTrains(trainsToSimulate);
            }

            @Override
            protected void succeeded() {
                SchedulerResult result = getValue();

                // 1. Mostrar Relatório (Agora Branco)
                resultTextArea.setText(formatSimulationOutput(result));

                progressIndicator.setVisible(false);
                runButton.setDisable(false);
                mainController.showNotification("Simulation completed!", "success");

                // 2. Iniciar Visualização
                if (railVisualizer != null && !result.scheduledTrips.isEmpty()) {
                    Map<Train, List<String>> visualData = prepareDataForVisualizer(result, trainsToSimulate);
                    LocalTime startTime = trainsToSimulate.stream()
                            .map(t -> t.getDepartureTime().toLocalTime())
                            .min(LocalTime::compareTo).orElse(LocalTime.of(8, 0));
                    railVisualizer.loadSchedule(visualData, startTime.minusMinutes(30));
                    railVisualizer.setSpeedFactor(speedSlider != null ? speedSlider.getValue() : 120.0);
                    railVisualizer.startAnimation();
                }
            }

            @Override
            protected void failed() {
                resultTextArea.setText("ERROR: " + getException().getMessage());
                progressIndicator.setVisible(false);
                runButton.setDisable(false);
                getException().printStackTrace();
            }
        };
        new Thread(simulationTask).start();
    }

    private Map<Train, List<String>> prepareDataForVisualizer(SchedulerResult result, List<Train> originalTrains) {
        Map<Train, List<String>> visualData = new HashMap<>();

        for (TrainTrip trip : result.scheduledTrips) {
            Train t = originalTrains.stream()
                    .filter(tr -> tr.getTrainId().equals(trip.getTripId()))
                    .findFirst()
                    .orElse(null);

            if (t == null) continue;

            List<String> logs = new ArrayList<>();

            // --- INJEÇÃO DE CONFLITO (Para o ecrã vermelho) ---
            Optional<Conflict> conflictOpt = result.resolvedConflicts.stream()
                    .filter(c -> c.tripId2.equals(trip.getTripId()))
                    .findFirst();

            if (conflictOpt.isPresent()) {
                Conflict c = conflictOpt.get();
                String stationName = getFacilityName(c.getSafeWaitFacilityId());
                logs.add("!!!CONFLICT|" + t.getTrainId() + "|" + stationName + "|" + c.delayMinutes);
            }

            for (SimulationSegmentEntry entry : trip.getSegmentEntries()) {
                String line = String.format("%s | %s | %s | %s | %.1f | %s | %s | 0",
                        entry.getSegmentId(),
                        entry.getStartFacilityName(),
                        entry.getEndFacilityName(),
                        entry.getSegment().getNumberTracks() > 1 ? "Double" : "Single",
                        entry.getSegment().getComprimento(),
                        entry.getEntryTime().toLocalTime(),
                        entry.getExitTime().toLocalTime()
                );
                logs.add(line);
            }
            visualData.put(t, logs);
        }
        return visualData;
    }

    // --- FORMATAÇÃO ALINHADA ---
    private String formatSimulationOutput(SchedulerResult result) {
        if (result.scheduledTrips.isEmpty()) {
            return "Simulation completed, but no valid trips were scheduled (check routes).";
        }

        StringBuilder output = new StringBuilder();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        // Larguras definidas para alinhar a tabela
        final int SEG_W = 8;
        final int FAC_W = 20;
        final int TYP_W = 8;
        final int LEN_W = 8;
        final int TIM_W = 6;
        final int SPD_W = 12;

        final String SEPARATOR = " | ";
        final String LINE_BREAK = "\n";

        String tableFormat = "%-" + SEG_W + "s" + SEPARATOR +
                "%-" + FAC_W + "s" + SEPARATOR +
                "%-" + FAC_W + "s" + SEPARATOR +
                "%-" + TYP_W + "s" + SEPARATOR +
                "%-" + LEN_W + "s" + SEPARATOR +
                "%-" + TIM_W + "s" + SEPARATOR +
                "%-" + TIM_W + "s" + SEPARATOR +
                "%-" + SPD_W + "s" + LINE_BREAK;

        int totalWidth = SEG_W + (FAC_W * 2) + TYP_W + LEN_W + (TIM_W * 2) + SPD_W + (SEPARATOR.length() * 7);
        String dashLine = "-".repeat(totalWidth) + "\n";

        output.append("=================================================================================\n");
        output.append("                       🚦 CONFLICT AND DELAY REPORT 🚦\n");
        output.append("=================================================================================\n");

        if (result.resolvedConflicts.isEmpty()) {
            output.append("✅ No single-track conflicts detected in the schedule.\n\n");
        } else {
            output.append("⚠️ ").append(result.resolvedConflicts.size()).append(" Conflicts resolved (delays injected):\n");
            for (Conflict c : result.resolvedConflicts) {
                output.append(String.format("   • [Trip %s] Delay: %2d min in %s due to [Trip %s]\n",
                        c.tripId2, c.delayMinutes, getFacilityName(c.getSafeWaitFacilityId()), c.tripId1));
            }
            output.append("\n");
        }

        output.append("=================================================================================================================\n");
        output.append("                                         DETAILED SEGMENT TIMETABLE\n");
        output.append("=================================================================================================================\n\n");

        for (TrainTrip trip : result.scheduledTrips) {
            long currentDelayMinutes = 0;
            String waitFacilityName = null;

            Optional<Conflict> conflictOpt = result.resolvedConflicts.stream()
                    .filter(c -> c.tripId2.equals(trip.getTripId()))
                    .findFirst();

            if (conflictOpt.isPresent()) {
                currentDelayMinutes = conflictOpt.get().delayMinutes;
                waitFacilityName = getFacilityName(conflictOpt.get().getSafeWaitFacilityId());
            }

            LocalDateTime nextSegmentEntryTime = trip.getDepartureTime();
            if (conflictOpt.isPresent()) {
                nextSegmentEntryTime = nextSegmentEntryTime.minusMinutes(currentDelayMinutes);
            }

            LocalDateTime finalArrivalTime = null;
            String rotaStart = trip.getRoute().isEmpty() ? "N/A" : getFacilityName(trip.getRoute().get(0).getIdEstacaoInicio());
            String rotaEnd = trip.getRoute().isEmpty() ? "N/A" : getFacilityName(trip.getRoute().get(trip.getRoute().size() - 1).getIdEstacaoFim());
            String arrivalPlaceholder = " | EAT: %s | AAT: %s";

            output.append(String.format("🚆 Train %s — Scheduled Departure: %s\n",
                    trip.getTripId(), trip.getDepartureTime().toLocalTime().format(timeFormatter)));

            output.append(String.format("   Max Calculated Speed: %.0f km/h | Route: %s -> %s%s\n",
                    trip.getMaxTrainSpeed(), rotaStart, rotaEnd, arrivalPlaceholder));

            if (trip.getPhysicsCalculationLog() != null && !trip.getPhysicsCalculationLog().isEmpty()) {
                output.append(trip.getPhysicsCalculationLog()).append("\n");
            }

            output.append("   [PAYLOAD] ");
            List<Wagon> trainWagons = trip.getWagons();
            if (trainWagons == null || trainWagons.isEmpty()) {
                output.append("Locomotive Only (No Wagons)\n");
            } else {
                output.append(trainWagons.size()).append(" Wagon(s):\n");
                for (Wagon w : trainWagons) {
                    String cargoStr = w.getBoxes().isEmpty() ? "Empty" :
                            String.format("%d Boxes [%s]", w.getBoxes().size(), w.getBoxes().stream().limit(3).map(Box::getSku).collect(Collectors.joining(",")) + (w.getBoxes().size()>3?"...":""));
                    output.append(String.format("      • %-5s -> %s\n", w.getIdWagon(), cargoStr));
                }
            }

            // --- CABEÇALHO ---
            output.append("\n");
            output.append(String.format(tableFormat, "ID", "START", "END", "TYPE", "LEN", "ENTRY", "EXIT", "SPD(C/A)"));
            output.append(dashLine);

            long remainingDelayMinutes = currentDelayMinutes;

            for (SimulationSegmentEntry entry : trip.getSegmentEntries()) {
                LocalDateTime finalEntryTime = nextSegmentEntryTime;
                Duration segmentDuration = Duration.between(entry.getEntryTime(), entry.getExitTime());
                LocalDateTime finalExitTime = finalEntryTime.plus(segmentDuration);

                String rawSegmentId = entry.getSegmentId();
                String segmentId = rawSegmentId.startsWith("INV_") ? rawSegmentId.substring(4) : rawSegmentId;

                // Usar constante FAC_W para o truncate alinhar com o cabeçalho
                String startName = truncate(entry.getStartFacilityName(), FAC_W);
                String endName = truncate(entry.getEndFacilityName(), FAC_W);

                String trackType = entry.getSegment().getNumberTracks() > 1 ? "Double" : "Single";
                String lengthStr = String.format("%.1fkm", entry.getSegment().getComprimento());

                if (remainingDelayMinutes > 0 && entry.getEndFacilityName().equals(waitFacilityName)) {
                    LocalDateTime departureFromWaitPoint = finalExitTime.plusMinutes(remainingDelayMinutes);

                    output.append(String.format(tableFormat,
                            segmentId, startName, endName, trackType, lengthStr,
                            finalEntryTime.toLocalTime().format(timeFormatter),
                            finalExitTime.toLocalTime().format(timeFormatter),
                            String.format("%.0f/%.0f", entry.getCalculatedSpeedKmh(), entry.getSegment().getVelocidadeMaxima())
                    ));

                    String waitShort = truncate(entry.getEndFacilityName(), FAC_W);

                    output.append(String.format(tableFormat,
                            "DELAY", waitShort, waitShort, "WAIT", "---",
                            finalExitTime.toLocalTime().format(timeFormatter),
                            departureFromWaitPoint.toLocalTime().format(timeFormatter),
                            "0/0"
                    ));

                    nextSegmentEntryTime = departureFromWaitPoint;
                    remainingDelayMinutes = 0;

                    if (entry == trip.getSegmentEntries().get(trip.getSegmentEntries().size() - 1)) {
                        finalArrivalTime = departureFromWaitPoint;
                    }
                    continue;
                }

                output.append(String.format(tableFormat,
                        segmentId, startName, endName, trackType, lengthStr,
                        finalEntryTime.toLocalTime().format(timeFormatter),
                        finalExitTime.toLocalTime().format(timeFormatter),
                        String.format("%.0f/%.0f", entry.getCalculatedSpeedKmh(), entry.getSegment().getVelocidadeMaxima())
                ));

                nextSegmentEntryTime = finalExitTime;

                if (entry == trip.getSegmentEntries().get(trip.getSegmentEntries().size() - 1)) {
                    finalArrivalTime = finalExitTime;
                }
            }

            String aat = "N/A", eat = "N/A";
            if (finalArrivalTime != null) {
                aat = finalArrivalTime.toLocalTime().format(timeFormatter);
                long totalDelay = result.resolvedConflicts.stream()
                        .filter(c -> c.tripId2.equals(trip.getTripId()))
                        .mapToLong(c -> c.delayMinutes).sum();
                eat = finalArrivalTime.minusMinutes(totalDelay).toLocalTime().format(timeFormatter);
            }

            int headerIdx = output.lastIndexOf("Max Calculated Speed:");
            if (headerIdx != -1) {
                int placeIdx = output.indexOf(arrivalPlaceholder, headerIdx);
                if (placeIdx != -1) output.replace(placeIdx, placeIdx + arrivalPlaceholder.length(), String.format(" | EAT: %s | AAT: %s", eat, aat));
            }
            output.append("\n");
        }
        return output.toString();
    }

    private String truncate(String str, int width) {
        if (str == null) return "";
        if (str.length() <= width) return str;
        return str.substring(0, width - 2) + "..";
    }

    private String getFacilityName(int id) {
        return (facilityRepository != null) ? facilityRepository.findNameById(id).orElse("F" + id) : "ID " + id;
    }

    public static class TrainWrapper {
        private final Train train;
        private final String trainId;
        private final String departureTime;
        private final String routeDescription;

        public TrainWrapper(Train t, FacilityRepository facilityRepository) {
            this.train = t;
            this.trainId = t.getTrainId();
            this.departureTime = (t.getDepartureTime() != null) ? t.getDepartureTime().toLocalTime().format(TIME_FORMATTER) : "N/A";
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