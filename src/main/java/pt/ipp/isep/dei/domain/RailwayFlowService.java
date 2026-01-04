package pt.ipp.isep.dei.domain;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Service dedicated to network flow analysis (USEI14).
 * This class implements maximum flow logic to determine the total transport
 * capacity between two network hubs.
 *
 * The solution is based on the Edmonds-Karp algorithm, which is an implementation
 * of the Ford-Fulkerson method that uses Breadth-First Search (BFS) to find
 * augmenting paths. This approach ensures that the path selected in each iteration
 * is the shortest in terms of number of edges, optimizing the algorithm's convergence.
 */
public class RailwayFlowService {

    /** Mapping of IDs to station names loaded from snapshots. */
    private final Map<Integer, String> csvStationNames = new HashMap<>();

    /** List of line segments processed from the data files. */
    private final List<LineSegment> csvSegments = new ArrayList<>();

    /** State flag to control data loading. */
    private boolean csvLoaded = false;

    public RailwayFlowService() {
    }

    /**
     * Data structure to encapsulate the results of the maximum flow calculation.
     * Includes the route summary, the flow value, and the corresponding complexity analysis.
     */
    public record MaxFlowResult(String source, String sink, int maxFlow, String complexity) {
        @Override
        public String toString() {
            return String.format("Summary: Source: %s, Sink: %s, Max Throughput: %d units.\nComplexity Analysis: %s",
                    source, sink, maxFlow, complexity);
        }
    }

    /**
     * Parses station and connection data from CSV files.
     * * @param stationsFile Path to the station metadata file.
     * @param linesFile Path to the file containing segments and their capacities.
     * @throws IOException If an error occurs while reading the files.
     */
    public void loadGraphFromCSV(String stationsFile, String linesFile) throws IOException {
        if (csvLoaded) return;

        csvStationNames.clear();
        csvSegments.clear();

        // 1. Loading Stations
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

        // 2. Loading Connections and Capacities
        try (BufferedReader br = new BufferedReader(new FileReader(linesFile))) {
            String line = br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    try {
                        int from = Integer.parseInt(parts[0].trim());
                        int to = Integer.parseInt(parts[1].trim());
                        double dist = Double.parseDouble(parts[2].trim());
                        int capacity = Integer.parseInt(parts[3].trim());

                        LineSegment seg = new LineSegment(
                                "CSV-" + from + "-" + to,
                                from, to, dist, 0, capacity, null, null
                        );
                        csvSegments.add(seg);

                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        csvLoaded = true;
        System.out.println("LOG: RailwayFlowService loaded " + csvStationNames.size() + " stations and " + csvSegments.size() + " segments.");
    }

    /**
     * Returns all loaded stations, sorted by ID.
     */
    public Map<Integer, String> getAllCsvStations() {
        return new TreeMap<>(csvStationNames);
    }

    /**
     * Resolves the name of a station based on its identifier.
     */
    public String getStationNameById(int id) {
        return csvStationNames.getOrDefault(id, "ID:" + id);
    }

    /**
     * Calculates the maximum flow between a source and a destination hub.
     * * The algorithm builds a residual graph where capacities are iteratively updated.
     * In each step, an augmenting path is sought. If found, the total flow is
     * increased by the "bottleneck" value of the path, and residual capacities
     * (both forward and backward edges) are updated accordingly.
     * * @param sourceId The ID of the source station.
     * @param sinkId The ID of the destination station.
     * @return An object containing flow details and O(V * E^2) time complexity.
     */
    public MaxFlowResult calculateMaximumThroughput(int sourceId, int sinkId) {
        if (!csvLoaded || csvSegments.isEmpty()) {
            throw new RuntimeException("Data not loaded. Please execute loadGraphFromCSV first.");
        }

        // Residual graph representation: Map<Source, Map<Destination, Capacity>>
        Map<Integer, Map<Integer, Integer>> residualGraph = new HashMap<>();

        // Initialize the residual graph considering the bidirectionality of the tracks
        for (LineSegment seg : csvSegments) {
            int u = seg.getIdEstacaoInicio();
            int v = seg.getIdEstacaoFim();
            int cap = seg.getNumberTracks();

            residualGraph.computeIfAbsent(u, k -> new HashMap<>()).put(v, cap);
            residualGraph.computeIfAbsent(v, k -> new HashMap<>()).put(u, cap);
        }

        int maxFlow = 0;
        Map<Integer, Integer> parentMap = new HashMap<>();

        // While an augmenting path with available capacity exists
        while (bfsShortestAugmentingPath(residualGraph, sourceId, sinkId, parentMap)) {

            // Identify the bottleneck capacity (minimum residual capacity) in the found path
            int pathFlow = Integer.MAX_VALUE;
            int curr = sinkId;

            while (curr != sourceId) {
                int prev = parentMap.get(curr);
                int availableCap = residualGraph.get(prev).get(curr);
                pathFlow = Math.min(pathFlow, availableCap);
                curr = prev;
            }

            // Update residual capacities and back-edges
            curr = sinkId;
            while (curr != sourceId) {
                int prev = parentMap.get(curr);

                // Reduce capacity in the direction of the flow
                int oldFwd = residualGraph.get(prev).get(curr);
                residualGraph.get(prev).put(curr, oldFwd - pathFlow);

                // Increase capacity in the reverse direction (flow cancellation)
                int oldBwd = residualGraph.get(curr).getOrDefault(prev, 0);
                residualGraph.get(curr).put(prev, oldBwd + pathFlow);

                curr = prev;
            }

            maxFlow += pathFlow;
        }

        String sourceName = getStationNameById(sourceId);
        String sinkName = getStationNameById(sinkId);

        return new MaxFlowResult(sourceName, sinkName, maxFlow, "O(V * E^2)");
    }

    /**
     * Finds the shortest augmenting path between source and sink in the residual graph.
     * Uses BFS to ensure the selected path has the minimum number of edges,
     * satisfying the Edmonds-Karp strategy.
     * * @return true if a path with positive capacity exists to the destination.
     */
    private boolean bfsShortestAugmentingPath(Map<Integer, Map<Integer, Integer>> rGraph, int source, int sink, Map<Integer, Integer> parent) {
        parent.clear();
        Queue<Integer> q = new LinkedList<>();
        Set<Integer> visited = new HashSet<>();

        q.add(source);
        visited.add(source);
        parent.put(source, -1);

        while (!q.isEmpty()) {
            int u = q.poll();

            if (u == sink) return true;

            Map<Integer, Integer> neighbors = rGraph.get(u);
            if (neighbors != null) {
                for (Map.Entry<Integer, Integer> entry : neighbors.entrySet()) {
                    int v = entry.getKey();
                    int residualCap = entry.getValue();

                    // Only explore unvisited edges with remaining residual capacity
                    if (!visited.contains(v) && residualCap > 0) {
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