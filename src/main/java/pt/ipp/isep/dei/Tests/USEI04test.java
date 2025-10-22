package pt.ipp.isep.dei.Tests;

import pt.ipp.isep.dei.domain.*; // Importa todas as classes do domínio necessárias

import java.util.*; // Importa Collections e outras utilidades
import java.util.stream.Collectors;

public class USEI04test implements Runnable {

    // Estrutura para guardar os resultados dos testes
    private final Map<String, Boolean> testResults = new HashMap<>();

    // --- Método Principal para Execução ---
    public static void main(String[] args) {
        USEI04test tester = new USEI04test();
        tester.run();
    }

    @Override
    public void run() {
        System.out.println("======================================================");
        System.out.println("     Relatório de Testes - USEI04 Pick Path Service    ");
        System.out.println("======================================================");

        PickingPathService pathService = new PickingPathService();

        // Executa cada cenário e guarda o resultado (true=PASS, false=FAIL)
        testResults.put("Cenário 01: Plano Nulo e Vazio", testPlanoNuloVazio(pathService));
        testResults.put("Cenário 02: Sem Localizações Válidas", testSemLocalizacoesValidas(pathService));
        testResults.put("Cenário 03: Apenas Entrada (1 assignment inválido)", testApenasEntrada(pathService));
        testResults.put("Cenário 04: Localizações Duplicadas", testLocalizacoesDuplicadas(pathService));
        testResults.put("Cenário 05: Localizações Apenas num Aisle (Ex: Aisle 2)", testApenasUmAisle(pathService));
        testResults.put("Cenário 06: Localizações em Vários Aisles (Caso Típico)", testVariosAisles(pathService));
        testResults.put("Cenário 07: Ordem Invertida / Pontos Distantes", testOrdemInvertida(pathService));
        // --- Novos Cenários ---
        testResults.put("Cenário 08: Apenas um Ponto Válido", testApenasUmPonto(pathService));
        testResults.put("Cenário 09: Pontos em Linha Horizontal", testLinhaHorizontal(pathService));
        testResults.put("Cenário 10: Pontos em Escada", testEscada(pathService));
        testResults.put("Cenário 11: Desempate Nearest Neighbour", testDesempateNearestNeighbour(pathService));


        // Imprime o sumário final
        printSummary();
    }

    // --- Métodos de Teste por Cenário (retornam boolean) ---

    private boolean testPlanoNuloVazio(PickingPathService service) {
        printScenarioHeader("Cenário 01: Plano Nulo e Vazio");
        boolean passed = true;

        System.out.println("--> Testando com Plano NULO:");
        Map<String, PickingPathService.PathResult> resultNull = service.calculatePickingPaths(null);
        passed &= checkResult(resultNull, "Plano Nulo", List.of(BayLocation.entrance()), 0.0, List.of(BayLocation.entrance()), 0.0);
        printResults(resultNull);

        System.out.println("\n--> Testando com Plano VAZIO (sem trolleys):");
        PickingPlan planVazio = createPlan("PLAN_VAZIO");
        Map<String, PickingPathService.PathResult> resultVazio = service.calculatePickingPaths(planVazio);
        passed &= checkResult(resultVazio, "Plano Vazio", List.of(BayLocation.entrance()), 0.0, List.of(BayLocation.entrance()), 0.0);
        printResults(resultVazio);

        printTestStatus(passed);
        return passed;
    }

    private boolean testSemLocalizacoesValidas(PickingPathService service) {
        printScenarioHeader("Cenário 02: Sem Localizações Válidas");
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
        boolean passed = checkResult(results, "Locs Inválidas", List.of(BayLocation.entrance()), 0.0, List.of(BayLocation.entrance()), 0.0);
        printResults(results);

        printTestStatus(passed);
        return passed;
    }

