package pt.ipp.isep.dei.domain;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EXHAUSTIVE Unit tests for USEI06 (BST/AVL Indexing)
 * and USEI07 (KD-Tree Construction) functionalities using the real 'train_stations_europe.csv' dataset.
 *
 * Objective: Full coverage of public APIs, including tiebreaker logic and balancing requirements.
 */
class StationIndexManagerTest {

    // Path to the real data file (project standard)
    private static final String FILE_PATH = "src/main/java/pt/ipp/isep/dei/FicheirosCSV/train_stations_europe.csv";

    // Domain services and static data
    private static StationIndexManager manager;
    private static InventoryManager loader;
    private static int totalStationsCount;

    // Known coordinates for tests (expecting duplicates/complexity)
    private static final double LISBON_APOLONIA_LAT = 38.71387;
    private static final double EXTREME_LAT_MAX = 90.0;
    private static final double EXTREME_LON_MIN = -180.0;

    @BeforeAll
    static void setUp() {
        loader = new InventoryManager();
        manager = new StationIndexManager();

        try {
            // 1. Load the real dataset
            List<EuropeanStation> loadedStations = loader.loadEuropeanStations(FILE_PATH);
            totalStationsCount = loader.getValidStationCount();

            // 2. Build the BST/AVL indexes (USEI06)
            manager.buildIndexes(loadedStations);

            // 3. Ensure the KD-Tree is built before running the tests (USEI07)
            manager.build2DTree();

            // Sanity Assertion
            assertTrue(totalStationsCount > 60000,
                    "The dataset loading must have more than 60,000 valid stations.");

        } catch (Exception e) {
            fail("Catastrophic failure loading and indexing the real dataset: " + e.getMessage());
        }
    }

    // =============================================================
    // 🧪 INTEGRITY TESTS (USEI06 & USEI07)
    // =============================================================

    @Test
    void testIntegrity_TotalCount_AllStructures() {
        // Ensures that the number of indexed elements (values) is consistent across all structures.

        // BSTs (inOrderTraversal counts all values, correct for duplicate keys)
        assertEquals(totalStationsCount, manager.getBstLatitude().inOrderTraversal().size(),
                "BST Latitude must index the total count of stations.");
        assertEquals(totalStationsCount, manager.getBstLongitude().inOrderTraversal().size(),
                "BST Longitude must index the total count of stations.");

        // KD-Tree (USEI07)
        assertEquals(totalStationsCount, manager.getStation2DTree().size(),
                "KD-Tree must index the total count of stations.");
    }

    @Test
    void testBuildIndexes_BST_EmptyTreeCreation() {
        // Verifies the size of a newly created BST (without using setUp)
        BST<Double, EuropeanStation> emptyBST = new BST<>();
        assertEquals(0, emptyBST.inOrderTraversal().size(), "The size of an empty BST must be 0.");
        assertTrue(emptyBST.inOrderTraversal().isEmpty());
    }


    // =============================================================
    // 🧪 USEI06 TESTS (Key Queries and Ordering)
    // =============================================================

    @Test
    void testBuildIndexes_DuplicateKeys_TiebreakerOrder() {
        // Requirement: Verifies the tiebreaker (Name ASC) for duplicate keys (Latitude).
        List<EuropeanStation> result = manager.getBstLatitude().findAll(LISBON_APOLONIA_LAT);

        assertTrue(result.size() > 1,
                "Exact Lat query must return multiple stations, confirming the tiebreaker is needed.");

        // Verifies ordering (Name ASC)
        String firstName = result.get(0).getStation();
        String secondName = result.get(1).getStation();

        assertTrue(firstName.compareTo(secondName) <= 0,
                "Stations with the same Latitude must be sorted alphabetically by name (tiebreaker).");
    }

    @Test
    void testGetStationsByTimeZoneGroup_NonExistentKey() {
        // Test: A non-existent key should return an empty list.
        List<EuropeanStation> result = manager.getStationsByTimeZoneGroup("NON_EXISTENT_TZG");
        assertTrue(result.isEmpty(), "Non-existent TZG must return an empty list.");
    }

    @Test
    void testGetStationsByTimeZoneGroup_WETGMT_OrderingExhaustive() {
        // Verifies the final ordering (Country ASC, Name ASC).
        final String TZG = "WET/GMT";
        List<EuropeanStation> wetStations = manager.getStationsByTimeZoneGroup(TZG);

        // Verifies the final ordering
        EuropeanStation prev = null;
        for (EuropeanStation current : wetStations) {
            if (prev != null) {
                int countryComparison = prev.getCountry().compareTo(current.getCountry());

                // Validates the TZG
                assertEquals(TZG, current.getTimeZoneGroup());

                // Validates the ordering
                if (countryComparison == 0) {
                    assertTrue(prev.getStation().compareTo(current.getStation()) <= 0,
                            "Ordering: Name must be ASC when Country is equal.");
                } else {
                    assertTrue(countryComparison < 0,
                            "Ordering: Country must be ASC.");
                }
            }
            prev = current;
        }
    }

