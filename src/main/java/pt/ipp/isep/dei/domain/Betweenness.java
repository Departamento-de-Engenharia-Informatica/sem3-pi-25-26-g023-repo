package pt.ipp.isep.dei.domain;

import java.util.*;

public class Betweenness {
    public static void compute(Graph g) {
        g.metricsMap.values().forEach(m -> m.betweenness = 0.0);

        for (int sId : g.metricsMap.keySet()) {
            Stack<Integer> S = new Stack<>();
            Map<Integer, List<Integer>> P = new HashMap<>();
            Map<Integer, Double> sigma = new HashMap<>(), dist = new HashMap<>();

            g.metricsMap.keySet().forEach(v -> {
                P.put(v, new ArrayList<>());
                sigma.put(v, 0.0);
                dist.put(v, Double.POSITIVE_INFINITY);
            });

            sigma.put(sId, 1.0); dist.put(sId, 0.0);
            PriorityQueue<double[]> pq = new PriorityQueue<>(Comparator.comparingDouble(a -> a[1]));
            pq.add(new double[]{sId, 0.0});

            while (!pq.isEmpty()) {
                int v = (int) pq.poll()[0]; S.push(v);
                for (Edge e : g.adj.get(v)) {
                    double nd = dist.get(v) + e.weight();
                    if (nd < dist.get(e.to())) {
                        dist.put(e.to(), nd); pq.add(new double[]{e.to(), nd});
                        sigma.put(e.to(), sigma.get(v));
                        P.get(e.to()).clear(); P.get(e.to()).add(v);
                    } else if (nd == dist.get(e.to())) {
                        sigma.put(e.to(), sigma.get(e.to()) + sigma.get(v));
                        P.get(e.to()).add(v);
                    }
                }
            }
            Map<Integer, Double> delta = new HashMap<>();
            g.metricsMap.keySet().forEach(v -> delta.put(v, 0.0));
            while (!S.isEmpty()) {
                int w = S.pop();
                for (int v : P.get(w)) {
                    delta.put(v, delta.get(v) + (sigma.get(v) / sigma.get(w)) * (1.0 + delta.get(w)));
                }
                if (w != sId) g.metricsMap.get(w).betweenness += delta.get(w);
            }
        }
    }
}
