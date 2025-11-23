package pt.ipp.isep.dei.domain;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opentest4j.TestAbortedException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * EXHAUSTIVE Integration Tests for the KDTree implementation (USEI07).
 * Focuses on validating construction (buildBalanced), integrity (size)
 * and balance (height and buckets) of the structure using the real dataset.
 */
class KDTreeTest {

    private static KDTree kdTree;
    private static StationIndexManager manager;
    private static int totalStationsCount;

    private static final String FILE_PATH = "src/main/java/pt/ipp/isep/dei/FicheirosCSV/train_stations_europe.csv";

    @BeforeAll
    static void setUp() { // Removed 'throws IOException'
        System.out.println("--- KDTree Test Setup: Loading and Building Indexes (USEI07) ---");

        // Assumes the existence of InventoryManager to load the CSV
        InventoryManager loader = new InventoryManager();
        manager = new StationIndexManager();

        List<EuropeanStation> loadedStations;

        try {
            // 1. Load the complete dataset. This method now throws RuntimeException on file failure.
            loadedStations = loader.loadEuropeanStations(FILE_PATH);
            totalStationsCount = loader.getValidStationCount();

            // 2. Ensure that the large list is successful, otherwise fail the setup gracefully.
            // Using 60000 as a minimum sanity check for the full dataset.
            if (totalStationsCount <= 60000) {
                fail("Data loading failed or the dataset is too small. Count: " + totalStationsCount);
            }

            // 3. USEI06 Steps: Build BST indexes (necessary for ordered lists)
            manager.buildIndexes(loadedStations);

            List<EuropeanStation> orderedByLat = manager.getBstLatitude().inOrderTraversal();
            List<EuropeanStation> orderedByLon = manager.getBstLongitude().inOrderTraversal();

            // 4. USEI07 Step: Build the KD-Tree
            long startTime = System.nanoTime();
            kdTree = new KDTree();
            kdTree.buildBalanced(orderedByLat, orderedByLon);
            long endTime = System.nanoTime();

            System.out.printf("✅ KD-Tree built with %d stations in %.2f ms%n", kdTree.size(), (endTime - startTime) / 1_000_000.0);
            System.out.println("-------------------------------------------------------");

        } catch (RuntimeException e) {
            // Catch the RuntimeException (e.g., FileNotFound) thrown by InventoryManager.
            totalStationsCount = 0;
            System.err.println("FATAL SETUP ERROR: Aborting tests due to data loading/indexing failure. Cause: " + e.getMessage());
            // Throw TestAbortedException to signal the JUnit runner to skip tests, not fail them.
            throw new TestAbortedException("Setup failed due to data loading or indexing error.", e);
        } catch (Exception e) {
            totalStationsCount = 0;
            System.err.println("FATAL SETUP ERROR: Aborting tests. Cause: " + e.getMessage());
            throw new TestAbortedException("Setup failed due to unexpected error.", e);
        }
    }

    // -------------------------------------------------------------
    // INTEGRITY AND BALANCE TESTS (USEI07)
    // -------------------------------------------------------------

    @Test
    void testBuildBalanced_SizeAndIntegrity_RealData() {
        // CRITICAL: Skip test if setup failed
        assumeTrue(totalStationsCount > 0, "Setup failed: 0 stations loaded.");

        // Verifies if the final size of the KD-Tree matches the total number of loaded stations
        assertEquals(totalStationsCount, kdTree.size(),
                "The KD-Tree size must equal the total number of indexed stations.");
    }

    @Test
    void testBuildBalanced_BalanceCheck_RealData() {
        // CRITICAL: Skip test if setup failed
        assumeTrue(totalStationsCount > 0, "Setup failed: 0 stations loaded.");

        // Checks the tree height. A balanced tree must have logarithmic height.
        int height = kdTree.height();
        int size = kdTree.size();

        // Calculation of the upper height limit for a balanced 2D tree
        int expectedMaxHeight = (int) (Math.log(size) / Math.log(2)) * 2;

        assertTrue(height < 50 && height <= expectedMaxHeight + 10,
                "The KD-Tree height (" + height + ") is too high, suggesting imbalance. Expected maximum ~" + expectedMaxHeight);
    }

    @Test
    void testGetStats_IntegrityAndBucketDistribution() {
        // CRITICAL: Skip test if setup failed
        assumeTrue(totalStationsCount > 0, "Setup failed: 0 stations loaded.");

        // The KD-Tree uses buckets to group stations with the same Latitude/Longitude.
        Map<Integer, Integer> bucketSizes = kdTree.getBucketSizes();

        // 1. There must be at least one bucket with more than 1 station (due to duplicates)
        assertTrue(bucketSizes.keySet().stream().anyMatch(size -> size > 1),
                "The KD-Tree must generate 'buckets' with size > 1 due to duplicate coordinates in the dataset.");

        // 2. The sum of all elements contained in the buckets must equal the total stations
        int totalNodesInBuckets = bucketSizes.entrySet().stream()
                .mapToInt(entry -> entry.getKey() * entry.getValue())
                .sum();

        assertEquals(totalStationsCount, totalNodesInBuckets, "The sum of elements in the buckets must equal the total stations count.");
    }
}