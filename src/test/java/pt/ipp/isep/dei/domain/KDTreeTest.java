package pt.ipp.isep.dei.domain;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue; // Para ignorar testes se o carregamento falhar

/**
 * Testes EXAUSTIVOS de Integração para a implementação da KDTree (USEI07).
 * Foca-se em validar a construção (buildBalanced), a integridade (tamanho)
 * e o balanceamento (altura e buckets) da estrutura utilizando o dataset real.
 */
class KDTreeTest {

    private static KDTree kdTree;
    private static StationIndexManager manager;
    private static int totalStationsCount;

    private static final String FILE_PATH = "src/main/java/pt/ipp/isep/dei/FicheirosCSV/train_stations_europe.csv";

    @BeforeAll
    static void setUp() throws IOException {
        System.out.println("--- KDTree Test Setup: Carregando e Construindo Índices (USEI07) ---");

        // Presume a existência do InventoryManager para carregar o CSV
        InventoryManager loader = new InventoryManager();
        manager = new StationIndexManager();

        // 1. Carregar o dataset completo
        List<EuropeanStation> loadedStations = loader.loadEuropeanStations(FILE_PATH);
        totalStationsCount = loader.getValidStationCount();

        // Garante que o carregamento da lista grande é bem-sucedido
        assumeTrue(totalStationsCount > 60000, "O carregamento do dataset falhou ou é muito pequeno. Testes de integração ignorados.");

        // 2. Passos USEI06: Construção dos índices BST (necessário para as listas ordenadas)
        manager.buildIndexes(loadedStations);

        List<EuropeanStation> orderedByLat = manager.getBstLatitude().inOrderTraversal();
        List<EuropeanStation> orderedByLon = manager.getBstLongitude().inOrderTraversal();

        // 3. Passo USEI07: construção da KD-Tree
        long startTime = System.nanoTime();
        kdTree = new KDTree();
        kdTree.buildBalanced(orderedByLat, orderedByLon);
        long endTime = System.nanoTime();

        System.out.printf("✅ KD-Tree construída com %d estações em %.2f ms%n", kdTree.size(), (endTime - startTime) / 1_000_000.0);
        System.out.println("-------------------------------------------------------");
    }

    // -------------------------------------------------------------
    // TESTES DE CONSTRUÇÃO E INTEGRIDADE (USEI07)
    // -------------------------------------------------------------

    @Test
    void testBuildBalanced_SizeAndIntegrity_RealData() {
        // Verifica se o tamanho final da KD-Tree corresponde ao número total de estações carregadas
        assertEquals(totalStationsCount, kdTree.size(),
                "O tamanho da KD-Tree deve ser igual ao número total de estações indexadas.");
    }

    // -------------------------------------------------------------
    // TESTES DE ESTATÍSTICAS E BALANCEAMENTO (USEI07)
    // -------------------------------------------------------------

    @Test
    void testBuildBalanced_BalanceCheck_RealData() {
        // Verifica a altura da árvore. Uma árvore balanceada deve ter altura logarítmica.
        int height = kdTree.height();
        int size = kdTree.size();

        // Cálculo conservador do limite superior de altura para uma árvore 2D balanceada
        int expectedMaxHeight = (int) (Math.log(size) / Math.log(2)) * 2;

        assertTrue(height < 50 && height <= expectedMaxHeight + 10,
                "A altura da KD-Tree (" + height + ") é alta demais, sugerindo desbalanceamento. Máximo esperado ~" + expectedMaxHeight);
    }

    @Test
    void testGetStats_IntegrityAndBucketDistribution() {
        // A KD-Tree usa buckets para agrupar estações com a mesma Latitude/Longitude.
        Map<Integer, Integer> bucketSizes = kdTree.getBucketSizes();

        // 1. Deve existir pelo menos um bucket com mais de 1 estação (devido a duplicados)
        assertTrue(bucketSizes.keySet().stream().anyMatch(size -> size > 1),
                "A KD-Tree deve gerar 'buckets' com tamanho > 1 devido a coordenadas duplicadas no dataset.");

        // 2. A soma de todos os elementos contidos nos buckets deve ser o total de estações
        int totalNodesInBuckets = bucketSizes.entrySet().stream()
                .mapToInt(entry -> entry.getKey() * entry.getValue())
                .sum();

        assertEquals(totalStationsCount, totalNodesInBuckets, "A soma dos elementos nos buckets deve ser o total de estações.");
    }
}