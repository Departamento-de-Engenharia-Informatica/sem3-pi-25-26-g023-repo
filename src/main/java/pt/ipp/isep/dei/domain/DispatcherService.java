package pt.ipp.isep.dei.domain;

import pt.ipp.isep.dei.repository.FacilityRepository;
import pt.ipp.isep.dei.repository.TrainRepository;
import pt.ipp.isep.dei.repository.LocomotiveRepository;
// import pt.ipp.isep.dei.repository.SegmentLineRepository; // Já não é necessário

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Serviço responsável por orquestrar a simulação de comboios,
 * convertendo os objetos de repositório (Train) para objetos de domínio
 * (TrainTrip) e delegando o agendamento e resolução de conflitos
 * ao SchedulerService.
 */
public class DispatcherService {

    private final TrainRepository trainRepo;
    private final RailwayNetworkService networkService;
    private final FacilityRepository facilityRepo;
    private final LocomotiveRepository locomotiveRepo;
    private final SchedulerService schedulerService; // <--- NOVO CAMPO INJETADO (ESSENCIAL)

    // Constantes de Cálculo (Mantidas apenas para a lógica de criação do TrainTrip)
    private static final double DEFAULT_MAX_SPEED = 1000.0; // Velocidade alta para shortest path

    // NOTA: Os campos de timeline e os métodos de simulação antigos foram removidos.

    /**
     * Construtor do DispatcherService.
     * * @param trainRepo Repositório de comboios.
     * @param networkService Serviço de rede ferroviária.
     * @param facilityRepo Repositório de facilities.
     * @param locomotiveRepo Repositório de locomotivas.
     * @param schedulerService Serviço de agendamento e resolução de conflitos (NOVO).
     */
    public DispatcherService(
            TrainRepository trainRepo,
            RailwayNetworkService networkService,
            FacilityRepository facilityRepo,
            LocomotiveRepository locomotiveRepo,
            SchedulerService schedulerService) { // <--- CONSTRUTOR ATUALIZADO

        this.trainRepo = trainRepo;
        this.networkService = networkService;
        this.facilityRepo = facilityRepo;
        this.locomotiveRepo = locomotiveRepo;
        this.schedulerService = schedulerService; // <--- INJEÇÃO
    }

    // =================================================================================
    // 1. Agendamento Principal (scheduleTrains - NOVO NOME PARA REFLETIR O OBJETIVO)
    // =================================================================================

    /**
     * Prepara as viagens (TrainTrip) e delega a simulação e resolução de conflitos
     * ao SchedulerService, retornando o resultado final.
     * * @param trainsToSimulate Lista dos comboios (Trains) a simular.
     * @return SchedulerResult contendo o horário final e os conflitos resolvidos.
     */
    public SchedulerResult scheduleTrains(List<Train> trainsToSimulate) {
        List<TrainTrip> initialTrips = new ArrayList<>();

        // 1. Converter Trains em TrainTrips
        for (Train train : trainsToSimulate) {
            Optional<TrainTrip> trip = createTrainTrip(train);
            trip.ifPresent(initialTrips::add);
        }

        if (initialTrips.isEmpty()) {
            return new SchedulerResult();
        }

        // 2. Delega a simulação completa (cálculo de tempos + resolução de conflitos)
        // para o SchedulerService. O SchedulerService retorna os horários recalculados.
        return schedulerService.dispatchTrains(initialTrips);
    }

    /**
     * Converte um objeto Train para um TrainTrip, encontrando a rota e a locomotiva.
     */
    private Optional<TrainTrip> createTrainTrip(Train train) {
        // Encontra a rota.
        List<LineSegment> route = findRouteForTrain(train);

        if (route.isEmpty()) {
            System.err.println("Skipping Train " + train.getTrainId() + ": Route not found for Route ID " + train.getRouteId());
            return Optional.empty();
        }

        // Encontra a locomotiva para construir o TrainTrip.
        List<Locomotive> locomotives = Collections.emptyList();
        try {
            int locoId = Integer.parseInt(train.getLocomotiveId());
            Optional<Locomotive> optLoco = locomotiveRepo.findById(locoId);
            if (optLoco.isPresent()) {
                locomotives = Collections.singletonList(optLoco.get());
            }
        } catch (NumberFormatException e) {
            System.err.println("Error: Locomotive ID is not a number for train " + train.getTrainId());
        }

        // Cria o TrainTrip. O cálculo de peso/potência e Vmax será feito pelo SchedulerService.
        TrainTrip trip = new TrainTrip(
                train.getTrainId(),
                train.getDepartureTime(),
                route,
                locomotives,
                Collections.emptyList() // Assumindo 0 vagões para simplificação ou use o repo de Vagões se aplicável.
        );

        return Optional.of(trip);
    }

    /**
     * Usa o RailwayNetworkService para encontrar a rota completa (baseado no Route ID do comboio).
     */
    private List<LineSegment> findRouteForTrain(Train train) {
        // Encontra o caminho mais rápido/mínimo, usando uma velocidade alta para priorizar o tempo mínimo
        RailwayPath path = networkService.findFastestPath(
                train.getStartFacilityId(),
                train.getEndFacilityId(),
                DEFAULT_MAX_SPEED
        );

        return (path != null) ? path.getSegments() : Collections.emptyList();
    }


    // =================================================================================
    // 2. MÉTODOS OBSOLETOS REMOVIDOS
    // =================================================================================
    /*
     * Os métodos runSimulation (antigo), calculateTrainSegmentTimes,
     * updateGlobalTimeline e checkConflictsAndSuggestCrossings foram removidos.
     */
}