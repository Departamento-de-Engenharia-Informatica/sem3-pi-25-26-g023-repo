package pt.ipp.isep.dei.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a trolley used for picking items in the warehouse.
 * Manages capacity and assigned picking assignments.
 */
public class Trolley {
    private final String id;
    private final double maxCapacity;
    private double currentWeight;
    private final List<PickingAssignment> assignments;

    /**
     * Creates a new trolley with specified capacity.
     * @param id unique identifier for the trolley
     * @param maxCapacity maximum weight capacity in kilograms
     */
    public Trolley(String id, double maxCapacity) {
        this.id = id;
        this.maxCapacity = maxCapacity;
        this.currentWeight = 0.0;
        this.assignments = new ArrayList<>();
    }

    /**
     * Attempts to add a picking assignment to the trolley.
     * Only adds if the assignment fits within remaining capacity.
     * @param assignment the picking assignment to add
     * @return true if assignment was added successfully, false if it exceeds capacity
     */
    public boolean addAssignment(PickingAssignment assignment) {
        if (currentWeight + assignment.getTotalWeight() <= maxCapacity) {
            assignments.add(assignment);
            currentWeight += assignment.getTotalWeight();
            assignment.setStatus(PickingStatus.ASSIGNED);
            return true;
        }
        return false;
    }

    /**
     * Calculates the current utilization percentage of the trolley.
     * @return utilization percentage (0-100)
     */
    public double getUtilization() {
        return maxCapacity > 0 ? (currentWeight / maxCapacity) * 100 : 0;
    }

    /**
     * Calculates the remaining weight capacity.
     * @return remaining capacity in kilograms
     */
    public double getRemainingCapacity() {
        return maxCapacity - currentWeight;
    }

    // Getters
    public String getId() { return id; }
    public double getMaxCapacity() { return maxCapacity; }
    public double getCurrentWeight() { return currentWeight; }
    public List<PickingAssignment> getAssignments() { return new ArrayList<>(assignments); }

    /**
     * @return string representation of the trolley status
     */
    @Override
    public String toString() {
        return String.format("Trolley %s: %.1f/%.1f kg (%.1f%%) - %d items",
                id, currentWeight, maxCapacity, getUtilization(), assignments.size());
    }
}