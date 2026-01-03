package pt.ipp.isep.dei.domain;
import java.util.*;

public class Dijkstra {
    public static Map<Integer, Double> shortestPaths(Graph g, int src) {
        Map<Integer, Double> dist = new HashMap<>();
        for (int v : g.adj.keySet()) dist.put(v, Double.POSITIVE_INFINITY);

        dist.put(src, 0.0);
        PriorityQueue<double[]> pq = new PriorityQueue<>(Comparator.comparingDouble(a -> a[1]));
        pq.add(new double[]{src, 0.0});

        while (!pq.isEmpty()) {
            double[] current = pq.poll();
            int u = (int) current[0];

            if (current[1] > dist.get(u)) continue;

            for (Edge e : g.adj.get(u)) {
                double nd = dist.get(u) + e.weight();
                if (nd < dist.get(e.to())) {
                    dist.put(e.to(), nd);
                    pq.add(new double[]{e.to(), nd});
                }
            }
        }
        return dist;
    }
}
