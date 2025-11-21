package pt.ipp.isep.dei.domain;

import pt.ipp.isep.dei.repository.FacilityRepository;
import pt.ipp.isep.dei.repository.TrainRepository;
import pt.ipp.isep.dei.repository.LocomotiveRepository; // <--- NOVO IMPORT
import pt.ipp.isep.dei.repository.SegmentLineRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DispatcherService {

    private final TrainRepository trainRepo;
    private final RailwayNetworkService networkService;
    private final FacilityRepository facilityRepo;
    private final LocomotiveRepository locomotiveRepo; // <--- NOVO CAMPO INJETADO

    // Mapa central para a Linha Temporal Global: Segment ID -> Lista de Passagens
    private final Map<String, List<SimulationSegmentEntry>> timeline;

    // Constantes de Cálculo Dinâmico (Movidas do SchedulerService para auto-suficiência)
    private static final double POWER_TO_TRACTION_FACTOR = 2.857; // Fator ajustado para forçar V_calc > V_line
    private static final double LOCOMOTIVE_TARA_TONS = 80.0;
    private static final double DEFAULT_TOTAL_TRAIN_WEIGHT_TONS = 100.0; // Peso base do comboio para cálculo Vmax (ajustável)
    private static final double MAX_FREIGHT_SPEED_CAP = 250.0;
    private static final double MIN_FREIGHT_SPEED = 30.0;

    public DispatcherService(TrainRepository trainRepo, RailwayNetworkService networkService, FacilityRepository facilityRepo, LocomotiveRepository locomotiveRepo) { // <--- CONSTRUTOR ATUALIZADO
        this.trainRepo = trainRepo;
        this.networkService = networkService;
        this.facilityRepo = facilityRepo;
        this.locomotiveRepo = locomotiveRepo; // <--- INJEÇÃO
        this.timeline = new HashMap<>();
    }

    // =================================================================================
    // 1. Simulação Principal
    // =================================================================================

    /**
     * Executa a simulação para os comboios fornecidos e calcula os tempos em cada segmento.
     * * @param trainsToSimulate Lista dos comboios a incluir na simulação.
     * @return Mapa (Train ID -> Lista de SimulationSegmentEntry)
     */
    public Map<String, List<SimulationSegmentEntry>> runSimulation(List<Train> trainsToSimulate) {
        this.timeline.clear();

        Map<String, List<SimulationSegmentEntry>> simulationResults = new HashMap<>();

        for (Train train : trainsToSimulate) {

            List<LineSegment> route = findRouteForTrain(train);

            if (route.isEmpty()) {
                System.err.println("Skipping Train " + train.getTrainId() + ": Route not found for Route ID " + train.getRouteId());
                continue;
            }

            // O cálculo da velocidade é feito aqui antes de calcular os tempos
            List<SimulationSegmentEntry> trainTimeline = calculateTrainSegmentTimes(train, route);
            simulationResults.put(train.getTrainId(), trainTimeline);
            updateGlobalTimeline(trainTimeline);
        }

        return simulationResults;
    }

    /**
     * Usa o RailwayNetworkService para encontrar a rota completa (baseado no Route ID do comboio).
     */
    private List<LineSegment> findRouteForTrain(Train train) {
        // Uso de velocidade alta para priorizar o tempo mínimo, desconsiderando a potência do comboio nesta fase.
        RailwayPath path = networkService.findFastestPath(
                train.getStartFacilityId(),
                train.getEndFacilityId(),
                1000.0
        );

        return (path != null) ? path.getSegments() : Collections.emptyList();
    }


    /**
     * Calcula o tempo de entrada e saída para cada segmento na rota.
     */
    private List<SimulationSegmentEntry> calculateTrainSegmentTimes(Train train, List<LineSegment> route) {
        List<SimulationSegmentEntry> entries = new ArrayList<>();
        LocalDateTime currentTime = train.getDepartureTime();

        // --- NOVO: CÁLCULO DINÂMICO DA VELOCIDADE DO COMBOIO (Vmax_train) ---
        double combinedPowerKw = 0.0;
        try {
            int locoId = Integer.parseInt(train.getLocomotiveId());
            Optional<Locomotive> optLoco = locomotiveRepo.findById(locoId);
            if (optLoco.isPresent()) {
                combinedPowerKw = optLoco.get().getPowerKW();
            }
        } catch (NumberFormatException e) {
            System.err.println("Error: Locomotive ID is not a number for train " + train.getTrainId());
        }

        // V_max_comboio [km/h] ≈ Fator * Power [kW] / Weight [tons]
        double vMaxTrain = (DEFAULT_TOTAL_TRAIN_WEIGHT_TONS > 0)
                ? (combinedPowerKw * POWER_TO_TRACTION_FACTOR) / DEFAULT_TOTAL_TRAIN_WEIGHT_TONS
                : MAX_FREIGHT_SPEED_CAP;

        // Aplica o CAP e MÍNIMO
        vMaxTrain = Math.min(vMaxTrain, MAX_FREIGHT_SPEED_CAP);
        vMaxTrain = Math.max(vMaxTrain, MIN_FREIGHT_SPEED);
        // --- FIM CÁLCULO DINÂMICO ---

        for (LineSegment segment : route) {
            double maxSpeedAllowedKmh = segment.getVelocidadeMaxima();

            // 1. Calcular Vcalc (Velocidade Calculada): min(V_linha, V_comboio)
            double calculatedSpeedKmh = Math.min(maxSpeedAllowedKmh, vMaxTrain); // <--- Vmax DINÂMICA USADA

            if (calculatedSpeedKmh <= 0) { continue; }

            // 2. Calcular Tempo de Viagem
            double lengthKm = segment.getComprimento();
            double travelTimeHours = lengthKm / calculatedSpeedKmh;

            long travelTimeSeconds = (long) (travelTimeHours * 3600);
            Duration travelDuration = Duration.ofSeconds(travelTimeSeconds);

            // 3. Calcular Tempos de Entrada e Saída
            LocalDateTime entryTime = currentTime;
            LocalDateTime exitTime = entryTime.plus(travelDuration);

            // 4. Criar a entrada
            String startName = facilityRepo.findNameById(segment.getIdEstacaoInicio()).orElse("F" + segment.getIdEstacaoInicio());
            String endName = facilityRepo.findNameById(segment.getIdEstacaoFim()).orElse("F" + segment.getIdEstacaoFim());

            SimulationSegmentEntry entry = new SimulationSegmentEntry(
                    train.getTrainId(),
                    segment,
                    entryTime,
                    exitTime,
                    maxSpeedAllowedKmh,
                    calculatedSpeedKmh, // <--- VALOR DINÂMICO PASSADO PARA O OUTPUT
                    startName,
                    endName);

            entries.add(entry);

            // 5. Atualizar tempo para o próximo segmento
            currentTime = exitTime;
        }

        return entries;
    }

    /**
     * Popula a linha temporal global (timeline).
     */
    private void updateGlobalTimeline(List<SimulationSegmentEntry> trainTimeline) {
        for (SimulationSegmentEntry entry : trainTimeline) {
            String segmentId = entry.getSegmentId();
            timeline.computeIfAbsent(segmentId, k -> new ArrayList<>()).add(entry);
        }
    }

    // =================================================================================
    // 2. Deteção e Resolução de Conflitos
    // =================================================================================

    /**
     * Retorna o ID FÍSICO do segmento (normalizando X e INV_X para X).
     */
    private String getPhysicalTrackId(String segmentId) {
        if (segmentId.startsWith(SegmentLineRepository.INVERSE_ID_PREFIX)) {
            return segmentId.substring(SegmentLineRepository.INVERSE_ID_PREFIX.length());
        }
        return segmentId;
    }

    /**
     * Analisa a linha temporal global para encontrar conflitos (em via única) e sugere cruzamentos.
     */
    public List<String> checkConflictsAndSuggestCrossings() {
        List<String> conflictReport = new ArrayList<>();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        // 1. Agrupa as passagens pelo ID FÍSICO do segmento (normalizando INV_X para X)
        Map<String, List<SimulationSegmentEntry>> allEntriesByPhysicalTrack = new HashMap<>();

        for (List<SimulationSegmentEntry> entries : this.timeline.values()) {
            for (SimulationSegmentEntry entry : entries) {
                // A chave de agregação é o ID FÍSICO (X para X e INV_X)
                String physicalId = getPhysicalTrackId(entry.getSegmentId());
                allEntriesByPhysicalTrack.computeIfAbsent(physicalId, k -> new ArrayList<>()).add(entry);
            }
        }

        // 2. Itera sobre cada recurso físico (via) para verificar colisões
        for (Map.Entry<String, List<SimulationSegmentEntry>> entry : allEntriesByPhysicalTrack.entrySet()) {
            List<SimulationSegmentEntry> segmentEntries = entry.getValue();

            // Só verifica se houver colisões potenciais e se for via única
            if (segmentEntries.size() < 2 || segmentEntries.get(0).getSegment().getNumberTracks() > 1) {
                continue;
            }

            segmentEntries.sort(Comparator.comparing(SimulationSegmentEntry::getEntryTime));

            for (int i = 0; i < segmentEntries.size(); i++) {
                for (int j = i + 1; j < segmentEntries.size(); j++) {
                    SimulationSegmentEntry entry1 = segmentEntries.get(i);
                    SimulationSegmentEntry entry2 = segmentEntries.get(j);

                    if (checkOverlap(entry1.getEntryTime(), entry1.getExitTime(), entry2.getEntryTime(), entry2.getExitTime())) {

                        // Determinar o tipo de conflito
                        boolean isHeadOn = !entry1.getSegmentId().equals(entry2.getSegmentId());
                        String conflictType = isHeadOn ? "HEAD-ON" : "PURSUIT";

                        // ⚠️ Conflito
                        conflictReport.add(String.format("⚠️ CONFLICT [%s]: Train %s and Train %s overlap on physical track %s",
                                conflictType,
                                entry1.getTrainId(),
                                entry2.getTrainId(),
                                entry.getKey()));

                        // Detalhes de tempo
                        conflictReport.add(String.format("   - T%s (%s): %s – %s",
                                entry1.getTrainId(),
                                entry1.getSegmentId(),
                                entry1.getEntryTime().toLocalTime().format(timeFormatter),
                                entry1.getExitTime().toLocalTime().format(timeFormatter)));

                        conflictReport.add(String.format("   - T%s (%s): %s – %s",
                                entry2.getTrainId(),
                                entry2.getSegmentId(),
                                entry2.getEntryTime().toLocalTime().format(timeFormatter),
                                entry2.getExitTime().toLocalTime().format(timeFormatter)));

                        // 🔁 Sugestão de Cruzamento
                        String waitingTrainId = entry1.getTrainId();
                        String safePoint = findLastSafePoint(entry1.getSegment());

                        conflictReport.add(String.format("   - RECOMMENDED CROSSING: Train %s should wait at %s for Train %s to clear physical track %s.",
                                waitingTrainId, safePoint, entry2.getTrainId(), entry.getKey()));
                    }
                }
            }
        }
        return conflictReport;
    }

    /**
     * Tenta encontrar o ponto seguro (siding ou estação de partida)
     */
    private String findLastSafePoint(LineSegment segment) {
        // Verifica se o segmento tem siding (assumindo que 0.0 significa que não tem)
        if (segment.getSidingLength() > 0.0) {
            return facilityRepo.findNameById(segment.getIdEstacaoInicio()).orElse("F" + segment.getIdEstacaoInicio()) + " (Siding)";
        }
        // Se não houver siding, a estação de partida é o ponto seguro.
        return facilityRepo.findNameById(segment.getIdEstacaoInicio()).orElse("F" + segment.getIdEstacaoInicio()) + " (Station)";
    }

    /**
     * Verifica se dois intervalos de tempo se sobrepõem.
     */
    private boolean checkOverlap(LocalDateTime start1, LocalDateTime end1, LocalDateTime start2, LocalDateTime end2) {
        return start1.isBefore(end2) && end1.isAfter(start2);
    }
}