package pt.ipp.isep.dei.domain;

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

        // Converter allocations para picking assignments
        List<PickingAssignment> assignments = convertToAssignments(allocations);

        // Usar factory para obter a heurística
        PackingHeuristic packingHeuristic = PackingHeuristicFactory.createHeuristic(heuristic);

        // Aplicar heurística
        List<Trolley> trolleys = packingHeuristic.packItems(assignments, trolleyCapacity);

        // Criar picking plan
        PickingPlan plan = new PickingPlan(generatePlanId(), heuristic, trolleyCapacity);
        trolleys.forEach(plan::addTrolley);

        return plan;
    }

    private List<PickingAssignment> convertToAssignments(List<Allocation> allocations) {
        List<PickingAssignment> assignments = new ArrayList<>();

        for (Allocation alloc : allocations) {
            Item item = itemsMap.get(alloc.sku);
            if (item == null) {
                // Criar item placeholder se não encontrado
                item = new Item(alloc.sku, "Unknown Product", "Unknown Category", "units", 1.0);
            }

            PickingAssignment assignment = new PickingAssignment(
                    alloc.orderId,
                    alloc.lineNo,
                    item,
                    alloc.qty,
                    alloc.boxId,
                    alloc.aisle,
                    alloc.bay
            );
            assignments.add(assignment);
        }
        return assignments;
    }

    private String generatePlanId() {
        return "PLAN_" + System.currentTimeMillis();
    }

    // Método auxiliar para exportar para CSV
    public String exportToCSV(PickingPlan plan) {
        StringBuilder csv = new StringBuilder();
        csv.append("TrolleyID,OrderID,LineNo,SKU,Quantity,BoxID,Location,Weight\n");

        for (Trolley trolley : plan.getTrolleys()) {
            for (PickingAssignment assignment : trolley.getAssignments()) {
                csv.append(String.format("%s,%s,%d,%s,%d,%s,%s,%.1f\n",
                        trolley.getId(),
                        assignment.getOrderId(),
                        assignment.getLineNo(),
                        assignment.getSku(),
                        assignment.getQuantity(),
                        assignment.getBoxId(),
                        assignment.getLocation(),
                        assignment.getTotalWeight()
                ));
            }
        }
        return csv.toString();
    }
}