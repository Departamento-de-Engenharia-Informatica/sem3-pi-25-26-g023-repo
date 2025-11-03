package pt.ipp.isep.dei.Tests;

import pt.ipp.isep.dei.domain.EuropeanStation;
import pt.ipp.isep.dei.domain.StationIndexManager;

import java.util.*;

/**
 * Test harness for USEI06: Time-Zone Index and Windowed Queries.
 * <p>
 * This class implements {@link Runnable} to execute test scenarios for the
 * {@link StationIndexManager} BST functionalities.
 * </p>
 * It tests:
 * <ul>
 * <li>BSTs creation (AC1)</li>
 * <li>Query by Time Zone Group (AC3)</li>
 * <li>Query by Time Zone Window (AC3)</li>
 * <li>Sorting by Country and Name (AC2)</li>
 * <li>Handling of non-existent queries.</li>
 * </ul>
 */
public class USEI06test implements Runnable {

    /** Stores the results of each test scenario (Scenario Name -> Pass/Fail). */
    private final Map<String, Boolean> testResults = new HashMap<>();
    private StationIndexManager manager;

    // Test Data
    private EuropeanStation s_A_LisbonOriente;
    private EuropeanStation s_B_LisbonSanta;
    private EuropeanStation s_C_Porto;
    private EuropeanStation s_D_Madrid;
    private EuropeanStation s_E_Paris;
    private EuropeanStation s_F_Berlin;

    /**
     * Main entry point for the test runner.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        new USEI06test().run();
    }

    /**
     * Constructs the test class and initializes the mock station data
     * and the StationIndexManager.
     */
    public USEI06test() {
        // 1. Initialize controlled data
        s_A_LisbonOriente = new EuropeanStation("Lisbon Oriente", "PT", "WET", 38.7139, -9.1223, true, true, false);
        s_B_LisbonSanta = new EuropeanStation("Lisbon Santa Apolonia", "PT", "WET", 38.7139, -9.1223, true, true, false); // Same coords (AC2)
        s_C_Porto = new EuropeanStation("Porto Campanha", "PT", "WET", 41.1496, -8.6110, true, true, false);
        s_D_Madrid = new EuropeanStation("Madrid Atocha", "ES", "CET", 40.4168, -3.7038, true, true, false);
        s_E_Paris = new EuropeanStation("Paris Gare de Lyon", "FR", "CET", 48.8566, 2.3522, true, true, false);
        s_F_Berlin = new EuropeanStation("Berlin Hbf", "DE", "CET", 52.5200, 13.4050, true, true, false); // Same TZG (AC2)

        List<EuropeanStation> testStations = new ArrayList<>(List.of(
                s_A_LisbonOriente, s_B_LisbonSanta, s_C_Porto, s_D_Madrid, s_E_Paris, s_F_Berlin
        ));

        // 2. Initialize the manager and build all indexes
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
        System.out.println("     Test Report - USEI06 Station Index (BST)     ");
        System.out.println("======================================================");

        testResults.put("Scenario 01: BSTs Build Successfully (AC1)", testBuildIndexes());
        testResults.put("Scenario 02: Query by TZG (CET) - Sort by Country (AC2)", testQueryByTZG_CET());
        testResults.put("Scenario 03: Query by TZG (WET) - Sort by Name (AC2)", testQueryByTZG_WET());
        testResults.put("Scenario 04: Query by Time Zone Window (AC3)", testQueryByWindow());
        testResults.put("Scenario 05: Query for Non-Existent TZG", testQueryNotFound());
        testResults.put("Scenario 06: Query for Empty Window", testWindowQueryEmpty());

        printSummary();
    }

    // --- Test Scenarios ---

    /**
     * Scenario 01: Tests that all BSTs were created and contain the correct number of items.
     */
    private boolean testBuildIndexes() {
        printScenarioHeader("Scenario 01: BSTs Build Successfully (AC1)");
        boolean bstLatOk = !manager.getBstLatitude().isEmpty() &&
                manager.getBstLatitude().inOrderTraversal().size() == 6;
        printResults("Latitude BST built with 6 stations", bstLatOk ? "Correct" : "Incorrect");

        boolean bstLonOk = !manager.getBstLongitude().isEmpty() &&
                manager.getBstLongitude().inOrderTraversal().size() == 6;
        printResults("Longitude BST built with 6 stations", bstLonOk ? "Correct" : "Incorrect");

        boolean bstTzgOk = !manager.getBstTimeZoneGroup().isEmpty() &&
                manager.getBstTimeZoneGroup().inOrderTraversal().size() == 6;
        printResults("Time Zone Group BST built with 6 stations", bstTzgOk ? "Correct" : "Incorrect");

        boolean passed = bstLatOk && bstLonOk && bstTzgOk;
        printTestStatus(passed);
        return passed;
    }

