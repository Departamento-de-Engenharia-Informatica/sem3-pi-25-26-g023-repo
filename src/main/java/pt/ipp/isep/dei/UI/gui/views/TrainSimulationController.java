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
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TrainSimulationController {

    // --- ANSI COLOR CONSTANTS (REMOVIDAS DO OUTPUT FINAL) ---
    private static final String ANSI_RESET = "";
    private static final String ANSI_RED = "";
    private static final String ANSI_YELLOW = "";
    private static final String ANSI_CYAN = "";
    private static final String ANSI_BOLD = "";
    // ----------------------------

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
     * CORREÇÃO: Remove o Total Weight e mascara o prefixo INV_ dos IDs.
     */
    private String formatSimulationOutput(SchedulerResult result) {
        if (result.scheduledTrips.isEmpty()) {
            return "Simulation completed, but no valid trips were scheduled (check routes).";
        }

        StringBuilder output = new StringBuilder();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        // Larguras de coluna ajustadas para Monoespaçado e Nomes longos
        final int SEGMENT_W = 8;
        final int FACILITY_W = 20;
        final int TIME_W = 8;
        final int SPD_W = 6;
        final String SEPARATOR = " | ";
        final String LINE_BREAK = "\n";

        // Formato para as linhas da tabela (garante alinhamento)
        String tableFormat = "%-" + SEGMENT_W + "s" + SEPARATOR +
                "%-" + FACILITY_W + "s" + SEPARATOR +
                "%-" + FACILITY_W + "s" + SEPARATOR +
                "%-" + TIME_W + "s" + SEPARATOR +
                "%-" + TIME_W + "s" + SEPARATOR +
                "%-" + SPD_W + "s" + LINE_BREAK;

        // 1. Relatório de Conflitos (LIMPO DE ANSI)
        output.append("=================================================================\n");
        output.append("             🚦 CONFLICT AND DELAY REPORT 🚦\n");
        output.append("=================================================================\n");

        if (result.resolvedConflicts.isEmpty()) {
            output.append("✅ No single-track conflicts detected in the schedule.\n\n");
        } else {
            output.append("⚠️ ").append(result.resolvedConflicts.size()).append(" Conflicts resolved (delays injected):\n");
            for (Conflict c : result.resolvedConflicts) {
                // REMOVIDO ANSI, APENAS STRING PLANA
                output.append(String.format("   • [Trip %s] Delay: %2d min in %s due to [Trip %s]\n",
                        c.tripId2, c.delayMinutes, getFacilityName(c.getSafeWaitFacilityId()), c.tripId1));
            }
            output.append("\n");
        }

        // 2. Linha Temporal Detalhada (LIMPO DE ANSI)
        output.append("=========================================================================================\n");
        output.append("                           DETAILED SEGMENT TIMETABLE\n");
        output.append("=========================================================================================\n\n");

        for (TrainTrip trip : result.scheduledTrips) {

            // Variáveis de estado para rastrear o atraso
            long currentDelayMinutes = 0;
            String waitFacilityName = null;

            Optional<Conflict> conflictOpt = result.resolvedConflicts.stream()
                    .filter(c -> c.tripId2.equals(trip.getTripId()))
                    .findFirst();

            if (conflictOpt.isPresent()) {
                currentDelayMinutes = conflictOpt.get().delayMinutes;
                waitFacilityName = getFacilityName(conflictOpt.get().getSafeWaitFacilityId());
            }

            // Ponto de partida agendado original (usado como base para calcular a hora de chegada)
            LocalDateTime originalDeparture = trip.getDepartureTime();

            // Variável que rastreia a próxima hora de entrada. Começa com a partida original menos o delay total.
            LocalDateTime nextSegmentEntryTime = originalDeparture;

            if (conflictOpt.isPresent()) {
                nextSegmentEntryTime = nextSegmentEntryTime.minusMinutes(currentDelayMinutes);
            }

            // Variável para armazenar a hora de chegada final (exit time do último segmento)
            LocalDateTime finalArrivalTime = null;
            LocalDateTime expectedArrivalTime = null;


            // --- Output de Informação do Comboio (LIMPO DE ANSI) ---

            String rotaStart = trip.getRoute().isEmpty() ? "N/A" : getFacilityName(trip.getRoute().get(0).getIdEstacaoInicio());
            String rotaEnd = trip.getRoute().isEmpty() ? "N/A" : getFacilityName(trip.getRoute().get(trip.getRoute().size() - 1).getIdEstacaoFim());

            // Placeholder para Arrival Times
            String arrivalPlaceholder = " | EAT: %s | AAT: %s";

            // Primeira Linha: Train ID, Departure (REMOVIDO TOTAL WEIGHT)
            String line1 = String.format("🚆 Train %s — Scheduled Departure: %s",
                    trip.getTripId(), nextSegmentEntryTime.toLocalTime().format(timeFormatter));
            output.append(line1).append(LINE_BREAK);

            // Segunda Linha: Speed, Route, ARRIVAL TIMES (EAT e AAT)
            output.append(String.format("   Max Calculated Speed: %.0f km/h | Route: %s -> %s%s",
                    trip.getMaxTrainSpeed(), rotaStart, rotaEnd, arrivalPlaceholder));
            output.append(LINE_BREAK);


            // Cabeçalho da Tabela
            output.append(String.format(tableFormat,
                    "SEGMENT", "START", "END", "ENTRY", "EXIT", "SPD (C/A)"));
            output.append("-".repeat(85)).append(LINE_BREAK);


            // ----------------------------------------------------------------
            // Itera pelas SimulationSegmentEntry
            // ----------------------------------------------------------------

            long remainingDelayMinutes = currentDelayMinutes;

            for (SimulationSegmentEntry entry : trip.getSegmentEntries()) {

                LocalDateTime finalEntryTime = nextSegmentEntryTime;

                // Calcula a duração do segmento (usando a diferença dos horários não ajustados)
                Duration segmentDuration = Duration.between(entry.getEntryTime(), entry.getExitTime());

                // Calcula o tempo de saída (tempo de viagem do segmento)
                LocalDateTime finalExitTime = finalEntryTime.plus(segmentDuration);

                // --- VARIÁVEIS PARA OUTPUT ---
                // MASCARAMENTO DO PREFIXO INV_
                String rawSegmentId = entry.getSegmentId();
                String segmentId = rawSegmentId.startsWith("INV_") ? rawSegmentId.substring(4) : rawSegmentId;

                String startName = entry.getStartFacilityName().substring(0, Math.min(entry.getStartFacilityName().length(), FACILITY_W));
                String endName = entry.getEndFacilityName().substring(0, Math.min(entry.getEndFacilityName().length(), FACILITY_W));

                // ----------------------------------------------------------------
                // LÓGICA DE APLICAÇÃO E INSERÇÃO DA LINHA DELAY
                // ----------------------------------------------------------------

                if (remainingDelayMinutes > 0 && entry.getEndFacilityName().equals(waitFacilityName)) {

                    // O tempo de partida após a espera
                    LocalDateTime departureFromWaitPoint = finalExitTime.plusMinutes(remainingDelayMinutes);

                    // --- IMPRIME A LINHA DO SEGMENTO (Chegada ao ponto de espera) ---
                    output.append(String.format(tableFormat,
                            segmentId,
                            startName,
                            endName,
                            finalEntryTime.toLocalTime().format(timeFormatter),
                            finalExitTime.toLocalTime().format(timeFormatter), // Chega ao ponto de espera
                            String.format("%.0f/%.0f", entry.getCalculatedSpeedKmh(), entry.getSegment().getVelocidadeMaxima())
                    ));

                    // --- INSERÇÃO DA LINHA DELAY ---
                    String waitFacilityShort = entry.getEndFacilityName().substring(0, Math.min(entry.getEndFacilityName().length(), FACILITY_W));

                    output.append(String.format(tableFormat,
                            "DELAY",
                            waitFacilityShort,
                            waitFacilityShort,
                            finalExitTime.toLocalTime().format(timeFormatter), // ENTRY = Chegada (Tempo real)
                            departureFromWaitPoint.toLocalTime().format(timeFormatter), // EXIT = Partida ajustada (Com delay)
                            "0/0"
                    ));

                    // Atualiza o tempo de entrada para o PRÓXIMO segmento
                    nextSegmentEntryTime = departureFromWaitPoint;
                    remainingDelayMinutes = 0; // Delay aplicado

                    // Se este for o último segmento, a chegada final é a partida ajustada
                    if (entry == trip.getSegmentEntries().get(trip.getSegmentEntries().size() - 1)) {
                        finalArrivalTime = departureFromWaitPoint;
                    }

                    continue; // Passa ao próximo segmento
                }

                // 2. Imprime o segmento normal (ou segmentos após a espera)
                output.append(String.format(tableFormat,
                        segmentId,
                        startName,
                        endName,
                        finalEntryTime.toLocalTime().format(timeFormatter),
                        finalExitTime.toLocalTime().format(timeFormatter),
                        String.format("%.0f/%.0f", entry.getCalculatedSpeedKmh(), entry.getSegment().getVelocidadeMaxima())
                ));

                nextSegmentEntryTime = finalExitTime; // Atualiza o tempo para o próximo segmento.

                // Se este for o último segmento, esta é a chegada final
                if (entry == trip.getSegmentEntries().get(trip.getSegmentEntries().size() - 1)) {
                    finalArrivalTime = finalExitTime;
                }
            }

            // --- CÁLCULO FINAL E SUBSTITUIÇÃO DO PLACEHOLDER ---

            // AAT = finalArrivalTime (tempo real com atrasos)
            // EAT = AAT - Total Delay (tempo sem atrasos)

            String aatString = "N/A";
            String eatString = "N/A";

            if (finalArrivalTime != null) {
                // O AAT é o tempo de chegada final (finalExitTime do último segmento)
                aatString = finalArrivalTime.toLocalTime().format(timeFormatter);

                // Calcula EAT: Final Arrival Time - Atraso Total Imposto
                // NOTA: O Atraso Total Imposto é o 'currentDelayMinutes' que foi capturado no início.
                // Se o comboio não foi atrasado, currentDelayMinutes é 0.
                long totalImposedDelay = result.resolvedConflicts.stream()
                        .filter(c -> c.tripId2.equals(trip.getTripId()))
                        .mapToLong(c -> c.delayMinutes)
                        .sum();

                LocalDateTime eatTime = finalArrivalTime.minusMinutes(totalImposedDelay);
                eatString = eatTime.toLocalTime().format(timeFormatter);
            }


            // Procura o placeholder " | EAT: %s | AAT: %s" e substitui-o
            int startOfHeaderLine2 = output.lastIndexOf("Max Calculated Speed:");

            if (startOfHeaderLine2 != -1) {
                // Encontra a posição exata do placeholder na string grande
                int placeholderStart = output.indexOf(arrivalPlaceholder, startOfHeaderLine2);

                if (placeholderStart != -1) {
                    // Constrói a string de substituição
                    String replacement = String.format(" | EAT: %s | AAT: %s", eatString, aatString);
                    // O tamanho da string do placeholder é constante, usamos seu comprimento
                    output.replace(placeholderStart, placeholderStart + arrivalPlaceholder.length(), replacement);
                }
            }

            output.append(LINE_BREAK);
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