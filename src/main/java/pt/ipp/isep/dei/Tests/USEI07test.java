package pt.ipp.isep.dei.Tests;

import pt.ipp.isep.dei.domain.EuropeanStation;
import pt.ipp.isep.dei.domain.StationIndexManager;

import java.util.*;

/**
 * Test harness for USEI07: Build a balanced 2D-Tree.
 * <p>
 * This class implements {@link Runnable} to execute test scenarios for the
 * 2D-Tree construction and analysis logic within {@link StationIndexManager}.
 * </p>
 * It tests:
 * <ul>
 * <li>2D-Tree build trigger (AC1, AC2)</li>
 * <li>Validation of Tree Size (AC4)</li>
 * <li>Validation of Tree Height (AC4)</li>
 * <li>Validation of Bucket Distribution for duplicate coordinates (AC3, AC4)</li>
 * </ul>
 */
public class USEI07test implements Runnable {

    /** Stores the results of each test scenario (Scenario Name -> Pass/Fail). */
    private final Map<String, Boolean> testResults = new HashMap<>();
    private StationIndexManager manager;
    private Map<String, Object> treeStats; // To store stats

    /**
     * Main entry point for the test runner.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        new USEI07test().run();
    }

    /**
     * Constructs the test class and initializes the mock station data.
     * Crucially, it runs `manager.buildIndexes()` as a prerequisite (AC2).
     */
    public USEI07test() {
        // 1. Initialize controlled data
        EuropeanStation s_A_LisbonOriente = new EuropeanStation("Lisbon Oriente", "PT", "WET", 38.7139, -9.1223, true, true, false);
        EuropeanStation s_B_LisbonSanta = new EuropeanStation("Lisbon Santa Apolonia", "PT", "WET", 38.7139, -9.1223, true, true, false); // Same coords (AC3)
        EuropeanStation s_C_Porto = new EuropeanStation("Porto Campanha", "PT", "WET", 41.1496, -8.6110, true, true, false);
        EuropeanStation s_D_Madrid = new EuropeanStation("Madrid Atocha", "ES", "CET", 40.4168, -3.7038, true, true, false);
        EuropeanStation s_E_Paris = new EuropeanStation("Paris Gare de Lyon", "FR", "CET", 48.8566, 2.3522, true, true, false);
        EuropeanStation s_F_Berlin = new EuropeanStation("Berlin Hbf", "DE", "CET", 52.5200, 13.4050, true, true, false);

        List<EuropeanStation> testStations = new ArrayList<>(List.of(
                s_A_LisbonOriente, s_B_LisbonSanta, s_C_Porto, s_D_Madrid, s_E_Paris, s_F_Berlin
        ));

        // 2. Initialize the manager and build all USEI06 indexes
        // This is a *dependency* for USEI07 (AC2)
        manager = new StationIndexManager();
        manager.buildIndexes(testStations);
    }

    /**
     * Orchestrates the execution of all test scenarios.
     * It calls each test method, stores its boolean result in the
     * {@code testResults} map, and finally calls {@link #printSummary()}
     * to display the final report.
     */
    @Override
    public void run() {
        System.out.println("======================================================");
        System.out.println("     Test Report - USEI07 2D-Tree Construction    ");
        System.out.println("======================================================");

        testResults.put("Scenario 01: Get 2D-Tree Stats (Triggers Build)", testGetStats());
        testResults.put("Scenario 02: Validate Tree Size (AC4)", testTreeSize());
        testResults.put("Scenario 03: Validate Tree Height (AC4)", testTreeHeight());
        testResults.put("Scenario 04: Validate Bucket Distribution (AC3, AC4)", testBucketDistribution());

        printSummary();
    }

    // --- Test Scenarios ---

    /**
     * Scenario 01: Tests that the 2D-Tree is built (or retrieved) when stats are requested.
     */
    private boolean testGetStats() {
        printScenarioHeader("Scenario 01: Get 2D-Tree Stats (Triggers Build)");
        // Act: This call implicitly triggers build2DTree() the first time
        treeStats = manager.get2DTreeStats();

        boolean statsOk = treeStats != null && !treeStats.isEmpty();
        printResults("Stats map was generated", statsOk ? "Correct" : "Incorrect, map is null or empty");

        boolean treeOk = manager.getStation2DTree() != null;
        printResults("2D-Tree object is no longer null", treeOk ? "Correct" : "Incorrect, tree is null");

        boolean passed = statsOk && treeOk;
        printTestStatus(passed);
        return passed;
    }

