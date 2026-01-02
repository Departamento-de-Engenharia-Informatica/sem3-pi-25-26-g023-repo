package pt.ipp.isep.dei.domain;

import java.util.*;

public class Graph {
    public Map<Integer, StationMetrics> metricsMap = new HashMap<>();
    public Map<Integer, List<Edge>> adj = new HashMap<>();

    public void addStation(Station s) {
        metricsMap.put(s.idEstacao(), new StationMetrics(s));
        adj.putIfAbsent(s.idEstacao(), new ArrayList<>());
    }

    // MUDANÇA AQUI: Adicionar 'double cost' nos parâmetros
    public void addEdge(int u, int v, double weight, double cost) {
        if (adj.containsKey(u) && adj.containsKey(v)) {
            // Passamos o 'cost' para o construtor da Edge
            adj.get(u).add(new Edge(v, weight, cost));
            adj.get(v).add(new Edge(u, weight, cost));
        }
    }
}
