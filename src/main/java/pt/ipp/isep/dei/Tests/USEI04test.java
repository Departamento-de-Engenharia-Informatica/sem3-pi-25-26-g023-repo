package pt.ipp.isep.dei.Tests;

import pt.ipp.isep.dei.domain.*; // Import all necessary domain classes

import java.util.*; // Import Collections and other utilities
import java.util.stream.Collectors;

/**
 * Test harness for USEI04: Pick Path Sequencing.
 * <p>
 * This class implements {@link Runnable} to execute test scenarios for the
 * {@link PickingPathService}. It validates the two primary pathfinding strategies
 * ("Deterministic Sweep" and "Nearest Neighbour") by checking the generated
 * path sequence and the total calculated distance.
 * </p>
 * It tests:
 * <ul>
 * <li>Edge cases (null/empty plan, invalid locations, duplicates).</li>
 * <li>Simple paths (single aisle, single point).</li>
 * <li>Complex paths (multiple aisles, "staircase", horizontal lines).</li>
 * <li>Strategy differentiation (cases where Nearest Neighbour is optimal).</li>
 * </ul>
 */
public class USEI04test implements Runnable {

    /** Structure to store test results (Scenario Name -> Pass/Fail). */
    private final Map<String, Boolean> testResults = new HashMap<>();

    // --- Main Method for Execution ---

    /**
     * Main entry point for the test runner.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        USEI04test tester = new USEI04test();
        tester.run();
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
        System.out.println("     Test Report - USEI04 Pick Path Service    ");
        System.out.println("======================================================");

        PickingPathService pathService = new PickingPathService();

        // Execute each scenario and store the result (true=PASS, false=FAIL)
        testResults.put("Scenario 01: Null and Empty Plan", testPlanoNuloVazio(pathService));
        testResults.put("Scenario 02: No Valid Locations", testSemLocalizacoesValidas(pathService));
        testResults.put("Scenario 03: Entrance Only (1 invalid assignment)", testApenasEntrada(pathService));
        testResults.put("Scenario 04: Duplicate Locations", testLocalizacoesDuplicadas(pathService));
        testResults.put("Scenario 05: Locations in Only One Aisle (Ex: Aisle 2)", testApenasUmAisle(pathService));
        testResults.put("Scenario 06: Locations in Multiple Aisles (Typical Case)", testVariosAisles(pathService));
        testResults.put("Scenario 07: Inverted Order / Distant Points", testOrdemInvertida(pathService));
        // --- New Scenarios ---
        testResults.put("Scenario 08: Only One Valid Point", testApenasUmPonto(pathService));
        testResults.put("Scenario 09: Points in Horizontal Line", testLinhaHorizontal(pathService));
        testResults.put("Scenario 10: Staircase Points", testEscada(pathService));
        testResults.put("Scenario 11: Nearest Neighbour Tie-Breaker", testDesempateNearestNeighbour(pathService));


        // Print the final summary
        printSummary();
    }

    // --- Test Methods per Scenario (return boolean) ---

    /**
     * Scenario 01: Tests behavior with a {@code null} plan and an empty plan.
     * Expects a default path containing only the entrance with 0 distance.
     *
     * @param service The {@link PickingPathService} instance.
     * @return {@code true} if the test passes, {@code false} otherwise.
     */
    private boolean testPlanoNuloVazio(PickingPathService service) {
        printScenarioHeader("Scenario 01: Null and Empty Plan");
        boolean passed = true;

        System.out.println("--> Testing with NULL Plan:");
        Map<String, PickingPathService.PathResult> resultNull = service.calculatePickingPaths(null);
        passed &= checkResult(resultNull, "Null Plan", List.of(BayLocation.entrance()), 0.0, List.of(BayLocation.entrance()), 0.0);
        printResults(resultNull);

        System.out.println("\n--> Testing with EMPTY Plan (no trolleys):");
        PickingPlan planVazio = createPlan("PLAN_VAZIO");
        Map<String, PickingPathService.PathResult> resultVazio = service.calculatePickingPaths(planVazio);
        passed &= checkResult(resultVazio, "Empty Plan", List.of(BayLocation.entrance()), 0.0, List.of(BayLocation.entrance()), 0.0);
        printResults(resultVazio);

        printTestStatus(passed);
        return passed;
    }

