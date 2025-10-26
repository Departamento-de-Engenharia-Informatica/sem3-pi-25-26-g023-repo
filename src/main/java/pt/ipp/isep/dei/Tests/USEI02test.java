package pt.ipp.isep.dei.Tests;

import pt.ipp.isep.dei.domain.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class USEI02test implements Runnable {

    private final Map<String, Boolean> testResults = new HashMap<>();
    private Map<String, Item> itemsMap; // Necessary for weights

    public static void main(String[] args) {
        new USEI02test().run();
    }

    // Initialize the items map before running tests
    public USEI02test() {
        itemsMap = createMockItems();
    }


    @Override
    public void run() {
        System.out.println("======================================================");
        System.out.println("    Test Report - USEI02 Order Allocation     ");
        System.out.println("======================================================");

        testResults.put("Scenario 01: No Orders", testSemEncomendas());
        testResults.put("Scenario 02: Empty Inventory", testInventarioVazio());
        testResults.put("Scenario 03: Sufficient Stock (Strict)", testStockSuficienteStrict());
        testResults.put("Scenario 04: Insufficient Stock (Strict)", testStockInsuficienteStrict());
        testResults.put("Scenario 05: Partial Stock (Strict)", testStockParcialStrict());
        testResults.put("Scenario 06: Sufficient Stock (Partial)", testStockSuficientePartial());
        testResults.put("Scenario 07: Insufficient Stock (Partial)", testStockInsuficientePartial());
        testResults.put("Scenario 08: Partial Stock (Partial)", testStockParcialPartial());
        testResults.put("Scenario 09: Order Priority", testPrioridadeEncomendas());
        testResults.put("Scenario 10: Line Priority", testPrioridadeLinhas());
        testResults.put("Scenario 11: FEFO Allocation", testAlocacaoFEFO());
        testResults.put("Scenario 12: FIFO Allocation", testAlocacaoFIFO());
        testResults.put("Scenario 13: Mixed FEFO/FIFO Allocation", testAlocacaoMista());
        testResults.put("Scenario 14: Allocation across Multiple Boxes", testAlocacaoMultiplasCaixas());
        testResults.put("Scenario 15: SKU does not exist in Inventory", testSkuNaoExiste());

        printSummary();
    }

    // --- Test Scenarios ---

    private boolean testSemEncomendas() {
        printScenarioHeader("Scenario 01: No Orders");
        OrderAllocator allocator = new OrderAllocator();
        allocator.setItems(itemsMap);
        List<Order> orders = new ArrayList<>();
        List<Box> inventory = List.of(createBox("B1", "SKU1", 10, null, LocalDateTime.now(), "1", "1"));
        AllocationResult result = allocator.allocateOrders(orders, inventory, OrderAllocator.Mode.STRICT);

        boolean passed = result.eligibilityList.isEmpty() && result.allocations.isEmpty();
        printResults("Eligibility and Allocations lists should be empty.", passed ? "Yes" : "No");
        printTestStatus(passed);
        return passed;
    }

    private boolean testInventarioVazio() {
        printScenarioHeader("Scenario 02: Empty Inventory");
        OrderAllocator allocator = new OrderAllocator();
        allocator.setItems(itemsMap);
        List<Order> orders = List.of(createOrder("ORD1", 1, LocalDate.now(), List.of(new OrderLine(1, "SKU1", 5))));
        List<Box> inventory = new ArrayList<>();
        AllocationResult result = allocator.allocateOrders(orders, inventory, OrderAllocator.Mode.STRICT);

        boolean passed = result.eligibilityList.size() == 1 &&
                result.eligibilityList.get(0).status == Status.UNDISPATCHABLE &&
                result.eligibilityList.get(0).allocatedQty == 0 &&
                result.allocations.isEmpty();
        printResults("Eligibility should be UNDISPATCHABLE, Allocations empty.", passed ? "Correct" : "Incorrect: " + result.eligibilityList);
        printTestStatus(passed);
        return passed;
    }

    private boolean testStockSuficienteStrict() {
        printScenarioHeader("Scenario 03: Sufficient Stock (Strict)");
        OrderAllocator allocator = new OrderAllocator();
        allocator.setItems(itemsMap);
        List<Order> orders = List.of(createOrder("ORD1", 1, LocalDate.now(), List.of(new OrderLine(1, "SKU1", 5))));
        // It's important to create a MUTABLE COPY of the inventory for the test
        List<Box> inventory = new ArrayList<>(List.of(
                createBox("B1", "SKU1", 10, null, LocalDateTime.now(), "1", "1")
        ));
        AllocationResult result = allocator.allocateOrders(orders, inventory, OrderAllocator.Mode.STRICT);

        boolean passed = result.eligibilityList.size() == 1 &&
                result.eligibilityList.get(0).status == Status.ELIGIBLE &&
                result.eligibilityList.get(0).allocatedQty == 5 &&
                result.allocations.size() == 1 &&
                result.allocations.get(0).qty == 5 &&
                result.allocations.get(0).boxId.equals("B1");
        printResults("Eligibility ELIGIBLE (5/5), 1 Allocation from B1.", passed ? "Correct" : "Incorrect: " + result.eligibilityList + " / " + result.allocations);
        printTestStatus(passed);
        return passed;
    }

    private boolean testStockInsuficienteStrict() {
        printScenarioHeader("Scenario 04: Insufficient Stock (Strict)");
        OrderAllocator allocator = new OrderAllocator();
        allocator.setItems(itemsMap);
        List<Order> orders = List.of(createOrder("ORD1", 1, LocalDate.now(), List.of(new OrderLine(1, "SKU1", 15))));
        List<Box> inventory = new ArrayList<>(List.of(
                createBox("B1", "SKU1", 10, null, LocalDateTime.now(), "1", "1")
        ));
        AllocationResult result = allocator.allocateOrders(orders, inventory, OrderAllocator.Mode.STRICT);

        boolean passed = result.eligibilityList.size() == 1 &&
                result.eligibilityList.get(0).status == Status.UNDISPATCHABLE &&
                result.eligibilityList.get(0).allocatedQty == 0 && // Strict allocates nothing if not complete
                result.allocations.isEmpty();
        printResults("Eligibility UNDISPATCHABLE (0/15), Allocations empty.", passed ? "Correct" : "Incorrect: " + result.eligibilityList + " / " + result.allocations);
        printTestStatus(passed);
        return passed;
    }

    private boolean testStockParcialStrict() {
        printScenarioHeader("Scenario 05: Partial Stock (Strict)");
        OrderAllocator allocator = new OrderAllocator();
        allocator.setItems(itemsMap);
        List<Order> orders = List.of(createOrder("ORD1", 1, LocalDate.now(), List.of(new OrderLine(1, "SKU1", 10))));
        List<Box> inventory = new ArrayList<>(List.of(
                createBox("B1", "SKU1", 5, null, LocalDateTime.now(), "1", "1")
        ));
        AllocationResult result = allocator.allocateOrders(orders, inventory, OrderAllocator.Mode.STRICT);

        // Same result as insufficient in STRICT mode
        boolean passed = result.eligibilityList.size() == 1 &&
                result.eligibilityList.get(0).status == Status.UNDISPATCHABLE &&
                result.eligibilityList.get(0).allocatedQty == 0 &&
                result.allocations.isEmpty();
        printResults("Eligibility UNDISPATCHABLE (0/10), Allocations empty.", passed ? "Correct" : "Incorrect: " + result.eligibilityList + " / " + result.allocations);
        printTestStatus(passed);
        return passed;
    }

    private boolean testStockSuficientePartial() {
        printScenarioHeader("Scenario 06: Sufficient Stock (Partial)");
        OrderAllocator allocator = new OrderAllocator();
        allocator.setItems(itemsMap);
        List<Order> orders = List.of(createOrder("ORD1", 1, LocalDate.now(), List.of(new OrderLine(1, "SKU1", 5))));
        List<Box> inventory = new ArrayList<>(List.of(
                createBox("B1", "SKU1", 10, null, LocalDateTime.now(), "1", "1")
        ));
        AllocationResult result = allocator.allocateOrders(orders, inventory, OrderAllocator.Mode.PARTIAL);

        // Same result as Strict when stock is sufficient
        boolean passed = result.eligibilityList.size() == 1 &&
                result.eligibilityList.get(0).status == Status.ELIGIBLE &&
                result.eligibilityList.get(0).allocatedQty == 5 &&
                result.allocations.size() == 1 &&
                result.allocations.get(0).qty == 5;
        printResults("Eligibility ELIGIBLE (5/5), 1 Allocation.", passed ? "Correct" : "Incorrect: " + result.eligibilityList + " / " + result.allocations);
        printTestStatus(passed);
        return passed;
    }

    private boolean testStockInsuficientePartial() {
        printScenarioHeader("Scenario 07: Insufficient Stock (Partial)");
        OrderAllocator allocator = new OrderAllocator();
        allocator.setItems(itemsMap);
        List<Order> orders = List.of(createOrder("ORD1", 1, LocalDate.now(), List.of(new OrderLine(1, "SKU_NOSTOCK", 10))));
        List<Box> inventory = new ArrayList<>(List.of(
                createBox("B1", "SKU1", 5, null, LocalDateTime.now(), "1", "1") // Different SKU
        ));
        AllocationResult result = allocator.allocateOrders(orders, inventory, OrderAllocator.Mode.PARTIAL);

        // Same result as Strict when there is NO stock of the SKU
        boolean passed = result.eligibilityList.size() == 1 &&
                result.eligibilityList.get(0).status == Status.UNDISPATCHABLE &&
                result.eligibilityList.get(0).allocatedQty == 0 &&
                result.allocations.isEmpty();
        printResults("Eligibility UNDISPATCHABLE (0/10), Allocations empty.", passed ? "Correct" : "Incorrect: " + result.eligibilityList + " / " + result.allocations);
        printTestStatus(passed);
        return passed;
    }

    private boolean testStockParcialPartial() {
        printScenarioHeader("Scenario 08: Partial Stock (Partial)");
        OrderAllocator allocator = new OrderAllocator();
        allocator.setItems(itemsMap);
        List<Order> orders = List.of(createOrder("ORD1", 1, LocalDate.now(), List.of(new OrderLine(1, "SKU1", 10))));
        List<Box> inventory = new ArrayList<>(List.of(
                createBox("B1", "SKU1", 7, null, LocalDateTime.now(), "1", "1") // Only 7 units
        ));
        AllocationResult result = allocator.allocateOrders(orders, inventory, OrderAllocator.Mode.PARTIAL);

        // Should allocate what exists
        boolean passed = result.eligibilityList.size() == 1 &&
                result.eligibilityList.get(0).status == Status.PARTIAL &&
                result.eligibilityList.get(0).allocatedQty == 7 && // Allocated 7
                result.allocations.size() == 1 &&
                result.allocations.get(0).qty == 7 && // Confirm allocation
                result.allocations.get(0).boxId.equals("B1");
        printResults("Eligibility PARTIAL (7/10), 1 Allocation of 7.", passed ? "Correct" : "Incorrect: " + result.eligibilityList + " / " + result.allocations);
        printTestStatus(passed);
        return passed;
    }

    private boolean testPrioridadeEncomendas() {
        printScenarioHeader("Scenario 09: Order Priority");
        OrderAllocator allocator = new OrderAllocator();
        allocator.setItems(itemsMap);
        Order ord1_p2 = createOrder("ORD1", 2, LocalDate.now(), List.of(new OrderLine(1, "SKU1", 5)));
        Order ord2_p1 = createOrder("ORD2", 1, LocalDate.now(), List.of(new OrderLine(1, "SKU1", 5))); // Higher priority
        Order ord3_p1_due = createOrder("ORD3", 1, LocalDate.now().minusDays(1), List.of(new OrderLine(1, "SKU1", 5))); // Higher priority and earlier Due Date
        List<Order> orders = List.of(ord1_p2, ord2_p1, ord3_p1_due); // Out of order
        List<Box> inventory = new ArrayList<>(List.of(
                createBox("B1", "SKU1", 12, null, LocalDateTime.now(), "1", "1") // Stock for 2.4 orders
        ));

        AllocationResult result = allocator.allocateOrders(orders, inventory, OrderAllocator.Mode.STRICT);

        // Check if ORD3 and ORD2 were ELIGIBLE and ORD1 was UNDISPATCHABLE
        Map<String, Status> statuses = result.eligibilityList.stream()
                .collect(Collectors.toMap(e -> e.orderId, e -> e.status));

        boolean passed = result.eligibilityList.size() == 3 &&
                statuses.getOrDefault("ORD3", null) == Status.ELIGIBLE &&
                statuses.getOrDefault("ORD2", null) == Status.ELIGIBLE &&
                statuses.getOrDefault("ORD1", null) == Status.UNDISPATCHABLE &&
                result.allocations.size() == 2; // Allocated for ORD3 and ORD2

        printResults("Expected allocation order: ORD3 (Eligible), ORD2 (Eligible), ORD1 (Undispatchable)", passed ? "Correct" : "Incorrect: " + statuses);
        printTestStatus(passed);
        return passed;
    }

    private boolean testPrioridadeLinhas() {
        printScenarioHeader("Scenario 10: Line Priority");
        OrderAllocator allocator = new OrderAllocator();
        allocator.setItems(itemsMap);
        Order order = createOrder("ORD1", 1, LocalDate.now(), List.of(
                new OrderLine(2, "SKU1", 5), // Line 2 first in list
                new OrderLine(1, "SKU1", 5)  // Line 1 after
        ));
        List<Box> inventory = new ArrayList<>(List.of(
                createBox("B1", "SKU1", 7, null, LocalDateTime.now(), "1", "1") // Stock for 1 complete line + partial
        ));

        AllocationResult result = allocator.allocateOrders(List.of(order), inventory, OrderAllocator.Mode.PARTIAL);

        // Check if line 1 was processed first and became ELIGIBLE, and line 2 became PARTIAL
        Map<Integer, Eligibility> eligMap = result.eligibilityList.stream()
                .collect(Collectors.toMap(e -> e.lineNo, e -> e));

        boolean passed = result.eligibilityList.size() == 2 &&
                eligMap.get(1).status == Status.ELIGIBLE && eligMap.get(1).allocatedQty == 5 &&
                eligMap.get(2).status == Status.PARTIAL && eligMap.get(2).allocatedQty == 2 && // Remainder 7-5=2
                result.allocations.size() == 2; // One allocation for each line

        printResults("Line 1 ELIGIBLE (5/5), Line 2 PARTIAL (2/5).", passed ? "Correct" : "Incorrect: " + eligMap);
        printTestStatus(passed);
        return passed;
    }

    private boolean testAlocacaoFEFO() {
        printScenarioHeader("Scenario 11: FEFO Allocation");
        OrderAllocator allocator = new OrderAllocator();
        allocator.setItems(itemsMap);
        List<Order> orders = List.of(createOrder("ORD1", 1, LocalDate.now(), List.of(new OrderLine(1, "SKU_P", 8)))); // Request 8
        List<Box> inventory = new ArrayList<>(List.of(
                createBox("B_EXP_LATER", "SKU_P", 5, LocalDate.now().plusDays(10), LocalDateTime.now(), "1", "1"),
                createBox("B_EXP_SOONER", "SKU_P", 5, LocalDate.now().plusDays(5), LocalDateTime.now(), "1", "2") // Should be used first
        ));
        AllocationResult result = allocator.allocateOrders(orders, inventory, OrderAllocator.Mode.PARTIAL);

        // Should allocate 5 from B_EXP_SOONER and 3 from B_EXP_LATER
        boolean passed = result.allocations.size() == 2 &&
                result.allocations.stream().anyMatch(a -> a.boxId.equals("B_EXP_SOONER") && a.qty == 5) &&
                result.allocations.stream().anyMatch(a -> a.boxId.equals("B_EXP_LATER") && a.qty == 3) &&
                result.eligibilityList.get(0).status == Status.ELIGIBLE;

        printResults("Allocated 5 from B_EXP_SOONER and 3 from B_EXP_LATER.", passed ? "Correct" : "Incorrect: " + result.allocations);
        printTestStatus(passed);
        return passed;
    }

    private boolean testAlocacaoFIFO() {
        printScenarioHeader("Scenario 12: FIFO Allocation");
        OrderAllocator allocator = new OrderAllocator();
        allocator.setItems(itemsMap);
        List<Order> orders = List.of(createOrder("ORD1", 1, LocalDate.now(), List.of(new OrderLine(1, "SKU_NP", 8)))); // Request 8
        List<Box> inventory = new ArrayList<>(List.of(
                createBox("B_ARRIVED_LATER", "SKU_NP", 5, null, LocalDateTime.now().minusDays(1), "1", "1"),
                createBox("B_ARRIVED_SOONER", "SKU_NP", 5, null, LocalDateTime.now().minusDays(5), "1", "2") // Should be used first
        ));
        AllocationResult result = allocator.allocateOrders(orders, inventory, OrderAllocator.Mode.PARTIAL);

        // Should allocate 5 from B_ARRIVED_SOONER and 3 from B_ARRIVED_LATER
        boolean passed = result.allocations.size() == 2 &&
                result.allocations.stream().anyMatch(a -> a.boxId.equals("B_ARRIVED_SOONER") && a.qty == 5) &&
                result.allocations.stream().anyMatch(a -> a.boxId.equals("B_ARRIVED_LATER") && a.qty == 3) &&
                result.eligibilityList.get(0).status == Status.ELIGIBLE;

        printResults("Allocated 5 from B_ARRIVED_SOONER and 3 from B_ARRIVED_LATER.", passed ? "Correct" : "Incorrect: " + result.allocations);
        printTestStatus(passed);
        return passed;
    }

    private boolean testAlocacaoMista() {
        printScenarioHeader("Scenario 13: Mixed FEFO/FIFO Allocation");
        OrderAllocator allocator = new OrderAllocator();
        allocator.setItems(itemsMap);
        List<Order> orders = List.of(createOrder("ORD1", 1, LocalDate.now(), List.of(new OrderLine(1, "SKU_MIX", 12)))); // Request 12
        List<Box> inventory = new ArrayList<>(List.of(
                createBox("B_FIFO_OLD", "SKU_MIX", 5, null, LocalDateTime.now().minusDays(10), "1", "1"), // FIFO 1
                createBox("B_FEFO_URGENT", "SKU_MIX", 5, LocalDate.now().plusDays(2), LocalDateTime.now().minusDays(5), "1", "2"), // FEFO 1 (most urgent)
                createBox("B_FEFO_NORMAL", "SKU_MIX", 5, LocalDate.now().plusDays(20), LocalDateTime.now().minusDays(1), "1", "3"), // FEFO 2
                createBox("B_FIFO_RECENT", "SKU_MIX", 5, null, LocalDateTime.now().minusDays(2), "1", "4")  // FIFO 2
        ));
        AllocationResult result = allocator.allocateOrders(orders, inventory, OrderAllocator.Mode.PARTIAL);

        // Expected allocation order: B_FEFO_URGENT (5), B_FEFO_NORMAL (5), B_FIFO_OLD (2)
        boolean passed = result.allocations.size() == 3 &&
                result.allocations.get(0).boxId.equals("B_FEFO_URGENT") && result.allocations.get(0).qty == 5 &&
                result.allocations.get(1).boxId.equals("B_FEFO_NORMAL") && result.allocations.get(1).qty == 5 &&
                result.allocations.get(2).boxId.equals("B_FIFO_OLD") && result.allocations.get(2).qty == 2 &&
                result.eligibilityList.get(0).status == Status.ELIGIBLE;

        printResults("Allocated FEFO_URGENT(5), FEFO_NORMAL(5), FIFO_OLD(2).", passed ? "Correct" : "Incorrect: " + result.allocations);
        printTestStatus(passed);
        return passed;
    }


    private boolean testAlocacaoMultiplasCaixas() {
        printScenarioHeader("Scenario 14: Allocation across Multiple Boxes");
        OrderAllocator allocator = new OrderAllocator();
        allocator.setItems(itemsMap);
        List<Order> orders = List.of(createOrder("ORD1", 1, LocalDate.now(), List.of(new OrderLine(1, "SKU1", 18)))); // Request 18
        List<Box> inventory = new ArrayList<>(List.of(
                createBox("B1", "SKU1", 5, null, LocalDateTime.now().minusDays(3), "1", "1"),
                createBox("B2", "SKU1", 5, null, LocalDateTime.now().minusDays(2), "1", "2"),
                createBox("B3", "SKU1", 5, null, LocalDateTime.now().minusDays(1), "1", "3"),
                createBox("B4", "SKU1", 5, null, LocalDateTime.now(), "1", "4")
        ));
        AllocationResult result = allocator.allocateOrders(orders, inventory, OrderAllocator.Mode.PARTIAL);

        // Should allocate 5 from B1, 5 from B2, 5 from B3, 3 from B4
        boolean passed = result.allocations.size() == 4 &&
                result.allocations.stream().anyMatch(a -> a.boxId.equals("B1") && a.qty == 5) &&
                result.allocations.stream().anyMatch(a -> a.boxId.equals("B2") && a.qty == 5) &&
                result.allocations.stream().anyMatch(a -> a.boxId.equals("B3") && a.qty == 5) &&
                result.allocations.stream().anyMatch(a -> a.boxId.equals("B4") && a.qty == 3) &&
                result.eligibilityList.get(0).status == Status.ELIGIBLE;

        printResults("Allocated B1(5), B2(5), B3(5), B4(3).", passed ? "Correct" : "Incorrect: " + result.allocations);
        printTestStatus(passed);
        return passed;
    }

    private boolean testSkuNaoExiste() {
        printScenarioHeader("Scenario 15: SKU does not exist in Inventory");
        OrderAllocator allocator = new OrderAllocator();
        allocator.setItems(itemsMap);
        List<Order> orders = List.of(createOrder("ORD1", 1, LocalDate.now(), List.of(new OrderLine(1, "SKU_GHOST", 5))));
        List<Box> inventory = new ArrayList<>(List.of(
                createBox("B1", "SKU1", 10, null, LocalDateTime.now(), "1", "1")
        ));
        AllocationResult result = allocator.allocateOrders(orders, inventory, OrderAllocator.Mode.PARTIAL);

        // Same result as Insufficient Stock
        boolean passed = result.eligibilityList.size() == 1 &&
                result.eligibilityList.get(0).status == Status.UNDISPATCHABLE &&
                result.eligibilityList.get(0).allocatedQty == 0 &&
                result.allocations.isEmpty();
        printResults("Eligibility UNDISPATCHABLE (0/5), Allocations empty.", passed ? "Correct" : "Incorrect: " + result.eligibilityList);
        printTestStatus(passed);
        return passed;
    }


    // --- Helper Methods ---

    private Map<String, Item> createMockItems() {
        Map<String, Item> items = new HashMap<>();
        // Add items used in tests with weights (weight affects USEI03, not USEI02 directly, but good to have)
        items.put("SKU1", new Item("SKU1", "Item SKU1", "Cat A", "unit", 1.5));
        items.put("SKU_P", new Item("SKU_P", "Perishable Item", "Cat P", "unit", 2.0));
        items.put("SKU_NP", new Item("SKU_NP", "Non-Perishable Item", "Cat NP", "unit", 1.0));
        items.put("SKU_MIX", new Item("SKU_MIX", "Mixed Item", "Cat M", "unit", 3.0));
        items.put("SKUA", new Item("SKUA", "Item A", "Cat X", "unit", 0.5));
        items.put("SKUB", new Item("SKUB", "Item B", "Cat Y", "unit", 0.8));
        // Add more SKUs if needed for other tests
        return items;
    }


    private Order createOrder(String id, int priority, LocalDate dueDate, List<OrderLine> lines) {
        Order order = new Order(id, priority, dueDate);
        order.lines.addAll(lines);
        return order;
    }

    // Creates a simple Box
    private Box createBox(String boxId, String sku, int qty, LocalDate expiry, LocalDateTime received, String aisle, String bay) {
        // Creates mutable copy of quantity to simulate consumption
        return new Box(boxId, sku, qty, expiry, received, aisle, bay);
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
        System.out.println("             USEI02 Test Report Summary      ");
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
        System.out.println("             End of USEI02 Test Report        ");
        System.out.println("======================================================");
    }
}