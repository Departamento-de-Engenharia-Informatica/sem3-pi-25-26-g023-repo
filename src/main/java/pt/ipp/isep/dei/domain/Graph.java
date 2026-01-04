package pt.ipp.isep.dei.domain;

import java.util.*;

public class Graph {
    public Map<Integer, StationMetrics> metricsMap = new HashMap<>();
    public Map<Integer, List<Edge>> adj = new HashMap<>();

    public void addStation(Station s) {
        metricsMap.put(s.idEstacao(), new StationMetrics(s));
        adj.putIfAbsent(s.idEstacao(), new ArrayList<>());
    }


    public void addEdge(int u, int v, double weight, double cost) {
        if (adj.containsKey(u) && adj.containsKey(v)) {
            // Adicionamos APENAS a direção definida no CSV
            adj.get(u).add(new Edge(v, weight, cost));
        }
    }
}
