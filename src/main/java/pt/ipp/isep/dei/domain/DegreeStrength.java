package pt.ipp.isep.dei.domain;
import java.util.List;

public class DegreeStrength {

    /**
     * Calcula o grau (número de ligações) e a força (soma dos pesos das ligações)
     * para cada estação no grafo.
     */
    public static void compute(Graph g) {
        for (StationMetrics m : g.metricsMap.values()) {
            int id = m.getStation().idEstacao();
            List<Edge> edges = g.adj.get(id);

            if (edges != null) {
                m.degree = edges.size(); // Número de ligações diretas
                m.strength = 0.0;
                for (Edge e : edges) {
                    m.strength += e.weight(); // Soma dos pesos (distâncias) das arestas
                }
            } else {
                m.degree = 0;
                m.strength = 0.0;
            }
        }
    }
}
