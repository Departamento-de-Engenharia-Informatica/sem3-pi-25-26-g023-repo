package pt.ipp.isep.dei.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class DijkstraTest {
    private Graph graph;

    @BeforeEach
    void setUp() {
        graph = new Graph();
        // Estações reais
        graph.addStation(new Station(6, "AALST", 50.9427, 4.0396, 0, 0));
        graph.addStation(new Station(151, "BEVEREN(W)", 51.2117, 4.2583, 0, 0));
        graph.addStation(new Station(906, "NIEUWKERKEN", 51.1925, 4.1866, 0, 0));

        // Arestas com pesos reais
        graph.addEdge(6, 151, 45.2, 0.0);
        graph.addEdge(151, 906, 5.1, 0.0);
        graph.addEdge(6, 906, 60.0, 0.0); // Caminho direto mais longo
    }

    @Test
    void testShortestPathToSelf() {
        Map<Integer, Double> dists = Dijkstra.shortestPaths(graph, 6);
        assertEquals(0.0, dists.get(6), "A distância para si próprio deve ser 0");
    }

    @Test
    void testShortestPathRealData() {
        // Caminho: 6 -> 151 -> 906 (45.2 + 5.1 = 50.3)
        // Comparado com caminho direto: 6 -> 906 (60.0)
        Map<Integer, Double> dists = Dijkstra.shortestPaths(graph, 6);

        double expectedDist = 50.3;
        assertEquals(expectedDist, dists.get(906), 0.001,
                "Dijkstra deve escolher o caminho via estação 151");
    }

    @Test
    void testUnreachableStation() {
        // Adicionar uma estação isolada
        graph.addStation(new Station(999, "ISOLATED", 0, 0, 0, 0));

        Map<Integer, Double> dists = Dijkstra.shortestPaths(graph, 6);
        assertEquals(Double.POSITIVE_INFINITY, dists.get(999),
                "Estação sem ligação deve ter distância infinita");
    }
}