    private boolean testApenasEntrada(PickingPathService service) {
        printScenarioHeader("Cenário 03: Apenas Entrada (1 assignment inválido)");
        PickingPlan plan = createPlan("PLAN_SO_ENTRADA");
        Trolley t1 = new Trolley("T1_SO_ENTRADA", 100);
        t1.addAssignment(createAssignment("ORD_X", 1, "SKU_X", 1, "BOXX", null, null));
        plan.addTrolley(t1);

        Map<String, PickingPathService.PathResult> results = service.calculatePickingPaths(plan);
        boolean passed = checkResult(results, "Apenas Entrada", List.of(BayLocation.entrance()), 0.0, List.of(BayLocation.entrance()), 0.0);
        printResults(results);

        printTestStatus(passed);
        return passed;
    }

    private boolean testLocalizacoesDuplicadas(PickingPathService service) {
        printScenarioHeader("Cenário 04: Localizações Duplicadas");
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


        System.out.println("--> Localizações nos assignments: (1,5), (2,3), (1,5), (2,3), (1,8), (1,5)");
        System.out.println("--> Localizações únicas esperadas: (1,5), (1,8), (2,3)");
        // Caminho Esperado A: (0,0)->(1,5)->(1,8)->(2,3) ; Dist = 8 + 3 + 14 = 25
        // Caminho Esperado B: (0,0)->(1,5)->(1,8)->(2,3) ; Dist = 8 + 3 + 14 = 25 (NN dá o mesmo aqui)
        List<BayLocation> expectedPath = List.of(
                BayLocation.entrance(), createLoc(1, 5), createLoc(1, 8), createLoc(2, 3)
        );
        double expectedDistance = 25.0;

        Map<String, PickingPathService.PathResult> results = service.calculatePickingPaths(plan);
        boolean passed = checkResult(results, "Duplicadas", expectedPath, expectedDistance, expectedPath, expectedDistance);
        printResults(results);

        printTestStatus(passed);
        return passed;
    }

    private boolean testApenasUmAisle(PickingPathService service) {
        printScenarioHeader("Cenário 05: Localizações Apenas num Aisle (Ex: Aisle 2)");
        PickingPlan plan = createPlan("PLAN_AISLE2");
        Trolley t1 = new Trolley("T1_AISLE2", 100);
        t1.addAssignment(createAssignment("ORD4", 1, "SKU_A", 1, "BOX20", "2", "8"));
        t1.addAssignment(createAssignment("ORD4", 2, "SKU_B", 1, "BOX21", "2", "3"));
        t1.addAssignment(createAssignment("ORD4", 3, "SKU_C", 1, "BOX22", "2", "10"));
        t1.addAssignment(createAssignment("ORD4", 4, "SKU_D", 1, "BOX23", "2", "1"));
        t1.addAssignment(createAssignment("ORD4", 5, "SKU_E", 1, "BOX24", "2", "5"));
        plan.addTrolley(t1);

        System.out.println("--> Localizações esperadas (Aisle 2): (2,1), (2,3), (2,5), (2,8), (2,10)");
        // Caminho Esperado (A e B iguais): (0,0)->(2,1)->(2,3)->(2,5)->(2,8)->(2,10)
        // Dist = 7 + 2 + 2 + 3 + 2 = 16
        List<BayLocation> expectedPath = List.of(
                BayLocation.entrance(), createLoc(2, 1), createLoc(2, 3), createLoc(2, 5), createLoc(2, 8), createLoc(2, 10)
        );
        double expectedDistance = 16.0;

        Map<String, PickingPathService.PathResult> results = service.calculatePickingPaths(plan);
        boolean passed = checkResult(results, "Um Aisle", expectedPath, expectedDistance, expectedPath, expectedDistance);
        printResults(results);
        System.out.println("    (Nota: Espera-se que ambas as estratégias deem o mesmo resultado)");

        printTestStatus(passed);
        return passed;
    }

