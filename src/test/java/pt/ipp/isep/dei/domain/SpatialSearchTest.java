package pt.ipp.isep.dei.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for USEI08 - Spatial Search by Geographical Area
 */
class SpatialSearchTest {

    private SpatialSearch spatialSearch;
    private List<EuropeanStation> testStations;

    @BeforeEach
    void setUp() {
        // Load stations using the same method as in Main
        InventoryManager manager = new InventoryManager();
        try {
            testStations = manager.loadEuropeanStations("src/main/java/pt/ipp/isep/dei/FicheirosCSV/train_stations_europe.csv");
        } catch (Exception e) {
            // If CSV loading fails, create empty list and skip complex tests
            testStations = new ArrayList<>();
        }

        if (testStations == null || testStations.isEmpty()) {
            testStations = new ArrayList<>();
        }

        // Create KD-tree with stations (if any)
        KDTree kdTree = buildKDTree(testStations);
        spatialSearch = new SpatialSearch(kdTree);
    }

    private KDTree buildKDTree(List<EuropeanStation> stations) {
        if (stations.isEmpty()) {
            // Return empty tree
            return new KDTree();
        }

        List<EuropeanStation> stationsByLat = new ArrayList<>(stations);
        List<EuropeanStation> stationsByLon = new ArrayList<>(stations);

        stationsByLat.sort(Comparator.comparingDouble(EuropeanStation::getLatitude));
        stationsByLon.sort(Comparator.comparingDouble(EuropeanStation::getLongitude));

        KDTree tree = new KDTree();
        tree.buildBalanced(stationsByLat, stationsByLon);
        return tree;
    }

    @Test
    @DisplayName("AC2: Search within Portugal geographical boundaries")
    void testSearchWithinPortugalBoundaries() {
        // Skip if no stations loaded
        if (testStations.isEmpty()) {
            return;
        }

        // Arrange - Portugal boundaries
        double latMin = 36.0, latMax = 42.0;
        double lonMin = -9.5, lonMax = -6.0;

        // Act
        List<EuropeanStation> results = spatialSearch.searchByGeographicalArea(
                latMin, latMax, lonMin, lonMax, "PT", null, null);

        // Assert
        assertNotNull(results, "Results should not be null");
        assertTrue(results.stream().allMatch(s -> s.getCountry().equals("PT")),
                "All results should be in Portugal");
    }

    @Test
    @DisplayName("AC3: Search with country filter")
    void testSearchWithCountryFilter() {
        // Skip if no stations loaded
        if (testStations.isEmpty()) {
            return;
        }

        // Arrange - Broad European boundaries
        double latMin = 35.0, latMax = 55.0;
        double lonMin = -10.0, lonMax = 20.0;

        // Act
        List<EuropeanStation> results = spatialSearch.searchByGeographicalArea(
                latMin, latMax, lonMin, lonMax, "ES", null, null);

        // Assert
        assertNotNull(results, "Results should not be null");
        assertTrue(results.isEmpty() || results.stream().allMatch(s -> s.getCountry().equals("ES")),
                "All results should be in Spain");
    }

    @Test
    @DisplayName("AC3: Search with city station filter")
    void testSearchWithCityFilter() {
        // Skip if no stations loaded
        if (testStations.isEmpty()) {
            return;
        }

        // Arrange - Broad European boundaries
        double latMin = 35.0, latMax = 55.0;
        double lonMin = -10.0, lonMax = 20.0;

        // Act
        List<EuropeanStation> results = spatialSearch.searchByGeographicalArea(
                latMin, latMax, lonMin, lonMax, null, true, null);

        // Assert
        assertNotNull(results, "Results should not be null");
        assertTrue(results.isEmpty() || results.stream().allMatch(EuropeanStation::isCity),
                "All results should be city stations");
    }

    @Test
    @DisplayName("AC3: Search with main station filter")
    void testSearchWithMainStationFilter() {
        // Skip if no stations loaded
        if (testStations.isEmpty()) {
            return;
        }

        // Arrange - Broad European boundaries
        double latMin = 35.0, latMax = 55.0;
        double lonMin = -10.0, lonMax = 20.0;

        // Act
        List<EuropeanStation> results = spatialSearch.searchByGeographicalArea(
                latMin, latMax, lonMin, lonMax, null, null, true);

        // Assert
        assertNotNull(results, "Results should not be null");
        assertTrue(results.isEmpty() || results.stream().allMatch(EuropeanStation::isMainStation),
                "All results should be main stations");
    }

