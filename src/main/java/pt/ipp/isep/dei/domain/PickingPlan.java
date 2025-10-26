package pt.ipp.isep.dei.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a picking plan for warehouse order fulfillment.
 */
public class PickingPlan {
    private final String id;
    private final List<Trolley> trolleys;
    private final HeuristicType heuristic;
    private final double trolleyCapacity;

    /**
     * Creates a new picking plan.
     */
    public PickingPlan(String id, HeuristicType heuristic, double trolleyCapacity) {
        this.id = id;
        this.heuristic = heuristic;
        this.trolleyCapacity = trolleyCapacity;
        this.trolleys = new ArrayList<>();
    }

    /** Adds a trolley to the picking plan. */
    public void addTrolley(Trolley trolley) {
        trolleys.add(trolley);
    }

    /** Returns the total number of trolleys in the plan. */
    public int getTotalTrolleys() { return trolleys.size(); }

    /** Returns the total weight of all trolleys. */
    public double getTotalWeight() {
        return trolleys.stream().mapToDouble(Trolley::getCurrentWeight).sum();
    }

    /** Returns the average utilization percentage of all trolleys. */
    public double getAverageUtilization() {
        return trolleys.stream()
                .mapToDouble(Trolley::getUtilization)
                .average()
                .orElse(0.0);
    }

    /** Returns a summary string of the picking plan. */
    public String getSummary() {
        return String.format("Picking Plan %s: %d trolleys, %.1f%% avg utilization, %.1f kg total",
                id, getTotalTrolleys(), getAverageUtilization(), getTotalWeight());
    }

    // Getters
    public String getId() { return id; }
    public List<Trolley> getTrolleys() { return new ArrayList<>(trolleys); }
    public HeuristicType getHeuristic() { return heuristic; }
    public double getTrolleyCapacity() { return trolleyCapacity; }

    /** Returns string representation of the picking plan. */
    @Override
    public String toString() {
        return getSummary();
    }
}