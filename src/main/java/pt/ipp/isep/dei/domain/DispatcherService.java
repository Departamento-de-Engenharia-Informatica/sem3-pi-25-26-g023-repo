// File: pt.ipp.isep.dei.domain.DispatcherService.java
package pt.ipp.isep.dei.domain;

import pt.ipp.isep.dei.repository.FacilityRepository;
import pt.ipp.isep.dei.repository.TrainRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DispatcherService {

    private final TrainRepository trainRepo;
    private final RailwayNetworkService networkService;
    private final FacilityRepository facilityRepo;

    // Mapa central para a Linha Temporal Global: Segment ID -> Lista de Passagens
    private final Map<String, List<SimulationSegmentEntry>> timeline;

    // Simplificação: Velocidade Máxima do Comboio de Mercadorias (em km/h)
    private static final double MAX_TRAIN_SPEED_KMH = 100.0;

    public DispatcherService(TrainRepository trainRepo, RailwayNetworkService networkService, FacilityRepository facilityRepo) {
        this.trainRepo = trainRepo;
        this.networkService = networkService;
        this.facilityRepo = facilityRepo;
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
        // MOCK: Assumindo que o Route ID dita a rota completa entre start e end facility
        // O código real usaria RouteSegmentRepository para ler o encadeamento R001.1 -> R001.2 ...
        // Como não temos esse repositório, usamos Dijkstra de ponta a ponta (como feito no SchedulerController)
        RailwayPath path = networkService.findFastestPath(
                train.getStartFacilityId(),
                train.getEndFacilityId(),
                1000.0 // Velocidade alta para priorizar o tempo mínimo
        );

        return (path != null) ? path.getSegments() : Collections.emptyList();
    }


    /**
     * Calcula o tempo de entrada e saída para cada segmento na rota.
     */
    private List<SimulationSegmentEntry> calculateTrainSegmentTimes(Train train, List<LineSegment> route) {
        List<SimulationSegmentEntry> entries = new ArrayList<>();
        LocalDateTime currentTime = train.getDepartureTime();

        for (LineSegment segment : route) {
            double maxSpeedAllowedKmh = segment.getVelocidadeMaxima();

            // 1. Calcular Vcalc (Velocidade Calculada)
            double calculatedSpeedKmh = Math.min(maxSpeedAllowedKmh, MAX_TRAIN_SPEED_KMH);

            if (calculatedSpeedKmh <= 0) { continue; }

            // 2. Calcular Tempo de Viagem
            double lengthKm = segment.getComprimento(); // Já em KM
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
                    calculatedSpeedKmh,
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
     * Analisa a linha temporal global para encontrar conflitos (em via única) e sugere cruzamentos.
     */
    public List<String> checkConflictsAndSuggestCrossings() {
        List<String> conflictReport = new ArrayList<>();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        for (Map.Entry<String, List<SimulationSegmentEntry>> entry : timeline.entrySet()) {
            List<SimulationSegmentEntry> segmentEntries = entry.getValue();

            // Só verificar se há pelo menos 2 comboios e se é via única
            if (segmentEntries.size() < 2 || segmentEntries.get(0).getSegment().getNumberTracks() > 1) {
                continue;
            }

            segmentEntries.sort((a, b) -> a.getEntryTime().compareTo(b.getEntryTime()));

            for (int i = 0; i < segmentEntries.size(); i++) {
                for (int j = i + 1; j < segmentEntries.size(); j++) {
                    SimulationSegmentEntry entry1 = segmentEntries.get(i);
                    SimulationSegmentEntry entry2 = segmentEntries.get(j);

                    // Conflito se os intervalos de tempo se sobrepõem
                    if (checkOverlap(entry1.getEntryTime(), entry1.getExitTime(),
                            entry2.getEntryTime(), entry2.getExitTime())) {

                        // ⚠️ Conflito
                        conflictReport.add(String.format("⚠️ CONFLICT: Train %s and Train %s overlap on segment %s",
                                entry1.getTrainId(),
                                entry2.getTrainId(),
                                entry1.getSegmentId()));

                        conflictReport.add(String.format("   - T%s: %s – %s",
                                entry1.getTrainId(),
                                entry1.getEntryTime().toLocalTime().format(timeFormatter),
                                entry1.getExitTime().toLocalTime().format(timeFormatter)));

                        conflictReport.add(String.format("   - T%s: %s – %s",
                                entry2.getTrainId(),
                                entry2.getEntryTime().toLocalTime().format(timeFormatter),
                                entry2.getExitTime().toLocalTime().format(timeFormatter)));

                        // 🔁 Sugestão de Cruzamento
                        String waitingTrainId = entry1.getTrainId();
                        String safePoint = findLastSafePoint(entry1.getSegment());

                        conflictReport.add(String.format("   - RECOMMENDED CROSSING: Train %s should wait at %s for Train %s to clear %s.",
                                waitingTrainId, safePoint, entry2.getTrainId(), entry1.getSegmentId()));
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

    // MOCK: Simula a obtenção do ID (mantido para evitar erros no código que o use, mas não é usado acima)
    private String getFacilityNameById(int facilityId) {
        return facilityRepo.findNameById(facilityId).orElse("Facility " + facilityId);
    }
}