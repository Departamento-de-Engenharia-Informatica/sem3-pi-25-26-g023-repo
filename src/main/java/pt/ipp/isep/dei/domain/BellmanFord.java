package pt.ipp.isep.dei.domain;

import java.util.*;

public class BellmanFord {

    public record PathResult(List<Integer> path, double totalCost, List<Integer> cycle) {}

    public static PathResult findPath(Graph g, int startId, int targetId) {
        if (!g.adj.containsKey(startId) || !g.adj.containsKey(targetId)) {
            return new PathResult(new ArrayList<>(), 0, null);
        }

        Map<Integer, Double> dist = new HashMap<>();
        Map<Integer, Integer> predecessor = new HashMap<>();
        List<Integer> nodes = new ArrayList<>(g.adj.keySet());

        for (int node : nodes) dist.put(node, Double.POSITIVE_INFINITY);
        dist.put(startId, 0.0);

        // Relaxar V-1 vezes
        for (int i = 1; i < nodes.size(); i++) {
            for (int u : nodes) {
                Double du = dist.get(u);
                if (du == null || du == Double.POSITIVE_INFINITY) continue;

                for (Edge e : g.adj.get(u)) {
                    Double dv = dist.get(e.to());
                    if (dv != null && du + e.cost() < dv) {
                        dist.put(e.to(), du + e.cost());
                        predecessor.put(e.to(), u);
                    }
                }
            }
        }

        // Verificação de Ciclo Negativo
        for (int u : nodes) {
            Double du = dist.get(u);
            if (du == null || du == Double.POSITIVE_INFINITY) continue;
            for (Edge e : g.adj.get(u)) {
                Double dv = dist.get(e.to());
                if (dv != null && du + e.cost() < dv) {
                    return new PathResult(null, 0, traceCycle(predecessor, e.to()));
                }
            }
        }

        // Reconstruir Caminho
        List<Integer> path = new LinkedList<>();
        Integer curr = targetId;
        while (curr != null) {
            path.add(0, curr);
            if (curr == startId) break;
            curr = predecessor.get(curr);
        }
        return new PathResult(path, dist.get(targetId), null);
    }

    private static List<Integer> traceCycle(Map<Integer, Integer> pred, int start) {
        List<Integer> cycle = new ArrayList<>();
        int curr = start;
        // Mover para dentro do ciclo para garantir captura correta
        for (int i = 0; i < pred.size(); i++) {
            if (pred.containsKey(curr)) curr = pred.get(curr);
        }
        int entry = curr;
        do {
            cycle.add(curr);
            curr = pred.get(curr);
        } while (curr != entry);
        cycle.add(entry);
        return cycle;
    }
}