package pt.ipp.isep.dei.domain;
import java.util.Map;

public class HarmonicCloseness {

    /**
     * Calcula a proximidade harmónica baseada no somatório dos inversos das distâncias.
     */
    public static void compute(Graph g) {
        for (StationMetrics m : g.metricsMap.values()) {
            // Calcula os caminhos mais curtos a partir desta estação para todas as outras
            Map<Integer, Double> dists = Dijkstra.shortestPaths(g, m.getStation().idEstacao());

            double hc = 0.0;
            for (Double d : dists.values()) {
                // Soma o inverso da distância para todos os nós alcançáveis (d > 0)
                if (d > 0 && d < Double.POSITIVE_INFINITY) {
                    hc += 1.0 / d;
                }
            }
            m.harmonicCloseness = hc;
        }
    }
}