    /**
     * Scenario 02: Tests a plan where all assignments have invalid locations.
     * Expects a default path containing only the entrance with 0 distance.
     *
     * @param service The {@link PickingPathService} instance.
     * @return {@code true} if the test passes, {@code false} otherwise.
     */
    private boolean testSemLocalizacoesValidas(PickingPathService service) {
        printScenarioHeader("Scenario 02: No Valid Locations");
        PickingPlan plan = createPlan("PLAN_INVALIDOS");
        Trolley t1 = new Trolley("T1_INVALIDOS", 100);
        t1.addAssignment(createAssignment("ORD1", 1, "SKU_A", 1, "BOX1", null, "1"));
        t1.addAssignment(createAssignment("ORD1", 2, "SKU_B", 1, "BOX2", "1", ""));
        t1.addAssignment(createAssignment("ORD1", 3, "SKU_C", 1, "BOX3", "ABC", "1"));
        t1.addAssignment(createAssignment("ORD1", 4, "SKU_D", 1, "BOX4", "1", "XYZ"));
        t1.addAssignment(createAssignment("ORD1", 5, "SKU_E", 1, "BOX5", "-1", "5"));
        t1.addAssignment(createAssignment("ORD1", 6, "SKU_F", 1, "BOX6", "1", "0"));
        t1.addAssignment(createAssignment("ORD1", 7, "SKU_G", 1, "BOX7", "N/A", "1"));
        plan.addTrolley(t1);

        Map<String, PickingPathService.PathResult> results = service.calculatePickingPaths(plan);
        boolean passed = checkResult(results, "Invalid Locs", List.of(BayLocation.entrance()), 0.0, List.of(BayLocation.entrance()), 0.0);
        printResults(results);

        printTestStatus(passed);
        return passed;
    }

    /**
     * Scenario 03: Tests a plan with one assignment having no location (null, null).
     * Expects a default path containing only the entrance with 0 distance.
     *
     * @param service The {@link PickingPathService} instance.
     * @return {@code true} if the test passes, {@code false} otherwise.
     */
    private boolean testApenasEntrada(PickingPathService service) {
        printScenarioHeader("Scenario 03: Entrance Only (1 invalid assignment)");
        PickingPlan plan = createPlan("PLAN_SO_ENTRADA");
        Trolley t1 = new Trolley("T1_SO_ENTRADA", 100);
        t1.addAssignment(createAssignment("ORD_X", 1, "SKU_X", 1, "BOXX", null, null));
        plan.addTrolley(t1);

        Map<String, PickingPathService.PathResult> results = service.calculatePickingPaths(plan);
        boolean passed = checkResult(results, "Entrance Only", List.of(BayLocation.entrance()), 0.0, List.of(BayLocation.entrance()), 0.0);
        printResults(results);

        printTestStatus(passed);
        return passed;
    }

