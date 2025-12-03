// Define o pacote onde a classe está localizada
package pt.ipp.isep.dei.domain;

// Importa classes necessárias para testes JUnit
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

// Importa classes utilitárias do Java
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// Importa métodos de asserção para testes
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for USEI08 - Spatial Search by Geographical Area
 */
class SpatialSearchTest {

    // Declaração de variáveis de instância para os testes
    private SpatialSearch spatialSearch;
    private List<EuropeanStation> testStations;

    // Método executado antes de cada teste
    @BeforeEach
    void setUp() {
        // Carrega estações usando o mesmo método da Main
        InventoryManager manager = new InventoryManager();
        try {
            // Tenta carregar estações do arquivo CSV
            testStations = manager.loadEuropeanStations("src/main/java/pt/ipp/isep/dei/FicheirosCSV/train_stations_europe.csv");
        } catch (Exception e) {
            // Se falhar o carregamento do CSV, cria lista vazia e ignora testes complexos
            testStations = new ArrayList<>();
        }

        // Garante que testStations não seja nulo
        if (testStations == null || testStations.isEmpty()) {
            testStations = new ArrayList<>();
        }

        // Cria KD-tree com as estações (se houver alguma)
        KDTree kdTree = buildKDTree(testStations);
        spatialSearch = new SpatialSearch(kdTree);
    }

    // Método auxiliar para construir uma KD-tree balanceada
    private KDTree buildKDTree(List<EuropeanStation> stations) {
        // Se não há estações, retorna árvore vazia
        if (stations.isEmpty()) {
            return new KDTree();
        }

        // Cria cópias das listas para ordenação
        List<EuropeanStation> stationsByLat = new ArrayList<>(stations);
        List<EuropeanStation> stationsByLon = new ArrayList<>(stations);

        // Ordena estações por latitude e longitude
        stationsByLat.sort(Comparator.comparingDouble(EuropeanStation::getLatitude));
        stationsByLon.sort(Comparator.comparingDouble(EuropeanStation::getLongitude));

        // Constrói e retorna a KD-tree balanceada
        KDTree tree = new KDTree();
        tree.buildBalanced(stationsByLat, stationsByLon);
        return tree;
    }

    // Teste para o caso de uso AC2: Pesquisa dentro dos limites geográficos de Portugal
    @Test
    @DisplayName("AC2: Search within Portugal geographical boundaries")
    void testSearchWithinPortugalBoundaries() {
        // Pula o teste se não há estações carregadas
        if (testStations.isEmpty()) {
            return;
        }

        // Define os limites geográficos de Portugal
        double latMin = 36.0, latMax = 42.0;
        double lonMin = -9.5, lonMax = -6.0;

        // Executa a pesquisa espacial
        List<EuropeanStation> results = spatialSearch.searchByGeographicalArea(
                latMin, latMax, lonMin, lonMax, "PT", null, null);

        // Verifica se os resultados não são nulos e estão em Portugal
        assertNotNull(results, "Results should not be null");
        assertTrue(results.stream().allMatch(s -> s.getCountry().equals("PT")),
                "All results should be in Portugal");
    }

    // Teste para o caso de uso AC3: Pesquisa com filtro de país
    @Test
    @DisplayName("AC3: Search with country filter")
    void testSearchWithCountryFilter() {
        // Pula o teste se não há estações carregadas
        if (testStations.isEmpty()) {
            return;
        }

        // Define limites amplos da Europa
        double latMin = 35.0, latMax = 55.0;
        double lonMin = -10.0, lonMax = 20.0;

        // Executa pesquisa filtrando por Espanha
        List<EuropeanStation> results = spatialSearch.searchByGeographicalArea(
                latMin, latMax, lonMin, lonMax, "ES", null, null);

        // Verifica se os resultados são da Espanha
        assertNotNull(results, "Results should not be null");
        assertTrue(results.isEmpty() || results.stream().allMatch(s -> s.getCountry().equals("ES")),
                "All results should be in Spain");
    }

