// File: pt.ipp.isep.dei.controller.SchedulerController.java
package pt.ipp.isep.dei.controller;

import pt.ipp.isep.dei.domain.*;
import pt.ipp.isep.dei.repository.LocomotiveRepository;
import pt.ipp.isep.dei.repository.SegmentLineRepository;
import pt.ipp.isep.dei.repository.WagonRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class SchedulerController {

    private final SchedulerService schedulerService;
    private final SegmentLineRepository segmentRepo;
    private final LocomotiveRepository locoRepo;
    private final WagonRepository wagonRepo;
    private final RailwayNetworkService networkService; // NOVO: Serviço de Rede Ferroviária

    // Construtor atualizado para injeção de dependência
    public SchedulerController(SchedulerService schedulerService,
                               SegmentLineRepository segmentRepo,
                               LocomotiveRepository locoRepo,
                               WagonRepository wagonRepo,
                               RailwayNetworkService networkService) {
        this.schedulerService = schedulerService;
        this.segmentRepo = segmentRepo;
        this.locoRepo = locoRepo;
        this.wagonRepo = wagonRepo;
        this.networkService = networkService;
    }

    /**
     * Constrói a rota a partir de Facilities e despacha a viagem.
     */
    public SchedulerResult dispatchRouteByFacilities(
            String tripId,
            LocalDateTime departureTime,
            List<String> facilityIds, // Lista de IDs de Facility (START, INTERMÉDIAS, END)
            List<Locomotive> locomotives, // Contém o PowerKW
            List<Wagon> wagons) // Contém o peso (Tara + Carga assumida)
    {
        // 1. Constrói a Rota
        List<LineSegment> route = findRouteSegmentsBetweenFacilities(facilityIds);

        if (route.isEmpty()) {
            throw new RuntimeException("A rota construída não contém segmentos válidos.");
        }

        // 2. Criar a Viagem
        TrainTrip trip = new TrainTrip(tripId, departureTime, route, locomotives, wagons);

        // 3. Despachar
        // A chamada a dispatchTrains desencadeará o cálculo da velocidade V_max_comboio no SchedulerService
        return schedulerService.dispatchTrains(Arrays.asList(trip));
    }

    /**
     * Constrói a rota completa encadeando os segmentos entre todas as Facilities fornecidas.
     * Tornamos este método PÚBLICO para ser usado pelo novo DispatcherService.
     */
    public List<LineSegment> findRouteSegmentsBetweenFacilities(List<String> facilityIds) {
        List<LineSegment> fullRoute = new ArrayList<>();

        // Usamos um limite de velocidade muito alto (1000 km/h) para que o Dijkstra encontre o caminho
        // mais rápido de acordo com as velocidades MÁXIMAS dos segmentos (Vseg), não por um limite externo.
        final double MAX_SPEED_FOR_CALC = 1000.0;

        // Itera sobre todos os pares de Facilities
        for (int i = 0; i < facilityIds.size() - 1; i++) {
            try {
                int startId = Integer.parseInt(facilityIds.get(i).trim());
                int endId = Integer.parseInt(facilityIds.get(i + 1).trim());

                // Tenta encontrar o segmento direto entre Facility A a Facility B
                Optional<LineSegment> directSegment = segmentRepo.findDirectSegment(startId, endId);

                if (directSegment.isPresent()) {
                    fullRoute.add(directSegment.get());
                } else {
                    // Se não houver segmento direto (rota longa ou multi-segmento), usa Dijkstra
                    RailwayPath path = networkService.findFastestPath(startId, endId, MAX_SPEED_FOR_CALC);
                    if (path == null || path.getSegments().isEmpty()) {
                        throw new RuntimeException("Rota inválida: Não foi encontrado caminho (Dijkstra) entre Facility " + startId + " e Facility " + endId);
                    }
                    fullRoute.addAll(path.getSegments());
                }
            } catch (NumberFormatException e) {
                throw new RuntimeException("IDs de Facility devem ser números inteiros válidos.");
            }
        }
        return fullRoute;
    }

    // Métodos de Listagem para UI
    public List<Locomotive> getAllLocomotives() { return locoRepo.findAll(); }
    public List<Wagon> getAllWagons() { return wagonRepo.findAll(); }
    public List<LineSegment> getAllSegments() { return segmentRepo.findAll(); }
    public WagonRepository getWagonRepository() { return this.wagonRepo; }
}