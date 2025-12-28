package pt.ipp.isep.dei.UI.gui.views;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import pt.ipp.isep.dei.UI.gui.MainController;
import pt.ipp.isep.dei.DatabaseConnection.DatabaseConnection;
import pt.ipp.isep.dei.domain.*;
import pt.ipp.isep.dei.repository.FacilityRepository;
import pt.ipp.isep.dei.repository.LocomotiveRepository;
import pt.ipp.isep.dei.repository.WagonRepository; // [NOVO] Import necessário

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TrainSimulationController {

    // Constantes de estilo (não usadas no Text Area mas mantidas por compatibilidade)
    private static final String ANSI_RESET = "";

    @FXML private TableView<TrainWrapper> trainTable;
    @FXML private TableColumn<TrainWrapper, String> idColumn;
    @FXML private TableColumn<TrainWrapper, String> departureColumn;
    @FXML private TableColumn<TrainWrapper, String> routeColumn;
    @FXML public Button runButton;
    @FXML private TextArea resultTextArea;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Button selectAllButton;

    // Dependências
    private FacilityRepository facilityRepository;
    private DispatcherService dispatcherService;
    private LocomotiveRepository locomotiveRepository;
    private MainController mainController;

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

                // [CRÍTICO] Instanciar Repositório de Vagões aqui para carregar a carga real
                WagonRepository wagonRepo = new WagonRepository();

                String sql = "SELECT train_id, operator_id, train_date, train_time, start_facility_id, end_facility_id, locomotive_id, route_id " +
                        "FROM TRAIN ORDER BY train_id";

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
                                LocalTime time = LocalTime.parse(timePart);
                                departureTime = date.toLocalDate().atTime(time);
                            }

                            Train train = new Train(trainId, operatorId, departureTime, startFacilityId, endFacilityId, locoId, routeId);

                            // --- [CORREÇÃO] CARREGAR VAGÕES COM DADOS REAIS ---
                            List<Wagon> wagons = wagonRepo.findWagonsByTrainId(trainId);
                            train.setWagons(wagons);
                            // --------------------------------------------------

                            trains.add(train);

                        } catch (Exception e) {
                            System.err.println("❌ Error parsing train: " + e.getMessage());
                        }
                    }
                } catch (SQLException e) {
                    System.err.println("❌ Fatal Error reading TRAIN table: " + e.getMessage());
                    throw e;
                }

                return trains.stream()
                        .map(t -> new TrainWrapper(t, facilityRepository))
                        .collect(Collectors.toList());
            }

            @Override
            protected void succeeded() {
                if (trainTable != null && progressIndicator != null && runButton != null && mainController != null) {
                    observableTrains = FXCollections.observableArrayList(getValue());
                    trainTable.setItems(observableTrains);
                    progressIndicator.setVisible(false);
                    trainTable.setDisable(false);
                    runButton.setDisable(false);
                    mainController.showNotification("Trains loaded successfully (" + getValue().size() + ").", "success");
                }
            }

            @Override
            protected void failed() {
                if (progressIndicator != null) progressIndicator.setVisible(false);
                if (trainTable != null) trainTable.setDisable(false);
                if (runButton != null) runButton.setDisable(false);

                if (mainController != null) mainController.showNotification("Error loading trains.", "error");
                getException().printStackTrace();
            }
        };

        new Thread(loadTask).start();
    }

    @FXML
    public void runSimulation() {
        List<TrainWrapper> selectedWrappers = trainTable.getSelectionModel().getSelectedItems();
        if (selectedWrappers.isEmpty()) {
            mainController.showNotification("No trains selected for simulation.", "error");
            return;
        }

        resultTextArea.setText("Simulating conflicts and schedules... (This may take a few seconds)");
        progressIndicator.setVisible(true);
        runButton.setDisable(true);

        List<Train> trainsToSimulate = selectedWrappers.stream()
                .map(TrainWrapper::getTrain)
                .collect(Collectors.toList());

        Task<SchedulerResult> simulationTask = new Task<>() {
            @Override
            protected SchedulerResult call() throws Exception {
                return dispatcherService.scheduleTrains(trainsToSimulate);
            }

            @Override
            protected void succeeded() {
                SchedulerResult result = getValue();
                String output = formatSimulationOutput(result);
                resultTextArea.setText(output);
                progressIndicator.setVisible(false);
                runButton.setDisable(false);
                mainController.showNotification("Simulation completed! " + result.scheduledTrips.size() + " trips scheduled.", "success");
            }

            @Override
            protected void failed() {
                resultTextArea.setText("FATAL SIMULATION ERROR:\n" + getException().getMessage());
                progressIndicator.setVisible(false);
                runButton.setDisable(false);
                mainController.showNotification("Simulation failed.", "error");
                getException().printStackTrace();
            }
        };

        new Thread(simulationTask).start();
    }

    /**
     * Converte o resultado da simulação para um formato legível (incluindo Type e Length).
     */
    private String formatSimulationOutput(SchedulerResult result) {
        if (result.scheduledTrips.isEmpty()) {
            return "Simulation completed, but no valid trips were scheduled (check routes).";
        }

        StringBuilder output = new StringBuilder();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        // Larguras ajustadas para o TextArea
        final int SEG_W = 6;
        final int FAC_W = 16;
        final int TYP_W = 6;
        final int LEN_W = 7;
        final int TIM_W = 5;
        final int SPD_W = 9;

        final String SEPARATOR = " | ";
        final String LINE_BREAK = "\n";

        // Formato da Tabela
        String tableFormat =
                "%-" + SEG_W + "s" + SEPARATOR +
                        "%-" + FAC_W + "s" + SEPARATOR +
                        "%-" + FAC_W + "s" + SEPARATOR +
                        "%-" + TYP_W + "s" + SEPARATOR +
                        "%-" + LEN_W + "s" + SEPARATOR +
                        "%-" + TIM_W + "s" + SEPARATOR +
                        "%-" + TIM_W + "s" + SEPARATOR +
                        "%-" + SPD_W + "s" + LINE_BREAK;

        // 1. Relatório de Conflitos
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

        // 2. Tabela Detalhada
        output.append("=================================================================================================================\n");
        output.append("                                         DETAILED SEGMENT TIMETABLE\n");
        output.append("=================================================================================================================\n\n");

        for (TrainTrip trip : result.scheduledTrips) {

            // Estado Inicial de Atrasos
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

            // Info de Rota
            String rotaStart = trip.getRoute().isEmpty() ? "N/A" : getFacilityName(trip.getRoute().get(0).getIdEstacaoInicio());
            String rotaEnd = trip.getRoute().isEmpty() ? "N/A" : getFacilityName(trip.getRoute().get(trip.getRoute().size() - 1).getIdEstacaoFim());
            String arrivalPlaceholder = " | EAT: %s | AAT: %s";

            // --- CABEÇALHO ---
            output.append(String.format("🚆 Train %s — Scheduled Departure: %s\n",
                    trip.getTripId(), trip.getDepartureTime().toLocalTime().format(timeFormatter)));

            output.append(String.format("   Max Calculated Speed: %.0f km/h | Route: %s -> %s%s\n",
                    trip.getMaxTrainSpeed(), rotaStart, rotaEnd, arrivalPlaceholder));

            // --- FÍSICA ---
            if (trip.getPhysicsCalculationLog() != null && !trip.getPhysicsCalculationLog().isEmpty()) {
                output.append(trip.getPhysicsCalculationLog()).append("\n");
            }

            // --- PAYLOAD (CARGA) ---
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

            // --- CABEÇALHO TABELA ---
            output.append(String.format(tableFormat,
                    "ID", "START", "END", "TYPE", "LEN", "ENTRY", "EXIT", "SPD(C/A)"));
            output.append("-".repeat(110)).append("\n");

            // --- LOOP SEGMENTOS ---
            long remainingDelayMinutes = currentDelayMinutes;

            for (SimulationSegmentEntry entry : trip.getSegmentEntries()) {

                LocalDateTime finalEntryTime = nextSegmentEntryTime;
                Duration segmentDuration = Duration.between(entry.getEntryTime(), entry.getExitTime());
                LocalDateTime finalExitTime = finalEntryTime.plus(segmentDuration);

                // Dados Formatados
                String rawSegmentId = entry.getSegmentId();
                String segmentId = rawSegmentId.startsWith("INV_") ? rawSegmentId.substring(4) : rawSegmentId;
                String startName = truncate(entry.getStartFacilityName(), FAC_W);
                String endName = truncate(entry.getEndFacilityName(), FAC_W);
                String trackType = entry.getSegment().getNumberTracks() > 1 ? "Double" : "Single";
                String lengthStr = String.format("%.1fkm", entry.getSegment().getComprimento());

                // Lógica de Delay
                if (remainingDelayMinutes > 0 && entry.getEndFacilityName().equals(waitFacilityName)) {
                    LocalDateTime departureFromWaitPoint = finalExitTime.plusMinutes(remainingDelayMinutes);

                    // Segmento de Chegada
                    output.append(String.format(tableFormat,
                            segmentId, startName, endName, trackType, lengthStr,
                            finalEntryTime.toLocalTime().format(timeFormatter),
                            finalExitTime.toLocalTime().format(timeFormatter),
                            String.format("%.0f/%.0f", entry.getCalculatedSpeedKmh(), entry.getSegment().getVelocidadeMaxima())
                    ));

                    // Linha de Espera (DELAY)
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

                // Segmento Normal
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

            // --- CÁLCULO FINAIS ---
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