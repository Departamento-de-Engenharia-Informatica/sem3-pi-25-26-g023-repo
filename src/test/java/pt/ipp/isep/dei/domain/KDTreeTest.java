package pt.ipp.isep.dei.domain;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes EXAUSTIVOS para a implementação da KDTree (USEI07).
 * Utiliza o dataset real e garante que o acesso à raiz e ao Manager está correto.
 */
class KDTreeTest {

    private static KDTree kdTree;
    private static StationIndexManager manager;
    private static int totalStationsCount;

    private static final String FILE_PATH = "src/main/java/pt/ipp/isep/dei/FicheirosCSV/train_stations_europe.csv";

    @BeforeAll
    static void setUp() throws IOException {
        InventoryManager loader = new InventoryManager();
        manager = new StationIndexManager();

        List<EuropeanStation> loadedStations = loader.loadEuropeanStations(FILE_PATH);
        totalStationsCount = loader.getValidStationCount();

        manager.buildIndexes(loadedStations);

        List<EuropeanStation> orderedByLat = manager.getBstLatitude().inOrderTraversal();
        List<EuropeanStation> orderedByLon = manager.getBstLongitude().inOrderTraversal();

        kdTree = new KDTree();
        kdTree.buildBalanced(orderedByLat, orderedByLon);

        assertTrue(totalStationsCount > 60000, "O carregamento do dataset falhou ou é muito pequeno.");
    }

    // -------------------------------------------------------------
    // TESTES DE CONSTRUÇÃO E INTEGRIDADE
    // -------------------------------------------------------------

    @Test
    void testBuildBalanced_SizeAndIntegrity_RealData() {
        assertEquals(totalStationsCount, kdTree.size(),
                "O tamanho da KD-Tree deve ser igual ao número total de estações indexadas.");
    }

    // -------------------------------------------------------------
    // TESTES DE ESTATÍSTICAS
    // -------------------------------------------------------------

    @Test
    void testBuildBalanced_BalanceCheck_RealData() {
        int height = kdTree.height();
        int size = kdTree.size();

        assertTrue(height < 50,
                "A altura da KD-Tree (" + height + ") é alta demais, sugerindo desbalanceamento.");
    }

    @Test
    void testGetStats_IntegrityAndBucketDistribution() {
        Map<Integer, Integer> bucketSizes = kdTree.getBucketSizes();

        assertTrue(bucketSizes.keySet().stream().anyMatch(size -> size > 1),
                "A KD-Tree deve gerar 'buckets' com tamanho > 1 devido a coordenadas duplicadas.");

        int totalNodesInBuckets = bucketSizes.entrySet().stream()
                .mapToInt(entry -> entry.getKey() * entry.getValue())
                .sum();

        assertEquals(totalStationsCount, totalNodesInBuckets, "A soma dos elementos nos buckets deve ser o total de estações.");
    }
}