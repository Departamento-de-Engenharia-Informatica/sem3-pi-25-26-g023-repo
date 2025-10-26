package pt.ipp.isep.dei.domain;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Service for generating and managing picking plans.
 */
public class PickingService {

    private Map<String, Item> itemsMap;

    /** Creates a new picking service. */
    public PickingService() {

        this.itemsMap = new HashMap<>();
    }

    /** Sets the items map for SKU lookup. */
    public void setItemsMap(Map<String, Item> itemsMap) {
        this.itemsMap = itemsMap != null ? itemsMap : new HashMap<>();
    }

    /**
     * Generates a picking plan based on allocations and heuristic.
     */
    public PickingPlan generatePickingPlan(List<Allocation> allocations,
                                           double trolleyCapacity,
                                           HeuristicType heuristic) {

        if (allocations == null || allocations.isEmpty()) {
            System.out.println("⚠️  No allocations provided for picking plan.");
            return new PickingPlan("EMPTY_PLAN_" + System.currentTimeMillis(), heuristic, trolleyCapacity);
        }

        List<PickingAssignment> assignments = convertToAssignments(allocations);

        if (assignments.isEmpty()) {
            System.out.println("⚠️  No valid assignments with location found.");
            return new PickingPlan("NO_VALID_ASSIGNMENTS_" + System.currentTimeMillis(), heuristic, trolleyCapacity);
        }

        System.out.printf("  ➡️  Converted %d allocations to %d valid assignments.%n",
                allocations.size(), assignments.size());

        PackingHeuristic packingHeuristic = PackingHeuristicFactory.createHeuristic(heuristic);
        List<Trolley> trolleys = packingHeuristic.packItems(assignments, trolleyCapacity);

        PickingPlan plan = new PickingPlan(generatePlanId(), heuristic, trolleyCapacity);
        trolleys.forEach(plan::addTrolley);

        return plan;
    }

    /** Converts allocations to picking assignments, filtering invalid locations. */
    private List<PickingAssignment> convertToAssignments(List<Allocation> allocations) {
        List<PickingAssignment> assignments = new ArrayList<>();
        int skippedCount = 0;

        for (Allocation alloc : allocations) {
            if (alloc.aisle == null || alloc.bay == null || alloc.aisle.trim().isEmpty() || alloc.bay.trim().isEmpty()) {
                System.out.printf("  ⚠️  Skipping allocation with missing location for Order %s, SKU %s%n",
                        alloc.orderId, alloc.sku);
                skippedCount++;
                continue;
            }

            Item item = itemsMap.get(alloc.sku);
            if (item == null) {
                System.out.printf("  ⚠️  Item not found for SKU %s. Using default weight.%n", alloc.sku);
                item = new Item(alloc.sku, "Unknown Product", "Unknown Category", "units", 1.0);
            }

            PickingAssignment assignment = new PickingAssignment(
                    alloc.orderId,
                    alloc.lineNo,
                    item,
                    alloc.qty,
                    alloc.boxId,
                    alloc.aisle.trim(),
                    alloc.bay.trim()
            );
            assignments.add(assignment);
        }

        if (skippedCount > 0) {
            System.out.printf("  ℹ️  Total %d allocations skipped due to missing location.%n", skippedCount);
        }

        return assignments;
    }

    /** Generates a unique plan ID. */
    private String generatePlanId() {
        return "PLAN_" + System.currentTimeMillis();
    }

    /** Exports picking plan to CSV string. */
    public String exportToCSV(PickingPlan plan) {
        StringBuilder csv = new StringBuilder();
        csv.append("PlanID,Heuristic,TrolleyCapacity,TrolleyID,TrolleyUtilization(%),OrderID,LineNo,SKU,ItemName,Quantity,BoxID,Aisle,Bay,Weight(kg)\n");

        if (plan == null || plan.getTrolleys().isEmpty()) {
            csv.append("N/A,N/A,N/A,N/A,N/A,N/A,N/A,N/A,N/A,N/A,N/A,N/A,N/A,N/A\n");
            return csv.toString();
        }

        for (Trolley trolley : plan.getTrolleys()) {
            for (PickingAssignment assignment : trolley.getAssignments()) {
                Item item = assignment.getItem();
                csv.append(String.format("%s,%s,%.1f,%s,%.1f,%s,%d,%s,%s,%d,%s,%s,%s,%.2f\n",
                        plan.getId(),
                        plan.getHeuristic(),
                        plan.getTrolleyCapacity(),
                        trolley.getId(),
                        trolley.getUtilization(),
                        assignment.getOrderId(),
                        assignment.getLineNo(),
                        assignment.getSku(),
                        (item != null ? item.getName().replace(",", ";") : "Unknown"),
                        assignment.getQuantity(),
                        assignment.getBoxId(),
                        assignment.getAisle(),
                        assignment.getBay(),
                        assignment.getTotalWeight()
                ));
            }
        }
        return csv.toString();
    }

    /** Exports picking plan to CSV file. */
    public void exportToCSVFile(PickingPlan plan, String filename) {
        String csvData = exportToCSV(plan);
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(csvData);
            System.out.printf("✅ Picking plan exported to '%s'%n", filename);
        } catch (IOException e) {
            System.err.printf("❌ Error exporting picking plan to CSV '%s': %s%n", filename, e.getMessage());
        }
    }
}