    /**
     * Scenario 04: Tests if duplicate locations in assignments are correctly consolidated.
     * Expects the path to visit each unique location only once.
     *
     * @param service The {@link PickingPathService} instance.
     * @return {@code true} if the test passes, {@code false} otherwise.
     */
    private boolean testLocalizacoesDuplicadas(PickingPathService service) {
        printScenarioHeader("Scenario 04: Duplicate Locations");
        PickingPlan plan = createPlan("PLAN_DUPLICADOS");
        Trolley t1 = new Trolley("T1_DUPS", 100);
        t1.addAssignment(createAssignment("ORD2", 1, "SKU_A", 5, "BOX10", "1", "5")); // (1,5)
        t1.addAssignment(createAssignment("ORD2", 2, "SKU_B", 3, "BOX11", "2", "3")); // (2,3)
        t1.addAssignment(createAssignment("ORD2", 3, "SKU_C", 2, "BOX12", "1", "5")); // (1,5) Dup
        t1.addAssignment(createAssignment("ORD2", 4, "SKU_D", 4, "BOX13", "2", "3")); // (2,3) Dup
        plan.addTrolley(t1);
        Trolley t2 = new Trolley("T2_DUPS", 100);
        t2.addAssignment(createAssignment("ORD3", 1, "SKU_E", 1, "BOX14", "1", "8")); // (1,8)
        t2.addAssignment(createAssignment("ORD3", 2, "SKU_F", 6, "BOX15", "1", "5")); // (1,5) Dup
        plan.addTrolley(t2);


        System.out.println("--> Locations in assignments: (1,5), (2,3), (1,5), (2,3), (1,8), (1,5)");
        System.out.println("--> Expected unique locations: (1,5), (1,8), (2,3)");
        // Expected Path A: (0,0)->(1,5)->(1,8)->(2,3) ; Dist = 8 + 3 + 14 = 25
        // Expected Path B: (0,0)->(1,5)->(1,8)->(2,3) ; Dist = 8 + 3 + 14 = 25 (NN gives the same here)
        List<BayLocation> expectedPath = List.of(
                BayLocation.entrance(), createLoc(1, 5), createLoc(1, 8), createLoc(2, 3)
        );
        double expectedDistance = 25.0;

        Map<String, PickingPathService.PathResult> results = service.calculatePickingPaths(plan);
        boolean passed = checkResult(results, "Duplicates", expectedPath, expectedDistance, expectedPath, expectedDistance);
        printResults(results);

        printTestStatus(passed);
        return passed;
    }

    /**
     * Scenario 05: Tests a path where all locations are in a single aisle.
     * Expects both strategies to produce the same simple up-and-down path.
     *
     * @param service The {@link PickingPathService} instance.
     * @return {@code true} if the test passes, {@code false} otherwise.
     */
    private boolean testApenasUmAisle(PickingPathService service) {
        printScenarioHeader("Scenario 05: Locations in Only One Aisle (Ex: Aisle 2)");
        PickingPlan plan = createPlan("PLAN_AISLE2");
        Trolley t1 = new Trolley("T1_AISLE2", 100);
        t1.addAssignment(createAssignment("ORD4", 1, "SKU_A", 1, "BOX20", "2", "8"));
        t1.addAssignment(createAssignment("ORD4", 2, "SKU_B", 1, "BOX21", "2", "3"));
        t1.addAssignment(createAssignment("ORD4", 3, "SKU_C", 1, "BOX22", "2", "10"));
        t1.addAssignment(createAssignment("ORD4", 4, "SKU_D", 1, "BOX23", "2", "1"));
        t1.addAssignment(createAssignment("ORD4", 5, "SKU_E", 1, "BOX24", "2", "5"));
        plan.addTrolley(t1);

        System.out.println("--> Expected locations (Aisle 2): (2,1), (2,3), (2,5), (2,8), (2,10)");
        // Expected Path (A and B equal): (0,0)->(2,1)->(2,3)->(2,5)->(2,8)->(2,10)
        // Dist = 7 + 2 + 2 + 3 + 2 = 16
        List<BayLocation> expectedPath = List.of(
                BayLocation.entrance(), createLoc(2, 1), createLoc(2, 3), createLoc(2, 5), createLoc(2, 8), createLoc(2, 10)
        );
        double expectedDistance = 16.0;

        Map<String, PickingPathService.PathResult> results = service.calculatePickingPaths(plan);
        boolean passed = checkResult(results, "One Aisle", expectedPath, expectedDistance, expectedPath, expectedDistance);
        printResults(results);
        System.out.println("    (Note: Both strategies are expected to give the same result)");

        printTestStatus(passed);
        return passed;
    }

