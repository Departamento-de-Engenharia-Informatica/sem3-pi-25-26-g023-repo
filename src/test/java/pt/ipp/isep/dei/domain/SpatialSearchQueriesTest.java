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
 * Unit tests for SpatialSearchQueries - USEI08 Demo Queries
 */
class SpatialSearchQueriesTest {

    // Declaração da variável para a classe sendo testada
    private SpatialSearchQueries spatialSearchQueries;

    // Método executado antes de cada teste
    @BeforeEach
    void setUp() {
        // Carrega estações usando o mesmo método da Main
        InventoryManager manager = new InventoryManager();
        List<EuropeanStation> stations;
        try {
            // Tenta carregar estações do arquivo CSV
            stations = manager.loadEuropeanStations(
                    "src/main/java/pt/ipp/isep/dei/FicheirosCSV/train_stations_europe.csv");
        } catch (Exception e) {
            // Se falhar o carregamento do CSV, cria lista vazia
            stations = new ArrayList<>();
        }

        // Garante que stations não seja nulo
        if (stations == null || stations.isEmpty()) {
            stations = new ArrayList<>();
        }

        // Constrói a KD-tree e inicializa as dependências
        KDTree kdTree = buildKDTree(stations);
        SpatialSearch spatialSearch = new SpatialSearch(kdTree);
        spatialSearchQueries = new SpatialSearchQueries(spatialSearch);
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

    // Teste para o caso de uso AC4: Executar todas as 5 queries de demonstração
    @Test
    @DisplayName("AC4: Execute all 5 demo queries successfully")
    void testExecuteAllDemoQueries() {
        // Executa todas as queries de demonstração
        List<SpatialSearchQueries.QueryResult> results = spatialSearchQueries.executeAllDemoQueries();

        // Verifica que foram executadas exatamente 5 queries
        assertEquals(5, results.size(), "Should execute exactly 5 demo queries");

        // Verifica que cada query tem uma descrição e resultados válidos
        for (SpatialSearchQueries.QueryResult result : results) {
            assertNotNull(result.description, "Query should have a description");
            assertNotNull(result.stations, "Query should return a stations list");
            assertTrue(result.executionTimeNs >= 0, "Execution time should be non-negative");
        }
    }

    // Teste para a Query 1: Todas as estações em Portugal
    @Test
    @DisplayName("AC4: Query 1 - All stations in Portugal")
    void testQueryAllStationsInPortugal() {
        // Executa a query específica para Portugal
        SpatialSearchQueries.QueryResult result = spatialSearchQueries.queryAllStationsInPortugal();

        // Verifica a descrição e resultados
        assertEquals("All stations in Portugal", result.description);
        assertNotNull(result.stations, "Stations list should not be null");
        // Se há estações portuguesas, todas devem ser de Portugal
        if (!result.stations.isEmpty()) {
            assertTrue(result.stations.stream().allMatch(s -> "PT".equals(s.getCountry())));
        }
    }

    // Teste para a Query 2: Estações principais na área de Lisboa
    @Test
    @DisplayName("AC4: Query 2 - Main stations in Lisbon area")
    void testQueryMainStationsInLisbon() {
        // Executa a query para estações principais em Lisboa
        SpatialSearchQueries.QueryResult result = spatialSearchQueries.queryMainStationsInLisbon();

        // Verifica a descrição e resultados
        assertEquals("Main stations in Lisbon area", result.description);
        assertNotNull(result.stations, "Stations list should not be null");
        // Se há resultados, devem ser estações principais em Portugal
        if (!result.stations.isEmpty()) {
            assertTrue(result.stations.stream().allMatch(s ->
                    "PT".equals(s.getCountry()) && s.isMainStation()));
        }
    }

    // Teste para a Query 3: Estações de cidade na França
    @Test
    @DisplayName("AC4: Query 3 - City stations in France")
    void testQueryCityStationsInFrance() {
        // Executa a query para estações de cidade na França
        SpatialSearchQueries.QueryResult result = spatialSearchQueries.queryCityStationsInFrance();

        // Verifica a descrição e resultados
        assertEquals("City stations in France", result.description);
        assertNotNull(result.stations, "Stations list should not be null");
        // Se há resultados, devem ser estações de cidade na França
        if (!result.stations.isEmpty()) {
            assertTrue(result.stations.stream().allMatch(s ->
                    "FR".equals(s.getCountry()) && s.isCity()));
        }
    }

    // Teste para a Query 4: Estações não-principais na Itália
    @Test
    @DisplayName("AC4: Query 4 - Non-main stations in Italy")
    void testQueryNonMainStationsInItaly() {
        // Executa a query para estações não-principais na Itália
        SpatialSearchQueries.QueryResult result = spatialSearchQueries.queryNonMainStationsInItaly();

        // Verifica a descrição e resultados
        assertEquals("Non-main stations in Italy", result.description);
        assertNotNull(result.stations, "Stations list should not be null");
        // Se há resultados, devem ser estações não-principais na Itália
        if (!result.stations.isEmpty()) {
            assertTrue(result.stations.stream().allMatch(s ->
                    "IT".equals(s.getCountry()) && !s.isMainStation()));
        }
    }

    // Teste para a Query 5: Todas as estações na área de Madrid
    @Test
    @DisplayName("AC4: Query 5 - All stations in Madrid area")
    void testQueryStationsInMadrid() {
        // Executa a query para estações na área de Madrid
        SpatialSearchQueries.QueryResult result = spatialSearchQueries.queryStationsInMadrid();

        // Verifica a descrição e resultados
        assertEquals("All stations in Madrid area", result.description);
        assertNotNull(result.stations, "Stations list should not be null");
        // Se há resultados, devem ser na Espanha (área de Madrid)
        if (!result.stations.isEmpty()) {
            assertTrue(result.stations.stream().allMatch(s -> "ES".equals(s.getCountry())));
        }
    }

    // Teste para geração de relatório de desempenho
    @Test
    @DisplayName("Generate performance report")
    void testGeneratePerformanceReport() {
        // Gera o relatório de desempenho
        String report = spatialSearchQueries.generatePerformanceReport();

        // Verifica que o relatório não é nulo e contém cabeçalhos relevantes
        assertNotNull(report, "Performance report should not be null");
        assertTrue(report.contains("USEI08 SPATIAL SEARCH") || report.contains("DEMO QUERIES"),
                "Report should contain relevant headers");
    }

    // Teste para obter amostras de queries
    @Test
    @DisplayName("Get query samples")
    void testGetQuerySamples() {
        // Obtém amostras de queries
        String samples = spatialSearchQueries.getQuerySamples();

        // Verifica que as amostras não são nulas e contêm informações de estações
        assertNotNull(samples, "Query samples should not be null");
        assertTrue(samples.contains("SAMPLE STATIONS") || samples.contains("stations"),
                "Samples should contain station information");
    }

    // Teste para tempo de execução da query em milissegundos
    @Test
    @DisplayName("Query execution time in milliseconds")
    void testQueryExecutionTimeMs() {
        // Executa uma query e obtém o tempo de execução em ms
        SpatialSearchQueries.QueryResult result = spatialSearchQueries.queryAllStationsInPortugal();
        double executionTimeMs = result.getExecutionTimeMs();

        // Verifica que o tempo de execução é não-negativo
        assertTrue(executionTimeMs >= 0, "Execution time should be non-negative");
    }

    // Teste para execução de query personalizada
    @Test
    @DisplayName("Custom query execution")
    void testCustomQueryExecution() {
        // Executa uma query personalizada com parâmetros específicos
        SpatialSearchQueries.QueryResult result = spatialSearchQueries.executeCustomQuery(
                "Custom Test Query", 40.0, 50.0, -5.0, 5.0, "FR", true, true);

        // Verifica a descrição e resultados
        assertEquals("Custom Test Query", result.description);
        assertNotNull(result.stations);
    }

    // Teste para criação e métodos da classe QueryResult
    @Test
    @DisplayName("QueryResult creation and methods")
    void testQueryResultCreation() {
        // Cria uma lista vazia de estações para teste
        List<EuropeanStation> stations = new ArrayList<>();

        // Cria um objeto QueryResult de teste
        SpatialSearchQueries.QueryResult result = new SpatialSearchQueries.QueryResult(
                "Test Query", stations, 1000000L); // 1.000.000 ns = 1 ms

        // Verifica todos os atributos do QueryResult
        assertEquals("Test Query", result.description);
        assertEquals(stations, result.stations);
        assertEquals(1000000L, result.executionTimeNs);
        assertEquals(0, result.stationsFound);
        assertEquals(1.0, result.getExecutionTimeMs(), 0.001); // Verifica conversão para ms
    }

    // Teste para o método toString da classe QueryResult
    @Test
    @DisplayName("QueryResult toString method")
    void testQueryResultToString() {
        // Cria um QueryResult para teste
        List<EuropeanStation> stations = new ArrayList<>();
        SpatialSearchQueries.QueryResult result = new SpatialSearchQueries.QueryResult(
                "Test Query", stations, 1500000L); // 1.5ms

        // Obtém a representação em string
        String toString = result.toString();

        // Verifica que a string não é nula e contém informações relevantes
        assertNotNull(toString);
        assertTrue(toString.contains("Test Query"));
        assertTrue(toString.contains("0 stations") || toString.contains("1.50"));
    }
}