package pt.ipp.isep.dei.Tests;

import pt.ipp.isep.dei.domain.*;

import java.util.*;
import java.util.stream.Collectors;

public class USEI03test implements Runnable {

    private final Map<String, Boolean> testResults = new HashMap<>();
    private Map<String, Item> itemsMap; // Para calcular pesos

    public static void main(String[] args) {
        new USEI03test().run();
    }

    public USEI03test() {
        itemsMap = createMockItems(); // Usa o mesmo mock de itens da USEI02test
    }

    @Override
    public void run() {
        System.out.println("======================================================");
        System.out.println("     Relatório de Testes - USEI03 Picking Plan         ");
        System.out.println("======================================================");

        testResults.put("Cenário 01: Sem Alocações", testSemAlocacoes());
        testResults.put("Cenário 02: Heurística First Fit (FF)", testFirstFit());
        testResults.put("Cenário 03: Heurística First Fit Decreasing (FFD)", testFirstFitDecreasing());
        testResults.put("Cenário 04: Heurística Best Fit Decreasing (BFD)", testBestFitDecreasing());
        testResults.put("Cenário 05: Item Excede Capacidade Total", testItemExcedeCapacidade());
        testResults.put("Cenário 06: Múltiplos Trolleys Necessários", testMultiplosTrolleys());
        testResults.put("Cenário 07: Utilização Perfeita (BFD)", testUtilizacaoPerfeitaBFD());


        printSummary();
    }

    // --- Cenários de Teste ---

    private boolean testSemAlocacoes() {
        printScenarioHeader("Cenário 01: Sem Alocações");
        PickingService service = new PickingService();
        service.setItemsMap(itemsMap);
        List<Allocation> allocations = new ArrayList<>();
        PickingPlan plan = service.generatePickingPlan(allocations, 100.0, HeuristicType.FIRST_FIT);

        boolean passed = plan != null && plan.getTrolleys().isEmpty() && plan.getTotalTrolleys() == 0;
        printResults("Plano gerado não deve ter trolleys.", passed ? "Correto" : "Incorreto: " + plan.getTotalTrolleys() + " trolleys");
        printTestStatus(passed);
        return passed;
    }

    private boolean testFirstFit() {
        printScenarioHeader("Cenário 02: Heurística First Fit (FF)");
        PickingService service = new PickingService();
        service.setItemsMap(itemsMap);
        // Pesos: A=15, B=40, C=25, D=30, E=10 (Total=120)
        List<Allocation> allocations = List.of(
                createAllocation("O1", 1, "SKUA", 10, "B1", "1", "1"), // 10 * 1.5 = 15kg
                createAllocation("O1", 2, "SKUB", 20, "B2", "1", "2"), // 20 * 2.0 = 40kg
                createAllocation("O1", 3, "SKUC", 10, "B3", "1", "3"), // 10 * 2.5 = 25kg
                createAllocation("O1", 4, "SKUD", 15, "B4", "1", "4"), // 15 * 2.0 = 30kg
                createAllocation("O1", 5, "SKUE", 5, "B5", "1", "5")   // 5 * 2.0 = 10kg
        );
        double capacity = 60.0;
        PickingPlan plan = service.generatePickingPlan(allocations, capacity, HeuristicType.FIRST_FIT);

        // FF Esperado (Cap=60):
        // T1: A(15) + B(40) = 55
        // T2: C(25) + D(30) = 55
        // T3: E(10) = 10
        boolean passed = plan.getTotalTrolleys() == 3 &&
                checkTrolleyContent(plan, "T1", List.of("SKUA", "SKUB"), 55.0) &&
                checkTrolleyContent(plan, "T2", List.of("SKUC", "SKUD"), 55.0) &&
                checkTrolleyContent(plan, "T3", List.of("SKUE"), 10.0);

        printResults("Número de trolleys esperado: 3", plan.getTotalTrolleys() == 3 ? "Correto" : "Incorreto: " + plan.getTotalTrolleys());
        printPlanDetails(plan);
        printTestStatus(passed);
        return passed;
    }