    /**
     * Scenario 02: Validates the 'size' (number of nodes) of the tree.
     */
    private boolean testTreeSize() {
        printScenarioHeader("Scenario 02: Validate Tree Size (AC4)");
        if (treeStats == null) {
            printResults("Prerequisite (Scenario 01) failed.", "SKIPPED");
            printTestStatus(false);
            return false;
        }

        // 6 stations, but 2 (Lisbon A, Lisbon B) share coordinates.
        // Expected nodes = 5 (1 node for the Lisbon bucket, 4 for the others)
        int expectedSize = 5;
        int actualSize = (int) treeStats.get("size");
        boolean passed = actualSize == expectedSize;

        printResults("Expected 5 nodes (6 stations, 2 at same coords)", passed ? "Correct" : "Incorrect: " + actualSize);
        printTestStatus(passed);
        return passed;
    }

    /**
     * Scenario 03: Validates the 'height' of the balanced tree.
     */
    private boolean testTreeHeight() {
        printScenarioHeader("Scenario 03: Validate Tree Height (AC4)");
        if (treeStats == null) {
            printResults("Prerequisite (Scenario 01) failed.", "SKIPPED");
            printTestStatus(false);
            return false;
        }

        // A balanced tree of 5 nodes should have a height of 2.
        int expectedHeight = 2;
        int actualHeight = (int) treeStats.get("height");
        boolean passed = actualHeight == expectedHeight;

        printResults("Expected height of 2 for a balanced 5-node tree", passed ? "Correct" : "Incorrect: " + actualHeight);
        printTestStatus(passed);
        return passed;
    }

    /**
     * Scenario 04: Validates the bucket distribution (AC3 and AC4).
     */
    private boolean testBucketDistribution() {
        printScenarioHeader("Scenario 04: Validate Bucket Distribution (AC3, AC4)");
        if (treeStats == null) {
            printResults("Prerequisite (Scenario 01) failed.", "SKIPPED");
            printTestStatus(false);
            return false;
        }

        @SuppressWarnings("unchecked")
        Map<Integer, Integer> buckets = (Map<Integer, Integer>) treeStats.get("bucketSizes");

        // We expect:
        // - 4 nodes with 1 station (Porto, Madrid, Paris, Berlin)
        // - 1 node with 2 stations (Lisbon Oriente, Lisbon Santa Apolonia)
        int nodesWith1 = buckets.getOrDefault(1, 0);
        int nodesWith2 = buckets.getOrDefault(2, 0);
        int nodesWith3 = buckets.getOrDefault(3, 0);

        boolean passedNodes1 = nodesWith1 == 4;
        printResults("Found 4 nodes with 1 station", passedNodes1 ? "Correct" : "Incorrect: " + nodesWith1);

        boolean passedNodes2 = nodesWith2 == 1;
        printResults("Found 1 node with 2 stations (AC3)", passedNodes2 ? "Correct" : "Incorrect: " + nodesWith2);

        boolean passedNodes3 = nodesWith3 == 0;
        printResults("Found 0 nodes with 3+ stations", passedNodes3 ? "Correct" : "Incorrect: " + nodesWith3);

        boolean passed = passedNodes1 && passedNodes2 && passedNodes3;
        printTestStatus(passed);
        return passed;
    }

    // --- Helper Methods (Copied from USEI03test) ---

    private void printScenarioHeader(String title) {
        System.out.println("\n------------------------------------------------------");
        System.out.println("  " + title);
        System.out.println("------------------------------------------------------");
    }

    private void printResults(String description, Object result) {
        System.out.printf("    - %s: %s%n", description, result.toString());
    }

    private void printTestStatus(boolean passed) {
        if (passed) {
            System.out.println("\n  --> Scenario Result: ✅ PASSED");
        } else {
            System.err.println("\n  --> Scenario Result: ❌ FAILED");
        }
    }

    private void printSummary() {
        System.out.println("\n======================================================");
        System.out.println("             USEI07 Test Report Summary      ");
        System.out.println("======================================================");
        int passCount = 0;
        int failCount = 0;
        List<String> sortedTestNames = new ArrayList<>(testResults.keySet());
        Collections.sort(sortedTestNames);

        for (String testName : sortedTestNames) {
            Boolean resultValue = testResults.get(testName);
            boolean passed = resultValue != null && resultValue;
            String result = passed ? "✅ PASSED" : "❌ FAILED";
            System.out.printf("  %s: %s%n", testName, result);
            if (passed) {
                passCount++;
            } else {
                failCount++;
            }
        }
        System.out.println("------------------------------------------------------");
        System.out.printf("  Total: %d Passed, %d Failed%n", passCount, failCount);
        System.out.println("======================================================");
        System.out.println("             End of USEI07 Test Report        ");
        System.out.println("======================================================");
    }
}