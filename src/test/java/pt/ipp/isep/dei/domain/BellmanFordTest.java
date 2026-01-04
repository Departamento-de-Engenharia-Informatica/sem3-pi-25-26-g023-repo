package pt.ipp.isep.dei.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class BellmanFordTest {
    private Graph graph;

    @BeforeEach
    void setUp() {
        graph = new Graph();
        // Estações Reais
        graph.addStation(new Station(6, "AALST", 0, 0, 0, 0));
        graph.addStation(new Station(151, "BEVEREN", 0, 0, 0, 0));
        graph.addStation(new Station(906, "NIEUWKERKEN", 0, 0, 0, 0));
        graph.addStation(new Station(351, "GENT-SINT-PIETERS", 0, 0, 0, 0));
    }

    @Test
    void testFindPathWithNegativeCostsButNoCycle() {
        // Cenário: Caminho com pesos negativos, mas sem ciclo
        // 351 -> 6 (custo 10)
        // 6 -> 151 (custo -5) -> Um "atalho" ou bónus de risco
        graph.addEdge(351, 6, 50.0, 10.0);
        graph.addEdge(6, 151, 20.0, -5.0);

        BellmanFord.PathResult result = BellmanFord.findPath(graph, 351, 151);

        assertNull(result.cycle(), "Não deve detetar ciclo aqui.");
        assertNotNull(result.path(), "Deve encontrar um caminho.");
        assertEquals(5.0, result.totalCost(), 0.001, "O custo total deve ser 10 + (-5) = 5");
        assertEquals(Integer.valueOf(351), result.path().get(0));
        assertEquals(Integer.valueOf(151), result.path().get(2));
    }

    @Test
    void testDetectNegativeCycle() {
        // Cenário: O ciclo vicioso que discutimos (Erro de Configuração)
        // 6 -> 151 (custo -2)
        // 151 -> 906 (custo -2)
        // 906 -> 6 (custo -2) -> Total do ciclo = -6 (Loop Infinito)
        graph.addEdge(6, 151, 10.0, -2.0);
        graph.addEdge(151, 906, 10.0, -2.0);
        graph.addEdge(906, 6, 10.0, -2.0);

        // Tentamos procurar um caminho partindo da estação 6
        BellmanFord.PathResult result = BellmanFord.findPath(graph, 6, 351);

        assertNotNull(result.cycle(), "Deve detetar o ciclo negativo.");
        assertTrue(result.cycle().contains(6));
        assertTrue(result.cycle().contains(151));
        assertTrue(result.cycle().contains(906));

        // Verifica se o ciclo fecha nele próprio (o primeiro e último elemento são iguais no ciclo retornado)
        int first = result.cycle().get(0);
        int last = result.cycle().get(result.cycle().size() - 1);
        assertEquals(first, last, "O ciclo deve começar e terminar no mesmo vértice.");
    }
}