    private boolean testFirstFitDecreasing() {
        printScenarioHeader("Cenário 03: Heurística First Fit Decreasing (FFD)");
        PickingService service = new PickingService();
        service.setItemsMap(itemsMap);
        // Mesmas alocações, pesos: B=40, D=30, C=25, A=15, E=10 (Total=120)
        List<Allocation> allocations = List.of(
                createAllocation("O1", 1, "SKUA", 10, "B1", "1", "1"), // 15kg
                createAllocation("O1", 2, "SKUB", 20, "B2", "1", "2"), // 40kg
                createAllocation("O1", 3, "SKUC", 10, "B3", "1", "3"), // 25kg
                createAllocation("O1", 4, "SKUD", 15, "B4", "1", "4"), // 30kg
                createAllocation("O1", 5, "SKUE", 5, "B5", "1", "5")   // 10kg
        );
        double capacity = 60.0;
        PickingPlan plan = service.generatePickingPlan(allocations, capacity, HeuristicType.FIRST_FIT_DECREASING);

        // FFD Esperado (Ordem: B, D, C, A, E) (Cap=60):
        // T1: B(40) + A(15) = 55
        // T2: D(30) + C(25) = 55
        // T3: E(10) = 10
        boolean passed = plan.getTotalTrolleys() == 3 &&
                checkTrolleyContent(plan, "T1", List.of("SKUB", "SKUA"), 55.0) &&
                checkTrolleyContent(plan, "T2", List.of("SKUD", "SKUC"), 55.0) &&
                checkTrolleyContent(plan, "T3", List.of("SKUE"), 10.0);

        printResults("Número de trolleys esperado: 3", plan.getTotalTrolleys() == 3 ? "Correto" : "Incorreto: " + plan.getTotalTrolleys());
        printPlanDetails(plan);
        printTestStatus(passed);
        return passed;
    }

    private boolean testBestFitDecreasing() {
        printScenarioHeader("Cenário 04: Heurística Best Fit Decreasing (BFD)");
        PickingService service = new PickingService();
        service.setItemsMap(itemsMap);
        // Mesmas alocações, pesos: B=40, D=30, C=25, A=15, E=10 (Total=120)
        List<Allocation> allocations = List.of(
                createAllocation("O1", 1, "SKUA", 10, "B1", "1", "1"), // 15kg
                createAllocation("O1", 2, "SKUB", 20, "B2", "1", "2"), // 40kg
                createAllocation("O1", 3, "SKUC", 10, "B3", "1", "3"), // 25kg
                createAllocation("O1", 4, "SKUD", 15, "B4", "1", "4"), // 30kg
                createAllocation("O1", 5, "SKUE", 5, "B5", "1", "5")   // 10kg
        );
        double capacity = 60.0;
        PickingPlan plan = service.generatePickingPlan(allocations, capacity, HeuristicType.BEST_FIT_DECREASING);

        // BFD Esperado (Ordem: B, D, C, A, E) (Cap=60):
        // T1: B(40)
        // T2: D(30)
        // T1: B(40) + A(15) = 55 (melhor fit para A)
        // T2: D(30) + C(25) = 55 (melhor fit para C)
        // T1: B(40) + A(15) + E(10)? Não cabe.
        // T2: D(30) + C(25) + E(10)? Não cabe.
        // T3: E(10)
        // Resultado igual a FFD neste caso.
        boolean passed = plan.getTotalTrolleys() == 3 &&
                checkTrolleyContent(plan, "T1", List.of("SKUB", "SKUA"), 55.0) && // Ordem pode variar dentro do trolley
                checkTrolleyContent(plan, "T2", List.of("SKUD", "SKUC"), 55.0) && // Ordem pode variar
                checkTrolleyContent(plan, "T3", List.of("SKUE"), 10.0);


        printResults("Número de trolleys esperado: 3", plan.getTotalTrolleys() == 3 ? "Correto" : "Incorreto: " + plan.getTotalTrolleys());
        printPlanDetails(plan);
        printTestStatus(passed);
        return passed;
    }