    private boolean testVariosAisles(PickingPathService service) {
        printScenarioHeader("Cenário 06: Localizações em Vários Aisles (Caso Típico)");
        PickingPlan plan = createPlan("PLAN_VARIOS_AISLES");
        Trolley t1 = new Trolley("T1_VARIOS", 100);
        // Pontos: (1,8), (3,4), (2,2), (1,3), (3,1)
        t1.addAssignment(createAssignment("ORD5", 1, "SKU_A", 1, "BOX30", "1", "8"));
        t1.addAssignment(createAssignment("ORD5", 2, "SKU_B", 1, "BOX31", "3", "4"));
        t1.addAssignment(createAssignment("ORD5", 3, "SKU_C", 1, "BOX32", "2", "2"));
        t1.addAssignment(createAssignment("ORD5", 4, "SKU_D", 1, "BOX33", "1", "3"));
        t1.addAssignment(createAssignment("ORD5", 5, "SKU_E", 1, "BOX34", "3", "1"));
        plan.addTrolley(t1);

        System.out.println("--> Localizações esperadas: (1,3), (1,8), (2,2), (3,1), (3,4)");
        // Caminho A (Sweep): (0,0)->(1,3)->(1,8)->(2,2)->(3,1)->(3,4) ; Dist = 6+5+13+6+3 = 33
        List<BayLocation> expectedPathA = List.of(
                BayLocation.entrance(), createLoc(1, 3), createLoc(1, 8), createLoc(2, 2), createLoc(3, 1), createLoc(3, 4)
        );
        double expectedDistanceA = 33.0;
        // Caminho B (Nearest): (0,0)->(1,3) D=6; (1,3)->(1,8) D=5; (1,8)->(2,2) D=13; (2,2)->(3,1) D=6; (3,1)->(3,4) D=3; Total = 6+5+13+6+3 = 33
        List<BayLocation> expectedPathB = List.of(
                BayLocation.entrance(), createLoc(1, 3), createLoc(1, 8), createLoc(2, 2), createLoc(3, 1), createLoc(3, 4)
        );
        double expectedDistanceB = 33.0;

        Map<String, PickingPathService.PathResult> results = service.calculatePickingPaths(plan);
        boolean passed = checkResult(results, "Varios Aisles", expectedPathA, expectedDistanceA, expectedPathB, expectedDistanceB);
        printResults(results);
        System.out.println("    (Nota: Neste caso específico, as estratégias dão o mesmo resultado)");


        printTestStatus(passed);
        return passed;
    }

    private boolean testOrdemInvertida(PickingPathService service) {
        printScenarioHeader("Cenário 07: Ordem Invertida / Pontos Distantes");
        PickingPlan plan = createPlan("PLAN_INVERTIDO");
        Trolley t1 = new Trolley("T1_INVERTIDO", 100);
        t1.addAssignment(createAssignment("ORD6", 1, "SKU_A", 1, "BOX40", "1", "10")); // (1,10)
        t1.addAssignment(createAssignment("ORD6", 2, "SKU_B", 1, "BOX41", "2", "1"));  // (2,1)
        plan.addTrolley(t1);

        System.out.println("--> Localizações esperadas: (1,10), (2,1)");
        // Caminho A (Sweep): (0,0)->(1,10)->(2,1) ; Dist = 13 + 14 = 27
        List<BayLocation> expectedPathA = List.of(
                BayLocation.entrance(), createLoc(1, 10), createLoc(2, 1)
        );
        double expectedDistanceA = 27.0;
        // Caminho B (Nearest): (0,0)->(2,1)->(1,10) ; Dist = 7 + 14 = 21
        List<BayLocation> expectedPathB = List.of(
                BayLocation.entrance(), createLoc(2, 1), createLoc(1, 10)
        );
        double expectedDistanceB = 21.0;

        Map<String, PickingPathService.PathResult> results = service.calculatePickingPaths(plan);
        boolean passed = checkResult(results, "Ordem Invertida", expectedPathA, expectedDistanceA, expectedPathB, expectedDistanceB);
        printResults(results);
        System.out.println("    (Nota: Nearest Neighbour deve ir para (2,1) primeiro)");

        printTestStatus(passed);
        return passed;
    }

    // --- Novos Cenários ---