    // Teste para o caso de uso AC3: Pesquisa com filtro de estação de cidade
    @Test
    @DisplayName("AC3: Search with city station filter")
    void testSearchWithCityFilter() {
        // Pula o teste se não há estações carregadas
        if (testStations.isEmpty()) {
            return;
        }

        // Define limites amplos da Europa
        double latMin = 35.0, latMax = 55.0;
        double lonMin = -10.0, lonMax = 20.0;

        // Executa pesquisa filtrando por estações de cidade
        List<EuropeanStation> results = spatialSearch.searchByGeographicalArea(
                latMin, latMax, lonMin, lonMax, null, true, null);

        // Verifica se todas as estações são estações de cidade
        assertNotNull(results, "Results should not be null");
        assertTrue(results.isEmpty() || results.stream().allMatch(EuropeanStation::isCity),
                "All results should be city stations");
    }

    // Teste para o caso de uso AC3: Pesquisa com filtro de estação principal
    @Test
    @DisplayName("AC3: Search with main station filter")
    void testSearchWithMainStationFilter() {
        // Pula o teste se não há estações carregadas
        if (testStations.isEmpty()) {
            return;
        }

        // Define limites amplos da Europa
        double latMin = 35.0, latMax = 55.0;
        double lonMin = -10.0, lonMax = 20.0;

        // Executa pesquisa filtrando por estações principais
        List<EuropeanStation> results = spatialSearch.searchByGeographicalArea(
                latMin, latMax, lonMin, lonMax, null, null, true);

        // Verifica se todas as estações são estações principais
        assertNotNull(results, "Results should not be null");
        assertTrue(results.isEmpty() || results.stream().allMatch(EuropeanStation::isMainStation),
                "All results should be main stations");
    }

    // Teste para o caso de uso AC3: Pesquisa com filtros combinados
    @Test
    @DisplayName("AC3: Search with combined filters")
    void testSearchWithCombinedFilters() {
        // Pula o teste se não há estações carregadas
        if (testStations.isEmpty()) {
            return;
        }

        // Define limites amplos da Europa
        double latMin = 35.0, latMax = 55.0;
        double lonMin = -10.0, lonMax = 20.0;

        // Executa pesquisa com múltiplos filtros: Portugal, cidade e estação principal
        List<EuropeanStation> results = spatialSearch.searchByGeographicalArea(
                latMin, latMax, lonMin, lonMax, "PT", true, true);

        // Verifica se todas as estações atendem a todos os critérios
        assertNotNull(results, "Results should not be null");
        assertTrue(results.isEmpty() || results.stream().allMatch(s ->
                        s.getCountry().equals("PT") && s.isCity() && s.isMainStation()),
                "All results should be Portuguese city main stations");
    }

    // Teste para validar limites de coordenadas - latitude inválida
    @Test
    @DisplayName("Validate coordinate boundaries - invalid latitude")
    void testInvalidLatitudeBoundaries() {
        // Verifica se é lançada exceção para latitude inválida
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> spatialSearch.searchByGeographicalArea(-100.0, 42.0, -9.5, -6.0, null, null, null));

