package pt.ipp.isep.dei.Tests;

import pt.ipp.isep.dei.domain.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Test class for USEI03 - Picking Plan generation with different heuristics.
 */
public class USEI03test implements Runnable {

    private final Map<String, Boolean> testResults = new HashMap<>();
    private Map<String, Item> itemsMap;

    public static void main(String[] args) {
        new USEI03test().run();
    }

    public USEI03test() {
        itemsMap = createMockItems();
    }

    @Override
    public void run() {
        System.out.println("======================================================");
        System.out.println("     Test Report - USEI03 Picking Plan         ");
        System.out.println("======================================================");

        testResults.put("Scenario 01: No Allocations", testNoAllocations());
        testResults.put("Scenario 02: First Fit Heuristic", testFirstFit());
        testResults.put("Scenario 03: First Fit Decreasing Heuristic", testFirstFitDecreasing());
        testResults.put("Scenario 04: Best Fit Decreasing Heuristic", testBestFitDecreasing());
        testResults.put("Scenario 05: Multiple Trolleys Required", testMultipleTrolleys());
        testResults.put("Scenario 06: Perfect Utilization (BFD)", testPerfectUtilizationBFD());

        printSummary();
    }

    // --- Test Scenarios ---

    private boolean testNoAllocations() {
        printScenarioHeader("Scenario 01: No Allocations");
        PickingService service = new PickingService();
        service.setItemsMap(itemsMap);
        List<Allocation> allocations = new ArrayList<>();
        PickingPlan plan = service.generatePickingPlan(allocations, 100.0, HeuristicType.FIRST_FIT);

        boolean passed = plan != null && plan.getTrolleys().isEmpty() && plan.getTotalTrolleys() == 0;
        printResults("Plan should have no trolleys", passed ? "Correct" : "Incorrect: " + plan.getTotalTrolleys() + " trolleys");
        printTestStatus(passed);
        return passed;
    }

    private boolean testFirstFit() {
        printScenarioHeader("Scenario 02: First Fit Heuristic");
        PickingService service = new PickingService();
        service.setItemsMap(itemsMap);
        // Weights: A=15, B=40, C=25, D=30, E=10 (Total=120)
        List<Allocation> allocations = List.of(
                createAllocation("O1", 1, "SKUA", 10, "B1", "1", "1"), // 10 * 1.5 = 15kg
                createAllocation("O1", 2, "SKUB", 20, "B2", "1", "2"), // 20 * 2.0 = 40kg
                createAllocation("O1", 3, "SKUC", 10, "B3", "1", "3"), // 10 * 2.5 = 25kg
                createAllocation("O1", 4, "SKUD", 15, "B4", "1", "4"), // 15 * 2.0 = 30kg
                createAllocation("O1", 5, "SKUE", 5, "B5", "1", "5")   // 5 * 2.0 = 10kg
        );
        double capacity = 60.0;
        PickingPlan plan = service.generatePickingPlan(allocations, capacity, HeuristicType.FIRST_FIT);

        // Expected FF (Cap=60):
        // T1: A(15) + B(40) = 55
        // T2: C(25) + D(30) = 55
        // T3: E(10) = 10
        boolean passed = plan.getTotalTrolleys() == 3 &&
                checkTrolleyByIndex(plan, 0, List.of("SKUA", "SKUB"), 55.0) &&
                checkTrolleyByIndex(plan, 1, List.of("SKUC", "SKUD"), 55.0) &&
                checkTrolleyByIndex(plan, 2, List.of("SKUE"), 10.0);

        printResults("Expected number of trolleys: 3", plan.getTotalTrolleys() == 3 ? "Correct" : "Incorrect: " + plan.getTotalTrolleys());
        printPlanDetails(plan);
        printTestStatus(passed);
        return passed;
    }

