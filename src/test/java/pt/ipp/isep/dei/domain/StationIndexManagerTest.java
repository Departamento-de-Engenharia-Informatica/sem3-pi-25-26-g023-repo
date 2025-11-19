package pt.ipp.isep.dei.domain;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários EXAUSTIVOS para as funcionalidades USEI06 (Indexação BST/AVL)
 * e USEI07 (Construção KD-Tree) utilizando o dataset real 'train_stations_europe.csv'.
 *
 * Objetivo: Cobertura total das APIs públicas, incluindo desempate e requisitos de balanceamento.
 */
class StationIndexManagerTest {

    // Caminho do arquivo real (padrão do projeto)
    private static final String FILE_PATH = "src/main/java/pt/ipp/isep/dei/FicheirosCSV/train_stations_europe.csv";

    // Serviços de domínio e dados estáticos
    private static StationIndexManager manager;
    private static InventoryManager loader;
    private static int totalStationsCount;

    // Coordenadas conhecidas para testes (com base no enunciado - onde se espera duplicatas/complexidade)
    private static final double LISBON_APOLONIA_LAT = 38.71387;
    private static final double EXTREME_LAT_MAX = 90.0;
    private static final double EXTREME_LON_MIN = -180.0;

    @BeforeAll
    static void setUp() {
        loader = new InventoryManager();
        manager = new StationIndexManager();

        try {
            // 1. Carrega o dataset real
            List<EuropeanStation> loadedStations = loader.loadEuropeanStations(FILE_PATH);
            totalStationsCount = loader.getValidStationCount();

            // 2. Constrói os índices BST/AVL (USEI06)
            manager.buildIndexes(loadedStations);

            // 3. Garante que a KD-Tree é construída antes dos testes (USEI07)
            manager.build2DTree();

            // Asserção de Sanidade
            assertTrue(totalStationsCount > 60000,
                    "O carregamento do dataset deve ter mais de 60.000 estações válidas.");

        } catch (Exception e) {
            fail("Falha catastrófica ao carregar e indexar o dataset real: " + e.getMessage());
        }
    }

    // =============================================================
    // 🧪 TESTES DE INTEGRIDADE (USEI06 & USEI07)
    // =============================================================

    @Test
    void testIntegrity_TotalCount_AllStructures() {
        // Garante que o número de elementos (valores) indexados é consistente em todas as estruturas.

        // BSTs (inOrderTraversal conta todos os valores, correto para chaves duplicadas)
        assertEquals(totalStationsCount, manager.getBstLatitude().inOrderTraversal().size(),
                "BST Latitude deve indexar o total de estações.");
        assertEquals(totalStationsCount, manager.getBstLongitude().inOrderTraversal().size(),
                "BST Longitude deve indexar o total de estações.");

        // KD-Tree (USEI07)
        assertEquals(totalStationsCount, manager.getStation2DTree().size(),
                "KD-Tree deve indexar o total de estações.");
    }

    @Test
    void testBuildIndexes_BST_EmptyTreeCreation() {
        // Verifica o size de uma BST recém-criada (sem usar o setUp)
        BST<Double, EuropeanStation> emptyBST = new BST<>();
        assertEquals(0, emptyBST.inOrderTraversal().size(), "O size de uma BST vazia deve ser 0.");
        assertTrue(emptyBST.inOrderTraversal().isEmpty());
    }


    // =============================================================
    // 🧪 TESTES DA USEI06 (Consultas de Chave e Ordenação)
    // =============================================================

    @Test
    void testBuildIndexes_DuplicateKeys_TiebreakerOrder() {
        // Requisito: Verifica o desempate (Nome ASC) em chaves duplicadas (Latitude).
        List<EuropeanStation> result = manager.getBstLatitude().findAll(LISBON_APOLONIA_LAT);

        assertTrue(result.size() > 1,
                "Consulta por Lat exata deve retornar múltiplas estações, confirmando o desempate.");

        // Verifica a ordenação (Nome ASC)
        String firstName = result.get(0).getStation();
        String secondName = result.get(1).getStation();

        assertTrue(firstName.compareTo(secondName) <= 0,
                "Estações com a mesma Latitude devem estar ordenadas alfabeticamente pelo nome (critério de desempate).");
    }

    @Test
    void testGetStationsByTimeZoneGroup_NonExistentKey() {
        // Teste: Chave que não existe deve retornar lista vazia.
        List<EuropeanStation> result = manager.getStationsByTimeZoneGroup("NON_EXISTENT_TZG");
        assertTrue(result.isEmpty(), "TZG inexistente deve retornar lista vazia.");
    }

    @Test
    void testGetStationsByTimeZoneGroup_WETGMT_OrderingExhaustive() {
        // Verifica a ordenação final (País ASC, Nome ASC).
        final String TZG = "WET/GMT";
        List<EuropeanStation> wetStations = manager.getStationsByTimeZoneGroup(TZG);

        // Verifica a ordenação final
        EuropeanStation prev = null;
        for (EuropeanStation current : wetStations) {
            if (prev != null) {
                int countryComparison = prev.getCountry().compareTo(current.getCountry());

                // Valida o TZG
                assertEquals(TZG, current.getTimeZoneGroup());

                // Valida a ordenação
                if (countryComparison == 0) {
                    assertTrue(prev.getStation().compareTo(current.getStation()) <= 0,
                            "Ordenação: Nome deve ser ASC quando País é igual.");
                } else {
                    assertTrue(countryComparison < 0,
                            "Ordenação: País deve ser ASC.");
                }
            }
            prev = current;
        }
    }

