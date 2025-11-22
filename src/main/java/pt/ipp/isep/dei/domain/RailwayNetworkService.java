package pt.ipp.isep.dei.domain;

import pt.ipp.isep.dei.repository.StationRepository;
import pt.ipp.isep.dei.repository.SegmentLineRepository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for railway network calculations and path finding.
 */
public class RailwayNetworkService {

    private final StationRepository estacaoRepo;
    private final SegmentLineRepository segmentoRepo;

    public RailwayNetworkService(StationRepository estacaoRepo, SegmentLineRepository segmentoRepo) {
        this.estacaoRepo = estacaoRepo;
        this.segmentoRepo = segmentoRepo;
    }

    /**
     * Finds the fastest path between two stations using Dijkstra's algorithm,
     * considerando a given maximum speed limit.
     *
     * @param idPartida ID da estação de partida.
     * @param idChegada ID da estação de chegada.
     * @param maxSpeedLimit A velocidade máxima em km/h a ser usada no cálculo.
     * @return O caminho mais rápido (RailwayPath) ou null se não houver caminho.
     */
    public RailwayPath findFastestPath(int idPartida, int idChegada, double maxSpeedLimit) {
        if (maxSpeedLimit <= 0) {
            throw new IllegalArgumentException("Maximum speed limit must be a positive value.");
        }

        List<LineSegment> allSegments = segmentoRepo.findAll();

        // --- CONSTRUÇÃO DO GRAFO DE ADJACÊNCIAS ---
        Map<Integer, List<LineSegment>> adj = new HashMap<>();
        Set<Integer> allFacilityIds = new HashSet<>();

        for (LineSegment seg : allSegments) {
            allFacilityIds.add(seg.getIdEstacaoInicio());
            allFacilityIds.add(seg.getIdEstacaoFim());

            // Adiciona o segmento (aresta) à lista de segmentos que saem do nó de início
            adj.computeIfAbsent(seg.getIdEstacaoInicio(), k -> new ArrayList<>()).add(seg);
        }
        // --- FIM CONSTRUÇÃO DO GRAFO ---

        // Mapas para o algoritmo de Dijkstra
        Map<Integer, Double> timeTo = new HashMap<>();
        Map<Integer, LineSegment> edgeTo = new HashMap<>();
        Map<Integer, Integer> predecessorNode = new HashMap<>();
        PriorityQueue<Map.Entry<Integer, Double>> pq = new PriorityQueue<>(Map.Entry.comparingByValue());

        // Inicializar distâncias
        for (int facilityId : allFacilityIds) {
            timeTo.put(facilityId, Double.POSITIVE_INFINITY);
        }

        if (!timeTo.containsKey(idPartida) || !timeTo.containsKey(idChegada)) {
            return null;
        }

        timeTo.put(idPartida, 0.0);
        pq.add(new AbstractMap.SimpleEntry<>(idPartida, 0.0));

        while (!pq.isEmpty()) {
            int u = pq.poll().getKey();

            if (u == idChegada) {
                break;
            }

            // Relaxar SOMENTE as arestas (segmentos) que saem de 'u'
            for (LineSegment seg : adj.getOrDefault(u, Collections.emptyList())) {

                int v = seg.getIdEstacaoFim();

                // Calcula o tempo de viagem
                double effectiveSpeed = Math.min(seg.getVelocidadeMaxima(), maxSpeedLimit);
                double lengthKm = seg.getComprimento();

                double travelTime;
                if (effectiveSpeed <= 0 || lengthKm <= 0) {
                    travelTime = Double.POSITIVE_INFINITY;
                } else {
                    travelTime = lengthKm / effectiveSpeed; // Tempo em horas
                }

                if (travelTime <= 0 || Double.isInfinite(travelTime) || Double.isNaN(travelTime)) {
                    continue;
                }

                if (timeTo.get(u) + travelTime < timeTo.get(v)) {
                    double newTime = timeTo.get(u) + travelTime;
                    timeTo.put(v, newTime);
                    edgeTo.put(v, seg);
                    predecessorNode.put(v, u);

                    Map.Entry<Integer, Double> oldEntry = new AbstractMap.SimpleEntry<>(v, timeTo.get(v));
                    pq.remove(oldEntry);
                    pq.add(new AbstractMap.SimpleEntry<>(v, newTime));
                }
            }
        }

        if (!timeTo.containsKey(idChegada) || timeTo.get(idChegada) == Double.POSITIVE_INFINITY) {
            return null;
        }

        // Reconstruir o caminho
        List<LineSegment> path = new ArrayList<>();
        double totalDistance = 0;
        int curr = idChegada;
        while (curr != idPartida) {
            if (!predecessorNode.containsKey(curr)) {
                return null;
            }
            LineSegment seg = edgeTo.get(curr);
            if (seg == null) {
                return null;
            }
            path.add(seg);
            totalDistance += seg.getComprimento();

            curr = seg.getIdEstacaoInicio();

            if (curr == seg.getIdEstacaoFim()) {
                return null;
            }
        }
        Collections.reverse(path);

        return new RailwayPath(path, totalDistance, timeTo.get(idChegada));
    }

    /**
     * Encontra todos os IDs de facilities alcançáveis (reachable) a partir de um ponto de partida.
     * Implementa uma busca em largura (BFS) no grafo ferroviário.
     * @param startFacilityId ID da facility de partida.
     * @return Lista de IDs de facilities alcançáveis.
     */
    public List<Integer> findAllReachableFacilities(int startFacilityId) {
        Set<Integer> reachable = new HashSet<>();
        Queue<Integer> queue = new LinkedList<>();

        // 1. Constrói a Lista de Adjacência bidirecional
        Map<Integer, List<LineSegment>> adj = new HashMap<>();
        for (LineSegment seg : segmentoRepo.findAll()) {
            // Adiciona as duas direções para garantir a conectividade
            adj.computeIfAbsent(seg.getIdEstacaoInicio(), k -> new ArrayList<>()).add(seg);
            adj.computeIfAbsent(seg.getIdEstacaoFim(), k -> new ArrayList<>()).add(seg);
        }

        // 2. Inicializa a busca em largura (BFS)
        queue.add(startFacilityId);
        reachable.add(startFacilityId);

        while (!queue.isEmpty()) {
            int current = queue.poll();

            for (LineSegment seg : adj.getOrDefault(current, Collections.emptyList())) {
                // Determina o vizinho
                int neighbor = (seg.getIdEstacaoInicio() == current) ? seg.getIdEstacaoFim() : seg.getIdEstacaoInicio();

                if (!reachable.contains(neighbor)) {
                    reachable.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }

        // 3. Retorna a lista de IDs alcançáveis (excluindo o próprio ponto de partida)
        reachable.remove(startFacilityId);
        return new ArrayList<>(reachable);
    }
}