    private boolean testFirstFitDecreasing() {
        printScenarioHeader("Scenario 03: First Fit Decreasing Heuristic");
        PickingService service = new PickingService();
        service.setItemsMap(itemsMap);
        // Same allocations, weights: B=40, D=30, C=25, A=15, E=10 (Total=120)
        List<Allocation> allocations = List.of(
                createAllocation("O1", 1, "SKUA", 10, "B1", "1", "1"), // 15kg
                createAllocation("O1", 2, "SKUB", 20, "B2", "1", "2"), // 40kg
                createAllocation("O1", 3, "SKUC", 10, "B3", "1", "3"), // 25kg
                createAllocation("O1", 4, "SKUD", 15, "B4", "1", "4"), // 30kg
                createAllocation("O1", 5, "SKUE", 5, "B5", "1", "5")   // 10kg
        );
        double capacity = 60.0;
        PickingPlan plan = service.generatePickingPlan(allocations, capacity, HeuristicType.FIRST_FIT_DECREASING);

        // Expected FFD (Order: B, D, C, A, E) (Cap=60):
        // T1: B(40) + A(15) = 55
        // T2: D(30) + C(25) = 55
        // T3: E(10) = 10
        boolean passed = plan.getTotalTrolleys() == 3 &&
                checkTrolleyByIndex(plan, 0, List.of("SKUB", "SKUA"), 55.0) &&
                checkTrolleyByIndex(plan, 1, List.of("SKUD", "SKUC"), 55.0) &&
                checkTrolleyByIndex(plan, 2, List.of("SKUE"), 10.0);

        printResults("Expected number of trolleys: 3", plan.getTotalTrolleys() == 3 ? "Correct" : "Incorrect: " + plan.getTotalTrolleys());
        printPlanDetails(plan);
        printTestStatus(passed);
        return passed;
    }

    private boolean testBestFitDecreasing() {
        printScenarioHeader("Scenario 04: Best Fit Decreasing Heuristic");
        PickingService service = new PickingService();
        service.setItemsMap(itemsMap);
        // Same allocations, weights: B=40, D=30, C=25, A=15, E=10 (Total=120)
        List<Allocation> allocations = List.of(
                createAllocation("O1", 1, "SKUA", 10, "B1", "1", "1"), // 15kg
                createAllocation("O1", 2, "SKUB", 20, "B2", "1", "2"), // 40kg
                createAllocation("O1", 3, "SKUC", 10, "B3", "1", "3"), // 25kg
                createAllocation("O1", 4, "SKUD", 15, "B4", "1", "4"), // 30kg
                createAllocation("O1", 5, "SKUE", 5, "B5", "1", "5")   // 10kg
        );
        double capacity = 60.0;
        PickingPlan plan = service.generatePickingPlan(allocations, capacity, HeuristicType.BEST_FIT_DECREASING);

        // Expected BFD (Order: B, D, C, A, E) (Cap=60):
        // T1: B(40) + A(15) = 55
        // T2: D(30) + C(25) = 55
        // T3: E(10) = 10
        boolean passed = plan.getTotalTrolleys() == 3 &&
                checkTrolleyByIndex(plan, 0, List.of("SKUB", "SKUA"), 55.0) &&
                checkTrolleyByIndex(plan, 1, List.of("SKUD", "SKUC"), 55.0) &&
                checkTrolleyByIndex(plan, 2, List.of("SKUE"), 10.0);

        printResults("Expected number of trolleys: 3", plan.getTotalTrolleys() == 3 ? "Correct" : "Incorrect: " + plan.getTotalTrolleys());
        printPlanDetails(plan);
        printTestStatus(passed);
        return passed;
    }

    private boolean testMultipleTrolleys() {
        printScenarioHeader("Scenario 05: Multiple Trolleys Required");
        PickingService service = new PickingService();
        service.setItemsMap(itemsMap);
        List<Allocation> allocations = new ArrayList<>();
        // Add 10 items of 15kg each = 150kg total
        for (int i = 1; i <= 10; i++) {
            allocations.add(createAllocation("O"+i, 1, "SKUA", 10, "B"+i, "1", String.valueOf(i))); // 15kg
        }
        double capacity = 50.0;
        PickingPlan plan = service.generatePickingPlan(allocations, capacity, HeuristicType.FIRST_FIT);

        // Expected FF (Cap=50): 10 * 15kg -> 150kg total
        // T1: 15+15+15 = 45
        // T2: 15+15+15 = 45
        // T3: 15+15+15 = 45
        // T4: 15 = 15
        boolean passed = plan.getTotalTrolleys() == 4;

        printResults("Expected number of trolleys: 4", passed ? "Correct" : "Incorrect: " + plan.getTotalTrolleys());
        printPlanDetails(plan);
        printTestStatus(passed);
        return passed;
    }

