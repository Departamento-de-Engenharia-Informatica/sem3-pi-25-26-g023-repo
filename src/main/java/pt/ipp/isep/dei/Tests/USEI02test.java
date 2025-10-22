package pt.ipp.isep.dei.Tests;

import pt.ipp.isep.dei.domain.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class USEI02test implements Runnable {

    private final Map<String, Boolean> testResults = new HashMap<>();
    private Map<String, Item> itemsMap; // Necessário para pesos

    public static void main(String[] args) {
        new USEI02test().run();
    }

    // Inicializa o mapa de itens antes de correr os testes
    public USEI02test() {
        itemsMap = createMockItems();
    }


    @Override
    public void run() {
        System.out.println("======================================================");
        System.out.println("    Relatório de Testes - USEI02 Order Allocation     ");
        System.out.println("======================================================");

        testResults.put("Cenário 01: Sem Encomendas", testSemEncomendas());
        testResults.put("Cenário 02: Inventário Vazio", testInventarioVazio());
        testResults.put("Cenário 03: Stock Suficiente (Strict)", testStockSuficienteStrict());
        testResults.put("Cenário 04: Stock Insuficiente (Strict)", testStockInsuficienteStrict());
        testResults.put("Cenário 05: Stock Parcial (Strict)", testStockParcialStrict());
        testResults.put("Cenário 06: Stock Suficiente (Partial)", testStockSuficientePartial());
        testResults.put("Cenário 07: Stock Insuficiente (Partial)", testStockInsuficientePartial());
        testResults.put("Cenário 08: Stock Parcial (Partial)", testStockParcialPartial());
        testResults.put("Cenário 09: Prioridade de Encomendas", testPrioridadeEncomendas());
        testResults.put("Cenário 10: Prioridade de Linhas", testPrioridadeLinhas());
        testResults.put("Cenário 11: Alocação FEFO", testAlocacaoFEFO());
        testResults.put("Cenário 12: Alocação FIFO", testAlocacaoFIFO());
        testResults.put("Cenário 13: Alocação Mista FEFO/FIFO", testAlocacaoMista());
        testResults.put("Cenário 14: Alocação entre Múltiplas Caixas", testAlocacaoMultiplasCaixas());
        testResults.put("Cenário 15: SKU não existe no Inventário", testSkuNaoExiste());

        printSummary();
    }

    // --- Cenários de Teste ---

    private boolean testSemEncomendas() {
        printScenarioHeader("Cenário 01: Sem Encomendas");
        OrderAllocator allocator = new OrderAllocator();
        allocator.setItems(itemsMap);
        List<Order> orders = new ArrayList<>();
        List<Box> inventory = List.of(createBox("B1", "SKU1", 10, null, LocalDateTime.now(), "1", "1"));
        AllocationResult result = allocator.allocateOrders(orders, inventory, OrderAllocator.Mode.STRICT);

        boolean passed = result.eligibilityList.isEmpty() && result.allocations.isEmpty();
        printResults("Listas de Eligibility e Allocations devem estar vazias.", passed ? "Sim" : "Não");
        printTestStatus(passed);
        return passed;
    }

    private boolean testInventarioVazio() {
        printScenarioHeader("Cenário 02: Inventário Vazio");
        OrderAllocator allocator = new OrderAllocator();
        allocator.setItems(itemsMap);
        List<Order> orders = List.of(createOrder("ORD1", 1, LocalDate.now(), List.of(new OrderLine(1, "SKU1", 5))));
        List<Box> inventory = new ArrayList<>();
        AllocationResult result = allocator.allocateOrders(orders, inventory, OrderAllocator.Mode.STRICT);

        boolean passed = result.eligibilityList.size() == 1 &&
                result.eligibilityList.get(0).status == Status.UNDISPATCHABLE &&
                result.eligibilityList.get(0).allocatedQty == 0 &&
                result.allocations.isEmpty();
        printResults("Eligibility deve ser UNDISPATCHABLE, Allocations vazias.", passed ? "Correto" : "Incorreto: " + result.eligibilityList);
        printTestStatus(passed);
        return passed;
    }

    private boolean testStockSuficienteStrict() {
        printScenarioHeader("Cenário 03: Stock Suficiente (Strict)");
        OrderAllocator allocator = new OrderAllocator();
        allocator.setItems(itemsMap);
        List<Order> orders = List.of(createOrder("ORD1", 1, LocalDate.now(), List.of(new OrderLine(1, "SKU1", 5))));
        // É importante criar uma CÓPIA MUTÁVEL do inventário para o teste
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
        printResults("Eligibility ELIGIBLE (5/5), 1 Allocation de B1.", passed ? "Correto" : "Incorreto: " + result.eligibilityList + " / " + result.allocations);
        printTestStatus(passed);
        return passed;
    }

    private boolean testStockInsuficienteStrict() {
        printScenarioHeader("Cenário 04: Stock Insuficiente (Strict)");
        OrderAllocator allocator = new OrderAllocator();
        allocator.setItems(itemsMap);
        List<Order> orders = List.of(createOrder("ORD1", 1, LocalDate.now(), List.of(new OrderLine(1, "SKU1", 15))));
        List<Box> inventory = new ArrayList<>(List.of(
                createBox("B1", "SKU1", 10, null, LocalDateTime.now(), "1", "1")
        ));
        AllocationResult result = allocator.allocateOrders(orders, inventory, OrderAllocator.Mode.STRICT);

        boolean passed = result.eligibilityList.size() == 1 &&
                result.eligibilityList.get(0).status == Status.UNDISPATCHABLE &&
                result.eligibilityList.get(0).allocatedQty == 0 && // Strict não aloca nada se não for completo
                result.allocations.isEmpty();
        printResults("Eligibility UNDISPATCHABLE (0/15), Allocations vazias.", passed ? "Correto" : "Incorreto: " + result.eligibilityList + " / " + result.allocations);
        printTestStatus(passed);
        return passed;
    }

    private boolean testStockParcialStrict() {
        printScenarioHeader("Cenário 05: Stock Parcial (Strict)");
        OrderAllocator allocator = new OrderAllocator();
        allocator.setItems(itemsMap);
        List<Order> orders = List.of(createOrder("ORD1", 1, LocalDate.now(), List.of(new OrderLine(1, "SKU1", 10))));
        List<Box> inventory = new ArrayList<>(List.of(
                createBox("B1", "SKU1", 5, null, LocalDateTime.now(), "1", "1")
        ));
        AllocationResult result = allocator.allocateOrders(orders, inventory, OrderAllocator.Mode.STRICT);

        // Mesmo resultado que insuficiente no modo STRICT
        boolean passed = result.eligibilityList.size() == 1 &&
                result.eligibilityList.get(0).status == Status.UNDISPATCHABLE &&
                result.eligibilityList.get(0).allocatedQty == 0 &&
                result.allocations.isEmpty();
        printResults("Eligibility UNDISPATCHABLE (0/10), Allocations vazias.", passed ? "Correto" : "Incorreto: " + result.eligibilityList + " / " + result.allocations);
        printTestStatus(passed);
        return passed;
    }

    private boolean testStockSuficientePartial() {
        printScenarioHeader("Cenário 06: Stock Suficiente (Partial)");
        OrderAllocator allocator = new OrderAllocator();
        allocator.setItems(itemsMap);
        List<Order> orders = List.of(createOrder("ORD1", 1, LocalDate.now(), List.of(new OrderLine(1, "SKU1", 5))));
        List<Box> inventory = new ArrayList<>(List.of(
                createBox("B1", "SKU1", 10, null, LocalDateTime.now(), "1", "1")
        ));
        AllocationResult result = allocator.allocateOrders(orders, inventory, OrderAllocator.Mode.PARTIAL);

        // Mesmo resultado que Strict quando há stock suficiente
        boolean passed = result.eligibilityList.size() == 1 &&
                result.eligibilityList.get(0).status == Status.ELIGIBLE &&
                result.eligibilityList.get(0).allocatedQty == 5 &&
                result.allocations.size() == 1 &&
                result.allocations.get(0).qty == 5;
        printResults("Eligibility ELIGIBLE (5/5), 1 Allocation.", passed ? "Correto" : "Incorreto: " + result.eligibilityList + " / " + result.allocations);
        printTestStatus(passed);
        return passed;
    }

    private boolean testStockInsuficientePartial() {
        printScenarioHeader("Cenário 07: Stock Insuficiente (Partial)");
        OrderAllocator allocator = new OrderAllocator();
        allocator.setItems(itemsMap);
        List<Order> orders = List.of(createOrder("ORD1", 1, LocalDate.now(), List.of(new OrderLine(1, "SKU_NOSTOCK", 10))));
        List<Box> inventory = new ArrayList<>(List.of(
                createBox("B1", "SKU1", 5, null, LocalDateTime.now(), "1", "1") // Outro SKU
        ));
        AllocationResult result = allocator.allocateOrders(orders, inventory, OrderAllocator.Mode.PARTIAL);

        // Mesmo resultado que Strict quando não há stock NENHUM do SKU
        boolean passed = result.eligibilityList.size() == 1 &&
                result.eligibilityList.get(0).status == Status.UNDISPATCHABLE &&
                result.eligibilityList.get(0).allocatedQty == 0 &&
                result.allocations.isEmpty();
        printResults("Eligibility UNDISPATCHABLE (0/10), Allocations vazias.", passed ? "Correto" : "Incorreto: " + result.eligibilityList + " / " + result.allocations);
        printTestStatus(passed);
        return passed;
    }

    private boolean testStockParcialPartial() {
        printScenarioHeader("Cenário 08: Stock Parcial (Partial)");
        OrderAllocator allocator = new OrderAllocator();
        allocator.setItems(itemsMap);
        List<Order> orders = List.of(createOrder("ORD1", 1, LocalDate.now(), List.of(new OrderLine(1, "SKU1", 10))));
        List<Box> inventory = new ArrayList<>(List.of(
                createBox("B1", "SKU1", 7, null, LocalDateTime.now(), "1", "1") // Só 7 unidades
        ));
        AllocationResult result = allocator.allocateOrders(orders, inventory, OrderAllocator.Mode.PARTIAL);

        // Deve alocar o que existe
        boolean passed = result.eligibilityList.size() == 1 &&
                result.eligibilityList.get(0).status == Status.PARTIAL &&
                result.eligibilityList.get(0).allocatedQty == 7 && // Alocou 7
                result.allocations.size() == 1 &&
                result.allocations.get(0).qty == 7 && // Confirma allocation
                result.allocations.get(0).boxId.equals("B1");
        printResults("Eligibility PARTIAL (7/10), 1 Allocation de 7.", passed ? "Correto" : "Incorreto: " + result.eligibilityList + " / " + result.allocations);
        printTestStatus(passed);
        return passed;
    }

    private boolean testPrioridadeEncomendas() {
        printScenarioHeader("Cenário 09: Prioridade de Encomendas");
        OrderAllocator allocator = new OrderAllocator();
        allocator.setItems(itemsMap);
        Order ord1_p2 = createOrder("ORD1", 2, LocalDate.now(), List.of(new OrderLine(1, "SKU1", 5)));
        Order ord2_p1 = createOrder("ORD2", 1, LocalDate.now(), List.of(new OrderLine(1, "SKU1", 5))); // Maior prioridade
        Order ord3_p1_due = createOrder("ORD3", 1, LocalDate.now().minusDays(1), List.of(new OrderLine(1, "SKU1", 5))); // Maior prioridade e Due Date anterior
        List<Order> orders = List.of(ord1_p2, ord2_p1, ord3_p1_due); // Fora de ordem
        List<Box> inventory = new ArrayList<>(List.of(
                createBox("B1", "SKU1", 12, null, LocalDateTime.now(), "1", "1") // Stock para 2.4 encomendas
        ));

        AllocationResult result = allocator.allocateOrders(orders, inventory, OrderAllocator.Mode.STRICT);

        // Verifica se ORD3 e ORD2 foram ELIGIBLE e ORD1 foi UNDISPATCHABLE
        Map<String, Status> statuses = result.eligibilityList.stream()
                .collect(Collectors.toMap(e -> e.orderId, e -> e.status));

        boolean passed = result.eligibilityList.size() == 3 &&
                statuses.getOrDefault("ORD3", null) == Status.ELIGIBLE &&
                statuses.getOrDefault("ORD2", null) == Status.ELIGIBLE &&
                statuses.getOrDefault("ORD1", null) == Status.UNDISPATCHABLE &&
                result.allocations.size() == 2; // Alocou para ORD3 e ORD2

        printResults("Ordem de alocação esperada: ORD3 (Eligible), ORD2 (Eligible), ORD1 (Undispatchable)", passed ? "Correta" : "Incorreta: " + statuses);
        printTestStatus(passed);
        return passed;
    }

    private boolean testPrioridadeLinhas() {
        printScenarioHeader("Cenário 10: Prioridade de Linhas");
        OrderAllocator allocator = new OrderAllocator();
        allocator.setItems(itemsMap);
        Order order = createOrder("ORD1", 1, LocalDate.now(), List.of(
                new OrderLine(2, "SKU1", 5), // Linha 2 primeiro na lista
                new OrderLine(1, "SKU1", 5)  // Linha 1 depois
        ));
        List<Box> inventory = new ArrayList<>(List.of(
                createBox("B1", "SKU1", 7, null, LocalDateTime.now(), "1", "1") // Stock para 1 linha completa + parcial
        ));

        AllocationResult result = allocator.allocateOrders(List.of(order), inventory, OrderAllocator.Mode.PARTIAL);

        // Verifica se a linha 1 foi processada primeiro e ficou ELIGIBLE, e a linha 2 ficou PARTIAL
        Map<Integer, Eligibility> eligMap = result.eligibilityList.stream()
                .collect(Collectors.toMap(e -> e.lineNo, e -> e));

        boolean passed = result.eligibilityList.size() == 2 &&
                eligMap.get(1).status == Status.ELIGIBLE && eligMap.get(1).allocatedQty == 5 &&
                eligMap.get(2).status == Status.PARTIAL && eligMap.get(2).allocatedQty == 2 && // Restante 7-5=2
                result.allocations.size() == 2; // Uma allocation para cada linha

        printResults("Linha 1 ELIGIBLE (5/5), Linha 2 PARTIAL (2/5).", passed ? "Correto" : "Incorreto: " + eligMap);
        printTestStatus(passed);
        return passed;
    }

    private boolean testAlocacaoFEFO() {
        printScenarioHeader("Cenário 11: Alocação FEFO");
        OrderAllocator allocator = new OrderAllocator();
        allocator.setItems(itemsMap);
        List<Order> orders = List.of(createOrder("ORD1", 1, LocalDate.now(), List.of(new OrderLine(1, "SKU_P", 8)))); // Pedido 8
        List<Box> inventory = new ArrayList<>(List.of(
                createBox("B_EXP_DEPOIS", "SKU_P", 5, LocalDate.now().plusDays(10), LocalDateTime.now(), "1", "1"),
                createBox("B_EXP_ANTES", "SKU_P", 5, LocalDate.now().plusDays(5), LocalDateTime.now(), "1", "2") // Deve ser usada primeiro
        ));
        AllocationResult result = allocator.allocateOrders(orders, inventory, OrderAllocator.Mode.PARTIAL);

        // Deve alocar 5 de B_EXP_ANTES e 3 de B_EXP_DEPOIS
        boolean passed = result.allocations.size() == 2 &&
                result.allocations.stream().anyMatch(a -> a.boxId.equals("B_EXP_ANTES") && a.qty == 5) &&
                result.allocations.stream().anyMatch(a -> a.boxId.equals("B_EXP_DEPOIS") && a.qty == 3) &&
                result.eligibilityList.get(0).status == Status.ELIGIBLE;

        printResults("Alocou 5 de B_EXP_ANTES e 3 de B_EXP_DEPOIS.", passed ? "Correto" : "Incorreto: " + result.allocations);
        printTestStatus(passed);
        return passed;
    }

    private boolean testAlocacaoFIFO() {
        printScenarioHeader("Cenário 12: Alocação FIFO");
        OrderAllocator allocator = new OrderAllocator();
        allocator.setItems(itemsMap);
        List<Order> orders = List.of(createOrder("ORD1", 1, LocalDate.now(), List.of(new OrderLine(1, "SKU_NP", 8)))); // Pedido 8
        List<Box> inventory = new ArrayList<>(List.of(
                createBox("B_CHEGOU_DEPOIS", "SKU_NP", 5, null, LocalDateTime.now().minusDays(1), "1", "1"),
                createBox("B_CHEGOU_ANTES", "SKU_NP", 5, null, LocalDateTime.now().minusDays(5), "1", "2") // Deve ser usada primeiro
        ));
        AllocationResult result = allocator.allocateOrders(orders, inventory, OrderAllocator.Mode.PARTIAL);

        // Deve alocar 5 de B_CHEGOU_ANTES e 3 de B_CHEGOU_DEPOIS
        boolean passed = result.allocations.size() == 2 &&
                result.allocations.stream().anyMatch(a -> a.boxId.equals("B_CHEGOU_ANTES") && a.qty == 5) &&
                result.allocations.stream().anyMatch(a -> a.boxId.equals("B_CHEGOU_DEPOIS") && a.qty == 3) &&
                result.eligibilityList.get(0).status == Status.ELIGIBLE;

        printResults("Alocou 5 de B_CHEGOU_ANTES e 3 de B_CHEGOU_DEPOIS.", passed ? "Correto" : "Incorreto: " + result.allocations);
        printTestStatus(passed);
        return passed;
    }

    private boolean testAlocacaoMista() {
        printScenarioHeader("Cenário 13: Alocação Mista FEFO/FIFO");
        OrderAllocator allocator = new OrderAllocator();
        allocator.setItems(itemsMap);
        List<Order> orders = List.of(createOrder("ORD1", 1, LocalDate.now(), List.of(new OrderLine(1, "SKU_MIX", 12)))); // Pedido 12
        List<Box> inventory = new ArrayList<>(List.of(
                createBox("B_FIFO_ANTIGO", "SKU_MIX", 5, null, LocalDateTime.now().minusDays(10), "1", "1"), // FIFO 1
                createBox("B_FEFO_URGENTE", "SKU_MIX", 5, LocalDate.now().plusDays(2), LocalDateTime.now().minusDays(5), "1", "2"), // FEFO 1 (mais urgente)
                createBox("B_FEFO_NORMAL", "SKU_MIX", 5, LocalDate.now().plusDays(20), LocalDateTime.now().minusDays(1), "1", "3"), // FEFO 2
                createBox("B_FIFO_RECENTE", "SKU_MIX", 5, null, LocalDateTime.now().minusDays(2), "1", "4")  // FIFO 2
        ));
        AllocationResult result = allocator.allocateOrders(orders, inventory, OrderAllocator.Mode.PARTIAL);

        // Ordem esperada de alocação: B_FEFO_URGENTE (5), B_FEFO_NORMAL (5), B_FIFO_ANTIGO (2)
        boolean passed = result.allocations.size() == 3 &&
                result.allocations.get(0).boxId.equals("B_FEFO_URGENTE") && result.allocations.get(0).qty == 5 &&
                result.allocations.get(1).boxId.equals("B_FEFO_NORMAL") && result.allocations.get(1).qty == 5 &&
                result.allocations.get(2).boxId.equals("B_FIFO_ANTIGO") && result.allocations.get(2).qty == 2 &&
                result.eligibilityList.get(0).status == Status.ELIGIBLE;

        printResults("Alocou FEFO_URGENTE(5), FEFO_NORMAL(5), FIFO_ANTIGO(2).", passed ? "Correto" : "Incorreto: " + result.allocations);
        printTestStatus(passed);
        return passed;
    }


    private boolean testAlocacaoMultiplasCaixas() {
        printScenarioHeader("Cenário 14: Alocação entre Múltiplas Caixas");
        OrderAllocator allocator = new OrderAllocator();
        allocator.setItems(itemsMap);
        List<Order> orders = List.of(createOrder("ORD1", 1, LocalDate.now(), List.of(new OrderLine(1, "SKU1", 18)))); // Pedido 18
        List<Box> inventory = new ArrayList<>(List.of(
                createBox("B1", "SKU1", 5, null, LocalDateTime.now().minusDays(3), "1", "1"),
                createBox("B2", "SKU1", 5, null, LocalDateTime.now().minusDays(2), "1", "2"),
                createBox("B3", "SKU1", 5, null, LocalDateTime.now().minusDays(1), "1", "3"),
                createBox("B4", "SKU1", 5, null, LocalDateTime.now(), "1", "4")
        ));
        AllocationResult result = allocator.allocateOrders(orders, inventory, OrderAllocator.Mode.PARTIAL);

        // Deve alocar 5 de B1, 5 de B2, 5 de B3, 3 de B4
        boolean passed = result.allocations.size() == 4 &&
                result.allocations.stream().anyMatch(a -> a.boxId.equals("B1") && a.qty == 5) &&
                result.allocations.stream().anyMatch(a -> a.boxId.equals("B2") && a.qty == 5) &&
                result.allocations.stream().anyMatch(a -> a.boxId.equals("B3") && a.qty == 5) &&
                result.allocations.stream().anyMatch(a -> a.boxId.equals("B4") && a.qty == 3) &&
                result.eligibilityList.get(0).status == Status.ELIGIBLE;

        printResults("Alocou B1(5), B2(5), B3(5), B4(3).", passed ? "Correto" : "Incorreto: " + result.allocations);
        printTestStatus(passed);
        return passed;
    }

    private boolean testSkuNaoExiste() {
        printScenarioHeader("Cenário 15: SKU não existe no Inventário");
        OrderAllocator allocator = new OrderAllocator();
        allocator.setItems(itemsMap);
        List<Order> orders = List.of(createOrder("ORD1", 1, LocalDate.now(), List.of(new OrderLine(1, "SKU_FANTASMA", 5))));
        List<Box> inventory = new ArrayList<>(List.of(
                createBox("B1", "SKU1", 10, null, LocalDateTime.now(), "1", "1")
        ));
        AllocationResult result = allocator.allocateOrders(orders, inventory, OrderAllocator.Mode.PARTIAL);

        // Mesmo resultado que Stock Insuficiente
        boolean passed = result.eligibilityList.size() == 1 &&
                result.eligibilityList.get(0).status == Status.UNDISPATCHABLE &&
                result.eligibilityList.get(0).allocatedQty == 0 &&
                result.allocations.isEmpty();
        printResults("Eligibility UNDISPATCHABLE (0/5), Allocations vazias.", passed ? "Correto" : "Incorreto: " + result.eligibilityList);
        printTestStatus(passed);
        return passed;
    }


    // --- Métodos Auxiliares ---

    private Map<String, Item> createMockItems() {
        Map<String, Item> items = new HashMap<>();
        // Adiciona itens usados nos testes com pesos (o peso afeta USEI03, não USEI02 diretamente, mas é bom ter)
        items.put("SKU1", new Item("SKU1", "Item SKU1", "Cat A", "unit", 1.5));
        items.put("SKU_P", new Item("SKU_P", "Item Perecível", "Cat P", "unit", 2.0));
        items.put("SKU_NP", new Item("SKU_NP", "Item Não Perecível", "Cat NP", "unit", 1.0));
        items.put("SKU_MIX", new Item("SKU_MIX", "Item Misto", "Cat M", "unit", 3.0));
        items.put("SKUA", new Item("SKUA", "Item A", "Cat X", "unit", 0.5));
        items.put("SKUB", new Item("SKUB", "Item B", "Cat Y", "unit", 0.8));
        // Adiciona mais SKUs se necessário para outros testes
        return items;
    }


    private Order createOrder(String id, int priority, LocalDate dueDate, List<OrderLine> lines) {
        Order order = new Order(id, priority, dueDate);
        order.lines.addAll(lines);
        return order;
    }

    // Cria um Box simples
    private Box createBox(String boxId, String sku, int qty, LocalDate expiry, LocalDateTime received, String aisle, String bay) {
        // Cria cópia mutável da quantidade para simular consumo
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
            System.out.println("\n  --> Resultado do Cenário: ✅ PASSOU");
        } else {
            System.err.println("\n  --> Resultado do Cenário: ❌ FALHOU");
        }
    }

    private void printSummary() {
        System.out.println("\n======================================================");
        System.out.println("             Sumário do Relatório de Testes USEI02      ");
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
        System.out.println("             Fim do Relatório de Testes USEI02        ");
        System.out.println("======================================================");
    }
}