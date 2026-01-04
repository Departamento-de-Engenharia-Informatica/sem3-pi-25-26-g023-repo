package pt.ipp.isep.dei.controller;

import pt.ipp.isep.dei.domain.*;
import pt.ipp.isep.dei.repository.FacilityRepository;
import pt.ipp.isep.dei.repository.SegmentLineRepository;

import java.util.*;
import java.util.stream.Collectors;

public class RoutePlannerController {

    private final RailwayNetworkService networkService;
    private final FacilityRepository facilityRepo;
    private final SegmentLineRepository segmentRepo;

    // Velocidade teórica para planeamento
    private static final double PLANNING_SPEED_LIMIT = 120.0;

    public RoutePlannerController(RailwayNetworkService networkService,
                                  FacilityRepository facilityRepo,
                                  SegmentLineRepository segmentRepo) {
        this.networkService = networkService;
        this.facilityRepo = facilityRepo;
        this.segmentRepo = segmentRepo;
    }

    public List<Station> getAllStations() {
        List<Station> stations = new ArrayList<>();
        Map<Integer, String> facilities = facilityRepo.findAllFacilityNames();

        if (facilities.isEmpty()) {
            return stations;
        }

        for (Map.Entry<Integer, String> entry : facilities.entrySet()) {
            stations.add(new Station(entry.getKey(), entry.getValue(), 0, 0, 0, 0));
        }

        stations.sort(Comparator.comparing(Station::nome));
        return stations;
    }

    public List<Station> getActiveStations() {
        Set<Integer> activeIds = new HashSet<>();
        List<LineSegment> segments = segmentRepo.findAll();

        if (segments.isEmpty()) return new ArrayList<>();

        for (LineSegment s : segments) {
            activeIds.add(s.getIdEstacaoInicio());
            activeIds.add(s.getIdEstacaoFim());
        }

        List<Station> stations = new ArrayList<>();
        for (Integer id : activeIds) {
            String name = getStationName(id);
            stations.add(new Station(id, name, 0, 0, 0, 0));
        }

        stations.sort(Comparator.comparing(Station::nome));
        return stations;
    }

    public List<Station> getReachableDestinations(int originId) {
        List<LineSegment> segments = segmentRepo.findAll();
        if (segments.isEmpty()) return new ArrayList<>();

        Map<Integer, List<Integer>> adj = new HashMap<>();
        for (LineSegment seg : segments) {
            int u = seg.getIdEstacaoInicio();
            int v = seg.getIdEstacaoFim();
            adj.computeIfAbsent(u, k -> new ArrayList<>()).add(v);
            adj.computeIfAbsent(v, k -> new ArrayList<>()).add(u);
        }

        if (!adj.containsKey(originId)) return new ArrayList<>();

        Set<Integer> visited = new HashSet<>();
        Queue<Integer> queue = new LinkedList<>();
        queue.add(originId);
        visited.add(originId);

        List<Station> reachable = new ArrayList<>();
        while (!queue.isEmpty()) {
            int current = queue.poll();
            if (current != originId) {
                reachable.add(new Station(current, getStationName(current), 0, 0, 0, 0));
            }
            for (int neighbor : adj.getOrDefault(current, Collections.emptyList())) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }
        reachable.sort(Comparator.comparing(Station::nome));
        return reachable;
    }

    public FreightRequest createFreightRequest(String id, int originId, int destId, String desc, double weight) {
        if (originId == destId) throw new IllegalArgumentException("Origem e Destino não podem ser iguais.");
        return new FreightRequest(id, originId, destId, desc, weight);
    }

    public PlannedRoute planRoute(int startStationId, List<FreightRequest> freights, boolean isSimpleRoute) {
        if (freights.isEmpty()) throw new IllegalArgumentException("Lista de cargas vazia.");
        if (isSimpleRoute && freights.size() > 1) {
            throw new IllegalArgumentException("Rotas Simples suportam apenas 1 carga.");
        }
        return calculateGreedyRoute(startStationId, freights);
    }