    private boolean testPerfectUtilizationBFD() {
        printScenarioHeader("Scenario 06: Perfect Utilization (BFD)");
        PickingService service = new PickingService();
        service.setItemsMap(itemsMap);
        // Items: 40, 30, 30, 20, 20, 10 (Total=150)
        List<Allocation> allocations = List.of(
                createAllocation("O1", 1, "SKUB", 20, "B1", "1", "1"), // 40kg
                createAllocation("O1", 2, "SKUD", 15, "B2", "1", "2"), // 30kg
                createAllocation("O1", 3, "SKUD", 15, "B3", "1", "3"), // 30kg
                createAllocation("O1", 4, "SKUB", 10, "B4", "1", "4"), // 20kg
                createAllocation("O1", 5, "SKUB", 10, "B5", "1", "5"), // 20kg
                createAllocation("O1", 6, "SKUE", 5, "B6", "1", "6")   // 10kg
        );
        double capacity = 50.0;
        PickingPlan plan = service.generatePickingPlan(allocations, capacity, HeuristicType.BEST_FIT_DECREASING);

        // Verify 3 trolleys with 100% utilization
        boolean passed = plan.getTotalTrolleys() == 3 &&
                plan.getTrolleys().stream().allMatch(t -> Math.abs(t.getUtilization() - 100.0) < 0.01);

        printResults("Expected number of trolleys: 3", plan.getTotalTrolleys() == 3 ? "Correct" : "Incorrect: " + plan.getTotalTrolleys());
        printResults("All trolleys should have 100% utilization", passed ? "Correct" : "Incorrect");
        printPlanDetails(plan);
        printTestStatus(passed);
        return passed;
    }

    // --- Helper Methods ---

    private Map<String, Item> createMockItems() {
        Map<String, Item> items = new HashMap<>();
        items.put("SKUA", new Item("SKUA", "Item A", "Cat X", "unit", 1.5));
        items.put("SKUB", new Item("SKUB", "Item B", "Cat Y", "unit", 2.0));
        items.put("SKUC", new Item("SKUC", "Item C", "Cat Z", "unit", 2.5));
        items.put("SKUD", new Item("SKUD", "Item D", "Cat W", "unit", 2.0));
        items.put("SKUE", new Item("SKUE", "Item E", "Cat V", "unit", 2.0));
        return items;
    }

    private Allocation createAllocation(String orderId, int lineNo, String sku, int qty, String boxId, String aisle, String bay) {
        double weight = itemsMap.getOrDefault(sku, new Item(sku,"","", "", 1.0)).getUnitWeight() * qty;
        return new Allocation(orderId, lineNo, sku, qty, weight, boxId, aisle, bay);
    }

    private boolean checkTrolleyByIndex(PickingPlan plan, int index, List<String> expectedSkus, double expectedWeight) {
        List<Trolley> trolleys = plan.getTrolleys();

        if (index < 0 || index >= trolleys.size()) {
            System.err.printf("    [Check Trolley] FAILED! Trolley index %d not found.%n", index);
            return false;
        }

        Trolley trolley = trolleys.get(index);
        List<String> actualSkus = trolley.getAssignments().stream()
                .map(PickingAssignment::getSku)
                .collect(Collectors.toList());

        // Compare SKUs as sets (order doesn't matter)
        boolean skusMatch = new HashSet<>(expectedSkus).equals(new HashSet<>(actualSkus));
        boolean weightMatch = Math.abs(trolley.getCurrentWeight() - expectedWeight) < 0.01;

        if (!skusMatch) {
            System.err.printf("    [Check Trolley %d] FAILED SKUs! Expected: %s | Got: %s%n",
                    index, expectedSkus, actualSkus);
        }
        if (!weightMatch) {
            System.err.printf("    [Check Trolley %d] FAILED Weight! Expected: %.2f | Got: %.2f%n",
                    index, expectedWeight, trolley.getCurrentWeight());
        }

        return skusMatch && weightMatch;
    }

    private void printPlanDetails(PickingPlan plan) {
        System.out.println("    Plan Details:");
        if (plan == null || plan.getTrolleys().isEmpty()) {
            System.out.println("      Empty plan.");
            return;
        }
        for (int i = 0; i < plan.getTrolleys().size(); i++) {
            Trolley t = plan.getTrolleys().get(i);
            System.out.printf("      - T%d: %.1f/%.1f kg (%.1f%%) - Items: %s%n",
                    i + 1,
                    t.getCurrentWeight(),
                    t.getMaxCapacity(),
                    t.getUtilization(),
                    t.getAssignments().stream()
                            .map(pa -> String.format("%s(%d)", pa.getSku(), pa.getQuantity()))
                            .collect(Collectors.joining(", "))
            );
        }
    }

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
        System.out.println("             USEI03 Test Report Summary      ");
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
        System.out.println("             End of USEI03 Test Report        ");
        System.out.println("======================================================");
    }
}