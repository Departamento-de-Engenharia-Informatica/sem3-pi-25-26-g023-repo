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

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
    @FXML private Button selectAllButton; // Referência ao novo botão

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

            // Adiciona classes CSS
            runButton.getStyleClass().add("run-button");
        }
        if (progressIndicator != null) {
            progressIndicator.setVisible(false);
        }
        if (resultTextArea != null) {
            resultTextArea.getStyleClass().add("result-text-area");
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
                                LocalTime time = LocalTime.parse(timeStr.substring(0, 8));
                                departureTime = date.toLocalDate().atTime(time);
                            }

                            trains.add(new Train(trainId, operatorId, departureTime, startFacilityId, endFacilityId, locoId, routeId));
                        } catch (Exception e) {
                            System.err.println("❌ Parsing/Typing Error reading Train from DB: " + e.getMessage());
                        }
                    }
                } catch (SQLException e) {
                    System.err.println("❌ Fatal Error reading TRAIN table. Possible connection failure: " + e.getMessage());
                    throw e;
                }

                // --- INJEÇÃO DE DADOS MOCK (Mantida) ---
                List<String> mockIds = new ArrayList<>();
                for (int k = 60; k < 80; k++) {
                    mockIds.add("54" + k);
                }
                trains.removeIf(t -> mockIds.contains(t.getTrainId()));

                LocalDateTime baseDate = LocalDateTime.of(2025, 10, 6, 0, 0, 0);

                int facilityLeixoes = 50;
                int facilityValenca = 11;
                String locoFast = "5621";
                String locoSlow = "5034";
                String routeLV = "R001";
                String routeVL = "R002";

                trains.add(new Train("5460", "MEDWAY", baseDate.with(LocalTime.of(8, 0, 0)), facilityLeixoes, facilityValenca, locoSlow, routeLV));
                trains.add(new Train("5461", "CAPTRAIN", baseDate.with(LocalTime.of(8, 20, 0)), facilityValenca, facilityLeixoes, locoFast, routeVL));
                trains.add(new Train("5462", "MEDWAY", baseDate.with(LocalTime.of(8, 15, 0)), facilityLeixoes, facilityValenca, locoSlow, routeLV));
                trains.add(new Train("5463", "CAPTRAIN", baseDate.with(LocalTime.of(8, 40, 0)), facilityValenca, facilityLeixoes, locoFast, routeVL));
                trains.add(new Train("5464", "MEDWAY", baseDate.with(LocalTime.of(8, 45, 0)), facilityLeixoes, facilityValenca, locoFast, routeLV));
                trains.add(new Train("5465", "CAPTRAIN", baseDate.with(LocalTime.of(8, 50, 0)), facilityValenca, facilityLeixoes, locoSlow, routeVL));
                trains.add(new Train("5466", "MEDWAY", baseDate.with(LocalTime.of(9, 0, 0)), facilityLeixoes, facilityValenca, locoSlow, routeLV));
                trains.add(new Train("5467", "CAPTRAIN", baseDate.with(LocalTime.of(9, 10, 0)), facilityValenca, facilityLeixoes, locoFast, routeVL));
                trains.add(new Train("5468", "MEDWAY", baseDate.with(LocalTime.of(9, 30, 0)), facilityLeixoes, facilityValenca, locoFast, routeLV));
                trains.add(new Train("5469", "CAPTRAIN", baseDate.with(LocalTime.of(9, 35, 0)), facilityValenca, facilityLeixoes, locoSlow, routeVL));
                trains.add(new Train("5470", "MEDWAY", baseDate.with(LocalTime.of(10, 0, 0)), facilityLeixoes, facilityValenca, locoSlow, routeLV));
                trains.add(new Train("5471", "CAPTRAIN", baseDate.with(LocalTime.of(10, 10, 0)), facilityValenca, facilityLeixoes, locoSlow, routeVL));
                trains.add(new Train("5472", "MEDWAY", baseDate.with(LocalTime.of(10, 5, 0)), facilityLeixoes, facilityValenca, locoFast, routeLV));
                trains.add(new Train("5473", "CAPTRAIN", baseDate.with(LocalTime.of(10, 30, 0)), facilityValenca, facilityLeixoes, locoFast, routeVL));
                trains.add(new Train("5474", "MEDWAY", baseDate.with(LocalTime.of(10, 45, 0)), facilityLeixoes, facilityValenca, locoSlow, routeLV));
                trains.add(new Train("5475", "CAPTRAIN", baseDate.with(LocalTime.of(10, 50, 0)), facilityValenca, facilityLeixoes, locoSlow, routeVL));
                trains.add(new Train("5476", "MEDWAY", baseDate.with(LocalTime.of(11, 0, 0)), facilityLeixoes, facilityValenca, locoFast, routeLV));
                trains.add(new Train("5477", "CAPTRAIN", baseDate.with(LocalTime.of(11, 15, 0)), facilityValenca, facilityLeixoes, locoSlow, routeVL));
                trains.add(new Train("5478", "MEDWAY", baseDate.with(LocalTime.of(11, 20, 0)), facilityLeixoes, facilityValenca, locoSlow, routeLV));
                trains.add(new Train("5479", "CAPTRAIN", baseDate.with(LocalTime.of(11, 30, 0)), facilityValenca, facilityLeixoes, locoFast, routeVL));

                if (facilityRepository == null) {
                    throw new IllegalStateException("facilityRepository is null. Injection failed in MainController.");
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
                Throwable exception = getException();

                if (progressIndicator != null) progressIndicator.setVisible(false);
                if (trainTable != null) trainTable.setDisable(false);
                if (runButton != null) runButton.setDisable(false);

                String errMsg = "CRITICAL ERROR: Loading Failed: " +
                        (exception.getMessage() != null ? exception.getMessage() : exception.getClass().getSimpleName());
                if (mainController != null) {
                    mainController.showNotification(errMsg, "error");
                }

                System.err.println("❌ Fatal Error in Train Loading Task:");
                exception.printStackTrace();
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

        // 1. Preparação da UI (Assíncrona)
        resultTextArea.setText("Simulating conflicts and schedules... (This may take a few seconds)");
        progressIndicator.setVisible(true);
        runButton.setDisable(true);

        List<Train> trainsToSimulate = selectedWrappers.stream()
                .map(TrainWrapper::getTrain)
                .collect(Collectors.toList());

        // 2. Task para a simulação (Assíncrona para não bloquear a UI)
        Task<SchedulerResult> simulationTask = new Task<>() {
            @Override
            protected SchedulerResult call() throws Exception {
                return dispatcherService.scheduleTrains(trainsToSimulate);
            }

            @Override
            protected void succeeded() {
                SchedulerResult result = getValue();

                // 3. Formatar e mostrar o resultado (OUTPUT DA CONSOLA NA UI FX)
                String output = formatSimulationOutput(result);
                resultTextArea.setText(output);

                progressIndicator.setVisible(false);
                runButton.setDisable(false);

                mainController.showNotification("Simulation completed! " + result.scheduledTrips.size() + " trips scheduled.", "success");
            }

            @Override
            protected void failed() {
                Throwable exception = getException();
                resultTextArea.setText("FATAL SIMULATION ERROR:\n" + exception.getMessage() + "\n\nCheck console for stack trace.");

                progressIndicator.setVisible(false);
                runButton.setDisable(false);

                mainController.showNotification("Simulation failed. Check output for error.", "error");
                System.err.println("❌ Fatal Error in Simulation Task:");
                exception.printStackTrace();
            }
        };

        new Thread(simulationTask).start();
    }

    /**
     * Converte o resultado da simulação para um formato legível (semelhante ao output da consola).
     */
    private String formatSimulationOutput(SchedulerResult result) {
        if (result.scheduledTrips.isEmpty()) {
            return "Simulation completed, but no valid trips were scheduled (check routes).";
        }

        StringBuilder output = new StringBuilder();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        // 1. Relatório de Conflitos
        output.append("=================================================================\n");
        output.append("             🚦 CONFLICT AND DELAY REPORT 🚦\n");
        output.append("=================================================================\n");

        if (result.resolvedConflicts.isEmpty()) {
            output.append("✅ No single-track conflicts detected in the schedule.\n\n");
        } else {
            output.append("⚠️ ").append(result.resolvedConflicts.size()).append(" Conflicts resolved (delays injected):\n");
            for (Conflict c : result.resolvedConflicts) {
                // Formato: [Trip B] Delay: 5 min in (Facility X) due to [Trip A]
                output.append(String.format("   • [Trip %s] Delay: %2d min in %s due to [Trip %s]\n",
                        c.tripId2, c.delayMinutes, getFacilityName(c.getSafeWaitFacilityId()), c.tripId1));
            }
            output.append("\n");
        }

        // 2. Linha Temporal Detalhada
        output.append("=========================================================================================\n");
        output.append("                           DETAILED SEGMENT TIMETABLE\n");
        output.append("=========================================================================================\n\n");

        for (TrainTrip trip : result.scheduledTrips) {
            String rotaStart = trip.getRoute().isEmpty() ? "N/A" : getFacilityName(trip.getRoute().get(0).getIdEstacaoInicio());
            String rotaEnd = trip.getRoute().isEmpty() ? "N/A" : getFacilityName(trip.getRoute().get(trip.getRoute().size() - 1).getIdEstacaoFim());

            output.append(String.format("🚆 Train %s — Scheduled Departure: %s | Total Weight: %.0f t\n",
                    trip.getTripId(), trip.getDepartureTime().toLocalTime().format(timeFormatter), trip.getTotalWeightKg() / 1000.0));
            output.append(String.format("   Max Calculated Speed: %.0f km/h | Route: %s -> %s\n",
                    trip.getMaxTrainSpeed(), rotaStart, rotaEnd));

            // Cabeçalho da Tabela
            output.append(String.format("\n%-8s | %-16s | %-16s | %-8s | %-8s | %-6s\n",
                    "SEGMENT", "START", "END", "ENTRY", "EXIT", "SPD (C/A)"));
            output.append("-".repeat(85)).append("\n");

            for (SimulationSegmentEntry entry : trip.getSegmentEntries()) {
                String segmentId = entry.getSegmentId().length() > 6 ? entry.getSegmentId().substring(0, 6) : entry.getSegmentId();
                // Limitar nomes para manter o alinhamento monoespaçado
                String startName = entry.getStartFacilityName().substring(0, Math.min(entry.getStartFacilityName().length(), 15));
                String endName = entry.getEndFacilityName().substring(0, Math.min(entry.getEndFacilityName().length(), 15));

                output.append(String.format("%-8s | %-16s | %-16s | %-8s | %-8s | %-6s\n",
                        segmentId,
                        startName,
                        endName,
                        entry.getEntryTime().toLocalTime().format(timeFormatter),
                        entry.getExitTime().toLocalTime().format(timeFormatter),
                        String.format("%.0f/%.0f", entry.getCalculatedSpeedKmh(), entry.getSegment().getVelocidadeMaxima())
                ));
            }
            output.append("\n");
        }

        return output.toString();
    }

    private String getFacilityName(int id) {
        if (facilityRepository != null) {
            return facilityRepository.findNameById(id).orElse("F" + id);
        }
        return "ID " + id;
    }


    public static class TrainWrapper {
        private final Train train;
        private final String trainId;
        private final String departureTime;
        private final String routeDescription;

        public TrainWrapper(Train t, FacilityRepository facilityRepository) {
            this.train = t;
            this.trainId = t.getTrainId();

            LocalDateTime departure = t.getDepartureTime();
            if (departure != null) {
                this.departureTime = departure.toLocalTime().format(TIME_FORMATTER);
            } else {
                this.departureTime = "N/A (DB Null)";
                System.err.println("❌ Null check needed: Train " + t.getTrainId() + " has null DepartureTime.");
            }


            String startName = facilityRepository.findNameById(t.getStartFacilityId()).orElse("F" + t.getStartFacilityId());
            String endName = facilityRepository.findNameById(t.getEndFacilityId()).orElse("F" + t.getEndFacilityId());
            // String de rota em inglês
            this.routeDescription = startName + " -> " + endName + " | Loco: " + t.getLocomotiveId();
        }

        public Train getTrain() { return train; }
        public String getTrainId() { return trainId; }
        public String getDepartureTime() { return departureTime; }
        public String getRouteDescription() { return routeDescription; }
    }
}