    private boolean testItemExcedeCapacidade() {
        printScenarioHeader("Cenário 05: Item Excede Capacidade Total");
        PickingService service = new PickingService();
        service.setItemsMap(itemsMap);
        List<Allocation> allocations = List.of(
                createAllocation("O1", 1, "SKUB", 40, "B1", "1", "1") // 40 * 2.0 = 80kg
        );
        double capacity = 50.0; // Capacidade menor que o item
        PickingPlan plan = service.generatePickingPlan(allocations, capacity, HeuristicType.FIRST_FIT);

        // Como a divisão/adiamento não está especificada para ser implementada,
        // o comportamento mais provável é criar um trolley só para este item, mesmo excedendo.
        // Ou poderia falhar/ignorar. Vamos verificar se criou 1 trolley com o item.
        boolean passed = plan.getTotalTrolleys() == 1 &&
                checkTrolleyContent(plan, "T1", List.of("SKUB"), 80.0);

        printResults("Esperado 1 trolley com o item (mesmo excedendo)", passed ? "Correto" : "Incorreto");
        printPlanDetails(plan);
        System.out.println("    (Nota: Verifica se este é o comportamento desejado para itens > capacidade)");
        printTestStatus(passed);
        return passed; // Considera PASS se colocou no trolley
    }

    private boolean testMultiplosTrolleys() {
        printScenarioHeader("Cenário 06: Múltiplos Trolleys Necessários");
        PickingService service = new PickingService();
        service.setItemsMap(itemsMap);
        List<Allocation> allocations = new ArrayList<>();
        // Adiciona 10 itens de 15kg cada = 150kg total
        for (int i = 1; i <= 10; i++) {
            allocations.add(createAllocation("O"+i, 1, "SKUA", 10, "B"+i, "1", String.valueOf(i))); // 15kg
        }
        double capacity = 50.0;
        PickingPlan plan = service.generatePickingPlan(allocations, capacity, HeuristicType.FIRST_FIT);

        // FF Esperado (Cap=50): 10 * 15kg -> 150kg total
        // T1: 15+15+15 = 45
        // T2: 15+15+15 = 45
        // T3: 15+15+15 = 45
        // T4: 15 = 15
        boolean passed = plan.getTotalTrolleys() == 4;

        printResults("Número de trolleys esperado: 4", passed ? "Correto" : "Incorreto: " + plan.getTotalTrolleys());
        printPlanDetails(plan); // Imprime detalhes para verificação manual
        printTestStatus(passed);
        return passed;
    }

    private boolean testUtilizacaoPerfeitaBFD() {
        printScenarioHeader("Cenário 07: Utilização Perfeita (BFD)");
        PickingService service = new PickingService();
        service.setItemsMap(itemsMap);
        // Itens: 40, 30, 30, 20, 20, 10 (Total=150)
        List<Allocation> allocations = List.of(
                createAllocation("O1", 1, "SKUB", 20, "B1", "1", "1"), // 40kg
                createAllocation("O1", 2, "SKUD", 15, "B2", "1", "2"), // 30kg
                createAllocation("O1", 3, "SKUD", 15, "B3", "1", "3"), // 30kg (outro D)
                createAllocation("O1", 4, "SKUB", 10, "B4", "1", "4"), // 20kg (outro B)
                createAllocation("O1", 5, "SKUB", 10, "B5", "1", "5"), // 20kg (outro B)
                createAllocation("O1", 6, "SKUE", 5, "B6", "1", "6")   // 10kg
        );
        double capacity = 50.0;
        PickingPlan plan = service.generatePickingPlan(allocations, capacity, HeuristicType.BEST_FIT_DECREASING);

        // BFD Esperado (Ordem: 40, 30, 30, 20, 20, 10) (Cap=50):
        // T1: 40
        // T2: 30
        // T3: 30
        // T1: 40 + 10 = 50 (melhor fit para 10)
        // T2: 30 + 20 = 50 (melhor fit para 20)
        // T3: 30 + 20 = 50 (melhor fit para 20)
        // 3 Trolleys com 100% utilização
        boolean passed = plan.getTotalTrolleys() == 3 &&
                plan.getTrolleys().stream().allMatch(t -> Math.abs(t.getCurrentWeight() - capacity) < 0.01);

        printResults("Número de trolleys esperado: 3", plan.getTotalTrolleys() == 3 ? "Correto" : "Incorreto: " + plan.getTotalTrolleys());
        printResults("Todos os trolleys devem ter 100% utilização", passed ? "Correto" : "Incorreto");
        printPlanDetails(plan);
        printTestStatus(passed);
        return passed;
    }