    /**
     * Scenario 06: Tests a typical case with locations in multiple aisles.
     *
     * @param service The {@link PickingPathService} instance.
     * @return {@code true} if the test passes, {@code false} otherwise.
     */
    private boolean testVariosAisles(PickingPathService service) {
        printScenarioHeader("Scenario 06: Locations in Multiple Aisles (Typical Case)");
        PickingPlan plan = createPlan("PLAN_VARIOS_AISLES");
        Trolley t1 = new Trolley("T1_VARIOS", 100);
        // Points: (1,8), (3,4), (2,2), (1,3), (3,1)
        t1.addAssignment(createAssignment("ORD5", 1, "SKU_A", 1, "BOX30", "1", "8"));
        t1.addAssignment(createAssignment("ORD5", 2, "SKU_B", 1, "BOX31", "3", "4"));
        t1.addAssignment(createAssignment("ORD5", 3, "SKU_C", 1, "BOX32", "2", "2"));
        t1.addAssignment(createAssignment("ORD5", 4, "SKU_D", 1, "BOX33", "1", "3"));
        t1.addAssignment(createAssignment("ORD5", 5, "SKU_E", 1, "BOX34", "3", "1"));
        plan.addTrolley(t1);

        System.out.println("--> Expected locations: (1,3), (1,8), (2,2), (3,1), (3,4)");
        // Path A (Sweep): (0,0)->(1,3)->(1,8)->(2,2)->(3,1)->(3,4) ; Dist = 6+5+13+6+3 = 33
        List<BayLocation> expectedPathA = List.of(
                BayLocation.entrance(), createLoc(1, 3), createLoc(1, 8), createLoc(2, 2), createLoc(3, 1), createLoc(3, 4)
        );
        double expectedDistanceA = 33.0;
        // Path B (Nearest): (0,0)->(1,3) D=6; (1,3)->(1,8) D=5; (1,8)->(2,2) D=13; (2,2)->(3,1) D=6; (3,1)->(3,4) D=3; Total = 6+5+13+6+3 = 33
        List<BayLocation> expectedPathB = List.of(
                BayLocation.entrance(), createLoc(1, 3), createLoc(1, 8), createLoc(2, 2), createLoc(3, 1), createLoc(3, 4)
        );
        double expectedDistanceB = 33.0;

        Map<String, PickingPathService.PathResult> results = service.calculatePickingPaths(plan);
        boolean passed = checkResult(results, "Multiple Aisles", expectedPathA, expectedDistanceA, expectedPathB, expectedDistanceB);
        printResults(results);
        System.out.println("    (Note: In this specific case, the strategies give the same result)");


        printTestStatus(passed);
        return passed;
    }

    /**
     * Scenario 07: Tests a case designed to highlight the difference between Sweep and NN.
     * Expects Nearest Neighbour to find a shorter path by not following the aisle sweep.
     *
     * @param service The {@link PickingPathService} instance.
     * @return {@code true} if the test passes, {@code false} otherwise.
     */
    private boolean testOrdemInvertida(PickingPathService service) {
        printScenarioHeader("Scenario 07: Inverted Order / Distant Points");
        PickingPlan plan = createPlan("PLAN_INVERTIDO");
        Trolley t1 = new Trolley("T1_INVERTIDO", 100);
        t1.addAssignment(createAssignment("ORD6", 1, "SKU_A", 1, "BOX40", "1", "10")); // (1,10)
        t1.addAssignment(createAssignment("ORD6", 2, "SKU_B", 1, "BOX41", "2", "1"));  // (2,1)
        plan.addTrolley(t1);

        System.out.println("--> Expected locations: (1,10), (2,1)");
        // Path A (Sweep): (0,0)->(1,10)->(2,1) ; Dist = 13 + 14 = 27
        List<BayLocation> expectedPathA = List.of(
                BayLocation.entrance(), createLoc(1, 10), createLoc(2, 1)
        );
        double expectedDistanceA = 27.0;
        // Path B (Nearest): (0,0)->(2,1)->(1,10) ; Dist = 7 + 14 = 21
        List<BayLocation> expectedPathB = List.of(
                BayLocation.entrance(), createLoc(2, 1), createLoc(1, 10)
        );
        double expectedDistanceB = 21.0;

        Map<String, PickingPathService.PathResult> results = service.calculatePickingPaths(plan);
        boolean passed = checkResult(results, "Inverted Order", expectedPathA, expectedDistanceA, expectedPathB, expectedDistanceB);
        printResults(results);
        System.out.println("    (Note: Nearest Neighbour should go to (2,1) first)");

        printTestStatus(passed);
        return passed;
    }

