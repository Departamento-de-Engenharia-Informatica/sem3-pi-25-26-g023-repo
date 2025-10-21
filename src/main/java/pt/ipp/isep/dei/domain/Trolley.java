package pt.ipp.isep.dei.domain;

import java.util.ArrayList;
import java.util.List;

public class Trolley {
    private final String id;
    private final double maxCapacity;
    private double currentWeight;
    private final List<PickingAssignment> assignments;

    public Trolley(String id, double maxCapacity) {
        this.id = id;
        this.maxCapacity = maxCapacity;
        this.currentWeight = 0.0;
        this.assignments = new ArrayList<>();
    }

    public boolean addAssignment(PickingAssignment assignment) {
        if (currentWeight + assignment.getTotalWeight() <= maxCapacity) {
            assignments.add(assignment);
            currentWeight += assignment.getTotalWeight();
            assignment.setStatus(PickingStatus.ASSIGNED);
            return true;
        }
        return false;
    }

    public double getUtilization() {
        return maxCapacity > 0 ? (currentWeight / maxCapacity) * 100 : 0;
    }

    public double getRemainingCapacity() {
        return maxCapacity - currentWeight;
    }

    // Getters
    public String getId() { return id; }
    public double getMaxCapacity() { return maxCapacity; }
    public double getCurrentWeight() { return currentWeight; }
    public List<PickingAssignment> getAssignments() { return new ArrayList<>(assignments); }

    @Override
    public String toString() {
        return String.format("Trolley %s: %.1f/%.1f kg (%.1f%%) - %d items",
                id, currentWeight, maxCapacity, getUtilization(), assignments.size());
    }
}