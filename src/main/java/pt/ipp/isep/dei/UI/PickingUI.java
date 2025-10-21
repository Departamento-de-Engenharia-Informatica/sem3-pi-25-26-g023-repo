package pt.ipp.isep.dei.UI;

import pt.ipp.isep.dei.domain.*;
import java.util.List;
import java.util.Scanner;

public class PickingUI {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== USEI03 - Picking Plan Generator ===");

        try {
            // 1. PRIMEIRO: Executar USEI02 para obter alocações
            System.out.println("\n1. Executando USEI02 para gerar alocações...");

            // Carregar dados necessários para USEI02 (orders e inventory)
            List<Order> orders = loadOrders(); // Método que tens que implementar
            List<Box> inventory = loadInventory(); // Método que tens que implementar

            OrderAllocator allocator = new OrderAllocator();
            AllocationResult allocationResult = allocator.allocateOrders(
                    orders, inventory, OrderAllocator.Mode.PARTIAL);

            // Verificar se há alocações válidas - AGORA CORRETO!
            if (allocationResult.allocations.isEmpty()) { // ← Campo direto
                System.out.println("❌ ERRO: Nenhuma alocação foi gerada na USEI02");
                System.out.println("Verifique se existem orders e inventory válidos.");
                return;
            }

            System.out.println("✅ USEI02 executado com sucesso!");
            System.out.println("Alocações geradas: " + allocationResult.allocations.size());
            System.out.println("Linhas elegíveis: " + allocationResult.eligibilityList.size());

            // 2. SEGUNDO: Parâmetros para USEI03
            System.out.println("\n2. Parâmetros para USEI03:");

            System.out.print("Trolley capacity (kg): ");
            double capacity = scanner.nextDouble();

            System.out.print("Heuristic (1=FF, 2=FFD, 3=BFD): ");
            int heuristicChoice = scanner.nextInt();

            HeuristicType heuristic = switch(heuristicChoice) {
                case 1 -> HeuristicType.FIRST_FIT;
                case 2 -> HeuristicType.FIRST_FIT_DECREASING;
                case 3 -> HeuristicType.BEST_FIT_DECREASING;
                default -> HeuristicType.FIRST_FIT;
            };

            // 3. TERCEIRO: Executar USEI03
            System.out.println("\n3. Executando USEI03...");
            PickingService service = new PickingService();
            PickingPlan plan = service.generatePickingPlan(
                    allocationResult.allocations, // ← CORRETO: campo direto!
                    capacity,
                    heuristic
            );

            // 4. QUARTO: Mostrar resultados
            System.out.println("\n4. RESULTADOS - Plano de Picking Gerado:");
            System.out.println("========================================");
            System.out.println(plan.getSummary());

            System.out.println("\nDetalhes por Trolley:");
            for (Trolley trolley : plan.getTrolleys()) {
                System.out.println("  " + trolley);
            }

            System.out.println("\nExportar para CSV? (s/n): ");
            String export = scanner.next();
            if (export.equalsIgnoreCase("s")) {
                String csv = service.exportToCSV(plan);
                System.out.println("\nCSV Exportado:");
                System.out.println(csv);
            }

        } catch (Exception e) {
            System.out.println("❌ Erro: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }

    // Métodos que precisas implementar:
    private static List<Order> loadOrders() {
        // TODO: Implementar carregamento de orders
        // Exemplo: return Order.loadFromCSV("orders.csv");
        System.out.println("⚠️  AVISO: Método loadOrders() precisa ser implementado");
        return List.of(); // Retornar lista vazia por enquanto
    }

    private static List<Box> loadInventory() {
        // TODO: Implementar carregamento de inventory
        // Exemplo: return Box.loadFromCSV("boxes.csv");
        System.out.println("⚠️  AVISO: Método loadInventory() precisa ser implementado");
        return List.of(); // Retornar lista vazia por enquanto
    }
}