    // --- New Scenarios ---

    /**
     * Scenario 08: Tests a path from the entrance to a single valid point.
     *
     * @param service The {@link PickingPathService} instance.
     * @return {@code true} if the test passes, {@code false} otherwise.
     */
    private boolean testApenasUmPonto(PickingPathService service) {
        printScenarioHeader("Scenario 08: Only One Valid Point");
        PickingPlan plan = createPlan("PLAN_UM_PONTO");
        Trolley t1 = new Trolley("T1_UM_PONTO", 100);
        t1.addAssignment(createAssignment("ORD7", 1, "SKU_A", 1, "BOX50", "3", "5")); // (3,5)
        t1.addAssignment(createAssignment("ORD7", 2, "SKU_B", 1, "BOX51", null, "1")); // Invalid
        plan.addTrolley(t1);

        System.out.println("--> Expected location: (3,5)");
        // Path A and B: (0,0)->(3,5) ; Dist = 0 + |0-3|*3 + 5 = 14
        List<BayLocation> expectedPath = List.of(BayLocation.entrance(), createLoc(3, 5));
        double expectedDistance = 14.0;

        Map<String, PickingPathService.PathResult> results = service.calculatePickingPaths(plan);
        boolean passed = checkResult(results, "One Point", expectedPath, expectedDistance, expectedPath, expectedDistance);
        printResults(results);

        printTestStatus(passed);
        return passed;
    }

    /**
     * Scenario 09: Tests a path with points at the same bay depth across different aisles.
     *
     * @param service The {@link PickingPathService} instance.
     * @return {@code true} if the test passes, {@code false} otherwise.
     */
    private boolean testLinhaHorizontal(PickingPathService service) {
        printScenarioHeader("Scenario 09: Points in Horizontal Line (Same Bay)");
        PickingPlan plan = createPlan("PLAN_HORIZONTAL");
        Trolley t1 = new Trolley("T1_HORIZONTAL", 100);
        t1.addAssignment(createAssignment("ORD8", 1, "SKU_A", 1, "BOX60", "1", "5")); // (1,5)
        t1.addAssignment(createAssignment("ORD8", 2, "SKU_B", 1, "BOX61", "3", "5")); // (3,5)
        t1.addAssignment(createAssignment("ORD8", 3, "SKU_C", 1, "BOX62", "2", "5")); // (2,5)
        plan.addTrolley(t1);

        System.out.println("--> Expected locations: (1,5), (2,5), (3,5)");
        // Path A: (0,0)->(1,5)->(2,5)->(3,5) ; Dist = 8 + (5+|1-2|*3+5) + (5+|2-3|*3+5) = 8 + 13 + 13 = 34
        List<BayLocation> expectedPathA = List.of(
                BayLocation.entrance(), createLoc(1, 5), createLoc(2, 5), createLoc(3, 5)
        );
        double expectedDistanceA = 34.0;
        // Path B: (0,0)->(1,5) D=8; (1,5)->(2,5) D=13; (2,5)->(3,5) D=13 ; Total = 34
        List<BayLocation> expectedPathB = List.of(
                BayLocation.entrance(), createLoc(1, 5), createLoc(2, 5), createLoc(3, 5)
        );
        double expectedDistanceB = 34.0;


        Map<String, PickingPathService.PathResult> results = service.calculatePickingPaths(plan);
        boolean passed = checkResult(results, "Horizontal Line", expectedPathA, expectedDistanceA, expectedPathB, expectedDistanceB);
        printResults(results);

        printTestStatus(passed);
        return passed;
    }

