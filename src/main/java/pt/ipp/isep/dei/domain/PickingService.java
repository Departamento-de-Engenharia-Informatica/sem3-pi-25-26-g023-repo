package pt.ipp.isep.dei.domain;

import java.io.FileWriter; // Para exportar CSV para ficheiro
import java.io.IOException; // Para exportar CSV para ficheiro
import java.util.*;

public class PickingService {

    private Map<String, Item> itemsMap;

    public PickingService() {
        this.itemsMap = new HashMap<>();
    }

    // Método para setar o mapa de items
    public void setItemsMap(Map<String, Item> itemsMap) {
        this.itemsMap = itemsMap != null ? itemsMap : new HashMap<>();
    }

    public PickingPlan generatePickingPlan(List<Allocation> allocations,
                                           double trolleyCapacity,
                                           HeuristicType heuristic) {

        if (allocations == null || allocations.isEmpty()) {
            System.out.println("⚠️  Não há alocações da USEI02 para gerar o plano de picking.");
            // Retorna um plano vazio ou lança exceção, dependendo do requisito
            return new PickingPlan("EMPTY_PLAN_" + System.currentTimeMillis(), heuristic, trolleyCapacity);
        }

        // Converter allocations para picking assignments, IGNORANDO as que não têm localização
        List<PickingAssignment> assignments = convertToAssignments(allocations);

        if (assignments.isEmpty()) {
            System.out.println("⚠️  Nenhuma alocação válida com localização foi encontrada para criar Picking Assignments.");
            return new PickingPlan("NO_VALID_ASSIGNMENTS_" + System.currentTimeMillis(), heuristic, trolleyCapacity);
        }

        System.out.printf("  ➡️  Convertidas %d alocações em %d Picking Assignments válidos (com localização).%n",
                allocations.size(), assignments.size());


        // Usar factory para obter a heurística
        PackingHeuristic packingHeuristic = PackingHeuristicFactory.createHeuristic(heuristic);

        // Aplicar heurística
        List<Trolley> trolleys = packingHeuristic.packItems(assignments, trolleyCapacity);

        // Criar picking plan
        PickingPlan plan = new PickingPlan(generatePlanId(), heuristic, trolleyCapacity);
        trolleys.forEach(plan::addTrolley);

        return plan;
    }

    // Método modificado para filtrar alocações sem localização válida
    private List<PickingAssignment> convertToAssignments(List<Allocation> allocations) {
        List<PickingAssignment> assignments = new ArrayList<>();
        int skippedCount = 0;

        for (Allocation alloc : allocations) {
            // *** VERIFICAÇÃO ADICIONADA: Ignorar alocações sem localização ***
            if (alloc.aisle == null || alloc.bay == null || alloc.aisle.trim().isEmpty() || alloc.bay.trim().isEmpty()) {
                System.out.printf("  ⚠️  A ignorar Alocação para Picking: Localização em falta (Aisle: %s, Bay: %s) para Order %s, SKU %s, Box %s%n",
                        alloc.aisle, alloc.bay, alloc.orderId, alloc.sku, alloc.boxId);
                skippedCount++;
                continue; // Passa para a próxima alocação
            }
            // *** FIM DA VERIFICAÇÃO ***


            Item item = itemsMap.get(alloc.sku);
            if (item == null) {
                // Criar item placeholder se não encontrado (ou poderia lançar erro)
                System.out.printf("  ⚠️  Item não encontrado para SKU %s na alocação da Order %s. Usando peso padrão 1.0 kg.%n", alloc.sku, alloc.orderId);
                item = new Item(alloc.sku, "Unknown Product", "Unknown Category", "units", 1.0); // Usar peso padrão
            }

            // Aisle e Bay da alocação já foram validados como não nulos/vazios
            PickingAssignment assignment = new PickingAssignment(
                    alloc.orderId,
                    alloc.lineNo,
                    item,
                    alloc.qty,
                    alloc.boxId,
                    alloc.aisle.trim(), // Usar trim() para garantir
                    alloc.bay.trim()    // Usar trim() para garantir
            );
            assignments.add(assignment);
        }

        if (skippedCount > 0) {
            System.out.printf("  ℹ️  Total de %d alocações ignoradas por falta de localização.%n", skippedCount);
        }

        return assignments;
    }


    private String generatePlanId() {
        return "PLAN_" + System.currentTimeMillis();
    }

    // Método auxiliar para exportar para CSV (String)
    public String exportToCSV(PickingPlan plan) {
        StringBuilder csv = new StringBuilder();
        // Cabeçalho melhorado
        csv.append("PlanID,Heuristic,TrolleyCapacity,TrolleyID,TrolleyUtilization(%),OrderID,LineNo,SKU,ItemName,Quantity,BoxID,Aisle,Bay,Weight(kg)\n");

        if (plan == null || plan.getTrolleys().isEmpty()) {
            csv.append("N/A,N/A,N/A,N/A,N/A,N/A,N/A,N/A,N/A,N/A,N/A,N/A,N/A,N/A\n"); // Linha vazia ou placeholder
            return csv.toString();
        }

        for (Trolley trolley : plan.getTrolleys()) {
            for (PickingAssignment assignment : trolley.getAssignments()) {
                Item item = assignment.getItem(); // Obter o item do assignment
                csv.append(String.format("%s,%s,%.1f,%s,%.1f,%s,%d,%s,%s,%d,%s,%s,%s,%.2f\n",
                        plan.getId(),
                        plan.getHeuristic(),
                        plan.getTrolleyCapacity(),
                        trolley.getId(),
                        trolley.getUtilization(), // Adicionado utilização do trolley
                        assignment.getOrderId(),
                        assignment.getLineNo(),
                        assignment.getSku(),
                        (item != null ? item.getName().replace(",", ";") : "Unknown"), // Evitar vírgulas no nome
                        assignment.getQuantity(),
                        assignment.getBoxId(),
                        assignment.getAisle(), // Aisle já é String aqui
                        assignment.getBay(),   // Bay já é String aqui
                        assignment.getTotalWeight()
                ));
            }
        }
        return csv.toString();
    }

    // Método opcional para exportar para ficheiro CSV
    public void exportToCSVFile(PickingPlan plan, String filename) {
        String csvData = exportToCSV(plan);
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(csvData);
            System.out.printf("✅ Plano de picking exportado com sucesso para '%s'%n", filename);
        } catch (IOException e) {
            System.err.printf("❌ Erro ao exportar plano de picking para CSV '%s': %s%n", filename, e.getMessage());
        }
    }
}
