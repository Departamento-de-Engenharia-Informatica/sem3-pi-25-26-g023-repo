package pt.ipp.isep.dei.domain;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Service dedicated to Network Flow Analysis (USEI14 - Sprint 3).
 * Works with CSV snapshots (stations.csv, lines.csv) instead of the live Database.
 */
public class RailwayFlowService {

    // --- Estruturas em Memória (CSV Cache) ---
    private final Map<Integer, String> csvStationNames = new HashMap<>();
    private final List<LineSegment> csvSegments = new ArrayList<>();
    private boolean csvLoaded = false;

    // Capacidade assumida se o CSV não tiver essa coluna
    private static final double DEFAULT_CAPACITY = 20.0;

    public RailwayFlowService() {
        // Construtor vazio, não precisa de Repositories da DB
    }

    /**
     * Carrega o grafo a partir dos ficheiros CSV.
     */
    public void loadGraphFromCSV(String stationsFile, String linesFile) throws IOException {
        if (csvLoaded) return; // Evita recarregar

        csvStationNames.clear();
        csvSegments.clear();

        // 1. Carregar Estações
        try (BufferedReader br = new BufferedReader(new FileReader(stationsFile))) {
            String line = br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    try {
                        int id = Integer.parseInt(parts[0].trim());
                        String name = parts[1].trim();
                        csvStationNames.put(id, name);
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        // 2. Carregar Linhas
        try (BufferedReader br = new BufferedReader(new FileReader(linesFile))) {
            String line = br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    try {
                        int from = Integer.parseInt(parts[0].trim());
                        int to = Integer.parseInt(parts[1].trim());
                        double dist = Double.parseDouble(parts[2].trim());

                        // Capacidade default para simulação
                        double capacity = DEFAULT_CAPACITY;

                        LineSegment seg = new LineSegment(
                                "CSV-" + from + "-" + to,
                                from, to, dist, 0, (int) capacity, null, null
                        );
                        csvSegments.add(seg);

                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        csvLoaded = true;
        System.out.println("LOG: RailwayFlowService loaded " + csvStationNames.size() + " stations and " + csvSegments.size() + " segments.");
    }

    public Map<Integer, String> getAllCsvStations() {
        return new TreeMap<>(csvStationNames);
    }

    public String getStationNameById(int id) {
        return csvStationNames.getOrDefault(id, null);
    }

    /**
     * USEI14 - Maximum throughput (Edmonds-Karp Algorithm).
     */
    public double maximumThroughput(int sourceId, int sinkId) {
        if (!csvLoaded || csvSegments.isEmpty()) {
            throw new RuntimeException("CSV Data not loaded. Call loadGraphFromCSV() first.");
        }

        // Construção do Grafo Residual
        Map<Integer, Map<Integer, Double>> residualGraph = new HashMap<>();

        for (LineSegment seg : csvSegments) {
            int u = seg.getIdEstacaoInicio();
            int v = seg.getIdEstacaoFim();
            double capacity = (double) seg.getNumberTracks();

            // Forward
            residualGraph.computeIfAbsent(u, k -> new HashMap<>()).put(v, capacity);
            residualGraph.computeIfAbsent(v, k -> new HashMap<>()).putIfAbsent(u, 0.0);

            // Backward/Bidirecional
            residualGraph.computeIfAbsent(v, k -> new HashMap<>()).put(u, capacity);
            residualGraph.computeIfAbsent(u, k -> new HashMap<>()).putIfAbsent(v, 0.0);
        }

        double maxFlow = 0.0;
        Map<Integer, Integer> parentMap = new HashMap<>();

        // Loop Edmonds-Karp (BFS)
        while (bfsAugmentingPath(residualGraph, sourceId, sinkId, parentMap)) {
            double pathFlow = Double.MAX_VALUE;
            int curr = sinkId;
            while (curr != sourceId) {
                int prev = parentMap.get(curr);
                pathFlow = Math.min(pathFlow, residualGraph.get(prev).get(curr));
                curr = prev;
            }

            maxFlow += pathFlow;
            curr = sinkId;
            while (curr != sourceId) {
                int prev = parentMap.get(curr);
                double oldFwd = residualGraph.get(prev).get(curr);
                residualGraph.get(prev).put(curr, oldFwd - pathFlow);

                double oldBwd = residualGraph.get(curr).getOrDefault(prev, 0.0);
                residualGraph.get(curr).put(prev, oldBwd + pathFlow);
                curr = prev;
            }
        }
        return maxFlow;
    }

    private boolean bfsAugmentingPath(Map<Integer, Map<Integer, Double>> rGraph, int source, int sink, Map<Integer, Integer> parent) {
        parent.clear();
        Queue<Integer> q = new LinkedList<>();
        Set<Integer> visited = new HashSet<>();
        q.add(source);
        visited.add(source);
        parent.put(source, -1);

        while (!q.isEmpty()) {
            int u = q.poll();
            if (u == sink) return true;

            Map<Integer, Double> neighbors = rGraph.get(u);
            if (neighbors != null) {
                for (Map.Entry<Integer, Double> entry : neighbors.entrySet()) {
                    int v = entry.getKey();
                    if (!visited.contains(v) && entry.getValue() > 0) {
                        visited.add(v);
                        parent.put(v, u);
                        q.add(v);
                        if (v == sink) return true;
                    }
                }
            }
        }
        return false;
    }
}