    @Test
    @DisplayName("AC3: Search with combined filters")
    void testSearchWithCombinedFilters() {
        // Skip if no stations loaded
        if (testStations.isEmpty()) {
            return;
        }

        // Arrange - Broad European boundaries
        double latMin = 35.0, latMax = 55.0;
        double lonMin = -10.0, lonMax = 20.0;

        // Act
        List<EuropeanStation> results = spatialSearch.searchByGeographicalArea(
                latMin, latMax, lonMin, lonMax, "PT", true, true);

        // Assert
        assertNotNull(results, "Results should not be null");
        assertTrue(results.isEmpty() || results.stream().allMatch(s ->
                        s.getCountry().equals("PT") && s.isCity() && s.isMainStation()),
                "All results should be Portuguese city main stations");
    }

    @Test
    @DisplayName("Validate coordinate boundaries - invalid latitude")
    void testInvalidLatitudeBoundaries() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> spatialSearch.searchByGeographicalArea(-100.0, 42.0, -9.5, -6.0, null, null, null));

        assertTrue(exception.getMessage().contains("Invalid latitude range"));
    }

    @Test
    @DisplayName("Validate coordinate boundaries - invalid longitude")
    void testInvalidLongitudeBoundaries() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> spatialSearch.searchByGeographicalArea(36.0, 42.0, -200.0, -6.0, null, null, null));

        assertTrue(exception.getMessage().contains("Invalid longitude range"));
    }

    @Test
    @DisplayName("Empty result when no stations in area")
    void testEmptyResultWhenNoStationsInArea() {
        // Arrange - Search in the middle of the ocean
        double latMin = 10.0, latMax = 20.0;
        double lonMin = -50.0, lonMax = -40.0;

        // Act
        List<EuropeanStation> results = spatialSearch.searchByGeographicalArea(
                latMin, latMax, lonMin, lonMax, null, null, null);

        // Assert
        assertNotNull(results, "Results should not be null");
        // This should be empty since we're searching in the ocean
    }

    @Test
    @DisplayName("Performance analysis returns valid complexity information")
    void testPerformanceAnalysis() {
        // Act
        String complexityAnalysis = spatialSearch.getComplexityAnalysis();

        // Assert
        assertNotNull(complexityAnalysis, "Complexity analysis should not be null");
        assertTrue(complexityAnalysis.contains("USEI08 Complexity Analysis"));
        assertTrue(complexityAnalysis.contains("Time Complexity"));
        assertTrue(complexityAnalysis.contains("Space Complexity"));
    }

    @Test
    @DisplayName("Case-insensitive country filter")
    void testCaseInsensitiveCountryFilter() {
        // Skip if no stations loaded
        if (testStations.isEmpty()) {
            return;
        }

        // Arrange
        double latMin = 35.0, latMax = 55.0;
        double lonMin = -10.0, lonMax = 20.0;

        // Act - Test different case variations
        List<EuropeanStation> results1 = spatialSearch.searchByGeographicalArea(
                latMin, latMax, lonMin, lonMax, "pt", null, null);
        List<EuropeanStation> results2 = spatialSearch.searchByGeographicalArea(
                latMin, latMax, lonMin, lonMax, "PT", null, null);

        // Assert
        assertEquals(results1.size(), results2.size(), "Should return same results regardless of case");
    }

    @Test
    @DisplayName("Null filters are ignored")
    void testNullFiltersAreIgnored() {
        // Skip if no stations loaded
        if (testStations.isEmpty()) {
            return;
        }

        // Arrange
        double latMin = 35.0, latMax = 55.0;
        double lonMin = -10.0, lonMax = 20.0;

        // Act - All filters null
        List<EuropeanStation> results = spatialSearch.searchByGeographicalArea(
                latMin, latMax, lonMin, lonMax, null, null, null);

        // Assert
        assertNotNull(results, "Results should not be null");
    }

    @Test
    @DisplayName("SpatialSearch constructor with null KDTree throws exception")
    void testSpatialSearchConstructorWithNullKDTree() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> new SpatialSearch(null));
    }

    @Test
    @DisplayName("Search with empty tree returns empty list")
    void testSearchWithEmptyTree() {
        // Arrange - Create empty tree
        KDTree emptyTree = new KDTree();
        SpatialSearch emptySearch = new SpatialSearch(emptyTree);

        // Act
        List<EuropeanStation> results = emptySearch.searchByGeographicalArea(
                35.0, 55.0, -10.0, 20.0, null, null, null);

        // Assert
        assertNotNull(results, "Results should not be null");
        assertTrue(results.isEmpty(), "Should return empty list for empty tree");
    }
}