package pt.ipp.isep.dei.domain;

import java.util.*;

public class PickingService {

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
            Item item = new Item(alloc.sku, "Product", "Category", "units", alloc.weight / alloc.qty);
            PickingAssignment assignment = new PickingAssignment(
                    alloc.orderId,   // ← campo público
                    alloc.lineNo,    // ← campo público
                    item,
                    alloc.qty,  // ← campo público
                    alloc.boxId,     // ← campo público
                    alloc.aisle,     // ← campo público
                    alloc.bay        // ← campo público
            );
            assignments.add(assignment);
        }
        return assignments;
    }

    private List<Trolley> applyHeuristic(List<PickingAssignment> assignments,
                                         double capacity, HeuristicType heuristic) {

        switch (heuristic) {
            case FIRST_FIT:
                return firstFit(assignments, capacity);
            case FIRST_FIT_DECREASING:
                return firstFitDecreasing(assignments, capacity);
            case BEST_FIT_DECREASING:
                return bestFitDecreasing(assignments, capacity);
            default:
                return firstFit(assignments, capacity);
        }
    }

    private List<Trolley> firstFit(List<PickingAssignment> assignments, double capacity) {
        List<Trolley> trolleys = new ArrayList<>();
        trolleys.add(new Trolley("T1", capacity));

        for (PickingAssignment assignment : assignments) {
            boolean added = false;
            for (Trolley trolley : trolleys) {
                if (trolley.addAssignment(assignment)) {
                    added = true;
                    break;
                }
            }
            if (!added) {
                Trolley newTrolley = new Trolley("T" + (trolleys.size() + 1), capacity);
                newTrolley.addAssignment(assignment);
                trolleys.add(newTrolley);
            }
        }
        return trolleys;
    }

    private List<Trolley> firstFitDecreasing(List<PickingAssignment> assignments, double capacity) {
        List<PickingAssignment> sorted = new ArrayList<>(assignments);
        sorted.sort((a1, a2) -> Double.compare(a2.getTotalWeight(), a1.getTotalWeight()));
        return firstFit(sorted, capacity);
    }

    private List<Trolley> bestFitDecreasing(List<PickingAssignment> assignments, double capacity) {
        List<PickingAssignment> sorted = new ArrayList<>(assignments);
        sorted.sort((a1, a2) -> Double.compare(a2.getTotalWeight(), a1.getTotalWeight()));

        List<Trolley> trolleys = new ArrayList<>();

        for (PickingAssignment assignment : sorted) {
            Trolley bestTrolley = null;
            double minRemaining = Double.MAX_VALUE;

            for (Trolley trolley : trolleys) {
                double remaining = trolley.getRemainingCapacity() - assignment.getTotalWeight();
                if (remaining >= 0 && remaining < minRemaining) {
                    minRemaining = remaining;
                    bestTrolley = trolley;
                }
            }

            if (bestTrolley != null) {
                bestTrolley.addAssignment(assignment);
            } else {
                Trolley newTrolley = new Trolley("T" + (trolleys.size() + 1), capacity);
                newTrolley.addAssignment(assignment);
                trolleys.add(newTrolley);
            }
        }
        return trolleys;
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