package pt.ipp.isep.dei.UI;

import pt.ipp.isep.dei.domain.*;
import java.util.ArrayList; // Import necessário para ArrayList
import java.util.List;
import java.util.Map; // Import necessário para Map
import java.util.Scanner;

public class PickingUI {
    private final InventoryManager inventoryManager;

    public PickingUI(InventoryManager inventoryManager) {
        this.inventoryManager = inventoryManager;
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("\n" + "=".repeat(50));
        System.out.println("        USEI03 - Picking Plan Generator");
        System.out.println("=".repeat(50));

        PickingPlan plan = null; // Declarar plan aqui para estar acessível mais tarde

        try {
            // 1. PRIMEIRO: Executar USEI02 para obter alocações
            System.out.println("\n📦 PASSO 1: Executando USEI02 para gerar alocações...");

            // Carregar orders
            List<Order> orders = loadOrders();
            // Obter uma cópia MUTÁVEL do inventário para simulação de alocação
            // A classe InventoryManager deve fornecer acesso ao Inventory, que por sua vez gere as Boxes
            List<Box> currentInventoryState = new ArrayList<>(inventoryManager.getInventory().getBoxes()); // Cria uma cópia mutável

            if (orders.isEmpty()) {
                System.out.println("❌ ERRO: Não há orders válidos para processar.");
                return;
            }

            if (currentInventoryState.isEmpty()) {
                System.out.println("❌ ERRO: O inventário está vazio.");
                return;
            }

            System.out.printf("✅ Dados carregados: %d orders, %d boxes no inventário%n",
                    orders.size(), currentInventoryState.size());

            // Configurar e executar OrderAllocator
            OrderAllocator allocator = new OrderAllocator();
            allocator.setItems(inventoryManager.getItemsMap()); // Passar items para cálculo de pesos

            // Passar a cópia MUTÁVEL do inventário
            AllocationResult allocationResult = allocator.allocateOrders(
                    orders, currentInventoryState, OrderAllocator.Mode.PARTIAL);

            // Verificar se há alocações válidas
            if (allocationResult.allocations.isEmpty()) {
                System.out.println("❌ Nenhuma alocação foi gerada na USEI02.");
                System.out.println("\n📊 Resumo das Eligibilidades:");
                for (Eligibility e : allocationResult.eligibilityList) {
                    System.out.println("  " + e);
                }
                return;
            }

            System.out.println("✅ USEI02 executado com sucesso!");
            System.out.printf("📊 Resultados: %d alocações geradas, %d linhas processadas%n",
                    allocationResult.allocations.size(), allocationResult.eligibilityList.size());

            // Mostrar resumo das eligibilidades
            System.out.println("\n📋 Resumo das Eligibilidades:");
            int eligible = 0, partial = 0, undispatchable = 0;
            for (Eligibility e : allocationResult.eligibilityList) {
                System.out.println("  " + e);
                switch (e.status) {
                    case ELIGIBLE: eligible++; break;
                    case PARTIAL: partial++; break;
                    case UNDISPATCHABLE: undispatchable++; break;
                }
            }
            System.out.printf("\n📈 Estatísticas: ELIGIBLE=%d, PARTIAL=%d, UNDISPATCHABLE=%d%n",
                    eligible, partial, undispatchable);

            // 2. SEGUNDO: Parâmetros para USEI03
            System.out.println("\n🎯 PASSO 2: Configurar USEI03 - Plano de Picking");

            System.out.print("➡️  Capacidade do trolley (kg): ");
            double capacity = scanner.nextDouble();

            if (capacity <= 0) {
                System.out.println("❌ Capacidade inválida. Usando valor padrão de 50kg.");
                capacity = 50.0;
            }

            System.out.println("\n🧠 Heurísticas disponíveis:");
            System.out.println("1. FIRST_FIT - Primeiro que cabe (mais rápido)");
            System.out.println("2. FIRST_FIT_DECREASING - Maiores primeiro (mais eficiente)");
            System.out.println("3. BEST_FIT_DECREASING - Melhor encaixe (otimiza espaço)");
            System.out.print("➡️  Escolha a heurística (1-3): ");

            int heuristicChoice = scanner.nextInt();
            HeuristicType heuristic = switch(heuristicChoice) {
                case 1 -> HeuristicType.FIRST_FIT;
                case 2 -> HeuristicType.FIRST_FIT_DECREASING;
                case 3 -> HeuristicType.BEST_FIT_DECREASING;
                default -> {
                    System.out.println("⚠️  Escolha inválida. Usando FIRST_FIT.");
                    yield HeuristicType.FIRST_FIT;
                }
            };

            // 3. TERCEIRO: Executar USEI03
            System.out.println("\n⚙️  PASSO 3: Executando USEI03...");

            PickingService service = new PickingService();
            service.setItemsMap(inventoryManager.getItemsMap()); // Passar items para o serviço

            plan = service.generatePickingPlan( // Atribuir ao 'plan' declarado fora do try
                    allocationResult.allocations,
                    capacity,
                    heuristic
            );

            // 4. QUARTO: Mostrar resultados USEI03
            System.out.println("\n" + "=".repeat(60));
            System.out.println("           📊 RESULTADOS USEI03 - Plano de Picking");
            System.out.println("=".repeat(60));
            System.out.println(plan.getSummary());

            System.out.println("\n🛒 Detalhes por Trolley:");
            System.out.println("-".repeat(50));

            int trolleyCount = 1;
            double totalUtilization = 0;

            for (Trolley trolley : plan.getTrolleys()) {
                System.out.printf("\n🚗 Trolley %d: %s (%.1f%% utilizado)%n",
                        trolleyCount, trolley.getId(), trolley.getUtilization());
                System.out.printf("   📦 Peso: %.1f/%.1f kg | Itens: %d%n",
                        trolley.getCurrentWeight(), trolley.getMaxCapacity(),
                        trolley.getAssignments().size());

                for (PickingAssignment assignment : trolley.getAssignments()) {
                    System.out.printf("   → %s | Peso: %.1f kg | Local: %s%n",
                            assignment, assignment.getTotalWeight(), assignment.getLocation());
                }
                totalUtilization += trolley.getUtilization();
                trolleyCount++;
            }

            // Calcular avgUtilization apenas se houver trolleys para evitar divisão por zero
            double avgUtilization = (plan.getTotalTrolleys() > 0) ? (totalUtilization / plan.getTotalTrolleys()) : 0.0;
            System.out.printf("\n📈 Utilização média: %.1f%%%n", avgUtilization);

            // 5. OPÇÃO: Exportar para CSV
            scanner.nextLine(); // Consumir a nova linha pendente após nextInt() ou nextDouble()
            System.out.print("\n💾 Exportar plano USEI03 para CSV? (s/n): ");
            String exportChoice = scanner.nextLine(); // Usar nextLine() para ler a resposta corretamente
            if (exportChoice.equalsIgnoreCase("s")) {
                String csv = service.exportToCSV(plan);
                System.out.println("\n📄 CSV Gerado (USEI03):");
                System.out.println("=".repeat(50));
                System.out.println(csv);
                System.out.println("=".repeat(50));
            }

            System.out.println("\n✅ USEI03 concluído com sucesso!");


            // --- INÍCIO DA INTEGRAÇÃO DA USEI04 ---

            // 6. EXECUTAR USEI04 - Sequenciamento de Rota
            System.out.println("\n" + "=".repeat(60));
            System.out.println("        🚀 USEI04 - Pick Path Sequencing");
            System.out.println("=".repeat(60));

            PickingPathService pathService = new PickingPathService();
            try {
                // Passar o 'plan' gerado na USEI03
                Map<String, PickingPathService.PathResult> pathResults = pathService.calculatePickingPaths(plan);

                if (pathResults.isEmpty()) {
                    System.out.println("Não foi possível calcular os percursos (verificar se o plano de picking tem localizações válidas).");
                } else {
                    System.out.println("\n--- Resultados do Sequenciamento (USEI04) ---");
                    pathResults.forEach((strategyName, result) -> {
                        System.out.println("\n" + strategyName + ":");
                        System.out.println(result); // Usa o toString() do PathResult
                        System.out.println("-".repeat(40));
                    });
                    System.out.println("\n✅ USEI04 concluído com sucesso!");
                }

            } catch (Exception e) {
                System.out.println("❌ Erro ao calcular percursos de picking (USEI04): " + e.getMessage());
                e.printStackTrace(); // Imprime stack trace para depuração
            }

            // --- FIM DA INTEGRAÇÃO DA USEI04 ---


        } catch (Exception e) {
            System.out.println("❌ Erro durante a execução global: " + e.getMessage());
            e.printStackTrace(); // Imprime a stack trace completa para depuração
        } finally {
            // É boa prática fechar o scanner se não for System.in ou se esta for a última utilização
            // No contexto de um menu maior, pode ser melhor não fechar aqui.
            // scanner.close();
        }
    }

    private List<Order> loadOrders() {
        try {
            // Usar o InventoryManager para carregar orders
            return inventoryManager.loadOrders(
                    "src/main/java/pt/ipp/isep/dei/FicheirosCSV/orders.csv",
                    "src/main/java/pt/ipp/isep/dei/FicheirosCSV/order_lines.csv"
            );
        } catch (Exception e) {
            System.out.println("❌ Erro ao carregar orders: " + e.getMessage());
            return List.of(); // Retorna lista vazia em caso de erro
        }
    }
}