    private boolean testApenasUmPonto(PickingPathService service) {
        printScenarioHeader("Cenário 08: Apenas um Ponto Válido");
        PickingPlan plan = createPlan("PLAN_UM_PONTO");
        Trolley t1 = new Trolley("T1_UM_PONTO", 100);
        t1.addAssignment(createAssignment("ORD7", 1, "SKU_A", 1, "BOX50", "3", "5")); // (3,5)
        t1.addAssignment(createAssignment("ORD7", 2, "SKU_B", 1, "BOX51", null, "1")); // Inválido
        plan.addTrolley(t1);

        System.out.println("--> Localização esperada: (3,5)");
        // Caminho A e B: (0,0)->(3,5) ; Dist = 0 + |0-3|*3 + 5 = 14
        List<BayLocation> expectedPath = List.of(BayLocation.entrance(), createLoc(3, 5));
        double expectedDistance = 14.0;

        Map<String, PickingPathService.PathResult> results = service.calculatePickingPaths(plan);
        boolean passed = checkResult(results, "Um Ponto", expectedPath, expectedDistance, expectedPath, expectedDistance);
        printResults(results);

        printTestStatus(passed);
        return passed;
    }

    private boolean testLinhaHorizontal(PickingPathService service) {
        printScenarioHeader("Cenário 09: Pontos em Linha Horizontal (Mesma Bay)");
        PickingPlan plan = createPlan("PLAN_HORIZONTAL");
        Trolley t1 = new Trolley("T1_HORIZONTAL", 100);
        t1.addAssignment(createAssignment("ORD8", 1, "SKU_A", 1, "BOX60", "1", "5")); // (1,5)
        t1.addAssignment(createAssignment("ORD8", 2, "SKU_B", 1, "BOX61", "3", "5")); // (3,5)
        t1.addAssignment(createAssignment("ORD8", 3, "SKU_C", 1, "BOX62", "2", "5")); // (2,5)
        plan.addTrolley(t1);

        System.out.println("--> Localizações esperadas: (1,5), (2,5), (3,5)");
        // Caminho A: (0,0)->(1,5)->(2,5)->(3,5) ; Dist = 8 + (5+|1-2|*3+5) + (5+|2-3|*3+5) = 8 + 13 + 13 = 34
        List<BayLocation> expectedPathA = List.of(
                BayLocation.entrance(), createLoc(1, 5), createLoc(2, 5), createLoc(3, 5)
        );
        double expectedDistanceA = 34.0;
        // Caminho B: (0,0)->(1,5) D=8; (1,5)->(2,5) D=13; (2,5)->(3,5) D=13 ; Total = 34
        List<BayLocation> expectedPathB = List.of(
                BayLocation.entrance(), createLoc(1, 5), createLoc(2, 5), createLoc(3, 5)
        );
        double expectedDistanceB = 34.0;


        Map<String, PickingPathService.PathResult> results = service.calculatePickingPaths(plan);
        boolean passed = checkResult(results, "Linha Horizontal", expectedPathA, expectedDistanceA, expectedPathB, expectedDistanceB);
        printResults(results);

        printTestStatus(passed);
        return passed;
    }

    private boolean testEscada(PickingPathService service) {
        printScenarioHeader("Cenário 10: Pontos em Escada");
        PickingPlan plan = createPlan("PLAN_ESCADA");
        Trolley t1 = new Trolley("T1_ESCADA", 100);
        t1.addAssignment(createAssignment("ORD9", 1, "SKU_A", 1, "BOX70", "1", "2")); // (1,2)
        t1.addAssignment(createAssignment("ORD9", 2, "SKU_B", 1, "BOX71", "2", "4")); // (2,4)
        t1.addAssignment(createAssignment("ORD9", 3, "SKU_C", 1, "BOX72", "3", "6")); // (3,6)
        plan.addTrolley(t1);

        System.out.println("--> Localizações esperadas: (1,2), (2,4), (3,6)");
        // Caminho A: (0,0)->(1,2)->(2,4)->(3,6) ; Dist = 5 + (2+|1-2|*3+4) + (4+|2-3|*3+6) = 5 + 9 + 13 = 27
        List<BayLocation> expectedPathA = List.of(
                BayLocation.entrance(), createLoc(1, 2), createLoc(2, 4), createLoc(3, 6)
        );
        double expectedDistanceA = 27.0;
        // Caminho B: (0,0)->(1,2) D=5; (1,2)->(2,4) D=9; (2,4)->(3,6) D=13; Total = 27
        List<BayLocation> expectedPathB = List.of(
                BayLocation.entrance(), createLoc(1, 2), createLoc(2, 4), createLoc(3, 6)
        );
        double expectedDistanceB = 27.0;

        Map<String, PickingPathService.PathResult> results = service.calculatePickingPaths(plan);
        boolean passed = checkResult(results, "Escada", expectedPathA, expectedDistanceA, expectedPathB, expectedDistanceB);
        printResults(results);

        printTestStatus(passed);
        return passed;
    }

