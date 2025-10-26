package pt.ipp.isep.dei.Tests;

import pt.ipp.isep.dei.domain.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class USEI05test implements Runnable {

    private final Map<String, Boolean> testResults = new HashMap<>();
    private int testCounter = 0;

    public static void main(String[] args) {
        new USEI05test().run();
    }

    // Generates a unique log file name for each test
    private String getUniqueLogFile() {
        testCounter++;
        return "audit_test_usei05_" + testCounter + ".log";
    }

    // Deletes the specific log file
    private void clearTestLog(String logFile) {
        try {
            Files.deleteIfExists(Paths.get(logFile));
        } catch (IOException e) {
            System.err.println("Warning: Could not delete test log file: " + logFile);
        }
    }

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

        quarantine.addReturn(ret1);
        quarantine.addReturn(ret2);
        quarantine.addReturn(ret3);

        wms.processReturns();

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

    private boolean testRestockSemEspaco() {
        printScenarioHeader("Scenario 07: Restock Failure (No Space)");
        String logFile = getUniqueLogFile();
        clearTestLog(logFile);

        Inventory inventory = new Inventory();
        Quarantine quarantine = new Quarantine();
        AuditLog auditLog = new AuditLog(logFile);
        List<Warehouse> warehouses = createWarehousesBasicos(1, 1, 1, 1);
        WMS wms = new WMS(quarantine, inventory, auditLog, warehouses);

        Box initialBox = createBox("B_INIT", "SKU_INIT", 1, null, LocalDateTime.now().minusDays(1), null, null);
        warehouses.get(0).storeBox(initialBox);

        Return ret = createReturn("R008", "SKU8", 1, "Customer Remorse", LocalDateTime.now(), null);
        quarantine.addReturn(ret);

        wms.processReturns();

        List<String> logLines = readLogLines(logFile);
        boolean passed = inventory.getBoxes().isEmpty() &&
                logLines.size() == 1 &&
                logLines.get(0).contains("returnId=R008") &&
                logLines.get(0).contains("Discarded (No Space)");

        printResults("Inventory should be empty.", inventory.getBoxes().isEmpty() ? "Yes" : "No");
        printResults("Log should contain 1 'Discarded (No Space)' line for R008.", passed ? "Yes" : "No: " + logLines);
        printTestStatus(passed);
        return passed;
    }

    private boolean testRestockOrdemInventario() {
        printScenarioHeader("Scenario 08: Restock with FEFO/FIFO Check");
        String logFile = getUniqueLogFile();
        clearTestLog(logFile);

        Inventory inventory = new Inventory();
        Quarantine quarantine = new Quarantine();
        AuditLog auditLog = new AuditLog(logFile);
        List<Warehouse> warehouses = createWarehousesBasicos(1, 1, 10);
        WMS wms = new WMS(quarantine, inventory, auditLog, warehouses);

        Box existingBox = createBox("B_EXIST", "SKU9", 5, LocalDate.now().plusDays(10), LocalDateTime.now().minusDays(5), "1", "1");
        inventory.insertBoxFEFO(existingBox);

        Return ret_antes = createReturn("R009", "SKU9", 3, "Customer Remorse", LocalDateTime.now(), LocalDate.now().plusDays(5).atStartOfDay());
        Return ret_depois = createReturn("R010", "SKU9", 2, "Cycle Count", LocalDateTime.now(), LocalDate.now().plusDays(15).atStartOfDay());
        Return ret_fifo = createReturn("R011", "SKU9", 4, "Customer Remorse", LocalDateTime.now(), null);

        quarantine.addReturn(ret_fifo);
        quarantine.addReturn(ret_depois);
        quarantine.addReturn(ret_antes);

        wms.processReturns();

        List<Box> finalInventory = inventory.getBoxes();
        List<String> finalBoxIds = finalInventory.stream().map(Box::getBoxId).toList();
        List<String> expectedIds = List.of("RET-R009", "B_EXIST", "RET-R010", "RET-R011");

        boolean passed = finalBoxIds.equals(expectedIds);

        printResults("Expected order in inventory: RET-R009, B_EXIST, RET-R010, RET-R011", passed ? "Correct" : "Incorrect: " + finalBoxIds);
        printTestStatus(passed);
        return passed;
    }

    // --- Helper Methods ---

    private Return createReturn(String id, String sku, int qty, String reason, LocalDateTime timestamp, LocalDateTime expiry) {
        return new Return(id, sku, qty, reason, timestamp, expiry);
    }

    private Box createBox(String boxId, String sku, int qty, LocalDate expiry, LocalDateTime received, String aisle, String bay) {
        return new Box(boxId, sku, qty, expiry, received, aisle, bay);
    }

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

    private List<Warehouse> createWarehousesBasicos(int numWH, int numAislesPerWH, int bayCapacity) {
        return createWarehousesBasicos(numWH, numAislesPerWH, 5, bayCapacity);
    }

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