package pt.ipp.isep.dei.controller;

import pt.ipp.isep.dei.domain.*;
import pt.ipp.isep.dei.repository.LocomotiveRepository;
import pt.ipp.isep.dei.repository.SegmentLineRepository;
import pt.ipp.isep.dei.repository.WagonRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

public class SchedulerController {

    private final SchedulerService schedulerService;
    private final SegmentLineRepository segmentRepo;
    private final LocomotiveRepository locoRepo;
    private final WagonRepository wagonRepo;

    public SchedulerController(SchedulerService schedulerService,
                               SegmentLineRepository segmentRepo,
                               LocomotiveRepository locoRepo,
                               WagonRepository wagonRepo) {
        this.schedulerService = schedulerService;
        this.segmentRepo = segmentRepo;
        this.locoRepo = locoRepo;
        this.wagonRepo = wagonRepo;
    }

    /**
     * Constrói a rota a partir de Facilities e despacha a viagem.
     */
    public SchedulerResult dispatchRouteByFacilities(
            String tripId,
            LocalDateTime departureTime,
            List<String> facilityIds, // Lista de IDs de Facility (START, INTERMÉDIAS, END)
            List<Locomotive> locomotives,
            List<Wagon> wagons)
    {
        List<LineSegment> route = findRouteSegmentsBetweenFacilities(facilityIds);

        if (route.isEmpty()) {
            // A exceção é lançada dentro do findRouteSegmentsBetweenFacilities
            throw new RuntimeException("A rota construída não contém segmentos válidos.");
        }

        // 2. Criar a Viagem
        TrainTrip trip = new TrainTrip(tripId, departureTime, route, locomotives, wagons);

        // 3. Despachar
        return schedulerService.dispatchTrains(Arrays.asList(trip));
    }

    /**
     * Constrói a rota completa encadeando os segmentos entre todas as Facilities fornecidas.
     */
    private List<LineSegment> findRouteSegmentsBetweenFacilities(List<String> facilityIds) {
        List<LineSegment> fullRoute = new ArrayList<>();

        // Se o utilizador só fornecer START e END, tentamos encontrar o caminho mais rápido/curto.
        if (facilityIds.size() == 2) {
            try {
                int startId = Integer.parseInt(facilityIds.get(0).trim());
                int endId = Integer.parseInt(facilityIds.get(1).trim());

                // CRÍTICO: Usa BFS para encontrar a rota A -> B, pois o findDirectSegment falha se a rota for multi-segmento
                return findPathUsingBFS(startId, endId);
            } catch (NumberFormatException e) {
                throw new RuntimeException("IDs de Facility devem ser números inteiros válidos.");
            }
        }

        // Se o utilizador fornecer Facilities intermédias (Rota Manual), encadeamos:
        for (int i = 0; i < facilityIds.size() - 1; i++) {
            try {
                int startId = Integer.parseInt(facilityIds.get(i).trim());
                int endId = Integer.parseInt(facilityIds.get(i + 1).trim());

                // Tenta encontrar o segmento direto entre Facility A a Facility B
                Optional<LineSegment> directSegment = segmentRepo.findDirectSegment(startId, endId);

                if (directSegment.isPresent()) {
                    fullRoute.add(directSegment.get());
                } else {
                    throw new RuntimeException("Rota inválida: Não foi encontrado segmento direto entre Facility " + startId + " e Facility " + endId);
                }
            } catch (NumberFormatException e) {
                throw new RuntimeException("IDs de Facility devem ser números inteiros válidos.");
            }
        }
        return fullRoute;
    }

    /**
     * Implementação de busca de caminho em largura (BFS) para encontrar a rota completa.
     */
    private List<LineSegment> findPathUsingBFS(int startId, int endId) {
        // Grafo não é mais necessário aqui, findDirectSegment é o método que usa a cache do repositório
        Map<Integer, List<LineSegment>> graph = segmentRepo.findAll().stream()
                .collect(Collectors.groupingBy(LineSegment::getIdEstacaoInicio));

        Map<Integer, LineSegment> edgeTo = new HashMap<>();
        Queue<Integer> queue = new LinkedList<>();
        Set<Integer> visited = new HashSet<>();

        queue.add(startId);
        visited.add(startId);

        while (!queue.isEmpty()) {
            int currentId = queue.poll();

            if (currentId == endId) {
                // Reconstruir o caminho
                List<LineSegment> path = new ArrayList<>();
                int curr = endId;
                while (edgeTo.containsKey(curr)) {
                    LineSegment seg = edgeTo.get(curr);
                    path.add(seg);
                    curr = (seg.getIdEstacaoFim() == curr) ? seg.getIdEstacaoInicio() : seg.getIdEstacaoFim();
                }
                Collections.reverse(path);
                return path;
            }

            for (LineSegment segment : graph.getOrDefault(currentId, Collections.emptyList())) {
                int neighborId = segment.getIdEstacaoFim();

                if (!visited.contains(neighborId)) {
                    visited.add(neighborId);
                    edgeTo.put(neighborId, segment);
                    queue.add(neighborId);
                }
            }
        }
        return Collections.emptyList(); // Caminho não encontrado
    }


    // Métodos de Listagem para UI
    public List<Locomotive> getAllLocomotives() { return locoRepo.findAll(); }
    public List<Wagon> getAllWagons() { return wagonRepo.findAll(); }
    public List<LineSegment> getAllSegments() { return segmentRepo.findAll(); }
    public WagonRepository getWagonRepository() { return this.wagonRepo; }
}