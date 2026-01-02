package pt.ipp.isep.dei.domain;
import java.util.*;

public class Graph {
    public Map<Integer, StationMetrics> metricsMap = new HashMap<>();
    public Map<Integer, List<Edge>> adj = new HashMap<>();

    public void addStation(Station s) {
        metricsMap.put(s.idEstacao(), new StationMetrics(s));
        adj.putIfAbsent(s.idEstacao(), new ArrayList<>());
    }

    public void addEdge(int u, int v, double weight) {
        adj.get(u).add(new Edge(v, weight));
        adj.get(v).add(new Edge(u, weight));
    }
}