    private boolean testDesempateNearestNeighbour(PickingPathService service) {
        printScenarioHeader("Cenário 11: Desempate Nearest Neighbour");
        PickingPlan plan = createPlan("PLAN_DESEMPATE");
        Trolley t1 = new Trolley("T1_DESEMPATE", 100);
        // Pontos equidistantes da entrada: (1,1) D=4, (2,(-1)) Inválido - Mas D((0,0),(2,1))=7
        // Adicionamos (1,4) D=7
        t1.addAssignment(createAssignment("ORD10", 1, "SKU_A", 1, "BOX80", "1", "4")); // (1,4) D=7
        t1.addAssignment(createAssignment("ORD10", 2, "SKU_B", 1, "BOX81", "2", "1")); // (2,1) D=7
        t1.addAssignment(createAssignment("ORD10", 3, "SKU_C", 1, "BOX82", "1", "1")); // (1,1) D=4
        plan.addTrolley(t1);

        System.out.println("--> Localizações esperadas: (1,1), (1,4), (2,1)");
        System.out.println("    Distâncias da Entrada: (1,1)=4, (1,4)=7, (2,1)=7");
        // Caminho A: (0,0)->(1,1)->(1,4)->(2,1) ; Dist = 4 + 3 + (4+|1-2|*3+1) = 4 + 3 + 8 = 15
        List<BayLocation> expectedPathA = List.of(
                BayLocation.entrance(), createLoc(1, 1), createLoc(1, 4), createLoc(2, 1)
        );
        double expectedDistanceA = 15.0;
        // Caminho B: (0,0)->(1,1) D=4. De (1,1): D(->1,4)=3, D(->2,1)=1+|1-2|*3+1=5. Vai para (1,4). De (1,4): D(->2,1)=4+|1-2|*3+1=8.
        // Caminho B: (0,0)->(1,1)->(1,4)->(2,1) ; Dist = 4 + 3 + 8 = 15
        List<BayLocation> expectedPathB = List.of(
                BayLocation.entrance(), createLoc(1, 1), createLoc(1, 4), createLoc(2, 1)
        );
        double expectedDistanceB = 15.0;


        Map<String, PickingPathService.PathResult> results = service.calculatePickingPaths(plan);
        boolean passed = checkResult(results, "Desempate NN", expectedPathA, expectedDistanceA, expectedPathB, expectedDistanceB);
        printResults(results);
        System.out.println("    (Nota: O desempate no NN deve escolher (1,4) sobre (2,1) como próximo após (1,1), se o compareTo estiver correto)");


        printTestStatus(passed);
        return passed;
    }


    // --- Métodos Auxiliares (mantidos da versão anterior) ---

    // ... (printScenarioHeader, createAssignment, createPlan, createLoc, printResults, checkResult, arePathsEqual, printTestStatus, printSummary) ...
    // (Copiar os métodos auxiliares da resposta anterior aqui)

    // Imprime o cabeçalho do cenário
    private void printScenarioHeader(String title) {
        System.out.println("\n------------------------------------------------------");
        System.out.println("  " + title);
        System.out.println("------------------------------------------------------");
    }

    // Cria um PickingAssignment simples para os testes
    private PickingAssignment createAssignment(String orderId, int lineNo, String sku, int qty, String boxId, String aisle, String bay) {
        // Mock Item (peso 1.0 para simplificar, pois não afeta o path)
        Item mockItem = new Item(sku, "Mock Item " + sku, "Mock Category", "unit", 1.0);
        return new PickingAssignment(orderId, lineNo, mockItem, qty, boxId, aisle, bay);
    }

