package pt.ipp.isep.dei.Tests;

import pt.ipp.isep.dei.domain.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Test harness for USEI05: Quarantine and Returns Processing.
 * <p>
 * This class implements {@link Runnable} to execute test scenarios for the
 * {@link WMS#processReturns()} method. It validates that items in the
 * {@link Quarantine} queue are processed correctly (LIFO order).
 * </p>
 * It tests:
 * <ul>
 * <li>Reasons for "Discarded" (Damaged, Expired).</li>
 * <li>Reasons for "Restocked" (Customer Remorse, Cycle Count).</li>
 * <li>Restock failure (e.g., no warehouse space).</li>
 * <li>Correct insertion of restocked items into the {@link Inventory} (FEFO/FIFO).</li>
 * <li>Audit logging for each action.</li>
 * </ul>
 */
public class USEI05test implements Runnable {

    /** Stores the results of each test scenario (Scenario Name -> Pass/Fail). */
    private final Map<String, Boolean> testResults = new HashMap<>();
    /** Counter to ensure unique log file names for each test. */
    private int testCounter = 0;

    /**
     * Main entry point for the test runner.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        new USEI05test().run();
    }

    /**
     * Generates a unique log file name for the current test.
     *
     * @return A unique file name (e.g., "audit_test_usei05_1.log").
     */
    private String getUniqueLogFile() {
        testCounter++;
        return "audit_test_usei05_" + testCounter + ".log";
    }

    /**
     * Deletes the specified log file to ensure a clean state for the test.
     *
     * @param logFile The path to the log file to delete.
     */
    private void clearTestLog(String logFile) {
        try {
            Files.deleteIfExists(Paths.get(logFile));
        } catch (IOException e) {
            System.err.println("Warning: Could not delete test log file: " + logFile);
        }
    }

    /**
     * Orchestrates the execution of all test scenarios.
     * It calls each test method, stores its boolean result in the
     * {@code testResults} map, prints a summary, and cleans up log files.
     */
    @Override
    public void run() {
        System.out.println("======================================================");
        System.out.println("   Test Report - USEI05 Returns & Quarantine   ");
        System.out.println("======================================================");

        testResults.put("Scenario 01: Empty Quarantine", testQuarentenaVazia());
        testResults.put("Scenario 02: Process Discarded Item (Damaged)", testItemDescartadoDamaged());
        testResults.put("Scenario 03: Process Discarded Item (Expired)", testItemDescartadoExpired());
        testResults.put("Scenario 04: Process Restockable Item (Customer Remorse)", testItemRestockableRemorse());
        testResults.put("Scenario 05: Process Restockable Item (Cycle Count)", testItemRestockableCycleCount());
        testResults.put("Scenario 06: Process Multiple Items (LIFO)", testProcessarMultiplosLIFO());
        testResults.put("Scenario 07: Restock Failure (No Space)", testRestockSemEspaco());
        testResults.put("Scenario 08: Restock with FEFO/FIFO Check", testRestockOrdemInventario());

        printSummary();

        // Cleans up all test log files at the end
        cleanupTestLogs();
    }

    // --- Test Scenarios ---

    /**
     * Scenario 01: Tests processing an empty quarantine queue.
     * Expects no change to inventory and no log entries.
     *
     * @return {@code true} if the test passes, {@code false} otherwise.
     */
    private boolean testQuarentenaVazia() {
        printScenarioHeader("Scenario 01: Empty Quarantine");
        String logFile = getUniqueLogFile();
        clearTestLog(logFile);

        Inventory inventory = new Inventory();
        Quarantine quarantine = new Quarantine();
        AuditLog auditLog = new AuditLog(logFile);
        List<Warehouse> warehouses = createWarehousesBasicos(1, 1, 10);
        WMS wms = new WMS(quarantine, inventory, auditLog, warehouses);

        wms.processReturns();

        boolean passed = inventory.getBoxes().isEmpty() && readLogLines(logFile).isEmpty();
        printResults("Inventory should be empty.", inventory.getBoxes().isEmpty() ? "Yes" : "No");
        printResults("Audit log should be empty.", readLogLines(logFile).isEmpty() ? "Yes" : "No");
        printTestStatus(passed);
        return passed;
    }

    /**
     * Scenario 02: Tests a return with reason "Damaged".
     * Expects the item to be "Discarded" and logged, with no inventory change.
     *
     * @return {@code true} if the test passes, {@code false} otherwise.
     */
    private boolean testItemDescartadoDamaged() {
        printScenarioHeader("Scenario 02: Process Discarded Item (Damaged)");
        String logFile = getUniqueLogFile();
        clearTestLog(logFile);

        Inventory inventory = new Inventory();
        Quarantine quarantine = new Quarantine();
        AuditLog auditLog = new AuditLog(logFile);
        List<Warehouse> warehouses = createWarehousesBasicos(1, 1, 10);
        WMS wms = new WMS(quarantine, inventory, auditLog, warehouses);
        Return ret = createReturn("R001", "SKU1", 5, "Damaged", LocalDateTime.now(), null);
        quarantine.addReturn(ret);

        wms.processReturns();

        List<String> logLines = readLogLines(logFile);
        boolean passed = inventory.getBoxes().isEmpty() &&
                logLines.size() == 1 &&
                logLines.get(0).contains("returnId=R001") &&
                logLines.get(0).contains("action=Discarded") &&
                logLines.get(0).contains("qty=5");

        printResults("Inventory should be empty.", inventory.getBoxes().isEmpty() ? "Yes" : "No");
        printResults("Log should contain 1 'Discarded' line for R001.", passed ? "Yes" : "No: " + logLines);
        printTestStatus(passed);
        return passed;
    }

    /**
     * Scenario 03: Tests a return with reason "Expired".
     * Expects the item to be "Discarded" and logged.
     *
     * @return {@code true} if the test passes, {@code false} otherwise.
     */
    private boolean testItemDescartadoExpired() {
        printScenarioHeader("Scenario 03: Process Discarded Item (Expired)");
        String logFile = getUniqueLogFile();
        clearTestLog(logFile);

        Inventory inventory = new Inventory();
        Quarantine quarantine = new Quarantine();
        AuditLog auditLog = new AuditLog(logFile);
        List<Warehouse> warehouses = createWarehousesBasicos(1, 1, 10);
        WMS wms = new WMS(quarantine, inventory, auditLog, warehouses);
        Return ret = createReturn("R002", "SKU2", 3, "Expired", LocalDateTime.now(), LocalDateTime.now().minusDays(1));
        quarantine.addReturn(ret);

        wms.processReturns();

        List<String> logLines = readLogLines(logFile);
        boolean passed = inventory.getBoxes().isEmpty() &&
                logLines.size() == 1 &&
                logLines.get(0).contains("returnId=R002") &&
                logLines.get(0).contains("action=Discarded") &&
                logLines.get(0).contains("qty=3");

        printResults("Inventory should be empty.", inventory.getBoxes().isEmpty() ? "Yes" : "No");
        printResults("Log should contain 1 'Discarded' line for R002.", passed ? "Yes" : "No: " + logLines);
        printTestStatus(passed);
        return passed;
    }

    /**
     * Scenario 04: Tests a return with reason "Customer Remorse".
     * Expects the item to be "Restocked" as a new box in inventory and logged.
     *
     * @return {@code true} if the test passes, {@code false} otherwise.
     */
    private boolean testItemRestockableRemorse() {
        printScenarioHeader("Scenario 04: Process Restockable Item (Customer Remorse)");
        String logFile = getUniqueLogFile();
        clearTestLog(logFile);

        Inventory inventory = new Inventory();
        Quarantine quarantine = new Quarantine();
        AuditLog auditLog = new AuditLog(logFile);
        List<Warehouse> warehouses = createWarehousesBasicos(1, 1, 10);
        WMS wms = new WMS(quarantine, inventory, auditLog, warehouses);
        Return ret = createReturn("R003", "SKU3", 7, "Customer Remorse", LocalDateTime.now(), null);
        quarantine.addReturn(ret);

        wms.processReturns();

        List<String> logLines = readLogLines(logFile);
        Optional<Box> restockedBox = inventory.getBoxes().stream().filter(b -> b.getBoxId().equals("RET-R003")).findFirst();

        boolean passed = inventory.getBoxes().size() == 1 &&
                restockedBox.isPresent() &&
                restockedBox.get().getSku().equals("SKU3") &&
                restockedBox.get().getQtyAvailable() == 7 &&
                restockedBox.get().getAisle() != null &&
                restockedBox.get().getBay() != null &&
                logLines.size() == 1 &&
                logLines.get(0).contains("returnId=R003") &&
                logLines.get(0).contains("action=Restocked") &&
                logLines.get(0).contains("qty=7");

        printResults("Inventory should contain 1 box 'RET-R003'.", restockedBox.isPresent() ? "Yes" : "No");
        restockedBox.ifPresent(box -> printResults("Box 'RET-R003' has location?", (box.getAisle() != null && box.getBay() != null) ? "Yes: "+box.getAisle()+"-"+box.getBay() : "No"));
        printResults("Log should contain 1 'Restocked' line for R003.", (logLines.size() == 1 && logLines.get(0).contains("Restocked")) ? "Yes" : "No: " + logLines);
        printTestStatus(passed);
        return passed;
    }

    /**
     * Scenario 05: Tests a return with reason "Cycle Count".
     * Expects the item to be "Restocked" with its original expiry date.
     *
     * @return {@code true} if the test passes, {@code false} otherwise.
     */
    private boolean testItemRestockableCycleCount() {
        printScenarioHeader("Scenario 05: Process Restockable Item (Cycle Count)");
        String logFile = getUniqueLogFile();
        clearTestLog(logFile);

        Inventory inventory = new Inventory();
        Quarantine quarantine = new Quarantine();
        AuditLog auditLog = new AuditLog(logFile);
        List<Warehouse> warehouses = createWarehousesBasicos(1, 1, 10);
        WMS wms = new WMS(quarantine, inventory, auditLog, warehouses);
        Return ret = createReturn("R004", "SKU4", 4, "Cycle Count", LocalDateTime.now(), LocalDate.now().plusYears(1).atStartOfDay());
        quarantine.addReturn(ret);

        wms.processReturns();

        List<String> logLines = readLogLines(logFile);
        Optional<Box> restockedBox = inventory.getBoxes().stream().filter(b -> b.getBoxId().equals("RET-R004")).findFirst();

        boolean passed = inventory.getBoxes().size() == 1 &&
                restockedBox.isPresent() &&
                restockedBox.get().getSku().equals("SKU4") &&
                restockedBox.get().getQtyAvailable() == 4 &&
                restockedBox.get().getExpiryDate() != null &&
                restockedBox.get().getAisle() != null &&
                restockedBox.get().getBay() != null &&
                logLines.size() == 1 &&
                logLines.get(0).contains("returnId=R004") &&
                logLines.get(0).contains("action=Restocked") &&
                logLines.get(0).contains("qty=4");

        printResults("Inventory should contain 1 box 'RET-R004'.", restockedBox.isPresent() ? "Yes" : "No");
        restockedBox.ifPresent(box -> printResults("Box 'RET-R004' has location and expiry?", (box.getAisle() != null && box.getBay() != null && box.getExpiryDate() != null) ? "Yes" : "No"));
        printResults("Log should contain 1 'Restocked' line for R004.", (logLines.size() == 1 && logLines.get(0).contains("Restocked")) ? "Yes" : "No: " + logLines);
        printTestStatus(passed);
        return passed;
    }

    /**
     * Scenario 06: Tests processing multiple items from quarantine.
     * Expects LIFO (Last-In, First-Out) processing order.
     *
     * @return {@code true} if the test passes, {@code false} otherwise.
     */
    private boolean testProcessarMultiplosLIFO() {
        printScenarioHeader("Scenario 06: Process Multiple Items (LIFO)");
        String logFile = getUniqueLogFile();
        clearTestLog(logFile);

        Inventory inventory = new Inventory();
        Quarantine quarantine = new Quarantine();
        AuditLog auditLog = new AuditLog(logFile);
        List<Warehouse> warehouses = createWarehousesBasicos(1, 1, 10);
        WMS wms = new WMS(quarantine, inventory, auditLog, warehouses);

        Return ret1 = createReturn("R005", "SKU5", 1, "Damaged", LocalDateTime.now().minusMinutes(10), null);
        Return ret2 = createReturn("R006", "SKU6", 2, "Customer Remorse", LocalDateTime.now().minusMinutes(5), null);
        Return ret3 = createReturn("R007", "SKU7", 3, "Expired", LocalDateTime.now(), null);

        quarantine.addReturn(ret1); // Added first
        quarantine.addReturn(ret2); // Added second
        quarantine.addReturn(ret3); // Added last

        wms.processReturns(); // Should process R007, then R006, then R005

        List<String> logLines = readLogLines(logFile);
        boolean passed = logLines.size() == 3 &&
                logLines.get(0).contains("returnId=R007") && logLines.get(0).contains("Discarded") &&
                logLines.get(1).contains("returnId=R006") && logLines.get(1).contains("Restocked") &&
                logLines.get(2).contains("returnId=R005") && logLines.get(2).contains("Discarded") &&
                inventory.getBoxes().size() == 1 &&
                inventory.getBoxes().get(0).getBoxId().equals("RET-R006");

        printResults("Log should have 3 lines in order R007(D), R006(R), R005(D).", passed ? "Yes" : "No: " + logLines);
        printResults("Inventory should only have box RET-R006.", (inventory.getBoxes().size() == 1 && inventory.getBoxes().get(0).getBoxId().equals("RET-R006")) ? "Yes" : "No: "+ inventory.getBoxes());
        printTestStatus(passed);
        return passed;
    }

    /**
     * Scenario 07: Tests a restockable item when the warehouse is full.
     * Expects the item to be "Discarded (No Space)" and logged.
     *
     * @return {@code true} if the test passes, {@code false} otherwise.
     */
    private boolean testRestockSemEspaco() {
        printScenarioHeader("Scenario 07: Restock Failure (No Space)");
        String logFile = getUniqueLogFile();
        clearTestLog(logFile);

        Inventory inventory = new Inventory();
        Quarantine quarantine = new Quarantine();
        AuditLog auditLog = new AuditLog(logFile);
        List<Warehouse> warehouses = createWarehousesBasicos(1, 1, 1, 1); // 1 WH, 1 Bay, 1 Capacity
        WMS wms = new WMS(quarantine, inventory, auditLog, warehouses);

        // Fill the only available slot
        Box initialBox = createBox("B_INIT", "SKU_INIT", 1, null, LocalDateTime.now().minusDays(1), null, null);
        warehouses.get(0).storeBox(initialBox); // Manually place box, bypassing inventory

        Return ret = createReturn("R008", "SKU8", 1, "Customer Remorse", LocalDateTime.now(), null);
        quarantine.addReturn(ret);

        wms.processReturns();

        List<String> logLines = readLogLines(logFile);
        boolean passed = inventory.getBoxes().isEmpty() && // Inventory remains empty (B_INIT was not added to it)
                logLines.size() == 1 &&
                logLines.get(0).contains("returnId=R008") &&
                logLines.get(0).contains("Discarded (No Space)");

        printResults("Inventory should be empty.", inventory.getBoxes().isEmpty() ? "Yes" : "No");
        printResults("Log should contain 1 'Discarded (No Space)' line for R008.", passed ? "Yes" : "No: " + logLines);
        printTestStatus(passed);
        return passed;
    }

    /**
     * Scenario 08: Tests if restocked items are inserted into the inventory
     * in the correct FEFO/FIFO order.
     *
     * @return {@code true} if the test passes, {@code false} otherwise.
     */
    private boolean testRestockOrdemInventario() {
        printScenarioHeader("Scenario 08: Restock with FEFO/FIFO Check");
        String logFile = getUniqueLogFile();
        clearTestLog(logFile);

        Inventory inventory = new Inventory();
        Quarantine quarantine = new Quarantine();
        AuditLog auditLog = new AuditLog(logFile);
        List<Warehouse> warehouses = createWarehousesBasicos(1, 1, 10);
        WMS wms = new WMS(quarantine, inventory, auditLog, warehouses);

        // Add an existing box
        Box existingBox = createBox("B_EXIST", "SKU9", 5, LocalDate.now().plusDays(10), LocalDateTime.now().minusDays(5), "1", "1");
        inventory.insertBoxFEFO(existingBox); // Manually insert into inventory

        // Create returns
        Return ret_antes = createReturn("R009", "SKU9", 3, "Customer Remorse", LocalDateTime.now(), LocalDate.now().plusDays(5).atStartOfDay()); // Expires before B_EXIST
        Return ret_depois = createReturn("R010", "SKU9", 2, "Cycle Count", LocalDateTime.now(), LocalDate.now().plusDays(15).atStartOfDay()); // Expires after B_EXIST
        Return ret_fifo = createReturn("R011", "SKU9", 4, "Customer Remorse", LocalDateTime.now(), null); // No expiry, goes last

        // Add to quarantine in LIFO order
        quarantine.addReturn(ret_fifo);
        quarantine.addReturn(ret_depois);
        quarantine.addReturn(ret_antes);

        wms.processReturns(); // Processes R009, then R010, then R011

        List<Box> finalInventory = inventory.getBoxes();
        List<String> finalBoxIds = finalInventory.stream().map(Box::getBoxId).toList();
        List<String> expectedIds = List.of("RET-R009", "B_EXIST", "RET-R010", "RET-R011");

        boolean passed = finalBoxIds.equals(expectedIds);

        printResults("Expected order in inventory: RET-R009, B_EXIST, RET-R010, RET-R011", passed ? "Correct" : "Incorrect: " + finalBoxIds);
        printTestStatus(passed);
        return passed;
    }

    // --- Helper Methods ---

    /**
     * Helper method to create a {@link Return} instance for testing.
     *
     * @param id        The return ID.
     * @param sku       The item SKU.
     * @param qty       The quantity.
     * @param reason    The reason for the return.
     * @param timestamp The timestamp of the return.
     * @param expiry    The expiry date (can be {@code null}).
     * @return A new {@link Return} object.
     */
    private Return createReturn(String id, String sku, int qty, String reason, LocalDateTime timestamp, LocalDateTime expiry) {
        return new Return(id, sku, qty, reason, timestamp, expiry);
    }

    /**
     * Helper method to create a {@link Box} instance for testing.
     *
     * @param boxId    The box identifier.
     * @param sku      The item SKU.
     * @param qty      The quantity.
     * @param expiry   The expiry date (can be {@code null}).
     * @param received The reception timestamp.
     * @param aisle    The aisle (can be {@code null}).
     * @param bay      The bay (can be {@code null}).
     * @return A new {@link Box} object.
     */
    private Box createBox(String boxId, String sku, int qty, LocalDate expiry, LocalDateTime received, String aisle, String bay) {
        return new Box(boxId, sku, qty, expiry, received, aisle, bay);
    }

    /**
     * Helper method to create a basic warehouse structure for testing.
     *
     * @param numWH           Number of warehouses to create.
     * @param numAislesPerWH  Number of aisles per warehouse.
     * @param numBaysPerAisle Number of bays per aisle.
     * @param bayCapacity     The capacity (in boxes) of each bay.
     * @return A list of configured {@link Warehouse} objects.
     */
    private List<Warehouse> createWarehousesBasicos(int numWH, int numAislesPerWH, int numBaysPerAisle, int bayCapacity) {
        List<Warehouse> warehouses = new ArrayList<>();
        for (int i = 1; i <= numWH; i++) {
            Warehouse wh = new Warehouse("W" + i);
            for (int j = 1; j <= numAislesPerWH; j++) {
                for (int k = 1; k <= numBaysPerAisle; k++) {
                    wh.addBay(new Bay("W" + i, j, k, bayCapacity));
                }
            }
            warehouses.add(wh);
        }
        return warehouses;
    }

    /**
     * Overloaded helper method to create a basic warehouse structure with a default
     * number of bays (5) per aisle.
     *
     * @param numWH          Number of warehouses.
     * @param numAislesPerWH Number of aisles per warehouse.
     * @param bayCapacity    The capacity of each bay.
     * @return A list of configured {@link Warehouse} objects.
     */
    private List<Warehouse> createWarehousesBasicos(int numWH, int numAislesPerWH, int bayCapacity) {
        return createWarehousesBasicos(numWH, numAislesPerWH, 5, bayCapacity);
    }

    /**
     * Helper method to read all lines from a specified log file.
     *
     * @param filePath The path to the log file.
     * @return A list of strings (lines from the file), or an empty list if an error occurs.
     */
    private List<String> readLogLines(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                return Files.readAllLines(path);
            } else {
                return new ArrayList<>();
            }
        } catch (IOException e) {
            System.err.println("Error reading log file " + filePath + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Deletes all log files created by this test run (e.g., "audit_test_usei05_1.log", ...).
     */
    private void cleanupTestLogs() {
        for (int i = 1; i <= testCounter; i++) {
            String logFile = "audit_test_usei05_" + i + ".log";
            try {
                Files.deleteIfExists(Paths.get(logFile));
            } catch (IOException e) {
                // Ignore cleanup errors
            }
        }
    }

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
     * Helper method to print a specific assertion or result line.
     *
     * @param description The description of the check.
     * @param result      The outcome of the check.
     */
    private void printResults(String description, Object result) {
        System.out.printf("    - %s: %s%n", description, result.toString());
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
        System.out.println("             USEI05 Test Report Summary      ");
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
        System.out.println("             End of USEI05 Test Report        ");
        System.out.println("======================================================");
    }
}