    // --- Métodos Auxiliares ---

    // Cria itens mock para teste
    private Map<String, Item> createMockItems() {
        Map<String, Item> items = new HashMap<>();
        items.put("SKUA", new Item("SKUA", "Item A", "Cat X", "unit", 1.5));
        items.put("SKUB", new Item("SKUB", "Item B", "Cat Y", "unit", 2.0));
        items.put("SKUC", new Item("SKUC", "Item C", "Cat Z", "unit", 2.5));
        items.put("SKUD", new Item("SKUD", "Item D", "Cat W", "unit", 2.0)); // Mesmo peso que B
        items.put("SKUE", new Item("SKUE", "Item E", "Cat V", "unit", 2.0)); // Mesmo peso que B e D
        return items;
    }

    // Cria uma Allocation simples
    private Allocation createAllocation(String orderId, int lineNo, String sku, int qty, String boxId, String aisle, String bay) {
        double weight = itemsMap.getOrDefault(sku, new Item(sku,"","", "", 1.0)).getUnitWeight() * qty;
        return new Allocation(orderId, lineNo, sku, qty, weight, boxId, aisle, bay);
    }

    // Cria um PickingPlan vazio
    private PickingPlan createPlan(String planId) {
        return new PickingPlan(planId, HeuristicType.FIRST_FIT, 0); // Heurística/Cap não importam aqui
    }

    // Verifica o conteúdo de um trolley específico
    private boolean checkTrolleyContent(PickingPlan plan, String trolleyId, List<String> expectedSkus, double expectedWeight) {
        Optional<Trolley> trolleyOpt = plan.getTrolleys().stream().filter(t -> t.getId().equals(trolleyId)).findFirst();
        if (trolleyOpt.isEmpty()) {
            System.err.printf("    [Check Trolley] FALHOU! Trolley %s não encontrado.%n", trolleyId);
            return false;
        }
        Trolley trolley = trolleyOpt.get();
        List<String> actualSkus = trolley.getAssignments().stream().map(PickingAssignment::getSku).collect(Collectors.toList());
        // Compara SKUs como conjuntos, pois a ordem dentro do trolley pode variar dependendo da heurística
        boolean skusMatch = new HashSet<>(expectedSkus).equals(new HashSet<>(actualSkus));
        boolean weightMatch = Math.abs(trolley.getCurrentWeight() - expectedWeight) < 0.01;

        if (!skusMatch) System.err.printf("    [Check Trolley %s] FALHOU SKUs! Esperado: %s | Obtido: %s%n", trolleyId, expectedSkus, actualSkus);
        if (!weightMatch) System.err.printf("    [Check Trolley %s] FALHOU Peso! Esperado: %.2f | Obtido: %.2f%n", trolleyId, expectedWeight, trolley.getCurrentWeight());

        return skusMatch && weightMatch;
    }

    // Imprime detalhes do plano (para verificação manual se necessário)
    private void printPlanDetails(PickingPlan plan) {
        System.out.println("    Detalhes do Plano:");
        if (plan == null || plan.getTrolleys().isEmpty()) {
            System.out.println("      Plano vazio.");
            return;
        }
        for (Trolley t : plan.getTrolleys()) {
            System.out.printf("      - %s (%.1f / %.1f kg, %.1f%%): %s%n",
                    t.getId(),
                    t.getCurrentWeight(),
                    t.getMaxCapacity(),
                    t.getUtilization(),
                    t.getAssignments().stream().map(pa -> String.format("%s(%d)", pa.getSku(), pa.getQuantity())).collect(Collectors.joining(", "))
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
            System.out.println("\n  --> Resultado do Cenário: ✅ PASSOU");
        } else {
            System.err.println("\n  --> Resultado do Cenário: ❌ FALHOU");
        }
    }

    private void printSummary() {
        System.out.println("\n======================================================");
        System.out.println("             Sumário do Relatório de Testes USEI03      ");
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
        System.out.println("             Fim do Relatório de Testes USEI03        ");
        System.out.println("======================================================");
    }
}