    /**
     * Scenario 10: Tests a "staircase" pattern of points.
     *
     * @param service The {@link PickingPathService} instance.
     * @return {@code true} if the test passes, {@code false} otherwise.
     */
    private boolean testEscada(PickingPathService service) {
        printScenarioHeader("Scenario 10: Staircase Points");
        PickingPlan plan = createPlan("PLAN_ESCADA");
        Trolley t1 = new Trolley("T1_ESCADA", 100);
        t1.addAssignment(createAssignment("ORD9", 1, "SKU_A", 1, "BOX70", "1", "2")); // (1,2)
        t1.addAssignment(createAssignment("ORD9", 2, "SKU_B", 1, "BOX71", "2", "4")); // (2,4)
        t1.addAssignment(createAssignment("ORD9", 3, "SKU_C", 1, "BOX72", "3", "6")); // (3,6)
        plan.addTrolley(t1);

        System.out.println("--> Expected locations: (1,2), (2,4), (3,6)");
        // Path A: (0,0)->(1,2)->(2,4)->(3,6) ; Dist = 5 + (2+|1-2|*3+4) + (4+|2-3|*3+6) = 5 + 9 + 13 = 27
        List<BayLocation> expectedPathA = List.of(
                BayLocation.entrance(), createLoc(1, 2), createLoc(2, 4), createLoc(3, 6)
        );
        double expectedDistanceA = 27.0;
        // Path B: (0,0)->(1,2) D=5; (1,2)->(2,4) D=9; (2,4)->(3,6) D=13; Total = 27
        List<BayLocation> expectedPathB = List.of(
                BayLocation.entrance(), createLoc(1, 2), createLoc(2, 4), createLoc(3, 6)
        );
        double expectedDistanceB = 27.0;

        Map<String, PickingPathService.PathResult> results = service.calculatePickingPaths(plan);
        boolean passed = checkResult(results, "Staircase", expectedPathA, expectedDistanceA, expectedPathB, expectedDistanceB);
        printResults(results);

        printTestStatus(passed);
        return passed;
    }