    /**
     * Algoritmo Principal com Manifesto Detalhado (Segmento a Segmento).
     */
    private PlannedRoute calculateGreedyRoute(int startStationId, List<FreightRequest> freights) {
        List<FreightRequest> pending = new ArrayList<>();
        for (FreightRequest f : freights) {
            pending.add(new FreightRequest(f.getId(), f.getOriginStationId(), f.getDestinationStationId(), f.getDescription(), f.getWeightTons()));
        }

        List<String> manifestLog = new ArrayList<>();
        List<LineSegment> fullRouteSegments = new ArrayList<>();
        int currentStationId = startStationId;

        manifestLog.add(String.format("🏁 LOCOMOTIVE START: %s (ID: %d)", getStationName(startStationId), startStationId));

        while (!pending.isEmpty()) {
            Map<Integer, List<FreightRequest>> targets = new HashMap<>();

            for (FreightRequest f : pending) {
                int target = f.isPickedUp() ? f.getDestinationStationId() : f.getOriginStationId();
                targets.computeIfAbsent(target, k -> new ArrayList<>()).add(f);
            }

            int bestTargetId = -1;
            double minTime = Double.MAX_VALUE;
            RailwayPath bestPath = null;

            for (Integer targetId : targets.keySet()) {
                if (targetId == currentStationId) {
                    bestTargetId = targetId;
                    minTime = 0;
                    bestPath = new RailwayPath(Collections.emptyList(), 0, 0);
                    break;
                }
                RailwayPath path = networkService.findFastestPath(currentStationId, targetId, PLANNING_SPEED_LIMIT);
                if (path != null && path.getTotalTimeHours() < minTime) {
                    minTime = path.getTotalTimeHours();
                    bestTargetId = targetId;
                    bestPath = path;
                }
            }

            if (bestTargetId == -1 || bestPath == null) {
                manifestLog.add("❌ ERRO: Rede desconexa. Impossível chegar aos destinos restantes.");
                break;
            }

            // --- MELHORIA AQUI: Log Detalhado dos Segmentos ---
            if (!bestPath.getSegments().isEmpty()) {
                fullRouteSegments.addAll(bestPath.getSegments());

                manifestLog.add(String.format("   🚆 MOVE to %s (Leg Total: %.2f km)",
                        getStationName(bestTargetId), bestPath.getTotalDistance()));

                // Simular o percurso segmento a segmento para mostrar no log
                int trackerNode = currentStationId;
                for (LineSegment seg : bestPath.getSegments()) {
                    // Determinar próximo nó com base na topologia do segmento
                    int nextNode = (seg.getIdEstacaoInicio() == trackerNode) ? seg.getIdEstacaoFim() : seg.getIdEstacaoInicio();

                    String segLog = String.format("      ↳ via [%s]: %s -> %s (%.2f km)",
                            seg.getIdSegmento(),
                            getStationName(trackerNode),
                            getStationName(nextNode),
                            seg.getComprimento());

                    manifestLog.add(segLog);
                    trackerNode = nextNode; // Avança o tracker
                }
            }

            currentStationId = bestTargetId;
            List<FreightRequest> actions = targets.get(currentStationId);

            if (actions != null) {
                for (FreightRequest f : actions) {
                    if (f.isPickedUp()) {
                        manifestLog.add(String.format("   ⬇️ UNLOAD: %s", f.getDescription()));
                        f.setDelivered(true);
                        pending.remove(f);
                    } else {
                        manifestLog.add(String.format("   ⬆️ LOAD: %s (Target: %s)",
                                f.getDescription(), getStationName(f.getDestinationStationId())));
                        f.setPickedUp(true);
                    }
                }
            }
        }

        manifestLog.add("🏁 ROUTE COMPLETED");
        return new PlannedRoute(fullRouteSegments, manifestLog);
    }

    private String getStationName(int id) {
        return facilityRepo.findNameById(id).orElse("ID " + id);
    }

    public record PlannedRoute(List<LineSegment> segments, List<String> manifest) {
        public double getTotalDistance() {
            return segments.stream().mapToDouble(LineSegment::getComprimento).sum();
        }
    }
}