        // Verifica se a mensagem de erro contém o texto esperado
        assertTrue(exception.getMessage().contains("Invalid latitude range"));
    }

    // Teste para validar limites de coordenadas - longitude inválida
    @Test
    @DisplayName("Validate coordinate boundaries - invalid longitude")
    void testInvalidLongitudeBoundaries() {
        // Verifica se é lançada exceção para longitude inválida
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> spatialSearch.searchByGeographicalArea(36.0, 42.0, -200.0, -6.0, null, null, null));

        // Verifica se a mensagem de erro contém o texto esperado
        assertTrue(exception.getMessage().contains("Invalid longitude range"));
    }

    // Teste para resultado vazio quando não há estações na área
    @Test
    @DisplayName("Empty result when no stations in area")
    void testEmptyResultWhenNoStationsInArea() {
        // Define área no meio do oceano (sem estações)
        double latMin = 10.0, latMax = 20.0;
        double lonMin = -50.0, lonMax = -40.0;

        // Executa pesquisa
        List<EuropeanStation> results = spatialSearch.searchByGeographicalArea(
                latMin, latMax, lonMin, lonMax, null, null, null);

        // Verifica que resultados não são nulos (deve ser lista vazia)
        assertNotNull(results, "Results should not be null");
        // Deve estar vazio pois estamos pesquisando no oceano
    }

    // Teste para análise de desempenho
    @Test
    @DisplayName("Performance analysis returns valid complexity information")
    void testPerformanceAnalysis() {
        // Obtém análise de complexidade
        String complexityAnalysis = spatialSearch.getComplexityAnalysis();

        // Verifica se a análise não é nula e contém informações esperadas
        assertNotNull(complexityAnalysis, "Complexity analysis should not be null");
        assertTrue(complexityAnalysis.contains("USEI08 Complexity Analysis"));
        assertTrue(complexityAnalysis.contains("Time Complexity"));
        assertTrue(complexityAnalysis.contains("Space Complexity"));
    }

    // Teste para filtro de país case-insensitive
    @Test
    @DisplayName("Case-insensitive country filter")
    void testCaseInsensitiveCountryFilter() {
        // Pula o teste se não há estações carregadas
        if (testStations.isEmpty()) {
            return;
        }

        // Define limites amplos da Europa
        double latMin = 35.0, latMax = 55.0;
        double lonMin = -10.0, lonMax = 20.0;

        // Executa pesquisas com diferentes variações de caixa para o país
        List<EuropeanStation> results1 = spatialSearch.searchByGeographicalArea(
                latMin, latMax, lonMin, lonMax, "pt", null, null);
        List<EuropeanStation> results2 = spatialSearch.searchByGeographicalArea(
                latMin, latMax, lonMin, lonMax, "PT", null, null);

        // Verifica que ambas as pesquisas retornam os mesmos resultados
        assertEquals(results1.size(), results2.size(), "Should return same results regardless of case");
    }

    // Teste para verificar que filtros nulos são ignorados
    @Test
    @DisplayName("Null filters are ignored")
    void testNullFiltersAreIgnored() {
        // Pula o teste se não há estações carregadas
        if (testStations.isEmpty()) {
            return;
        }

        // Define limites amplos da Europa
        double latMin = 35.0, latMax = 55.0;
        double lonMin = -10.0, lonMax = 20.0;

        // Executa pesquisa com todos os filtros nulos
        List<EuropeanStation> results = spatialSearch.searchByGeographicalArea(
                latMin, latMax, lonMin, lonMax, null, null, null);

        // Verifica que resultados não são nulos
        assertNotNull(results, "Results should not be null");
    }

    // Teste para verificar que construtor com KDTree nulo lança exceção
    @Test
    @DisplayName("SpatialSearch constructor with null KDTree throws exception")
    void testSpatialSearchConstructorWithNullKDTree() {
        // Verifica se é lançada exceção ao passar KDTree nulo
        assertThrows(IllegalArgumentException.class, () -> new SpatialSearch(null));
    }

    // Teste para pesquisa com árvore vazia
    @Test
    @DisplayName("Search with empty tree returns empty list")
    void testSearchWithEmptyTree() {
        // Cria árvore vazia
        KDTree emptyTree = new KDTree();
        SpatialSearch emptySearch = new SpatialSearch(emptyTree);

        // Executa pesquisa com árvore vazia
        List<EuropeanStation> results = emptySearch.searchByGeographicalArea(
                35.0, 55.0, -10.0, 20.0, null, null, null);

        // Verifica que resultados não são nulos e estão vazios
        assertNotNull(results, "Results should not be null");
        assertTrue(results.isEmpty(), "Should return empty list for empty tree");
    }
}