    /**
     * Scenario 11: Tests the Nearest Neighbour tie-breaking logic.
     * When two points are equidistant, the one with the lower aisle, then lower bay
     * (based on {@link BayLocation#compareTo}) should be chosen.
     *
     * @param service The {@link PickingPathService} instance.
     * @return {@code true} if the test passes, {@code false} otherwise.
     */
    private boolean testDesempateNearestNeighbour(PickingPathService service) {
        printScenarioHeader("Scenario 11: Nearest Neighbour Tie-Breaker");
        PickingPlan plan = createPlan("PLAN_DESEMPATE");
        Trolley t1 = new Trolley("T1_DESEMPATE", 100);
        // Equidistant points from entrance: (1,1) D=4, (2,(-1)) Invalid - But D((0,0),(2,1))=7
        // We add (1,4) D=7
        t1.addAssignment(createAssignment("ORD10", 1, "SKU_A", 1, "BOX80", "1", "4")); // (1,4) D=7
        t1.addAssignment(createAssignment("ORD10", 2, "SKU_B", 1, "BOX81", "2", "1")); // (2,1) D=7
        t1.addAssignment(createAssignment("ORD10", 3, "SKU_C", 1, "BOX82", "1", "1")); // (1,1) D=4
        plan.addTrolley(t1);

        System.out.println("--> Expected locations: (1,1), (1,4), (2,1)");
        System.out.println("    Distances from Entrance: (1,1)=4, (1,4)=7, (2,1)=7");
        // Path A: (0,0)->(1,1)->(1,4)->(2,1) ; Dist = 4 + 3 + (4+|1-2|*3+1) = 4 + 3 + 8 = 15
        List<BayLocation> expectedPathA = List.of(
                BayLocation.entrance(), createLoc(1, 1), createLoc(1, 4), createLoc(2, 1)
        );
        double expectedDistanceA = 15.0;
        // Path B: (0,0)->(1,1) D=4. From (1,1): D(->1,4)=3, D(->2,1)=1+|1-2|*3+1=5. Goes to (1,4). From (1,4): D(->2,1)=4+|1-2|*3+1=8.
        // Path B: (0,0)->(1,1)->(1,4)->(2,1) ; Dist = 4 + 3 + 8 = 15
        List<BayLocation> expectedPathB = List.of(
                BayLocation.entrance(), createLoc(1, 1), createLoc(1, 4), createLoc(2, 1)
        );
        double expectedDistanceB = 15.0;


        Map<String, PickingPathService.PathResult> results = service.calculatePickingPaths(plan);
        boolean passed = checkResult(results, "NN Tie-Breaker", expectedPathA, expectedDistanceA, expectedPathB, expectedDistanceB);
        printResults(results);
        System.out.println("    (Note: The NN tie-breaker should choose (1,4) over (2,1) as the next stop after (1,1), if compareTo is correct)");


        printTestStatus(passed);
        return passed;
    }


    // --- Helper Methods (maintained from previous version) ---

    /**
     * Helper method to print a standardized header for each test scenario.
     *
     * @param title The title of the scenario.
     */
    private void printScenarioHeader(String title) {
        System.out.println("\n------------------------------------------------------");
        System.out.println("  " + title);
        System.out.println("------------------------------------------------------");
    }

    /**
     * Helper method to create a {@link PickingAssignment} instance for testing.
     *
     * @param orderId The order ID.
     * @param lineNo  The order line number.
     * @param sku     The item SKU.
     * @param qty     The quantity.
     * @param boxId   The source box ID.
     * @param aisle   The source aisle string.
     * @param bay     The source bay string.
     * @return A new {@link PickingAssignment} object.
     */
    private PickingAssignment createAssignment(String orderId, int lineNo, String sku, int qty, String boxId, String aisle, String bay) {
        // Mock Item (weight 1.0 for simplicity, as it doesn't affect path)
        Item mockItem = new Item(sku, "Mock Item " + sku, "Mock Category", "unit", 1.0);
        return new PickingAssignment(orderId, lineNo, mockItem, qty, boxId, aisle, bay);
    }

    /**
     * Helper method to create an empty {@link PickingPlan} for testing.
     *
     * @param planId The ID for the plan.
     * @return A new, empty {@link PickingPlan} object.
     */
    private PickingPlan createPlan(String planId) {
        // Heuristic and capacity are not relevant for USEI04, we use dummy values
        return new PickingPlan(planId, HeuristicType.FIRST_FIT, 0);
    }

    /**
     * Helper method to create a {@link BayLocation} instance for comparison.
     * This is necessary because the main {@code BayLocation} constructor is private.
     *
     * @param aisle The aisle number.
     * @param bay   The bay number.
     * @return A new {@link BayLocation} object.
     */
    private BayLocation createLoc(int aisle, int bay) {
        // Create a dummy assignment just to use the public BayLocation constructor
        PickingAssignment dummy = createAssignment("dummy", 0, "dummy", 0, "dummy", String.valueOf(aisle), String.valueOf(bay));
        return new BayLocation(dummy);
    }


    /**
     * Helper method to print the path results from the service.
     *
     * @param results The map of results returned by the {@link PickingPathService}.
     */
    private void printResults(Map<String, PickingPathService.PathResult> results) {
        if (results == null || results.isEmpty()) {
            System.out.println("    ERROR: Null or empty results returned.");
            return;
        }
        results.forEach((strategyName, result) -> {
            System.out.println("\n  " + strategyName + ":");
            if (result == null) {
                System.out.println("    ERROR: Null PathResult.");
            } else {
                System.out.println("    " + result); // Uses the toString() of PathResult
            }
        });
    }