    /**
     * Scenario 02: Tests query by a single TZG and validates sorting by Country (AC2).
     */
    private boolean testQueryByTZG_CET() {
        printScenarioHeader("Scenario 02: Query by TZG (CET) - Sort by Country (AC2)");
        List<EuropeanStation> results = manager.getStationsByTimeZoneGroup("CET");

        boolean sizeOk = results.size() == 3;
        printResults("Found 3 stations for CET", sizeOk ? "Correct" : "Incorrect: " + results.size());

        // Expected order: DE (Berlin), ES (Madrid), FR (Paris)
        boolean orderOk = sizeOk &&
                results.get(0).equals(s_F_Berlin) && // Berlin (DE)
                results.get(1).equals(s_D_Madrid) && // Madrid (ES)
                results.get(2).equals(s_E_Paris);    // Paris (FR)
        printResults("Results sorted by Country (DE, ES, FR)", orderOk ? "Correct" : "Incorrect");

        printTestStatus(sizeOk && orderOk);
        return sizeOk && orderOk;
    }

    /**
     * Scenario 03: Tests query by TZG and validates fallback sorting by Name (AC2).
     */
    private boolean testQueryByTZG_WET() {
        printScenarioHeader("Scenario 03: Query by TZG (WET) - Sort by Name (AC2)");
        List<EuropeanStation> results = manager.getStationsByTimeZoneGroup("WET");

        boolean sizeOk = results.size() == 3;
        printResults("Found 3 stations for WET", sizeOk ? "Correct" : "Incorrect: " + results.size());

        // All are 'PT', so sorting falls back to name (as per EuropeanStation.compareTo)
        // Expected order: "Lisbon Oriente", "Lisbon Santa Apolonia", "Porto Campanha"
        boolean orderOk = sizeOk &&
                results.get(0).equals(s_A_LisbonOriente) &&
                results.get(1).equals(s_B_LisbonSanta) &&
                results.get(2).equals(s_C_Porto);
        printResults("Results sorted by Station Name", orderOk ? "Correct" : "Incorrect");

        printTestStatus(sizeOk && orderOk);
        return sizeOk && orderOk;
    }

    /**
     * Scenario 04: Tests query by a window of time zones (AC3).
     */
    private boolean testQueryByWindow() {
        printScenarioHeader("Scenario 04: Query by Time Zone Window (AC3)");
        List<EuropeanStation> results = manager.getStationsInTimeZoneWindow("CAT", "EET"); // Includes CET

        boolean sizeOk = results.size() == 3;
        printResults("Found 3 stations in window [CAT, EET]", sizeOk ? "Correct" : "Incorrect: " + results.size());

        boolean contentOk = sizeOk &&
                results.contains(s_D_Madrid) &&
                results.contains(s_E_Paris) &&
                results.contains(s_F_Berlin);
        printResults("Window contains Madrid, Paris, and Berlin", contentOk ? "Correct" : "Incorrect");

        printTestStatus(sizeOk && contentOk);
        return sizeOk && contentOk;
    }

    /**
     * Scenario 05: Tests query for a TZG that does not exist.
     */
    private boolean testQueryNotFound() {
        printScenarioHeader("Scenario 05: Query for Non-Existent TZG");
        List<EuropeanStation> results = manager.getStationsByTimeZoneGroup("XYZ");

        boolean passed = results.isEmpty();
        printResults("Result list should be empty", passed ? "Correct" : "Incorrect");
        printTestStatus(passed);
        return passed;
    }

    /**
     * Scenario 06: Tests a window query that returns no results.
     */
    private boolean testWindowQueryEmpty() {
        printScenarioHeader("Scenario 06: Query for Empty Window");
        List<EuropeanStation> results = manager.getStationsInTimeZoneWindow("AAA", "AAB");

        boolean passed = results.isEmpty();
        printResults("Result list should be empty", passed ? "Correct" : "Incorrect");
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
        System.out.println("             USEI06 Test Report Summary      ");
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
        System.out.println("             End of USEI06 Test Report        ");
        System.out.println("======================================================");
    }
}