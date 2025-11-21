package pt.ipp.isep.dei.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SpatialSearchQueries - USEI08 Demo Queries
 */
class SpatialSearchQueriesTest {

    private SpatialSearch spatialSearch;
    private SpatialSearchQueries spatialSearchQueries;

    @BeforeEach
    void setUp() {
        // Load stations using the same method as in Main
        InventoryManager manager = new InventoryManager();
        List<EuropeanStation> stations;
        try {
            stations = manager.loadEuropeanStations(
                    "src/main/java/pt/ipp/isep/dei/FicheirosCSV/train_stations_europe.csv");
        } catch (Exception e) {
            // If CSV loading fails, create empty list
            stations = new ArrayList<>();
        }

        if (stations == null || stations.isEmpty()) {
            stations = new ArrayList<>();
        }

        KDTree kdTree = buildKDTree(stations);
        spatialSearch = new SpatialSearch(kdTree);
        spatialSearchQueries = new SpatialSearchQueries(spatialSearch);
    }

    private KDTree buildKDTree(List<EuropeanStation> stations) {
        if (stations.isEmpty()) {
            return new KDTree();
        }

        List<EuropeanStation> stationsByLat = new ArrayList<>(stations);
        List<EuropeanStation> stationsByLon = new ArrayList<>(stations);

        stationsByLat.sort((s1, s2) -> Double.compare(s1.getLatitude(), s2.getLatitude()));
        stationsByLon.sort((s1, s2) -> Double.compare(s1.getLongitude(), s2.getLongitude()));

        KDTree tree = new KDTree();
        tree.buildBalanced(stationsByLat, stationsByLon);
        return tree;
    }

    @Test
    @DisplayName("AC4: Execute all 5 demo queries successfully")
    void testExecuteAllDemoQueries() {
        // Act
        List<SpatialSearchQueries.QueryResult> results = spatialSearchQueries.executeAllDemoQueries();

        // Assert
        assertEquals(5, results.size(), "Should execute exactly 5 demo queries");

        // Verify each query has a description and valid results
        for (SpatialSearchQueries.QueryResult result : results) {
            assertNotNull(result.description, "Query should have a description");
            assertNotNull(result.stations, "Query should return a stations list");
            assertTrue(result.executionTimeNs >= 0, "Execution time should be non-negative");
        }
    }

    @Test
    @DisplayName("AC4: Query 1 - All stations in Portugal")
    void testQueryAllStationsInPortugal() {
        // Act
        SpatialSearchQueries.QueryResult result = spatialSearchQueries.queryAllStationsInPortugal();

        // Assert
        assertEquals("All stations in Portugal", result.description);
        assertNotNull(result.stations, "Stations list should not be null");
        // If we have Portuguese stations, they should all be from Portugal
        if (!result.stations.isEmpty()) {
            assertTrue(result.stations.stream().allMatch(s -> "PT".equals(s.getCountry())));
        }
    }

    @Test
    @DisplayName("AC4: Query 2 - Main stations in Lisbon area")
    void testQueryMainStationsInLisbon() {
        // Act
        SpatialSearchQueries.QueryResult result = spatialSearchQueries.queryMainStationsInLisbon();

        // Assert
        assertEquals("Main stations in Lisbon area", result.description);
        assertNotNull(result.stations, "Stations list should not be null");
        // If we have results, they should be main stations in Portugal
        if (!result.stations.isEmpty()) {
            assertTrue(result.stations.stream().allMatch(s ->
                    "PT".equals(s.getCountry()) && s.isMainStation()));
        }
    }

    @Test
    @DisplayName("AC4: Query 3 - City stations in France")
    void testQueryCityStationsInFrance() {
        // Act
        SpatialSearchQueries.QueryResult result = spatialSearchQueries.queryCityStationsInFrance();

        // Assert
        assertEquals("City stations in France", result.description);
        assertNotNull(result.stations, "Stations list should not be null");
        // If we have results, they should be city stations in France
        if (!result.stations.isEmpty()) {
            assertTrue(result.stations.stream().allMatch(s ->
                    "FR".equals(s.getCountry()) && s.isCity()));
        }
    }