    /**
     * Helper method to validate the results from both pathfinding strategies.
     *
     * @param actualResults   The map of results returned by the service.
     * @param testName        The name of the test scenario for logging.
     * @param expectedPathA   The expected path for Strategy A (Sweep).
     * @param expectedDistA   The expected distance for Strategy A.
     * @param expectedPathB   The expected path for Strategy B (Nearest Neighbour).
     * @param expectedDistB   The expected distance for Strategy B.
     * @return {@code true} if both strategies match their expected results, {@code false} otherwise.
     */
    private boolean checkResult(Map<String, PickingPathService.PathResult> actualResults, String testName,
                                List<BayLocation> expectedPathA, double expectedDistA,
                                List<BayLocation> expectedPathB, double expectedDistB) {
        boolean passA = false;
        boolean passB = false;

        PickingPathService.PathResult actualA = actualResults.get("Strategy A (Deterministic Sweep)");
        PickingPathService.PathResult actualB = actualResults.get("Strategy B (Nearest Neighbour)");

        // Check Strategy A
        if (actualA != null && arePathsEqual(expectedPathA, actualA.path) && Math.abs(expectedDistA - actualA.totalDistance) < 0.01) {
            passA = true;
        } else {
            System.err.printf("    [%s - Strat A] FAILED! Expected: Path=%s, Dist=%.2f | Got: Path=%s, Dist=%.2f%n",
                    testName, expectedPathA, expectedDistA, actualA != null ? actualA.path : "NULL", actualA != null ? actualA.totalDistance : Double.NaN);
        }

        // Check Strategy B
        if (actualB != null && arePathsEqual(expectedPathB, actualB.path) && Math.abs(expectedDistB - actualB.totalDistance) < 0.01) {
            passB = true;
        } else {
            System.err.printf("    [%s - Strat B] FAILED! Expected: Path=%s, Dist=%.2f | Got: Path=%s, Dist=%.2f%n",
                    testName, expectedPathB, expectedDistB, actualB != null ? actualB.path : "NULL", actualB != null ? actualB.totalDistance : Double.NaN);
        }

        return passA && passB;
    }

    /**
     * Compares two lists of {@link BayLocation} objects for equality (order matters).
     *
     * @param path1 The first path.
     * @param path2 The second path.
     * @return {@code true} if the paths are identical, {@code false} otherwise.
     */
    private boolean arePathsEqual(List<BayLocation> path1, List<BayLocation> path2) {
        return Objects.equals(path1, path2); // Uses the list's equals, which compares element by element
    }


    /**
     * Helper method to print the final PASSED (✅) or FAILED (❌)
     * status for a scenario.
     *
     * @param passed {@code true} if the scenario passed, {@code false} otherwise.
     */
    private void printTestStatus(boolean passed) {
        if (passed) {
            System.out.println("\n  --> Scenario Result: ✅ PASSED");
        } else {
            System.err.println("\n  --> Scenario Result: ❌ FAILED");
        }
    }

    /**
     * Prints a final summary report of all executed tests, collating
     * results from the {@code testResults} map.
     */
    private void printSummary() {
        System.out.println("\n======================================================");
        System.out.println("                 Test Report Summary         ");
        System.out.println("======================================================");
        int passCount = 0;
        int failCount = 0;
        // Sort test names for consistent presentation
        List<String> sortedTestNames = new ArrayList<>(testResults.keySet());
        Collections.sort(sortedTestNames);

        for (String testName : sortedTestNames) {
            Boolean resultValue = testResults.get(testName);
            // Correct logic to treat null as failure
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
        System.out.println("                 End of Test Report             ");
    }

}