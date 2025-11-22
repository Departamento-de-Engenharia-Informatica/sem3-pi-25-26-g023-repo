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
import java.time.Duration; // Import necessário
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional; // Import necessário
import java.util.stream.Collectors;

public class TrainSimulationController {

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

        // FIX 1: Tipo de retorno do Task e do call() deve ser List<TrainWrapper>
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
                                // FIX 2: CORREÇÃO DE LEITURA DA HORA
                                String timePart = timeStr.length() >= 8 ? timeStr.substring(0, 8) : timeStr;
                                LocalTime time = LocalTime.parse(timePart);
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

                // --- DADOS MOCK REMOVIDOS ---

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
     * Corrigido para aplicar o atraso de conflito sequencialmente, refletindo o agendamento final.
     */
    private String formatSimulationOutput(SchedulerResult result) {
        if (result.scheduledTrips.isEmpty()) {
            return "Simulation completed, but no valid trips were scheduled (check routes).";
        }

        StringBuilder output = new StringBuilder();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        // 1. Relatório de Conflitos (Formato da consola)
        output.append("=================================================================\n");
        output.append("             🚦 CONFLICT AND DELAY REPORT 🚦\n");
        output.append("=================================================================\n");

        if (result.resolvedConflicts.isEmpty()) {
            output.append("✅ No single-track conflicts detected in the schedule.\n\n");
        } else {
            output.append("⚠️ ").append(result.resolvedConflicts.size()).append(" Conflicts resolved (delays injected):\n");
            for (Conflict c : result.resolvedConflicts) {
                // Assumindo que tripId2, delayMinutes e tripId1 são campos public
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

            // --- LÓGICA DE RECALCULO DE TEMPO SEQUENCIAL (PARA REPLICAR O OUTPUT DA CONSOLA) ---
            int delayMinutes = 0;
            String waitFacilityName = null;

            Optional<Conflict> conflictOpt = result.resolvedConflicts.stream()
                    .filter(c -> c.tripId2.equals(trip.getTripId()))
                    .findFirst();

            if (conflictOpt.isPresent()) {
                delayMinutes = (int) conflictOpt.get().delayMinutes;
                // Usa getFacilityName para garantir consistência com o nome na tabela
                waitFacilityName = getFacilityName(conflictOpt.get().getSafeWaitFacilityId());
            }

            // O tempo de saída do último segmento/partida agendado.
            LocalDateTime currentSimulatedTime = trip.getDepartureTime();
            // -------------------------------------------------------------------------

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

                // Calcula a duração do segmento (usando a diferença dos horários não ajustados)
                Duration segmentDuration = Duration.between(entry.getEntryTime(), entry.getExitTime());

                // O tempo de entrada do segmento é o tempo simulado atual.
                LocalDateTime finalEntryTime = currentSimulatedTime;

                // Calcula o tempo de chegada (Arrival time)
                LocalDateTime finalArrivalTime = finalEntryTime.plus(segmentDuration);
                LocalDateTime finalExitTime = finalArrivalTime; // Valor padrão

                // ----------------------------------------------------------------
                // LÓGICA DE APLICAÇÃO E INSERÇÃO DA LINHA DELAY
                // ----------------------------------------------------------------
                // 1. Verifica se este segmento termina na estação de espera e se ainda há atraso para aplicar
                if (delayMinutes > 0 && entry.getEndFacilityName().equals(waitFacilityName)) {

                    // O tempo real de partida após a espera
                    LocalDateTime delayDepartureTime = finalArrivalTime.plusMinutes(delayMinutes);

                    // --- INSERÇÃO DA LINHA DELAY (para replicar o output do console) ---
                    // O nome da estação no console é cortado a 15, aqui usaremos o nome completo e o short para o alinhamento
                    String waitFacilityShort = entry.getEndFacilityName().substring(0, Math.min(entry.getEndFacilityName().length(), 15));

                    // Saída do segmento atual (Ex: INV_21) é a chegada (finalArrivalTime)
                    finalExitTime = finalArrivalTime;

                    // A linha DELAY é inserida AGORA (após o segmento INV_21 e antes do INV_18)
                    output.append(String.format("%-8s | %-16s | %-16s | %-8s | %-8s | %-6s\n",
                            "DELAY",
                            waitFacilityShort,
                            waitFacilityShort,
                            finalArrivalTime.toLocalTime().format(timeFormatter), // ENTRY = Chegada (Ex: 10:03)
                            delayDepartureTime.toLocalTime().format(timeFormatter), // EXIT = Partida ajustada (Ex: 10:22)
                            "0/0"
                    ));

                    // Atualiza o currentSimulatedTime para o tempo de partida ajustado
                    currentSimulatedTime = delayDepartureTime;

                    // Marca o atraso como aplicado para não repetir a lógica
                    delayMinutes = 0;

                } else {
                    // Sem atraso extra neste ponto, ou o atraso já foi aplicado.
                    finalExitTime = finalArrivalTime;
                    currentSimulatedTime = finalExitTime;
                }

                String segmentId = entry.getSegmentId().length() > 6 ? entry.getSegmentId().substring(0, 6) : entry.getSegmentId();
                String startName = entry.getStartFacilityName().substring(0, Math.min(entry.getStartFacilityName().length(), 15));
                String endName = entry.getEndFacilityName().substring(0, Math.min(entry.getEndFacilityName().length(), 15));

                output.append(String.format("%-8s | %-16s | %-16s | %-8s | %-8s | %-6s\n",
                        segmentId,
                        startName,
                        endName,
                        finalEntryTime.toLocalTime().format(timeFormatter),
                        finalExitTime.toLocalTime().format(timeFormatter),
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