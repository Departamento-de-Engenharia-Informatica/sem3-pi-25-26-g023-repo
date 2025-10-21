package pt.ipp.isep.dei.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PickingPlan {
    private final String id;
    private final LocalDateTime createdDate;
    private final List<Trolley> trolleys;
    private final HeuristicType heuristic;
    private final double trolleyCapacity;

    public PickingPlan(String id, HeuristicType heuristic, double trolleyCapacity) {
        this.id = id;
        this.createdDate = LocalDateTime.now();
        this.heuristic = heuristic;
        this.trolleyCapacity = trolleyCapacity;
        this.trolleys = new ArrayList<>();
    }

    public void addTrolley(Trolley trolley) {
        trolleys.add(trolley);
    }

    public int getTotalTrolleys() { return trolleys.size(); }

    public double getTotalWeight() {
        return trolleys.stream().mapToDouble(Trolley::getCurrentWeight).sum();
    }

    public double getAverageUtilization() {
        return trolleys.stream()
                .mapToDouble(Trolley::getUtilization)
                .average()
                .orElse(0.0);
    }

    public String getSummary() {
        return String.format("Picking Plan %s: %d trolleys, %.1f%% avg utilization, %.1f kg total",
                id, getTotalTrolleys(), getAverageUtilization(), getTotalWeight());
    }

    // Getters
    public String getId() { return id; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public List<Trolley> getTrolleys() { return new ArrayList<>(trolleys); }
    public HeuristicType getHeuristic() { return heuristic; }
    public double getTrolleyCapacity() { return trolleyCapacity; }

    @Override
    public String toString() {
        return getSummary();
    }
}