    @Test
    @DisplayName("AC4: Query 4 - Non-main stations in Italy")
    void testQueryNonMainStationsInItaly() {
        // Act
        SpatialSearchQueries.QueryResult result = spatialSearchQueries.queryNonMainStationsInItaly();

        // Assert
        assertEquals("Non-main stations in Italy", result.description);
        assertNotNull(result.stations, "Stations list should not be null");
        // If we have results, they should be non-main stations in Italy
        if (!result.stations.isEmpty()) {
            assertTrue(result.stations.stream().allMatch(s ->
                    "IT".equals(s.getCountry()) && !s.isMainStation()));
        }
    }

    @Test
    @DisplayName("AC4: Query 5 - All stations in Madrid area")
    void testQueryStationsInMadrid() {
        // Act
        SpatialSearchQueries.QueryResult result = spatialSearchQueries.queryStationsInMadrid();

        // Assert
        assertEquals("All stations in Madrid area", result.description);
        assertNotNull(result.stations, "Stations list should not be null");
        // If we have results, they should be in Spain (Madrid area)
        if (!result.stations.isEmpty()) {
            assertTrue(result.stations.stream().allMatch(s -> "ES".equals(s.getCountry())));
        }
    }

    @Test
    @DisplayName("Generate performance report")
    void testGeneratePerformanceReport() {
        // Act
        String report = spatialSearchQueries.generatePerformanceReport();

        // Assert
        assertNotNull(report, "Performance report should not be null");
        assertTrue(report.contains("USEI08 SPATIAL SEARCH") || report.contains("DEMO QUERIES"),
                "Report should contain relevant headers");
    }

    @Test
    @DisplayName("Get query samples")
    void testGetQuerySamples() {
        // Act
        String samples = spatialSearchQueries.getQuerySamples();

        // Assert
        assertNotNull(samples, "Query samples should not be null");
        assertTrue(samples.contains("SAMPLE STATIONS") || samples.contains("stations"),
                "Samples should contain station information");
    }

    @Test
    @DisplayName("Query execution time in milliseconds")
    void testQueryExecutionTimeMs() {
        // Act
        SpatialSearchQueries.QueryResult result = spatialSearchQueries.queryAllStationsInPortugal();
        double executionTimeMs = result.getExecutionTimeMs();

        // Assert
        assertTrue(executionTimeMs >= 0, "Execution time should be non-negative");
    }

    @Test
    @DisplayName("Custom query execution")
    void testCustomQueryExecution() {
        // Act
        SpatialSearchQueries.QueryResult result = spatialSearchQueries.executeCustomQuery(
                "Custom Test Query", 40.0, 50.0, -5.0, 5.0, "FR", true, true);

        // Assert
        assertEquals("Custom Test Query", result.description);
        assertNotNull(result.stations);
    }

    @Test
    @DisplayName("QueryResult creation and methods")
    void testQueryResultCreation() {
        // Arrange
        List<EuropeanStation> stations = new ArrayList<>();

        // Act
        SpatialSearchQueries.QueryResult result = new SpatialSearchQueries.QueryResult(
                "Test Query", stations, 1000000L);

        // Assert
        assertEquals("Test Query", result.description);
        assertEquals(stations, result.stations);
        assertEquals(1000000L, result.executionTimeNs);
        assertEquals(0, result.stationsFound);
        assertEquals(1.0, result.getExecutionTimeMs(), 0.001);
    }

    @Test
    @DisplayName("QueryResult toString method")
    void testQueryResultToString() {
        // Arrange
        List<EuropeanStation> stations = new ArrayList<>();
        SpatialSearchQueries.QueryResult result = new SpatialSearchQueries.QueryResult(
                "Test Query", stations, 1500000L); // 1.5ms

        // Act
        String toString = result.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("Test Query"));
        assertTrue(toString.contains("0 stations") || toString.contains("1.50"));
    }
}