    // Cria um PickingPlan vazio
    private PickingPlan createPlan(String planId) {
        // Heurística e capacidade não são relevantes para USEI04, usamos valores dummy
        return new PickingPlan(planId, HeuristicType.FIRST_FIT, 0);
    }

    // Cria uma BayLocation específica (necessário porque o construtor é privado)
    private BayLocation createLoc(int aisle, int bay) {
        // Cria um assignment dummy só para poder usar o construtor público de BayLocation
        PickingAssignment dummy = createAssignment("dummy", 0, "dummy", 0, "dummy", String.valueOf(aisle), String.valueOf(bay));
        return new BayLocation(dummy);
    }


    // Imprime os resultados de forma formatada
    private void printResults(Map<String, PickingPathService.PathResult> results) {
        if (results == null || results.isEmpty()) {
            System.out.println("    ERRO: Resultados nulos ou vazios retornados.");
            return;
        }
        results.forEach((strategyName, result) -> {
            System.out.println("\n  " + strategyName + ":");
            if (result == null) {
                System.out.println("    ERRO: PathResult nulo.");
            } else {
                System.out.println("    " + result); // Usa o toString() do PathResult
            }
        });
    }

    // Verifica se os resultados obtidos correspondem aos esperados
    private boolean checkResult(Map<String, PickingPathService.PathResult> actualResults, String testName,
                                List<BayLocation> expectedPathA, double expectedDistA,
                                List<BayLocation> expectedPathB, double expectedDistB) {
        boolean passA = false;
        boolean passB = false;

        PickingPathService.PathResult actualA = actualResults.get("Strategy A (Deterministic Sweep)");
        PickingPathService.PathResult actualB = actualResults.get("Strategy B (Nearest Neighbour)");

        // Verifica Estratégia A
        if (actualA != null && arePathsEqual(expectedPathA, actualA.path) && Math.abs(expectedDistA - actualA.totalDistance) < 0.01) {
            passA = true;
        } else {
            System.err.printf("    [%s - Strat A] FALHOU! Esperado: Path=%s, Dist=%.2f | Obtido: Path=%s, Dist=%.2f%n",
                    testName, expectedPathA, expectedDistA, actualA != null ? actualA.path : "NULL", actualA != null ? actualA.totalDistance : Double.NaN);
        }

        // Verifica Estratégia B
        if (actualB != null && arePathsEqual(expectedPathB, actualB.path) && Math.abs(expectedDistB - actualB.totalDistance) < 0.01) {
            passB = true;
        } else {
            System.err.printf("    [%s - Strat B] FALHOU! Esperado: Path=%s, Dist=%.2f | Obtido: Path=%s, Dist=%.2f%n",
                    testName, expectedPathB, expectedDistB, actualB != null ? actualB.path : "NULL", actualB != null ? actualB.totalDistance : Double.NaN);
        }

        return passA && passB;
    }

    // Compara duas listas de BayLocation (ordem importa)
    private boolean arePathsEqual(List<BayLocation> path1, List<BayLocation> path2) {
        return Objects.equals(path1, path2); // Usa o equals da lista, que compara elemento a elemento
    }


    // Imprime o estado final do teste
    private void printTestStatus(boolean passed) {
        if (passed) {
            System.out.println("\n  --> Resultado do Cenário: ✅ PASSOU");
        } else {
            System.err.println("\n  --> Resultado do Cenário: ❌ FALHOU");
        }
    }

    // Imprime o sumário final
    private void printSummary() {
        System.out.println("\n======================================================");
        System.out.println("                 Sumário do Relatório de Testes         ");
        System.out.println("======================================================");
        int passCount = 0;
        int failCount = 0;
        // Ordena os nomes dos testes para uma apresentação consistente
        List<String> sortedTestNames = new ArrayList<>(testResults.keySet());
        Collections.sort(sortedTestNames);

        for (String testName : sortedTestNames) {
            Boolean resultValue = testResults.get(testName);
            // Corrige a lógica para tratar null como falha
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
        System.out.println("                 Fim do Relatório de Testes             ");
        System.out.println("======================================================");
    }

}