package pt.ipp.isep.dei.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a picking plan for warehouse order fulfillment.
 * (Version 2.1 - Fixed String.format bug)
 */
public class PickingPlan {

    // --- ANSI Color Codes (Only the necessary ones) ---
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m"; // For utilization > 90%
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_BOLD = "\u001B[1m";

    private final String id;
    private final List<Trolley> trolleys;
    private final HeuristicType heuristic;
    private final double trolleyCapacity;

    /**
     * Creates a new picking plan.
     *
     * @param id The unique identifier of the plan.
     * @param heuristic The heuristic used to generate the plan.
     * @param trolleyCapacity The maximum capacity (weight) of each trolley.
     */
    public PickingPlan(String id, HeuristicType heuristic, double trolleyCapacity) {
        this.id = id;
        this.heuristic = heuristic;
        this.trolleyCapacity = trolleyCapacity;
        this.trolleys = new ArrayList<>();
    }

    /** Adds a trolley to the picking plan.
     * @param trolley The trolley to add.
     */
    public void addTrolley(Trolley trolley) {
        trolleys.add(trolley);
    }

    /**
     * Returns the total number of trolleys in the plan.
     * @return The number of trolleys.
     */
    public int getTotalTrolleys() { return trolleys.size(); }

    /**
     * Returns the total weight of all trolleys combined.
     * @return The total weight in kilograms.
     */
    public double getTotalWeight() {
        return trolleys.stream().mapToDouble(Trolley::getCurrentWeight).sum();
    }

    /**
     * Returns the average utilization percentage of all trolleys.
     * @return The average utilization percentage (0.0 to 100.0).
     */
    public double getAverageUtilization() {
        return trolleys.stream()
                .mapToDouble(Trolley::getUtilization)
                .average()
                .orElse(0.0);
    }

    // -----------------------------------------------------------------
    // --- CORRECTION (Line 75 - removed the extra %s) ---
    // -----------------------------------------------------------------
    /**
     * Returns a "pretty" summary string of the picking plan, including ANSI color coding.
     *
     * <p>Utilization is colored: Red (> 90%), Yellow (> 75%), Green (otherwise).</p>
     * @return The formatted summary string.
     */
    public String getSummary() {
        // Formats the utilization with colors (Red if > 90%, Yellow if > 75%, Green otherwise)
        double avgUtil = getAverageUtilization();
        String utilColor = (avgUtil > 90) ? ANSI_RED : (avgUtil > 75 ? ANSI_YELLOW : ANSI_GREEN);

        // The extra %s was removed from the last line
        return String.format(
                "  Plan ID: %s%s%s\n" +
                        "  Heuristic Used: %s%s%s\n" +
                        "  Trolley Capacity: %s%.2f kg%s\n" +
                        "  " + ANSI_BOLD + "--------------------------------------\n" + ANSI_RESET +
                        "  Total Trolleys: %s%d%s\n" +
                        "  Total Weight: %s%.2f kg%s\n" +
                        "  Avg. Utilization: %s%.1f%%%s", // <-- CORRECTED LINE
                ANSI_BOLD, id, ANSI_RESET,
                ANSI_CYAN, heuristic, ANSI_RESET,
                ANSI_YELLOW, trolleyCapacity, ANSI_RESET,
                ANSI_BOLD + ANSI_CYAN, getTotalTrolleys(), ANSI_RESET,
                ANSI_BOLD, getTotalWeight(), ANSI_RESET,
                ANSI_BOLD + utilColor, avgUtil, ANSI_RESET // <-- Arguments now correspond
        );
    }

    // Getters
    /** Returns the unique ID of the plan. */
    public String getId() { return id; }
    /** Returns a copy of the list of trolleys in the plan. */
    public List<Trolley> getTrolleys() { return new ArrayList<>(trolleys); }
    /** Returns the heuristic used. */
    public HeuristicType getHeuristic() { return heuristic; }
    /** Returns the capacity of each trolley. */
    public double getTrolleyCapacity() { return trolleyCapacity; }

    /**
     * Returns string representation of the picking plan by calling the summary method.
     * @return The formatted summary string.
     */
    @Override
    public String toString() {
        return getSummary(); // Calls the "pretty" method
    }
}