    @Test
    void testGetStationsInTimeZoneWindow_RangeQuery_Extremes() {
        // Tests the widest possible TZG range.
        final String TZG_MIN = "A"; // Alphabetical minimum
        final String TZG_MAX = "Z"; // Alphabetical maximum

        List<EuropeanStation> windowStations = manager.getStationsInTimeZoneWindow(TZG_MIN, TZG_MAX);

        // Should return all loaded stations
        assertEquals(totalStationsCount, windowStations.size(), "The widest TZG range must return all stations.");

        // Verifies the TZG ordering (ASC)
        EuropeanStation prev = null;
        for (EuropeanStation current : windowStations) {
            if (prev != null) {
                // The primary ordering is by TimeZoneGroup
                assertTrue(prev.getTimeZoneGroup().compareTo(current.getTimeZoneGroup()) <= 0,
                        "Ordering: TimeZoneGroup must be ASC.");
            }
            prev = current;
        }
    }

    // --- Tests for Coordinate Range Queries (findInRange) ---

    @Test
    void testBST_LongitudeRangeQuery_ExtremeBoundaries() {
        // Tests the widest geographical range for Longitude
        final double MIN_LON = EXTREME_LON_MIN; // -180.0
        final double MAX_LON = 180.0;

        List<EuropeanStation> result = manager.getBstLongitude().findInRange(MIN_LON, MAX_LON);

        // Must return all stations (assuming all Lat/Lon are valid)
        assertEquals(totalStationsCount, result.size(), "Extreme Longitude range must return all stations.");

        // Verifies the ordering (Longitude ASC)
        double prevLon = -180.1;
        for (EuropeanStation s : result) {
            assertTrue(s.getLongitude() >= prevLon, "The list must be sorted by Longitude.");
            prevLon = s.getLongitude();
        }
    }

    @Test
    void testBST_RangeQuery_SinglePointCase() {
        // Tests if findInRange(K, K) works like findAll(K) and maintains tiebreaker order.
        List<EuropeanStation> result = manager.getBstLatitude().findInRange(LISBON_APOLONIA_LAT, LISBON_APOLONIA_LAT);

        assertTrue(result.size() > 1, "Single point range must return duplicates.");

        // Verifies that the ordering is by name (tiebreaker)
        EuropeanStation prev = null;
        for (EuropeanStation current : result) {
            if (prev != null) {
                assertTrue(prev.getStation().compareTo(current.getStation()) <= 0,
                        "Single point range must maintain the tiebreaker ordering (Name ASC).");
            }
            prev = current;
        }
    }

    @Test
    void testEdgeCase_InvertedRangeReturnsEmpty() {
        // Tests a range where MAX < MIN (must return an empty list).
        List<EuropeanStation> latResult = manager.getBstLatitude().findInRange(50.0, 40.0);
        assertTrue(latResult.isEmpty(), "A range where MAX < MIN must return an empty list (Latitude).");

        List<EuropeanStation> tzgResult = manager.getBstTimeZoneGroup().findInRange("Z", "A");
        assertTrue(tzgResult.isEmpty(), "A TZG range where MAX < MIN must return an empty list.");
    }

    // =============================================================
    // 🧪 USEI07 TESTS (KD-Tree Construction & Stats)
    // =============================================================

    @Test
    void testKDTree_BalanceCheck_USEI07() {
        // Non-functional requirement: The KD-Tree must be balanced (O(N log N)).
        Map<String, Object> stats = manager.get2DTreeStats();
        int height = (int) stats.get("height");
        int size = (int) stats.get("size");

        // Sanity limit for balanced height (theoretical maximum is ~2 * log2(N))
        assertTrue(height < 50,
                "The KD-Tree height (" + height + ") is too high, suggesting imbalance.");
    }

    @Test
    void testKDTree_StatsReporting_USEI07() {
        // Requirement: Verifies that all statistics are reported with the correct types.
        Map<String, Object> stats = manager.get2DTreeStats();

        assertTrue(stats.containsKey("size"), "Missing 'size' statistic.");
        assertTrue(stats.containsKey("height"), "Missing 'height' statistic.");
        assertTrue(stats.containsKey("bucketSizes"), "Missing 'bucketSizes' statistic.");

        assertInstanceOf(Integer.class, stats.get("size"));
        assertInstanceOf(Integer.class, stats.get("height"));
        assertInstanceOf(Map.class, stats.get("bucketSizes"));
    }

    @Test
    void testKDTree_BucketDistributionCheck_USEI07() {
        // Requirement: Verifies that the tiebreaker mechanism (multiple values per node) worked.
        Map<String, Object> stats = manager.get2DTreeStats();
        @SuppressWarnings("unchecked")
        Map<Integer, Integer> bucketSizes = (Map<Integer, Integer>) stats.get("bucketSizes");

        // Must have nodes with 1 station and nodes with > 1 station.
        assertTrue(bucketSizes.containsKey(1), "Must have leaf nodes with 1 station.");
        assertTrue(bucketSizes.keySet().stream().anyMatch(size -> size > 1),
                "Must have leaf nodes containing more than 1 station (coordinate duplicates).");
    }
}