    @Test
    void testGetStationsInTimeZoneWindow_RangeQuery_Extremes() {
        // Testa o intervalo de TZG mais abrangente possível.
        final String TZG_MIN = "A"; // Mínimo alfabético
        final String TZG_MAX = "Z"; // Máximo alfabético

        List<EuropeanStation> windowStations = manager.getStationsInTimeZoneWindow(TZG_MIN, TZG_MAX);

        // Deve retornar todas as estações carregadas
        assertEquals(totalStationsCount, windowStations.size(), "O maior intervalo de TZG deve retornar todas as estações.");

        // Verifica a ordenação do TZG (ASC)
        EuropeanStation prev = null;
        for (EuropeanStation current : windowStations) {
            if (prev != null) {
                // A ordenação principal é pelo TimeZoneGroup
                assertTrue(prev.getTimeZoneGroup().compareTo(current.getTimeZoneGroup()) <= 0,
                        "Ordenação: TimeZoneGroup deve ser ASC.");
            }
            prev = current;
        }
    }

    // --- Testes de Consultas de Intervalo de Coordenadas (findInRange) ---

    @Test
    void testBST_LongitudeRangeQuery_ExtremeBoundaries() {
        // Testa o intervalo mais abrangente de Longitude (geográfico)
        final double MIN_LON = EXTREME_LON_MIN; // -180.0
        final double MAX_LON = 180.0;

        List<EuropeanStation> result = manager.getBstLongitude().findInRange(MIN_LON, MAX_LON);

        // Deve retornar todas as estações (assumindo que todas Lat/Lon são válidas)
        assertEquals(totalStationsCount, result.size(), "Intervalo Longitude extremo deve retornar todas as estações.");

        // Verifica a ordenação (Longitude ASC)
        double prevLon = -180.1;
        for (EuropeanStation s : result) {
            assertTrue(s.getLongitude() >= prevLon, "A lista deve estar ordenada pela Longitude.");
            prevLon = s.getLongitude();
        }
    }

    @Test
    void testBST_RangeQuery_SinglePointCase() {
        // Testa se findInRange(K, K) funciona como findAll(K) e mantém a ordem de desempate.
        List<EuropeanStation> result = manager.getBstLatitude().findInRange(LISBON_APOLONIA_LAT, LISBON_APOLONIA_LAT);

        assertTrue(result.size() > 1, "Intervalo de ponto único deve retornar duplicatas.");

        // Verifica que a ordenação está pelo nome (desempate)
        EuropeanStation prev = null;
        for (EuropeanStation current : result) {
            if (prev != null) {
                assertTrue(prev.getStation().compareTo(current.getStation()) <= 0,
                        "Intervalo de ponto único deve manter a ordenação de desempate (Nome ASC).");
            }
            prev = current;
        }
    }

    @Test
    void testEdgeCase_InvertedRangeReturnsEmpty() {
        // Testa um intervalo onde MAX < MIN (deve retornar lista vazia).
        List<EuropeanStation> latResult = manager.getBstLatitude().findInRange(50.0, 40.0);
        assertTrue(latResult.isEmpty(), "Um intervalo onde MAX < MIN deve retornar uma lista vazia (Latitude).");

        List<EuropeanStation> tzgResult = manager.getBstTimeZoneGroup().findInRange("Z", "A");
        assertTrue(tzgResult.isEmpty(), "Um intervalo TZG onde MAX < MIN deve retornar uma lista vazia.");
    }

    // =============================================================
    // 🧪 TESTES DA USEI07 (KD-Tree Construction & Stats)
    // =============================================================

    @Test
    void testKDTree_BalanceCheck_USEI07() {
        // Requisito não-funcional: A KD-Tree deve ser balanceada (O(N log N)).
        Map<String, Object> stats = manager.get2DTreeStats();
        int height = (int) stats.get("height");
        int size = (int) stats.get("size");

        // Limite de sanidade para altura balanceada (o máximo teoricamente é ~2 * log2(N))
        assertTrue(height < 50,
                "A altura da KD-Tree (" + height + ") é alta demais, sugerindo desbalanceamento.");
    }

    @Test
    void testKDTree_StatsReporting_USEI07() {
        // Requisito: Verifica se todas as estatísticas são reportadas e com tipos corretos.
        Map<String, Object> stats = manager.get2DTreeStats();

        assertTrue(stats.containsKey("size"), "Falta a estatística 'size'.");
        assertTrue(stats.containsKey("height"), "Falta a estatística 'height'.");
        assertTrue(stats.containsKey("bucketSizes"), "Falta a estatística 'bucketSizes'.");

        assertInstanceOf(Integer.class, stats.get("size"));
        assertInstanceOf(Integer.class, stats.get("height"));
        assertInstanceOf(Map.class, stats.get("bucketSizes"));
    }

    @Test
    void testKDTree_BucketDistributionCheck_USEI07() {
        // Requisito: Verifica se o mecanismo de desempate (múltiplos valores por nó) funcionou.
        Map<String, Object> stats = manager.get2DTreeStats();
        @SuppressWarnings("unchecked")
        Map<Integer, Integer> bucketSizes = (Map<Integer, Integer>) stats.get("bucketSizes");

        // Deve haver nós com 1 estação e nós com > 1 estação.
        assertTrue(bucketSizes.containsKey(1), "Deve haver nós folha com 1 estação.");
        assertTrue(bucketSizes.keySet().stream().anyMatch(size -> size > 1),
                "Deve haver nós folha que contêm mais de 1 estação (duplicatas de coordenadas).");
    }
}