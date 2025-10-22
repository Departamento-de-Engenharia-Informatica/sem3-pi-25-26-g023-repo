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
    private final String TEST_LOG_FILE = "audit_test_usei05.log"; // Ficheiro de log para os testes

    public static void main(String[] args) {
        new USEI05test().run();
    }

    // Apaga o ficheiro de log antes de cada execução de run()
    private void clearTestLog() {
        try {
            Files.deleteIfExists(Paths.get(TEST_LOG_FILE));
        } catch (IOException e) {
            System.err.println("Aviso: Não foi possível apagar o ficheiro de log de teste: " + TEST_LOG_FILE);
        }
    }


    @Override
    public void run() {
        System.out.println("======================================================");
        System.out.println("   Relatório de Testes - USEI05 Returns & Quarantine   ");
        System.out.println("======================================================");

        clearTestLog(); // Garante um log limpo para cada execução completa dos testes

        testResults.put("Cenário 01: Quarentena Vazia", testQuarentenaVazia());
        testResults.put("Cenário 02: Processar Item Descartado (Damaged)", testItemDescartadoDamaged());
        testResults.put("Cenário 03: Processar Item Descartado (Expired)", testItemDescartadoExpired());
        testResults.put("Cenário 04: Processar Item Restockable (Customer Remorse)", testItemRestockableRemorse());
        testResults.put("Cenário 05: Processar Item Restockable (Cycle Count)", testItemRestockableCycleCount());
        testResults.put("Cenário 06: Processar Múltiplos Itens (LIFO)", testProcessarMultiplosLIFO());
        testResults.put("Cenário 07: Restock Falha (Sem Espaço)", testRestockSemEspaco());
        testResults.put("Cenário 08: Restock com Verificação FEFO/FIFO", testRestockOrdemInventario());

        printSummary();
    }

    // --- Cenários de Teste ---

    private boolean testQuarentenaVazia() {
        printScenarioHeader("Cenário 01: Quarentena Vazia");
        Inventory inventory = new Inventory();
        Quarantine quarantine = new Quarantine();
        AuditLog auditLog = new AuditLog(TEST_LOG_FILE);
        List<Warehouse> warehouses = createWarehousesBasicos(1, 1, 10);
        WMS wms = new WMS(quarantine, inventory, auditLog, warehouses);

        wms.processReturns(); // Executa o processamento

        boolean passed = inventory.getBoxes().isEmpty() && readLogLines(TEST_LOG_FILE).isEmpty();
        printResults("Inventário deve estar vazio.", inventory.getBoxes().isEmpty() ? "Sim" : "Não");
        printResults("Log de auditoria deve estar vazio.", readLogLines(TEST_LOG_FILE).isEmpty() ? "Sim" : "Não");
        printTestStatus(passed);
        return passed;
    }

    private boolean testItemDescartadoDamaged() {
        printScenarioHeader("Cenário 02: Processar Item Descartado (Damaged)");
        Inventory inventory = new Inventory();
        Quarantine quarantine = new Quarantine();
        AuditLog auditLog = new AuditLog(TEST_LOG_FILE);
        List<Warehouse> warehouses = createWarehousesBasicos(1, 1, 10);
        WMS wms = new WMS(quarantine, inventory, auditLog, warehouses);
        Return ret = createReturn("R001", "SKU1", 5, "Damaged", LocalDateTime.now(), null);
        quarantine.addReturn(ret);

        wms.processReturns();

        List<String> logLines = readLogLines(TEST_LOG_FILE);
        boolean passed = inventory.getBoxes().isEmpty() &&
                logLines.size() == 1 &&
                logLines.get(0).contains("returnId=R001") &&
                logLines.get(0).contains("action=Discarded") &&
                logLines.get(0).contains("qty=5");

        printResults("Inventário deve estar vazio.", inventory.getBoxes().isEmpty() ? "Sim" : "Não");
        printResults("Log deve conter 1 linha 'Discarded' para R001.", passed ? "Sim" : "Não: " + logLines);
        printTestStatus(passed);
        return passed;
    }

    private boolean testItemDescartadoExpired() {
        printScenarioHeader("Cenário 03: Processar Item Descartado (Expired)");
        Inventory inventory = new Inventory();
        Quarantine quarantine = new Quarantine();
        AuditLog auditLog = new AuditLog(TEST_LOG_FILE);
        List<Warehouse> warehouses = createWarehousesBasicos(1, 1, 10);
        WMS wms = new WMS(quarantine, inventory, auditLog, warehouses);
        // Nota: A lógica `isRestockable` atual não verifica a data de expiração, apenas a razão.
        // O teste assume que a razão "Expired" é suficiente para descarte.
        Return ret = createReturn("R002", "SKU2", 3, "Expired", LocalDateTime.now(), LocalDateTime.now().minusDays(1));
        quarantine.addReturn(ret);

        wms.processReturns();

        List<String> logLines = readLogLines(TEST_LOG_FILE);
        boolean passed = inventory.getBoxes().isEmpty() &&
                logLines.size() == 1 &&
                logLines.get(0).contains("returnId=R002") &&
                logLines.get(0).contains("action=Discarded") &&
                logLines.get(0).contains("qty=3");

        printResults("Inventário deve estar vazio.", inventory.getBoxes().isEmpty() ? "Sim" : "Não");
        printResults("Log deve conter 1 linha 'Discarded' para R002.", passed ? "Sim" : "Não: " + logLines);
        printTestStatus(passed);
        return passed;
    }

    private boolean testItemRestockableRemorse() {
        printScenarioHeader("Cenário 04: Processar Item Restockable (Customer Remorse)");
        Inventory inventory = new Inventory();
        Quarantine quarantine = new Quarantine();
        AuditLog auditLog = new AuditLog(TEST_LOG_FILE);
        List<Warehouse> warehouses = createWarehousesBasicos(1, 1, 10);
        WMS wms = new WMS(quarantine, inventory, auditLog, warehouses);
        Return ret = createReturn("R003", "SKU3", 7, "Customer Remorse", LocalDateTime.now(), null);
        quarantine.addReturn(ret);

        wms.processReturns();

        List<String> logLines = readLogLines(TEST_LOG_FILE);
        Optional<Box> restockedBox = inventory.getBoxes().stream().filter(b -> b.getBoxId().equals("RET-R003")).findFirst();

        boolean passed = inventory.getBoxes().size() == 1 &&
                restockedBox.isPresent() &&
                restockedBox.get().getSku().equals("SKU3") &&
                restockedBox.get().getQtyAvailable() == 7 &&
                restockedBox.get().getAisle() != null && // Verifica se tem localização
                restockedBox.get().getBay() != null &&
                logLines.size() == 1 &&
                logLines.get(0).contains("returnId=R003") &&
                logLines.get(0).contains("action=Restocked") &&
                logLines.get(0).contains("qty=7");

        printResults("Inventário deve conter 1 caixa 'RET-R003'.", restockedBox.isPresent() ? "Sim" : "Não");
        restockedBox.ifPresent(box -> printResults("Caixa 'RET-R003' tem localização?", (box.getAisle() != null && box.getBay() != null) ? "Sim: "+box.getAisle()+"-"+box.getBay() : "Não"));
        printResults("Log deve conter 1 linha 'Restocked' para R003.", (logLines.size() == 1 && logLines.get(0).contains("Restocked")) ? "Sim" : "Não: " + logLines);
        printTestStatus(passed);
        return passed;
    }

    private boolean testItemRestockableCycleCount() {
        printScenarioHeader("Cenário 05: Processar Item Restockable (Cycle Count)");
        Inventory inventory = new Inventory();
        Quarantine quarantine = new Quarantine();
        AuditLog auditLog = new AuditLog(TEST_LOG_FILE);
        List<Warehouse> warehouses = createWarehousesBasicos(1, 1, 10);
        WMS wms = new WMS(quarantine, inventory, auditLog, warehouses);
        Return ret = createReturn("R004", "SKU4", 4, "Cycle Count", LocalDateTime.now(), LocalDate.now().plusYears(1).atStartOfDay()); // Com expiry futuro
        quarantine.addReturn(ret);

        wms.processReturns();

        List<String> logLines = readLogLines(TEST_LOG_FILE);
        Optional<Box> restockedBox = inventory.getBoxes().stream().filter(b -> b.getBoxId().equals("RET-R004")).findFirst();

        boolean passed = inventory.getBoxes().size() == 1 &&
                restockedBox.isPresent() &&
                restockedBox.get().getSku().equals("SKU4") &&
                restockedBox.get().getQtyAvailable() == 4 &&
                restockedBox.get().getExpiryDate() != null && // Deve ter data de expiração
                restockedBox.get().getAisle() != null &&
                restockedBox.get().getBay() != null &&
                logLines.size() == 1 &&
                logLines.get(0).contains("returnId=R004") &&
                logLines.get(0).contains("action=Restocked") &&
                logLines.get(0).contains("qty=4");

        printResults("Inventário deve conter 1 caixa 'RET-R004'.", restockedBox.isPresent() ? "Sim" : "Não");
        restockedBox.ifPresent(box -> printResults("Caixa 'RET-R004' tem localização e expiry?", (box.getAisle() != null && box.getBay() != null && box.getExpiryDate() != null) ? "Sim" : "Não"));
        printResults("Log deve conter 1 linha 'Restocked' para R004.", (logLines.size() == 1 && logLines.get(0).contains("Restocked")) ? "Sim" : "Não: " + logLines);
        printTestStatus(passed);
        return passed;
    }

    private boolean testProcessarMultiplosLIFO() {
        printScenarioHeader("Cenário 06: Processar Múltiplos Itens (LIFO)");
        Inventory inventory = new Inventory();
        Quarantine quarantine = new Quarantine();
        AuditLog auditLog = new AuditLog(TEST_LOG_FILE);
        List<Warehouse> warehouses = createWarehousesBasicos(1, 1, 10);
        WMS wms = new WMS(quarantine, inventory, auditLog, warehouses);

        Return ret1 = createReturn("R005", "SKU5", 1, "Damaged", LocalDateTime.now().minusMinutes(10), null); // Chegou primeiro
        Return ret2 = createReturn("R006", "SKU6", 2, "Customer Remorse", LocalDateTime.now().minusMinutes(5), null); // Chegou depois
        Return ret3 = createReturn("R007", "SKU7", 3, "Expired", LocalDateTime.now(), null); // Chegou por último

        quarantine.addReturn(ret1);
        quarantine.addReturn(ret2);
        quarantine.addReturn(ret3); // R007 no topo

        wms.processReturns();

        List<String> logLines = readLogLines(TEST_LOG_FILE);
        boolean passed = logLines.size() == 3 &&
                logLines.get(0).contains("returnId=R007") && logLines.get(0).contains("Discarded") && // Primeiro a processar (LIFO)
                logLines.get(1).contains("returnId=R006") && logLines.get(1).contains("Restocked") && // Segundo
                logLines.get(2).contains("returnId=R005") && logLines.get(2).contains("Discarded") && // Último
                inventory.getBoxes().size() == 1 && // Apenas R006 foi restockado
                inventory.getBoxes().get(0).getBoxId().equals("RET-R006");

        printResults("Log deve ter 3 linhas na ordem R007(D), R006(R), R005(D).", passed ? "Sim" : "Não: " + logLines);
        printResults("Inventário deve ter apenas a caixa RET-R006.", (inventory.getBoxes().size() == 1 && inventory.getBoxes().get(0).getBoxId().equals("RET-R006")) ? "Sim" : "Não: "+ inventory.getBoxes());
        printTestStatus(passed);
        return passed;
    }

    private boolean testRestockSemEspaco() {
        printScenarioHeader("Cenário 07: Restock Falha (Sem Espaço)");
        Inventory inventory = new Inventory();
        Quarantine quarantine = new Quarantine();
        AuditLog auditLog = new AuditLog(TEST_LOG_FILE);
        // Warehouse com capacidade apenas para 1 caixa
        List<Warehouse> warehouses = createWarehousesBasicos(1, 1, 1, 1);
        WMS wms = new WMS(quarantine, inventory, auditLog, warehouses);

        // Caixa inicial para encher o armazém
        Box initialBox = createBox("B_INIT", "SKU_INIT", 1, null, LocalDateTime.now().minusDays(1), null, null);
        warehouses.get(0).storeBox(initialBox); // Coloca diretamente no warehouse (não no inventário lógico para este teste)

        Return ret = createReturn("R008", "SKU8", 1, "Customer Remorse", LocalDateTime.now(), null); // Para restockar
        quarantine.addReturn(ret);

        wms.processReturns();

        List<String> logLines = readLogLines(TEST_LOG_FILE);
        // Espera-se que o restock falhe e seja logado como descartado por falta de espaço
        boolean passed = inventory.getBoxes().isEmpty() && // Não deve ter entrado no inventário lógico
                logLines.size() == 1 &&
                logLines.get(0).contains("returnId=R008") &&
                logLines.get(0).contains("Discarded (No Space)"); // Verifica a mensagem específica

        printResults("Inventário deve estar vazio.", inventory.getBoxes().isEmpty() ? "Sim" : "Não");
        printResults("Log deve conter 1 linha 'Discarded (No Space)' para R008.", passed ? "Sim" : "Não: " + logLines);
        printTestStatus(passed);
        return passed;
    }

    private boolean testRestockOrdemInventario() {
        printScenarioHeader("Cenário 08: Restock com Verificação FEFO/FIFO");
        Inventory inventory = new Inventory();
        Quarantine quarantine = new Quarantine();
        AuditLog auditLog = new AuditLog(TEST_LOG_FILE);
        List<Warehouse> warehouses = createWarehousesBasicos(1, 1, 10);
        WMS wms = new WMS(quarantine, inventory, auditLog, warehouses);

        // Caixa existente no inventário
        Box existingBox = createBox("B_EXIST", "SKU9", 5, LocalDate.now().plusDays(10), LocalDateTime.now().minusDays(5), "1", "1");
        inventory.insertBoxFEFO(existingBox); // Adiciona diretamente ao inventário lógico (assume que já tem local)

        // Devolução para restockar que deve ir ANTES da existente (expira antes)
        Return ret_antes = createReturn("R009", "SKU9", 3, "Customer Remorse", LocalDateTime.now(), LocalDate.now().plusDays(5).atStartOfDay());
        // Devolução para restockar que deve ir DEPOIS da existente (expira depois)
        Return ret_depois = createReturn("R010", "SKU9", 2, "Cycle Count", LocalDateTime.now(), LocalDate.now().plusDays(15).atStartOfDay());
        // Devolução não perecível que deve ir DEPOIS de todas as perecíveis (chegou agora)
        Return ret_fifo = createReturn("R011", "SKU9", 4, "Customer Remorse", LocalDateTime.now(), null);


        quarantine.addReturn(ret_fifo);   // Último a entrar (primeiro a sair LIFO)
        quarantine.addReturn(ret_depois); // Penúltimo a entrar
        quarantine.addReturn(ret_antes);  // Primeiro a entrar (último a sair LIFO) - MAS é processado por último

        wms.processReturns(); // Processa na ordem: R009, R010, R011

        List<Box> finalInventory = inventory.getBoxes();
        List<String> finalBoxIds = finalInventory.stream().map(Box::getBoxId).toList();
        // Ordem esperada no inventário após processar R009, R010, R011 (FEFO > FIFO):
        // RET-R009 (exp 5d), B_EXIST (exp 10d), RET-R010 (exp 15d), RET-R011 (null)
        List<String> expectedIds = List.of("RET-R009", "B_EXIST", "RET-R010", "RET-R011");

        boolean passed = finalBoxIds.equals(expectedIds);

        printResults("Ordem esperada no inventário: RET-R009, B_EXIST, RET-R010, RET-R011", passed ? "Correta" : "Incorreta: " + finalBoxIds);
        printTestStatus(passed);
        return passed;
    }


    // --- Métodos Auxiliares ---

    // Cria um Return simples
    private Return createReturn(String id, String sku, int qty, String reason, LocalDateTime timestamp, LocalDateTime expiry) {
        return new Return(id, sku, qty, reason, timestamp, expiry);
    }

    // Cria um Box simples
    private Box createBox(String boxId, String sku, int qty, LocalDate expiry, LocalDateTime received, String aisle, String bay) {
        return new Box(boxId, sku, qty, expiry, received, aisle, bay);
    }

    // Cria warehouses básicos
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
        return createWarehousesBasicos(numWH, numAislesPerWH, 5, bayCapacity); // Default 5 bays
    }


    // Lê as linhas do ficheiro de log (simplificado)
    private List<String> readLogLines(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                return Files.readAllLines(path);
            } else {
                return new ArrayList<>(); // Retorna lista vazia se o ficheiro não existe
            }
        } catch (IOException e) {
            System.err.println("Erro ao ler ficheiro de log " + filePath + ": " + e.getMessage());
            return new ArrayList<>(); // Retorna lista vazia em caso de erro
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
            System.out.println("\n  --> Resultado do Cenário: ✅ PASSOU");
        } else {
            System.err.println("\n  --> Resultado do Cenário: ❌ FALHOU");
        }
    }

    private void printSummary() {
        System.out.println("\n======================================================");
        System.out.println("             Sumário do Relatório de Testes USEI05      ");
        System.out.println("======================================================");
        int passCount = 0;
        int failCount = 0;
        List<String> sortedTestNames = new ArrayList<>(testResults.keySet());
        Collections.sort(sortedTestNames);

        for (String testName : sortedTestNames) {
            Boolean resultValue = testResults.get(testName);
            boolean passed = resultValue != null && resultValue;
            String result = passed ? "✅ PASSOU" : "❌ FALHOU";
            System.out.printf("  %s: %s%n", testName, result);
            if (passed) {
                passCount++;
            } else {
                failCount++;
            }
        }
        System.out.println("------------------------------------------------------");
        System.out.printf("  Total: %d Passaram, %d Falharam%n", passCount, failCount);
        System.out.println("======================================================");
        System.out.println("             Fim do Relatório de Testes USEI05        ");
        System.out.println("======================================================");
    }

}