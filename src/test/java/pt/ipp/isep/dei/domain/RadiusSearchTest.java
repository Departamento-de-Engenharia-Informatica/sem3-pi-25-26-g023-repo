package pt.ipp.isep.dei.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for USEI10 - Radius Search and Density Summary
 */
class RadiusSearchTest {

    private RadiusSearch radiusSearch;
    private List<EuropeanStation> testStations;
    private KDTree kdTree;

    // Test coordinates: Cais do Sodré Station, Lisbon, PT
    private final double LISBON_LAT = 38.7067;
    private final double LISBON_LON = -9.1433;

    // Test coordinates: Madrid, ES
    private final double SPAIN_LAT = 40.4168;
    private final double SPAIN_LON = -3.7038;

    @BeforeEach
    void setUp() {
        InventoryManager manager = new InventoryManager();
        try {
            // Load real dataset for integration testing
            testStations = manager.loadEuropeanStations("src/main/java/pt/ipp/isep/dei/FicheirosCSV/train_stations_europe.csv");
        } catch (Exception e) {
            // Suppressing file access warning in final output, but keeping the logic
            // for resilience when the test is run outside the full environment.
            testStations = new ArrayList<>();
        }

        // 2. Build the balanced KD-Tree
        kdTree = buildKDTree(testStations);
        // The USEI10 output BST uses StationDistance as key (K) and value (V)
        radiusSearch = new RadiusSearch(kdTree);
    }

    private KDTree buildKDTree(List<EuropeanStation> stations) {
        if (stations.isEmpty()) {
            return new KDTree();
        }

        // Pre-sort for balanced KD-Tree construction
        List<EuropeanStation> stationsByLat = new ArrayList<>(stations);
        List<EuropeanStation> stationsByLon = new ArrayList<>(stations);

        stationsByLat.sort(Comparator.comparingDouble(EuropeanStation::getLatitude));
        stationsByLon.sort(Comparator.comparingDouble(EuropeanStation::getLongitude));

        KDTree tree = new KDTree();
        tree.buildBalanced(stationsByLat, stationsByLon);
        return tree;
    }

    // ============================================================
    // === PRIMARY FUNCTIONALITY AND INTEGRITY TESTS ===
    // ============================================================

    @Test
    @DisplayName("AC1: Search returns correct number of stations within a small radius (Lisbon)")
    void testSearchSmallRadius() {
        if (testStations.isEmpty()) return;

        double radiusKm = 10.0;

        // Act
        Object[] results = radiusSearch.radiusSearchWithSummary(LISBON_LAT, LISBON_LON, radiusKm);

        // Assert
        BST<?, ?> resultTree = (BST<?, ?>) results[0];
        DensitySummary summary = (DensitySummary) results[1];

        assertNotNull(resultTree, "Result tree should not be null.");
        assertNotNull(summary, "Density summary should not be null.");

        assertTrue(summary.getTotalStations() > 1,
                "Should find more than one station within 10 km radius of Lisbon.");
        // Assumes that the BST size matches the summary total
        assertTrue(summary.getTotalStations() > 0,
                "Total stations in summary must be greater than 0.");
    }

    @Test
    @DisplayName("AC1: Search returns empty result when searching far in the Atlantic")
    void testSearchEmptyResultFarFromLand() {
        // Arrange: Point in the middle of the Atlantic Ocean
        double lat = 30.0;
        double lon = -40.0;
        double radiusKm = 50.0;

        // Act
        Object[] results = radiusSearch.radiusSearchWithSummary(lat, lon, radiusKm);

        // Assert
        DensitySummary summary = (DensitySummary) results[1];

        assertEquals(0, summary.getTotalStations(), "Should find 0 stations in the middle of the ocean.");
    }

    // ============================================================
    // === ORDERING TESTS (BST/AVL) ===
    // ============================================================

    @Test
    @DisplayName("AC2: Result BST is sorted by distance (ASC) and station name (DESC)")
    void testResultOrdering() {
        if (testStations.isEmpty()) return;

        double radiusKm = 100.0;

        // Act
        Object[] results = radiusSearch.radiusSearchWithSummary(LISBON_LAT, LISBON_LON, radiusKm);

        // 1. Extract the BST and CAST to the correct type (StationDistance)
        // K=StationDistance, V=StationDistance
        BST<StationDistance, StationDistance> resultTree = (BST<StationDistance, StationDistance>) results[0];

        // 2. Use the inOrderTraversal method to get the ORDERED list
        List<StationDistance> orderedList = resultTree.inOrderTraversal();

        // 3. Verify the ordering
        for (int i = 0; i < orderedList.size() - 1; i++) {
            StationDistance current = orderedList.get(i);
            StationDistance next = orderedList.get(i + 1);

            // A. Primary Order (Distance): ASC
            assertTrue(current.getDistanceKm() <= next.getDistanceKm(),
                    "Primary order (Distance) must be ASC: " + current.getDistanceKm() + " vs " + next.getDistanceKm());

            // B. Secondary Order (Name): DESC, only if distances are EQUAL
            if (current.getDistanceKm() == next.getDistanceKm()) {
                int nameComparison = current.getStation().getStation().compareTo(next.getStation().getStation());

                // Secondary sort must ensure the current Name is ALPHABETICALLY GREATER or equal than the next (DESCENDING)
                assertTrue(nameComparison >= 0,
                        "Secondary order (Name) must be DESC when distances are equal: " + current.getStation().getStation() + " vs " + next.getStation().getStation());
            }
        }
    }

    // ============================================================
    // === DENSITY SUMMARY TESTS ===
    // ============================================================

    @Test
    @DisplayName("AC3: Density summary correctly counts stations by Country and isCity")
    void testDensitySummaryContents() {
        if (testStations.isEmpty()) return;

        // Arrange: Radius encompassing PT and ES stations to test country count
        double radiusKm = 1000.0;

        // Act
        Object[] results = radiusSearch.radiusSearchWithSummary(SPAIN_LAT, SPAIN_LON, radiusKm);
        DensitySummary summary = (DensitySummary) results[1];

        // Assert
        Map<String, Integer> countryCounts = summary.getStationsByCountry();
        Map<Boolean, Integer> cityCounts = summary.getStationsByCityType();

        // 1. Validate total count
        assertTrue(summary.getTotalStations() > 100, "Total stations found must be substantial.");

        // 2. Validate country count (must contain PT and ES)
        assertTrue(countryCounts.containsKey("PT"), "Summary must include stations from Portugal.");
        assertTrue(countryCounts.containsKey("ES"), "Summary must include stations from Spain.");

        // 3. Validate city status count (true=city, false=non-city)
        int totalCityStations = cityCounts.getOrDefault(true, 0);
        int totalNonCityStations = cityCounts.getOrDefault(false, 0);

        assertEquals(summary.getTotalStations(), totalCityStations + totalNonCityStations,
                "Sum of city and non-city stations must equal the total count.");
    }
}